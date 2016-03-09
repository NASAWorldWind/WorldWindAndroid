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

public interface Globe {

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the equator
     *
     * @return the radius at the equator, in meters.
     */
    double getEquatorialRadius();

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the poles
     *
     * @return the radius at the poles, in meters.
     */
    double getPolarRadius();

    /**
     * Indicates the radius in meters of the globe's ellipsoid at a location
     *
     * @param latitude  the location's latitude in degrees
     * @param longitude the location's longitude in degrees
     *
     * @return the radius in meters of the globe's ellipsoid at the specified location
     */
    double getRadiusAt(double latitude, double longitude);

    /**
     *
     * @return
     */
    double getEccentricitySquared();

    /**
     *
     * @return
     */
    GeographicProjection getProjection();

    /**
     *
     * @param projection
     */
    void setProjection(GeographicProjection projection);

    /**
     *
     * @param latitude
     * @param longitude
     * @param altitude
     * @param result
     * @return
     */
    Vec3 geographicToCartesian(double latitude, double longitude, double altitude, Vec3 result);

    /**
     *
     * @param latitude
     * @param longitude
     * @param result
     * @return
     */
    Vec3 geographicToCartesianNormal(double latitude, double longitude, Vec3 result);

    /**
     *
     * @param latitude
     * @param longitude
     * @param altitude
     * @param result
     * @return
     */
    Matrix4 geographicToCartesianTransform(double latitude, double longitude, double altitude, Matrix4 result);

    /**
     *
     * @param sector
     * @param numLat
     * @param numLon
     * @param elevations
     * @param referencePoint
     * @param result
     * @return
     */
    FloatBuffer geographicToCartesianGrid(Sector sector, int numLat, int numLon, double[] elevations,
                                           Vec3 referencePoint, FloatBuffer result);

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param result
     * @return
     */
    Position cartesianToGeographic(double x, double y, double z, Position result);
}
