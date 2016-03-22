/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.view.MotionEvent;

import gov.nasa.worldwind.WorldWind;

/**
 * Gesture recognizer implementation that detects touch panning gestures.
 */
public class PanRecognizer extends GestureRecognizer {

    protected int minNumberOfPointers = 1;

    protected int maxNumberOfPointers = Integer.MAX_VALUE;

    protected float interpretDistance = 20;

    public PanRecognizer() {
    }

    public PanRecognizer(GestureListener listener) {
        super(listener);
    }

    public int getMinNumberOfPointers() {
        return minNumberOfPointers;
    }

    public void setMinNumberOfPointers(int count) {
        this.minNumberOfPointers = count;
    }

    public int getMaxNumberOfPointers() {
        return maxNumberOfPointers;
    }

    public void setMaxNumberOfPointers(int count) {
        this.maxNumberOfPointers = count;
    }

    @Override
    protected void actionMove(MotionEvent event) {
        int state = this.getState();
        if (state == WorldWind.POSSIBLE) {
            if (this.shouldInterpret(event)) {
                if (this.shouldRecognize(event)) {
                    this.transitionToState(event, WorldWind.BEGAN);
                } else {
                    this.transitionToState(event, WorldWind.FAILED);
                }
            }
        } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
            this.transitionToState(event, WorldWind.CHANGED);
        }
    }

    @Override
    protected void prepareToRecognize(MotionEvent event) {
        // set translation to zero when the pan begins
        this.setTranslationX(0);
        this.setTranslationY(0);
    }

    protected boolean shouldInterpret(MotionEvent event) {
        float dx = this.getTranslationX();
        float dy = this.getTranslationY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance > this.interpretDistance; // interpret touches when the touch centroid moves far enough
    }

    protected boolean shouldRecognize(MotionEvent event) {
        int count = event.getPointerCount();
        return count != 0
            && count >= this.minNumberOfPointers
            && count <= this.maxNumberOfPointers;
    }
}
