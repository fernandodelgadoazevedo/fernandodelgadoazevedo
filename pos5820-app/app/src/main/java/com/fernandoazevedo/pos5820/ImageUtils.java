package com.fernandoazevedo.pos5820;

import android.graphics.Bitmap;
import android.graphics.Color;

public final class ImageUtils {
    private ImageUtils() {
    }

    private static float[] luminance(Bitmap b) {
        int w = b.getWidth(), h = b.getHeight();
        int[] px = new int[w * h];
        b.getPixels(px, 0, w, 0, 0, w, h);
        float[] lum = new float[w * h];
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int a = (c >>> 24) & 0xFF;
            float l = 0.299f * ((c >> 16) & 0xFF) + 0.587f * ((c >> 8) & 0xFF) + 0.114f * (c & 0xFF);
            // Transparente vira branco
            lum[i] = 255f - (255f - l) * a / 255f;
        }
        return lum;
    }

    /** Converte para preto/branco; com dither usa Floyd–Steinberg (bom para fotos). */
    private static boolean[] binarize(Bitmap b, boolean dither) {
        int w = b.getWidth(), h = b.getHeight();
        float[] lum = luminance(b);
        boolean[] black = new boolean[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                float old = lum[i];
                boolean k = old < 128f;
                black[i] = k;
                if (!dither) continue;
                float err = old - (k ? 0f : 255f);
                if (x + 1 < w) lum[i + 1] += err * 7f / 16f;
                if (y + 1 < h) {
                    if (x > 0) lum[i + w - 1] += err * 3f / 16f;
                    lum[i + w] += err * 5f / 16f;
                    if (x + 1 < w) lum[i + w + 1] += err * 1f / 16f;
                }
            }
        }
        return black;
    }

    public static MonoBitmap toMono(Bitmap b, boolean dither) {
        int w = b.getWidth(), h = b.getHeight();
        boolean[] black = binarize(b, dither);
        int wb = (w + 7) / 8;
        byte[] out = new byte[wb * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (black[y * w + x]) out[y * wb + (x >> 3)] |= (byte) (0x80 >> (x & 7));
            }
        }
        return new MonoBitmap(wb, h, out);
    }

    /** Redimensiona para a largura indicada e aplica dither, devolvendo um Bitmap só preto/branco. */
    public static Bitmap ditherScaled(Bitmap src, int targetWidth) {
        Bitmap scaled = src;
        if (src.getWidth() != targetWidth) {
            int th = Math.max(1, Math.round(src.getHeight() * (targetWidth / (float) src.getWidth())));
            scaled = Bitmap.createScaledBitmap(src, targetWidth, th, true);
        }
        int w = scaled.getWidth(), h = scaled.getHeight();
        boolean[] black = binarize(scaled, true);
        int[] px = new int[w * h];
        for (int i = 0; i < px.length; i++) px[i] = black[i] ? Color.BLACK : Color.WHITE;
        return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888);
    }

    /** Remove linhas brancas no final (economiza papel em bobina). */
    public static Bitmap trimBottom(Bitmap b, int padding) {
        int w = b.getWidth(), h = b.getHeight();
        int[] row = new int[w];
        int last = -1;
        for (int y = h - 1; y >= 0 && last < 0; y--) {
            b.getPixels(row, 0, w, 0, y, w, 1);
            for (int x = 0; x < w; x++) {
                int c = row[x];
                if (((c >>> 24) & 0xFF) > 0 && (c & 0xFF) < 128) {
                    last = y;
                    break;
                }
            }
        }
        int newH = Math.min(h, Math.max(1, last + 1 + padding));
        return newH == h ? b : Bitmap.createBitmap(b, 0, 0, w, newH);
    }
}
