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

    public final Matrix4 projection = new Matrix4();

    public final Matrix4 modelview = new Matrix4();

    public final DrawableQueue drawableQueue = new DrawableQueue();

    public final DrawableList drawableTerrain = new DrawableList();

//    public final PickedObjectList pickedObjects = new PickedObjectList();
//
//    private boolean isDone;
//
//    private Lock doneLock = new ReentrantLock();
//
//    private Condition doneCondition = this.doneLock.newCondition();

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

//    public void awaitDone() {
//        this.doneLock.lock();
//        try {
//            while (!this.isDone) {
//                this.doneCondition.await();
//            }
//        } catch (InterruptedException ignored) {
//            // silently ignore interrupted exceptions, but stop waiting
//        } finally {
//            this.doneLock.unlock();
//        }
//    }
//
//    public void signalDone() {
//        this.doneLock.lock();
//        try {
//            this.isDone = true;
//            this.doneCondition.signalAll();
//        } finally {
//            this.doneLock.unlock();
//        }
//    }

    public void recycle() {
        this.viewport.setEmpty();
        this.projection.setToIdentity();
        this.modelview.setToIdentity();
        this.drawableQueue.clearDrawables();
        this.drawableTerrain.clearDrawables();
        //this.pickedObjects.clearPickedObjects(); // TODO
        //this.isDone = false;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }
}
