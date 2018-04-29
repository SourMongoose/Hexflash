package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;

abstract class Collectible {
    float dx, dy, w;
    protected Platform p;

    Canvas c;
    Bitmap bmp;

    Collectible(Canvas c, Platform p) {
        this.c = c;
        this.p = p;

        w = p.getW()/3;

        double r = Math.random() * (p.getW()-w)/2;
        double theta = Math.random() * 2 * Math.PI;

        dx = (float)(r*Math.cos(theta));
        dy = (float)(r*Math.sin(theta));
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
        return getY()-shift+w/2 > 0 && getY()-shift-w/2 < c.getHeight();
    }

    void draw() {
        c.save();
        c.translate(getX(), getY());

        c.drawBitmap(bmp,-w/2,-w/2,null);

        c.restore();
    }
}
