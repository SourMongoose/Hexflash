package com.chrisx.hexflash;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.MotionEvent;

class OpenGLSurfaceView extends GLSurfaceView {
    private final OpenGLRenderer r;

    private boolean paused = false;

    //frame data
    final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    OpenGLSurfaceView(Context context){
        super(context);

        //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        r = new OpenGLRenderer(context);
        setRenderer(r);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        nanosecondsPerFrame = (int)1e9 / FRAMES_PER_SECOND;

        final Handler handler = new Handler();

        //UI Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) requestRender();
                        }
                    });

                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
    }
    
    @Override
    public void buildLayer() {
        if (r.menu.equals("shop")) {
            r.goToMenu(r.prevMenu);
        } else if (r.menu.equals("more")) {
            r.goToMenu("start");
        } else if (r.menu.equals("stats")) {
            r.goToMenu("more");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX(event.getActionIndex());
        float Y = event.getY(event.getActionIndex());
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            r.lastPressMenu = r.menu;
            r.pressed = true;

            for (CircleButton cb : r.cbs)
                if (cb.contains(X, Y)) cb.press();
            for (RoundRectButton rrb : r.rrbs)
                if (rrb.contains(X, Y)) rrb.press();
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (CircleButton cb : r.cbs)
                if (!cb.contains(X, Y)) cb.release();
            for (RoundRectButton rrb : r.rrbs)
                if (!rrb.contains(X, Y)) rrb.release();
        } else if (action == MotionEvent.ACTION_UP) {
            r.pressed = false;
        }

        if (r.menu.equals("start")) {
            if (action == MotionEvent.ACTION_UP) {
                if (r.middle.isPressed()) {
                    r.gamemode = "classic";
                    r.goToMenu("game");
                } else if (r.right.isPressed()) {
                    r.goToMenu("more");
                } else if (r.left.isPressed()) {
                    r.goToMenu("shop");
                }
            }
        } else if (r.menu.equals("shop")) {
            //selecting skins
            if (action == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < r.nBlitz; i++) {
                    if (r.hasSkin(r.blitzskins[i]) && r.blitzskins_rectf[i].contains(X, Y)) {
                        r.editor.putString("blitzskin", r.blitzskins[i]);
                        r.editor.apply();
                    }
                }
                for (int i = 0; i < r.nRiver; i++) {
                    if (r.hasSkin(r.riverskins[i]) && r.riverskins_rectf[i].contains(X, Y)) {
                        r.editor.putString("riverskin", r.riverskins[i]);
                        r.editor.apply();
                    }
                }
            }
            //buying skins
            if (action == MotionEvent.ACTION_UP) {
                for (int i = 0; i < r.nBlitz; i++) {
                    if (!r.hasSkin(r.blitzskins[i]) && r.blitzskins_rectf[i].contains(X, Y)) {
                        if (r.getPoroSnax() >= r.blitzskins_cost[i]) {
                            r.editor.putBoolean("has_skin_" + r.blitzskins[i], true);
                            r.editor.putInt("porosnax", r.getPoroSnax() - r.blitzskins_cost[i]);
                            r.editor.apply();
                        }
                    }
                }
                for (int i = 0; i < r.nRiver; i++) {
                    if (!r.hasSkin(r.riverskins[i]) && r.riverskins_rectf[i].contains(X, Y)) {
                        if (r.getPoroSnax() >= r.riverskins_cost[i]) {
                            r.editor.putBoolean("has_skin_" + r.riverskins[i], true);
                            r.editor.putInt("porosnax", r.getPoroSnax() - r.riverskins_cost[i]);
                            r.editor.apply();
                        }
                    }
                }
            }

            /*
            //watch video ad
            if (action == MotionEvent.ACTION_UP && X > r.w() - r.c854(100) && Y < r.c854(100)) {
                if (r.rva.isLoaded()) {
                    r.rva.show();
                }
            }
            */

            //back arrow
            if (action == MotionEvent.ACTION_UP && X < r.c854(100) && Y > r.h() - r.c854(100))
                r.goToMenu(r.prevMenu);
        } else if (r.menu.equals("more")) {
            if (action == MotionEvent.ACTION_DOWN) {
                r.downX = X;
                r.downY = Y;
            }
            if (action == MotionEvent.ACTION_UP) {
                for (int i = 1; i < r.rrbs.length - 1; i++) {
                    RoundRectButton rrb = r.rrbs[i];

                    if (rrb.isPressed()) {
                        if (rrb == r.scuttle) r.gamemode = "scuttle";
                        else if (rrb == r.snare) r.gamemode = "snare";
                        else if (rrb == r.spin) r.gamemode = "spin";
                        else if (rrb == r.light) r.gamemode = "light";
                        else if (rrb == r.cc) r.gamemode = "cc";

                        r.goToMenu("game");
                        break;
                    }
                }

                //combo chaos/rainbow river
                if (r.rrbs[1].contains(r.downX, r.downY) && r.rrbs[r.rrbs.length - 2].contains(X, Y)) {
                    r.gamemode = "rr";
                    r.goToMenu("game");
                }
            }

            //back arrow
            if (action == MotionEvent.ACTION_UP &&
                    X < r.c854(100) && Y > r.h() - r.c854(100)) r.goToMenu("start");

            //stats menu
            if (action == MotionEvent.ACTION_UP &&
                    X > r.w() - r.c854(100) && Y > r.h() - r.c854(100)) r.goToMenu("stats");
        } else if (r.menu.equals("stats")) {
            //back arrow
            if (action == MotionEvent.ACTION_UP &&
                    X < r.c854(100) && Y > r.h() - r.c854(100)) r.goToMenu("more");
        } else if (r.menu.equals("game")) {
            r.lastX = X;
            r.lastY = Y;
            if (action == MotionEvent.ACTION_DOWN && !r.channeling) {
                //start r.channeling with a speed dependent on screen-shift speed
                r.waitingForTap = false;
                float sec = (float) Math.min(2.5, r.player.getMaxRange() / Math.max(1.25,r.shiftSpeed) / FRAMES_PER_SECOND - 0.5);
                if (r.gamemode.equals("r.scuttle")) sec *= 0.8;
                else if (r.gamemode.equals("cc") || r.gamemode.equals("rr")) sec *= 0.5;
                r.player.startChannel(sec);
            } else if (action == MotionEvent.ACTION_UP) {
                //release
                if (r.player.isChanneling()) r.player.endChannel();
            }
        } else if (r.menu.equals("limbo")) {
            if (r.lastPressMenu.equals("limbo")) {
                if (action == MotionEvent.ACTION_UP) {
                    if (r.middle.isPressed()) {
                        r.player.reset();
                        r.goToMenu("game");
                    } else if (r.right.isPressed()) {
                        r.goToMenu("start");
                    } else if (r.left.isPressed()) {
                        r.goToMenu("shop");
                    }
                }
            }
        }

        if (action == MotionEvent.ACTION_UP) {
            for (CircleButton cb : r.cbs)
                cb.release();
            for (RoundRectButton rrb : r.rrbs)
                rrb.release();
        }

        return true;
    }
}
