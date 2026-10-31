package com.fernandoazevedo.pos5820;

/** Imagem 1 bit por pixel, linhas empacotadas (MSB primeiro), bit 1 = preto. */
public class MonoBitmap {
    public final int widthBytes;
    public final int height;
    public final byte[] data;

    public MonoBitmap(int widthBytes, int height, byte[] data) {
        this.widthBytes = widthBytes;
        this.height = height;
        this.data = data;
    }
}
