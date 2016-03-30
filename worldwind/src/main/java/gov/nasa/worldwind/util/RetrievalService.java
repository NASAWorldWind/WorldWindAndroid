/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RetrievalService implements ThreadFactory, Thread.UncaughtExceptionHandler {

    protected final AtomicInteger threadNumber = new AtomicInteger(1);

    protected Executor executor;

    public RetrievalService() {
    }

    public boolean offer(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RetrievalService", "offer", "missingRunnable"));
        }

        try {
            this.executor().execute(runnable);
            return true;
        } catch (RejectedExecutionException ignored) { // log a message but suppress the stack trace
            Logger.log(Logger.INFO, "World Wind retrieval service rejected task \'" + runnable.toString() + "\'");
            return false;
        }
    }

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread thread = new Thread(runnable, "World Wind Retriever " + this.threadNumber.getAndIncrement());
        thread.setDaemon(true); // retrieval thread's do not prevent the process from terminating
        thread.setUncaughtExceptionHandler(this);
        return thread;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        Logger.log(Logger.WARN, "Uncaught exception during retrieval task execution \'" + thread.getName() + "\'", e);
    }

    protected Executor executor() {
        if (this.executor == null) {
            this.executor = new ThreadPoolExecutor(
                0, 8, // use between 0 and 8 threads
                60, TimeUnit.SECONDS, // kept idle threads alive for at most 60 seconds
                new SynchronousQueue<Runnable>(), // queue rejects tasks when the thread pool is full
                this); // use this as the ThreadFactory
        }

        return this.executor;
    }
}
