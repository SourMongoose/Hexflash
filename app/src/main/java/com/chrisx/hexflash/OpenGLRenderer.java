package com.chrisx.hexflash;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressWarnings("all")
class OpenGLRenderer implements GLSurfaceView.Renderer {
    public Context context;

    static int width, height;


    static Bitmap poro, poro_black, scuttler, scuttler_candy, titlescreen, porosnax, porosnax_count,
            snaptrap, snarefx, hook_classic, hook_iblitz, hook_arcade, icon_classic, icon_iblitz,
            icon_arcade, icon_river, icon_candy, blitzwithporo, iblitzwithporo, arcadewithporo,
            lilypad, lilypadlotus, candypad_red, candypad_orange, candypad_yellow, sadporo,
            sadporo_spin, burntporo, riverbmp, riverbmp_candy, restart, home, shop, play, more,
            leftarrow, maxrange, indicator, bubble, border, bulletbmp, explosion, lock, gradient,
            stats, video, flash, flash2;
    static Bitmap[] sinking, medals;
    public Bitmap gameoverBmp;

    public String blitzskins[] = {"classic", "iblitz", "arcade"};
    public int blitzskins_cost[] = {0, 50, 50};
    public int nBlitz = blitzskins.length;
    public RectF blitzskins_rectf[] = new RectF[nBlitz];
    public int ICON_WIDTH;
    public float BLITZSKINS_Y;
    public boolean blitzskins_owned[];
    public String blitzskin_used;

    public String riverskins[] = {"river", "candy"};
    public int riverskins_cost[] = {0, 100};
    public int nRiver = riverskins.length;
    public RectF riverskins_rectf[] = new RectF[nRiver];
    public float RIVERSKINS_Y;
    public boolean riverskins_owned[];
    public String riverskin_used;

    public int prevPorosnax;
    public boolean adLoaded;

    static SharedPreferences sharedPref;
    public SharedPreferences.Editor editor;

    public Typeface cd, cd_i, cd_b, cd_bi;

    public boolean paused = false;
    public long frameCount = 0;

    public String menu = "start", prevMenu = "start";
    public String loadMenu = "";
    public String lastPressMenu;
    public String gamemode = "classic";

    //frame data
    static final int FRAMES_PER_SECOND = 60;
    public long nanosecondsPerFrame;

    static final int TRANSITION_MAX = FRAMES_PER_SECOND * 2 / 3;
    public int transition = TRANSITION_MAX / 2;
    public int prevTransition;

    public float lastX, lastY;
    public float downX, downY;
    public boolean pressed;

    public Paint title_bold, title, mode, scoreTitle, scoreText, river_fade, quarter,
            adText, priceText, medalText, tutorialText, white, startText;
    public int river = Color.rgb(35,66,94);

    public CircleButton middle, left, right;
    public float offset, MIDDLE_Y1, MIDDLE_Y2;

    public RoundRectButton classic, light, spin, scuttle, snare, cc, rr;

    public String modeNames[] = {"CLASSIC", "NIGHT LIGHTS", "SPIN TO WIN", "SCUTTLE TROUBLE",
            "SNARE FAIR", "CURTAIN CALL", "COMBO CHAOS"};
    public String modeCodes[] = {"classic", "light", "spin", "scuttle", "snare", "cc", "rr"};
    public int medal_scores[][] = {{2500,5000,10000},{1500,3000,6000},{1000,2000,4000},
            {1000,2000,4000},{1500,3000,6000},{1500,3000,6000},{250,500,1000}};

    public CircleButton cbs[];
    public boolean cbs_pressed[];
    public RoundRectButton rrbs[];
    public boolean rrbs_pressed[];

    public Poro player;
    public boolean channeling, waitingForTap;
    public float playerY;
    public int score;

    public double lightning, wait;
    public boolean jumped = false;
    public final double MAX_LIGHTNING = 1.5;
    public final double MAX_WAIT = 2.75;

    public double ev_scuttle, ev_porosnax, ev_snaptrap; //expected value
    public int num_scuttle = 0, num_porosnax = 0, num_snaptrap = 0; //actual

    public List<Platform> platforms = new ArrayList<>();
    public List<PoroSnax> snaxlist = new ArrayList<>();
    public List<SnapTrap> snaptraps = new ArrayList<>();
    public Bullet bullet;
    public double bulletCD; //cooldown

