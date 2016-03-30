/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;

public interface SurfaceTile {

    Sector getSector();

    boolean bindTexture(DrawContext dc, int texUnit);

    boolean applyTexCoordTransform(DrawContext dc, Matrix3 result);
}
