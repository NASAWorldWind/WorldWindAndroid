/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public class BasicPool<T> implements Pool<T> {

    protected static final int MIN_CAPACITY_INCREMENT = 12;

    protected Object[] entries;

    protected int size;

    public BasicPool() {
        this.entries = new Object[0];
    }

    @SuppressWarnings("unchecked")
    public T acquire() {
        if (this.size > 0) {
            int last = --this.size;
            T instance = (T) this.entries[last];
            this.entries[last] = null;
            return instance;
        }

        return null;
    }

    public void release(T instance) {
        // TODO reduce the pool size when excess entries may not be needed
        // TODO use a keep alive time to indicate how long to keep stale instances
        if (instance != null) {
            int capacity = this.entries.length;
            if (capacity == this.size) { // increase the pool size by the larger of 50% or the minimum increment
                int increment = Math.max(capacity >> 1, MIN_CAPACITY_INCREMENT);
                Object[] newEntries = new Object[capacity + increment];
                System.arraycopy(this.entries, 0, newEntries, 0, capacity);
                this.entries = newEntries;
            }

            this.entries[this.size++] = instance;
        }
    }
}