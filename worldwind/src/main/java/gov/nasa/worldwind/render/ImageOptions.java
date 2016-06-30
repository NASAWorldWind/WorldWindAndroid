/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.WorldWind;

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

    /**
     * Constructs an image options with default values.
     */
    public ImageOptions() {
    }

    /**
     * Constructs an image options with an image format.
     *
     * @param imageFormat the image format to use. Accepted values are {@link WorldWind#IMAGE_FORMAT_RGBA_8888} and
     *                    {@link WorldWind#IMAGE_FORMAT_RGB_565}.
     *
     * @return the new image options
     */
    public static ImageOptions fromImageFormat(@WorldWind.ImageFormat int imageFormat) {
        ImageOptions imageOptions = new ImageOptions();
        imageOptions.imageFormat = imageFormat;
        return imageOptions;
    }
}
