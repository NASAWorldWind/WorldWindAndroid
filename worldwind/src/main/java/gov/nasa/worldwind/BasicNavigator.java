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

public class BasicNavigator implements Navigator {

    protected Camera camera = new Camera();

    protected double fieldOfView = 45;

    public BasicNavigator() {
    }

    @Override
    public synchronized double getLatitude() {
        return this.camera.latitude;
    }

    @Override
    public synchronized Navigator setLatitude(double latitude) {
        this.camera.latitude = latitude;
        return this;
    }

    @Override
    public synchronized double getLongitude() {
        return this.camera.longitude;
    }

    @Override
    public synchronized Navigator setLongitude(double longitude) {
        this.camera.longitude = longitude;
        return this;
    }

    @Override
    public synchronized double getAltitude() {
        return this.camera.altitude;
    }

    @Override
    public synchronized Navigator setAltitude(double altitude) {
        this.camera.altitude = altitude;
        return this;
    }

    @Override
    public synchronized double getHeading() {
        return this.camera.heading;
    }

    @Override
    public synchronized Navigator setHeading(double headingDegrees) {
        this.camera.heading = headingDegrees;
        return this;
    }

    @Override
    public synchronized double getTilt() {
        return this.camera.tilt;
    }

    @Override
    public synchronized Navigator setTilt(double tiltDegrees) {
        this.camera.tilt = tiltDegrees;
        return this;
    }

    @Override
    public synchronized double getRoll() {
        return this.camera.roll;
    }

    @Override
    public synchronized Navigator setRoll(double rollDegrees) {
        this.camera.roll = rollDegrees;
        return this;
    }

    @Override
    public synchronized double getFieldOfView() {
        return fieldOfView;
    }

    @Override
    public synchronized Navigator setFieldOfView(double fovyDegrees) {
        if (fovyDegrees <= 0 || fovyDegrees >= 180) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setPosition", "invalidFieldOfView"));
        }

        this.fieldOfView = fovyDegrees;
        return this;
    }

    @Override
    public synchronized Camera getAsCamera(Globe globe, Camera result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingResult"));
        }

        result.set(this.camera);

        return result;
    }

    @Override
    public synchronized Navigator setAsCamera(Globe globe, Camera camera) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingGlobe"));
        }

        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingCamera"));
        }

        this.camera.set(camera); // TODO interpret altitude modes other than absolute

        return this;
    }

    @Override
    public synchronized LookAt getAsLookAt(Globe globe, LookAt result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsLookAt", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsLookAt", "missingResult"));
        }

        globe.cameraToLookAt(this.camera, result);

        return result;
    }

    @Override
    public synchronized Navigator setAsLookAt(Globe globe, LookAt lookAt) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsLookAt", "missingGlobe"));
        }

        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsLookAt", "missingLookAt"));
        }

        globe.lookAtToCamera(lookAt, this.camera); // TODO convert altitudeMode to absolute if necessary

        return this;
    }
}
