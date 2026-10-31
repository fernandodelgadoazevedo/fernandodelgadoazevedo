package com.fernandoazevedo.pos5820;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Converte conteúdo em comandos ESC/POS ou TSPL e envia para a impressora. */
public final class PrintEngine {
    private PrintEngine() {
    }

    public static boolean isLabel(PrinterSettings s) {
        return s.protocol == PrinterSettings.PROTO_TSPL;
    }

    public static Bitmap render(LabelRenderer.Content c, PrinterSettings s) throws Exception {
        if (isLabel(s)) return LabelRenderer.render(c, s.labelWidthDots(), s.labelHeightDots());
        return LabelRenderer.render(c, s.printWidth, 0);
    }

    public static void printContent(LabelRenderer.Content c, PrinterSettings s, int copies) throws Exception {
        printBitmaps(Collections.singletonList(render(c, s)), s, copies);
    }

    public static void printBitmaps(List<Bitmap> pages, PrinterSettings s, int copies) throws IOException {
        List<MonoBitmap> mono = new ArrayList<>();
        for (Bitmap b : pages) mono.add(ImageUtils.toMono(b, false));
        int n = Math.max(1, copies);
        byte[] job = isLabel(s) ? Tspl.printJob(s, mono, n) : EscPos.printJob(s, mono, n);
        PrinterManager.get().send(s.address, job);
    }

    public static void testPage(PrinterSettings s) throws Exception {
        if (isLabel(s)) {
            PrinterManager.get().send(s.address, Tspl.testLabel(s));
            return;
        }
        PrinterManager.get().send(s.address, EscPos.testText());
        LabelRenderer.Content c = new LabelRenderer.Content();
        c.text = "Modo imagem OK\nAcentuação: ção ÁÉÍÓÚ ãõ ç";
        c.align = android.text.Layout.Alignment.ALIGN_CENTER;
        c.qr = "POS-5820";
        printContent(c, s, 1);
    }

    public static void feed(PrinterSettings s) throws IOException {
        PrinterManager.get().send(s.address, isLabel(s) ? Tspl.feedCommand(s) : EscPos.feedCommand(s));
    }

    /** Renderiza as páginas de um PDF (vindo do sistema de impressão do Android). */
    public static List<Bitmap> renderPdf(File pdf, PrinterSettings s) throws IOException {
        List<Bitmap> out = new ArrayList<>();
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY);
        PdfRenderer r = new PdfRenderer(pfd);
        try {
            for (int i = 0; i < r.getPageCount(); i++) {
                PdfRenderer.Page p = r.openPage(i);
                int pw = p.getWidth(), ph = p.getHeight();
                int bw, bh;
                if (isLabel(s)) {
                    float sc = Math.min(s.labelWidthDots() / (float) pw, s.labelHeightDots() / (float) ph);
                    bw = Math.max(1, Math.round(pw * sc));
                    bh = Math.max(1, Math.round(ph * sc));
                } else {
                    bw = s.printWidth;
                    bh = Math.max(1, Math.round(ph * (bw / (float) pw)));
                }
                Bitmap b = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
                b.eraseColor(Color.WHITE);
                p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                p.close();
                Bitmap bwImg = ImageUtils.ditherScaled(b, bw);
                b.recycle();
                if (isLabel(s)) {
                    Bitmap label = Bitmap.createBitmap(s.labelWidthDots(), s.labelHeightDots(), Bitmap.Config.ARGB_8888);
                    Canvas cv = new Canvas(label);
                    cv.drawColor(Color.WHITE);
                    cv.drawBitmap(bwImg, (label.getWidth() - bw) / 2f, (label.getHeight() - bh) / 2f, new Paint());
                    out.add(label);
                } else {
                    out.add(ImageUtils.trimBottom(bwImg, 16));
                }
            }
        } finally {
            r.close();
            pfd.close();
        }
        return out;
    }
}
