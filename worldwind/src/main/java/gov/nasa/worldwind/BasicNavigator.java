/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

public class BasicNavigator implements Navigator {

    protected double latitude;

    protected double longitude;

    protected double altitude;

    protected double heading;

    protected double tilt;

    protected double roll;

    protected double fieldOfView = 45;

    protected Camera scratchCamera = new Camera();

    public BasicNavigator() {
    }

    // TODO remove method level synchronization
    @Override
    public synchronized double getLatitude() {
        return this.latitude;
    }

    @Override
    public synchronized Navigator setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    @Override
    public synchronized double getLongitude() {
        return this.longitude;
    }

    @Override
    public synchronized Navigator setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    @Override
    public synchronized double getAltitude() {
        return this.altitude;
    }

    @Override
    public synchronized Navigator setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    @Override
    public synchronized double getHeading() {
        return this.heading;
    }

    @Override
    public synchronized Navigator setHeading(double headingDegrees) {
        this.heading = headingDegrees;
        return this;
    }

    @Override
    public synchronized double getTilt() {
        return this.tilt;
    }

    @Override
    public synchronized Navigator setTilt(double tiltDegrees) {
        this.tilt = tiltDegrees;
        return this;
    }

    @Override
    public synchronized double getRoll() {
        return this.roll;
    }

    @Override
    public synchronized Navigator setRoll(double rollDegrees) {
        this.roll = rollDegrees;
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

        result.latitude = this.latitude;
        result.longitude = this.longitude;
        result.altitude = this.altitude;
        result.altitudeMode = WorldWind.ABSOLUTE;
        result.heading = this.heading;
        result.tilt = this.tilt;
        result.roll = this.roll;

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

        this.latitude = camera.latitude;
        this.longitude = camera.longitude;
        this.altitude = camera.altitude; // TODO interpret altitude modes other than absolute
        this.heading = camera.heading;
        this.tilt = camera.tilt;
        this.roll = camera.roll;

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

        this.getAsCamera(globe, this.scratchCamera); // get this navigator's properties as a Camera
        globe.cameraToLookAt(this.scratchCamera, result); // convert the Camera to a LookAt

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

        globe.lookAtToCamera(lookAt, this.scratchCamera); // convert the LookAt to a Camera
        this.setAsCamera(globe, this.scratchCamera); // set this navigator's properties as a Camera

        return this;
    }
}
