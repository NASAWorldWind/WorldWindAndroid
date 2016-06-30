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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.WWUtil;

public class ImageRetriever extends Retriever<ImageSource, ImageOptions, Bitmap> {

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
    protected void retrieveAsync(ImageSource imageSource, ImageOptions imageOptions,
                                 Callback<ImageSource, ImageOptions, Bitmap> callback) {
        try {
            Bitmap bitmap = this.decodeImage(imageSource, imageOptions);

            if (bitmap != null) {
                callback.retrievalSucceeded(this, imageSource, imageOptions, bitmap);
            } else {
                callback.retrievalFailed(this, imageSource, null); // failed but no exception
            }
        } catch (Throwable logged) {
            callback.retrievalFailed(this, imageSource, logged); // failed with exception
        }
    }

    // TODO can we explicitly recycle bitmaps from image sources other than direct Bitmap references?
    // TODO does explicit recycling help?
    protected Bitmap decodeImage(ImageSource imageSource, ImageOptions imageOptions) throws IOException {
        if (imageSource.isBitmap()) {
            return imageSource.asBitmap();
        }

        if (imageSource.isBitmapFactory()) {
            return imageSource.asBitmapFactory().createBitmap();
        }

        if (imageSource.isResource()) {
            return this.decodeResource(imageSource.asResource(), imageOptions);
        }

        if (imageSource.isFilePath()) {
            return this.decodeFilePath(imageSource.asFilePath(), imageOptions);
        }

        if (imageSource.isUrl()) {
            return this.decodeUrl(imageSource.asUrl(), imageOptions);
        }

        return this.decodeUnrecognized(imageSource);
    }

    protected Bitmap decodeResource(int id, ImageOptions imageOptions) {
        BitmapFactory.Options factoryOptions = this.bitmapFactoryOptions(imageOptions);
        return (this.resources != null) ? BitmapFactory.decodeResource(this.resources, id, factoryOptions) : null;
    }

    protected Bitmap decodeFilePath(String pathName, ImageOptions imageOptions) {
        BitmapFactory.Options factoryOptions = this.bitmapFactoryOptions(imageOptions);
        return BitmapFactory.decodeFile(pathName, factoryOptions);
    }

    protected Bitmap decodeUrl(String urlString, ImageOptions imageOptions) throws IOException {
        // TODO establish a file caching service for remote resources
        // TODO retry absent resources, they are currently handled but suppressed entirely after the first failure
        // TODO configurable connect and read timeouts

        InputStream stream = null;
        try {
            URLConnection conn = new URL(urlString).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(30000);

            stream = new BufferedInputStream(conn.getInputStream());

            BitmapFactory.Options factoryOptions = this.bitmapFactoryOptions(imageOptions);
            return BitmapFactory.decodeStream(stream, null, factoryOptions);
        } finally {
            WWUtil.closeSilently(stream);
        }
    }

    protected Bitmap decodeUnrecognized(ImageSource imageSource) {
        Logger.log(Logger.WARN, "Unrecognized image source \'" + imageSource + "\'");
        return null;
    }

    protected BitmapFactory.Options bitmapFactoryOptions(ImageOptions imageOptions) {
        BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
        factoryOptions.inScaled = false; // suppress default image scaling; load the image in its native dimensions

        if (imageOptions != null) {
            switch (imageOptions.imageConfig) {
                case WorldWind.RGBA_8888:
                    factoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    break;
                case WorldWind.RGB_565:
                    factoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    break;
            }
        }

        return factoryOptions;
    }
}
