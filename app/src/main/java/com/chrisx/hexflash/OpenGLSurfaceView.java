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

    private boolean touch = false;

    OpenGLSurfaceView(Context context){
        super(context);

        //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        r = new OpenGLRenderer(context);
        setRenderer(r);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        nanosecondsPerFrame = (int)1e9 / FRAMES_PER_SECOND;

        final Handler handler = new Handler();

        //Thread for updates
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (true) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                if (r.transition < r.TRANSITION_MAX / 2) {
                                    if (r.menu.equals("start")) {
                                    } else if (r.menu.equals("more")) {
                                    } else if (r.menu.equals("stats")) {
                                    } else if (r.menu.equals("shop")) {
                                    } else if (r.menu.equals("game")) {
                                        r.shiftSpeed = r.c854(0.75f + 0.02f * r.frameCount / FRAMES_PER_SECOND);

                                        if (!r.waitingForTap) {
                                            if (r.transition == 0) r.movePlatforms();
                                            r.generatePlatforms();

                                            r.updatePoroSnax();

                                            r.updateSnapTraps();

                                            if (r.gamemode.equals("cc") || r.gamemode.equals("rr"))
                                                r.updateBullet();

                                            //mid-channel
                                            if (r.player.isChanneling()) {
                                                r.channeling = true;
                                                r.player.update(r.lastX, r.lastY + r.shift);
                                            }

                                            //just hexflashed
                                            if (r.channeling && !r.player.isChanneling()) {
                                                //if the r.player doesn't land on a platform
                                                if (!r.checkForPlatform(r.player)) {
                                                    r.goToMenu("sink");
                                                    r.gameoverBmp = (r.gamemode.equals("spin") || r.gamemode.equals("rr")) ? r.sadporo_spin : r.sadporo;
                                                    r.sinkAnimation = 0;
                                                }
                                                r.channeling = false;
                                            } else {
                                                r.player.update(); //moving platform
                                                if (r.gamemode.equals("spin") || r.gamemode.equals("rr"))
                                                    r.player.addSpin();
                                            }

                                            //reaches top of screen
                                            if (r.menu.equals("game") && r.player.getY() - r.shift < r.h() / 10) {
                                                r.player.interruptChannel();
                                                r.playerY = r.player.getY();

                                                r.goToMenu("hook");
                                                r.gameoverBmp = r.getHookGameoverBmp();
                                                r.hookAnimation = 0;
                                            }

                                            if (r.gamemode.equals("spin") || r.gamemode.equals("rr"))
                                                r.shiftSpeed *= 0.75;
                                            if (r.transition == 0) {
                                                if (r.gamemode.equals("light") || r.gamemode.equals("rr"))
                                                    r.shift += r.shiftSpeed *= 0.75;
                                                else
                                                    r.shift += r.shiftSpeed;
                                            }
                                        }
                                    } else if (r.menu.equals("hook")) {
                                        r.player.updateAnimations();
                                        if ((r.gamemode.equals("cc") || r.gamemode.equals("rr"))
                                                && r.bullet != null && r.bullet.visible(r.shift)) {
                                            r.bullet.update();
                                        }

                                        int hookDuration = FRAMES_PER_SECOND * 2 / 3;
                                        if (r.hookAnimation < hookDuration / 2) {
                                        } else {
                                            //hook exits screen w/ poro
                                            float hookY = (r.playerY + r.player.getW() - r.shift) * ((hookDuration - r.hookAnimation) / (hookDuration / 2f));
                                            r.player.setY(hookY - r.player.getW() + r.shift);
                                        }

                                        if (r.hookAnimation > hookDuration + FRAMES_PER_SECOND / 3)
                                            r.goToMenu("gameover");

                                        r.hookAnimation++;
                                    } else if (r.menu.equals("sink")) {
                                        r.player.updateAnimations();

                                        //fade effect over poro
                                        int sinkDuration = FRAMES_PER_SECOND;
                                        r.player.setBmp(r.sinking[Math.min(r.sinking.length-1,
                                                r.sinkAnimation/(sinkDuration/r.sinking.length))]);

                                        if ((r.gamemode.equals("cc") || r.gamemode.equals("rr"))
                                                && r.bullet != null && r.bullet.visible(r.shift)) {
                                            r.bullet.update();
                                        }

                                        if (r.sinkAnimation > sinkDuration + FRAMES_PER_SECOND / 3)
                                            r.goToMenu("gameover");

                                        r.sinkAnimation++;
                                    } else if (r.menu.equals("burned")) {
                                        r.player.updateAnimations();

                                        int burnDuration = FRAMES_PER_SECOND / 2;
                                        if (r.burnAnimation > burnDuration + FRAMES_PER_SECOND / 3) {
                                            r.goToMenu("gameover");
                                            r.gameoverBmp = r.burntporo;
                                        }

                                        r.burnAnimation++;
                                    } else if (r.menu.equals("gameover")) {
                                        if (r.transition == 0) r.goToMenu("limbo");
                                    } else if (r.menu.equals("limbo")) {
                                    }
                                }

                                //fading r.transition effect
                                if (r.transition > 0) {
                                    r.transition--;
                                }

                                if (!r.waitingForTap) r.frameCount++;
                            }
                        }
                    });

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();

        //UI Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            requestRender();
                        }
                    });

                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();
    }

    private float prevX, prevY;

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
