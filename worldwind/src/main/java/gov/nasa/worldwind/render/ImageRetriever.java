/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gov.nasa.worldwind.util.AbstractRetriever;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class ImageRetriever extends AbstractRetriever<ImageSource, Bitmap> {

    protected static final int MAX_THREAD_POOL_SIZE = 8;

    protected Resources resources;

    protected Executor executor;

    public ImageRetriever() {
        super(MAX_THREAD_POOL_SIZE * 2); // pool twice as many tasks as available threads
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources res) {
        this.resources = res;
    }

    @Override
    protected Executor executor() {
        if (this.executor == null) {

            ThreadPoolExecutor tpe = new ThreadPoolExecutor(0, MAX_THREAD_POOL_SIZE, // thread pool between 0 and max
                60, TimeUnit.SECONDS, // kept idle threads alive for at most 60 seconds
                new SynchronousQueue<Runnable>()); // queue rejects tasks when the thread pool is full

            tpe.setThreadFactory(new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r, "World Wind Image Retriever " + this.threadNumber.getAndIncrement());
                    thread.setDaemon(true); // retrieval threads do not prevent the process from terminating
                    return thread;
                }
            });

            tpe.setRejectedExecutionHandler(new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    throw new RejectedExecutionException(); // throw an exception but suppress the message to avoid string allocation
                }
            });

            this.executor = tpe;
        }

        return this.executor;
    }

    @Override
    protected void retrieveAsync(ImageSource imageSource, Callback<ImageSource, Bitmap> callback) {
        try {
            Bitmap bitmap = this.decodeImage(imageSource);

            if (bitmap != null) {
                callback.retrievalSucceeded(this, imageSource, bitmap);
            } else {
                callback.retrievalFailed(this, imageSource, null); // failed but no exception
            }
        } catch (Throwable logged) {
            callback.retrievalFailed(this, imageSource, logged); // failed with exception
        }
    }

    protected Bitmap decodeImage(ImageSource imageSource) throws IOException {
        if (imageSource.isBitmap()) {
            return imageSource.asBitmap();
        }

        if (imageSource.isResource()) {
            return this.decodeResource(imageSource.asResource());
        }

        if (imageSource.isFilePath()) {
            return this.decodeFilePath(imageSource.asFilePath());
        }

        if (imageSource.isUrl()) {
            return this.decodeUrl(imageSource.asUrl());
        }

        return this.decodeUnrecognized(imageSource);
    }

    protected Bitmap decodeResource(int id) {
        BitmapFactory.Options options = this.defaultBitmapFactoryOptions();
        return (this.resources != null) ? BitmapFactory.decodeResource(this.resources, id, options) : null;
    }

    protected Bitmap decodeFilePath(String pathName) {
        BitmapFactory.Options options = this.defaultBitmapFactoryOptions();
        return BitmapFactory.decodeFile(pathName, options);
    }

    protected Bitmap decodeUrl(String urlString) throws IOException {
        // TODO establish a file caching service for remote resources
        // TODO retry absent resources, they are currently handled but suppressed entirely after the first failure
        // TODO configurable connect and read timeouts

        InputStream stream = null;
        try {
            URLConnection conn = new URL(urlString).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(30000);

            stream = new BufferedInputStream(conn.getInputStream());

            BitmapFactory.Options options = this.defaultBitmapFactoryOptions();
            return BitmapFactory.decodeStream(stream, null, options);
        } finally {
            WWUtil.closeSilently(stream);
        }
    }

    protected Bitmap decodeUnrecognized(ImageSource imageSource) {
        Logger.log(Logger.WARN, "Unrecognized image source \'" + imageSource + "\'");
        return null;
    }

    protected BitmapFactory.Options defaultBitmapFactoryOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // suppress default image scaling; load the image in its native dimensions

        return options;
    }
}
