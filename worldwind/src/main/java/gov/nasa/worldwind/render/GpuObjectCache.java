/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.graphics.Bitmap;

import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Retriever;

public class GpuObjectCache extends LruMemoryCache<Object, GpuObject> {

    protected Resources resources;

    protected Queue<Entry<Object, GpuObject>> evictionQueue = new ConcurrentLinkedQueue<>();

    protected Queue<Entry<Object, GpuObject>> pendingQueue = new ConcurrentLinkedQueue<>();

    protected Retriever<ImageSource, Bitmap> imageRetriever = new ImageRetriever();

    protected Retriever.Callback<ImageSource, Bitmap> imageRetrieverCallback = new Retriever.Callback<ImageSource, Bitmap>() {

        @Override
        public void retrievalSucceeded(Retriever<ImageSource, Bitmap> retriever, ImageSource key, Bitmap value) {
            if (Logger.isLoggable(Logger.DEBUG)) {
                Logger.log(Logger.DEBUG, "Image retrieval succeeded \'" + key + "\'");
            }

            GpuTexture texture = new GpuTexture(value);
            Entry<Object, GpuObject> entry = new Entry<Object, GpuObject>(key, texture, texture.getImageByteCount());
            pendingQueue.offer(entry);
            WorldWind.requestRender();
        }

        @Override
        public void retrievalFailed(Retriever<ImageSource, Bitmap> retriever, ImageSource key, Throwable ex) {
            if (ex instanceof SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
                Logger.log(Logger.ERROR, "Socket timeout retrieving image \'" + key + "\'");
            } else if (ex != null) { // log checked exceptions with the entire stack trace
                Logger.log(Logger.ERROR, "Image retrieval failed with exception \'" + key + "\'");
            } else {
                Logger.log(Logger.ERROR, "Image retrieval failed \'" + key + "\'");
            }
        }

        @Override
        public void retrievalRejected(Retriever<ImageSource, Bitmap> retriever, ImageSource key) {
            if (Logger.isLoggable(Logger.DEBUG)) {
                Logger.log(Logger.DEBUG, "Image retrieval rejected \'" + key + "\'");
            }
        }
    };

    public GpuObjectCache(int capacity) {
        super(capacity);
    }

    public GpuObjectCache(int capacity, int lowWater) {
        super(capacity, lowWater);
    }

    public Resources getResources() {
        return this.resources;
    }

    public void setResources(Resources res) {
        this.resources = res;
        ((ImageRetriever) this.imageRetriever).setResources(res);
    }

    public void contextLost(DrawContext dc) {
        // TODO requires synchronization between the Activity thread and the OpenGL thread (eventually)
        // TODO the context lost message is sent to the OpenGL thread, but can this work be done on the Activity thread
        this.entries.clear(); // the cache entries are invalid; clear but don't call entryRemoved
        this.evictionQueue.clear(); // the eviction queue no longer needs to be processed
        this.pendingQueue.clear();
        this.usedCapacity = 0;
    }

    public void releaseEvictedResources(DrawContext dc) {
        Entry<Object, GpuObject> evicted;
        while ((evicted = this.evictionQueue.poll()) != null) {
            try {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Released render resource \'" + evicted.key + "\'");
                }
                evicted.value.dispose(dc); // TODO rename as dispose as release
            } catch (Exception ignored) {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Exception releasing render resource \'" + evicted.key + "\'", ignored);
                }
            }
        }
    }

    @Override
    protected void entryRemoved(Entry<Object, GpuObject> entry) {
        if (entry != null) {
            this.evictionQueue.offer(entry);
        }
    }

    public GpuTexture retrieveTexture(ImageSource imageSource) {
        if (imageSource == null) {
            return null;
        }

        if (imageSource.isBitmap()) {
            GpuTexture texture = new GpuTexture(imageSource.asBitmap());
            return (GpuTexture) this.put(imageSource, texture, texture.getImageByteCount());
        }

        GpuTexture texture = (GpuTexture) this.processPendingQueue(imageSource);
        if (texture != null) {
            return texture;
        }

        this.imageRetriever.retrieve(imageSource, this.imageRetrieverCallback); // adds entries to pendingQueue

        return null;
    }

    protected GpuObject processPendingQueue(Object key) {
        Entry<Object, GpuObject> match = null;
        Entry<Object, GpuObject> pending;
        while ((pending = this.pendingQueue.poll()) != null) {
            if (match == null && pending.key.equals(key)) {
                match = pending;
            }
            this.putEntry(pending);
        }

        return (match != null) ? match.value : null;
    }
}
