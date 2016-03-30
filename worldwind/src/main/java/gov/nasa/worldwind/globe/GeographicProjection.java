/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.FloatBuffer;

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
     * @param offset    an offset to apply to the Cartesian output. Typically only projections that are continuous apply
     *                  to this offset. Others ignore it. May be null to indicate no offset is applied.
     * @param result    a pre-allocated {@link Vec3} in which to store the computed X, Y and Z Cartesian coordinates
     *
     * @return the result argument, set to the computed Cartesian coordinates
     *
     * @throws IllegalArgumentException If any argument is null
     */
    Vec3 geographicToCartesian(Globe globe, double latitude, double longitude, double altitude, Vec3 offset, Vec3 result);

    /**
     * @param globe
     * @param latitude
     * @param longitude
     * @param result
     *
     * @return
     */
    Vec3 geographicToCartesianNormal(Globe globe, double latitude, double longitude, Vec3 result);

    /**
     * @param globe
     * @param latitude
     * @param longitude
     * @param altitude
     * @param result
     *
     * @return
     */
    Matrix4 geographicToCartesianTransform(Globe globe, double latitude, double longitude, double altitude, Vec3 offset, Matrix4 result);

    /**
     * @param globe
     * @param sector
     * @param numLat
     * @param numLon
     * @param elevations
     * @param origin
     * @param offset
     * @param result
     *
     * @return
     */
    FloatBuffer geographicToCartesianGrid(Globe globe, Sector sector, int numLat, int numLon, double[] elevations,
                                          Vec3 origin, Vec3 offset, FloatBuffer result, int stride);

    /**
     * Converts a Cartesian point to a geographic position.
     *
     * @param globe
     * @param x      the Cartesian point's X component
     * @param y      the Cartesian point's Y component
     * @param z      the Cartesian point's Z component
     * @param offset an offset to apply to the Cartesian output. Typically only projections that are continuous apply to
     *               this offset. Others ignore it. May be null to indicate no offset is applied.
     * @param result a pre-allocated {@link Position} in which to store the computed geographic position
     *
     * @return the result argument, set to the computed geographic position
     *
     * @throws IllegalArgumentException if the result is null
     */
    Position cartesianToGeographic(Globe globe, double x, double y, double z, Vec3 offset, Position result);

    Matrix4 cartesianToLocalTransform(Globe globe, double x, double y, double z, Vec3 offset, Matrix4 result);

    /**
     * Computes the first intersection of a specified globe and line. The line is interpreted as a ray; intersection
     * points behind the line's origin are ignored.
     *
     * @param globe  the globe this projection is applied to
     * @param line   the line to intersect with the globe
     * @param offset an offset to apply to the Cartesian output. Typically only projections that are continuous apply to
     *               this offset. Others ignore it. May be null to indicate no offset is applied.
     * @param result a pre-allocated {@link Vec3} in which to return the computed point
     *
     * @return true if the ray intersects the globe, otherwise false
     *
     * @throws IllegalArgumentException If any of the globe, line or result are null
     */
    boolean intersect(Globe globe, Line line, Vec3 offset, Vec3 result);
}
