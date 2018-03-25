package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

class CircleButton {
    private float x, y, r;
    private Canvas c;
    private Paint p, p2;
    private boolean pressed;

    CircleButton(Canvas c, float x, float y, float r) {
        this.c = c;
        this.x = x;
        this.y = y;
        this.r = r;

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(102,165,193));

        p2 = new Paint(p);
        p2.setColor(Color.rgb(62,125,153));
    }

    float getX() {
        return x;
    }
    float getY() {
        return y;
    }
    float getR() {
        return pressed ? r*.9f : r;
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
        Bitmap bmp = MainActivity.bubble;
        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()),
                new RectF(x-getR(),y-getR(),x+getR(),y+getR()), null);
    }
}
