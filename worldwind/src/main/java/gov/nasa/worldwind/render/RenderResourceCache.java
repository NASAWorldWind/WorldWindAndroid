/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;

import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.SynchronizedMemoryCache;

public class RenderResourceCache extends LruMemoryCache<Object, RenderResource>
    implements Retriever.Callback<ImageSource, ImageOptions, Bitmap>, Handler.Callback {

    protected Resources resources;

    protected Handler handler;

    protected Queue<RenderResource> evictionQueue;

    protected Retriever<ImageSource, ImageOptions, Bitmap> imageRetriever;

    protected Retriever<ImageSource, ImageOptions, Bitmap> urlImageRetriever;

    protected LruMemoryCache<ImageSource, Bitmap> imageRetrieverCache;

    protected static final int STALE_RETRIEVAL_AGE = 3000;

    protected static final int TRIM_STALE_RETRIEVALS = 1;

    protected static final int TRIM_STALE_RETRIEVALS_DELAY = 6000;

    public RenderResourceCache(int capacity) {
        super(capacity);
        this.init();
    }

    public RenderResourceCache(int capacity, int lowWater) {
        super(capacity, lowWater);
        this.init();
    }

    protected void init() {
        this.handler = new Handler(this);
        this.evictionQueue = new ConcurrentLinkedQueue<>();
        this.imageRetriever = new ImageRetriever(2);
        this.urlImageRetriever = new ImageRetriever(8);
        this.imageRetrieverCache = new SynchronizedMemoryCache<>(this.getCapacity() / 8);

        Logger.log(Logger.INFO, String.format(Locale.US, "RenderResourceCache initialized  %,.0f KB  (%,.0f KB retrieval cache)",
            this.getCapacity() / 1024.0, this.imageRetrieverCache.getCapacity() / 1024.0));
    }

    public static int recommendedCapacity(Context context) {
        ActivityManager am = (context != null) ? (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE) : null;
        if (am != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            if (mi.totalMem >= 1024 * 1024 * 2048L) { // use 384 MB on machines with 2048 MB or more
                return 1024 * 1024 * 384;
            } else if (mi.totalMem >= 1024 * 1024 * 1536) { // use 256 MB on machines with 1536 MB or more
                return 1024 * 1024 * 256;
            } else if (mi.totalMem >= 1024 * 1024 * 1024) { // use 192 MB on machines with 1024 MB or more
                return 1024 * 1024 * 192;
            } else if (mi.totalMem >= 1024 * 1024 * 512) { // use 96 MB on machines with 512 MB or more
                return 1024 * 1024 * 96;
            } else { // use 64 MB on machines with less than 512 MB
                return 1024 * 1024 * 64;
            }
        } else { // use 64 MB by default
            return 1024 * 1024 * 64;
        }
    }

    public Resources getResources() {
        return this.resources;
    }

    public void setResources(Resources res) {
        this.resources = res;
        ((ImageRetriever) this.imageRetriever).setResources(res);
    }

    public void clear() { // TODO rename as contextLost to clarify this method's purpose for RenderResourceCache
        this.handler.removeMessages(TRIM_STALE_RETRIEVALS);
        this.entries.clear(); // the cache entries are invalid; clear but don't call entryRemoved
        this.evictionQueue.clear(); // the eviction queue no longer needs to be processed
        this.imageRetrieverCache.clear(); // the retrieval queue should be cleared to make room
        this.usedCapacity = 0;
    }

    public void releaseEvictedResources(DrawContext dc) {
        RenderResource evicted;
        while ((evicted = this.evictionQueue.poll()) != null) {
            try {
                evicted.release(dc);
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Released render resource \'" + evicted + "\'");
                }
            } catch (Exception ignored) {
                if (Logger.isLoggable(Logger.ERROR)) {
                    Logger.log(Logger.ERROR, "Exception releasing render resource \'" + evicted + "\'", ignored);
                }
            }
        }
    }

    @Override
    protected void entryRemoved(Object key, RenderResource oldValue, RenderResource newValue, boolean evicted) {
        this.evictionQueue.offer(oldValue);
    }

    public Texture retrieveTexture(ImageSource imageSource, ImageOptions options) {
        if (imageSource == null) {
            return null; // a null image source corresponds to a null texture
        }

        // Bitmap image sources are already in memory, so a texture may be created and put into the cache immediately.
        if (imageSource.isBitmap()) {
            Texture texture = this.createTexture(imageSource, options, imageSource.asBitmap());
            this.put(imageSource, texture, texture.getByteCount());
            return texture;
        }

        // All other image sources must be retrieved from disk or network and must be retrieved on a separate thread.
        // This includes bitmap factory image sources, since we cannot make any guarantees about what a bitmap factory
        // implementation may do. First look for the image in the image retrieval cache, removing it and creating a
        // corresponding texture if found.
        Bitmap bitmap = this.imageRetrieverCache.remove(imageSource);
        if (bitmap != null) {
            Texture texture = this.createTexture(imageSource, options, bitmap);
            this.put(imageSource, texture, texture.getByteCount());
            return texture;
        }

        // The image must be retrieved on a separate thread. Request the image source and return null to indicate that
        // the texture is not in memory. The image is added to the image retrieval cache upon successful retrieval. It's
        // then expected that a subsequent render frame will result in another call to retrieveTexture, in which case
        // the image will be found in the image retrieval cache.
        if (imageSource.isUrl()) {
            this.urlImageRetriever.retrieve(imageSource, options, this);
        } else {
            this.imageRetriever.retrieve(imageSource, options, this);
        }
        return null;
    }

    protected Texture createTexture(ImageSource imageSource, ImageOptions options, Bitmap bitmap) {
        Texture texture = new Texture(bitmap);

        if (options != null && options.resamplingMode == WorldWind.NEAREST_NEIGHBOR) {
            texture.setTexParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            texture.setTexParameter(GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        }

        if (options != null && options.wrapMode == WorldWind.REPEAT) {
            texture.setTexParameter(GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            texture.setTexParameter(GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        }

        return texture;
    }

    @Override
    public void retrievalSucceeded(Retriever<ImageSource, ImageOptions, Bitmap> retriever, ImageSource key, ImageOptions options, Bitmap value) {
        this.imageRetrieverCache.put(key, value, value.getByteCount());
        WorldWind.requestRedraw();

        if (!this.handler.hasMessages(TRIM_STALE_RETRIEVALS)) {
            this.handler.sendEmptyMessageDelayed(TRIM_STALE_RETRIEVALS, TRIM_STALE_RETRIEVALS_DELAY);
        }

        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Image retrieval succeeded \'" + key + "\'");
        }
    }

    @Override
    public void retrievalFailed(Retriever<ImageSource, ImageOptions, Bitmap> retriever, ImageSource key, Throwable ex) {
        if (ex instanceof SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
            Logger.log(Logger.ERROR, "Socket timeout retrieving image \'" + key + "\'");
        } else if (ex != null) { // log checked exceptions with the entire stack trace
            Logger.log(Logger.ERROR, "Image retrieval failed with exception \'" + key + "\'", ex);
        } else {
            Logger.log(Logger.ERROR, "Image retrieval failed \'" + key + "\'");
        }
    }

    @Override
    public void retrievalRejected(Retriever<ImageSource, ImageOptions, Bitmap> retriever, ImageSource key) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Image retrieval rejected \'" + key + "\'");
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == TRIM_STALE_RETRIEVALS) {
            this.trimStaleRetrievals();
        }
        return false;
    }

    protected void trimStaleRetrievals() {
        long now = System.currentTimeMillis();
        int trimmedCapacity = this.imageRetrieverCache.trimToAge(now - STALE_RETRIEVAL_AGE);

        if (!this.handler.hasMessages(TRIM_STALE_RETRIEVALS) && this.imageRetrieverCache.getUsedCapacity() != 0) {
            this.handler.sendEmptyMessageDelayed(TRIM_STALE_RETRIEVALS, TRIM_STALE_RETRIEVALS_DELAY);
        }

        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, String.format(Locale.US, "Trimmed stale image retrievals %,.0f KB",
                trimmedCapacity / 1024.0));
        }
    }
}
