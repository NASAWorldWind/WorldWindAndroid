/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

public class Camera {

    public double latitude;

    public double longitude;

    public double altitude;

    @WorldWind.AltitudeMode
    public int altitudeMode = WorldWind.ABSOLUTE;

    public double heading;

    public double tilt;

    public double roll;

    public Camera() {
    }

    public Camera(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode,
                  double heading, double tilt, double roll) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeMode = altitudeMode;
        this.heading = heading;
        this.tilt = tilt;
        this.roll = roll;
    }

    public Camera(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Camera", "constructor", "missingCamera"));
        }

        this.latitude = camera.latitude;
        this.longitude = camera.longitude;
        this.altitude = camera.altitude;
        this.altitudeMode = camera.altitudeMode;
        this.heading = camera.heading;
        this.tilt = camera.tilt;
        this.roll = camera.roll;
    }

    public Camera set(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode,
                      double heading, double tilt, double roll) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeMode = altitudeMode;
        this.heading = heading;
        this.tilt = tilt;
        this.roll = roll;

        return this;
    }

    public Camera set(Camera camera) {
        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Camera", "set", "missingCamera"));
        }

        this.latitude = camera.latitude;
        this.longitude = camera.longitude;
        this.altitude = camera.altitude;
        this.altitudeMode = camera.altitudeMode;
        this.heading = camera.heading;
        this.tilt = camera.tilt;
        this.roll = camera.roll;

        return this;
    }

    @Override
    public String toString() {
        return "Camera{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ", altitude=" + altitude +
            ", altitudeMode=" + altitudeMode +
            ", heading=" + heading +
            ", tilt=" + tilt +
            ", roll=" + roll +
            '}';
    }
}
