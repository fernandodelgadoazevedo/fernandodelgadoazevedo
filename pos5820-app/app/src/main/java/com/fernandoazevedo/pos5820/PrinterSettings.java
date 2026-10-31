package com.fernandoazevedo.pos5820;

import android.content.Context;
import android.content.SharedPreferences;

/** Configurações persistentes da impressora e do último layout usado. */
public class PrinterSettings {
    public static final int PROTO_ESCPOS = 0;
    public static final int PROTO_TSPL = 1;

    public static final int MEDIA_GAP = 0;
    public static final int MEDIA_BLINE = 1;
    public static final int MEDIA_CONTINUOUS = 2;

    private static final String PREFS = "pos5820";

    public String address = "";
    public String deviceName = "";
    public int protocol = PROTO_ESCPOS;
    public int media = MEDIA_GAP;
    public int labelW = 50;
    public int labelH = 30;
    public float gap = 2f;
    public int density = 8;
    public boolean invert = false;
    public boolean nextLabel = false;
    public int printWidth = 384;
    public int fontIndex = 1;
    public int alignIndex = 0;
    public boolean bold = false;
    public int copies = 1;

    public static PrinterSettings load(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        PrinterSettings s = new PrinterSettings();
        s.address = p.getString("address", s.address);
        s.deviceName = p.getString("deviceName", s.deviceName);
        s.protocol = p.getInt("protocol", s.protocol);
        s.media = p.getInt("media", s.media);
        s.labelW = p.getInt("labelW", s.labelW);
        s.labelH = p.getInt("labelH", s.labelH);
        s.gap = p.getFloat("gap", s.gap);
        s.density = p.getInt("density", s.density);
        s.invert = p.getBoolean("invert", s.invert);
        s.nextLabel = p.getBoolean("nextLabel", s.nextLabel);
        s.printWidth = p.getInt("printWidth", s.printWidth);
        s.fontIndex = p.getInt("fontIndex", s.fontIndex);
        s.alignIndex = p.getInt("alignIndex", s.alignIndex);
        s.bold = p.getBoolean("bold", s.bold);
        s.copies = p.getInt("copies", s.copies);
        return s;
    }

    public void save(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("address", address)
                .putString("deviceName", deviceName)
                .putInt("protocol", protocol)
                .putInt("media", media)
                .putInt("labelW", labelW)
                .putInt("labelH", labelH)
                .putFloat("gap", gap)
                .putInt("density", density)
                .putBoolean("invert", invert)
                .putBoolean("nextLabel", nextLabel)
                .putInt("printWidth", printWidth)
                .putInt("fontIndex", fontIndex)
                .putInt("alignIndex", alignIndex)
                .putBoolean("bold", bold)
                .putInt("copies", copies)
                .apply();
    }

    /** 203 dpi = 8 pontos por mm. A cabeça da POS-5820 tem 48 mm (384 pontos). */
    public int labelWidthDots() {
        return Math.min(labelW * 8, printWidth);
    }

    public int labelHeightDots() {
        return labelH * 8;
    }
}
