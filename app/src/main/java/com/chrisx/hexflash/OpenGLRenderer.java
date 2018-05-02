package com.chrisx.hexflash;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class OpenGLRenderer implements GLSurfaceView.Renderer {
    private int width, height;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //Create the shaders, images
        int vertexShader = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
                GraphicTools.vs_Image);
        int fragmentShader = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
                GraphicTools.fs_Image);

        GraphicTools.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(GraphicTools.sp_Image, vertexShader);
        GLES20.glAttachShader(GraphicTools.sp_Image, fragmentShader);
        GLES20.glLinkProgram(GraphicTools.sp_Image);

        //Set our shader programm
        GLES20.glUseProgram(GraphicTools.sp_Image);
    }

    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        //Redraw background color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        //Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1, 0, 0, -1, 0, 1, 0);

        //Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        /*
        //Set center of rotation at center of image
        Matrix.translateM(mMVPMatrix, 0, width/2, height/2, 0);
        //Create a rotation transformation for the triangle
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, 1);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        //Undo previous translation
        Matrix.translateM(scratch, 0, -width/2, -height/2, 0);
        */

    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;

        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, 0, height, 1000, -1000);
    }
}
