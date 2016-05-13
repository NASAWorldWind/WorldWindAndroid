/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskService {

    protected ExecutorService executorService;

    public void execute(Runnable command) {
        if (command == null) {
            return;
        }

        this.executorService().execute(command);
    }

    protected ExecutorService executorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                this.threadFactory(),
                this.rejectedExecutionHandler());
        }

        return this.executorService;
    }

    protected ThreadFactory threadFactory() {
        final String threadName = "World Wind Task Service ";
        final AtomicInteger threadNumber = new AtomicInteger(1);

        return new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r, threadName + threadNumber.getAndIncrement());
                thread.setDaemon(true); // task threads do not prevent the process from terminating
                return thread;
            }
        };
    }

    protected RejectedExecutionHandler rejectedExecutionHandler() {
        return new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                throw new RejectedExecutionException(); // throw an exception but suppress the message to avoid string allocation
            }
        };
    }
}
