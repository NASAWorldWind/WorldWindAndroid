/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import androidx.annotation.NonNull;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

public class Camera {

    public final Position position = new Position();

    @WorldWind.AltitudeMode
    public int altitudeMode = WorldWind.ABSOLUTE;

    public double heading;

    public double tilt;

    public double roll;

    private double fieldOfView = 45;

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

}
