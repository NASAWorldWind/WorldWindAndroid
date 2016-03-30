/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.view.MotionEvent;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.WWMath;

public class RotationRecognizer extends GestureRecognizer {

    protected float rotation = 0;

    protected float rotationOffset = 0;

    protected float referenceAngle = 0;

    protected float interpretAngle = 20;

    protected int[] pointerIds = new int[2];

    protected int pointerIdCount = 0;

    public RotationRecognizer() {
    }

    public RotationRecognizer(GestureListener listener) {
        super(listener);
    }

    public float getRotation() {
        return (float) WWMath.normalizeAngle180(this.rotation + this.rotationOffset);
    }

    protected void reset() {
        super.reset();
        this.rotation = 0;
        this.rotationOffset = 0;
        this.referenceAngle = 0;
        this.pointerIdCount = 0;
    }

    protected void actionDown(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        if (this.pointerIdCount < 2) {
            this.pointerIds[this.pointerIdCount++] = pointerId; // add it to the pointer ID array
            if (this.pointerIdCount == 2) {
                this.referenceAngle = this.currentTouchAngle(event);
                this.rotationOffset += this.rotation;
                this.rotation = 0;
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
                float angle = this.currentTouchAngle(event);
                float newRotation = (float) WWMath.normalizeAngle180(angle - this.referenceAngle);
                this.rotation = this.lowPassFilter(this.rotation, newRotation);
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
        this.referenceAngle = this.currentTouchAngle(event);
        this.rotation = 0;
    }

    protected boolean shouldRecognize(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return false; // require exactly two pointers to recognize the gesture
        }

        float angle = this.currentTouchAngle(event);
        float rotation = (float) WWMath.normalizeAngle180(angle - this.referenceAngle);

        return Math.abs(rotation) > this.interpretAngle;
    }

    protected float currentTouchAngle(MotionEvent event) {
        int index0 = event.findPointerIndex(this.pointerIds[0]);
        int index1 = event.findPointerIndex(this.pointerIds[1]);
        float dx = event.getX(index0) - event.getX(index1);
        float dy = event.getY(index0) - event.getY(index1);
        float rad = (float) Math.atan2(dy, dx);

        return (float) Math.toDegrees(rad);
    }
}
