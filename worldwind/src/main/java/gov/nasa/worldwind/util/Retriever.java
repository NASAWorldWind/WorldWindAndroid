/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import gov.nasa.worldwind.WorldWind;

public abstract class Retriever<K, O, V> {

    public interface Callback<K, O, V> {

        void retrievalSucceeded(Retriever<K, O, V> retriever, K key, O options, V value);

        void retrievalFailed(Retriever<K, O, V> retriever, K key, Throwable ex);

        void retrievalRejected(Retriever<K, O, V> retriever, K key);
    }

    protected final Object lock = new Object();

    protected int maxAsyncTasks;

    protected Set<K> asyncTaskSet;

    protected Pool<AsyncTask<K, O, V>> asyncTaskPool;

    public Retriever(int maxSimultaneousRetrievals) {
        this.maxAsyncTasks = maxSimultaneousRetrievals;
        this.asyncTaskSet = new HashSet<>();
        this.asyncTaskPool = new BasicPool<>();
    }

    public void retrieve(K key, O options, Callback<K, O, V> callback) {
        if (key == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Retriever", "retrieve", "missingKey"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Retriever", "retrieve", "missingCallback"));
        }

        AsyncTask<K, O, V> task = this.obtainAsyncTask(key, options, callback);
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

    protected abstract void retrieveAsync(K key, O options, Callback<K, O, V> callback);

    protected AsyncTask<K, O, V> obtainAsyncTask(K key, O options, Callback<K, O, V> callback) {
        synchronized (this.lock) {
            if (this.asyncTaskSet.size() >= this.maxAsyncTasks || this.asyncTaskSet.contains(key)) {
                return null;
            }

            this.asyncTaskSet.add(key);

            AsyncTask<K, O, V> instance = this.asyncTaskPool.acquire();
            return (instance != null ? instance : new AsyncTask<K, O, V>()).set(this, key, options, callback);
        }
    }

    protected void recycleAsyncTask(AsyncTask<K, O, V> instance) {
        synchronized (this.lock) {
            this.asyncTaskSet.remove(instance.key);
            this.asyncTaskPool.release(instance.reset());
        }
    }

    protected static class AsyncTask<K, O, V> implements Runnable {

        protected Retriever<K, O, V> retriever;

        protected K key;

        protected O options;

        protected Callback<K, O, V> callback;

        public AsyncTask<K, O, V> set(Retriever<K, O, V> retriever, K key, O options, Callback<K, O, V> callback) {
            this.retriever = retriever;
            this.key = key;
            this.options = options;
            this.callback = callback;
            return this;
        }

        public AsyncTask<K, O, V> reset() {
            this.retriever = null;
            this.key = null;
            this.options = null;
            this.callback = null;
            return this;
        }

        @Override
        public void run() {
            try {
                this.retriever.retrieveAsync(this.key, this.options, this.callback);
            } catch (Throwable ex) {
                this.callback.retrievalFailed(this.retriever, this.key, ex);
            } finally {
                this.retriever.recycleAsyncTask(this);
            }
        }
    }
}
