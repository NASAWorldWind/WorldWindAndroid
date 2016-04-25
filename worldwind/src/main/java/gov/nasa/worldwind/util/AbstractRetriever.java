/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.support.v4.util.Pools;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

public abstract class AbstractRetriever<K, V> implements Retriever<K, V> {

    protected final Object lock = new Object();

    protected Pools.Pool<RetrieverTask<K, V>> retrievalTaskPool;

    protected Set<K> retrievalSet;

    public AbstractRetriever(int maxTaskPoolSize) {
        this.retrievalTaskPool = new Pools.SimplePool<>(maxTaskPoolSize);
        this.retrievalSet = new HashSet<>();
    }

    @Override
    public void retrieve(K key, Callback<K, V> callback) {
        if (key == null || this.hasTask(key)) { // suppress duplicate requests if the task was accepted
            callback.retrievalRejected(this, key);
            return;
        }

        RetrieverTask<K, V> task = this.obtainTask(key, callback);

        try {
            this.executor().execute(task);
        } catch (RejectedExecutionException ignored) {
            this.recycleTask(task);
            callback.retrievalRejected(this, key);
        }
    }

    protected abstract Executor executor();

    protected abstract void retrieveAsync(K key, Callback<K, V> callback);

    protected boolean hasTask(K key) {
        synchronized (this.lock) {
            return this.retrievalSet.contains(key);
        }
    }

    protected RetrieverTask<K, V> obtainTask(K key, Callback<K, V> callback) {
        synchronized (this.lock) {
            RetrieverTask<K, V> instance = this.retrievalTaskPool.acquire();
            if (instance == null) {
                instance = new RetrieverTask<>();
            }

            this.retrievalSet.add(key);
            return instance.set(this, key, callback);
        }
    }

    protected void recycleTask(RetrieverTask<K, V> instance) {
        synchronized (this.lock) {
            this.retrievalSet.remove(instance.key);
            this.retrievalTaskPool.release(instance.reset());
        }
    }

    protected static class RetrieverTask<K, V> implements Runnable {

        protected AbstractRetriever<K, V> retriever;

        protected K key;

        protected Callback<K, V> callback;

        public RetrieverTask<K, V> set(AbstractRetriever<K, V> retriever, K key, Callback<K, V> callback) {
            this.retriever = retriever;
            this.key = key;
            this.callback = callback;
            return this;
        }

        public RetrieverTask<K, V> reset() {
            this.retriever = null;
            this.key = null;
            this.callback = null;
            return this;
        }

        @Override
        public void run() {
            try {
                this.retriever.retrieveAsync(this.key, this.callback);
            } catch (Throwable ex) {
                this.callback.retrievalFailed(this.retriever, this.key, ex);
            } finally {
                this.retriever.recycleTask(this);
            }
        }
    }
}
