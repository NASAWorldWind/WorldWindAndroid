/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.support.v4.util.Pools;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import gov.nasa.worldwind.WorldWind;

public abstract class AbstractRetriever<K, V> implements Retriever<K, V> {

    protected final Object lock = new Object();

    protected int maxAsyncTasks;

    protected Set<K> asyncTaskSet;

    protected Pools.Pool<AsyncTask<K, V>> asyncTaskPool;

    public AbstractRetriever(int maxSimultaneousRetrievals) {
        this.maxAsyncTasks = maxSimultaneousRetrievals;
        this.asyncTaskSet = new HashSet<>();
        this.asyncTaskPool = new Pools.SimplePool<>(maxSimultaneousRetrievals);
    }

    @Override
    public void retrieve(K key, Callback<K, V> callback) {
        if (key == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AbstractRetriever", "retrieve", "missingKey"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AbstractRetriever", "retrieve", "missingCallback"));
        }

        AsyncTask<K, V> task = this.obtainAsyncTask(key, callback);
        if (task == null) { // too many async tasks running, or a task for 'key' is already running
            callback.retrievalRejected(this, key);
            return;
        }

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException ignored) { // singleton task service is full
            this.recycleAsyncTask(task);
            callback.retrievalRejected(this, key);
        }
    }

    protected abstract void retrieveAsync(K key, Callback<K, V> callback);

    protected AsyncTask<K, V> obtainAsyncTask(K key, Callback<K, V> callback) {
        synchronized (this.lock) {
            if (this.asyncTaskSet.size() >= this.maxAsyncTasks || this.asyncTaskSet.contains(key)) {
                return null;
            }

            this.asyncTaskSet.add(key);

            AsyncTask<K, V> instance = this.asyncTaskPool.acquire();
            return (instance != null ? instance : new AsyncTask<K, V>()).set(this, key, callback);
        }
    }

    protected void recycleAsyncTask(AsyncTask<K, V> instance) {
        synchronized (this.lock) {
            this.asyncTaskSet.remove(instance.key);
            this.asyncTaskPool.release(instance.reset());
        }
    }

    protected static class AsyncTask<K, V> implements Runnable {

        protected AbstractRetriever<K, V> retriever;

        protected K key;

        protected Callback<K, V> callback;

        public AsyncTask<K, V> set(AbstractRetriever<K, V> retriever, K key, Callback<K, V> callback) {
            this.retriever = retriever;
            this.key = key;
            this.callback = callback;
            return this;
        }

        public AsyncTask<K, V> reset() {
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
                this.retriever.recycleAsyncTask(this);
            }
        }
    }
}
