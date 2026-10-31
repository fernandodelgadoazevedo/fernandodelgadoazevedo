package com.fernandoazevedo.pos5820;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

/** Comandos TSPL (modo etiqueta com sensor de gap / marca preta). */
public final class Tspl {
    private Tspl() {
    }

    private static final Charset ASCII = Charset.forName("US-ASCII");

    private static String mm(float v) {
        if (v == Math.rint(v)) return String.valueOf((int) v);
        return String.format(Locale.US, "%.1f", v);
    }

    private static String setup(PrinterSettings s) {
        StringBuilder b = new StringBuilder();
        b.append("SIZE ").append(s.labelW).append(" mm,").append(s.labelH).append(" mm\r\n");
        switch (s.media) {
            case PrinterSettings.MEDIA_BLINE:
                b.append("BLINE ").append(mm(s.gap)).append(" mm,0 mm\r\n");
                break;
            case PrinterSettings.MEDIA_CONTINUOUS:
                b.append("GAP 0,0\r\n");
                break;
            default:
                b.append("GAP ").append(mm(s.gap)).append(" mm,0 mm\r\n");
        }
        b.append("DENSITY ").append(Math.max(0, Math.min(15, s.density))).append("\r\n");
        b.append("DIRECTION 1\r\n");
        b.append("REFERENCE 0,0\r\n");
        return b.toString();
    }

    private static void write(ByteArrayOutputStream o, String s) {
        byte[] b = s.getBytes(ASCII);
        o.write(b, 0, b.length);
    }

    /** Uma etiqueta por imagem. No TSPL o bit 0 imprime (preto), por isso os bits são invertidos. */
    public static byte[] printJob(PrinterSettings s, java.util.List<MonoBitmap> labels, int copies) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        write(o, setup(s));
        for (MonoBitmap m : labels) {
            write(o, "CLS\r\n");
            write(o, "BITMAP 0,0," + m.widthBytes + "," + m.height + ",0,");
            byte[] d = m.data.clone();
            if (!s.invert) {
                for (int i = 0; i < d.length; i++) d[i] = (byte) ~d[i];
            }
            o.write(d, 0, d.length);
            write(o, "\r\nPRINT 1," + Math.max(1, copies) + "\r\n");
        }
        return o.toByteArray();
    }

    public static byte[] feedCommand(PrinterSettings s) {
        return (setup(s) + "FORMFEED\r\n").getBytes(ASCII);
    }

    public static byte[] calibrate(PrinterSettings s) {
        String cmd = s.media == PrinterSettings.MEDIA_BLINE ? "BLINEDETECT\r\n" : "GAPDETECT\r\n";
        return (setup(s) + cmd).getBytes(ASCII);
    }

    public static byte[] testLabel(PrinterSettings s) {
        int w = s.labelWidthDots(), h = s.labelHeightDots();
        String b = setup(s)
                + "CLS\r\n"
                + "BOX 4,4," + (w - 4) + "," + (h - 4) + ",3\r\n"
                + "TEXT 20,20,\"3\",0,1,1,\"POS-5820\"\r\n"
                + "TEXT 20,70,\"2\",0,1,1,\"Teste TSPL\"\r\n"
                + "TEXT 20,110,\"2\",0,1,1,\"" + s.labelW + "x" + s.labelH + " mm\"\r\n"
                + "PRINT 1,1\r\n";
        return b.getBytes(ASCII);
    }
}
