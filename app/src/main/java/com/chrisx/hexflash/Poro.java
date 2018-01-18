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

    private Platform platform;
    private float offsetX, offsetY; //distance away from center of platform
    private float offsetAngle;

    private Canvas c;
    private Bitmap bmp;
    private Paint hitbox, indicator, maxRangeCircle, currRangeCircle, rangeGradient;

    Poro(Canvas c) {
        this.c = c;
        this.bmp = MainActivity.poro;

        maxRange = c.getHeight() / 4;

        hitbox = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitbox.setStyle(Paint.Style.STROKE);
        hitbox.setColor(Color.WHITE);

        indicator = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicator.setStyle(Paint.Style.FILL);
        indicator.setColor(Color.rgb(123,251,242));

        maxRangeCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        maxRangeCircle.setStyle(Paint.Style.STROKE);
        maxRangeCircle.setColor(Color.rgb(123,251,242));
        maxRangeCircle.setStrokeWidth(c.getWidth() / 80);

        currRangeCircle = new Paint(maxRangeCircle);
        currRangeCircle.setStrokeWidth(c.getWidth() / 150);

        rangeGradient = new Paint(Paint.ANTI_ALIAS_FLAG);
        rangeGradient.setShader(new RadialGradient(0, 0, maxRange,
                Color.argb(0,0,0,0), Color.argb(50,123,251,242), Shader.TileMode.CLAMP));

        w = c.getWidth() / 8;
        reset();
    }

    void reset() {
        x = c.getWidth() / 2;
        y = c.getHeight() / 2;
        angle = Math.PI / 2;
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

    void setPlatform(Platform p) {
        this.platform = p;
        offsetX = p.getX() - x;
        offsetY = p.getY() - y;
        offsetAngle = (float)(p.getAngle()*Math.PI/180 - angle);
    }

    void startChannel(float secToMaxRange) {
        channel = true;
        this.currRange = 0;
        this.secToMaxRange = secToMaxRange;
    }
    void stopChannel() {
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
            stopChannel();
        }
    }
    void update() {
        x = platform.getX() - offsetX;
        y = platform.getY() - offsetY;
        angle = platform.getAngle()*Math.PI/180 - offsetAngle;
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

        c.restore();
    }

    void drawHitbox() {
        c.drawCircle(x, y, w/2, hitbox);
    }

    void drawRange() {
        c.drawCircle(0, 0, maxRange, rangeGradient);
        c.drawCircle(0, 0, currRange, currRangeCircle);
        c.drawCircle(0, 0, maxRange, maxRangeCircle);
    }

    void drawIndicator() {
        double tempAngle = Math.atan2(targetY - y, targetX - x);
        float tempX = currRange * (float)Math.cos(tempAngle),
                tempY = currRange * (float)Math.sin(tempAngle);
        c.drawCircle(tempX, tempY, maxRange*3/40, currRangeCircle);
        c.drawCircle(tempX, tempY, maxRange/40, indicator);
    }
}
