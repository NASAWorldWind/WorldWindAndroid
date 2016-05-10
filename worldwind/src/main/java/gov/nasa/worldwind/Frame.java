/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.graphics.Rect;

import gov.nasa.worldwind.draw.DrawableList;
import gov.nasa.worldwind.draw.DrawableQueue;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.util.Pool;

public class Frame {

    public final Rect viewport = new Rect();

    public final Matrix4 modelview = new Matrix4();

    public final Matrix4 projection = new Matrix4();

    public final DrawableQueue drawableQueue = new DrawableQueue();

    public final DrawableList drawableTerrain = new DrawableList();

    private Pool<Frame> pool;

    public Frame() {
    }

    public static Frame obtain(Pool<Frame> pool) {
        Frame instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new Frame().setPool(pool);
    }

    private Frame setPool(Pool<Frame> pool) {
        this.pool = pool;
        return this;
    }

    public void recycle() {
        this.viewport.setEmpty();
        this.modelview.setToIdentity();
        this.projection.setToIdentity();
        this.drawableQueue.clearDrawables();
        this.drawableTerrain.clearDrawables();

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }
}
