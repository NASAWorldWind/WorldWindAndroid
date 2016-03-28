/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a line in Cartesian coordinates.
 */
public class Line {

    /**
     * This line's origin.
     */
    public final Vec3 origin = new Vec3();

    /**
     * This line's direction.
     */
    public final Vec3 direction = new Vec3();

    /**
     * Constructs a line with origin and direction both zero.
     */
    public Line() {
    }

    /**
     * Constructs a line with a specified origin and direction.
     *
     * @param origin    the line's origin
     * @param direction the line's direction
     *
     * @throws IllegalArgumentException If either the origin or the direction are null
     */
    public Line(Vec3 origin, Vec3 direction) {
        if (origin == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "constructor", "The origin is null"));
        }

        if (direction == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "constructor", "The direction is null"));
        }

        this.origin.set(origin);
        this.direction.set(direction);
    }

    /**
     * Constructs a line with the origin and direction from a specified line.
     *
     * @param line the line specifying origin and direction
     *
     * @throws IllegalArgumentException If the line is null
     */
    public Line(Line line) {
        if (line == null) {
            throw new IllegalArgumentException(Logger.logMessage(
                Logger.ERROR, "Line", "constructor", "missingLine"));
        }

        this.origin.set(line.origin);
        this.direction.set(line.direction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Line that = (Line) o;
        return this.origin.equals(that.origin) && this.direction.equals(that.direction);

    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "origin=[" + origin + "], direction=[" + direction + ']';
    }

    /**
     * Sets this line to a specified origin and direction.
     *
     * @param origin    the line's new origin
     * @param direction the line's new direction
     *
     * @return this line, set to the new origin and direction
     *
     * @throws IllegalArgumentException If either the origin or the direction are null
     */
    public Line set(Vec3 origin, Vec3 direction) {
        if (origin == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "set", "The origin is null"));
        }

        if (direction == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "set", "The direction is null"));
        }

        this.origin.set(origin);
        this.direction.set(direction);

        return this;
    }

    /**
     * Sets this line to the specified segment. This line has its origin at the first endpoint and its direction
     * extending from the first endpoint to the second.
     *
     * @param pointA the segment's first endpoint
     * @param pointB the segment's second endpoint
     *
     * @return this line, set to the specified segment
     *
     * @throws IllegalArgumentException If either endpoint is null
     */
    public Line setToSegment(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "setToSegment", "missingVector"));
        }

        this.origin.set(pointA);
        this.direction.set(pointB.x - pointA.x, pointB.y - pointA.y, pointB.z - pointA.z);

        return this;
    }

    /**
     * Computes a Cartesian point a specified distance along this line.
     *
     * @param distance The distance from this line's origin at which to compute the point.
     * @param result   A pre-allocated {@link Vec3} instance in which to return the computed point.
     *
     * @return The specified result argument containing the computed point.
     *
     * @throws IllegalArgumentException If the specified result argument is null or undefined.
     */
    public Vec3 pointAt(double distance, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "pointAt", "missingResult"));
        }

        result.x = this.origin.x + this.direction.x * distance;
        result.y = this.origin.y + this.direction.y * distance;
        result.z = this.origin.z + this.direction.z * distance;

        return result;
    }
}
