/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public class SynchronizedPool<T> extends BasicPool<T> {

    protected final Object lock = new Object();

    public SynchronizedPool() {
    }

    @Override
    public T acquire() {
        synchronized (this.lock) {
            return super.acquire();
        }
    }

    @Override
    public void release(T instance) {
        synchronized (this.lock) {
            super.release(instance);
        }
    }
}