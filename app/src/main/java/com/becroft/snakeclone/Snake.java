package com.becroft.snakeclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import java.util.ArrayList;

public class Snake {
    // Location in grid of all segments
    private ArrayList<Point> segmentLocations;

    // How big are the segments
    private int segmentSize;

    // How big is the whole grid
    private Point movementRange;

    // Center of screen horizontally in pixels
    private int halfwayPoint;

    // For tracking movement direction
    private enum Heading{
        UP, RIGHT, DOWN, LEFT
    }

    // Start by heading right
    private Heading heading = Heading.RIGHT;

    // A bitmap for each direction the head can face
    private Bitmap bitmapHeadRight;
    private Bitmap bitmapHeadLeft;
    private Bitmap bitmapHeadUp;
    private Bitmap bitmapHeadDown;
    // Body bitmap
    private Bitmap bitmapBody;

    Snake(Context context, Point mr, int ss){
        // init arrayList
        this.segmentLocations = new ArrayList<>();
        // init segment size and movement range
        this.movementRange = mr;
        this.segmentSize = ss;

        // Create and scale bitmaps
        // Head right
        bitmapHeadRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        // Other three heads
        bitmapHeadLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        bitmapHeadDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        bitmapHeadUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        // Modify bitmaps to make them face the correct direction
        bitmapHeadRight = Bitmap.createScaledBitmap(bitmapHeadRight, ss, ss, false);

        // Matrix for scaling
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        bitmapHeadLeft = Bitmap.createBitmap(bitmapHeadRight, 0,0,ss,ss,matrix,true);
        // Matrix for rotating
        matrix.preRotate(-90);
        bitmapHeadUp = Bitmap.createBitmap(bitmapHeadRight,0,0,ss,ss,matrix,true);
        // matrix operations are cumulative rotate by 180
        matrix.preRotate(180);
        bitmapHeadDown = Bitmap.createBitmap(bitmapHeadRight,0,0,ss,ss,matrix,true);

        // Create and scale body
        bitmapBody = BitmapFactory.decodeResource(context.getResources(), R.drawable.body);
        bitmapBody = Bitmap.createScaledBitmap(bitmapBody, ss, ss, false);

        // Halfway point to determine which side of screen is pressed
        halfwayPoint = mr.x * ss /2;

    }

    // Get snake ready for new game
    void reset(int w, int h){
        // Reset heading
        heading = Heading.RIGHT;
        // Delete old contents of array list
        segmentLocations.clear();
        // Start with single segment
        segmentLocations.add(new Point(w/2,h/2));
    }


    void move(){
        // Move the body, Start at back and move it
        // to the positon of the segment infront of it
        for(int i = segmentLocations.size() -1; i > 0; i--){
            // Make it the same value as the segment going forwards towards the head
            segmentLocations.get(i).x = segmentLocations.get(i-1).x;
            segmentLocations.get(i).y = segmentLocations.get(i-1).y;

        }

        // Move the head in the appropriate heading
        // Get existing head position
        Point p = segmentLocations.get(0);

        // Move it appropriately
        switch (heading){
            case UP:
                p.y--;
                break;
            case DOWN:
                p.y++;
                break;
            case RIGHT:
                p.x++;
                break;
            case LEFT:
                p.x--;
                break;
        }

        segmentLocations.set(0,p);

    }

    boolean detectDeath(){
        // Has snake died?
        boolean dead = false;
        // Check if hit and screen edges
        if(segmentLocations.get(0).x == -1 || segmentLocations.get(0).x > movementRange.x ||
                segmentLocations.get(0).y == -1 || segmentLocations.get(0).y > movementRange.y){
            dead = true;
        }

        // Eaten itself
        for(int i = segmentLocations.size() -1; i>0; i--){
            //Have any sections collided with head
            if (segmentLocations.get(0).x == segmentLocations.get(i).x &&segmentLocations.get(0).y == segmentLocations.get(i).y){
                dead = true;
            }
        }
        return dead;
    }

    boolean checkDinner(Point l){
        // if snakeX == l.x && snakeY == l.y
        if(segmentLocations.get(0).x == l.x && segmentLocations.get(0).y == l.y){
            // add new Point to list located off screen.
            segmentLocations.add(new Point(-10,-10));
            return true;
        }
        return false;
    }

    void draw(Canvas canvas, Paint paint){
        // Dont run this code with nothing in arrayList
        if(!segmentLocations.isEmpty()){
            // draw head
            switch (heading){
                case RIGHT:
                    canvas.drawBitmap(bitmapHeadRight, segmentLocations.get(0).x * segmentSize,
                            segmentLocations.get(0).y *segmentSize, paint);
                    break;
                case LEFT:
                    canvas.drawBitmap(bitmapHeadLeft, segmentLocations.get(0).x * segmentSize,
                            segmentLocations.get(0).y *segmentSize, paint);
                    break;
                case UP:
                    canvas.drawBitmap(bitmapHeadUp, segmentLocations.get(0).x * segmentSize,
                            segmentLocations.get(0).y *segmentSize, paint);
                    break;
                case DOWN:
                    canvas.drawBitmap(bitmapHeadDown, segmentLocations.get(0).x * segmentSize,
                            segmentLocations.get(0).y *segmentSize, paint);
                    break;
            }
            for(int i = 1; i<segmentLocations.size();i++){
                canvas.drawBitmap(bitmapBody, segmentLocations.get(i).x *segmentSize,
                        segmentLocations.get(i).y * segmentSize, paint);
            }
        }
    }

    // handle changing direction
    void switchHeading(MotionEvent motionEvent) {
        // Is tap on right side of screen?
        if (motionEvent.getX() >= halfwayPoint) {
            switch (heading) {
                case UP:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.UP;
            }
        } else {
            // Rotate Left
            switch (heading) {
                case UP:
                    heading = Heading.LEFT;
                    break;
                case RIGHT:
                    heading = Heading.UP;
                    break;
                case DOWN:
                    heading = Heading.RIGHT;
                    break;
                case LEFT:
                    heading = Heading.DOWN;
            }
        }
    }
}
