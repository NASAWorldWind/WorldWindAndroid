/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.SurfaceImage;
import gov.nasa.worldwind.util.Logger;

/**
 * Displays a single image spanning the globe. By default, BackgroundLayer is configured to display NASA's Blue Marble
 * next generation image at 40km resolution from the built-in World Wind library resource
 * res/drawable/gov_nasa_worldwind_worldtopobathy2004053.
 */
public class BackgroundLayer extends RenderableLayer {

    /**
     * Constructs a Blue Marble background layer with the built-in World Wind library resource
     * res/drawable/gov_nasa_worldwind_worldtopobathy2004053. The resource must be accessible from the Android Context
     * associated with the World Window.
     */
    public BackgroundLayer() {
        this(ImageSource.fromResource(R.drawable.gov_nasa_worldwind_worldtopobathy2004053), new ImageOptions(WorldWind.RGB_565));
    }

    /**
     * Constructs a background image layer with an image source. The image's dimensions must be no greater than 2048 x
     * 2048.
     *
     * @param imageSource  the image source
     * @param imageOptions the image options, or null to use the default options
     *
     * @throws IllegalArgumentException If the image source is null
     */
    public BackgroundLayer(ImageSource imageSource, ImageOptions imageOptions) {
        if (imageSource == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BackgroundLayer", "constructor", "missingSource"));
        }

        this.setDisplayName("Background");

        // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
        this.setPickEnabled(false);

        // Delegate display to the SurfaceImage shape.
        SurfaceImage surfaceImage = new SurfaceImage(new Sector().setFullSphere(), imageSource);
        surfaceImage.setImageOptions(imageOptions);
        this.addRenderable(surfaceImage);
    }
}
