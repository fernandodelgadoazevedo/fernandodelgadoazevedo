package com.fernandoazevedo.pos5820;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * Desenha o conteúdo (imagem, texto, QR, código de barras) num bitmap do tamanho da
 * área de impressão. Tudo é impresso como imagem, então acentos e qualquer fonte funcionam.
 */
public final class LabelRenderer {
    private LabelRenderer() {
    }

    public static class Content {
        public String text = "";
        public float textSize = 26f;
        public Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
        public boolean bold = false;
        public String qr = "";
        public String barcode = "";
        public Bitmap image;

        public boolean isEmpty() {
            return text.trim().isEmpty() && qr.trim().isEmpty() && barcode.trim().isEmpty() && image == null;
        }
    }

    private static final int MARGIN = 8;
    private static final int SPACING = 12;
    private static final int BARCODE_HEIGHT = 80;
    private static final float BARCODE_TEXT = 22f;

    /** @param fixedHeight 0 = altura livre (bobina); >0 = etiqueta, o conteúdo é reduzido para caber. */
    @SuppressWarnings("deprecation")
    public static Bitmap render(Content c, int width, int fixedHeight) throws Exception {
        int inner = Math.max(8, width - 2 * MARGIN);

        Bitmap img = c.image != null ? ImageUtils.ditherScaled(c.image, inner) : null;

        StaticLayout text = null;
        if (!c.text.trim().isEmpty()) {
            TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            tp.setColor(Color.BLACK);
            tp.setTextSize(c.textSize);
            tp.setTypeface(c.bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            text = new StaticLayout(c.text, tp, inner, c.align, 1f, 0f, false);
        }

        Bitmap qr = !c.qr.trim().isEmpty() ? Codes.qr(c.qr.trim(), Math.min(inner, 280)) : null;
        Bitmap bar = !c.barcode.trim().isEmpty() ? Codes.code128(c.barcode.trim(), inner, BARCODE_HEIGHT) : null;

        int contentH = 0;
        int blocks = 0;
        if (img != null) { contentH += img.getHeight(); blocks++; }
        if (text != null) { contentH += text.getHeight(); blocks++; }
        if (qr != null) { contentH += qr.getHeight(); blocks++; }
        if (bar != null) { contentH += bar.getHeight() + (int) (BARCODE_TEXT + 6); blocks++; }
        contentH += Math.max(0, blocks - 1) * SPACING;
        contentH = Math.max(1, contentH);

        int h = fixedHeight > 0 ? fixedHeight : contentH + 2 * MARGIN;
        Bitmap out = Bitmap.createBitmap(width, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(out);
        cv.drawColor(Color.WHITE);

        float scale = 1f;
        float offY = MARGIN;
        if (fixedHeight > 0) {
            int avail = Math.max(1, h - 2 * MARGIN);
            if (contentH > avail) scale = avail / (float) contentH;
            offY = MARGIN + (avail - contentH * scale) / 2f;
        }
        cv.save();
        cv.translate(MARGIN + inner * (1f - scale) / 2f, offY);
        cv.scale(scale, scale);

        Paint plain = new Paint();
        plain.setFilterBitmap(false);
        int y = 0;
        if (img != null) {
            cv.drawBitmap(img, (inner - img.getWidth()) / 2f, y, plain);
            y += img.getHeight() + SPACING;
        }
        if (text != null) {
            cv.save();
            cv.translate(0, y);
            text.draw(cv);
            cv.restore();
            y += text.getHeight() + SPACING;
        }
        if (qr != null) {
            cv.drawBitmap(qr, (inner - qr.getWidth()) / 2f, y, plain);
            y += qr.getHeight() + SPACING;
        }
        if (bar != null) {
            if (bar.getWidth() > inner) {
                android.graphics.Rect dst = new android.graphics.Rect(0, y, inner, y + bar.getHeight());
                cv.drawBitmap(bar, null, dst, plain);
            } else {
                cv.drawBitmap(bar, (inner - bar.getWidth()) / 2f, y, plain);
            }
            y += bar.getHeight();
            Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
            tp.setColor(Color.BLACK);
            tp.setTextSize(BARCODE_TEXT);
            tp.setTextAlign(Paint.Align.CENTER);
            cv.drawText(c.barcode.trim(), inner / 2f, y + BARCODE_TEXT, tp);
        }
        cv.restore();
        return out;
    }
}
