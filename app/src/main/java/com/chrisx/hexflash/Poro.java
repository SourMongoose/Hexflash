package com.chrisx.hexflash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class Poro {
    private float x, y, w;
    private float angle;

    private Canvas c;
    private Bitmap bmp;
    private Paint hitbox;

    public Poro(Canvas c, Bitmap bmp) {
        this.c = c;
        this.bmp = bmp;

        hitbox = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitbox.setStyle(Paint.Style.STROKE);
        hitbox.setColor(Color.WHITE);

        w = c.getWidth() / 8;
        reset();
    }

    public void reset() {
        x = c.getWidth() / 2;
        y = c.getHeight() / 2;
        angle = 0;
    }

    public void draw() {
        c.save();
        c.translate(x, y);
        c.rotate(angle);
        c.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),bmp.getHeight()), new RectF(-w/2,-w/2,w/2,w/2), null);
        c.restore();
    }

    public void drawHitbox() {
        c.drawCircle(x, y, w/2, hitbox);
    }
}
