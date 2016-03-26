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

public class TaskService implements Executor, ThreadFactory, Thread.UncaughtExceptionHandler {

    protected final AtomicInteger threadNumber = new AtomicInteger(1);

    protected Executor executor;

    public TaskService() {
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        try {
            this.executor().execute(runnable);
        } catch (RejectedExecutionException ignored) { // log a message but suppress the stack trace
            Logger.log(Logger.INFO, "World Wind executor rejected task \'" + runnable.toString() + "\'");
        }
    }

    public boolean offer(@NonNull Runnable runnable) {
        try {
            this.executor().execute(runnable);
            return true;
        } catch (RejectedExecutionException ignored) { // log a message but suppress the stack trace
            Logger.log(Logger.INFO, "World Wind executor rejected task \'" + runnable.toString() + "\'");
            return false;
        }
    }

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread thread = new Thread(runnable, "World Wind Task " + this.threadNumber.getAndIncrement());
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(this);
        return thread;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        Logger.log(Logger.WARN, "Uncaught exception during execution \'" + thread.getName() + "\'", e);
    }

    protected Executor executor() {
        if (this.executor == null) {
            this.executor = new ThreadPoolExecutor(0, 8, 60, TimeUnit.SECONDS, // max 8 threads, kept alive for at most 60 seconds
                new SynchronousQueue<Runnable>(), this); // use this as the ThreadFactory
        }

        return this.executor;
    }
}
