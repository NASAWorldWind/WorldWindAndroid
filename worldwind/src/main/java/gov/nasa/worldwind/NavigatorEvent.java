/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.view.InputEvent;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.util.BasicPool;
import gov.nasa.worldwind.util.Pool;

public class NavigatorEvent {

    private static Pool<NavigatorEvent> pool = new BasicPool<>();

    protected Camera camera;

    @WorldWind.NavigatorAction
    protected int action = WorldWind.NAVIGATOR_MOVED;

    protected InputEvent lastInputEvent;

    protected NavigatorEvent() {
    }

    public static NavigatorEvent obtain(Camera camera, @WorldWind.NavigatorAction int action, InputEvent lastInputEvent) {
        NavigatorEvent instance = pool.acquire();
        if (instance == null) {
            instance = new NavigatorEvent();
        }

        instance.camera = camera;
        instance.action = action;
        instance.lastInputEvent = lastInputEvent;

        return instance;
    }

    /**
     * Recycle the event, making it available for re-use.
     */
    public void recycle() {
        this.camera = null;
        this.action = WorldWind.NAVIGATOR_MOVED;
        this.lastInputEvent = null;
        pool.release(this);
    }

    public Camera getCamera() {
        return this.camera;
    }

    @WorldWind.NavigatorAction
    public int getAction() {
        return this.action;
    }

    public InputEvent getLastInputEvent() {
        return this.lastInputEvent;
    }
}
