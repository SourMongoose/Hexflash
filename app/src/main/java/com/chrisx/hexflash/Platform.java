package com.chrisx.hexflash;

import android.graphics.Canvas;

public class Platform {
    private float x, y, width;
    private boolean moving; //true=scuttle, false=lilypad
    private float speed; //in x-direction
    private int dir; //-1=left, 1=right

    private Canvas c;

    public Platform(Canvas c) {
        width = c.getWidth() / 5;

        this.c = c;
    }

    public void draw() {

    }
}
