/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gov.nasa.worldwind.draw.DrawableList;
import gov.nasa.worldwind.draw.DrawableQueue;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Viewport;
import gov.nasa.worldwind.util.Pool;

public class Frame {

    public final Viewport viewport = new Viewport();

    public final Matrix4 projection = new Matrix4();

    public final Matrix4 modelview = new Matrix4();

    public final Matrix4 infiniteProjection = new Matrix4();

    public final DrawableQueue drawableQueue = new DrawableQueue();

    public final DrawableQueue drawableTerrain = new DrawableQueue();

    public PickedObjectList pickedObjects;

    public Viewport pickViewport;

    public Vec2 pickPoint;

    public Line pickRay;

    public boolean pickMode;

    private boolean isDone;

    private boolean isAwaitingDone;

    private final Lock doneLock = new ReentrantLock();

    private final Condition doneCondition = this.doneLock.newCondition();

    private Pool<Frame> pool;

    public Frame() {
    }

    public static Frame obtain(Pool<Frame> pool) {
        Frame instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.init(pool) : new Frame().init(pool);
    }

    private Frame init(Pool<Frame> pool) {
        this.pool = pool;
        this.isDone = false;
        this.isAwaitingDone = false;
        return this;
    }

    public void recycle() {
        this.viewport.setEmpty();
        this.projection.setToIdentity();
        this.modelview.setToIdentity();
        this.drawableQueue.clearDrawables();
        this.drawableTerrain.clearDrawables();
        this.pickedObjects = null;
        this.pickViewport = null;
        this.pickPoint = null;
        this.pickRay = null;
        this.pickMode = false;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    public void awaitDone() {
        this.doneLock.lock();
        try {
            while (!this.isDone) {
                this.isAwaitingDone = true;
                this.doneCondition.await();
            }
        } catch (InterruptedException ignored) {
            // stop waiting, but suppress any exception logging
        } finally {
            this.doneLock.unlock();
        }
    }

    public void signalDone() {
        this.doneLock.lock();
        try {
            this.isDone = true;
            if (this.isAwaitingDone) {
                this.doneCondition.signal();
            }
        } finally {
            this.doneLock.unlock();
        }
    }
}
