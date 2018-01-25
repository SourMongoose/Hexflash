package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

class PoroSnax {
    private float x, y, w;
    private int angle;

    private Canvas c;
    private Bitmap bmp;

    PoroSnax(Canvas c, Platform p) {
        this.c = c;

        w = p.getW()/3;

        double r = Math.random() * (p.getW()-w)/2;
        double theta = Math.random() * 2 * Math.PI;

        x = (float)(p.getX() + r*Math.cos(theta));
        y = (float)(p.getY() + r*Math.sin(theta));

        angle = (int)(Math.random()*360);
        bmp = MainActivity.porosnax;
    }

    float getX() {
        return x;
    }
    float getY() {
        return y;
    }
    float getW() {
        return w;
    }
    float getAngle() {
        return angle;
    }

    boolean visible(float shift) {
        return y-shift+w/2 > 0 && y-shift-w/2 < c.getHeight();
    }

    void draw() {
        c.save();
        c.translate(x, y);

        c.rotate(angle);
        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()), new RectF(-w/2,-w/2,w/2,w/2), null);

        c.restore();
    }
}
