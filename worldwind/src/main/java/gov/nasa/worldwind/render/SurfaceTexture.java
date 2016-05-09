/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;

public interface SurfaceTexture { // TODO this likely belongs in the draw package

    Sector getSector();

    Matrix3 getTexCoordTransform();

    boolean bindTexture(DrawContext dc);
}
