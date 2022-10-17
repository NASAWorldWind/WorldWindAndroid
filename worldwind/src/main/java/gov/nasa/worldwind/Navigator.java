/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

@Deprecated
public class Navigator {

    private final WorldWindow wwd;

    public Navigator(WorldWindow wwd) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Navigator", "constructor", "missingWorldwindow"));
        }

        this.wwd = wwd;
    }

    public double getLatitude() {
        return this.wwd.camera.position.latitude;
    }

    public Navigator setLatitude(double latitude) {
        this.wwd.camera.position.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return this.wwd.camera.position.longitude;
    }

    public Navigator setLongitude(double longitude) {
        this.wwd.camera.position.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return this.wwd.camera.position.altitude;
    }

    public Navigator setAltitude(double altitude) {
        this.wwd.camera.position.altitude = altitude;
        return this;
    }

    public double getHeading() {
        return this.wwd.camera.heading;
    }

    public Navigator setHeading(double headingDegrees) {
        this.wwd.camera.heading = headingDegrees;
        return this;
    }

    public double getTilt() {
        return this.wwd.camera.tilt;
    }

    public Navigator setTilt(double tiltDegrees) {
        this.wwd.camera.tilt = tiltDegrees;
        return this;
    }

    public double getRoll() {
        return this.wwd.camera.roll;
    }

    public Navigator setRoll(double rollDegrees) {
        this.wwd.camera.roll = rollDegrees;
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

        result.set(this.wwd.camera);

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

        this.wwd.camera.set(camera);

        return this;
    }

    public LookAt getAsLookAt(Globe globe, LookAt result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingResult"));
        }

        this.wwd.cameraAsLookAt(result);

        return result;
    }

    public Navigator setAsLookAt(Globe globe, LookAt lookAt) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "setAsLookAt", "missingLookAt"));
        }

        this.wwd.cameraFromLookAt(lookAt);

        return this;
    }

    public Matrix4 getAsViewingMatrix(Globe globe, Matrix4 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsViewingMatrix", "missingResult"));
        }

        this.wwd.cameraToViewingTransform(result);

        return result;
    }

}
