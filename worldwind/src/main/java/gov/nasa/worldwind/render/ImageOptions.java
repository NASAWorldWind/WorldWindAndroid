/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;

/**
 * Options for images displayed by World Wind components.
 */
public class ImageOptions {

    /**
     * Indicates the in-memory representation for images displayed by World Wind components. By default, images are
     * represented in the 32-bit RGBA_8888 format, the highest quality available. Components that do not require an
     * alpha channel and want to conserve memory may use the 16-bit RGBA_565 format. Accepted values are {@link
     * WorldWind#IMAGE_FORMAT_RGBA_8888} and {@link WorldWind#IMAGE_FORMAT_RGB_565}.
     */
    @WorldWind.ImageFormat
    public int imageFormat = WorldWind.IMAGE_FORMAT_RGBA_8888;

    private static Pool<ImageOptions> pool = new SynchronizedPool<>();

    private ImageOptions() {
    }

    /**
     * Obtains a new image options instance from a global pool.
     *
     * @return the new instance
     */
    public static ImageOptions obtain() {
        ImageOptions instance = pool.acquire();
        return (instance != null) ? instance : new ImageOptions();
    }

    /**
     * Recycles this image options instance into the global pool.
     */
    public void recycle() {
        this.imageFormat = WorldWind.IMAGE_FORMAT_RGBA_8888;
        pool.release(this);
    }
}
