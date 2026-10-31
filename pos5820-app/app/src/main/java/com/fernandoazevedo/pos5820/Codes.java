package com.fernandoazevedo.pos5820;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

/** Geração de QR code e código de barras CODE128 como bitmap nítido (escala inteira). */
public final class Codes {
    private Codes() {
    }

    public static Bitmap qr(String text, int maxSize) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
        int scale = Math.max(1, maxSize / m.getWidth());
        return fromMatrix(m, scale, scale);
    }

    public static Bitmap code128(String text, int maxWidth, int height) throws Exception {
        BitMatrix m = new Code128Writer().encode(text, BarcodeFormat.CODE_128, 0, 1);
        int scale = Math.max(1, maxWidth / m.getWidth());
        return fromMatrix(m, scale, height);
    }

    private static Bitmap fromMatrix(BitMatrix m, int scaleX, int heightOrScaleY) {
        int mw = m.getWidth(), mh = m.getHeight();
        int w = mw * scaleX;
        boolean oneD = mh == 1;
        int h = oneD ? heightOrScaleY : mh * heightOrScaleY;
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            int my = oneD ? 0 : y / heightOrScaleY;
            for (int x = 0; x < w; x++) {
                px[y * w + x] = m.get(x / scaleX, my) ? Color.BLACK : Color.WHITE;
            }
        }
        return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888);
    }
}
