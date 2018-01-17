package com.chrisx.hexflash;

/**
 * Organized in order of priority:
 * @TODO everything
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
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

    private Bitmap poro, scuttler, porosnax, hook, blitzwithporo;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface cd, cd_i, cd_b, cd_bi;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";

    //frame data
    private static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;
    private long millisecondsPerFrame;

    private float lastX, lastY;

    private Paint title;
    private int river = Color.rgb(35,66,94);


    private Poro player;
    private float playerY;
    private float shift, //pixels translated down
        shiftSpeed;
    private int hookAnimation;
    private float maxRange, secToMaxRange;

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

        poro = BitmapFactory.decodeResource(getResources(), R.drawable.poro);
        scuttler = BitmapFactory.decodeResource(getResources(), R.drawable.scuttler);
        porosnax = BitmapFactory.decodeResource(getResources(), R.drawable.porosnax);
        hook = BitmapFactory.decodeResource(getResources(), R.drawable.hook);
        blitzwithporo = BitmapFactory.decodeResource(getResources(), R.drawable.blitzwithporo);

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        nanosecondsPerFrame = (long)1e9 / FRAMES_PER_SECOND;
        millisecondsPerFrame = (long)1e3 / FRAMES_PER_SECOND;

        //initialize fonts
        cd = Typeface.createFromAsset(getAssets(), "fonts/CaviarDreams.ttf");
        cd_b = Typeface.createFromAsset(getAssets(), "fonts/CaviarDreams_Bold.ttf");
        cd_i = Typeface.createFromAsset(getAssets(), "fonts/CaviarDreams_Italic.ttf");
        cd_bi = Typeface.createFromAsset(getAssets(), "fonts/CaviarDreams_BoldItalic.ttf");

        //background
        canvas.drawColor(river);

        //pre-defined paints
        title = newPaint(Color.WHITE);
        title.setTextAlign(Paint.Align.CENTER);
        title.setTextSize(c854(80));
        title.setTypeface(cd_b);

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
                            if (!paused) {
                                if (menu.equals("game")) {
                                    //background
                                    canvas.drawColor(river);

                                    canvas.save();
                                    canvas.translate(0, -shift);

                                    player.draw();
                                    //player.drawHitbox(); //debugging purposes
                                    if (player.isChanneling()) player.update(FRAMES_PER_SECOND, lastX, lastY+shift);

                                    if (!player.isChanneling() && player.getY()-shift < h()/10) {
                                        menu = "hook";
                                        hookAnimation = 0;
                                        playerY = player.getY();
                                    }

                                    canvas.restore();

                                    shiftSpeed = c854((float)(0.75 + 0.02 * frameCount / FRAMES_PER_SECOND));
                                    shift += shiftSpeed;
                                } else if (menu.equals("hook")) {
                                    //background
                                    canvas.drawColor(river);

                                    canvas.save();
                                    canvas.translate(0, -shift);
                                    player.draw();
                                    canvas.restore();

                                    int hookDuration = FRAMES_PER_SECOND * 5/6;
                                    if (hookAnimation < hookDuration / 2) {
                                        float hookY = (playerY+player.getW()-shift)*(hookAnimation/(hookDuration/2f));
                                        drawBmp(hook, new RectF(player.getX()-w()/16,-w()/8*7.5f,player.getX()+w()/16,hookY));
                                    } else {
                                        float hookY = (playerY+player.getW()-shift)*((hookDuration-hookAnimation)/(hookDuration/2f));
                                        drawBmp(hook, new RectF(player.getX()-w()/16,-w()/8*7.5f,player.getX()+w()/16,hookY));
                                        player.setY(hookY-player.getW());
                                    }

                                    if (hookAnimation > hookDuration) {
                                        menu = "gameover";
                                    }
                                    hookAnimation++;
                                } else if (menu.equals("gameover")) {
                                    drawGameoverScreen();
                                    menu = "limbo";
                                }
                            }

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

        if (menu.equals("start")) {
            if (action == MotionEvent.ACTION_UP) {
                menu = "game";
                player = new Poro(canvas, poro);
                shift = frameCount = 0;
            }
        } else if (menu.equals("game")) {
            lastX = X;
            lastY = Y;
            if (action == MotionEvent.ACTION_DOWN) {
                player.startChannel((float)Math.min(2.5, player.getMaxRange() / shiftSpeed / FRAMES_PER_SECOND - 0.5));
            } else if (action == MotionEvent.ACTION_UP) {
                if (player.isChanneling()) player.stopChannel();
            }
        } else if (menu.equals("limbo")) {
            if (action == MotionEvent.ACTION_UP) {
                menu = "game";
                player.reset();
                shift = frameCount = 0;
            }
        }

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
        p.setTypeface(cd);

        return p;
    }

    private float c854(float f) {
        return h() / (854 / f);
    }

    private long getHighScore() {
        return sharedPref.getInt("high_score", 0);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    private void drawBmp(Bitmap bmp, RectF rectF) {
        canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), rectF, null);
    }

    private void drawTitleMenu() {
        canvas.drawText("HEXFLASH", w()/2, h()/2+w()*3/8-c854(46), title);
        drawBmp(porosnax, new RectF(w()/8, h()/2-w()*3/8-c854(30), w()*7/8, h()/2+w()*3/8-c854(30)));
        title.setTypeface(cd);
        title.setTextSize(c854(40));
        canvas.drawText("tap to start", w()/2, h()/2+w()/2, title);
    }

    private void drawGameoverScreen() {
        canvas.drawColor(river);
        canvas.drawBitmap(blitzwithporo, new Rect(3,3,blitzwithporo.getWidth()-2,blitzwithporo.getHeight()-2),
                new RectF(w()/8,h()/2-w()*3/8,w()*7/8,h()/2+w()*3/8), null);
    }
}
