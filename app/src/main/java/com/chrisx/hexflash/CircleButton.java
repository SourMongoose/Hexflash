package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;

class CircleButton {
    private float x, y, r;
    private Canvas c;
    private boolean pressed;
    private Bitmap bmp;

    CircleButton(Canvas c, float x, float y, float r) {
        this.c = c;
        this.x = x;
        this.y = y;
        this.r = r;

        bmp = Bitmap.createScaledBitmap(MainActivity.bubble, Math.round(r*2), Math.round(r*2), false);
    }

    float getX() {
        return x;
    }
    float getY() {
        return y;
    }
    float getR() {
        return r;
    }

    void setX(float x) {
        this.x = x;
    }
    void setY(float y) {
        this.y = y;
    }
    void setR(float r) {
        this.r = r;
    }

    void press() {
        pressed = true;
    }
    void release() {
        pressed = false;
    }

    boolean contains(float x, float y) {
        return MainActivity.distance(x,y,this.x,this.y) <= r;
    }
    boolean isPressed() {
        return pressed;
    }

    void draw() {
        c.save();
        c.translate(x, y);
        if (pressed) c.scale(0.9f,0.9f);

        c.drawBitmap(bmp,-r,-r,null);

        c.restore();
    }
}
