package com.chrisx.hexflash;

/**
 * Organized in order of priority:
 * @TODO *optional* prevent hexflashing off the screen
 * @TODO bugfix: blitz hooks poro after hexflashing backwards
 * @TODO instructions menu
 * @TODO porosnax
 *
 * @TODO shop with different poro skins?
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Bitmap bmp;
    private Canvas canvas;
    private LinearLayout ll;

    static Bitmap poro, scuttler, porosnax, hook, blitzwithporo, lilypad, sadporo;
    private Bitmap gameoverBmp;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface cd, cd_i, cd_b, cd_bi;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start";
    private boolean pressedDuringLimbo = false;

    private int transition = 0;
    private final int TRANSITION_MAX = 40;

    //frame data
    static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;
    private long millisecondsPerFrame;

    private float lastX, lastY;

    private Paint title_bold, title, sink, scoreTitle, scoreText;
    private int river = Color.rgb(35,66,94);


    private Poro player;
    private boolean channeling;
    private float playerY;
    private int score;

    private double ev_scuttle; //expected value
    private int num_scuttle; //actual

    private List<Platform> platforms = new ArrayList<>();

    private float shift, //pixels translated down
        shiftSpeed;
    private int hookAnimation, sinkAnimation;
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

        poro = BitmapFactory.decodeResource(getResources(), R.drawable.poro_lowres);
        scuttler = BitmapFactory.decodeResource(getResources(), R.drawable.scuttler);
        porosnax = BitmapFactory.decodeResource(getResources(), R.drawable.porosnax);
        hook = BitmapFactory.decodeResource(getResources(), R.drawable.hook);
        blitzwithporo = BitmapFactory.decodeResource(getResources(), R.drawable.blitzwithporo);
        lilypad = BitmapFactory.decodeResource(getResources(), R.drawable.lilypad);
        sadporo = BitmapFactory.decodeResource(getResources(), R.drawable.sadporo);

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
        title_bold = newPaint(Color.WHITE);
        title_bold.setTextAlign(Paint.Align.CENTER);
        title_bold.setTextSize(c854(80));
        title_bold.setTypeface(cd_b);

        title = new Paint(title_bold);
        title.setTextSize(c854(40));
        title.setTypeface(cd);

        sink = new Paint(Paint.ANTI_ALIAS_FLAG);

        scoreTitle = newPaint(Color.WHITE);
        scoreTitle.setTextSize(c854(20));
        scoreText = newPaint(Color.WHITE);
        scoreText.setTextSize(c854(30));

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
                                if (transition < TRANSITION_MAX / 2) {
                                    if (menu.equals("game")) {
                                        //background
                                        canvas.drawColor(river);

                                        canvas.save();
                                        canvas.translate(0, -shift);

                                        drawPlatforms();
                                        if (transition == 0) movePlatforms();
                                        generatePlatforms();

                                        player.draw();
                                        //player.drawHitbox(); //debugging purposes

                                        //mid-channel
                                        if (player.isChanneling()) {
                                            channeling = true;
                                            player.update(lastX, lastY + shift);
                                        }

                                        //just hexflashed
                                        if (channeling && !player.isChanneling()) {
                                            //if the player doesn't land on a platform
                                            if (!checkForPlatform()) {
                                                goToMenu("sink");
                                                gameoverBmp = sadporo;
                                                sinkAnimation = 0;
                                            }
                                            channeling = false;
                                        } else {
                                            player.update(); //moving platform
                                        }

                                        //reaches top of screen
                                        if (player.getY() - shift < h() / 10) {
                                            player.interruptChannel();
                                            playerY = player.getY();

                                            goToMenu("hook");
                                            gameoverBmp = blitzwithporo;
                                            hookAnimation = 0;
                                        }

                                        canvas.restore();

                                        drawScores();

                                        shiftSpeed = c854((float) (0.75 + 0.02 * frameCount / FRAMES_PER_SECOND));
                                        if (transition == 0) shift += shiftSpeed;
                                    } else if (menu.equals("hook")) {
                                        //background
                                        canvas.drawColor(river);

                                        canvas.save();
                                        canvas.translate(0, -shift);
                                        drawPlatforms();
                                        player.draw();
                                        canvas.restore();

                                        int hookDuration = FRAMES_PER_SECOND * 5 / 6;
                                        if (hookAnimation < hookDuration / 2) {
                                            float hookY = (playerY + player.getW() - shift) * (hookAnimation / (hookDuration / 2f));
                                            drawBmp(hook, new RectF(player.getX() - w() / 16, -w() / 8 * 7.5f, player.getX() + w() / 16, hookY));
                                        } else {
                                            float hookY = (playerY + player.getW() - shift) * ((hookDuration - hookAnimation) / (hookDuration / 2f));
                                            drawBmp(hook, new RectF(player.getX() - w() / 16, -w() / 8 * 7.5f, player.getX() + w() / 16, hookY));
                                            player.setY(hookY - player.getW());
                                        }

                                        drawScores();

                                        if (hookAnimation > hookDuration + FRAMES_PER_SECOND / 3)
                                            goToMenu("gameover");

                                        hookAnimation++;
                                    } else if (menu.equals("sink")) {
                                        //background
                                        canvas.drawColor(river);

                                        canvas.save();
                                        canvas.translate(0, -shift);
                                        drawPlatforms();
                                        player.draw();

                                        int sinkDuration = FRAMES_PER_SECOND;
                                        int alpha = (int) (255. * Math.min(1, Math.pow(1. * sinkAnimation / sinkDuration, 2)));
                                        sink.setShader(new RadialGradient(player.getX(), player.getY(), player.getW()/2+c480(3),
                                                Color.argb(alpha, Color.red(river), Color.green(river), Color.blue(river)),
                                                river, Shader.TileMode.CLAMP));
                                        canvas.drawCircle(player.getX(), player.getY(), player.getW()/2+c480(3), sink);

                                        canvas.restore();

                                        drawScores();

                                        if (sinkAnimation > sinkDuration + FRAMES_PER_SECOND / 3)
                                            goToMenu("gameover");

                                        sinkAnimation++;
                                    } else if (menu.equals("gameover")) {
                                        drawGameoverScreen();

                                        if (transition == 0) goToMenu("limbo");
                                    }
                                }

                                if (transition > 0) {
                                    int t = TRANSITION_MAX / 2, alpha;
                                    if (transition > t) {
                                        alpha = 255 - 255*(transition-t)/t;
                                    } else {
                                        alpha = 255 - 255*(t-transition)/t;
                                    }
                                    canvas.drawColor(Color.argb(alpha,
                                            Color.red(river), Color.green(river), Color.blue(river)));
                                }
                            }

                            //update canvas
                            ll.invalidate();
                        }
                    });

                    if (transition > 0) transition--;
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
                player = new Poro(canvas);
                goToMenu("game");
            }
        } else if (menu.equals("game")) {
            lastX = X;
            lastY = Y;
            if (action == MotionEvent.ACTION_DOWN && !channeling) {
                player.startChannel((float)Math.min(2.5, player.getMaxRange() / shiftSpeed / FRAMES_PER_SECOND - 0.5));
            } else if (action == MotionEvent.ACTION_UP) {
                if (player.isChanneling()) player.endChannel();
            }
        } else if (menu.equals("limbo")) {
            if (action == MotionEvent.ACTION_DOWN) pressedDuringLimbo = true;
            if (pressedDuringLimbo && action == MotionEvent.ACTION_UP) {
                player.reset();
                goToMenu("game");

                pressedDuringLimbo = false;
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

    private float c480(float f) {
        return w() / (450 / f);
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

    private double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    private void drawBmp(Bitmap bmp, RectF rectF) {
        canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), rectF, null);
    }

    private void goToMenu(String s) {
        if (s.equals("game") || s.equals("gameover"))
            transition = TRANSITION_MAX;

        if (s.equals("game") && (menu.equals("start") || menu.equals("limbo"))) {
            shift = frameCount = score = 0;
            resetPlatforms();
            generatePlatforms();
        }

        if (s.equals("gameover")) {
            if (score > getHighScore()) {
                editor.putInt("high_score", score);
                editor.apply();
            }
        }

        menu = s;
    }

    private void drawTitleMenu() {
        canvas.drawText("HEXFLASH", w()/2, c854(561), title_bold);
        drawBmp(porosnax, new RectF(w()/2-c854(180), c854(217), w()/2+c854(180), c854(577)));
        canvas.drawText("tap to start", w()/2, c854(667), title);
    }

    private void drawGameoverScreen() {
        canvas.drawColor(river);
        canvas.drawBitmap(gameoverBmp, new Rect(3,3,gameoverBmp.getWidth()-2,gameoverBmp.getHeight()-2),
                new RectF(w()/2-c854(180),h()/2-c854(180),w()/2+c854(180),h()/2+c854(180)), null);
        title_bold.setTextSize(c854(70));
        canvas.drawText("GAME OVER", w()/2, c854(160), title_bold);
        canvas.drawText("you scored: " + score, w()/2, c854(200), title);
        canvas.drawText("play again?", w()/2, c854(700), title);
    }

    private void drawScores() {
        scoreTitle.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("score", c480(10), c854(25), scoreTitle);
        scoreTitle.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("high", w()-c480(10), c854(25), scoreTitle);
        scoreText.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(score+"", c480(10), c854(60), scoreText);
        scoreText.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(getHighScore()+"", w()-c480(10), c854(60), scoreText);
    }

    private void resetPlatforms() {
        platforms.clear();
        platforms.add(new Platform(canvas,w()/2,h()/2));
        player.setPlatform(platforms.get(0));
    }
    private void generatePlatforms() {
        if (platforms.isEmpty()) resetPlatforms();

        //remove platforms that have gone past the top of the screen
        while (!platforms.get(0).visible(shift)) platforms.remove(0);

        while (platforms.get(platforms.size()-1).visible(shift)) {
            Platform prev = platforms.get(platforms.size()-1);
            float platformW = prev.getW();

            float dist = (float)(platformW*2 +
                    Math.random() * Math.min(0.5+score/1000.,4) * w());
            dist += prev.getX() - platformW/2;

            int rows = (int)(dist / (w()-platformW));
            dist %= (w()-platformW);

            if (platforms.get(platforms.size()-1).getSpeed() > 0) rows++;

            float newX = platformW/2 + dist;
            float newY = (float)(prev.getY() + (rows+Math.random()/2) * platformW);
            if (distance(prev.getX(),prev.getY(),newX,newY) < player.getMaxRange()) {
                double prob = 0.5 * score/1000 / 15;
                ev_scuttle += prob;
                double adjProb = prob * (1 + (ev_scuttle-num_scuttle)/2);

                if (rows > 0 && Math.random() < adjProb) {
                    platforms.add(new Platform(canvas, newX, newY, (float) (3 + 0.5 * Math.random())));
                    num_scuttle++;
                } else {
                    platforms.add(new Platform(canvas, newX, newY));
                }
            }
        }
    }
    private void drawPlatforms() {
        for (Platform p : platforms)
            if (p.visible(shift)) p.draw();
    }
    private void movePlatforms() {
        for (Platform p : platforms)
            if (p.visible(shift)) p.update();
    }

    private boolean checkForPlatform() {
        for (Platform p : platforms) {
            double dist = distance(player.getX(),player.getY(),p.getX(),p.getY());
            if (dist < (player.getW() + p.getW()) / 2) {
                player.setPlatform(p);
                score = (int)Math.max(score, (player.getY()-h()/2)/h()*1000); //number of screens travelled * 1000
                return true;
            }
        }
        return false;
    }
}
