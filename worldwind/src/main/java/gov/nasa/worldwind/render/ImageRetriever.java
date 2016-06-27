/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import gov.nasa.worldwind.util.AbstractRetriever;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class ImageRetriever extends AbstractRetriever<ImageSource, Bitmap> {

    protected Resources resources;

    public ImageRetriever(int maxSimultaneousRetrievals) {
        super(maxSimultaneousRetrievals);
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources res) {
        this.resources = res;
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

    // TODO can we explicitly recycle bitmaps from image sources other than direct Bitmap references?
    // TODO does explicit recycling help?
    protected Bitmap decodeImage(ImageSource imageSource) throws IOException {
        if (imageSource.isBitmap()) {
            return imageSource.asBitmap();
        }

        if (imageSource.isBitmapFactory()) {
            return imageSource.asBitmapFactory().createBitmap();
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
