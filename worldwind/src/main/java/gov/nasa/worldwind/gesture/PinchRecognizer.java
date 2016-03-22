/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.view.MotionEvent;

import gov.nasa.worldwind.WorldWind;

/**
 * Gesture recognizer implementation that detects for two finger pinch gestures.
 */
public class PinchRecognizer extends GestureRecognizer {

    protected float scale = 1;

    protected float scaleOffset = 1;

    protected float referenceDistance = 0;

    protected float interpretDistance = 20;

    protected int[] pointerIds = new int[2];

    protected int pointerIdCount = 0;

    public PinchRecognizer() {
    }

    public PinchRecognizer(GestureListener listener) {
        super(listener);
    }

    public float getScale() {
        return this.scale * this.scaleOffset;
    }

    @Override
    protected void reset() {
        super.reset();
        this.scale = 1;
        this.scaleOffset = 1;
        this.referenceDistance = 0;
        this.pointerIdCount = 0;
    }

    protected void actionDown(MotionEvent event) {
        int pointerId = event.getPointerId(event.getActionIndex());

        if (this.pointerIdCount < 2) {
            this.pointerIds[this.pointerIdCount++] = pointerId; // add it to the pointer ID array
            if (this.pointerIdCount == 2) {
                this.referenceDistance = this.currentPinchDistance(event);
                this.scaleOffset *= this.scale;
                this.scale = 1;
            }
        }
    }

    protected void actionMove(MotionEvent event) {
        if (this.pointerIdCount == 2) {
            int state = this.getState();
            if (state == WorldWind.POSSIBLE) {
                if (this.shouldRecognize(event)) {
                    this.transitionToState(event, WorldWind.BEGAN);
                }
            } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
                float distance = this.currentPinchDistance(event);
                float newScale = Math.abs(distance / this.referenceDistance);
                this.scale = this.lowPassFilter(this.scale, newScale);
                this.transitionToState(event, WorldWind.CHANGED);
            }
        }
    }

    protected void actionUp(MotionEvent event) {
        int pointerId = event.getPointerId(event.getActionIndex());
        if (this.pointerIds[0] == pointerId) { // remove the first pointer ID
            this.pointerIds[0] = this.pointerIds[1];
            this.pointerIdCount--;
        } else if (this.pointerIds[1] == pointerId) { // remove the second pointer ID
            this.pointerIdCount--;
        }
    }

    protected void prepareToRecognize(MotionEvent event) {
        this.referenceDistance = this.currentPinchDistance(event);
        this.scale = 1;
    }

    protected boolean shouldRecognize(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return false; // require exactly two pointers to recognize the gesture
        }

        double distance = this.currentPinchDistance(event);
        return Math.abs(distance - this.referenceDistance) > this.interpretDistance;
    }

    protected float currentPinchDistance(MotionEvent event) {
        int index0 = event.findPointerIndex(this.pointerIds[0]);
        int index1 = event.findPointerIndex(this.pointerIds[1]);
        float dx = event.getX(index0) - event.getX(index1);
        float dy = event.getY(index0) - event.getY(index1);

        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
