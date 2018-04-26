package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

class PoroSnax {
    private float dx, dy, w;
    private Platform p;
    private int dAngle;

    private Canvas c;
    private Bitmap bmp;

    PoroSnax(Canvas c, Platform p) {
        this.c = c;
        this.p = p;

        w = p.getW()/3;

        double r = Math.random() * (p.getW()-w)/2;
        double theta = Math.random() * 2 * Math.PI;

        dx = (float)(r*Math.cos(theta));
        dy = (float)(r*Math.sin(theta));

        dAngle = (int)(Math.random()*360);
        bmp = MainActivity.porosnax;
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
    float getAngle() {
        return p.getAngle()+dAngle;
    }

    void addSpin() {
        dAngle = (dAngle + 360 / MainActivity.FRAMES_PER_SECOND) % 360;
    }

    boolean visible(float shift) {
        return getY()-shift+w/2 > 0 && getY()-shift-w/2 < c.getHeight();
    }

    void draw() {
        c.save();
        c.translate(getX(), getY());

        c.rotate(getAngle());
        c.drawBitmap(bmp,-w/2,-w/2,null);

        c.restore();
    }
}
