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
import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Retriever;

public class RenderResourceCache extends LruMemoryCache<Object, RenderResource> {

    protected Resources resources;

    protected Queue<Entry<Object, RenderResource>> evictionQueue = new ConcurrentLinkedQueue<>();

    protected Queue<Entry<Object, RenderResource>> retrievalQueue = new ConcurrentLinkedQueue<>();

    protected Retriever<ImageSource, Bitmap> imageRetriever = new ImageRetriever();

    protected Retriever.Callback<ImageSource, Bitmap> imageRetrieverCallback = new Retriever.Callback<ImageSource, Bitmap>() {

        @Override
        public void retrievalSucceeded(Retriever<ImageSource, Bitmap> retriever, ImageSource key, Bitmap value) {
            Texture texture = new Texture(value);
            Entry<Object, RenderResource> entry = new Entry<Object, RenderResource>(key, texture, texture.getTextureByteCount());
            retrievalQueue.offer(entry);
            WorldWind.requestRedraw();

            if (Logger.isLoggable(Logger.DEBUG)) {
                Logger.log(Logger.DEBUG, "Image retrieval succeeded \'" + key + "\'");
            }
        }

        @Override
        public void retrievalFailed(Retriever<ImageSource, Bitmap> retriever, ImageSource key, Throwable ex) {
            if (ex instanceof SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
                Logger.log(Logger.ERROR, "Socket timeout retrieving image \'" + key + "\'");
            } else if (ex != null) { // log checked exceptions with the entire stack trace
                Logger.log(Logger.ERROR, "Image retrieval failed with exception \'" + key + "\'", ex);
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

    public RenderResourceCache(int capacity) {
        super(capacity);
    }

    public RenderResourceCache(int capacity, int lowWater) {
        super(capacity, lowWater);
    }

    public Resources getResources() {
        return this.resources;
    }

    public void setResources(Resources res) {
        this.resources = res;
        ((ImageRetriever) this.imageRetriever).setResources(res);
    }

    public void clear() {
        this.entries.clear(); // the cache entries are invalid; clear but don't call entryRemoved
        this.evictionQueue.clear(); // the eviction queue no longer needs to be processed
        this.usedCapacity = 0;
    }

    public void releaseEvictedResources(DrawContext dc) {
        Entry<Object, RenderResource> evicted;
        while ((evicted = this.evictionQueue.poll()) != null) {
            try {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Released render resource \'" + evicted.key + "\'");
                }
                evicted.value.release(dc);
            } catch (Exception ignored) {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Exception releasing render resource \'" + evicted.key + "\'", ignored);
                }
            }
        }
    }

    @Override
    protected void entryRemoved(Entry<Object, RenderResource> entry) {
        if (entry != null) {
            this.evictionQueue.offer(entry);
        }
    }

    @Override
    protected void entryReplaced(Entry<Object, RenderResource> oldEntry, Entry<Object, RenderResource> newEntry) {
        if (oldEntry != null) {
            this.evictionQueue.offer(oldEntry);
        }
    }

    public Texture retrieveTexture(ImageSource imageSource) {
        if (imageSource == null) {
            return null;
        }

        if (imageSource.isBitmap()) {
            Texture texture = new Texture(imageSource.asBitmap());
            this.put(imageSource, texture, texture.getTextureByteCount());
            return texture;
        }

        Texture texture = (Texture) this.putRetrievedResources(imageSource);
        if (texture != null) {
            return texture;
        }

        this.imageRetriever.retrieve(imageSource, this.imageRetrieverCallback); // adds entries to retrievalQueue
        return null;
    }

    protected RenderResource putRetrievedResources(Object key) {
        Entry<Object, RenderResource> match = null;
        Entry<Object, RenderResource> pending;
        while ((pending = this.retrievalQueue.poll()) != null) {
            if (match == null && pending.key.equals(key)) {
                match = pending;
            }
            this.putEntry(pending);
        }

        return (match != null) ? match.value : null;
    }
}
