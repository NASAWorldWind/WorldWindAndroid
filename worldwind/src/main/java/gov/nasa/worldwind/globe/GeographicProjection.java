/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

/**
 * Represents a geographic projection.
 */
public interface GeographicProjection {

    /**
     *
     * @return
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
     * @param result    a pre-allocated Vec3 in which to store the converted coordinates
     *
     * @return the result argument set to the convertex coordinates
     *
     * @throws IllegalArgumentException If any argument is null
     */
    Vec3 geographicToCartesian(Globe globe, double latitude, double longitude, double altitude, Vec3 offset, Vec3 result);

    /**
     *
     * @param globe
     * @param latitude
     * @param longitude
     * @param result
     * @return
     */
    Vec3 geographicToCartesianNormal(Globe globe, double latitude, double longitude, Vec3 result);

    /**
     *
     * @param globe
     * @param latitude
     * @param longitude
     * @param altitude
     * @param result
     * @return
     */
    Matrix4 geographicToCartesianTransform(Globe globe, double latitude, double longitude, double altitude, Vec3 offset, Matrix4 result);

    /**
     *
     * @param globe
     * @param sector
     * @param numLat
     * @param numLon
     * @param elevations
     * @param referencePoint
     * @param offset
     * @param result
     * @return
     */
    FloatBuffer geographicToCartesianGrid(Globe globe, Sector sector, int numLat, int numLon, double[] elevations,
                                           Vec3 referencePoint, Vec3 offset, FloatBuffer result, int stride);

    /**
     *
     * @param globe
     * @param x
     * @param y
     * @param z
     * @param offset
     * @param result
     * @return
     */
    Position cartesianToGeographic(Globe globe, double x, double y, double z, Vec3 offset, Position result);
}
