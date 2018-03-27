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

    static Bitmap poro, scuttler, titlescreen, porosnax, snaptrap, snarefx, hook_classic, hook_iblitz,
            icon_classic, icon_iblitz, blitzwithporo, iblitzwithporo, lilypad, lilypadlotus, sadporo,
            sadporo_spin, riverbmp, restart, home, shop, play, more, leftarrow, maxrange, currrange,
            indicator, bubble, border, line;
    static Bitmap[] sinking;
    private Bitmap gameoverBmp;

    private String blitzskins[] = {"classic", "iblitz"};
    private int nBlitz = blitzskins.length;
    private RectF blitzskins_rectf[] = new RectF[nBlitz];
    private float ICON_WIDTH, BLITZSKINS_Y;

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
    private float lastX2, lastY2; //for multiflash

    private Paint title_bold, title, mode, scoreTitle, scoreText;
    private int river = Color.rgb(35,66,94);

    private CircleButton middle, left, right;
    private float offset, MIDDLE_Y1, MIDDLE_Y2;

    private RoundRectButton light, spin, scuttle, snare, multi;

    private CircleButton cbs[];
    private RoundRectButton rrbs[];

    private Poro player, player2;
    private boolean channeling, channeling2;
    private float playerY, playerX;
    private int score;

    private double lightning, wait;
    private boolean jumped = false;
    private final double MAX_LIGHTNING = 1.5;
    private final double MAX_WAIT = 2.75;

    private double ev_scuttle, ev_porosnax, ev_snaptrap; //expected value
    private int num_scuttle = 0, num_porosnax = 0, num_snaptrap = 0; //actual

    private List<Platform> platforms = new ArrayList<>();
    private List<PoroSnax> snaxlist = new ArrayList<>();
    private List<SnapTrap> snaptraps = new ArrayList<>();

    private float shift, //pixels translated down
        shiftSpeed = 0.75f;
    private int hookAnimation, sinkAnimation;

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
        titlescreen = BitmapFactory.decodeResource(getResources(), R.drawable.titlescreen);
        porosnax = BitmapFactory.decodeResource(getResources(), R.drawable.porosnax_lowres);
        snaptrap = BitmapFactory.decodeResource(getResources(), R.drawable.snaptrap);
        snarefx = BitmapFactory.decodeResource(getResources(), R.drawable.snarefx);
        hook_classic = BitmapFactory.decodeResource(getResources(), R.drawable.hook_classic);
        hook_iblitz = BitmapFactory.decodeResource(getResources(), R.drawable.hook_iblitz);
        icon_classic = BitmapFactory.decodeResource(getResources(), R.drawable.icon_classic);
        icon_iblitz = BitmapFactory.decodeResource(getResources(), R.drawable.icon_iblitz);
        blitzwithporo = BitmapFactory.decodeResource(getResources(), R.drawable.blitzwithporo);
        iblitzwithporo = BitmapFactory.decodeResource(getResources(), R.drawable.iblitzwithporo);
        lilypad = BitmapFactory.decodeResource(getResources(), R.drawable.lilypad_nolotus_lowres);
        lilypadlotus = BitmapFactory.decodeResource(getResources(), R.drawable.lilypad_lotus_lowres);
        sadporo = BitmapFactory.decodeResource(getResources(), R.drawable.sadporo);
        sadporo_spin = BitmapFactory.decodeResource(getResources(), R.drawable.sadporo_spin);
        riverbmp = BitmapFactory.decodeResource(getResources(), R.drawable.river_mediumres);
        restart = BitmapFactory.decodeResource(getResources(), R.drawable.restart_lowres);
        home = BitmapFactory.decodeResource(getResources(), R.drawable.home_lowres);
        shop = BitmapFactory.decodeResource(getResources(), R.drawable.shop);
        play = BitmapFactory.decodeResource(getResources(), R.drawable.play);
        more = BitmapFactory.decodeResource(getResources(), R.drawable.more);
        leftarrow = BitmapFactory.decodeResource(getResources(), R.drawable.leftarrow);
        maxrange = BitmapFactory.decodeResource(getResources(), R.drawable.maxrange);
        currrange = BitmapFactory.decodeResource(getResources(), R.drawable.currrange);
        indicator = BitmapFactory.decodeResource(getResources(), R.drawable.indicator_lowres);
        bubble = BitmapFactory.decodeResource(getResources(), R.drawable.bubble);
        border = BitmapFactory.decodeResource(getResources(), R.drawable.border);

        sinking = new Bitmap[15];
        for (int i = 0; i < sinking.length; i++)
            sinking[i] = BitmapFactory.decodeResource(getResources(), R.drawable.poro01+i);

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

        scoreTitle = newPaint(Color.WHITE);
        scoreTitle.setTextSize(c854(20));
        scoreText = newPaint(Color.WHITE);
        scoreText.setTextSize(c854(30));

        //buttons
        offset = c854(125);
        MIDDLE_Y1 = c854(720);
        MIDDLE_Y2 = c854(290);
        middle = new CircleButton(canvas,w()/2,MIDDLE_Y1,c854(70));
        right = new CircleButton(canvas,w()/2+offset,MIDDLE_Y1+c854(30),c854(40));
        left = new CircleButton(canvas,w()/2-offset,MIDDLE_Y1+c854(30),c854(40));
        cbs = new CircleButton[]{middle, right, left};

        light = new RoundRectButton(canvas,c480(48),c854(187),c480(432),c854(267),Color.rgb(255,68,68));
        spin = new RoundRectButton(canvas,c480(48),c854(287),c480(432),c854(367),Color.rgb(255,140,0));
        scuttle = new RoundRectButton(canvas,c480(48),c854(387),c480(432),c854(467),Color.rgb(54,173,31));
        snare = new RoundRectButton(canvas,c480(48),c854(487),c480(432),c854(567),Color.rgb(80,163,215));
        multi = new RoundRectButton(canvas,c480(48),c854(587),c480(432),c854(667),Color.rgb(178,55,170));
        rrbs = new RoundRectButton[]{light, spin, scuttle, snare, multi};

        //blitz skins
        ICON_WIDTH = c854(100);
        BLITZSKINS_Y = c854(225);
        float totalWidth = ICON_WIDTH*nBlitz + ICON_WIDTH*(nBlitz-1)/4;
        for (int i = 0; i < nBlitz; i++) {
            float x = w()/2 - totalWidth/2 + i * (ICON_WIDTH*1.25f);
            blitzskins_rectf[i] = new RectF(x,BLITZSKINS_Y,x+ICON_WIDTH,BLITZSKINS_Y+ICON_WIDTH);
        }

        //player
        player = new Poro(canvas);
        player2 = new Poro(canvas);

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

                                        float tmp = (mode.ascent() + mode.descent()) / 2;
                                        light.draw();
                                        canvas.drawText("NIGHT LIGHTS", light.getRectF().centerX(),
                                                light.getRectF().centerY()-tmp, mode);
                                        spin.draw();
                                        canvas.drawText("SPIN TO WIN", spin.getRectF().centerX(),
                                                spin.getRectF().centerY()-tmp, mode);
                                        scuttle.draw();
                                        canvas.drawText("SCUTTLE TROUBLE", scuttle.getRectF().centerX(),
                                                scuttle.getRectF().centerY()-tmp, mode);
                                        snare.draw();
                                        canvas.drawText("SNARE FAIR", snare.getRectF().centerX(),
                                                snare.getRectF().centerY()-tmp, mode);
                                        multi.draw();
                                        canvas.drawText("MULTIFLASH", multi.getRectF().centerX(),
                                                multi.getRectF().centerY()-tmp, mode);

                                        //back
                                        drawBmp(leftarrow, new RectF(c854(10),h()-c854(90),c854(90),h()-c854(10)));
                                    } else if (menu.equals("shop")) {
                                        canvas.drawColor(river);

                                        title_bold.setTextSize(c854(60));
                                        canvas.drawText("SHOP", w()/2, c854(80), title_bold);

                                        //blitzcrank skins
                                        canvas.drawText("BLITZ SKINS", w()/2, BLITZSKINS_Y-ICON_WIDTH/2, title);
                                        for (int i = 0; i < nBlitz; i++) {
                                            drawBmp(getIconBmp(blitzskins[i]), blitzskins_rectf[i]);
                                            if (getBlitzSkin().equals(blitzskins[i])) {
                                                RectF rf = blitzskins_rectf[i];
                                                drawBmp(border, new RectF(rf.left-ICON_WIDTH/8,rf.top-ICON_WIDTH/8,
                                                        rf.right+ICON_WIDTH/8,rf.bottom+ICON_WIDTH/8));
                                            }
                                        }

                                        //porosnax count
                                        drawBmp(porosnax, new RectF(w()-c854(75),h()-c854(75),w()-c854(25),h()-c854(25)));
                                        title.setTextAlign(Paint.Align.RIGHT);
                                        canvas.drawText(getPoroSnax()+"", w()-c854(85), h()-c854(50)-(title.ascent()+title.descent())/2, title);
                                        title.setTextAlign(Paint.Align.CENTER);

                                        //back
                                        drawBmp(leftarrow, new RectF(c854(10),h()-c854(90),c854(90),h()-c854(10)));
                                    } else if (menu.equals("game")) {
                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift

                                        //draw and update platforms
                                        drawPlatforms();
                                        if (transition == 0) movePlatforms();
                                        generatePlatforms();

                                        //draw and update porosnax
                                        drawPoroSnax();
                                        updatePoroSnax();

                                        //draw and update snap-traps
                                        drawSnapTraps();
                                        updateSnapTraps();

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
                                            if (!checkForPlatform(player)) {
                                                player2.interruptChannel();
                                                goToMenu("sink");
                                                gameoverBmp = gamemode.equals("spin") ? sadporo_spin : sadporo;
                                                sinkAnimation = 0;
                                            }
                                            channeling = false;
                                        } else {
                                            player.update(); //moving platform
                                            if (gamemode.equals("spin")) player.addSpin();
                                        }

                                        //reaches top of screen
                                        if (menu.equals("game") && player.getY() - shift < h() / 10) {
                                            player.interruptChannel();
                                            player2.interruptChannel();
                                            playerY = player.getY();

                                            goToMenu("hook");
                                            gameoverBmp = getHookGameoverBmp();
                                            hookAnimation = 0;
                                        }

                                        //multiflash
                                        if (gamemode.equals("multi")) {
                                            player2.draw();

                                            //mid-channel
                                            if (player2.isChanneling()) {
                                                channeling2 = true;
                                                player2.update(lastX2, lastY2 + shift);
                                            }

                                            //just hexflashed
                                            if (channeling2 && !player2.isChanneling()) {
                                                //if the player doesn't land on a platform
                                                if (!checkForPlatform(player2)) {
                                                    player.interruptChannel();
                                                    goToMenu("sink2");
                                                    gameoverBmp = gamemode.equals("spin") ? sadporo_spin : sadporo;
                                                    sinkAnimation = 0;
                                                }
                                                channeling2 = false;
                                            } else {
                                                player2.update(); //moving platform
                                            }

                                            //reaches top of screen
                                            if (menu.equals("game") && player2.getY() - shift < h() / 10) {
                                                player.interruptChannel();
                                                player2.interruptChannel();
                                                playerY = player2.getY();

                                                goToMenu("hook2");
                                                gameoverBmp = getHookGameoverBmp();
                                                hookAnimation = 0;
                                            }
                                        }

                                        canvas.restore();

                                        if (gamemode.equals("light")) drawLightning();

                                        drawScores();

                                        shiftSpeed = c854((float) (0.75 + 0.02 * frameCount / FRAMES_PER_SECOND));
                                        if (gamemode.equals("spin"))
                                            shiftSpeed *= 0.75;
                                        if (transition == 0) {
                                            if (gamemode.equals("light") || gamemode.equals("multi"))
                                                shift += shiftSpeed *= 0.75;
                                            else
                                                shift += shiftSpeed;
                                        }
                                    } else if (menu.equals("hook") || menu.equals("hook2")) {
                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift
                                        drawPlatforms();
                                        drawPoroSnax();
                                        drawSnapTraps();
                                        player.draw();
                                        if (gamemode.equals("multi")) player2.draw();
                                        canvas.restore();

                                        int hookDuration = FRAMES_PER_SECOND * 5 / 6;
                                        float hookWidth = w() / 6;
                                        if (hookAnimation < hookDuration / 2) {
                                            //hook enters screen
                                            float hookY = (playerY + player.getW() - shift) * (hookAnimation / (hookDuration / 2f));
                                            if (menu.equals("hook2"))
                                                drawBmp(getHookBmp(), new RectF(player2.getX() - hookWidth/2, hookY - hookWidth*3,
                                                        player2.getX() + hookWidth/2, hookY));
                                            else
                                                drawBmp(getHookBmp(), new RectF(player.getX() - hookWidth/2, hookY - hookWidth*3,
                                                        player.getX() + hookWidth/2, hookY));
                                        } else {
                                            //hook exits screen w/ poro
                                            float hookY = (playerY + player.getW() - shift) * ((hookDuration - hookAnimation) / (hookDuration / 2f));
                                            if (menu.equals("hook2")) {
                                                drawBmp(getHookBmp(), new RectF(player2.getX() - hookWidth/2, hookY - hookWidth*3,
                                                        player2.getX() + hookWidth/2, hookY));
                                                player2.setY(hookY - player2.getW() + shift);
                                            } else {
                                                drawBmp(getHookBmp(), new RectF(player.getX() - hookWidth / 2, hookY - hookWidth * 3,
                                                        player.getX() + hookWidth / 2, hookY));
                                                player.setY(hookY - player.getW() + shift);
                                            }
                                        }

                                        drawScores();

                                        if (hookAnimation > hookDuration + FRAMES_PER_SECOND / 3)
                                            goToMenu("gameover");

                                        hookAnimation++;
                                    } else if (menu.equals("sink") || menu.equals("sink2")) {
                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift

                                        drawPlatforms();
                                        drawPoroSnax();
                                        drawSnapTraps();

                                        player.draw();
                                        if (gamemode.equals("multi")) player2.draw();

                                        //fade effect over poro
                                        int sinkDuration = FRAMES_PER_SECOND;
                                        if (menu.equals("sink2"))
                                            player2.setBmp(sinking[Math.min(sinking.length-1,
                                                    sinkAnimation/(sinkDuration/sinking.length))]);
                                        else
                                            player.setBmp(sinking[Math.min(sinking.length-1,
                                                    sinkAnimation/(sinkDuration/sinking.length))]);

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
        float X = event.getX(event.getActionIndex());
        float Y = event.getY(event.getActionIndex());
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            lastPressMenu = menu;

            for (CircleButton cb : cbs)
                if (cb.contains(X, Y)) cb.press();
            for (RoundRectButton rrb : rrbs)
                if (rrb.contains(X, Y)) rrb.press();
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (CircleButton cb : cbs)
                if (!cb.contains(X, Y)) cb.release();
            for (RoundRectButton rrb : rrbs)
                if (!rrb.contains(X, Y)) rrb.release();
        }

        if (menu.equals("start")) {
            if (action == MotionEvent.ACTION_UP) {
                if (middle.isPressed()) {
                    player.reset();
                    gamemode = "classic";
                    goToMenu("game");
                } else if (right.isPressed()) {
                    goToMenu("more");
                } else if (left.isPressed()) {
                    goToMenu("shop");
                }
            }
        } else if (menu.equals("shop")) {
            if (action == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < nBlitz; i++) {
                    if (blitzskins_rectf[i].contains(X, Y)) {
                        editor.putString("blitzskin", blitzskins[i]);
                        editor.apply();
                    }
                }
            }

            //back arrow
            if (action == MotionEvent.ACTION_UP &&
                    X < c854(100) && Y > h()-c854(100)) goToMenu(prevMenu);
        } else if (menu.equals("more")) {
            if (action == MotionEvent.ACTION_UP) {
                for (RoundRectButton rrb : rrbs) {
                    if (rrb.isPressed()) {
                        player.reset();
                        player2.reset();

                        if (rrb == scuttle) gamemode = "scuttle";
                        else if (rrb == snare) gamemode = "snare";
                        else if (rrb == spin) gamemode = "spin";
                        else if (rrb == light) gamemode = "light";
                        else if (rrb == multi) gamemode = "multi";

                        if (gamemode.equals("multi")) {
                            player.setX(w()/4);
                            player2.setX(w()*3/4);
                        }

                        goToMenu("game");
                        break;
                    }
                }
            }

            //back arrow
            if (action == MotionEvent.ACTION_UP &&
                    X < c854(100) && Y > h()-c854(100)) goToMenu(prevMenu);
        } else if (menu.equals("game")) {
            if (gamemode.equals("multi")) {
                if (X < w() / 2) {
                    lastX = X;
                    lastY = Y;
                    if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
                            && !channeling) {
                        float sec = (float) Math.min(2.5, player.getMaxRange() / shiftSpeed / FRAMES_PER_SECOND - 0.5);
                        player.startChannel(sec);
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                        if (player.isChanneling()) player.endChannel();
                    }
                } else {
                    lastX2 = X;
                    lastY2 = Y;
                    if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
                            && !channeling2) {
                        float sec = (float) Math.min(2.5, player2.getMaxRange() / shiftSpeed / FRAMES_PER_SECOND - 0.5);
                        player2.startChannel(sec);
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                        if (player2.isChanneling()) player2.endChannel();
                    }
                }
            } else {
                lastX = X;
                lastY = Y;
                if (action == MotionEvent.ACTION_DOWN && !channeling) {
                    //start channeling with a speed dependent on screen-shift speed
                    float sec = (float) Math.min(2.5, player.getMaxRange() / shiftSpeed / FRAMES_PER_SECOND - 0.5);
                    if (gamemode.equals("scuttle")) sec *= 0.8;
                    player.startChannel(sec);
                } else if (action == MotionEvent.ACTION_UP) {
                    //release
                    if (player.isChanneling()) player.endChannel();
                }
            }
        } else if (menu.equals("limbo")) {
            if (lastPressMenu.equals("limbo")) {
                if (action == MotionEvent.ACTION_UP) {
                    if (middle.isPressed()) {
                        player.reset();
                        player2.reset();
                        if (gamemode.equals("multi")) {
                            player.setX(w()/4);
                            player2.setX(w()*3/4);
                        }
                        goToMenu("game");
                    } else if (right.isPressed()) {
                        goToMenu("start");
                    } else if (left.isPressed()) {
                        goToMenu("shop");
                    }
                }
            }
        }

        if (action == MotionEvent.ACTION_UP) {
            for (CircleButton cb : cbs)
                cb.release();
            for (RoundRectButton rrb : rrbs)
                rrb.release();
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
    private String getBlitzSkin() {
        return sharedPref.getString("blitzskin", "classic");
    }

    private Bitmap getHookBmp() {
        switch(getBlitzSkin()) {
            case "iblitz":
                return hook_iblitz;
            default:
                return hook_classic;
        }
    }
    private Bitmap getHookGameoverBmp() {
        switch(getBlitzSkin()) {
            case "iblitz":
                return iblitzwithporo;
            default:
                return blitzwithporo;
        }
    }
    private Bitmap getIconBmp(String s) {
        switch(s) {
            case "iblitz":
                return icon_iblitz;
            default:
                return icon_classic;
        }
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
            ev_snaptrap = num_snaptrap = 0;
            lightning = 0;
            wait = MAX_WAIT / 2;
            jumped = false;
            clearPoroSnax();
            clearSnapTraps();
            resetPlatforms();
            generatePlatforms();
        }

        if (s.equals("gameover")) {
            //check for new high score
            if (score > getHighScore(gamemode)) {
                editor.putInt("high_score_"+gamemode, score);
                editor.apply();
            }
            //move buttons
            middle.setY(MIDDLE_Y2);
            right.setY(MIDDLE_Y2+c854(30));
            left.setY(MIDDLE_Y2+c854(30));
        }

        if (s.equals("start")) {
            //move buttons
            middle.setY(MIDDLE_Y1);
            right.setY(MIDDLE_Y1+c854(30));
            left.setY(MIDDLE_Y1+c854(30));
        }

        menu = s;
    }

    private void drawTitleMenu() {
        if (h()/w() > 4./3) { //thinner
            float w = h() * titlescreen.getWidth() / titlescreen.getHeight();
            drawBmp(titlescreen, new RectF(w() / 2 - w / 2, 0, w() / 2 + w / 2, h()));
        } else { //thicker
            float h = w() * titlescreen.getHeight() / titlescreen.getWidth();
            drawBmp(titlescreen, new RectF(0, 0, w(), h));
        }

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

        float tmp = Math.max(h()-w(), middle.getY()+middle.getR()+c854(5));
        drawBmp(gameoverBmp, new RectF(0,tmp,w(),tmp+w()));

        title_bold.setTextSize(c854(60));
        canvas.drawText("GAME OVER", w()/2, c854(125), title_bold);
        canvas.drawText("you scored: " + score, w()/2, c854(170), title);

        drawGameoverButtons();
    }

    private void drawRiver() {
        //background
        float tmp = -shift+w()*3;
        while (tmp < 0) tmp += w()*3;
        drawBmp(riverbmp, new RectF(0,tmp-w()*3,w(),tmp));
        if (tmp <= h()) drawBmp(riverbmp, new RectF(0,tmp,w(),tmp+w()*3));
    }

    private int blend(int a, int b) {
        int a_r = Color.red(a), a_g = Color.green(a), a_b = Color.blue(a),
            b_r = Color.red(b), b_g = Color.green(b), b_b = Color.blue(b);
        double a_a = Color.alpha(a)/255., b_a = Color.alpha(b)/255.;
        double alpha = a_a + b_a * (1 - a_a);
        return Color.argb(
                (int)(alpha * 255),
                (int)((a_r * a_a  + b_r * b_a * (1 - a_a)) / alpha),
                (int)((a_g * a_a  + b_g * b_a * (1 - a_a)) / alpha),
                (int)((a_b * a_a  + b_b * b_a * (1 - a_a)) / alpha)
        );
    }
    private void drawLightning() {
        //darkness
        int alpha = (int)(240 * (MAX_LIGHTNING - lightning) / MAX_LIGHTNING);
        int dark = Color.argb(alpha,0,0,0);
        //lightning
        int light = Color.argb(240-alpha,255,255,255);
        canvas.drawColor(blend(dark, light));

        if (wait <= 0) {
            lightning = (.8+.2*Math.random())*MAX_LIGHTNING;
            wait = (.7+.3*Math.random())*MAX_WAIT;
            jumped = false;
        } else {
            if (!jumped && lightning > MAX_LIGHTNING/2 && (lightning-1./FRAMES_PER_SECOND) < MAX_LIGHTNING/2) {
                if (Math.random() < 0.3) {
                    lightning = (.8+.2*Math.random())*MAX_LIGHTNING;
                    jumped = true;
                }
            }
            lightning = Math.max(0, lightning - 1. / FRAMES_PER_SECOND);
            wait = Math.max(0, wait - 1. / FRAMES_PER_SECOND);
        }
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
        if (gamemode.equals("multi")) {
            platforms.add(new Platform(canvas, w()/4, h()/2));
            platforms.add(new Platform(canvas, w()*3/4, h()/2));
            player.setPlatform(platforms.get(0));
            player2.setPlatform(platforms.get(1));
        } else {
            platforms.add(new Platform(canvas, w() / 2, h() / 2));
            player.setPlatform(platforms.get(0));
        }
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
                double prob = gamemode.equals("scuttle") ? 1 : 0.5 * score/1000 / 15;
                ev_scuttle += prob;
                double adjProb = prob * (1 + (ev_scuttle-num_scuttle)/1.5);

                if (rows > 0 && Math.random() < adjProb) {
                    platforms.add(new Platform(canvas, newX, newY, (float) (1 + shiftSpeed + 0.5 * Math.random())));
                    num_scuttle++;
                } else {
                    platforms.add(new Platform(canvas, newX, newY));
                    if (gamemode.equals("multi") && Math.abs(newX-w()/2) > platforms.get(0).getW()/2)
                        platforms.add(new Platform(canvas, w()-newX, newY));
                }
                //add a porosnax?
                double prob2 = 0.2;
                ev_porosnax += prob2;
                double adjProb2 = prob2 * (1 + (ev_porosnax-num_porosnax)/2);
                if (Math.random() < adjProb2) {
                    snaxlist.add(new PoroSnax(canvas, platforms.get(platforms.size() - 1)));
                    num_porosnax++;
                }
                //add a snap trap?
                double prob3 = 0.15;
                ev_snaptrap += prob3;
                double adjProb3 = gamemode.equals("snare") ? 1 : prob3 * (1 + (ev_snaptrap-num_snaptrap)/2);
                if (Math.random() < adjProb3) {
                    snaptraps.add(new SnapTrap(canvas, platforms.get(platforms.size() - 1)));
                    num_snaptrap++;
                }
            }
        }

        //remove overlapping lilypads
        for (int i = 0; i < platforms.size(); i++) {
            for (int j = i+1; j < platforms.size(); j++) {
                Platform p1 = platforms.get(i), p2 = platforms.get(j);
                if (distance(p1.getX(),p1.getY(),p2.getX(),p2.getY()) < p1.getW()) {
                    if (p1.getX() < w()/2) {
                        platforms.remove(j);
                        j--;
                    } else {
                        platforms.remove(i);
                        i--;
                        break;
                    }
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
            if (p.visible(shift)) {
                p.update();
                if (gamemode.equals("spin")) p.addSpin();
            }
    }

    //find the platform that the player has landed on, return false if lands in water
    private boolean checkForPlatform(Poro player) {
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
            } else if (gamemode.equals("multi")) {
                if (distance(snaxlist.get(i).getX(),snaxlist.get(i).getY(),player2.getX(),player2.getY())
                        < (player2.getW()+snaxlist.get(i).getW())/2) {
                    snaxlist.remove(i);
                    editor.putInt("porosnax", getPoroSnax()+1);
                    editor.apply();
                }
            }
        }

        for (PoroSnax ps : snaxlist) {
            if (ps.visible(shift) && gamemode.equals("spin")) ps.addSpin();
        }
    }

    private void clearSnapTraps() {
        snaptraps.clear();
    }
    private void drawSnapTraps() {
        for (SnapTrap s : snaptraps)
            if (s.visible(shift)) s.draw();
    }
    private void updateSnapTraps() {
        //remove traps that have gone off the screen
        while (!snaptraps.isEmpty() && !snaptraps.get(0).visible(shift)
                && snaptraps.get(0).getY()-shift < h()) snaptraps.remove(0);

        //check if traps have been triggered
        for (int i = snaptraps.size()-1; i >= 0; i--) {
            if (distance(snaptraps.get(i).getX(),snaptraps.get(i).getY(),player.getX(),player.getY())
                    < (player.getW()+snaptraps.get(i).getW())/2) {
                snaptraps.remove(i);
                player.snare();
            } else if (gamemode.equals("multi")) {
                if (distance(snaptraps.get(i).getX(),snaptraps.get(i).getY(),player2.getX(),player2.getY())
                        < (player2.getW()+snaptraps.get(i).getW())/2) {
                    snaptraps.remove(i);
                    player2.snare();
                }
            }
        }

        for (SnapTrap st : snaptraps) {
            if (st.visible(shift) && gamemode.equals("spin")) st.addSpin();
        }
    }
}
