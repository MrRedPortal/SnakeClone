package com.becroft.snakeclone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;

public class SnakeActivity extends Activity {

    // Declare instance of snake game
    SnakeGame snakeGame;
    // Set up the game
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // get pixel dimensions of screen
        Display display = getWindowManager().getDefaultDisplay();

        // Intialise the result into a point object
        Point size = new Point();
        display.getSize(size);

        // create instance of snakeGame
        snakeGame = new SnakeGame(this, size);

        // MAke snakeGame the view of activity
        setContentView(snakeGame);
    }

    // Start thread in snakeGame
    @Override
    protected void onResume(){
        super.onResume();
        snakeGame.resume();
    }

    // Stop thread in snakeGame
    @Override
    protected void onPause(){
        super.onPause();
        snakeGame.pause();
    }

}











