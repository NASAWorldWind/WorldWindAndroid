/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public class SynchronizedMemoryCache<K, V> extends LruMemoryCache<K, V> {

    protected final Object lock = new Object();

    public SynchronizedMemoryCache(int capacity) {
        super(capacity);
    }

    public SynchronizedMemoryCache(int capacity, int lowWater) {
        super(capacity, lowWater);
    }

    @Override
    public int getCapacity() {
        synchronized (this.lock) {
            return super.getCapacity();
        }
    }

    @Override
    public int getUsedCapacity() {
        synchronized (this.lock) {
            return super.getUsedCapacity();
        }
    }

    @Override
    public int getEntryCount() {
        synchronized (this.lock) {
            return super.getEntryCount();
        }
    }

    @Override
    public V get(K key) {
        synchronized (this.lock) {
            return super.get(key);
        }
    }

    @Override
    public V put(K key, V value, int size) {
        synchronized (this.lock) {
            return super.put(key, value, size);
        }
    }

    @Override
    public V remove(K key) {
        synchronized (this.lock) {
            return super.remove(key);
        }
    }

    @Override
    public int trimToAge(long timeMillis) {
        synchronized (this.lock) {
            return super.trimToAge(timeMillis);
        }
    }

    @Override
    public boolean containsKey(K key) {
        synchronized (this.lock) {
            return super.containsKey(key);
        }
    }

    @Override
    public void clear() {
        synchronized (this.lock) {
            super.clear();
        }
    }
}
