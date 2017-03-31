/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

/**
 * Represents transformations between geographic coordinates and Cartesian coordinates. GeographicProjection specifies
 * the Cartesian coordinate system used by Globe and WorldWindow.
 */
public interface GeographicProjection {

    /**
     * This projection's display name.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Converts a geographic position to Cartesian coordinates.
     *
     * @param globe     the globe this projection is applied to
     * @param latitude  the position's latitude in degrees
     * @param longitude the position's longitude in degrees
     * @param altitude  the position's altitude in meters
     * @param result    a pre-allocated {@link Vec3} in which to store the computed X, Y and Z Cartesian coordinates
     *
     * @return the result argument, set to the computed Cartesian coordinates
     *
     * @throws IllegalArgumentException If any argument is null
     */
    Vec3 geographicToCartesian(Globe globe, double latitude, double longitude, double altitude, Vec3 result);

    Vec3 geographicToCartesianNormal(Globe globe, double latitude, double longitude, Vec3 result);

    Matrix4 geographicToCartesianTransform(Globe globe, double latitude, double longitude, double altitude, Matrix4 result);

    float[] geographicToCartesianGrid(Globe globe, Sector sector, int numLat, int numLon, float[] height, float verticalExaggeration,
                                      Vec3 origin, float[] result, int offset, int rowStride);

    float[] geographicToCartesianBorder(Globe globe, Sector sector, int numLat, int numLon, float height,
                                        Vec3 origin, float[] result);

    /**
     * Converts a Cartesian point to a geographic position.
     *
     * @param globe
     * @param x      the Cartesian point's X component
     * @param y      the Cartesian point's Y component
     * @param z      the Cartesian point's Z component
     * @param result a pre-allocated {@link Position} in which to store the computed geographic position
     *
     * @return the result argument, set to the computed geographic position
     *
     * @throws IllegalArgumentException if the result is null
     */
    Position cartesianToGeographic(Globe globe, double x, double y, double z, Position result);

    Matrix4 cartesianToLocalTransform(Globe globe, double x, double y, double z, Matrix4 result);

    /**
     * Computes the first intersection of a specified globe and line. The line is interpreted as a ray; intersection
     * points behind the line's origin are ignored.
     *
     * @param globe  the globe this projection is applied to
     * @param line   the line to intersect with the globe
     * @param result a pre-allocated {@link Vec3} in which to return the computed point
     *
     * @return true if the ray intersects the globe, otherwise false
     *
     * @throws IllegalArgumentException If any of the globe, line or result are null
     */
    boolean intersect(Globe globe, Line line, Vec3 result);
}
