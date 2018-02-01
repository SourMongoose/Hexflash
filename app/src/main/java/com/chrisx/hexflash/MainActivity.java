package com.chrisx.hexflash;

/**
 * Organized in order of priority:
 * @TODO different gamemodes (scuttle chaos?)
 * @TODO instructions menu
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
    static Canvas canvas;
    private LinearLayout ll;

    static Bitmap poro, scuttler, porowsnax, porosnax, hook, blitzwithporo, lilypad, sadporo,
            restart, home, shop, play, more, leftarrow;
    private Bitmap gameoverBmp;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface cd, cd_i, cd_b, cd_bi;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start", prevMenu = "start";
    private String lastPressMenu;
    private String gamemode = "classic";

    private int transition = 0;
    private final int TRANSITION_MAX = 40;

    //frame data
    static final int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;
    private long millisecondsPerFrame;

    private float lastX, lastY;

    private Paint title_bold, title, mode, sink, scoreTitle, scoreText;
    private int river = Color.rgb(35,66,94);

    private CircleButton middle, left, right;
    private float offset;

    private RoundRectButton scuttle;

    private Poro player;
    private boolean channeling;
    private float playerY;
    private int score;

    private double ev_scuttle, ev_porosnax; //expected value
    private int num_scuttle = 0, num_porosnax = 0; //actual

    private List<Platform> platforms = new ArrayList<>();
    private List<PoroSnax> snaxlist = new ArrayList<>();

    private float shift, //pixels translated down
        shiftSpeed = 0.75f;
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

        //initialize bitmaps
        poro = BitmapFactory.decodeResource(getResources(), R.drawable.poro_lowres);
        scuttler = BitmapFactory.decodeResource(getResources(), R.drawable.scuttler_lowres);
        porowsnax = BitmapFactory.decodeResource(getResources(), R.drawable.porowsnax);
        porosnax = BitmapFactory.decodeResource(getResources(), R.drawable.porosnax_lowres);
        hook = BitmapFactory.decodeResource(getResources(), R.drawable.hook);
        blitzwithporo = BitmapFactory.decodeResource(getResources(), R.drawable.blitzwithporo);
        lilypad = BitmapFactory.decodeResource(getResources(), R.drawable.lilypad);
        sadporo = BitmapFactory.decodeResource(getResources(), R.drawable.sadporo);
        restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart_lowres);
        home = BitmapFactory.decodeResource(getResources(), R.drawable.home_lowres);
        shop = BitmapFactory.decodeResource(getResources(), R.drawable.shop);
        play = BitmapFactory.decodeResource(getResources(), R.drawable.play);
        more = BitmapFactory.decodeResource(getResources(), R.drawable.more);
        leftarrow = BitmapFactory.decodeResource(getResources(), R.drawable.leftarrow);

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

        mode = new Paint(title);
        mode.setTextSize(c854(35));

        sink = new Paint(Paint.ANTI_ALIAS_FLAG);

        scoreTitle = newPaint(Color.WHITE);
        scoreTitle.setTextSize(c854(20));
        scoreText = newPaint(Color.WHITE);
        scoreText.setTextSize(c854(30));

        //buttons
        offset = c854(125);
        middle = new CircleButton(canvas,w()/2,c854(700),c854(70));
        right = new CircleButton(canvas,w()/2+offset,c854(730),c854(40));
        left = new CircleButton(canvas,w()/2-offset,c854(730),c854(40));

        scuttle = new RoundRectButton(canvas,c480(48),c854(387),c480(432),c854(467),Color.rgb(255,140,0));

        //player
        player = new Poro(canvas);

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
                                    if (menu.equals("start")) {
                                        //title screen
                                        drawTitleMenu();
                                    } else if (menu.equals("more")) {
                                        canvas.drawColor(river);

                                        title_bold.setTextSize(c854(60));
                                        canvas.drawText("GAMEMODES", w()/2, c854(80), title_bold);

                                        scuttle.draw();
                                        canvas.drawText("SCUTTLE MAYHEM", scuttle.getRectF().centerX(),
                                                scuttle.getRectF().centerY()-(mode.ascent()+mode.descent())/2, mode);

                                        //back
                                        drawBmp(leftarrow, new RectF(c854(10),h()-c854(90),c854(90),h()-c854(10)));
                                    } else if (menu.equals("shop")) {
                                        canvas.drawColor(river);

                                        title_bold.setTextSize(c854(60));
                                        canvas.drawText("SHOP", w()/2, c854(80), title_bold);

                                        canvas.drawText("Check back later!", w()/2, h()/2, title);

                                        //back
                                        drawBmp(leftarrow, new RectF(c854(10),h()-c854(90),c854(90),h()-c854(10)));
                                    } else if (menu.equals("game")) {
                                        //background
                                        canvas.drawColor(river);

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift

                                        //draw and update platforms
                                        drawPlatforms();
                                        if (transition == 0) movePlatforms();
                                        generatePlatforms();

                                        //draw and update porosnax
                                        drawPoroSnax();
                                        updatePoroSnax();

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
                                        if (menu.equals("game") && player.getY() - shift < h() / 10) {
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
                                        canvas.translate(0, -shift); //screen shift
                                        drawPlatforms();
                                        drawPoroSnax();
                                        player.draw();
                                        canvas.restore();

                                        int hookDuration = FRAMES_PER_SECOND * 5 / 6;
                                        if (hookAnimation < hookDuration / 2) {
                                            //hook enters screen
                                            float hookY = (playerY + player.getW() - shift) * (hookAnimation / (hookDuration / 2f));
                                            drawBmp(hook, new RectF(player.getX() - w() / 16, -w() / 8 * 7.5f, player.getX() + w() / 16, hookY));
                                        } else {
                                            //hook exits screen w/ poro
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
                                        canvas.translate(0, -shift); //screen shift

                                        player.draw();

                                        //fade effect over poro
                                        int sinkDuration = FRAMES_PER_SECOND;
                                        int alpha = (int) (255. * Math.min(1, Math.pow(1. * sinkAnimation / sinkDuration, 2)));
                                        sink.setShader(new RadialGradient(player.getX(), player.getY(), player.getW()/2+c480(3),
                                                Color.argb(alpha, Color.red(river), Color.green(river), Color.blue(river)),
                                                river, Shader.TileMode.CLAMP));
                                        canvas.drawCircle(player.getX(), player.getY(), player.getW()/2+c480(3), sink);

                                        drawPlatforms();
                                        drawPoroSnax();

                                        canvas.restore();

                                        drawScores();

                                        if (sinkAnimation > sinkDuration + FRAMES_PER_SECOND / 3)
                                            goToMenu("gameover");

                                        sinkAnimation++;
                                    } else if (menu.equals("gameover")) {
                                        drawGameoverScreen();

                                        if (transition == 0) goToMenu("limbo");
                                    } else if (menu.equals("limbo")) {
                                        drawGameoverScreen();
                                    }
                                }

                                //fading transition effect
                                if (transition > 0) {
                                    int t = TRANSITION_MAX / 2, alpha;
                                    if (transition > t) {
                                        alpha = 255 - 255*(transition-t)/t;
                                    } else {
                                        alpha = 255 - 255*(t-transition)/t;
                                    }
                                    canvas.drawColor(Color.argb(alpha,
                                            Color.red(river), Color.green(river), Color.blue(river)));

                                    transition--;
                                }

                                frameCount++;

                                //update canvas
                                ll.invalidate();
                            }
                        }
                    });

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
        if (menu.equals("shop") || menu.equals("more")) {
            goToMenu(prevMenu);
        }
    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX();
        float Y = event.getY();
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            lastPressMenu = menu;

            if (middle.contains(X, Y)) middle.press();
            if (right.contains(X, Y)) right.press();
            if (left.contains(X, Y)) left.press();
            if (scuttle.contains(X, Y)) scuttle.press();
        } else if (action == MotionEvent.ACTION_MOVE){
            if (!middle.contains(X, Y)) middle.release();
            if (!right.contains(X, Y)) right.release();
            if (!left.contains(X, Y)) left.release();
            if (!scuttle.contains(X, Y)) scuttle.release();
        }

        if (menu.equals("start")) {
            if (action == MotionEvent.ACTION_UP) {
                if (middle.isPressed()) {
                    middle.release();
                    player.reset();
                    gamemode = "classic";
                    goToMenu("game");
                } else if (right.isPressed()) {
                    right.release();
                    goToMenu("more");
                } else if (left.isPressed()) {
                    left.release();
                    goToMenu("shop");
                }
            }
        } else if (menu.equals("shop")) {
            //back arrow
            if (X < c854(100) && Y > h()-c854(100)) goToMenu(prevMenu);
        } else if (menu.equals("more")) {
            if (action == MotionEvent.ACTION_UP) {
                if (scuttle.isPressed()) {
                    scuttle.release();
                    player.reset();
                    gamemode = "scuttle";
                    goToMenu("game");
                }
            }

            //back arrow
            if (X < c854(100) && Y > h()-c854(100)) goToMenu(prevMenu);
        } else if (menu.equals("game")) {
            lastX = X;
            lastY = Y;
            if (action == MotionEvent.ACTION_DOWN && !channeling) {
                //start channeling with a speed dependent on screen-shift speed
                float sec = (float)Math.min(2.5, player.getMaxRange() / shiftSpeed / FRAMES_PER_SECOND - 0.5);
                if (gamemode.equals("scuttle")) sec *= 0.8;
                player.startChannel(sec);
            } else if (action == MotionEvent.ACTION_UP) {
                //release
                if (player.isChanneling()) player.endChannel();
            }
        } else if (menu.equals("limbo")) {
            if (lastPressMenu.equals("limbo")) {
                if (action == MotionEvent.ACTION_UP) {
                    if (middle.isPressed()) {
                        middle.release();
                        player.reset();
                        goToMenu("game");
                    } else if (right.isPressed()) {
                        right.release();
                        goToMenu("start");
                    } else if (left.isPressed()) {
                        left.release();
                        goToMenu("shop");
                    }
                }
            }
        }

        return true;
    }

    //shorthand for w() and h()
    static float w() {
        return canvas.getWidth();
    }
    static float h() {
        return canvas.getHeight();
    }

    //creates an instance of Paint set to a given color
    private Paint newPaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTypeface(cd);

        return p;
    }

    static float c480(float f) {
        return w() / (480 / f);
    }
    static float c854(float f) {
        return h() / (854 / f);
    }

    private long getHighScore(String s) {
        return sharedPref.getInt("high_score_"+s, 0);
    }
    private int getPoroSnax() {
        return sharedPref.getInt("porosnax", 0);
    }

    private double toRad(double deg) {
        return Math.PI/180*deg;
    }

    //distance between (x1,y1) and (x2,y2)
    static double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    //draw a bitmap w/o cropping
    private void drawBmp(Bitmap bmp, RectF rectF) {
        canvas.drawBitmap(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), rectF, null);
    }

    private void goToMenu(String s) {
        prevMenu = menu;

        if (s.equals("start")
                || s.equals("game")
                || s.equals("gameover")
                || s.equals("shop")
                || s.equals("more")
                || (s.equals("limbo") && (menu.equals("shop") || menu.equals("start"))))
            transition = TRANSITION_MAX;

        if (s.equals("game") && (menu.equals("start") || menu.equals("limbo") || menu.equals("more"))) {
            //restart
            shift = frameCount = 0;
            score = 0;
            ev_scuttle = ev_porosnax = 0;
            num_scuttle = num_porosnax = 0;
            clearPoroSnax();
            resetPlatforms();
            generatePlatforms();
        }

        if (s.equals("gameover")) {
            //check for new high score
            if (score > getHighScore(gamemode)) {
                editor.putInt("high_score_"+gamemode, score);
                editor.apply();
            }
        }

        menu = s;
    }

    private void drawTitleMenu() {
        canvas.drawColor(river);
        title_bold.setTextSize(c854(80));
        canvas.drawText("HEXFLASH", w()/2, c854(561), title_bold);
        drawBmp(porowsnax, new RectF(w()/2-c854(180), c854(217), w()/2+c854(180), c854(577)));
        
        //play button
        middle.draw();
        RectF tmp = new RectF(middle.getX()-middle.getR()/2f, middle.getY()-middle.getR()/1.8f,
                middle.getX()+middle.getR()/1.6f, middle.getY()+middle.getR()/1.8f);
        drawBmp(play, tmp);

        //shop
        left.draw();
        tmp = new RectF(left.getX()-left.getR()/1.414f, left.getY()-left.getR()/1.414f,
                left.getX()+left.getR()/1.414f, left.getY()+left.getR()/1.414f);
        drawBmp(shop, tmp);

        //more gamemodes
        right.draw();
        tmp = new RectF(right.getX()-right.getR()/1.9f, right.getY()-right.getR()/1.9f,
                right.getX()+right.getR()/1.9f, right.getY()+right.getR()/1.9f);
        drawBmp(more, tmp);
    }

    private void drawGameoverButtons() {
        canvas.drawRect(0,middle.getY()-middle.getR()-c854(5),w(),middle.getY()+middle.getR()+c854(5),newPaint(river));

        //restart button
        middle.draw();
        RectF tmp = new RectF(middle.getX()-middle.getR()/1.8f, middle.getY()-middle.getR()/1.8f,
                middle.getX()+middle.getR()/1.8f, middle.getY()+middle.getR()/1.8f);
        drawBmp(restart, tmp);

        //shop
        left.draw();
        tmp = new RectF(left.getX()-left.getR()/1.414f, left.getY()-left.getR()/1.414f,
                left.getX()+left.getR()/1.414f, left.getY()+left.getR()/1.414f);
        drawBmp(shop, tmp);

        //back to home
        right.draw();
        tmp = new RectF(right.getX()-right.getR()/1.9f, right.getY()-right.getR()/1.9f,
                right.getX()+right.getR()/1.9f, right.getY()+right.getR()/1.9f);
        drawBmp(home, tmp);
    }
    private void drawGameoverScreen() {
        canvas.drawColor(river);
        canvas.drawBitmap(gameoverBmp, new Rect(3,3,gameoverBmp.getWidth()-2,gameoverBmp.getHeight()-2),
                new RectF(w()/2-c854(180),h()/2-c854(180),w()/2+c854(180),h()/2+c854(180)), null);
        title_bold.setTextSize(c854(60));
        canvas.drawText("GAME OVER", w()/2, c854(155), title_bold);
        canvas.drawText("you scored: " + score, w()/2, c854(200), title);

        drawGameoverButtons();
    }

    private void drawScores() {
        scoreTitle.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("score", c480(10), c854(25), scoreTitle);
        scoreTitle.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("high", w()-c480(10), c854(25), scoreTitle);
        scoreText.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(score+"", c480(10), c854(60), scoreText);
        scoreText.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(getHighScore(gamemode)+"", w()-c480(10), c854(60), scoreText);
    }

    //delete all platforms and initialize one lilypad
    private void resetPlatforms() {
        platforms.clear();
        platforms.add(new Platform(canvas,w()/2,h()/2));
        player.setPlatform(platforms.get(0));
    }
    private void generatePlatforms() {
        //check that at least one platform exists
        if (platforms.isEmpty()) resetPlatforms();

        //remove platforms that have gone past the top of the screen
        while (!platforms.isEmpty() && !platforms.get(0).visible(shift)) platforms.remove(0);

        //while the lowest platform is visible
        while (platforms.get(platforms.size()-1).visible(shift)) {
            Platform prev = platforms.get(platforms.size()-1);
            float platformW = prev.getW();

            float dist = (float)(platformW*2 +
                    Math.random() * Math.min(0.5+score/1000.,4) * w());
            dist += prev.getX() - platformW/2;

            int rows = (int)(dist / (w()-platformW));
            dist %= (w()-platformW);

            if (platforms.get(platforms.size()-1).getSpeed() > 0) rows++;
            if (gamemode.equals("scuttle")) rows = 1;

            float newX = platformW/2 + dist;
            float newY = (float)(prev.getY() + (rows+Math.random()/2) * platformW);
            if (distance(prev.getX(),prev.getY(),newX,newY) < player.getMaxRange()) {
                //probability of platform being a scuttle crab
                double prob;
                if (gamemode.equals("scuttle")) prob = 1;
                else prob = 0.5 * score/1000 / 15;
                ev_scuttle += prob;
                double adjProb = prob * (1 + (ev_scuttle-num_scuttle)/1.5);

                if (rows > 0 && Math.random() < adjProb) {
                    platforms.add(new Platform(canvas, newX, newY, (float) (1 + shiftSpeed + 0.5 * Math.random())));
                    num_scuttle++;
                } else {
                    platforms.add(new Platform(canvas, newX, newY));
                }
                //add a porosnax?
                double prob2 = 0.2;
                ev_porosnax += prob2;
                double adjProb2 = prob2 * (1 + (ev_porosnax-num_porosnax)/2);
                if (Math.random() < adjProb2) {
                    snaxlist.add(new PoroSnax(canvas, platforms.get(platforms.size() - 1)));
                    num_porosnax++;
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

    //find the platform that the player has landed on, return false if lands in water
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

    private void clearPoroSnax() {
        snaxlist.clear();
    }
    private void drawPoroSnax() {
        for (PoroSnax p : snaxlist)
            if (p.visible(shift)) p.draw();
    }
    private void updatePoroSnax() {
        //remove porosnax that have gone off the screen
        while (!snaxlist.isEmpty() && !snaxlist.get(0).visible(shift)
                && snaxlist.get(0).getY()-shift < h()) snaxlist.remove(0);

        //check if porosnax have been eaten
        for (int i = snaxlist.size()-1; i >= 0; i--) {
            if (distance(snaxlist.get(i).getX(),snaxlist.get(i).getY(),player.getX(),player.getY())
                    < (player.getW()+snaxlist.get(i).getW())/2) {
                snaxlist.remove(i);
                editor.putInt("porosnax", getPoroSnax()+1);
                editor.apply();
            }
        }
    }
}
