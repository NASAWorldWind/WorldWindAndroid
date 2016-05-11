/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.util.BasicPool;
import gov.nasa.worldwind.util.Pool;

public class NavigatorEvent {

    private static Pool<NavigatorEvent> pool = new BasicPool<>();

    protected Navigator navigator;

    @WorldWind.NavigatorEventType
    protected int type;

    public static NavigatorEvent obtain() {
        NavigatorEvent instance = pool.acquire();
        return (instance != null) ? instance : new NavigatorEvent();
    }

    public static NavigatorEvent obtain(Navigator navigator, @WorldWind.NavigatorEventType int type) {
        NavigatorEvent instance = obtain();
        instance.navigator = navigator;
        instance.type = type;
        return instance;
    }

    public Navigator getNavigator() {
        return this.navigator;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @WorldWind.NavigatorEventType
    public int getType() {
        return this.type;
    }

    public void setType(@WorldWind.NavigatorEventType int type) {
        this.type = type;
    }

    /**
     * Recycle the event, making it available to be re-used.
     */
    public void recycle() {
        pool.release(this);
    }
}
