package com.chrisx.hexflash;

import android.graphics.Canvas;

class SnapTrap extends Collectible {
    SnapTrap(Canvas c, Platform p) {
        super(c, p);
        bmp = MainActivity.snaptrap;
    }
}
