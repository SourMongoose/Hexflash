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
    private boolean channel;
    private double snared;
    private final double MAX_SNARE = 0.5;

    private Platform platform;
    private float offsetX, offsetY; //distance away from center of platform
    private float offsetAngle;

    private Canvas c;
    private Bitmap bmp, snarefx, m_range, c_range, indicator; //max/current range
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

        bmp = MainActivity.poro;
        snarefx = MainActivity.snarefx;
        m_range = MainActivity.maxrange;
        c_range = MainActivity.currrange;
        indicator = MainActivity.indicator;
    }

    float getW() {
        return w;
    }

    float getX() {
        return x;
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
        channel = false;
        angle = Math.atan2(targetY - y, targetX - x);
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

        if (snared > 0) snared = Math.max(snared - 1. / MainActivity.FRAMES_PER_SECOND, 0);
    }

    void snare() {
        snared = MAX_SNARE;
    }

    void draw() {
        c.save();
        c.translate(x, y);

        if (channel) {
            drawRange();
            drawIndicator();
        }
        c.rotate((float)(angle * 180/Math.PI - 90)); //convert to degrees and shift by 90deg
        //c.drawText(currRange + " / " + maxRange, 0,0,indicator); //debugging purposes
        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()), new RectF(-w/2,-w/2,w/2,w/2), null);
        if (snared > 0) {
            c.rotate((float)(-45 + 90 * (snared / MAX_SNARE)));
            c.drawBitmap(snarefx, new Rect(0,0,snarefx.getWidth(),snarefx.getHeight()),
                    new RectF(-w/1.5f,-w/1.5f,w/1.5f,w/1.5f), null);
        }

        c.restore();
    }

    void drawHitbox() {
        c.drawCircle(x, y, w/2, hitbox);
    }

    void drawRange() {
        float mr = maxRange, cr = currRange;
        c.drawBitmap(m_range, new Rect(0,0,m_range.getWidth(),m_range.getHeight()), new RectF(-mr,-mr,mr,mr), null);
        c.drawBitmap(c_range, new Rect(0,0,c_range.getWidth(),c_range.getHeight()), new RectF(-cr,-cr,cr,cr), null);
    }

    void drawIndicator() {
        double tempAngle = Math.atan2(targetY - y, targetX - x) / Math.PI * 180;
        float cr = currRange;

        c.rotate((float)tempAngle);
        c.drawBitmap(indicator, new Rect(0,0,indicator.getWidth(),indicator.getHeight()), new RectF(cr-w/2,-w/2,cr+w/2,w/2), null);
        c.rotate(-(float)tempAngle);
    }
}
