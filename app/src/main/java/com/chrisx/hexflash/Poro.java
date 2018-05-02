package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.opengl.Matrix;

class Poro implements Image {
    private float
            x, y, targetX, targetY, w,
            maxRange, currRange, secToMaxRange;
    private double angle;
    private int spin;
    private boolean channel, burned;
    private double snared;
    private final double MAX_SNARE = 0.5;
    private double flashAnimation;
    private float prevX, prevY;
    private final double MAX_FLASH = 0.3;

    private Platform platform;
    private float offsetX, offsetY; //distance away from center of platform
    private float offsetAngle;

    private float width, height;
    private Bitmap bmp;

    private BitmapRect br, br_flash, br_flash2, br_snare, br_mrange, br_indicator;

    Poro(float width, float height) {
        this.width = width;
        this.height = height;

        maxRange = height / 4;

        w = width / 8;
        reset();

        br_flash = new BitmapRect(MainActivity.flash, -w/2, w/2, w/2, -w/2, 0.5f);
        br_flash2 = new BitmapRect(MainActivity.flash2, -w/2, w/2, w/2, -w/2, 0.5f);
        br_snare = new BitmapRect(MainActivity.snarefx, -w/1.5f, w/1.5f, w/1.5f, -w/1.5f, 0.5f);
        br_mrange = new BitmapRect(MainActivity.maxrange, -maxRange, maxRange, maxRange, -maxRange, 0.5f);
        br_indicator = new BitmapRect(MainActivity.indicator, -w/2, w/2, w/2, -w/2, 0.5f);
    }

    void reset() {
        x = width / 2;
        y = height / 2;
        angle = Math.PI / 2;
        spin = 0;
        channel = burned = false;
        flashAnimation = 0;

        setBmp(MainActivity.poro);
    }

    float getW() {
        return w;
    }

    float getX() {
        return x;
    }
    void setX(float x) {
        this.x = x;
    }

    float getY() {
        return y;
    }
    void setY(float y) {
        this.y = y;
    }

    float getMaxRange() {
        return maxRange;
    }

    void setBmp(Bitmap bmp) {
        this.bmp = bmp;
        br = new BitmapRect(bmp, -w/2, w/2, w/2, -w/2, 0.4f);
    }

    void setPlatform(Platform p) {
        this.platform = p;
        offsetX = p.getX() - x;
        offsetY = p.getY() - y;
        offsetAngle = (float)(p.getAngle()*Math.PI/180 - angle);
    }

    void startChannel(float secToMaxRange) {
        if (snared <= 0) {
            channel = true;
            this.currRange = 0;
            this.secToMaxRange = secToMaxRange;
        }
    }
    void interruptChannel() {
        channel = false;
    }
    void endChannel() {
        flashAnimation = MAX_FLASH;
        prevX = x;
        prevY = y;

        channel = false;
        angle = Math.atan2(targetY - y, targetX - x) + spin*Math.PI/180;
        x += currRange * Math.cos(angle);
        y += currRange * Math.sin(angle);
    }
    boolean isChanneling() {
        return channel;
    }
    void update(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        currRange += maxRange / secToMaxRange / MainActivity.FRAMES_PER_SECOND;

        //hits max range
        if (currRange >= maxRange) {
            currRange = maxRange;
            endChannel();
        }
    }
    void update() {
        x = platform.getX() - offsetX;
        y = platform.getY() - offsetY;
        angle = platform.getAngle()*Math.PI/180 - offsetAngle;

        updateAnimations();
    }
    void updateAnimations() {
        if (snared > 0)
            snared = Math.max(snared - 1. / MainActivity.FRAMES_PER_SECOND, 0);
        if (flashAnimation > 0)
            flashAnimation = Math.max(flashAnimation - 1. / MainActivity.FRAMES_PER_SECOND, 0);
    }
    void addSpin() {
        spin = (spin + 360 / MainActivity.FRAMES_PER_SECOND * 3 / 2) % 360;
    }

    void snare() {
        snared = MAX_SNARE;
    }

    void burn() {
        burned = true;
        setBmp(MainActivity.poro_black);
        interruptChannel();
    }
    boolean isBurned() {
        return burned;
    }

    public void draw(float[] m) {
        float[] mtx = m.clone();
        Matrix.translateM(mtx, 0, x, y, 0);

        if (channel) {
            br_mrange.draw(mtx);
            drawIndicator(mtx);
        }

        Matrix.rotateM(mtx, 0, (float)(angle * 180/Math.PI - 90) + spin, 0, 0, 1);

        br.draw(mtx);
        if (snared > 0) {
            Matrix.rotateM(mtx, 0, (float)(-45 + 90 * (snared / MAX_SNARE)), 0, 0, 1);
            br_snare.draw(mtx);
        }

        if (flashAnimation > 0) {
            mtx = m.clone();
            Matrix.translateM(mtx, 0, prevX, prevY, 0);
            Matrix.rotateM(mtx, 0, (float) (angle * 180 / Math.PI - 90), 0, 0, 1);
            /*
            Paint opacity = new Paint();
            opacity.setAlpha((int) (255 * flashAnimation / MAX_FLASH));
            */
            br_flash.draw(mtx);

            mtx = m.clone();
            Matrix.translateM(mtx, 0, x, y, 0);
            br_flash2.draw(mtx);
        }
    }

    void drawIndicator(float[] m) {
        float[] mtx = m.clone();

        double tempAngle = Math.atan2(targetY - y, targetX - x) / Math.PI * 180;
        float cr = currRange;

        Matrix.rotateM(mtx, 0, (float)tempAngle+spin, 0, 0, 1);
        Matrix.translateM(mtx, 0, cr, 0, 0);
        br_indicator.draw(mtx);
    }
}
