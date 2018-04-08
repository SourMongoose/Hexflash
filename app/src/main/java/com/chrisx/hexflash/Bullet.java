package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

class Bullet {
    private float x, y, w, s, f, H;
    private double angle;

    private Canvas c;
    private Poro p;
    private Bitmap bmp;

    Bullet(Canvas c, Poro p, float shift) {
        this.c = c;
        this.p = p;

        x = c.getWidth()/2;
        y = (float)(-c.getWidth()/2/Math.tan(5*Math.PI/12)) + shift;
        angle = Math.atan2(p.getY()-y,p.getX()-x);
        w = c.getWidth() / 18;
        s = c.getWidth() * 1.7f / MainActivity.FRAMES_PER_SECOND; //pixels per frame

        bmp = MainActivity.bulletbmp;

        f = 1f/3; //fraction of bmp that is part of hitbox
        H = bmp.getHeight() / bmp.getWidth() * w;
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
    float getH() {
        return H;
    }

    boolean visible(float shift) {
        return y-shift+H > 0 && y-shift-H < c.getHeight();
    }

    void draw() {
        c.save();
        c.translate(x, y);
        c.rotate((float)(angle * 180/Math.PI - 90));

        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()),
                new RectF(-w/2,f*H/2-H,w/2,f*H/2), null);

        c.restore();
    }

    void update() {
        float[][] corners = new float[][]{{-w/2,f*H/2-H},{-w/2,f*H/2},{w/2,f*H/2-H},{w/2,f*H/2}};
        for (float[] pt : corners) {
            if (MainActivity.distance(x+pt[0]*(float)Math.cos(angle+Math.PI/2)+pt[1]*(float)(Math.cos(angle)),
                    y+pt[0]*(float)Math.sin(angle+Math.PI/2)+pt[1]*(float)Math.sin(angle),
                    p.getX(),p.getY()) < p.getW()/2) {
                y += c.getHeight();
                p.burn();
                return;
            }
        }

        x += s*Math.cos(angle);
        y += s*Math.sin(angle);
    }
}
