package com.becroft.snakeclone;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class SnakeGame extends SurfaceView implements Runnable {

    //Objects for game loop/thread
    private Thread thread = null;
    //Control Pausing between updates
    private long nextFrameTime;
    // Is game currently paused
    private volatile boolean playing = false;
    private volatile boolean paused = true;

    // for playing sound effects
    private SoundPool soundPool;
    private int EAT_ID = -1;
    private int CRASH_ID = -1;

    // Size in segments of playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    // How many points
    private int score;

    // Objects for drawing
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    // a snake
    private Snake snake;
    // Apple duh
    private Apple apple;

    public SnakeGame(Context context, Point size) {
        super(context);

        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many block of the same size will fit into height
        numBlocksHigh = size.y / blockSize;

        // Init soundpool
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).
                    setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

            soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
        }
        try{
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            //Prepare sound in memory;
            descriptor = assetManager.openFd("get_apple.ogg");
            EAT_ID = soundPool.load(descriptor,0);

            descriptor = assetManager.openFd("snake_death.ogg");
            CRASH_ID = soundPool.load(descriptor,0);
        } catch (IOException e) {
            // Error
        }

        //init drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // Call game object constructors
        apple = new Apple(context, new Point(NUM_BLOCKS_WIDE, numBlocksHigh),blockSize);
        snake = new Snake(context, new Point(NUM_BLOCKS_WIDE, numBlocksHigh), blockSize);

    }

    // called to start new game
    public void newGame(){

        // Reset snake
        snake.reset(NUM_BLOCKS_WIDE,numBlocksHigh);

        // Prepare apple
        apple.spawn();
        // Reset score
        score = 0;
        // Setup nextFrameTime so an update can be triggered
        nextFrameTime = System.currentTimeMillis();
    }

    @Override
    // handles game loop
    public void run(){
        while(playing){
            if(!paused){
                // update 10 times a second
                if(updateRequired()){
                    update();
                }
            }
            draw();
        }
    }

    // Check to see if time for an update
    public boolean updateRequired(){
        // Run at 10fps
        final long TARGET_FPS = 10;
        // Milis in second
        final long MILLIS_IN_SECOND = 1000;

        // Are we updating?
        if(nextFrameTime<=System.currentTimeMillis()){
            // tenth of second has passed

            // Setup next frame time
            nextFrameTime = System.currentTimeMillis() + MILLIS_IN_SECOND / TARGET_FPS;

            // Return true so that the update can draw
            return true;
        }
        return false;
    }

    // Update all game objects
    public void update(){
        // Move snake
        snake.move();

        // Did head ate apple
        if(snake.checkDinner(apple.getLocation())){
            // Position new apple
            apple.spawn();
            // Increase score
            score = score +1;
            // play tune
            soundPool.play(EAT_ID,1,1,0,0,1);
        }
        // Did snake died?
        if (snake.detectDeath()){
            // Pause screen and play tune
            soundPool.play(CRASH_ID, 1,1,0,0,1);
            paused = true;
        }

    }

    // Draw the things
    public void draw(){
        // lock canvas
        if (surfaceHolder.getSurface().isValid()){
            canvas = surfaceHolder.lockCanvas();

            // fill screen with colour
            canvas.drawColor(Color.argb(255,26,128,182));

            // Set the size and colour of text
            paint.setColor(Color.argb(255,255,255,255));
            paint.setTextSize(120);

            //Draw score
            canvas.drawText("" + score, 20,120,paint);

            // draw apple and snake
            apple.draw(canvas, paint);
            snake.draw(canvas, paint);

            // Draw some text while paused
            if(paused){
                // set colour and size
                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(250);

                // Draw message
                // Subject to change
                canvas.drawText(getResources().getString(R.string.tap_to_play),200,700,paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // When touches happen
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch(motionEvent.getAction() & MotionEvent.ACTION_MASK){
            case(MotionEvent.ACTION_UP):
                if(paused){
                    paused=false;
                    newGame();

                    // Don't want to process snake movement on this tap
                    return true;
                }
                // Let snake class handle input
                snake.switchHeading(motionEvent);
                break;
            default:
                break;
        }
        return true;
    }

    // Stop thread
    public void pause(){
        playing = false;
        try{
            thread.join();
        } catch (InterruptedException e){
            // Error
        }
    }

    // Start the thread
    public void resume(){
        playing=true;
        thread = new Thread(this);
        thread.start();
    }
}
