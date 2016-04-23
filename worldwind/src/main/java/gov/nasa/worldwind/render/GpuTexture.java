/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.util.WWUtil;

public class GpuTexture implements GpuObject, Runnable {

    protected final ImageSource imageSource;

    protected int textureId;

    protected int imageWidth;

    protected int imageHeight;

    protected int imageByteCount;

    protected boolean disposed;

    protected boolean requested;

    protected volatile Bitmap imageBitmap;

    protected volatile Resources resources;

    public GpuTexture(DrawContext dc, ImageSource imageSource) {
        if (imageSource == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "constructor", "missingSource"));
        }

        this.imageSource = imageSource;

        if (imageSource.isBitmap()) {
            this.loadImage(dc, imageSource.asBitmap());
            dc.gpuObjectCache.put(imageSource, this, this.imageByteCount);
        } else {
            dc.gpuObjectCache.put(imageSource, this, 1); // cache entry is replaced upon texture load
        }
    }

    @Override
    public String toString() {
        return "GpuTexture{" +
            "imageSource=" + this.imageSource +
            ", textureId=" + this.textureId +
            ", imageByteCount=" + this.imageByteCount +
            '}';
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public int getImageByteCount() {
        return this.imageByteCount;
    }

    @Override
    public void dispose(DrawContext dc) {
        synchronized (this) { // synchronize texture disposal and loading
            if (this.textureId != 0) {
                GLES20.glDeleteTextures(1, new int[]{this.textureId}, 0);
            }

            this.textureId = 0;
            this.imageBitmap = null;
            this.resources = null;
            this.disposed = true;
        }
    }

    public boolean hasTexture() {
        return (this.textureId != 0) || (this.imageBitmap != null);
    }

    public boolean bindTexture(DrawContext dc, int texUnit) {
        if (this.mustLoadImage()) {
            this.loadImage(dc, this.imageBitmap);
            this.imageBitmap = null;
            this.resources = null;
            dc.gpuObjectCache.put(this.imageSource, this, this.imageByteCount); // update the GPU cache entry size
        } else if (this.mustRequestImage()) {
            this.requestImage(dc);
        }

        if (this.textureId != 0) {
            dc.bindTexture(texUnit, this.textureId);
        }

        return this.textureId != 0;
    }

    public boolean applyTexCoordTransform(Matrix3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "applyTexCoordTransform", "missingResult"));
        }

        if (this.textureId != 0) {
            result.multiplyByVerticalFlip();
        }

        return this.textureId != 0;
    }

    @Override
    public void run() {
        synchronized (this) { // synchronize texture disposal and loading
            if (this.disposed) { // texture disposed between request initiated and now
                return;
            }
        }

        Bitmap bitmap = this.guardedDecodeImage(this.imageSource);
        if (bitmap == null) {
            // TODO establish a file caching service for remote resources
            // TODO retry absent resources, they are currently handled but suppressed entirely after the first failure
            return; // image retrieval failed; the reason is logged in guardedDecodeImage
        }

        synchronized (this) { // synchronize texture disposal and loading
            if (!this.disposed) { // texture disposed during guardedDecodeImage
                this.imageBitmap = bitmap;
                WorldWind.requestRender();
            }
        }
    }

    protected boolean mustRequestImage() {
        return !this.requested;
    }

    protected void requestImage(DrawContext dc) {
        this.resources = dc.resources; // temporarily reference used to load Android resources asynchronously
        this.requested = WorldWind.retrievalService().offer(this); // suppress duplicate requests if the task was accepted
    }

    protected Bitmap guardedDecodeImage(ImageSource imageSource) {
        try {

            Bitmap bitmap = this.decodeImage(imageSource);

            if (bitmap != null) {
                if (Logger.isLoggable(Logger.DEBUG)) {
                    Logger.log(Logger.DEBUG, "Image retrieval succeeded \'" + this.imageSource + "\'");
                }
            } else {
                Logger.log(Logger.ERROR, "Image retrieval failed \'" + this.imageSource + "\'");
            }

            return bitmap;

        } catch (SocketTimeoutException ignored) { // log socket timeout exceptions while suppressing the stack trace
            Logger.log(Logger.ERROR, "Socket timeout retrieving image \'" + this.imageSource);
        } catch (Exception logged) { // log checked exceptions with the entire stack trace
            Logger.log(Logger.ERROR, "Image retrieval failed with exception \'" + this.imageSource, logged);
        }

        return null;
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
        return BitmapFactory.decodeResource(this.resources, id, options);
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

    protected boolean mustLoadImage() {
        return this.imageBitmap != null;
    }

    protected void loadImage(DrawContext dc, Bitmap bitmap) {
        int[] newTexture = new int[1];
        int[] prevTexture = new int[1];
        boolean isPowerOfTwo = WWMath.isPowerOfTwo(bitmap.getWidth()) && WWMath.isPowerOfTwo(bitmap.getHeight());

        GLES20.glGenTextures(1, newTexture, 0);
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, prevTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, newTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            isPowerOfTwo ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.imageBitmap, 0);

        if (isPowerOfTwo) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, prevTexture[0]); // restore the previous OpenGL texture binding

        this.textureId = newTexture[0];
        this.imageWidth = bitmap.getWidth();
        this.imageHeight = bitmap.getHeight();
        this.imageByteCount = bitmap.getByteCount();
    }
}
