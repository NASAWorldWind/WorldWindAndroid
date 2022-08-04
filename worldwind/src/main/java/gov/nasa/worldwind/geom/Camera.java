/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import androidx.annotation.NonNull;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

public class Camera {

    private final static double COLLISION_THRESHOLD = 10.0; // 10m above surface

    public final Position position = new Position();

    @WorldWind.AltitudeMode
    public int altitudeMode = WorldWind.ABSOLUTE;

    public double heading;

    public double tilt;

    public double roll;

    private double fieldOfView = 45;

    private final Matrix4 modelview = new Matrix4();

    private final Matrix4 origin = new Matrix4();

    private final Vec3 originPoint = new Vec3();

    private final Position originPos = new Position();

    private final Line forwardRay = new Line();

    public Camera set(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode,
                      double heading, double tilt, double roll) {
        this.position.set(latitude, longitude, altitude);
        this.altitudeMode = altitudeMode;
        this.heading = heading;
        this.tilt = tilt;
        this.roll = roll;

        return this;
    }

    public void setFieldOfView(double fovyDegrees) {
        if (fovyDegrees <= 0 || fovyDegrees >= 180) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "WorldWindow", "setFieldOfView", "invalidFieldOfView"));
        }

        this.fieldOfView = fovyDegrees;
    }

    public double getFieldOfView() {
        return this.fieldOfView;
    }

    public Camera set(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Camera", "set", "missingCamera"));
        }

        this.position.set(camera.position);
        this.altitudeMode = camera.altitudeMode;
        this.heading = camera.heading;
        this.tilt = camera.tilt;
        this.roll = camera.roll;
        this.fieldOfView = camera.fieldOfView;

        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Camera{" +
            "latitude=" + position.latitude +
            ", longitude=" + position.longitude +
            ", altitude=" + position.altitude +
            ", altitudeMode=" + altitudeMode +
            ", heading=" + heading +
            ", tilt=" + tilt +
            ", roll=" + roll +
            '}';
    }

    public Matrix4 computeViewingTransform(Globe globe, double verticalExaggeration, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Camera", "computeViewingTransform", "missingResult"));
        }

        // Transform by the local cartesian transform at the camera's position.
        this.geographicToCartesianTransform(globe, verticalExaggeration, this.position, this.altitudeMode, result);

        // Transform by the heading, tilt and roll.
        result.multiplyByRotation(0, 0, 1, -this.heading); // rotate clockwise about the Z axis
        result.multiplyByRotation(1, 0, 0, this.tilt); // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0, 0, 1, this.roll); // rotate counter-clockwise about the Z axis (again)

        // Make the transform a viewing matrix.
        result.invertOrthonormal();

        return result;
    }

    public LookAt getAsLookAt(Globe globe, double verticalExaggeration, Position terrainPosition, LookAt result) {
        this.computeViewingTransform(globe, verticalExaggeration, this.modelview);

        if (terrainPosition != null) {
            // Use picked terrain position including approximate rendered altitude
            this.originPos.set(terrainPosition);
            globe.geographicToCartesian(this.originPos.latitude, this.originPos.longitude, this.originPos.altitude, this.originPoint);
        } else {
            // Center is outside the globe - use point on horizon
            this.modelview.extractEyePoint(this.forwardRay.origin);
            this.modelview.extractForwardVector(this.forwardRay.direction);
            this.forwardRay.pointAt(globe.horizonDistance(this.position.altitude), this.originPoint);
            globe.cartesianToGeographic(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.originPos);
        }
        globe.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);

        this.modelview.multiplyByMatrix(this.origin);

        result.position.set(this.originPos);
        result.range = -this.modelview.m[11];
        result.heading = this.modelview.extractHeading(this.roll); // disambiguate heading and roll
        result.tilt = this.modelview.extractTilt();
        result.roll = this.roll; // roll passes straight through

        return result;
    }

    public Camera setFromLookAt(Globe globe, double verticalExaggeration, LookAt lookAt) {
        this.lookAtToViewingTransform(globe, verticalExaggeration, lookAt, this.modelview);
        this.modelview.extractEyePoint(this.originPoint);

        globe.cartesianToGeographic(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.originPos);
        globe.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);
        this.modelview.multiplyByMatrix(this.origin);

        this.position.set(this.originPos);
        this.altitudeMode = WorldWind.ABSOLUTE; // Calculated position is absolute
        this.heading = this.modelview.extractHeading(lookAt.roll); // disambiguate heading and roll
        this.tilt = this.modelview.extractTilt();
        this.roll = lookAt.roll; // roll passes straight through

        // Check if camera altitude is not under the surface
        double elevation = globe.getElevationAtLocation(this.position.latitude, this.position.longitude) * verticalExaggeration + COLLISION_THRESHOLD;
        if(elevation > this.position.altitude) {
            // Set camera altitude above the surface
            this.position.altitude = elevation;
            // Compute new camera point
            globe.geographicToCartesian(this.position.latitude, this.position.longitude, this.position.altitude, originPoint);
            // Compute look at point
            globe.geographicToCartesian(lookAt.position.latitude, lookAt.position.longitude, lookAt.position.altitude, forwardRay.origin);
            // Compute normal to globe in look at point
            globe.geographicToCartesianNormal(lookAt.position.latitude, lookAt.position.longitude, forwardRay.direction);
            // Calculate tilt angle between new camera point and look at point
            originPoint.subtract(forwardRay.origin).normalize();
            double dot = forwardRay.direction.dot(originPoint);
            if (dot >= -1 || dot <= 1) {
                this.tilt = Math.toDegrees(Math.acos(dot));
            }
        }

        return this;
    }

    protected Matrix4 lookAtToViewingTransform(Globe globe, double verticalExaggeration, LookAt lookAt, Matrix4 result) {
        // Transform by the local cartesian transform at the look-at's position.
        this.geographicToCartesianTransform(globe, verticalExaggeration, lookAt.position, lookAt.altitudeMode, result);

        // Transform by the heading and tilt.
        result.multiplyByRotation(0, 0, 1, -lookAt.heading); // rotate clockwise about the Z axis
        result.multiplyByRotation(1, 0, 0, lookAt.tilt); // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0, 0, 1, lookAt.roll); // rotate counter-clockwise about the Z axis (again)

        // Transform by the range.
        result.multiplyByTranslation(0, 0, lookAt.range);

        // Make the transform a viewing matrix.
        result.invertOrthonormal();

        return result;
    }

    protected void geographicToCartesianTransform(Globe globe, double verticalExaggeration, Position position,
                                                  @WorldWind.AltitudeMode int altitudeMode, Matrix4 result) {
        switch (altitudeMode) {
            case WorldWind.ABSOLUTE:
                globe.geographicToCartesianTransform(
                    position.latitude, position.longitude, position.altitude, result);
                break;
            case WorldWind.CLAMP_TO_GROUND:
                globe.geographicToCartesianTransform(
                    position.latitude, position.longitude, globe.getElevationAtLocation(
                            position.latitude, position.longitude) * verticalExaggeration, result);
                break;
            case WorldWind.RELATIVE_TO_GROUND:
                globe.geographicToCartesianTransform(
                    position.latitude, position.longitude, (position.altitude + globe.getElevationAtLocation(
                            position.latitude, position.longitude)) * verticalExaggeration, result);
                break;
        }
    }

}
