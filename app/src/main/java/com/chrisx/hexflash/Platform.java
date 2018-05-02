package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.opengl.Matrix;

class Platform implements Image {
    private float x, y, w;
    private float speed; //in x-direction
    private int angle;
    private int spin;

    private float width, height;
    private BitmapRect br;

    //constructor for lilypad
    Platform(float width, float height, float x, float y) {
        this.width = width;
        this.height = height;

        this.x = x;
        this.y = y;
        w = width / 5;
        speed = 0;
        spin = 0;

        angle = (int)(Math.random()*360);
        if (MainActivity.getRiverSkin().equals("candy"))
            setBmp(Math.random() < 0.333 ? MainActivity.candypad_red :
                    Math.random() < 0.5 ? MainActivity.candypad_orange : MainActivity.candypad_yellow);
        else
            setBmp(Math.random() > 0.2 ? MainActivity.lilypad : MainActivity.lilypadlotus);
    }
    //constructor for scuttle
    Platform(float width, float height, float x, float y, float speed) {
        this.width = width;
        this.height = height;

        this.x = x;
        this.y = y;
        this.w = width / 5;
        this.speed = speed;

        angle = Math.random() < 0.5 ? 90 : 270;
        setBmp(MainActivity.getRiverSkin().equals("candy") ? MainActivity.scuttler_candy : MainActivity.scuttler);
    }

    void setBmp(Bitmap bmp) {
        br = new BitmapRect(bmp, -w/2, w/2, w/2, -w/2, 0);
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
    float getSpeed() {
        return speed;
    }
    float getAngle() {
        return angle;
    }

    boolean visible(float shift) {
        return y-shift+w/2 > 0 && y-shift-w/2 < height;
    }

    void update() {
        if (speed > 0) {
            if (x + w / 2 > width || x - w / 2 < 0)
                angle = (angle + 360 / MainActivity.FRAMES_PER_SECOND * 2) % 360;

            if (angle == 90) x += speed;
            if (angle == 270) x -= speed;
        }
    }
    void addSpin() {
        spin = (spin + 360 / MainActivity.FRAMES_PER_SECOND) % 360;
    }

    public void draw(float[] m) {
        float[] mtx = m.clone();
        Matrix.translateM(mtx, 0, x, y, 0);
        Matrix.rotateM(mtx, 0, angle-90+spin, 0, 0, 1);

        br.draw(mtx);
    }
}
