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

    @WorldWind.AltitudeMode
    public int altitudeMode = WorldWind.ABSOLUTE;

    public double range;

    public double heading;

    public double tilt;

    public double roll;

    public LookAt() {
    }

    public LookAt(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, double range,
                  double heading, double tilt, double roll) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeMode = altitudeMode;
        this.range = range;
        this.heading = heading;
        this.tilt = tilt;
        this.roll = roll;
    }

    public LookAt(LookAt lookAt) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LookAt", "constructor", "missingLookAt"));
        }

        this.latitude = lookAt.latitude;
        this.longitude = lookAt.longitude;
        this.altitude = lookAt.altitude;
        this.altitudeMode = lookAt.altitudeMode;
        this.range = lookAt.range;
        this.heading = lookAt.heading;
        this.tilt = lookAt.tilt;
        this.roll = lookAt.roll;
    }

    public LookAt set(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, double range,
                      double heading, double tilt, double roll) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.altitudeMode = altitudeMode;
        this.range = range;
        this.heading = heading;
        this.tilt = tilt;
        this.roll = roll;

        return this;
    }

    public LookAt set(LookAt lookAt) {
        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LookAt", "set", "missingLookAt"));
        }

        this.latitude = lookAt.latitude;
        this.longitude = lookAt.longitude;
        this.altitude = lookAt.altitude;
        this.altitudeMode = lookAt.altitudeMode;
        this.range = lookAt.range;
        this.heading = lookAt.heading;
        this.tilt = lookAt.tilt;
        this.roll = lookAt.roll;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LookAt lookAt = (LookAt) o;

        if (Double.compare(lookAt.latitude, latitude) != 0) return false;
        if (Double.compare(lookAt.longitude, longitude) != 0) return false;
        if (Double.compare(lookAt.altitude, altitude) != 0) return false;
        if (altitudeMode != lookAt.altitudeMode) return false;
        if (Double.compare(lookAt.range, range) != 0) return false;
        if (Double.compare(lookAt.heading, heading) != 0) return false;
        if (Double.compare(lookAt.tilt, tilt) != 0) return false;
        return Double.compare(lookAt.roll, roll) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(altitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + altitudeMode;
        temp = Double.doubleToLongBits(range);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(heading);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(tilt);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(roll);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LookAt{" +
            "latitude=" + latitude +
            ", longitude=" + longitude +
            ", altitude=" + altitude +
            ", altitudeMode=" + altitudeMode +
            ", range=" + range +
            ", heading=" + heading +
            ", tilt=" + tilt +
            ", roll=" + roll +
            '}';
    }

}
