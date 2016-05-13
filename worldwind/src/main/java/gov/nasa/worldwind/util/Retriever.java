/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public interface Retriever<K, V> {

    void retrieve(K key, Callback<K, V> callback);

    interface Callback<K, V> {

        void retrievalSucceeded(Retriever<K, V> retriever, K key, V value);

        void retrievalFailed(Retriever<K, V> retriever, K key, Throwable ex);

        void retrievalRejected(Retriever<K, V> retriever, K key);
    }
}
