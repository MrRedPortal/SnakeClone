package com.becroft.snakeclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.Random;

public class Apple {
    // The locaiton of the apple on the grid (Not in pixels)
    private Point location = new Point();

    // Range of values to choose from spawn apple
    private Point spawnRange;
    private int size;

    // An image to represent apple
    private Bitmap bitmapApple;

    // Apple constructor
    Apple(Context context, Point sr, int s){
        // Note passed spawn range
        this.spawnRange = sr;
        // Note size of apple
        this.size = s;
        // Hide apple off screen
        location.x = -10;
        // Load image to bitmap
        bitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
        // Resize bitmap
        bitmapApple = Bitmap.createScaledBitmap(bitmapApple,s,s,false);
    }

    // Called everytime apple is eaten
    void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        location.x = random.nextInt(spawnRange.x) + 1;
        location.y = random.nextInt(spawnRange.y - 1) + 1;
    }

    // Let snakeGame know where apple is SnakeGame will share this with Snake
    Point getLocation(){
        return location;
    }

    void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(bitmapApple, location.x * size, location.y * size, paint);
    }
}
