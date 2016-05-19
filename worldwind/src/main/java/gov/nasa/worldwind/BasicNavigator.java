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

    protected Camera scratchCamera = new Camera();

    public BasicNavigator() {
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public Navigator setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public Navigator setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    @Override
    public double getAltitude() {
        return this.altitude;
    }

    @Override
    public Navigator setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    @Override
    public double getHeading() {
        return this.heading;
    }

    @Override
    public Navigator setHeading(double headingDegrees) {
        this.heading = headingDegrees;
        return this;
    }

    @Override
    public double getTilt() {
        return this.tilt;
    }

    @Override
    public Navigator setTilt(double tiltDegrees) {
        this.tilt = tiltDegrees;
        return this;
    }

    @Override
    public double getRoll() {
        return this.roll;
    }

    @Override
    public Navigator setRoll(double rollDegrees) {
        this.roll = rollDegrees;
        return this;
    }

    @Override
    public Camera getAsCamera(Globe globe, Camera result) {
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
    public Navigator setAsCamera(Globe globe, Camera camera) {
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
    public LookAt getAsLookAt(Globe globe, LookAt result) {
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
    public Navigator setAsLookAt(Globe globe, LookAt lookAt) {
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
