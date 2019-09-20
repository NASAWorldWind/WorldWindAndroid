/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class OnGestureHandler extends GestureDetector.SimpleOnGestureListener {

    FlingRecognizer flingRecognizer;

    @Override
    public boolean onDown(MotionEvent e) {
        flingRecognizer.stopAnimation();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return flingRecognizer != null && flingRecognizer.isValidFling(event1, event2, velocityX, velocityY);
    }

    public void setFlingRecognizer(FlingRecognizer flingRecognizer) {
        this.flingRecognizer = flingRecognizer;
    }
}
