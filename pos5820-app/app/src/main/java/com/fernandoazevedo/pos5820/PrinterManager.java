package com.fernandoazevedo.pos5820;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/** Conexão Bluetooth clássica (SPP/RFCOMM) compartilhada entre o app e o serviço de impressão. */
public final class PrinterManager {
    private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final PrinterManager INSTANCE = new PrinterManager();
    private static final int CHUNK = 256;

    private BluetoothSocket socket;
    private OutputStream out;
    private String address;

    public static PrinterManager get() {
        return INSTANCE;
    }

    public synchronized boolean isConnected() {
        return socket != null && out != null;
    }

    public synchronized String getAddress() {
        return address;
    }

    @SuppressLint("MissingPermission")
    public synchronized void connect(String addr) throws IOException {
        if (isConnected() && addr.equals(address)) return;
        disconnect();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) throw new IOException("Bluetooth não disponível");
        if (!adapter.isEnabled()) throw new IOException("Bluetooth desligado");
        try {
            adapter.cancelDiscovery();
        } catch (SecurityException ignored) {
        }
        BluetoothDevice dev = adapter.getRemoteDevice(addr);
        BluetoothSocket s;
        try {
            s = dev.createRfcommSocketToServiceRecord(SPP);
            s.connect();
        } catch (IOException e1) {
            try {
                s = dev.createInsecureRfcommSocketToServiceRecord(SPP);
                s.connect();
            } catch (IOException e2) {
                // Alguns clones só respondem no canal 1
                try {
                    Method m = dev.getClass().getMethod("createRfcommSocket", int.class);
                    s = (BluetoothSocket) m.invoke(dev, 1);
                    s.connect();
                } catch (Exception e3) {
                    throw new IOException("Não foi possível conectar à impressora. Ela está ligada e pareada? (" + e1.getMessage() + ")");
                }
            }
        }
        socket = s;
        out = s.getOutputStream();
        address = addr;
    }

    public synchronized void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        socket = null;
        out = null;
    }

    /** Envia os dados; se o socket estiver "morto", reconecta e tenta mais uma vez. */
    public synchronized void send(String addr, byte[] data) throws IOException {
        if (addr == null || addr.isEmpty()) throw new IOException("Nenhuma impressora selecionada");
        try {
            connect(addr);
            write(data);
        } catch (IOException e) {
            disconnect();
            connect(addr);
            write(data);
        }
    }

    private void write(byte[] data) throws IOException {
        for (int off = 0; off < data.length; off += CHUNK) {
            int len = Math.min(CHUNK, data.length - off);
            out.write(data, off, len);
            out.flush();
            try {
                Thread.sleep(8);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("Impressão interrompida");
            }
        }
    }
}
