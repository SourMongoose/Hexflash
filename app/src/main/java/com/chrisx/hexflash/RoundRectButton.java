package com.chrisx.hexflash;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

class RoundRectButton {
    private RectF rectf;
    private Paint p, p2;
    private boolean pressed;

    RoundRectButton(float x1, float y1, float x2, float y2, int color) {
        this.rectf = new RectF(x1,y1,x2,y2);

        int tmp = Color.rgb((255+Color.red(color))/2,(255+Color.green(color))/2,(255+Color.blue(color))/2);

        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, y1, 0, y2, tmp, color, Shader.TileMode.CLAMP));

        p2 = new Paint(p);
        p2.setShader(new LinearGradient(0, y1, 0, y2, color, tmp, Shader.TileMode.CLAMP));
    }

    RectF getRectF() {
        return rectf;
    }

    void press() {
        pressed = true;
    }
    void release() {
        pressed = false;
    }

    boolean contains(float x, float y) {
        return rectf.contains(x, y);
    }
    boolean isPressed() {
        return pressed;
    }

    void draw() {
        /*
        float r = OpenGLRenderer.c854(15);
        if (pressed) c.drawRoundRect(rectf, r, r, p2);
        else c.drawRoundRect(rectf, r, r, p);
        */
    }
}