    public float shift, //pixels translated down
            shiftSpeed = 0.75f;
    public int hookAnimation, sinkAnimation, burnAnimation;
    

    public final float[] mMVPMatrix = new float[16];
    public final float[] mProjectionMatrix = new float[16];
    public final float[] mViewMatrix = new float[16];
    public float[] mRotationMatrix = new float[16];

    OpenGLRenderer(Context context) {
        this.context = context;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
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

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = width;
        this.height = height;

        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, 0, height, 1000, -1000);
    }

    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        //Redraw background color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        //Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1, 0, 0, -1, 0, 1, 0);

        //Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        
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
                if (update) drawTitleMenu(mMVPMatrix);
            } else if (menu.equals("more")) {
                //check for updates (button presses)
                for (int i = 1; i < rrbs.length-1; i++) {
                    if (rrbs[i].isPressed() != rrbs_pressed[i]) {
                        update = true;
                        rrbs_pressed[i] = rrbs[i].isPressed();
                    }
                }

                if (update) {
                    //canvas.drawColor(river);

                    title_bold.setTextSize(c854(60));
                    //canvas.drawText("GAMEMODES", w()/2, c854(80), title_bold);

                    mode.setTextAlign(Paint.Align.CENTER);
                    float tmp = (mode.ascent() + mode.descent()) / 2;
                    for (int i = 1; i < modeNames.length-1; i++) {
                        rrbs[i].draw();
                        //canvas.drawText(modeNames[i], rrbs[i].getRectF().centerX(),
                        //        rrbs[i].getRectF().centerY()-tmp, mode);
                    }

                    //back
                    //canvas.drawBitmap(leftarrow,c854(10),h()-c854(90),null);
                    //stats
                    //canvas.drawBitmap(stats,w()-c854(80),h()-c854(80),null);
                }
            } else if (menu.equals("stats")) {
                if (update) {
                    //canvas.drawColor(river);

                    title_bold.setTextSize(c854(50));
                    //canvas.drawText("HIGH SCORES", w()/2, c854(70), title_bold);

                    mode.setTextAlign(Paint.Align.LEFT);
                    for (int i = 0; i < modeNames.length; i++) {
                        float tmp = rrbs[i].getRectF().centerY();
                        tmp = h()/2+(tmp-h()/2)*0.9f;

                        mode.setTextSize(c854(25));
                        //canvas.drawText(modeNames[i], c480(20), tmp, mode);
                        mode.setTextSize(c854(35));
                        //canvas.drawText(getHighScore(modeCodes[i])+"", c480(20), tmp+c854(35), mode);

                        int nextMedal = -1;
                        for (int m = 0; m < 3; m++) {
                            //canvas.drawBitmap((getHighScore(modeCodes[i]) >= medal_scores[i][m] ? medals[m] : medals[3]),
                            //        c480(460)-c854(120-m*40),tmp-c854(15),null);
                            if (nextMedal == -1 && getHighScore(modeCodes[i]) < medal_scores[i][m])
                                nextMedal = medal_scores[i][m];
                        }
                        //if (nextMedal != -1)
                            //canvas.drawText("Next medal: " + nextMedal, w()-c480(20), tmp+c854(50), medalText);
                    }

                    //back
                    //canvas.drawBitmap(leftarrow,c854(10),h()-c854(90),null);
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
                /*
                if (adLoaded != rva.isLoaded()) {
                    update = true;
                    adLoaded = rva.isLoaded();
                }
                */
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
                    //canvas.drawColor(river);

                    title_bold.setTextSize(c854(60));
                    //canvas.drawText("SHOP", w()/2, c854(80), title_bold);

                    //blitzcrank skins
                    //canvas.drawText("BLITZ SKINS", w()/2, BLITZSKINS_Y-ICON_WIDTH/2, title);
                    for (int i = 0; i < nBlitz; i++) {
                        //canvas.drawBitmap(getIconBmp(blitzskins[i]),blitzskins_rectf[i].left,blitzskins_rectf[i].top,null);
                        if (getBlitzSkin().equals(blitzskins[i])) {
                            RectF rf = blitzskins_rectf[i];
                            //canvas.drawBitmap(border,rf.left-ICON_WIDTH/8,rf.top-ICON_WIDTH/8,null);
                        }
                        if (!hasSkin(blitzskins[i])) {
                            RectF rf = blitzskins_rectf[i];
                            //canvas.drawRect(rf, river_fade);
                            //canvas.drawBitmap(lock,rf.left+rf.width()/3,rf.top+rf.width()/3,null);
                            //canvas.drawText(blitzskins_cost[i]+"", rf.centerX(),
                            //        rf.bottom-rf.width()/10, priceText);
                        }
                    }
                    //river skins
                    //canvas.drawText("RIVER SKINS", w()/2, RIVERSKINS_Y-ICON_WIDTH/2, title);
                    for (int i = 0; i < nRiver; i++) {
                        //canvas.drawBitmap(getIconBmp(riverskins[i]),
                        //        riverskins_rectf[i].left,riverskins_rectf[i].top,null);
                        if (getRiverSkin().equals(riverskins[i])) {
                            RectF rf = riverskins_rectf[i];
                            //canvas.drawBitmap(border,rf.left-ICON_WIDTH/8,rf.top-ICON_WIDTH/8,null);
                        }
                        if (!hasSkin(riverskins[i])) {
                            RectF rf = riverskins_rectf[i];
                            //canvas.drawRect(rf, river_fade);
                            //canvas.drawBitmap(lock,rf.left+rf.width()/3,rf.top+rf.width()/3,null);
                            //canvas.drawText(riverskins_cost[i]+"", rf.centerX(),
                            //        rf.bottom-rf.width()/10, priceText);
                        }
                    }

                    //porosnax count
                    //canvas.drawBitmap(porosnax_count,w()-c854(75),h()-c854(75),null);
                    title.setTextAlign(Paint.Align.RIGHT);
                    //canvas.drawText(getPoroSnax()+"", w()-c854(85), h()-c854(50)-(title.ascent()+title.descent())/2, title);
                    title.setTextAlign(Paint.Align.CENTER);

                    /*
                    //video ad
                    if (rva.isLoaded()) {
                        //canvas.drawBitmap(video,w()-c854(75),c854(25),null);
                        adText.setAlpha(255);
                    } else {
                        //canvas.drawBitmap(video, new Rect(0,0,video.getWidth(),video.getHeight()),
                                new RectF(w()-c854(75),c854(25),w()-c854(25),c854(75)), quarter);
                        adText.setAlpha(64);
                    }
                    //canvas.drawText("+10", w()-c854(50), c854(100), adText);
                    */

                    //back
                    //canvas.drawBitmap(leftarrow,c854(10),h()-c854(90),null);
                }
            } else if (menu.equals("game")) {
                update = true;

                //background
                drawRiver();

                float[] mtx = mMVPMatrix.clone();
                Matrix.translateM(mtx, 0, 0, -shift, 0); //screen shift

                drawPlatforms(mtx);
                drawPoroSnax(mtx);
                drawSnapTraps(mtx);

                player.draw(mtx);

                if (!waitingForTap && (gamemode.equals("cc") || gamemode.equals("rr")))
                    if (bullet != null && bullet.visible(shift))
                        bullet.draw(mtx);

                //stop using mtx

                if (!waitingForTap && (gamemode.equals("light") || gamemode.equals("rr")))
                    drawLightning();

                drawScores();

                if (waitingForTap) {
                    if (getRiverSkin().equals("candy"))
                        startText.setColor(Color.BLACK);
                    else startText.setColor(Color.WHITE);
                    float y = platforms.get(0).getY() - platforms.get(0).getW();
                    //canvas.drawText("tap to start", w() / 2, y, startText);
                }
            } else if (menu.equals("hook")) {
                update = true;

                //background
                drawRiver();

                float[] mtx = mMVPMatrix.clone();
                Matrix.translateM(mtx, 0, 0, -shift, 0); //screen shift

                drawPlatforms(mtx);
                drawPoroSnax(mtx);
                drawSnapTraps(mtx);
                player.draw(mtx);
                if ((gamemode.equals("cc") || gamemode.equals("rr"))
                        && bullet != null && bullet.visible(shift)) {
                    bullet.draw(mtx);
                }
                //stop using mtx

                int hookDuration = FRAMES_PER_SECOND * 2 / 3;
                float hookWidth = w() / 6;
                if (hookAnimation < hookDuration / 2) {
                    //hook enters screen
                    float hookY = (playerY + player.getW() - shift) * (hookAnimation / (hookDuration / 2f));
                    //canvas.drawBitmap(getHookBmp(),player.getX()-hookWidth/2,hookY-hookWidth*3,null);
                } else {
                    //hook exits screen w/ poro
                    float hookY = (playerY + player.getW() - shift) * ((hookDuration - hookAnimation) / (hookDuration / 2f));
                    //canvas.drawBitmap(getHookBmp(),player.getX()-hookWidth/2,hookY-hookWidth*3,null);
                }

                drawScores();
            } else if (menu.equals("sink")) {
                update = true;

                //background
                drawRiver();

                float[] mtx = mMVPMatrix.clone();
                Matrix.translateM(mtx, 0, 0, -shift, 0); //screen shift

                drawPlatforms(mtx);
                drawPoroSnax(mtx);
                drawSnapTraps(mtx);

                player.draw(mtx);

                if ((gamemode.equals("cc") || gamemode.equals("rr"))
                        && bullet != null && bullet.visible(shift)) {
                    bullet.draw(mtx);
                }

                //stop using mtx

                drawScores();
            } else if (menu.equals("burned")) {
                update = true;

                //background
                drawRiver();

                float[] mtx = mMVPMatrix.clone();
                Matrix.translateM(mtx, 0, 0, -shift, 0); //screen shift

                drawPlatforms(mtx);
                drawPoroSnax(mtx);
                drawSnapTraps(mtx);

                player.draw(mtx);

                int explodeDuration = FRAMES_PER_SECOND / 3;
                if (burnAnimation < explodeDuration) {
                    float f = (float)(explodeDuration - burnAnimation) / explodeDuration;
                    drawBmp(explosion, new RectF(player.getX()-player.getW()*f,
                            player.getY()-player.getW()*f,
                            player.getX()+player.getW()*f,
                            player.getY()+player.getW()*f));
                }

                //stop using mtx

                drawScores();
            } else if (menu.equals("gameover")) {
                update = true;

                drawGameoverScreen(mMVPMatrix);

                if (transition == 0) goToMenu("limbo");
            } else if (menu.equals("limbo")) {
                //check for updates (button presses)
                for (int i = 0; i < cbs.length; i++) {
                    if (cbs[i].isPressed() != cbs_pressed[i]) {
                        update = true;
                        cbs_pressed[i] = cbs[i].isPressed();
                    }
                }

                if (update) drawGameoverScreen(mMVPMatrix);
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
            //canvas.drawColor(Color.argb(alpha,
            //        Color.red(river), Color.green(river), Color.blue(river)));
        }
    }

    static float w() {
        return width;
    }
    static float h() {
        return height;
    }
    
    public static int loadShader(int type, String shaderCode){
        //create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        //or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        //add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    //creates an instance of Paint set to a given color
    public Paint newPaint(int color) {
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

    public long getHighScore(String s) {
        return sharedPref.getInt("high_score_"+s, 0);
    }
    public int getPoroSnax() {
        return sharedPref.getInt("porosnax", 0);
    }
    public String getBlitzSkin() {
        return sharedPref.getString("blitzskin", "classic");
    }
    static String getRiverSkin() {
        return sharedPref.getString("riverskin", "river");
    }
    public boolean firstTime() {
        return sharedPref.getBoolean("first_time", true);
    }

    public Bitmap getHookBmp() {
        switch(getBlitzSkin()) {
            case "iblitz":
                return hook_iblitz;
            case "arcade":
                return hook_arcade;
            default:
                return hook_classic;
        }
    }
    public Bitmap getHookGameoverBmp() {
        switch(getBlitzSkin()) {
            case "iblitz":
                return iblitzwithporo;
            case "arcade":
                return arcadewithporo;
            default:
                return blitzwithporo;
        }
    }
    public Bitmap getIconBmp(String s) {
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

    public boolean hasSkin(String s) {
        if (s.equals("river") || s.equals("classic")) return true;
        return sharedPref.getBoolean("has_skin_"+s, false);
    }

    public Bitmap getRiverBmp() {
        switch(getRiverSkin()) {
            case "candy":
                return riverbmp_candy;
            default:
                return riverbmp;
        }
    }

    public double toRad(double deg) {
        return Math.PI/180*deg;
    }

    //distance between (x1,y1) and (x2,y2)
    static double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    //draw a bitmap w/o cropping
    public void drawBmp(Bitmap bmp, RectF rectF) {
        //canvas.drawBitmap(bmp, null, rectF, null);
    }

    public void goToMenu(String s) {
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

        //if (s.equals("shop") && !rva.isLoaded()) loadRewardedVideoAd();

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

    public void drawTitleMenu(float[] m) {
        if (h()/w() > 4./3) { //thinner
            //canvas.drawBitmap(titlescreen,w()/2-titlescreen.getWidth()/2,0,null);
        } else { //thicker
            //canvas.drawBitmap(titlescreen,0,0,null);
        }

        //mini tutorial
        if (firstTime()) {
            //canvas.drawARGB(100,0,0,0);

            //canvas.drawLine(left.getX(),left.getY(),left.getX(),h()*2/3,white);
            //canvas.drawText("SHOP",left.getX(),h()*2/3-c854(10),tutorialText);

            //canvas.drawLine(middle.getX(),middle.getY(),middle.getX(),h()/3,white);
            //canvas.drawText("PLAY",middle.getX(),h()/3-c854(10),tutorialText);

            //canvas.drawLine(right.getX(),right.getY(),right.getX(),h()*2/3,white);
            //canvas.drawText("MORE",right.getX(),h()*2/3-c854(10)-c480(25),tutorialText);
            //canvas.drawText("GAMEMODES",right.getX(),h()*2/3-c854(10),tutorialText);
        }

        //play button
        middle.draw(m);
        RectF tmp = new RectF(middle.getX()-middle.getR()/2f, middle.getY()-middle.getR()/1.8f,
                middle.getX()+middle.getR()/1.6f, middle.getY()+middle.getR()/1.8f);
        drawBmp(play, tmp);

        //shop
        left.draw(m);
        tmp = new RectF(left.getX()-left.getR()/1.414f, left.getY()-left.getR()/1.414f,
                left.getX()+left.getR()/1.414f, left.getY()+left.getR()/1.414f);
        drawBmp(shop, tmp);

        //more gamemodes
        right.draw(m);
        tmp = new RectF(right.getX()-right.getR()/1.9f, right.getY()-right.getR()/1.9f,
                right.getX()+right.getR()/1.9f, right.getY()+right.getR()/1.9f);
        drawBmp(more, tmp);
    }

    public void drawGameoverButtons(float[] m) {
        //canvas.drawRect(0,middle.getY()-middle.getR()-c854(5),w(),middle.getY()+middle.getR()+c854(5),newPaint(river));

        //restart button
        middle.draw(m);
        RectF tmp = new RectF(middle.getX()-middle.getR()/1.8f, middle.getY()-middle.getR()/1.8f,
                middle.getX()+middle.getR()/1.8f, middle.getY()+middle.getR()/1.8f);
        drawBmp(restart, tmp);

        //shop
        left.draw(m);
        tmp = new RectF(left.getX()-left.getR()/1.414f, left.getY()-left.getR()/1.414f,
                left.getX()+left.getR()/1.414f, left.getY()+left.getR()/1.414f);
        drawBmp(shop, tmp);

        //back to home
        right.draw(m);
        tmp = new RectF(right.getX()-right.getR()/1.9f, right.getY()-right.getR()/1.9f,
                right.getX()+right.getR()/1.9f, right.getY()+right.getR()/1.9f);
        drawBmp(home, tmp);
    }
    public void drawGameoverScreen(float[] m) {
        //canvas.drawColor(river);

        float tmp = Math.max(h()-w(), middle.getY()+middle.getR()+c854(5));
        //canvas.drawBitmap(gameoverBmp,0,tmp,null);

        title_bold.setTextSize(c854(60));
        //canvas.drawText("GAME OVER", w()/2, c854(125), title_bold);
        //canvas.drawText("you scored: " + score, w()/2, c854(170), title);

        drawGameoverButtons(m);
    }

    public void drawRiver() {
        //background
        float tmp = -shift+w()*3;
        while (tmp < 0) tmp += w()*3;

        Bitmap bmp = getRiverBmp();
        /*
        int topY = (int)(bmp.getHeight() * (w()*3 - tmp) / (w()*3) - 1);
        //canvas.drawBitmap(bmp, new Rect(0,topY,bmp.getWidth(),bmp.getHeight()),
                new RectF(0,0,w(),tmp), null);
        int botY = (int)(bmp.getHeight() * (h() - tmp) / (w()*3) + 1);
        if (tmp <= h()) //canvas.drawBitmap(bmp, new Rect(0,0,bmp.getWidth(),botY),
                new RectF(0,tmp,w(),h()), null);
        */
        //canvas.drawBitmap(bmp,0,tmp-bmp.getHeight(),null);
        //if (tmp <= h()) canvas.drawBitmap(bmp,0,tmp,null);
    }

    public int blend(int a, int b) {
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
    public void drawLightning() {
        //darkness
        int alpha = (int)(240 * (MAX_LIGHTNING - lightning) / MAX_LIGHTNING);
        int dark = Color.argb(alpha,0,0,0);
        //lightning
        int light = Color.argb(240-alpha,255,255,255);
        //canvas.drawColor(blend(dark, light));

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

    public void drawScores() {
        if (getRiverSkin().equals("candy") && !gamemode.equals("light") && !gamemode.equals("rr")) {
            //canvas.drawBitmap(gradient,0,0,null);
            scoreTitle.setColor(Color.BLACK);
            scoreText.setColor(Color.BLACK);
        } else {
            scoreTitle.setColor(Color.WHITE);
            scoreText.setColor(Color.WHITE);
        }

        scoreTitle.setTextAlign(Paint.Align.LEFT);
        //canvas.drawText("score", c480(10), c854(25), scoreTitle);
        scoreTitle.setTextAlign(Paint.Align.RIGHT);
        //canvas.drawText("high", w()-c480(10), c854(25), scoreTitle);
        scoreText.setTextAlign(Paint.Align.LEFT);
        //canvas.drawText(score+"", c480(10), c854(60), scoreText);
        scoreText.setTextAlign(Paint.Align.RIGHT);
        //canvas.drawText(getHighScore(gamemode)+"", w()-c480(10), c854(60), scoreText);
    }

    //delete all platforms and initialize one lilypad
    public void resetPlatforms() {
        platforms.clear();
        platforms.add(new Platform(w(), h(), w() / 2, h() / 2));
        player.setPlatform(platforms.get(0));
    }
    public void generatePlatforms() {
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
                    platforms.add(new Platform(w(), h(), newX, newY, (float) (1 + shiftSpeed + 0.5 * Math.random())));
                    num_scuttle++;
                } else {
                    platforms.add(new Platform(w(), h(), newX, newY));
                }
                //add a porosnax?
                double prob2 = 0.2;
                ev_porosnax += prob2;
                double adjProb2 = prob2 * (1 + (ev_porosnax-num_porosnax)/2);
                if (Math.random() < adjProb2) {
                    snaxlist.add(new PoroSnax(w(), h(), platforms.get(platforms.size() - 1)));
                    num_porosnax++;
                }
                //add a snap trap?
                double prob3 = 0.15;
                ev_snaptrap += prob3;
                double adjProb3 = (gamemode.equals("snare") || gamemode.equals("rr"))
                        ? 1 : prob3 * (1 + (ev_snaptrap-num_snaptrap)/2);
                if (Math.random() < adjProb3) {
                    snaptraps.add(new SnapTrap(w(), h(), platforms.get(platforms.size() - 1)));
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
    public void drawPlatforms(float[] m) {
        for (Platform p : platforms)
            if (p.visible(shift)) p.draw(m);
    }
    public void movePlatforms() {
        for (Platform p : platforms)
            if (p.visible(shift)) {
                p.update();
                if (gamemode.equals("spin") || gamemode.equals("rr")) p.addSpin();
            }
    }

    //find the platform that the player has landed on, return false if lands in water
    public boolean checkForPlatform(Poro player) {
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

    public void clearPoroSnax() {
        snaxlist.clear();
    }
    public void drawPoroSnax(float[] m) {
        for (PoroSnax p : snaxlist)
            if (p.visible(shift)) p.draw(m);
    }
    public void updatePoroSnax() {
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

    public void clearSnapTraps() {
        snaptraps.clear();
    }
    public void drawSnapTraps(float[] m) {
        for (SnapTrap s : snaptraps)
            if (s.visible(shift)) s.draw(m);
    }
    public void updateSnapTraps() {
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

    public void updateBullet() {
        if (bullet != null) bullet.update();

        if (bulletCD <= 0) {
            bullet = new Bullet(w(), h(), player, shift);
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
