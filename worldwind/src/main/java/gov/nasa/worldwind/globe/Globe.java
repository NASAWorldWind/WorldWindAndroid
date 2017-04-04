/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Ellipsoid;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

/**
 * Planet or celestial object approximated by a reference ellipsoid and elevation models. Globe expresses its
 * ellipsoidal parameters and elevation values in meters.
 */
public class Globe {

    /**
     * The globe's reference ellipsoid defining the globe's equatorial radius and polar radius.
     */
    protected Ellipsoid ellipsoid = new Ellipsoid();

    protected ElevationModel elevationModel = new ElevationModel();

    /**
     * Indicates the geographic projection used by this globe. The projection specifies this globe's Cartesian
     * coordinate system.
     */
    protected GeographicProjection projection;

    /**
     * Constructs a globe with a specified reference ellipsoid and projection.
     *
     * @param ellipsoid  the reference ellipsoid defining the globe's equatorial radius and polar radius
     * @param projection the geographic projection used by the globe, specifies the globe's Cartesian coordinate system
     *
     * @throws IllegalArgumentException If the ellipsoid is null
     */
    public Globe(Ellipsoid ellipsoid, GeographicProjection projection) {
        if (ellipsoid == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "setProjection", "missingEllipsoid"));
        }

        if (projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "setProjection", "missingProjection"));
        }

        this.ellipsoid.set(ellipsoid);
        this.projection = projection;
    }

    /**
     * Indicates the reference ellipsoid defining this globe's equatorial radius and polar radius.
     *
     * @return this globe's reference ellipsoid
     */
    public Ellipsoid getEllipsoid() {
        return this.ellipsoid;
    }

    /**
     * Sets the reference ellipsoid that defines this globe's equatorial radius and polar radius.
     *
     * @param ellipsoid the new reference ellipsoid
     *
     * @throws IllegalArgumentException If the ellipsoid is null
     */
    public void setEllipsoid(Ellipsoid ellipsoid) {
        if (ellipsoid == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "setEllipsoid", "missingEllipsoid"));
        }

        this.ellipsoid.set(ellipsoid);
    }

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the equator.
     *
     * @return the radius at the equator, in meters.
     */
    public double getEquatorialRadius() {
        return this.ellipsoid.semiMajorAxis();
    }

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the poles.
     *
     * @return the radius at the poles, in meters.
     */
    public double getPolarRadius() {
        return this.ellipsoid.semiMinorAxis();
    }

    /**
     * Indicates the radius in meters of the globe's ellipsoid at a specified location.
     *
     * @param latitude  the location's latitude in degrees
     * @param longitude the location's longitude in degrees
     *
     * @return the radius in meters of the globe's ellipsoid at the specified location
     */
    public double getRadiusAt(double latitude, double longitude) {
        // The radius for an ellipsoidal globe is a function of its latitude. The following solution was derived by
        // observing that the length of the ellipsoidal point at the specified latitude and longitude indicates the
        // radius at that location. The formula for the length of the ellipsoidal point was then converted into the
        // simplified form below.

        double sinLat = Math.sin(Math.toRadians(latitude));
        double ec2 = this.ellipsoid.eccentricitySquared();
        double rpm = this.ellipsoid.semiMajorAxis() / Math.sqrt(1 - ec2 * sinLat * sinLat);
        return rpm * Math.sqrt(1 + (ec2 * ec2 - 2 * ec2) * sinLat * sinLat);
    }

    /**
     * Indicates the eccentricity squared parameter of the globe's ellipsoid. This is equivalent to <code>2*f -
     * f*f</code>, where <code>f</code> is the ellipsoid's flattening parameter.
     *
     * @return the eccentricity squared parameter of the globe's ellipsoid.
     */
    public double getEccentricitySquared() {
        return this.ellipsoid.eccentricitySquared();
    }

    public ElevationModel getElevationModel() {
        return elevationModel;
    }

    public void setElevationModel(ElevationModel elevationModel) {
        this.elevationModel = elevationModel;
    }

    /**
     * Indicates the geographic projection used by this globe. The projection specifies this globe's Cartesian
     * coordinate system.
     *
     * @return the globe's projection
     */
    public GeographicProjection getProjection() {
        return projection;
    }

    /**
     * Sets the geographic projection used by this globe. The projection specifies this globe's Cartesian coordinate
     * system.
     *
     * @param projection the projection to use
     *
     * @throws IllegalArgumentException if the projection is null
     */
    public void setProjection(GeographicProjection projection) {
        if (projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "setProjection", "missingProjection"));
        }

        this.projection = projection;
    }

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
    public Vec3 geographicToCartesian(double latitude, double longitude, double altitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesian", "missingResult"));
        }

        return this.projection.geographicToCartesian(this, latitude, longitude, altitude, result);
    }

    public Vec3 geographicToCartesianNormal(double latitude, double longitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianNormal", "missingResult"));
        }

        return this.projection.geographicToCartesianNormal(this, latitude, longitude, result);
    }

    public Matrix4 geographicToCartesianTransform(double latitude, double longitude, double altitude, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianTransform", "missingResult"));
        }

        return this.projection.geographicToCartesianTransform(this, latitude, longitude, altitude, result);
    }

    public float[] geographicToCartesianGrid(Sector sector, int numLat, int numLon, float[] height, float verticalExaggeration,
                                             Vec3 origin, float[] result, int offset, int rowStride) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianGrid", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianGrid",
                    "Number of latitude or longitude locations is less than one"));
        }

        int numPoints = numLat * numLon;
        if (height != null && height.length < numPoints) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianGrid", "missingArray"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianGrid", "missingResult"));
        }

        return this.projection.geographicToCartesianGrid(this, sector, numLat, numLon, height, verticalExaggeration,
            origin, result, offset, rowStride);
    }

    public float[] geographicToCartesianBorder(Sector sector, int numLat, int numLon, float height,
                                               Vec3 origin, float[] result) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianBorder", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Globe",
                "geographicToCartesianBorder", "Number of latitude or longitude locations is less than one"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianBorder", "missingResult"));
        }

        return this.projection.geographicToCartesianBorder(this, sector, numLat, numLon, height, origin, result);
    }

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
    public Position cartesianToGeographic(double x, double y, double z, Position result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "cartesianToGeographic", "missingResult"));
        }

        return this.projection.cartesianToGeographic(this, x, y, z, result);
    }

    public Matrix4 cartesianToLocalTransform(double x, double y, double z, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "cartesianToLocalTransform", "missingResult"));
        }

        return this.projection.cartesianToLocalTransform(this, x, y, z, result);
    }

    /**
     * Indicates the distance to the globe's horizon from a specified height above the globe's ellipsoid. The result of
     * this method is undefined if the height is negative.
     *
     * @param height the viewer's height above the globe's ellipsoid in meters
     *
     * @return the horizon distance in meters
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public double horizonDistance(double height) {
        double r = this.ellipsoid.semiMajorAxis();
        return Math.sqrt(height * (2 * r + height));
    }

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
    public boolean intersect(Line line, Vec3 result) {
        if (line == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "intersect", "missingLine"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "intersect", "missingResult"));
        }

        return this.projection.intersect(this, line, result);
    }
}
