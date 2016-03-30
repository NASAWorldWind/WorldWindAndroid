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
import gov.nasa.worldwind.util.Logger;

/**
 * Generic ellipsoidal Globe enabling construction with standard ellipsoid values.
 */
public class BasicGlobe implements Globe {

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the equator.
     */
    protected double equatorialRadius;

    /**
     * Indicates the radius in meters of the globe's ellipsoid at the poles. This is equivalent to <code>semiMajorAxis *
     * (1 - f)</code>, where <code>semiMajorAxis</code> and <code>f</code> are the arguments specified at construction.
     */
    protected double polarRadius;

    /**
     * Indicates the eccentricity squared parameter of the globe's ellipsoid. This is equivalent to <code>2*f -
     * f*f</code>, where <code>f</code> is the flattening argument specified at construction.
     */
    protected double eccentricitySquared;

    /**
     * Indicates the geographic projection used by this globe. The projection specifies this globe's Cartesian
     * coordinate system.
     */
    protected GeographicProjection projection;

    /**
     *
     */
    protected Tessellator tessellator;

    private Matrix4 modelview = new Matrix4();

    private Matrix4 origin = new Matrix4();

    private Vec3 originPoint = new Vec3();

    private Position originPos = new Position();

    private Line forwardRay = new Line();

    /**
     * Constructs a generic Globe implementation with specified ellipsoid parameters.
     *
     * @param semiMajorAxis     one half of the globe's major axis, which runs through the center to opposite points on
     *                          the equator
     * @param inverseFlattening a measure of the ellipsoid's compression
     * @param projection        the geographic projection used by this globe, which specifies the Cartesian coordinate
     *                          system
     *
     * @throws IllegalArgumentException if either of the semi-major axis or the inverse flattening are less than or
     *                                  equal to zero, or if the projection is null
     */
    public BasicGlobe(double semiMajorAxis, double inverseFlattening, GeographicProjection projection) {
        if (semiMajorAxis <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "Semi-major axis is invalid"));
        }

        if (inverseFlattening <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "Inverse flattening is invalid"));
        }

        if (projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "constructor", "missingProjection"));
        }

        double f = 1 / inverseFlattening;
        this.equatorialRadius = semiMajorAxis;
        this.polarRadius = semiMajorAxis * (1 - f);
        this.eccentricitySquared = 2 * f - f * f;
        this.projection = projection;
        this.tessellator = new BasicTessellator();
    }

    @Override
    public double getEquatorialRadius() {
        return equatorialRadius;
    }

    @Override
    public double getPolarRadius() {
        return polarRadius;
    }

    @Override
    public double getRadiusAt(double latitude, double longitude) {
        // The radius for an ellipsoidal globe is a function of its latitude. The following solution was derived by
        // observing that the length of the ellipsoidal point at the specified latitude and longitude indicates the
        // radius at that location. The formula for the length of the ellipsoidal point was then converted into the
        // simplified form below.

        double sinLat = Math.sin(Math.toRadians(latitude));
        double ec2 = this.eccentricitySquared;
        double rpm = this.equatorialRadius / Math.sqrt(1 - ec2 * sinLat * sinLat);
        return rpm * Math.sqrt(1 + (ec2 * ec2 - 2 * ec2) * sinLat * sinLat);
    }

    @Override
    public double getEccentricitySquared() {
        return eccentricitySquared;
    }

    @Override
    public GeographicProjection getProjection() {
        return projection;
    }

    @Override
    public void setProjection(GeographicProjection projection) {
        if (projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "setProjection", "missingProjection"));
        }

        this.projection = projection;
    }

    @Override
    public Tessellator getTessellator() {
        return tessellator;
    }

    @Override
    public void setTessellator(Tessellator tessellator) {
        if (tessellator == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "setTessellator", "missingTessellator"));
        }

        this.tessellator = tessellator;
    }

    @Override
    public Vec3 geographicToCartesian(double latitude, double longitude, double altitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesian", "missingResult"));
        }

        return this.projection.geographicToCartesian(this, latitude, longitude, altitude, null, result);
    }

    @Override
    public Vec3 geographicToCartesianNormal(double latitude, double longitude, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianNormal", "missingResult"));
        }

        return this.projection.geographicToCartesianNormal(this, latitude, longitude, result);
    }

    @Override
    public Matrix4 geographicToCartesianTransform(double latitude, double longitude, double altitude, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianTransform", "missingResult"));
        }

        return this.projection.geographicToCartesianTransform(this, latitude, longitude, altitude, null, result);
    }

    @Override
    public FloatBuffer geographicToCartesianGrid(Sector sector, int numLat, int numLon, double[] elevations,
                                                 Vec3 origin, FloatBuffer result, int stride) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "BasicGlobe",
                "geographicToCartesianGrid", "Number of latitude or longitude locations is less than one"));
        }

        int numPoints = numLat * numLon;
        if (elevations != null && elevations.length < numPoints) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "BasicGlobe",
                "geographicToCartesianGrid", "missingArray"));
        }

        if (result == null || result.remaining() < numPoints * stride) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingResult"));
        }

        return this.projection.geographicToCartesianGrid(this, sector, numLat, numLon, elevations, origin, null,
            result, stride);
    }

    @Override
    public Position cartesianToGeographic(double x, double y, double z, Position result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cartesianToGeographic", "missingResult"));
        }

        return this.projection.cartesianToGeographic(this, x, y, z, null, result);
    }

    @Override
    public Matrix4 cartesianToLocalTransform(double x, double y, double z, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cartesianToLocalTransform", "missingResult"));
        }

        return this.projection.cartesianToLocalTransform(this, x, y, z, null, result);
    }

    @Override
    public Matrix4 cameraToCartesianTransform(Camera camera, Matrix4 result) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cameraToCartesianTransform", "missingCamera"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cameraToCartesianTransform", "missingResult"));
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

    @Override
    public LookAt cameraToLookAt(Camera camera, LookAt result) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cameraToLookAt", "missingCamera"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "cameraToLookAt", "missingResult"));
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

    @Override
    public Matrix4 lookAtToCartesianTransform(LookAt lookAt, Matrix4 result) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "lookAtToCartesianTransform", "missingLookAt"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "lookAtToCartesianTransform", "missingResult"));
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

    @Override
    public Camera lookAtToCamera(LookAt lookAt, Camera result) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "lookAtToCamera", "missingLookAt"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "lookAtToCamera", "missingResult"));
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

    @Override
    public double horizonDistance(double eyeAltitude) {
        double eye = eyeAltitude;
        double eqr = this.equatorialRadius;

        return Math.sqrt(eye * (2 * eqr + eye));
    }

    @Override
    public double horizonDistance(double eyeAltitude, double objectAltitude) {
        double eye = eyeAltitude;
        double obj = objectAltitude;
        double eqr = this.equatorialRadius;
        double eyeDistance = Math.sqrt(eye * (2 * eqr + eye)); // distance from eye altitude to globe MSL horizon
        double horDistance = Math.sqrt(obj * (2 * eqr + obj)); // distance from object altitude to globe MSL horizon

        return eyeDistance + horDistance; // desired distance is the sum of the two horizon distances
    }

    @Override
    public boolean intersect(Line line, Vec3 result) {
        if (line == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "intersect", "missingLine"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "intersect", "missingResult"));
        }

        return this.projection.intersect(this, line, null, result);
    }
}
