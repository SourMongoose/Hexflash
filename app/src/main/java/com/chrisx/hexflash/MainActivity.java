package com.chrisx.hexflash;

/**
 * Organized in order of priority:
 * @TODO everything
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private Bitmap bmp;
    private Canvas canvas;
    private LinearLayout ll;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface trebuchetms;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";

    //frame data
    private static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;
    private long millisecondsPerFrame;

    private float downX, downY;

    private Paint title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //creates the bitmap
        //note: Star 4.5 is 480x854
        bmp = Bitmap.createBitmap(Resources.getSystem().getDisplayMetrics().widthPixels,
                Resources.getSystem().getDisplayMetrics().heightPixels,
                Bitmap.Config.ARGB_8888);

        //creates canvas
        canvas = new Canvas(bmp);

        ll = (LinearLayout) findViewById(R.id.draw_area);
        ll.setBackgroundDrawable(new BitmapDrawable(bmp));

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        nanosecondsPerFrame = (long)1e9 / FRAMES_PER_SECOND;
        millisecondsPerFrame = (long)1e3 / FRAMES_PER_SECOND;

        //initialize fonts
        trebuchetms = Typeface.createFromAsset(getAssets(), "fonts/TrebuchetMS.ttf");

        canvas.drawColor(Color.BLACK);

        //pre-defined paints
        title = newPaint(Color.WHITE);

        //title screen
        drawTitleMenu();

        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (!menu.equals("quit")) {
                    long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {


                            //update canvas
                            ll.invalidate();
                        }
                    });

                    frameCount++;

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX();
        float Y = event.getY();
        int action = event.getAction();

        return true;
    }

    //shorthand for w() and h()
    private float w() {
        return canvas.getWidth();
    }
    private float h() {
        return canvas.getHeight();
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(trebuchetms);

        return p;
    }

    private float convert854(float f) {
        return h() / (854 / f);
    }

    private long getHighScore() {
        return sharedPref.getInt("high_score", 0);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    private void drawTitleMenu() {

    }
}
