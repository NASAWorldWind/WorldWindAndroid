/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

public class NavigatorEventSupport {

    protected WorldWindow wwd;

    protected List<NavigatorListener> listeners = new ArrayList<>();

    protected long stoppedEventDelay = 250;

    protected Matrix4 lastModelview;

    protected MotionEvent lastTouchEvent;

    protected MotionEvent stopTouchEvent;

    protected Handler stopHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            onNavigatorStopped();
            return false;
        }
    });

    public NavigatorEventSupport(WorldWindow wwd) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "NavigatorEventSupport", "constructor", "missingWorldWindow"));
        }

        this.wwd = wwd;
    }

    public void reset() {
        this.lastModelview = null;
        this.stopHandler.removeMessages(0 /*what*/);

        if (this.lastTouchEvent != null) {
            this.lastTouchEvent.recycle();
            this.lastTouchEvent = null;
        }

        if (this.stopTouchEvent != null) {
            this.stopTouchEvent.recycle();
            this.stopTouchEvent = null;
        }
    }

    public void addNavigatorListener(NavigatorListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "NavigatorEventSupport", "addNavigatorListener", "missingListener"));
        }

        this.listeners.add(listener);
    }

    public void removeNavigatorListener(NavigatorListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "NavigatorEventSupport", "removeNavigatorListener", "missingListener"));
        }

        this.listeners.remove(listener);
    }

    public long getNavigatorStoppedDelay() {
        return this.stoppedEventDelay;
    }

    public void setNavigatorStoppedDelay(long delay, TimeUnit unit) {
        this.stoppedEventDelay = unit.toMillis(delay);
    }

    public void onTouchEvent(MotionEvent event) {
        if (this.listeners.isEmpty()) {
            return; // no listeners to notify; ignore the event
        }

        if (this.lastModelview == null) {
            return; // no frame rendered yet; ignore the event
        }

        if (this.lastTouchEvent != null) {
            this.lastTouchEvent.recycle();
        }
        this.lastTouchEvent = MotionEvent.obtain(event);
    }

    public void onFrameRendered(RenderContext rc) {
        if (this.listeners.isEmpty()) {
            return; // no listeners to notify; ignore the event
        }

        if (this.lastModelview == null) { // this is the first frame; copy the frame's modelview
            this.lastModelview = new Matrix4(rc.modelview);
        } else if (!this.lastModelview.equals(rc.modelview)) { // the frame's modelview has changed
            this.lastModelview.set(rc.modelview);
            // Notify the listeners of a navigator moved event.
            this.onNavigatorMoved();
            // Schedule a navigator stopped event after a specified delay in milliseconds.
            this.stopHandler.removeMessages(0 /*what*/);
            this.stopHandler.sendEmptyMessageDelayed(0 /*what*/, this.stoppedEventDelay);
        }
    }

    protected void onNavigatorMoved() {
        this.notifyListeners(WorldWind.NAVIGATOR_MOVED, this.lastTouchEvent);

        if (this.lastTouchEvent != null) {
            if (this.stopTouchEvent != null) {
                this.stopTouchEvent.recycle();
            }
            this.stopTouchEvent = this.lastTouchEvent;
            this.lastTouchEvent = null;
        }
    }

    protected void onNavigatorStopped() {
        this.notifyListeners(WorldWind.NAVIGATOR_STOPPED, this.stopTouchEvent);

        if (this.stopTouchEvent != null) {
            this.stopTouchEvent.recycle();
            this.stopTouchEvent = null;
        }
    }

    protected void notifyListeners(int action, InputEvent inputEvent) {
        NavigatorEvent event = NavigatorEvent.obtain(this.wwd.navigator, action, inputEvent);
        for (int idx = 0, len = this.listeners.size(); idx < len; idx++) {
            this.listeners.get(idx).onNavigatorEvent(this.wwd, event);
        }
        event.recycle();
    }
}
