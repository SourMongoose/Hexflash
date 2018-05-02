package com.chrisx.hexflash;

import android.content.Context;
import android.opengl.GLSurfaceView;

class OpenGLSurfaceView extends GLSurfaceView {
    private final OpenGLRenderer mRenderer;

    OpenGLSurfaceView(Context context){
        super(context);

        //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new OpenGLRenderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
