/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

public class Navigator {

    protected double latitude;

    protected double longitude;

    protected double altitude;

    protected double heading;

    protected double tilt;

    protected double roll;

    private final Camera scratchCamera = new Camera();

    private final Matrix4 modelview = new Matrix4();

    private final Matrix4 origin = new Matrix4();

    private final Vec3 originPoint = new Vec3();

    private final Position originPos = new Position();

    private final Line forwardRay = new Line();

    private final WorldWindow wwd;

    private final static double COLLISION_THRESHOLD = 10.0; // 10m above surface

    public Navigator(WorldWindow wwd) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Navigator", "constructor", "missingWorldWindow"));
        }

        this.wwd = wwd;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public Navigator setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public Navigator setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return this.altitude;
    }

    public Navigator setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public double getHeading() {
        return this.heading;
    }

    public Navigator setHeading(double headingDegrees) {
        this.heading = headingDegrees;
        return this;
    }

    public double getTilt() {
        return this.tilt;
    }

    public Navigator setTilt(double tiltDegrees) {
        this.tilt = tiltDegrees;
        return this;
    }

    public double getRoll() {
        return this.roll;
    }

    public Navigator setRoll(double rollDegrees) {
        this.roll = rollDegrees;
        return this;
    }

    public Camera getAsCamera(Globe globe, Camera result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsCamera", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsCamera", "missingResult"));
        }

        result.latitude = this.latitude;
        result.longitude = this.longitude;
        result.altitude = this.altitude;
        result.altitudeMode = WorldWind.ABSOLUTE;
        result.heading = this.heading;
        result.tilt = this.tilt;
        result.roll = this.roll;

        return result;
    }

    public Navigator setAsCamera(Globe globe, Camera camera) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsCamera", "missingGlobe"));
        }

        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsCamera", "missingCamera"));
        }

        this.latitude = camera.latitude;
        this.longitude = camera.longitude;
        this.altitude = camera.altitude; // TODO interpret altitude modes other than absolute
        this.heading = camera.heading;
        this.tilt = camera.tilt;
        this.roll = camera.roll;

        return this;
    }

    public LookAt getAsLookAt(Globe globe, LookAt result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingResult"));
        }

        this.getAsCamera(globe, this.scratchCamera); // get this navigator's properties as a Camera
        this.cameraToLookAt(globe, this.scratchCamera, result); // convert the Camera to a LookAt

        return result;
    }

    public Navigator setAsLookAt(Globe globe, LookAt lookAt) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsLookAt", "missingGlobe"));
        }

        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsLookAt", "missingLookAt"));
        }

        this.lookAtToCamera(globe, lookAt, this.scratchCamera); // convert the LookAt to a Camera
        this.setAsCamera(globe, this.scratchCamera); // set this navigator's properties as a Camera

        return this;
    }

    public Matrix4 getAsViewingMatrix(Globe globe, Matrix4 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsViewingMatrix", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsViewingMatrix", "missingResult"));
        }

        this.getAsCamera(globe, this.scratchCamera); // get this navigator's properties as a Camera
        this.cameraToViewingMatrix(globe, this.scratchCamera, result); // convert the Camera to a viewing matrix

        return result;
    }

    protected LookAt cameraToLookAt(Globe globe, Camera camera, LookAt result) {
        this.cameraToViewingMatrix(globe, camera, this.modelview);

        // Pick terrain located behind the viewport center point
        PickedObject terrainPickedObject = wwd.pick(wwd.getViewport().width / 2f, wwd.getViewport().height / 2f).terrainPickedObject();
        if (terrainPickedObject != null) {
            // Use picked terrain position including approximate rendered altitude
            this.originPos.set(terrainPickedObject.getTerrainPosition());
            globe.geographicToCartesian(this.originPos.latitude, this.originPos.longitude, this.originPos.altitude, this.originPoint);
            globe.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);
        } else {
            // Center is outside the globe - use point on horizon
            this.modelview.extractEyePoint(this.forwardRay.origin);
            this.modelview.extractForwardVector(this.forwardRay.direction);
            this.forwardRay.pointAt(globe.horizonDistance(camera.altitude), this.originPoint);
            globe.cartesianToGeographic(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.originPos);
            globe.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);
        }

        this.modelview.multiplyByMatrix(this.origin);

        result.latitude = this.originPos.latitude;
        result.longitude = this.originPos.longitude;
        result.altitude = this.originPos.altitude;
        result.range = -this.modelview.m[11];
        result.heading = this.modelview.extractHeading(camera.roll); // disambiguate heading and roll
        result.tilt = this.modelview.extractTilt();
        result.roll = camera.roll; // roll passes straight through

        return result;
    }

    protected Matrix4 cameraToViewingMatrix(Globe globe, Camera camera, Matrix4 result) {
        // TODO interpret altitude mode other than absolute
        // Transform by the local cartesian transform at the camera's position.
        globe.geographicToCartesianTransform(camera.latitude, camera.longitude, camera.altitude, result);

        // Transform by the heading, tilt and roll.
        result.multiplyByRotation(0, 0, 1, -camera.heading); // rotate clockwise about the Z axis
        result.multiplyByRotation(1, 0, 0, camera.tilt); // rotate counter-clockwise about the X axis
        result.multiplyByRotation(0, 0, 1, camera.roll); // rotate counter-clockwise about the Z axis (again)

        // Make the transform a viewing matrix.
        result.invertOrthonormal();

        return result;
    }

    protected Camera lookAtToCamera(Globe globe, LookAt lookAt, Camera result) {
        this.lookAtToViewingTransform(globe, lookAt, this.modelview);
        this.modelview.extractEyePoint(this.originPoint);

        globe.cartesianToGeographic(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.originPos);
        globe.cartesianToLocalTransform(this.originPoint.x, this.originPoint.y, this.originPoint.z, this.origin);
        this.modelview.multiplyByMatrix(this.origin);

        result.latitude = this.originPos.latitude;
        result.longitude = this.originPos.longitude;
        result.altitude = this.originPos.altitude;
        result.heading = this.modelview.extractHeading(lookAt.roll); // disambiguate heading and roll
        result.tilt = this.modelview.extractTilt();
        result.roll = lookAt.roll; // roll passes straight through

        // Check if camera altitude is not under the surface
        double elevation = globe.getElevationAtLocation(result.latitude, result.longitude) * wwd.getVerticalExaggeration() + COLLISION_THRESHOLD;
        if(elevation > result.altitude) {
            // Set camera altitude above the surface
            result.altitude = elevation;
            // Compute new camera point
            globe.geographicToCartesian(result.latitude, result.longitude, result.altitude, originPoint);
            // Compute look at point
            globe.geographicToCartesian(lookAt.latitude, lookAt.longitude, lookAt.altitude, forwardRay.origin);
            // Compute normal to globe in look at point
            globe.geographicToCartesianNormal(lookAt.latitude, lookAt.longitude, forwardRay.direction);
            // Calculate tilt angle between new camera point and look at point
            originPoint.subtract(forwardRay.origin).normalize();
            double dot = forwardRay.direction.dot(originPoint);
            if (dot >= -1 || dot <= 1) {
                result.tilt = Math.toDegrees(Math.acos(dot));
            }
        }

        return result;
    }

    protected Matrix4 lookAtToViewingTransform(Globe globe, LookAt lookAt, Matrix4 result) {
        // TODO interpret altitude mode other than absolute
        // Transform by the local cartesian transform at the look-at's position.
        globe.geographicToCartesianTransform(lookAt.latitude, lookAt.longitude, lookAt.altitude, result);

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
}
