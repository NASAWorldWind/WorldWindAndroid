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
                    Logger.logMessage(Logger.ERROR, "Navigator", "constructor", "missingWorldWindow"));
        }

        this.wwd = wwd;
    }

    public double getLatitude() {
        return this.wwd.getCamera().position.latitude;
    }

    public Navigator setLatitude(double latitude) {
        this.wwd.getCamera().position.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return this.wwd.getCamera().position.longitude;
    }

    public Navigator setLongitude(double longitude) {
        this.wwd.getCamera().position.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return this.wwd.getCamera().position.altitude;
    }

    public Navigator setAltitude(double altitude) {
        this.wwd.getCamera().position.altitude = altitude;
        return this;
    }

    public double getHeading() {
        return this.wwd.getCamera().heading;
    }

    public Navigator setHeading(double headingDegrees) {
        this.wwd.getCamera().heading = headingDegrees;
        return this;
    }

    public double getTilt() {
        return this.wwd.getCamera().tilt;
    }

    public Navigator setTilt(double tiltDegrees) {
        this.wwd.getCamera().tilt = tiltDegrees;
        return this;
    }

    public double getRoll() {
        return this.wwd.getCamera().roll;
    }

    public Navigator setRoll(double rollDegrees) {
        this.wwd.getCamera().roll = rollDegrees;
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
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Navigator", "getAsLookAt", "missingResult"));
        }

        this.wwd.getCamera().getAsLookAt(result);

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

        this.wwd.getCamera().setFromLookAt(lookAt);

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

        this.wwd.getCamera().computeViewingTransform(result);

        return result;
    }

}
