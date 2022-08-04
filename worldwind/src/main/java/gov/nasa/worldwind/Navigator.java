/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

@Deprecated
public class Navigator {

    private final Camera camera;

    public Navigator(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Navigator", "constructor", "missingCamera"));
        }

        this.camera = camera;
    }

    public double getLatitude() {
        return this.camera.position.latitude;
    }

    public Navigator setLatitude(double latitude) {
        this.camera.position.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return this.camera.position.longitude;
    }

    public Navigator setLongitude(double longitude) {
        this.camera.position.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return this.camera.position.altitude;
    }

    public Navigator setAltitude(double altitude) {
        this.camera.position.altitude = altitude;
        return this;
    }

    public double getHeading() {
        return this.camera.heading;
    }

    public Navigator setHeading(double headingDegrees) {
        this.camera.heading = headingDegrees;
        return this;
    }

    public double getTilt() {
        return this.camera.tilt;
    }

    public Navigator setTilt(double tiltDegrees) {
        this.camera.tilt = tiltDegrees;
        return this;
    }

    public double getRoll() {
        return this.camera.roll;
    }

    public Navigator setRoll(double rollDegrees) {
        this.camera.roll = rollDegrees;
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

        result.set(this.camera);

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

        this.camera.set(camera);

        return this;
    }

    public LookAt getAsLookAt(Globe globe, double verticalExaggeration, Position terrainPosition, LookAt result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingResult"));
        }

        this.camera.getAsLookAt(globe, verticalExaggeration, terrainPosition, result);

        return result;
    }

    public Navigator setAsLookAt(Globe globe, double verticalExaggeration, LookAt lookAt) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsLookAt", "missingGlobe"));
        }

        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsLookAt", "missingLookAt"));
        }

        this.camera.setFromLookAt(globe, verticalExaggeration, lookAt);

        return this;
    }

    public Matrix4 getAsViewingMatrix(Globe globe, double verticalExaggeration, Matrix4 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsViewingMatrix", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsViewingMatrix", "missingResult"));
        }

        this.camera.computeViewingTransform(globe, verticalExaggeration, result);

        return result;
    }

}
