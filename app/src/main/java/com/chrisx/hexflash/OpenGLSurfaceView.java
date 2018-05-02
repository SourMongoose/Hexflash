package com.chrisx.opengltest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.MotionEvent;

class OpenGLSurfaceView extends GLSurfaceView {
    private final OpenGLRenderer mRenderer;

    //frame data
    static int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    private boolean touch = false;

    OpenGLSurfaceView(Context context){
        super(context);

        //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new OpenGLRenderer(context);
        setRenderer(mRenderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        nanosecondsPerFrame = (int)1e9 / FRAMES_PER_SECOND;

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (true) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (touch) mRenderer.rotate(-2);
                            else mRenderer.rotate(2);

                            requestRender();
                        }
                    });

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();
    }

    private float prevX, prevY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        int action = e.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            touch = true;
        } else if (action == MotionEvent.ACTION_UP) {
            touch = false;
        }

        prevX = x;
        prevY = y;
        return true;
    }
}
