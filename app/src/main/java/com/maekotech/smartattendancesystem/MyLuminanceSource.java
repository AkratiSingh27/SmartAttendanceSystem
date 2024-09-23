//convert the image data into a format ZXing can process.
package com.maekotech.smartattendancesystem;

import com.google.zxing.LuminanceSource;

public class MyLuminanceSource extends LuminanceSource {
    private final byte[] luminance;

    public MyLuminanceSource(byte[] data, int width, int height) {
        super(width, height);
        this.luminance = data;
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        System.arraycopy(luminance, y * getWidth(), row, 0, getWidth());
        return row;
    }

    @Override
    public byte[] getMatrix() {
        return luminance;
    }

    @Override
    public boolean isCropSupported() {
        return false; // cropping is not supported
    }

    @Override
    public LuminanceSource crop(int left, int top, int width, int height) {
        throw new UnsupportedOperationException("Crop is not supported.");
    }
}