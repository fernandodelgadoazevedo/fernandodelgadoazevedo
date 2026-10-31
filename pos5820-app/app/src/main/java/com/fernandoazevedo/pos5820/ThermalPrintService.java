package com.fernandoazevedo.pos5820;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * "Driver" do Android: depois de ativado em Configurações > Impressão, a POS-5820 aparece
 * no menu Imprimir de qualquer app (PDF, navegador, galeria...).
 */
public class ThermalPrintService extends PrintService {
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        return new Session();
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        printJob.cancel();
    }

    @Override
    protected void onPrintJobQueued(final PrintJob job) {
        if (!job.isQueued()) return;
        job.start();
        final ParcelFileDescriptor data = job.getDocument().getData();
        if (data == null) {
            job.fail("Documento vazio");
            return;
        }
        final PrinterSettings s = PrinterSettings.load(this);
        s.address = job.getInfo().getPrinterId().getLocalId();
        final int copies = Math.max(1, job.getInfo().getCopies());
        final File cache = getCacheDir();
        exec.execute(new Runnable() {
            @Override
            public void run() {
                String err = null;
                File tmp = null;
                try {
                    tmp = File.createTempFile("job", ".pdf", cache);
                    InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(data);
                    OutputStream os = new FileOutputStream(tmp);
                    try {
                        byte[] buf = new byte[8192];
                        int n;
                        while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
                    } finally {
                        in.close();
                        os.close();
                    }
                    List<Bitmap> pages = PrintEngine.renderPdf(tmp, s);
                    PrintEngine.printBitmaps(pages, s, copies);
                } catch (Throwable t) {
                    err = t.getMessage() != null ? t.getMessage() : t.toString();
                } finally {
                    if (tmp != null) //noinspection ResultOfMethodCallIgnored
                        tmp.delete();
                }
                final String fe = err;
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        if (fe == null) job.complete();
                        else job.fail(fe);
                    }
                });
            }
        });
    }

    private static int mils(int mm) {
        return Math.round(mm * 1000f / 25.4f);
    }

    @SuppressLint("MissingPermission")
    private List<PrinterInfo> buildPrinters() {
        List<PrinterInfo> out = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31
                && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return out;
        }
        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        if (a == null) return out;
        Set<BluetoothDevice> bonded;
        try {
            bonded = a.getBondedDevices();
        } catch (SecurityException e) {
            return out;
        }
        if (bonded == null) return out;

        PrinterSettings s = PrinterSettings.load(this);
        List<BluetoothDevice> chosen = new ArrayList<>();
        for (BluetoothDevice d : bonded) {
            if (looksLikePrinter(d, s)) chosen.add(d);
        }
        if (chosen.isEmpty()) chosen.addAll(bonded);

        boolean label = PrintEngine.isLabel(s);
        PrintAttributes.MediaSize roll = new PrintAttributes.MediaSize(
                "roll58", "Bobina 58mm", mils(48), mils(200));
        PrintAttributes.MediaSize labelSize = new PrintAttributes.MediaSize(
                "label_" + s.labelW + "x" + s.labelH,
                String.format(Locale.US, "Etiqueta %dx%d mm", s.labelW, s.labelH),
                mils(s.labelW), mils(s.labelH));

        for (BluetoothDevice d : chosen) {
            PrinterId id = generatePrinterId(d.getAddress());
            String name = d.getName() != null ? d.getName() : d.getAddress();
            PrinterCapabilitiesInfo caps = new PrinterCapabilitiesInfo.Builder(id)
                    .addMediaSize(roll, !label)
                    .addMediaSize(labelSize, label)
                    .addMediaSize(PrintAttributes.MediaSize.ISO_A4, false)
                    .addResolution(new PrintAttributes.Resolution("r203", "203 dpi", 203, 203), true)
                    .setColorModes(PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_MONOCHROME)
                    .setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0))
                    .build();
            out.add(new PrinterInfo.Builder(id, name, PrinterInfo.STATUS_IDLE)
                    .setCapabilities(caps)
                    .build());
        }
        return out;
    }

    @SuppressLint("MissingPermission")
    private static boolean looksLikePrinter(BluetoothDevice d, PrinterSettings s) {
        if (d.getAddress().equals(s.address)) return true;
        BluetoothClass c = d.getBluetoothClass();
        if (c != null && c.getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) return true;
        String n = d.getName() == null ? "" : d.getName().toUpperCase(Locale.US);
        return n.contains("POS") || n.contains("PRINT") || n.contains("58") || n.contains("MPT") || n.contains("PT-");
    }

    private class Session extends PrinterDiscoverySession {
        @Override
        public void onStartPrinterDiscovery(List<PrinterId> priorityList) {
            addPrinters(buildPrinters());
        }

        @Override
        public void onStopPrinterDiscovery() {
        }

        @Override
        public void onValidatePrinters(List<PrinterId> printerIds) {
        }

        @Override
        public void onStartPrinterStateTracking(PrinterId printerId) {
        }

        @Override
        public void onStopPrinterStateTracking(PrinterId printerId) {
        }

        @Override
        public void onDestroy() {
        }
    }
}
