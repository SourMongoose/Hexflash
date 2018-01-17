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

    private Canvas c;
    private Bitmap bmp;
    private Paint hitbox, indicator, maxRangeCircle, currRangeCircle, rangeGradient;

    public Poro(Canvas c, Bitmap bmp) {
        this.c = c;
        this.bmp = bmp;

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

    public void reset() {
        x = c.getWidth() / 2;
        y = c.getHeight() / 2;
        angle = Math.PI / 2;
    }

    public float getW() {
        return w;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getMaxRange() {
        return maxRange;
    }

    public void startChannel(float secToMaxRange) {
        channel = true;
        this.currRange = 0;
        this.secToMaxRange = secToMaxRange;
    }
    public void stopChannel() {
        channel = false;
        angle = Math.atan2(targetY - y, targetX - x);
        x += currRange * Math.cos(angle);
        y += currRange * Math.sin(angle);
    }
    public boolean isChanneling() {
        return channel;
    }
    public void update(int fps, float x, float y) {
        this.targetX = x;
        this.targetY = y;
        currRange += maxRange / secToMaxRange / fps;

        //hits max range
        if (currRange >= maxRange) {
            currRange = maxRange;
            stopChannel();
        }
    }

    public void draw() {
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

    public void drawHitbox() {
        c.drawCircle(x, y, w/2, hitbox);
    }

    public void drawRange() {
        c.drawCircle(0, 0, maxRange, rangeGradient);
        c.drawCircle(0, 0, currRange, currRangeCircle);
        c.drawCircle(0, 0, maxRange, maxRangeCircle);
    }

    public void drawIndicator() {
        double tempAngle = Math.atan2(targetY - y, targetX - x);
        float tempX = currRange * (float)Math.cos(tempAngle),
                tempY = currRange * (float)Math.sin(tempAngle);
        c.drawCircle(tempX, tempY, maxRange*3/40, currRangeCircle);
        c.drawCircle(tempX, tempY, maxRange/40, indicator);
    }
}
