package com.fernandoazevedo.pos5820;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;

/** Comandos ESC/POS (modo recibo da POS-5820). */
public final class EscPos {
    private EscPos() {
    }

    public static final byte[] INIT = {0x1B, 0x40};
    /** GS FF: avança até o início da próxima etiqueta (usa o sensor). */
    public static final byte[] NEXT_LABEL = {0x1D, 0x0C};

    public static byte[] feed(int lines) {
        return new byte[]{0x1B, 0x64, (byte) lines};
    }

    /** GS v 0: imagem raster, enviada em faixas para não estourar o buffer da impressora. */
    public static void raster(ByteArrayOutputStream o, MonoBitmap m) {
        int wb = m.widthBytes;
        final int band = 128;
        for (int y = 0; y < m.height; y += band) {
            int h = Math.min(band, m.height - y);
            o.write(0x1D);
            o.write(0x76);
            o.write(0x30);
            o.write(0x00);
            o.write(wb & 0xFF);
            o.write((wb >> 8) & 0xFF);
            o.write(h & 0xFF);
            o.write((h >> 8) & 0xFF);
            o.write(m.data, y * wb, h * wb);
        }
    }

    private static void finish(ByteArrayOutputStream o, PrinterSettings s) {
        if (s.nextLabel) o.write(NEXT_LABEL, 0, NEXT_LABEL.length);
        else {
            byte[] f = feed(4);
            o.write(f, 0, f.length);
        }
    }

    public static byte[] printJob(PrinterSettings s, List<MonoBitmap> pages, int copies) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (int c = 0; c < copies; c++) {
            o.write(INIT, 0, INIT.length);
            for (MonoBitmap m : pages) raster(o, m);
            finish(o, s);
        }
        return o.toByteArray();
    }

    public static byte[] feedCommand(PrinterSettings s) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        finish(o, s);
        return o.toByteArray();
    }

    public static byte[] testText() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        Charset a = Charset.forName("US-ASCII");
        byte[][] parts = {
                INIT,
                {0x1B, 0x61, 0x01},           // centralizar
                {0x1D, 0x21, 0x11},           // dupla altura e largura
                "POS-5820\n".getBytes(a),
                {0x1D, 0x21, 0x00},
                {0x1B, 0x45, 0x01},           // negrito
                "Teste de impressao\n".getBytes(a),
                {0x1B, 0x45, 0x00},
                {0x1B, 0x61, 0x00},           // esquerda
                "Protocolo: ESC/POS\n".getBytes(a),
                "Papel: 58mm (384 pontos)\n".getBytes(a),
                "0123456789ABCDEFGHIJKLMNOPQRSTUV\n".getBytes(a),
                "--------------------------------\n".getBytes(a),
        };
        for (byte[] p : parts) o.write(p, 0, p.length);
        return o.toByteArray();
    }
}
