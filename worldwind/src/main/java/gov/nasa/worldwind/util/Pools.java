/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public class Pools {

    /**
     * Pool provides an interface for managing a pool of object instances.
     *
     * @param <T> the pooled type
     */
    public interface Pool<T> {

        /**
         * Acquires an instance from the pool. This returns null if the pool is empty.
         *
         * @return an instance from the pool, or null if the pool is empty
         */
        T acquire();

        /**
         * Releases an instance to the pool. This has no effect if the instance is null.
         *
         * @param instance the instance to release
         */
        void release(T instance);
    }

    public static <T> Pool<T> newPool() {
        return new BasicPool<>();
    }

    public static <T> Pool<T> newSynchronizedPool() {
        return new SynchronizedPool<>();
    }

    protected static class BasicPool<T> implements Pool<T> {

        protected Object[] entries;

        protected int size;

        public BasicPool() {
            this.entries = new Object[10]; // default capacity is 10
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
            if (instance != null) {
                int capacity = this.entries.length;
                if (capacity == this.size) { // increase the pool size by 50%
                    Object[] newEntries = new Object[capacity + (capacity >> 1)];
                    System.arraycopy(this.entries, 0, newEntries, 0, capacity);
                    this.entries = newEntries;
                }

                this.entries[this.size++] = instance;
            }
        }
    }

    protected static class SynchronizedPool<T> extends BasicPool<T> {

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
}
