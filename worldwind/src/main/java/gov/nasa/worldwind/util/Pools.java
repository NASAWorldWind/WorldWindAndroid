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

    public static <T> Pool<T> newPool(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Pools", "newBasicPool", "invalidCapacity"));
        }

        return new BasicPool<>(initialCapacity);
    }

    public static <T> Pool<T> newSynchronizedPool(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Pools", "newSynchronizedPool", "invalidCapacity"));
        }

        return new SynchronizedPool<>(initialCapacity);
    }

    protected static class BasicPool<T> implements Pool<T> {

        protected Object[] pool;

        protected int remaining;

        public BasicPool(int initialCapacity) {
            if (initialCapacity < 1) {
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Pools.BasicPool", "constructor", "invalidCapacity"));
            }

            this.pool = new Object[initialCapacity];
        }

        @SuppressWarnings("unchecked")
        public T acquire() {
            if (this.remaining > 0) {
                int last = --this.remaining;
                T instance = (T) this.pool[last];
                this.pool[last] = null;
                return instance;
            }

            return null;
        }

        public void release(T instance) {
            // TODO reduce the pool size when excess entries may not be needed
            if (instance != null) {
                int capacity = this.pool.length;
                if (capacity <= this.remaining) {
                    Object[] newPool = new Object[capacity + (capacity >> 1)];
                    System.arraycopy(this.pool, 0, newPool, 0, capacity);
                    this.pool = newPool;
                }

                this.pool[this.remaining++] = instance;
            }
        }
    }

    protected static class SynchronizedPool<T> extends BasicPool<T> {

        protected final Object lock = new Object();

        public SynchronizedPool(int initialCapacity) {
            super(initialCapacity);
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
