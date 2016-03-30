/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

/**
 * Planet or celestial object that can be modeled by an ellipsoid. Implementations of this interface specify the
 * ellipsoidal parameters and projection appropriate for a specific planet or celestial object.
 * <p/>
 * A globe uses the Cartesian coordinate system specified by its {@link GeographicProjection}. All Cartesian coordinates
 * and elevations are in meters.
 */
public interface Globe {

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the equator.
     *
     * @return the radius at the equator, in meters.
     */
    double getEquatorialRadius();

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the poles.
     *
     * @return the radius at the poles, in meters.
     */
    double getPolarRadius();

    /**
     * Indicates the radius in meters of the globe's ellipsoid at a specified location.
     *
     * @param latitude  the location's latitude in degrees
     * @param longitude the location's longitude in degrees
     *
     * @return the radius in meters of the globe's ellipsoid at the specified location
     */
    double getRadiusAt(double latitude, double longitude);

    /**
     * Indicates the eccentricity squared parameter of the globe's ellipsoid. This is equivalent to <code>2*f -
     * f*f</code>, where <code>f</code> is the ellipsoid's flattening parameter.
     *
     * @return the eccentricity squared parameter of the globe's ellipsoid.
     */
    double getEccentricitySquared();

    /**
     * Indicates the geographic projection used by this globe. The projection specifies this globe's Cartesian
     * coordinate system.
     *
     * @return the globe's projection
     */
    GeographicProjection getProjection();

    /**
     * Sets the geographic projection used by this globe. The projection specifies this globe's Cartesian coordinate
     * system.
     *
     * @param projection the projection to use
     *
     * @throws IllegalArgumentException if the projection is null
     */
    void setProjection(GeographicProjection projection);

    /**
     * @return
     */
    Tessellator getTessellator();

    /**
     * @param tessellator
     */
    void setTessellator(Tessellator tessellator);

    /**
     * Converts a geographic position to Cartesian coordinates. This globe's projection specifies the Cartesian
     * coordinate system.
     *
     * @param latitude  the position's latitude in degrees
     * @param longitude the position's longitude in degrees
     * @param altitude  the position's altitude in meters
     * @param result    a pre-allocated {@link Vec3} in which to store the computed X, Y and Z Cartesian coordinates
     *
     * @return the result argument, set to the computed Cartesian coordinates
     *
     * @throws IllegalArgumentException if the result is null
     */
    Vec3 geographicToCartesian(double latitude, double longitude, double altitude, Vec3 result);

    /**
     * @param latitude
     * @param longitude
     * @param result
     *
     * @return
     *
     * @throws IllegalArgumentException if the result is null
     */
    Vec3 geographicToCartesianNormal(double latitude, double longitude, Vec3 result);

    /**
     * @param latitude
     * @param longitude
     * @param altitude
     * @param result
     *
     * @return
     *
     * @throws IllegalArgumentException if the result is null
     */
    Matrix4 geographicToCartesianTransform(double latitude, double longitude, double altitude, Matrix4 result);

    /**
     * @param sector
     * @param numLat
     * @param numLon
     * @param elevations
     * @param origin
     * @param result
     *
     * @return
     *
     * @throws IllegalArgumentException if any argument is null,
     */
    FloatBuffer geographicToCartesianGrid(Sector sector, int numLat, int numLon, double[] elevations, Vec3 origin,
                                          FloatBuffer result, int stride);

    /**
     * Converts a Cartesian point to a geographic position. This globe's projection specifies the Cartesian coordinate
     * system.
     *
     * @param x      the Cartesian point's X component
     * @param y      the Cartesian point's Y component
     * @param z      the Cartesian point's Z component
     * @param result a pre-allocated {@link Position} in which to store the computed geographic position
     *
     * @return the result argument, set to the computed geographic position
     *
     * @throws IllegalArgumentException if the result is null
     */
    Position cartesianToGeographic(double x, double y, double z, Position result);

    Matrix4 cartesianToLocalTransform(double x, double y, double z, Matrix4 result);

    Matrix4 cameraToCartesianTransform(Camera camera, Matrix4 result);

    LookAt cameraToLookAt(Camera camera, LookAt result);

    Matrix4 lookAtToCartesianTransform(LookAt lookAt, Matrix4 result);

    Camera lookAtToCamera(LookAt lookAt, Camera result);

    /**
     * Indicates the distance to the globe's horizon from a specified eye altitude. The result of this method is
     * undefined if the eye altitude is negative.
     *
     * @param eyeAltitude the eye altitude in meters
     *
     * @return the distance in meters
     */
    double horizonDistance(double eyeAltitude);

    /**
     * Indicates the distance to an object passing over the globe's horizon from a specified eye altitude. This computes
     * the distance at which a point at objectAltitude is on the threshold of passing beyond the globe's horizon, and
     * would thereafter be occluded by the globe. The result of this method is undefined if either altitude is
     * negative.
     *
     * @param eyeAltitude    the eye altitude in meters
     * @param objectAltitude the object altitude in meters
     *
     * @return the distance in meters
     */
    double horizonDistance(double eyeAltitude, double objectAltitude);

    /**
     * Computes the first intersection of this globe with a specified line. The line is interpreted as a ray;
     * intersection points behind the line's origin are ignored.
     *
     * @param line   the line to intersect with this globe
     * @param result a pre-allocated {@link Vec3} in which to return the computed point
     *
     * @return true if the ray intersects the globe, otherwise false
     *
     * @throws IllegalArgumentException If either argument is null
     */
    boolean intersect(Line line, Vec3 result);
}
