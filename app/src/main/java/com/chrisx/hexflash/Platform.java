package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

class Platform {
    private float x, y, w;
    private float speed; //in x-direction
    private int angle;

    private Canvas c;
    private Bitmap bmp;

    //constructor for lilypad
    Platform(Canvas c, float x, float y) {
        this.c = c;

        this.x = x;
        this.y = y;
        w = c.getWidth() / 5;
        speed = 0;

        angle = (int)(Math.random()*360);
        bmp = MainActivity.lilypad;
    }
    //constructor for scuttle
    Platform(Canvas c, float x, float y, float speed) {
        this.c = c;

        this.x = x;
        this.y = y;
        this.w = c.getWidth() / 5;
        this.speed = speed;

        angle = Math.random() < 0.5 ? 90 : 270;
        bmp = MainActivity.scuttler;
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

    boolean visible() {
        return y+w/2 > 0 || y-w/2 < c.getHeight();
    }

    void update() {
        if (speed > 0) {
            if (x + w/2 > c.getWidth() || x - w/2 < 0)
                angle = (angle + 360 / MainActivity.FRAMES_PER_SECOND) % 360;

            if (angle == 90) x += speed;
            if (angle == 270) x -= speed;
        }
    }

    void draw() {
        c.save();
        c.translate(x, y);

        c.rotate(angle + 90); //convert to degrees and shift by 90deg
        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()), new RectF(-w/2,-w/2,w/2,w/2), null);

        c.restore();
    }
}