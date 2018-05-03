package com.chrisx.hexflash;

import android.opengl.Matrix;

class CircleButton {
    private float x, y, r;
    private boolean pressed;
    private BitmapRect br;

    CircleButton(float x, float y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;

        br = new BitmapRect(OpenGLRenderer.bubble, -r, r, r, -r, 0.5f);
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
        return OpenGLRenderer.distance(x,y,this.x,this.y) <= r;
    }
    boolean isPressed() {
        return pressed;
    }

    void draw(float[] m) {
        float[] tmp = m.clone();
        Matrix.translateM(tmp, 0, x, y, 0);

        br.draw(tmp);
    }
}
