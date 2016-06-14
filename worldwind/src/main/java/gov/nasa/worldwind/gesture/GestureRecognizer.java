/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

public class GestureRecognizer {

    protected boolean enabled = true;

    protected float x;

    protected float y;

    protected float startX;

    protected float startY;

    protected float translationX;

    protected float translationY;

    protected float centroidShiftX;

    protected float centroidShiftY;

    protected float[] centroidArray = new float[2];

    protected List<GestureListener> listenerList = new ArrayList<>();

    protected float lowPassWeight = 0.4f;

    @WorldWind.GestureState
    private int state = WorldWind.POSSIBLE;

    private long stateSequence;

    public GestureRecognizer() {
    }

    public GestureRecognizer(GestureListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GestureRecognizer", "constructor", "missingListener"));
        }

        this.listenerList.add(listener);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @WorldWind.GestureState
    public int getState() {
        return state;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getTranslationX() {
        return this.translationX;
    }

    public void setTranslationX(float x) {
        this.translationX = x;
        this.startX = this.x;
        this.centroidShiftX = 0;
    }

    public float getTranslationY() {
        return this.translationY;
    }

    public void setTranslationY(float y) {
        this.translationY = y;
        this.startY = this.y;
        this.centroidShiftY = 0;
    }

    public void addListener(GestureListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GestureRecognizer", "addListener", "missingListener"));
        }

        this.listenerList.add(listener);
    }

    public void removeListener(GestureListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GestureRecognizer", "removeListener", "missingListener"));
        }

        this.listenerList.remove(listener);
    }

    public List<GestureListener> getListeners() {
        return this.listenerList;
    }

    protected void notifyListeners(MotionEvent event) {
        for (GestureListener listener : this.listenerList) {
            listener.gestureStateChanged(event, this);
        }
    }

    protected void reset() {
        this.state = WorldWind.POSSIBLE;
        this.stateSequence = 0;
        this.x = 0;
        this.y = 0;
        this.startX = 0;
        this.startY = 0;
        this.translationX = 0;
        this.translationY = 0;
        this.centroidShiftX = 0;
        this.centroidShiftY = 0;
    }

    protected void transitionToState(MotionEvent event, @WorldWind.GestureState int newState) {
        switch (newState) {
            case WorldWind.POSSIBLE:
                this.state = newState;
                break;
            case WorldWind.FAILED:
                this.state = newState;
                break;
            case WorldWind.RECOGNIZED:
                this.state = newState;
                this.stateSequence++;
                this.prepareToRecognize(event);
                this.notifyListeners(event);
                break;
            case WorldWind.BEGAN:
                this.state = newState;
                this.stateSequence++;
                this.prepareToRecognize(event);
                this.notifyListeners(event);
                break;
            case WorldWind.CHANGED:
                this.state = newState;
                this.stateSequence++;
                this.notifyListeners(event);
                break;
            case WorldWind.CANCELLED:
                this.state = newState;
                this.stateSequence++;
                this.notifyListeners(event);
                break;
            case WorldWind.ENDED:
                this.state = newState;
                this.stateSequence++;
                this.notifyListeners(event);
                break;
        }
    }

    protected void prepareToRecognize(MotionEvent event) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.enabled) {
            return false;
        }

        long currentStateSequence = this.stateSequence;

        try {

            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN: // first pointer down
                    this.handleActionDown(event);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN: // subsequent pointer down
                    this.handleActionPointerDown(event);
                    break;
                case MotionEvent.ACTION_MOVE: // one or more pointers moved
                    this.handleActionMove(event);
                    break;
                case MotionEvent.ACTION_CANCEL: // all pointers cancelled
                    this.handleActionCancel(event);
                    break;
                case MotionEvent.ACTION_POINTER_UP: // subsequent pointer up
                    this.handleActionPointerUp(event);
                    break;
                case MotionEvent.ACTION_UP: // last pointer up
                    this.handleActionUp(event);
                    break;
                default:
                    if (Logger.isLoggable(Logger.DEBUG)) {
                        Logger.logMessage(Logger.DEBUG, "GestureRecognizer", "onTouchEvent",
                            "Unrecognized event action \'" + action + "\'");
                    }
                    break;
            }

        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "GestureRecognizer", "onTouchEvent", "Exception handling event", e);
        }

        return currentStateSequence != this.stateSequence; // stateSequence changes if the event was recognized
    }

    protected void handleActionDown(MotionEvent event) {
        int index = event.getActionIndex();
        this.x = event.getX(index);
        this.y = event.getY(index);
        this.startX = this.x;
        this.startY = this.y;
        this.translationX = 0;
        this.translationY = 0;
        this.centroidShiftX = 0;
        this.centroidShiftY = 0;

        this.actionDown(event);
    }

    protected void handleActionPointerDown(MotionEvent event) {
        this.centroidChanged(event);
        this.actionDown(event);
    }

    protected void handleActionMove(MotionEvent event) {
        this.eventCentroid(event, this.centroidArray);
        float dx = this.centroidArray[0] - this.startX + this.centroidShiftX;
        float dy = this.centroidArray[1] - this.startY + this.centroidShiftY;
        this.x = this.centroidArray[0];
        this.y = this.centroidArray[1];
        this.translationX = this.lowPassFilter(this.translationX, dx);
        this.translationY = this.lowPassFilter(this.translationY, dy);

        this.actionMove(event);
    }

    protected void handleActionCancel(MotionEvent event) {
        this.actionCancel(event);

        int state = this.getState();
        if (state == WorldWind.POSSIBLE) {
            this.transitionToState(event, WorldWind.FAILED);
        } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
            this.transitionToState(event, WorldWind.CANCELLED);
        }

        this.reset();
    }

    protected void handleActionPointerUp(MotionEvent event) {
        this.centroidChanged(event);
        this.actionUp(event);
    }

    protected void handleActionUp(MotionEvent event) {
        this.actionUp(event);

        int state = this.getState();
        if (state == WorldWind.POSSIBLE) {
            this.transitionToState(event, WorldWind.FAILED);
        } else if (state == WorldWind.BEGAN || state == WorldWind.CHANGED) {
            this.transitionToState(event, WorldWind.ENDED);
        }

        this.reset();
    }

    protected void centroidChanged(MotionEvent event) {
        this.centroidShiftX += this.x;
        this.centroidShiftY += this.y;
        this.eventCentroid(event, this.centroidArray);
        this.x = this.centroidArray[0];
        this.y = this.centroidArray[1];
        this.centroidShiftX -= this.centroidArray[0];
        this.centroidShiftY -= this.centroidArray[1];
    }

    protected float[] eventCentroid(MotionEvent event, float[] result) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();

        float x = 0;
        float y = 0;
        float count = 0;

        for (int i = 0, len = event.getPointerCount(); i < len; i++) {
            if (i == index && action == MotionEvent.ACTION_POINTER_UP) {
                continue; // suppress coordinates from pointers that are no longer down
            }

            x += event.getX(i);
            y += event.getY(i);
            count++;
        }

        result[0] = x / count;
        result[1] = y / count;
        return result;
    }

    protected float lowPassFilter(float value, float newValue) {
        float w = this.lowPassWeight;
        return value * (1 - w) + newValue * w;
    }

    protected void actionDown(MotionEvent event) {
    }

    protected void actionMove(MotionEvent event) {
    }

    protected void actionCancel(MotionEvent event) {

    }

    protected void actionUp(MotionEvent event) {
    }
}
