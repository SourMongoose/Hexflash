package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.opengl.Matrix;

abstract class Collectible {
    private float dx, dy, w, width, height;
    protected Platform p;

    private Bitmap bmp;
    private BitmapRect br;

    Collectible(float width, float height, Platform p) {
        this.width = width;
        this.height = height;
        this.p = p;

        w = p.getW()/3;

        double r = Math.random() * (p.getW()-w)/2;
        double theta = Math.random() * 2 * Math.PI;

        dx = (float)(r*Math.cos(theta));
        dy = (float)(r*Math.sin(theta));
    }

    void setBmp(Bitmap bmp) {
        this.bmp = bmp;
        br = new BitmapRect(bmp, -w/2, w/2, w/2, -w/2, 0.3f);
    }

    float getX() {
        return p.getX() + dx;
    }
    float getY() {
        return p.getY() + dy;
    }
    float getW() {
        return w;
    }

    boolean visible(float shift) {
        return getY()-shift+w/2 > 0 && getY()-shift-w/2 < height;
    }

    void draw(float[] m) {
        float[] mtx = m.clone();
        Matrix.translateM(mtx, 0, getX(), getY(), 0);

        br.draw(mtx);
    }
}
