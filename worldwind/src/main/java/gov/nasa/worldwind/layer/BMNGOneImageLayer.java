/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.shape.SurfaceImage;

/**
 * Displays a Blue Marble image that spans the entire globe at ~20km resolution.
 */
public class BMNGOneImageLayer extends RenderableLayer {

    /**
     * Constructs a Blue Marble image layer that spans the entire globe.
     */
    public BMNGOneImageLayer() {
        super("Blue Marble Image");

        // Delegate display to the SurfaceImage shape.
        this.addRenderable(new SurfaceImage(new Sector(-90, 90, -180, 180), R.drawable.gov_nasa_worldwind_worldtopobathy2004053));

        // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
        this.setPickEnabled(false);
    }
}
