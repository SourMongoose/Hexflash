package com.chrisx.hexflash;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements RewardedVideoAdListener {
    private Bitmap bmp;
    static Canvas canvas;
    private LinearLayout ll;
    private float scaleFactor;

    private RewardedVideoAd rva;

    static Bitmap poro, poro_black, scuttler, scuttler_candy, titlescreen, porosnax, porosnax_count,
            snaptrap, snarefx, hook_classic, hook_iblitz, hook_arcade, icon_classic, icon_iblitz,
            icon_arcade, icon_river, icon_candy, blitzwithporo, iblitzwithporo, arcadewithporo,
            lilypad, lilypadlotus, candypad_red, candypad_orange, candypad_yellow, sadporo,
            sadporo_spin, burntporo, riverbmp, riverbmp_candy, restart, home, shop, play, more,
            leftarrow, maxrange, indicator, bubble, border, bulletbmp, explosion, lock, gradient,
            stats, video, flash, flash2;
    static Bitmap[] sinking, medals;
    private Bitmap gameoverBmp;

    private String blitzskins[] = {"classic", "iblitz", "arcade"};
    private int blitzskins_cost[] = {0, 50, 50};
    private int nBlitz = blitzskins.length;
    private RectF blitzskins_rectf[] = new RectF[nBlitz];
    private int ICON_WIDTH;
    private float BLITZSKINS_Y;
    private boolean blitzskins_owned[];
    private String blitzskin_used;

    private String riverskins[] = {"river", "candy"};
    private int riverskins_cost[] = {0, 100};
    private int nRiver = riverskins.length;
    private RectF riverskins_rectf[] = new RectF[nRiver];
    private float RIVERSKINS_Y;
    private boolean riverskins_owned[];
    private String riverskin_used;

    private int prevPorosnax;
    private boolean adLoaded;

    static SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private Typeface cd, cd_i, cd_b, cd_bi;

    private boolean paused = false;
    private long frameCount = 0;

    private String menu = "start", prevMenu = "start";
    private String loadMenu = "";
    private String lastPressMenu;
    private String gamemode = "classic";

    //frame data
    static int FRAMES_PER_SECOND = 60;
    private long nanosecondsPerFrame;

    private int TRANSITION_MAX = FRAMES_PER_SECOND * 2 / 3;
    private int transition = TRANSITION_MAX / 2;
    private int prevTransition;

    private float lastX, lastY;
    private float downX, downY;
    private boolean pressed;

    private Paint title_bold, title, mode, scoreTitle, scoreText, river_fade, quarter,
            adText, priceText, medalText, tutorialText, white, startText;
    private int river = Color.rgb(35,66,94);

    private CircleButton middle, left, right;
    private float offset, MIDDLE_Y1, MIDDLE_Y2;

    private RoundRectButton classic, light, spin, scuttle, snare, cc, rr;

    private String modeNames[] = {"CLASSIC", "NIGHT LIGHTS", "SPIN TO WIN", "SCUTTLE TROUBLE",
            "SNARE FAIR", "CURTAIN CALL", "COMBO CHAOS"};
    private String modeCodes[] = {"classic", "light", "spin", "scuttle", "snare", "cc", "rr"};
    private int medal_scores[][] = {{2500,5000,10000},{1500,3000,6000},{1000,2000,4000},
            {1000,2000,4000},{1500,3000,6000},{1500,3000,6000},{250,500,1000}};

    private CircleButton cbs[];
    private boolean cbs_pressed[];
    private RoundRectButton rrbs[];
    private boolean rrbs_pressed[];

    private Poro player;
    private boolean channeling, waitingForTap;
    private float playerY;
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
    private Bullet bullet;
    private double bulletCD; //cooldown

    private float shift, //pixels translated down
        shiftSpeed = 0.75f;
    private int hookAnimation, sinkAnimation, burnAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-2436690700589338~9070068520");
        rva = MobileAds.getRewardedVideoAdInstance(this);
        rva.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        //creates the bitmap
        //note: Star 4.5 is 480x854
        int targetH = 854,
                wpx = Resources.getSystem().getDisplayMetrics().widthPixels,
                hpx = Resources.getSystem().getDisplayMetrics().heightPixels;
        scaleFactor = Math.min(1,(float)targetH/hpx);
        bmp = Bitmap.createBitmap(Math.round(wpx*scaleFactor),targetH,Bitmap.Config.RGB_565);

        //creates canvas
        canvas = new Canvas(bmp);

        ll = (LinearLayout) findViewById(R.id.draw_area);
        ll.setBackgroundDrawable(new BitmapDrawable(bmp));

        //initialize bitmaps
        Resources res = getResources();
        poro = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.poro),
                Math.round(w()/8),Math.round(w()/8),false);
        poro_black = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.poro_black),
                poro.getWidth(),poro.getHeight(),false);
        flash = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.flash),
                poro.getWidth(),poro.getHeight(),false);
        flash2 = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.flash2),
                poro.getWidth(),poro.getHeight(),false);

        int pw = Math.round(w()/5); //platform width
        scuttler = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.scuttler),
                Math.round(pw*2/1.7f),Math.round(pw*2/1.7f),false);
        scuttler_candy = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.scuttler_candy),
                Math.round(pw*2/1.7f),Math.round(pw*2/1.7f),false);
        lilypad = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.lilypad_nolotus),
                pw,pw,false);
        lilypadlotus = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.lilypad_lotus),
                pw,pw,false);
        candypad_red = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.candypad_red),
                pw,pw,false);
        candypad_orange = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.candypad_orange),
                pw,pw,false);
        candypad_yellow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.candypad_yellow),
                pw,pw,false);

        BitmapFactory.Options rgb565 = new BitmapFactory.Options();
        rgb565.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap tmp = BitmapFactory.decodeResource(res, R.drawable.titlescreen, rgb565);
        if (h()/w() > 4./3) { //thinner
            int w = Math.round(h() * tmp.getWidth() / tmp.getHeight());
            titlescreen = Bitmap.createScaledBitmap(tmp,w,Math.round(h()),false);
        } else { //thicker
            int h = Math.round(w() * tmp.getHeight() / tmp.getWidth());
            titlescreen = Bitmap.createScaledBitmap(tmp,Math.round(w()),h,false);
        }
        tmp.recycle();
        
        porosnax = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.porosnax),
                lilypad.getWidth()/3,lilypad.getWidth()/3,false);
        porosnax_count = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.porosnax),
                Math.round(c854(50)),Math.round(c854(50)),false);
        snaptrap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.snaptrap),
                lilypad.getWidth()/3,lilypad.getWidth()/3,false);
        snarefx = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.snarefx),
                poro.getWidth()*4/3,poro.getWidth()*4/3,false);
        
        int hw = Math.round(w()/6); //hook width
        hook_classic = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.hook_classic),
                hw,hw*3,false);
        hook_iblitz = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.hook_iblitz),
                hw,hw*3,false);
        hook_arcade = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.hook_arcade),
                hw,hw*3,false);
        
        ICON_WIDTH = Math.round(c854(100));
        int ih = ICON_WIDTH; //icon height
        icon_classic = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.icon_classic),
                ICON_WIDTH,ih,false);
        icon_iblitz = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.icon_iblitz),
                ICON_WIDTH,ih,false);
        icon_arcade = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.icon_arcade),
                ICON_WIDTH,ih,false);
        icon_river = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.lilypad_lotus),
                ICON_WIDTH,ih,false);
        icon_candy = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.candypad_red),
                ICON_WIDTH,ih,false);

        blitzwithporo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.blitzwithporo),
                Math.round(w()),Math.round(w()),false);
        iblitzwithporo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.iblitzwithporo),
                Math.round(w()),Math.round(w()),false);
        arcadewithporo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.arcadewithporo),
                Math.round(w()),Math.round(w()),false);
        sadporo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.sadporo),
                Math.round(w()),Math.round(w()),false);
        sadporo_spin = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.sadporo_spin),
                Math.round(w()),Math.round(w()),false);
        burntporo = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.burntporo),
                Math.round(w()),Math.round(w()),false);

        riverbmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.river_mediumres, rgb565),
                Math.round(w()),Math.round(w()*3),false);
        riverbmp_candy = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.river_candy_mediumres_compressed, rgb565),
                Math.round(w()),Math.round(w()*3),false);

        restart = BitmapFactory.decodeResource(res, R.drawable.restart);
        home = BitmapFactory.decodeResource(res, R.drawable.home);
        shop = BitmapFactory.decodeResource(res, R.drawable.shop);
        play = BitmapFactory.decodeResource(res, R.drawable.play);
        more = BitmapFactory.decodeResource(res, R.drawable.more);
        leftarrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.leftarrow),
                Math.round(c854(80)),Math.round(c854(80)),false);
        maxrange = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.maxrange),
                Math.round(h()/2),Math.round(h()/2),false);
        indicator = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.indicator),
                poro.getWidth(),poro.getHeight(),false);
        bubble = BitmapFactory.decodeResource(res, R.drawable.bubble);
        border = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.border),
                ICON_WIDTH*5/4,ICON_WIDTH*5/4,false);
        bulletbmp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.bullet_thin),
                Math.round(w()/24),Math.round(w()/3),false);
        explosion = BitmapFactory.decodeResource(res, R.drawable.explosion);
        lock = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.lock),
                ICON_WIDTH/3,ICON_WIDTH/3,false);
        gradient = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.gradient),
                Math.round(w()),Math.round(c854(150)),false);
        stats = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.stats),
                Math.round(c854(60)),Math.round(c854(60)),false);
        video = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.video),
                Math.round(c854(50)),Math.round(c854(50)),false);

        int mw = Math.round(c854(50)); //medal width
        medals = new Bitmap[]{Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.medal_bronze), mw, mw, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.medal_silver), mw, mw, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.medal_gold), mw, mw, false),
                Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.medal_blank), mw, mw, false)};

        sinking = new Bitmap[15];
        for (int i = 0; i < sinking.length; i++)
            sinking[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(res, R.drawable.poro01+i),
                    poro.getWidth(),poro.getHeight(),false);

        //initializes SharedPreferences
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        nanosecondsPerFrame = (long)1e9 / FRAMES_PER_SECOND;

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

        river_fade = newPaint(river);
        river_fade.setStyle(Paint.Style.FILL);
        river_fade.setAlpha(150);

        quarter = new Paint();
        quarter.setAlpha(64);

        adText = newPaint(Color.WHITE);
        adText.setTextAlign(Paint.Align.CENTER);
        adText.setTextSize(c854(20));
        adText.setTypeface(cd_b);

        priceText = new Paint(adText);

        medalText = new Paint(scoreTitle);
        medalText.setTextAlign(Paint.Align.RIGHT);
        medalText.setTextSize(c854(15));

        tutorialText = newPaint(Color.WHITE);
        tutorialText.setTypeface(cd_b);
        tutorialText.setTextSize(c480(25));
        tutorialText.setTextAlign(Paint.Align.CENTER);

        white = newPaint(Color.WHITE);
        white.setStyle(Paint.Style.STROKE);
        white.setStrokeWidth(c480(2));

        startText = new Paint(mode);

        //buttons
        offset = c854(125);
        MIDDLE_Y1 = c854(720);
        MIDDLE_Y2 = c854(290);
        middle = new CircleButton(canvas,w()/2,MIDDLE_Y1,c854(70));
        right = new CircleButton(canvas,w()/2+offset,MIDDLE_Y1+c854(30),c854(40));
        left = new CircleButton(canvas,w()/2-offset,MIDDLE_Y1+c854(30),c854(40));
        cbs = new CircleButton[]{middle, right, left};
        cbs_pressed = new boolean[cbs.length];

        classic = new RoundRectButton(canvas,c480(48),c854(87),c480(432),c854(167),Color.BLACK);
        light = new RoundRectButton(canvas,c480(48),c854(187),c480(432),c854(267),Color.rgb(255,68,68));
        spin = new RoundRectButton(canvas,c480(48),c854(287),c480(432),c854(367),Color.rgb(255,140,0));
        scuttle = new RoundRectButton(canvas,c480(48),c854(387),c480(432),c854(467),Color.rgb(54,173,31));
        snare = new RoundRectButton(canvas,c480(48),c854(487),c480(432),c854(567),Color.rgb(80,163,215));
        cc = new RoundRectButton(canvas,c480(48),c854(587),c480(432),c854(667),Color.rgb(178,55,170));
        rr = new RoundRectButton(canvas,c480(48),c854(687),c480(432),c854(767),Color.BLACK);
        rrbs = new RoundRectButton[]{classic, light, spin, scuttle, snare, cc, rr};
        rrbs_pressed = new boolean[rrbs.length];

        //blitz skins
        BLITZSKINS_Y = c854(275);
        float totalWidth = ICON_WIDTH*nBlitz + ICON_WIDTH*(nBlitz-1)/4;
        for (int i = 0; i < nBlitz; i++) {
            float x = w()/2 - totalWidth/2 + i * (ICON_WIDTH*1.25f);
            blitzskins_rectf[i] = new RectF(x,BLITZSKINS_Y,x+ICON_WIDTH,BLITZSKINS_Y+ICON_WIDTH);
        }
        blitzskins_owned = new boolean[nBlitz];
        blitzskin_used = getBlitzSkin();
        //river skins
        RIVERSKINS_Y = c854(550);
        totalWidth = ICON_WIDTH*nRiver + ICON_WIDTH*(nRiver-1)/4;
        for (int i = 0; i < nRiver; i++) {
            float x = w()/2 - totalWidth/2 + i * (ICON_WIDTH*1.25f);
            riverskins_rectf[i] = new RectF(x,RIVERSKINS_Y,x+ICON_WIDTH,RIVERSKINS_Y+ICON_WIDTH);
        }
        riverskins_owned = new boolean[nRiver];
        riverskin_used = getRiverSkin();

        //player
        player = new Poro(canvas);

        final Handler handler = new Handler();

        //thread for calculations/updates
        new Thread(new Runnable() {
            @Override
            public void run() {
                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                if (transition < TRANSITION_MAX / 2) {
                                    if (menu.equals("start")) {
                                    } else if (menu.equals("more")) {
                                    } else if (menu.equals("stats")) {
                                    } else if (menu.equals("shop")) {
                                    } else if (menu.equals("game")) {
                                        shiftSpeed = c854(0.75f + 0.02f * frameCount / FRAMES_PER_SECOND);

                                        if (!waitingForTap) {
                                            if (transition == 0) movePlatforms();
                                            generatePlatforms();

                                            updatePoroSnax();

                                            updateSnapTraps();

                                            if (gamemode.equals("cc") || gamemode.equals("rr"))
                                                updateBullet();

                                            //mid-channel
                                            if (player.isChanneling()) {
                                                channeling = true;
                                                player.update(lastX, lastY + shift);
                                            }

                                            //just hexflashed
                                            if (channeling && !player.isChanneling()) {
                                                //if the player doesn't land on a platform
                                                if (!checkForPlatform(player)) {
                                                    goToMenu("sink");
                                                    gameoverBmp = (gamemode.equals("spin") || gamemode.equals("rr")) ? sadporo_spin : sadporo;
                                                    sinkAnimation = 0;
                                                }
                                                channeling = false;
                                            } else {
                                                player.update(); //moving platform
                                                if (gamemode.equals("spin") || gamemode.equals("rr"))
                                                    player.addSpin();
                                            }

                                            //reaches top of screen
                                            if (menu.equals("game") && player.getY() - shift < h() / 10) {
                                                player.interruptChannel();
                                                playerY = player.getY();

                                                goToMenu("hook");
                                                gameoverBmp = getHookGameoverBmp();
                                                hookAnimation = 0;
                                            }

                                            if (gamemode.equals("spin") || gamemode.equals("rr"))
                                                shiftSpeed *= 0.75;
                                            if (transition == 0) {
                                                if (gamemode.equals("light") || gamemode.equals("rr"))
                                                    shift += shiftSpeed *= 0.75;
                                                else
                                                    shift += shiftSpeed;
                                            }
                                        }
                                    } else if (menu.equals("hook")) {
                                        player.updateAnimations();
                                        if ((gamemode.equals("cc") || gamemode.equals("rr"))
                                                && bullet != null && bullet.visible(shift)) {
                                            bullet.update();
                                        }

                                        int hookDuration = FRAMES_PER_SECOND * 2 / 3;
                                        if (hookAnimation < hookDuration / 2) {
                                        } else {
                                            //hook exits screen w/ poro
                                            float hookY = (playerY + player.getW() - shift) * ((hookDuration - hookAnimation) / (hookDuration / 2f));
                                            player.setY(hookY - player.getW() + shift);
                                        }

                                        if (hookAnimation > hookDuration + FRAMES_PER_SECOND / 3)
                                            goToMenu("gameover");

                                        hookAnimation++;
                                    } else if (menu.equals("sink")) {
                                        player.updateAnimations();

                                        //fade effect over poro
                                        int sinkDuration = FRAMES_PER_SECOND;
                                        player.setBmp(sinking[Math.min(sinking.length-1,
                                                sinkAnimation/(sinkDuration/sinking.length))]);

                                        if ((gamemode.equals("cc") || gamemode.equals("rr"))
                                                && bullet != null && bullet.visible(shift)) {
                                            bullet.update();
                                        }

                                        if (sinkAnimation > sinkDuration + FRAMES_PER_SECOND / 3)
                                            goToMenu("gameover");

                                        sinkAnimation++;
                                    } else if (menu.equals("burned")) {
                                        player.updateAnimations();

                                        int burnDuration = FRAMES_PER_SECOND / 2;
                                        if (burnAnimation > burnDuration + FRAMES_PER_SECOND / 3) {
                                            goToMenu("gameover");
                                            gameoverBmp = MainActivity.burntporo;
                                        }

                                        burnAnimation++;
                                    } else if (menu.equals("gameover")) {
                                        if (transition == 0) goToMenu("limbo");
                                    } else if (menu.equals("limbo")) {
                                    }
                                }

                                //fading transition effect
                                if (transition > 0) {
                                    transition--;
                                }

                                if (!waitingForTap) frameCount++;
                            }
                        }
                    });

                    //wait until frame is done
                    while (System.nanoTime() - startTime < nanosecondsPerFrame);
                }
            }
        }).start();

        //UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                drawTitleMenu();

                //draw loop
                while (!menu.equals("quit")) {
                    final long startTime = System.nanoTime();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!paused) {
                                boolean update = false;

                                if (prevTransition != transition) {
                                    update = true;
                                    prevTransition = transition;
                                }

                                if (transition < TRANSITION_MAX / 2) {
                                    if (menu.equals("start")) {
                                        //check for updates (button presses)
                                        for (int i = 0; i < cbs.length; i++) {
                                            if (cbs[i].isPressed() != cbs_pressed[i]) {
                                                update = true;
                                                cbs_pressed[i] = cbs[i].isPressed();
                                            }
                                        }

                                        //title screen
                                        if (update) drawTitleMenu();
                                    } else if (menu.equals("more")) {
                                        //check for updates (button presses)
                                        for (int i = 1; i < rrbs.length-1; i++) {
                                            if (rrbs[i].isPressed() != rrbs_pressed[i]) {
                                                update = true;
                                                rrbs_pressed[i] = rrbs[i].isPressed();
                                            }
                                        }

                                        if (update) {
                                            canvas.drawColor(river);

                                            title_bold.setTextSize(c854(60));
                                            canvas.drawText("GAMEMODES", w()/2, c854(80), title_bold);

                                            mode.setTextAlign(Paint.Align.CENTER);
                                            float tmp = (mode.ascent() + mode.descent()) / 2;
                                            for (int i = 1; i < modeNames.length-1; i++) {
                                                rrbs[i].draw();
                                                canvas.drawText(modeNames[i], rrbs[i].getRectF().centerX(),
                                                        rrbs[i].getRectF().centerY()-tmp, mode);
                                            }

                                            //back
                                            canvas.drawBitmap(leftarrow,c854(10),h()-c854(90),null);
                                            //stats
                                            canvas.drawBitmap(stats,w()-c854(80),h()-c854(80),null);
                                        }
                                    } else if (menu.equals("stats")) {
                                        if (update) {
                                            canvas.drawColor(river);

                                            title_bold.setTextSize(c854(50));
                                            canvas.drawText("HIGH SCORES", w()/2, c854(70), title_bold);

                                            mode.setTextAlign(Paint.Align.LEFT);
                                            for (int i = 0; i < modeNames.length; i++) {
                                                float tmp = rrbs[i].getRectF().centerY();
                                                tmp = h()/2+(tmp-h()/2)*0.9f;

                                                mode.setTextSize(c854(25));
                                                canvas.drawText(modeNames[i], c480(20), tmp, mode);
                                                mode.setTextSize(c854(35));
                                                canvas.drawText(getHighScore(modeCodes[i])+"", c480(20), tmp+c854(35), mode);

                                                int nextMedal = -1;
                                                for (int m = 0; m < 3; m++) {
                                                    canvas.drawBitmap((getHighScore(modeCodes[i]) >= medal_scores[i][m] ? medals[m] : medals[3]),
                                                            c480(460)-c854(120-m*40),tmp-c854(15),null);
                                                    if (nextMedal == -1 && getHighScore(modeCodes[i]) < medal_scores[i][m])
                                                        nextMedal = medal_scores[i][m];
                                                }
                                                if (nextMedal != -1)
                                                    canvas.drawText("Next medal: " + nextMedal, w()-c480(20), tmp+c854(50), medalText);
                                            }

                                            //back
                                            canvas.drawBitmap(leftarrow,c854(10),h()-c854(90),null);
                                        }
                                    } else if (menu.equals("shop")) {
                                        //check for updates
                                        if (!blitzskin_used.equals(getBlitzSkin()) || !riverskin_used.equals(getRiverSkin())) {
                                            update = true;
                                            blitzskin_used = getBlitzSkin();
                                            riverskin_used = getRiverSkin();
                                        }
                                        if (prevPorosnax != getPoroSnax()) {
                                            update = true;
                                            prevPorosnax = getPoroSnax();
                                        }
                                        if (adLoaded != rva.isLoaded()) {
                                            update = true;
                                            adLoaded = rva.isLoaded();
                                        }
                                        for (int i = 0; i < nBlitz; i++) {
                                            if (blitzskins_owned[i] != hasSkin(blitzskins[i])) {
                                                update = true;
                                                blitzskins_owned[i] = hasSkin(blitzskins[i]);
                                            }
                                        }
                                        for (int i = 0; i < nRiver; i++) {
                                            if (riverskins_owned[i] != hasSkin(riverskins[i])) {
                                                update = true;
                                                riverskins_owned[i] = hasSkin(riverskins[i]);
                                            }
                                        }

                                        if (update) {
                                            canvas.drawColor(river);

                                            title_bold.setTextSize(c854(60));
                                            canvas.drawText("SHOP", w()/2, c854(80), title_bold);

                                            //blitzcrank skins
                                            canvas.drawText("BLITZ SKINS", w()/2, BLITZSKINS_Y-ICON_WIDTH/2, title);
                                            for (int i = 0; i < nBlitz; i++) {
                                                canvas.drawBitmap(getIconBmp(blitzskins[i]),blitzskins_rectf[i].left,blitzskins_rectf[i].top,null);
                                                if (getBlitzSkin().equals(blitzskins[i])) {
                                                    RectF rf = blitzskins_rectf[i];
                                                    canvas.drawBitmap(border,rf.left-ICON_WIDTH/8,rf.top-ICON_WIDTH/8,null);
                                                }
                                                if (!hasSkin(blitzskins[i])) {
                                                    RectF rf = blitzskins_rectf[i];
                                                    canvas.drawRect(rf, river_fade);
                                                    canvas.drawBitmap(lock,rf.left+rf.width()/3,rf.top+rf.width()/3,null);
                                                    canvas.drawText(blitzskins_cost[i]+"", rf.centerX(),
                                                            rf.bottom-rf.width()/10, priceText);
                                                }
                                            }
                                            //river skins
                                            canvas.drawText("RIVER SKINS", w()/2, RIVERSKINS_Y-ICON_WIDTH/2, title);
                                            for (int i = 0; i < nRiver; i++) {
                                                canvas.drawBitmap(getIconBmp(riverskins[i]),
                                                        riverskins_rectf[i].left,riverskins_rectf[i].top,null);
                                                if (getRiverSkin().equals(riverskins[i])) {
                                                    RectF rf = riverskins_rectf[i];
                                                    canvas.drawBitmap(border,rf.left-ICON_WIDTH/8,rf.top-ICON_WIDTH/8,null);
                                                }
                                                if (!hasSkin(riverskins[i])) {
                                                    RectF rf = riverskins_rectf[i];
                                                    canvas.drawRect(rf, river_fade);
                                                    canvas.drawBitmap(lock,rf.left+rf.width()/3,rf.top+rf.width()/3,null);
                                                    canvas.drawText(riverskins_cost[i]+"", rf.centerX(),
                                                            rf.bottom-rf.width()/10, priceText);
                                                }
                                            }

                                            //porosnax count
                                            canvas.drawBitmap(porosnax_count,w()-c854(75),h()-c854(75),null);
                                            title.setTextAlign(Paint.Align.RIGHT);
                                            canvas.drawText(getPoroSnax()+"", w()-c854(85), h()-c854(50)-(title.ascent()+title.descent())/2, title);
                                            title.setTextAlign(Paint.Align.CENTER);

                                            //video ad
                                            if (rva.isLoaded()) {
                                                canvas.drawBitmap(video,w()-c854(75),c854(25),null);
                                                adText.setAlpha(255);
                                            } else {
                                                canvas.drawBitmap(video, new Rect(0,0,video.getWidth(),video.getHeight()),
                                                        new RectF(w()-c854(75),c854(25),w()-c854(25),c854(75)), quarter);
                                                adText.setAlpha(64);
                                            }
                                            canvas.drawText("+10", w()-c854(50), c854(100), adText);

                                            //back
                                            canvas.drawBitmap(leftarrow,c854(10),h()-c854(90),null);
                                        }
                                    } else if (menu.equals("game")) {
                                        update = true;

                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift

                                        drawPlatforms();

                                        drawPoroSnax();

                                        drawSnapTraps();

                                        player.draw();
                                        //player.drawHitbox(); //debugging purposes

                                        if (!waitingForTap && (gamemode.equals("cc") || gamemode.equals("rr")))
                                            if (bullet != null && bullet.visible(shift))
                                                bullet.draw();

                                        canvas.restore();

                                        if (!waitingForTap && (gamemode.equals("light") || gamemode.equals("rr")))
                                            drawLightning();

                                        drawScores();

                                        if (waitingForTap) {
                                            if (getRiverSkin().equals("candy"))
                                                startText.setColor(Color.BLACK);
                                            else startText.setColor(Color.WHITE);
                                            float y = platforms.get(0).getY() - platforms.get(0).getW();
                                            canvas.drawText("tap to start", w() / 2, y, startText);
                                        }
                                    } else if (menu.equals("hook")) {
                                        update = true;

                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift
                                        drawPlatforms();
                                        drawPoroSnax();
                                        drawSnapTraps();
                                        player.draw();
                                        if ((gamemode.equals("cc") || gamemode.equals("rr"))
                                                && bullet != null && bullet.visible(shift)) {
                                            bullet.draw();
                                        }
                                        canvas.restore();

                                        int hookDuration = FRAMES_PER_SECOND * 2 / 3;
                                        float hookWidth = w() / 6;
                                        if (hookAnimation < hookDuration / 2) {
                                            //hook enters screen
                                            float hookY = (playerY + player.getW() - shift) * (hookAnimation / (hookDuration / 2f));
                                            canvas.drawBitmap(getHookBmp(),player.getX()-hookWidth/2,hookY-hookWidth*3,null);
                                        } else {
                                            //hook exits screen w/ poro
                                            float hookY = (playerY + player.getW() - shift) * ((hookDuration - hookAnimation) / (hookDuration / 2f));
                                            canvas.drawBitmap(getHookBmp(),player.getX()-hookWidth/2,hookY-hookWidth*3,null);
                                        }

                                        drawScores();
                                    } else if (menu.equals("sink")) {
                                        update = true;

                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift

                                        drawPlatforms();
                                        drawPoroSnax();
                                        drawSnapTraps();

                                        player.draw();

                                        if ((gamemode.equals("cc") || gamemode.equals("rr"))
                                                && bullet != null && bullet.visible(shift)) {
                                            bullet.draw();
                                        }

                                        canvas.restore();

                                        drawScores();
                                    } else if (menu.equals("burned")) {
                                        update = true;

                                        //background
                                        drawRiver();

                                        canvas.save();
                                        canvas.translate(0, -shift); //screen shift

                                        drawPlatforms();
                                        drawPoroSnax();
                                        drawSnapTraps();

                                        player.draw();

                                        int explodeDuration = FRAMES_PER_SECOND / 3;
                                        if (burnAnimation < explodeDuration) {
                                            float f = (float)(explodeDuration - burnAnimation) / explodeDuration;
                                            drawBmp(explosion, new RectF(player.getX()-player.getW()*f,
                                                    player.getY()-player.getW()*f,
                                                    player.getX()+player.getW()*f,
                                                    player.getY()+player.getW()*f));
                                        }

                                        canvas.restore();

                                        drawScores();
                                    } else if (menu.equals("gameover")) {
                                        update = true;

                                        drawGameoverScreen();

                                        if (transition == 0) goToMenu("limbo");
                                    } else if (menu.equals("limbo")) {
                                        //check for updates (button presses)
                                        for (int i = 0; i < cbs.length; i++) {
                                            if (cbs[i].isPressed() != cbs_pressed[i]) {
                                                update = true;
                                                cbs_pressed[i] = cbs[i].isPressed();
                                            }
                                        }

                                        if (update) drawGameoverScreen();
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
                                }

                                //update canvas
                                if (update) ll.invalidate();
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
        rva.pause(this);
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        rva.resume(this);
        super.onResume();
        paused = false;
    }

    @Override
    protected void onDestroy() {
        rva.destroy(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (menu.equals("shop")) {
            goToMenu(prevMenu);
        } else if (menu.equals("more")) {
            goToMenu("start");
        } else if (menu.equals("stats")) {
            goToMenu("more");
        }
    }

    @Override
    //handles touch events
    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX(event.getActionIndex())*scaleFactor;
        float Y = event.getY(event.getActionIndex())*scaleFactor;
        //Log.i("Touch","("+X+", "+Y+")");

        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            lastPressMenu = menu;
            pressed = true;

            for (CircleButton cb : cbs)
                if (cb.contains(X, Y)) cb.press();
            for (RoundRectButton rrb : rrbs)
                if (rrb.contains(X, Y)) rrb.press();
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (CircleButton cb : cbs)
                if (!cb.contains(X, Y)) cb.release();
            for (RoundRectButton rrb : rrbs)
                if (!rrb.contains(X, Y)) rrb.release();
        } else if (action == MotionEvent.ACTION_UP) {
            pressed = false;
        }

        if (menu.equals("start")) {
            if (action == MotionEvent.ACTION_UP) {
                if (middle.isPressed()) {
                    gamemode = "classic";
                    goToMenu("game");
                } else if (right.isPressed()) {
                    goToMenu("more");
                } else if (left.isPressed()) {
                    goToMenu("shop");
                }
            }
        } else if (menu.equals("shop")) {
            //selecting skins
            if (action == MotionEvent.ACTION_DOWN) {
                for (int i = 0; i < nBlitz; i++) {
                    if (hasSkin(blitzskins[i]) && blitzskins_rectf[i].contains(X, Y)) {
                        editor.putString("blitzskin", blitzskins[i]);
                        editor.apply();
                    }
                }
                for (int i = 0; i < nRiver; i++) {
                    if (hasSkin(riverskins[i]) && riverskins_rectf[i].contains(X, Y)) {
                        editor.putString("riverskin", riverskins[i]);
                        editor.apply();
                    }
                }
            }
            //buying skins
            if (action == MotionEvent.ACTION_UP) {
                for (int i = 0; i < nBlitz; i++) {
                    if (!hasSkin(blitzskins[i]) && blitzskins_rectf[i].contains(X, Y)) {
                        if (getPoroSnax() >= blitzskins_cost[i]) {
                            editor.putBoolean("has_skin_" + blitzskins[i], true);
                            editor.putInt("porosnax", getPoroSnax() - blitzskins_cost[i]);
                            editor.apply();
                        }
                    }
                }
                for (int i = 0; i < nRiver; i++) {
                    if (!hasSkin(riverskins[i]) && riverskins_rectf[i].contains(X, Y)) {
                        if (getPoroSnax() >= riverskins_cost[i]) {
                            editor.putBoolean("has_skin_" + riverskins[i], true);
                            editor.putInt("porosnax", getPoroSnax() - riverskins_cost[i]);
                            editor.apply();
                        }
                    }
                }
            }

            //watch video ad
            if (action == MotionEvent.ACTION_UP && X > w() - c854(100) && Y < c854(100)) {
                if (rva.isLoaded()) {
                    rva.show();
                }
            }

            //back arrow
            if (action == MotionEvent.ACTION_UP && X < c854(100) && Y > h() - c854(100))
                goToMenu(prevMenu);
        } else if (menu.equals("more")) {
            if (action == MotionEvent.ACTION_DOWN) {
                downX = X;
                downY = Y;
            }
            if (action == MotionEvent.ACTION_UP) {
                for (int i = 1; i < rrbs.length - 1; i++) {
                    RoundRectButton rrb = rrbs[i];

                    if (rrb.isPressed()) {
                        if (rrb == scuttle) gamemode = "scuttle";
                        else if (rrb == snare) gamemode = "snare";
                        else if (rrb == spin) gamemode = "spin";
                        else if (rrb == light) gamemode = "light";
                        else if (rrb == cc) gamemode = "cc";

                        goToMenu("game");
                        break;
                    }
                }

                //combo chaos/rainbow river
                if (rrbs[1].contains(downX, downY) && rrbs[rrbs.length - 2].contains(X, Y)) {
                    gamemode = "rr";
                    goToMenu("game");
                }
            }

            //back arrow
            if (action == MotionEvent.ACTION_UP &&
                    X < c854(100) && Y > h() - c854(100)) goToMenu("start");

            //stats menu
            if (action == MotionEvent.ACTION_UP &&
                    X > w() - c854(100) && Y > h() - c854(100)) goToMenu("stats");
        } else if (menu.equals("stats")) {
            //back arrow
            if (action == MotionEvent.ACTION_UP &&
                    X < c854(100) && Y > h() - c854(100)) goToMenu("more");
        } else if (menu.equals("game")) {
            lastX = X;
            lastY = Y;
            if (action == MotionEvent.ACTION_DOWN && !channeling) {
                //start channeling with a speed dependent on screen-shift speed
                waitingForTap = false;
                float sec = (float) Math.min(2.5, player.getMaxRange() / Math.max(c854(1.25f),shiftSpeed) / FRAMES_PER_SECOND - 0.5);
                if (gamemode.equals("scuttle")) sec *= 0.8;
                else if (gamemode.equals("cc") || gamemode.equals("rr")) sec *= 0.5;
                player.startChannel(sec);
            } else if (action == MotionEvent.ACTION_UP) {
                //release
                if (player.isChanneling()) player.endChannel();
            }
        } else if (menu.equals("limbo")) {
            if (lastPressMenu.equals("limbo")) {
                if (action == MotionEvent.ACTION_UP) {
                    if (middle.isPressed()) {
                        player.reset();
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

    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(this, "Thanks for watching the video! You have been gifted 10 Poro-Snax.",
                Toast.LENGTH_SHORT).show();
        editor.putInt("porosnax", getPoroSnax()+10);
        editor.apply();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        // Load the next rewarded video ad
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        /*
        switch(errorCode) {
            case 0:
                Toast.makeText(this, "ERROR_CODE_INTERNAL_ERROR", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "ERROR_CODE_INVALID REQUEST", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, "ERROR_CODE_NETWORK_ERROR", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this, "ERROR_CODE_NO_FILL", Toast.LENGTH_SHORT).show();
                break;
        }
        */
    }
    @Override
    public void onRewardedVideoAdLeftApplication() {}
    @Override
    public void onRewardedVideoAdLoaded() {}
    @Override
    public void onRewardedVideoAdOpened() {}
    @Override
    public void onRewardedVideoStarted() {}
    @Override
    public void onRewardedVideoCompleted() {}

    //shorthand for w() and h()
    static float w() {
        return canvas.getWidth();
    }
    static float h() {
        return canvas.getHeight();
    }

    //load video ad
    private void loadRewardedVideoAd() {
        // Actual ad ID: ca-app-pub-2436690700589338/1186751210
        // Test ad ID: ca-app-pub-3940256099942544/5224354917
        rva.loadAd("ca-app-pub-2436690700589338/1186751210",
                new AdRequest.Builder().build());
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
    static String getRiverSkin() {
        return sharedPref.getString("riverskin", "river");
    }
    private boolean firstTime() {
        return sharedPref.getBoolean("first_time", true);
    }

    private Bitmap getHookBmp() {
        switch(getBlitzSkin()) {
            case "iblitz":
                return hook_iblitz;
            case "arcade":
                return hook_arcade;
            default:
                return hook_classic;
        }
    }
    private Bitmap getHookGameoverBmp() {
        switch(getBlitzSkin()) {
            case "iblitz":
                return iblitzwithporo;
            case "arcade":
                return arcadewithporo;
            default:
                return blitzwithporo;
        }
    }
    private Bitmap getIconBmp(String s) {
        switch(s) {
            case "candy":
                return icon_candy;
            case "river":
                return icon_river;
            case "iblitz":
                return icon_iblitz;
            case "arcade":
                return icon_arcade;
            default:
                return icon_classic;
        }
    }

    private boolean hasSkin(String s) {
        if (s.equals("river") || s.equals("classic")) return true;
        return sharedPref.getBoolean("has_skin_"+s, false);
    }

    private Bitmap getRiverBmp() {
        switch(getRiverSkin()) {
            case "candy":
                return riverbmp_candy;
            default:
                return riverbmp;
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
        canvas.drawBitmap(bmp, null, rectF, null);
    }

    private void goToMenu(String s) {
        if (firstTime()) {
            editor.putBoolean("first_time", false);
            editor.apply();
        }

        prevMenu = menu;

        if (s.equals("start")
                || s.equals("game")
                || s.equals("stats")
                || s.equals("gameover")
                || s.equals("shop")
                || s.equals("more")
                || (s.equals("limbo") && (menu.equals("shop") || menu.equals("start"))))
            transition = TRANSITION_MAX;

        if (s.equals("shop") && !rva.isLoaded()) loadRewardedVideoAd();

        if (s.equals("burned")) burnAnimation = 0;

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
            channeling = false;
            waitingForTap = true;
            bullet = null;
            bulletCD = 3 + Math.random();
            clearPoroSnax();
            clearSnapTraps();
            player.reset();
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
            canvas.drawBitmap(titlescreen,w()/2-titlescreen.getWidth()/2,0,null);
        } else { //thicker
            canvas.drawBitmap(titlescreen,0,0,null);
        }

        //mini tutorial
        if (firstTime()) {
            canvas.drawARGB(100,0,0,0);

            canvas.drawLine(left.getX(),left.getY(),left.getX(),h()*2/3,white);
            canvas.drawText("SHOP",left.getX(),h()*2/3-c854(10),tutorialText);

            canvas.drawLine(middle.getX(),middle.getY(),middle.getX(),h()/3,white);
            canvas.drawText("PLAY",middle.getX(),h()/3-c854(10),tutorialText);

            canvas.drawLine(right.getX(),right.getY(),right.getX(),h()*2/3,white);
            canvas.drawText("MORE",right.getX(),h()*2/3-c854(10)-c480(25),tutorialText);
            canvas.drawText("GAMEMODES",right.getX(),h()*2/3-c854(10),tutorialText);
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
        canvas.drawBitmap(gameoverBmp,0,tmp,null);

        title_bold.setTextSize(c854(60));
        canvas.drawText("GAME OVER", w()/2, c854(125), title_bold);
        canvas.drawText("you scored: " + score, w()/2, c854(170), title);

        drawGameoverButtons();
    }

    private void drawRiver() {
        //background
        float tmp = -shift+w()*3;
        while (tmp < 0) tmp += w()*3;

        Bitmap bmp = getRiverBmp();
        /*
        int topY = (int)(bmp.getHeight() * (w()*3 - tmp) / (w()*3) - 1);
        canvas.drawBitmap(bmp, new Rect(0,topY,bmp.getWidth(),bmp.getHeight()),
                new RectF(0,0,w(),tmp), null);
        int botY = (int)(bmp.getHeight() * (h() - tmp) / (w()*3) + 1);
        if (tmp <= h()) canvas.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),botY),
                new RectF(0,tmp,w(),h()), null);
        */
        canvas.drawBitmap(bmp,0,tmp-bmp.getHeight(),null);
        if (tmp <= h()) canvas.drawBitmap(bmp,0,tmp,null);
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
        if (getRiverSkin().equals("candy") && !gamemode.equals("light") && !gamemode.equals("rr")) {
            canvas.drawBitmap(gradient,0,0,null);
            scoreTitle.setColor(Color.BLACK);
            scoreText.setColor(Color.BLACK);
        } else {
            scoreTitle.setColor(Color.WHITE);
            scoreText.setColor(Color.WHITE);
        }

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
        platforms.add(new Platform(canvas, w() / 2, h() / 2));
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
            if (gamemode.equals("scuttle") || gamemode.equals("rr")) rows = 1;

            float newX = platformW/2 + dist;
            float newY = (float)(prev.getY() + (rows+Math.random()/2) * platformW);
            if (distance(prev.getX(),prev.getY(),newX,newY) < player.getMaxRange()) {
                //probability of platform being a scuttle crab
                double prob = (gamemode.equals("scuttle") || gamemode.equals("rr"))
                        ? 1 : 0.5 * score/1000 / 15;
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
                //add a snap trap?
                double prob3 = 0.15;
                ev_snaptrap += prob3;
                double adjProb3 = (gamemode.equals("snare") || gamemode.equals("rr"))
                        ? 1 : prob3 * (1 + (ev_snaptrap-num_snaptrap)/2);
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
                if (gamemode.equals("spin") || gamemode.equals("rr")) p.addSpin();
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
            }
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
            }
        }
    }

    private void updateBullet() {
        if (bullet != null) bullet.update();

        if (bulletCD <= 0) {
            bullet = new Bullet(canvas, player, shift);
            bulletCD = 2 + Math.random();
        } else {
            bulletCD -= 1. / FRAMES_PER_SECOND;
        }

        if (player.isBurned()) {
            goToMenu("burned");
            gameoverBmp = getHookGameoverBmp();
        }
    }
}
