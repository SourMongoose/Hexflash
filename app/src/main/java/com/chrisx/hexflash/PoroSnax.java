package com.chrisx.hexflash;

import android.graphics.Canvas;

class PoroSnax extends Collectible {
    PoroSnax(Canvas c, Platform p) {
        super(c, p);
        bmp = MainActivity.porosnax;
    }
}
