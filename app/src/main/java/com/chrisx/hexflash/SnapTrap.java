package com.chrisx.hexflash;

class SnapTrap extends Collectible {
    SnapTrap(float w, float h, Platform p) {
        super(w, h, p);
        setBmp(OpenGLRenderer.snaptrap);
    }
}
