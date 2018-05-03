package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.opengl.Matrix;

class Bullet {
    private float x, y, w, s, f, H;
    private double angle;

    private float width, height;
    private Poro p;
    private Bitmap bmp;
    
    private BitmapRect br;

    Bullet(float width, float height, Poro p, float shift) {
        this.width = width;
        this.height = height;
        this.p = p;

        x = width/2;
        y = (float)(-width/2/Math.tan(5*Math.PI/12)) + shift;
        angle = Math.atan2(p.getY()-y,p.getX()-x);
        w = width / 24;
        s = width * 1.7f / OpenGLRenderer.FRAMES_PER_SECOND; //pixels per frame

        bmp = OpenGLRenderer.bulletbmp;

        f = 225f/800; //fraction of bmp that is part of hitbox
        H = bmp.getHeight() / bmp.getWidth() * w;

        br = new BitmapRect(bmp, -w/2, H/2, w/2, -H/2, 0.4f);
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
        return y-shift+H > 0 && y-shift-H < height;
    }

    void draw(float[] m) {
        float[] mtx = m.clone();
        Matrix.translateM(mtx, 0, x, y, 0);
        Matrix.rotateM(mtx, 0, (float)(angle * 180/Math.PI - 90), 0, 0, 1);
        Matrix.translateM(mtx, 0, 0, f*H/2-H/2, 0);

        br.draw(mtx);
    }

    void update() {
        float[][] corners = new float[][]{{-w/2,-f*H/2},{-w/2,f*H/2},{w/2,-f*H/2},{w/2,f*H/2}};
        for (float[] pt : corners) {
            if (OpenGLRenderer.distance(x+pt[0]*(float)Math.cos(angle+Math.PI/2)+pt[1]*(float)(Math.cos(angle)),
                    y+pt[0]*(float)Math.sin(angle+Math.PI/2)+pt[1]*(float)Math.sin(angle),
                    p.getX(),p.getY()) < p.getW()/2) {
                y += height;
                p.burn();
                return;
            }
        }

        x += s*Math.cos(angle);
        y += s*Math.sin(angle);
    }
}
