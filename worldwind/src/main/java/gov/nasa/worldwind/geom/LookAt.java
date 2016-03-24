/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

public class LookAt {

    public double latitude;

    public double longitude;

    public double altitude;

    public int altitudeMode = WorldWind.ABSOLUTE;

    public double range;

    public double heading;

    public double tilt;

    public LookAt() {
    }

    public LookAt(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, double range, double heading, double tilt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeMode = altitudeMode;
        this.range = range;
        this.heading = heading;
        this.tilt = tilt;
    }

    public LookAt set(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, double range, double heading, double tilt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeMode = altitudeMode;
        this.range = range;
        this.heading = heading;
        this.tilt = tilt;

        return this;
    }

    public LookAt set(LookAt lookAt) {
        if (lookAt == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "LookAt", "set", "missingLookAt"));
        }

        this.latitude = lookAt.latitude;
        this.longitude = lookAt.longitude;
        this.altitude = lookAt.altitude;
        this.altitudeMode = lookAt.altitudeMode;
        this.range = lookAt.range;
        this.heading = lookAt.heading;
        this.tilt = lookAt.tilt;

        return this;
    }
}
