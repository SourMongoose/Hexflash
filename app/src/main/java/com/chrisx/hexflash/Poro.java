package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

class Poro {
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

    private Canvas c;
    private Bitmap bmp, snarefx, m_range, indicator; //max range
    private Paint hitbox;

    Poro(Canvas c) {
        this.c = c;

        maxRange = c.getHeight() / 4;

        hitbox = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitbox.setStyle(Paint.Style.STROKE);
        hitbox.setColor(Color.WHITE);

        w = c.getWidth() / 8;
        reset();
    }

    void reset() {
        x = c.getWidth() / 2;
        y = c.getHeight() / 2;
        angle = Math.PI / 2;
        spin = 0;
        channel = burned = false;
        flashAnimation = 0;

        bmp = MainActivity.poro;
        snarefx = MainActivity.snarefx;
        m_range = MainActivity.maxrange;
        indicator = MainActivity.indicator;
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
        bmp = MainActivity.poro_black;
        interruptChannel();
    }
    boolean isBurned() {
        return burned;
    }

    void draw() {
        c.save();
        c.translate(x, y);

        if (channel) {
            drawRange();
            drawIndicator();
        }
        c.rotate((float)(angle * 180/Math.PI - 90) + spin); //convert to degrees and shift by 90deg
        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()), new RectF(-w/2,-w/2,w/2,w/2), null);
        if (snared > 0) {
            c.rotate((float)(-45 + 90 * (snared / MAX_SNARE)));
            c.drawBitmap(snarefx, new Rect(0,0,snarefx.getWidth(),snarefx.getHeight()),
                    new RectF(-w/1.5f,-w/1.5f,w/1.5f,w/1.5f), null);
        }

        c.restore();

        if (flashAnimation > 0) {
            c.save();
            c.translate(prevX, prevY);
            c.rotate((float) (angle * 180 / Math.PI - 90));
            Paint opacity = new Paint();
            opacity.setAlpha((int) (255 * flashAnimation / MAX_FLASH));
            c.drawBitmap(MainActivity.flash,
                    new Rect(0, 0, MainActivity.flash.getWidth(), MainActivity.flash.getHeight()),
                    new RectF(-w/2, -w/2, w/2, w/2), opacity);
            c.restore();
            c.save();
            c.translate(x, y);
            if (flashAnimation > 0) {
                c.drawBitmap(MainActivity.flash2,
                        new Rect(0, 0, MainActivity.flash2.getWidth(), MainActivity.flash2.getHeight()),
                        new RectF(-w/2, -w/2, w/2, w/2), opacity);
            }
            c.restore();
        }
    }

    void drawHitbox() {
        c.drawCircle(x, y, w/2, hitbox);
    }

    void drawRange() {
        float mr = maxRange;
        c.drawBitmap(m_range, new Rect(0,0,m_range.getWidth(),m_range.getHeight()), new RectF(-mr,-mr,mr,mr), null);
    }

    void drawIndicator() {
        double tempAngle = Math.atan2(targetY - y, targetX - x) / Math.PI * 180;
        float cr = currRange;

        c.rotate((float)tempAngle+spin);
        c.drawBitmap(indicator, new Rect(0,0,indicator.getWidth(),indicator.getHeight()), new RectF(cr-w/2,-w/2,cr+w/2,w/2), null);
        c.rotate(-(float)tempAngle-spin);
    }
}
