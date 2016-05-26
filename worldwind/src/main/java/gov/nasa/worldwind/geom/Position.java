/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

/**
 * Geographic position with a latitude and longitude in degrees and altitude in meters.
 */
public class Position extends Location {

    /**
     * The position's altitude in meters.
     */
    public double altitude;

    /**
     * Constructs a position with latitude, longitude and altitude all 0.
     */
    public Position() {
    }

    /**
     * Constructs a position with a specified latitude and longitude in degrees and altitude in meters.
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     * @param altitude  the altitude in meters
     */
    public Position(double latitude, double longitude, double altitude) {
        super(latitude, longitude);
        this.altitude = altitude;
    }

    /**
     * Constructs a position with the latitude, longitude and altitude of a specified position.
     *
     * @param position the position specifying the coordinates
     *
     * @throws IllegalArgumentException If the position is null
     */
    public Position(Position position) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "constructor", "missingPosition"));
        }

        this.latitude = position.latitude;
        this.longitude = position.longitude;
        this.altitude = position.altitude;
    }

    /**
     * Constructs a position with a specified latitude and longitude in degrees and altitude in meters.
     *
     * @param latitudeDegrees  the latitude in degrees
     * @param longitudeDegrees the longitude in degrees
     * @param altitude         the altitude in meters
     *
     * @return the new position
     */
    public static Position fromDegrees(double latitudeDegrees, double longitudeDegrees, double altitude) {
        Position pos = new Position();
        pos.latitude = latitudeDegrees;
        pos.longitude = longitudeDegrees;
        pos.altitude = altitude;
        return pos;
    }

    /**
     * Constructs a position with a specified latitude and longitude in radians and altitude in meters.
     *
     * @param latitudeRadians  the latitude in radians
     * @param longitudeRadians the longitude in radians
     * @param altitude         the altitude in meters
     *
     * @return the new position
     */
    public static Position fromRadians(double latitudeRadians, double longitudeRadians, double altitude) {
        Position pos = new Position();
        pos.latitude = Math.toDegrees(latitudeRadians);
        pos.longitude = Math.toDegrees(longitudeRadians);
        pos.altitude = altitude;
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Position that = (Position) o;
        return this.latitude == that.latitude
            && this.longitude == that.longitude
            && this.altitude == that.altitude;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(altitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return this.latitude + "\u00b0, " + this.longitude + "\u00b0, " + this.altitude;
    }

    /**
     * Sets this position to a specified latitude and longitude in degrees and altitude in meters.
     *
     * @param latitude  the new latitude in degrees
     * @param longitude the new longitude in degrees
     * @param altitude  the new altitude in meters
     *
     * @return this position with its latitude, longitude and altitude set to the specified values
     */
    public Position set(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        return this;
    }

    /**
     * Sets this position to the latitude, longitude and altitude of a specified position.
     *
     * @param position the position specifying the new coordinates
     *
     * @return this position with its latitude, longitude and altitude set to that of the specified position
     *
     * @throws IllegalArgumentException If the position is null
     */
    public Position set(Position position) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "set", "missingPosition"));
        }

        this.latitude = position.latitude;
        this.longitude = position.longitude;
        this.altitude = position.altitude;
        return this;
    }

    /**
     * Compute a position along a path between two positions. The amount indicates the fraction of the path at which to
     * compute a position. This value is typically between 0 and 1, where 0 indicates the begin position (this position)
     * and 1 indicates the end position.
     *
     * @param endPosition the path's end position
     * @param pathType    {@link gov.nasa.worldwind.WorldWind.PathType} indicating type of path to assume
     * @param amount      the fraction of the path at which to compute a position
     * @param result      a pre-allocated Position in which to return the computed result
     *
     * @return the result argument set to the computed position
     *
     * @throws IllegalArgumentException If either of the end position or the result argument is null
     */
    public Position interpolateAlongPath(Position endPosition, @WorldWind.PathType int pathType, double amount,
                                         Position result) {
        if (endPosition == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "interpolateAlongPath", "missingPosition"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "interpolateAlongPath", "missingResult"));
        }

        // Interpolate latitude and longitude.
        super.interpolateAlongPath(endPosition, pathType, amount, result);
        // Interpolate altitude.
        result.altitude = (1 - amount) * this.altitude + amount * endPosition.altitude;

        return result;
    }
}
