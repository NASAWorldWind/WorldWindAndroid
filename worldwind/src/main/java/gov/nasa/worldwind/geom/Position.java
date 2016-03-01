/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

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
     * Constructs a position from a specified latitude and longitude in degrees and altitude in
     * meters.
     *
     * @param latitude  the latitude in degrees.
     * @param longitude the longitude in degrees.
     * @param altitude  the altitude in meters.
     */
    public Position(double latitude, double longitude, double altitude) {
        super(latitude, longitude);
        this.altitude = altitude;
    }

    /**
     * Constructs a position from a specified latitude and longitude in degrees and altitude in
     * meters.
     *
     * @param latitudeDegrees  the latitude in degrees.
     * @param longitudeDegrees the longitude in degrees.
     * @param altitude         the altitude in meters.
     *
     * @return the new position.
     */
    public static Position fromDegrees(double latitudeDegrees, double longitudeDegrees, double altitude) {
        return new Position(latitudeDegrees, longitudeDegrees, altitude);
    }

    /**
     * Constructs a position from a specified latitude and longitude in radians and altitude in
     * meters.
     *
     * @param latitudeRadians  the latitude in radians.
     * @param longitudeRadians the longitude in radians.
     * @param altitude         the altitude in meters.
     *
     * @return the new position.
     */
    public static Position fromRadians(double latitudeRadians, double longitudeRadians, double altitude) {
        return new Position(latitudeRadians * RADIANS_TO_DEGREES, longitudeRadians * RADIANS_TO_DEGREES, altitude);
    }

    /**
     * Returns a new position with latitude, longitude and altitude all 0.
     *
     * @return a position at 0.
     */
    public static Position zero() {
        return new Position(0, 0, 0);
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
    public String toString() {
        return this.latitude + "\u00b0, " + this.longitude + "\u00b0, " + this.altitude;
    }

    /**
     * Sets this position to a specified latitude and longitude in degrees and altitude in meters.
     *
     * @param latitude  the new latitude in degrees.
     * @param longitude the new longitude in degrees.
     * @param altitude  the new altitude in meters.
     *
     * @return this position.
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
     * @param that the position specifying the new coordinates.
     *
     * @return this position.
     *
     * @throws IllegalArgumentException If the specified position is null.
     */
    public Position set(Position that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "set", "missingPosition"));
        }

        this.latitude = that.latitude;
        this.longitude = that.longitude;
        this.altitude = that.altitude;
        return this;
    }

    public Position interpolateAlongPath(PathType pathType, double amount, Position endPosition, Position result) {
        if (endPosition == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "interpolateAlongPath", "missingPosition"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Position", "interpolateAlongPath", "missingResult"));
        }

        // Interpolate latitude and longitude.
        super.interpolateAlongPath(pathType, amount, endPosition, result);
        // Interpolate altitude.
        result.altitude = (1 - amount) * this.altitude + amount * endPosition.altitude;

        return result;
    }
}
