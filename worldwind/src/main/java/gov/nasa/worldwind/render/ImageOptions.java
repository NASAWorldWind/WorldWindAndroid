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
     * Indicates the in-memory configuration for images displayed by World Wind components. By default, images are
     * represented in the 32-bit RGBA_8888 configuration, the highest quality available. Components that do not require
     * an alpha channel and want to conserve memory may use the 16-bit RGBA_565 configuration. Accepted values are
     * {@link WorldWind#RGBA_8888} and {@link WorldWind#RGB_565}.
     */
    @WorldWind.ImageConfig
    public int imageConfig = WorldWind.RGBA_8888;

    /**
     * Indicates the image sampling algorithm used by World Wind to display images that appear larger or smaller on
     * screen than their native resolution. Accepted values are {@link WorldWind#BILINEAR} and {@link
     * WorldWind#NEAREST_NEIGHBOR}.
     */
    @WorldWind.ResamplingMode
    public int resamplingMode = WorldWind.BILINEAR;

    /**
     * Indicates how World Wind displays the contents of an image when attempting to draw a region outside of the image
     * bounds. Accepted values are {@link WorldWind#CLAMP} and {@link WorldWind#REPEAT}.
     */
    @WorldWind.WrapMode
    public int wrapMode = WorldWind.CLAMP;

    /**
     * Constructs an image options with default values.
     */
    public ImageOptions() {
    }

    /**
     * Constructs an image options with an image configuration.
     *
     * @param imageConfig the image configuration to use. Accepted values are {@link WorldWind#RGBA_8888} and {@link
     *                    WorldWind#RGB_565}.
     */
    public ImageOptions(@WorldWind.ImageConfig int imageConfig) {
        this.imageConfig = imageConfig;
    }
}
