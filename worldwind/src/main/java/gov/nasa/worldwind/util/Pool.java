/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

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
