/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;

public interface Terrain {

    Sector getSector();

    int getTileCount();

    Sector getTileSector(int index);

    Vec3 getTileVertexOrigin(int index);

    void applyTexCoordTransform(int index, Sector dst, Matrix3 result);

    void useVertexPointAttrib(DrawContext dc, int index, int attribLocation);

    void useVertexTexCoordAttrib(DrawContext dc, int attribLocation);

    void drawTileTriangles(DrawContext dc, int index);

    void drawTileLines(DrawContext dc, int index);

    Vec3 geographicToCartesian(double latitude, double longitude, double altitude,
                               @WorldWind.AltitudeMode int altitudeMode, Vec3 result);
}
