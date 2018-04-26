package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

class Platform {
    private float x, y, w;
    private float speed; //in x-direction
    private int angle;
    private int spin;

    private Canvas c;
    private Bitmap bmp;

    private Paint hitbox;

    //constructor for lilypad
    Platform(Canvas c, float x, float y) {
        this.c = c;

        this.x = x;
        this.y = y;
        w = c.getWidth() / 5;
        speed = 0;
        spin = 0;

        angle = (int)(Math.random()*360);
        if (MainActivity.getRiverSkin().equals("candy"))
            bmp = Math.random() < 0.333 ? MainActivity.candypad_red :
                    Math.random() < 0.5 ? MainActivity.candypad_orange : MainActivity.candypad_yellow;
        else
            bmp = Math.random() > 0.2 ? MainActivity.lilypad : MainActivity.lilypadlotus;

        hitbox = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitbox.setStyle(Paint.Style.STROKE);
        hitbox.setColor(Color.WHITE);
    }
    //constructor for scuttle
    Platform(Canvas c, float x, float y, float speed) {
        this.c = c;

        this.x = x;
        this.y = y;
        this.w = c.getWidth() / 5;
        this.speed = speed;

        angle = Math.random() < 0.5 ? 90 : 270;
        bmp = MainActivity.getRiverSkin().equals("candy") ? MainActivity.scuttler_candy : MainActivity.scuttler;

        hitbox = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitbox.setStyle(Paint.Style.STROKE);
        hitbox.setColor(Color.WHITE);
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
        return y-shift+w/2 > 0 && y-shift-w/2 < c.getHeight();
    }

    void update() {
        if (speed > 0) {
            if (x + w / 2 > c.getWidth() || x - w / 2 < 0)
                angle = (angle + 360 / MainActivity.FRAMES_PER_SECOND * 2) % 360;

            if (angle == 90) x += speed;
            if (angle == 270) x -= speed;
        }
    }
    void addSpin() {
        spin = (spin + 360 / MainActivity.FRAMES_PER_SECOND) % 360;
    }

    void draw() {
        c.save();
        c.translate(x, y);

        c.rotate(angle - 90 + spin); //shift by 90deg
        if (speed == 0) c.drawBitmap(bmp,-w/2,-w/2,null);
        else c.drawBitmap(bmp,-w/1.7f,-w/1.7f,null);

        c.restore();
    }

    void drawHitbox() {
        c.drawCircle(x, y, w/2, hitbox);
    }
}
