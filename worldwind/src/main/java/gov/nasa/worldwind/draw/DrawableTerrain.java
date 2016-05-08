/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;

public interface DrawableTerrain extends Drawable {

    Sector getSector();

    Vec3 getVertexOrigin();

    void useVertexPointAttrib(DrawContext dc, int attribLocation);

    void useVertexTexCoordAttrib(DrawContext dc, int attribLocation);

    void drawLines(DrawContext dc);

    void drawTriangles(DrawContext dc);
}
