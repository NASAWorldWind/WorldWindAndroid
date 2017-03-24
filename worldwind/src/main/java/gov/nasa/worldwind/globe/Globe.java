/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Ellipsoid;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.LookAt;
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

    /**
     * Indicates the geographic projection used by this globe. The projection specifies this globe's Cartesian
     * coordinate system.
     */
    protected GeographicProjection projection;

    private Matrix4 modelview = new Matrix4();

    private Matrix4 origin = new Matrix4();

    private Vec3 originPoint = new Vec3();

    private Position originPos = new Position();

    private Line forwardRay = new Line();

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

    /**
     * @param latitude
     * @param longitude
     * @param result
     *
     * @return
     *
     * @throws IllegalArgumentException if the result is null
     */
    public Vec3 geographicToCartesianNormal(double latitude, double longitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianNormal", "missingResult"));
        }

        return this.projection.geographicToCartesianNormal(this, latitude, longitude, result);
    }

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
    public Matrix4 geographicToCartesianTransform(double latitude, double longitude, double altitude, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianTransform", "missingResult"));
        }

        return this.projection.geographicToCartesianTransform(this, latitude, longitude, altitude, result);
    }

    /**
     * @param sector
     * @param numLat
     * @param numLon
     * @param elevations
     * @param origin
     * @param result
     * @param stride
     * @param pos
     *
     * @return
     *
     * @throws IllegalArgumentException if any argument is null,
     */
    public float[] geographicToCartesianGrid(Sector sector, int numLat, int numLon, float[] elevations,
                                             Vec3 origin, float[] result, int stride, int pos) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianGrid", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Globe",
                "geographicToCartesianGrid", "Number of latitude or longitude locations is less than one"));
        }

        int numPoints = numLat * numLon;
        if (elevations != null && elevations.length < numPoints) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Globe",
                "geographicToCartesianGrid", "missingArray"));
        }

        if (result == null || result.length < numPoints * stride + pos) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "geographicToCartesianGrid", "missingResult"));
        }

        return this.projection.geographicToCartesianGrid(this, sector, numLat, numLon, elevations, origin,
            result, stride, pos);
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

    public Matrix4 cameraToCartesianTransform(Camera camera, Matrix4 result) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "cameraToCartesianTransform", "missingCamera"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "cameraToCartesianTransform", "missingResult"));
        }

        // TODO interpret altitude mode other than absolute
        // Transform by the local cartesian transform at the camera's position.
        this.geographicToCartesianTransform(camera.latitude, camera.longitude, camera.altitude, result);

        // Transform by the heading, tilt and roll.
        result.multiplyByRotation(0, 0, 1, -camera.heading); // rotate clockwise about the Z axis
        result.multiplyByRotation(1, 0, 0, camera.tilt); // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0, 0, 1, camera.roll); // rotate counter-clockwise about the Z axis (again)

        return result;
    }

    public LookAt cameraToLookAt(Camera camera, LookAt result) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "cameraToLookAt", "missingCamera"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "cameraToLookAt", "missingResult"));
        }

        this.cameraToCartesianTransform(camera, this.modelview).invertOrthonormal();
        this.modelview.extractEyePoint(this.forwardRay.origin);
        this.modelview.extractForwardVector(this.forwardRay.direction);

        if (!this.intersect(this.forwardRay, this.originPoint)) {
            double horizon = this.horizonDistance(camera.altitude);
            this.forwardRay.pointAt(horizon, this.originPoint);
        }

        this.cartesianToGeographic(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.originPos);
        this.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);
        this.modelview.multiplyByMatrix(this.origin);

        result.latitude = this.originPos.latitude;
        result.longitude = this.originPos.longitude;
        result.altitude = this.originPos.altitude;
        result.range = -this.modelview.m[11];
        result.heading = this.computeViewHeading(this.modelview, camera.roll); // disambiguate heading and roll
        result.tilt = this.computeViewTilt(this.modelview);
        result.roll = camera.roll; // roll passes straight through

        return result;
    }

    public Matrix4 lookAtToCartesianTransform(LookAt lookAt, Matrix4 result) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "lookAtToCartesianTransform", "missingLookAt"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "lookAtToCartesianTransform", "missingResult"));
        }

        // TODO interpret altitude mode other than absolute
        // Transform by the local cartesian transform at the look-at's position.
        this.geographicToCartesianTransform(lookAt.latitude, lookAt.longitude, lookAt.altitude, result);

        // Transform by the heading and tilt.
        result.multiplyByRotation(0, 0, 1, -lookAt.heading); // rotate clockwise about the Z axis
        result.multiplyByRotation(1, 0, 0, lookAt.tilt); // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0, 0, 1, lookAt.roll); // rotate counter-clockwise about the Z axis (again)

        // Transform by the range.
        result.multiplyByTranslation(0, 0, lookAt.range);

        return result;
    }

    public Camera lookAtToCamera(LookAt lookAt, Camera result) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "lookAtToCamera", "missingLookAt"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Globe", "lookAtToCamera", "missingResult"));
        }

        this.lookAtToCartesianTransform(lookAt, this.modelview).invertOrthonormal();
        this.modelview.extractEyePoint(this.originPoint);

        this.cartesianToGeographic(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.originPos);
        this.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);
        this.modelview.multiplyByMatrix(this.origin);

        result.latitude = this.originPos.latitude;
        result.longitude = this.originPos.longitude;
        result.altitude = this.originPos.altitude;
        result.heading = this.computeViewHeading(this.modelview, lookAt.roll); // disambiguate heading and roll
        result.tilt = this.computeViewTilt(this.modelview);
        result.roll = lookAt.roll; // roll passes straight through

        return result;
    }

    protected double computeViewHeading(Matrix4 matrix, double roll) {
        double rad = Math.toRadians(roll);
        double cr = Math.cos(rad);
        double sr = Math.sin(rad);

        double[] m = matrix.m;
        double ch = cr * m[0] - sr * m[4];
        double sh = sr * m[5] - cr * m[1];
        return Math.toDegrees(Math.atan2(sh, ch));
    }

    protected double computeViewTilt(Matrix4 matrix) {
        double[] m = matrix.m;
        double ct = m[10];
        double st = Math.sqrt(m[2] * m[2] + m[6] * m[6]);
        return Math.toDegrees(Math.atan2(st, ct));
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
