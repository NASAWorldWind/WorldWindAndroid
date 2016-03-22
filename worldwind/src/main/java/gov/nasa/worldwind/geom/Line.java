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
    Vec3 origin;

    /**
     * This line's direction.
     */
    Vec3 direction;

    /**
     * Constructs a line from a specified origin and direction.
     *
     * @param origin    The line's origin.
     * @param direction The line's direction.
     *
     * @throws IllegalArgumentException If either the origin or the direction are null or undefined.
     */
    public Line(Vec3 origin, Vec3 direction) {
        if (origin == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "constructor",
                    "Origin is null or undefined."));
        }

        if (direction == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "constructor",
                    "Direction is null or undefined."));
        }

        this.origin = origin;
        this.direction = direction;
    }


    /**
     * Creates a line given two specified endpoints.
     *
     * @param pointA The first endpoint.
     * @param pointB The second endpoint.
     *
     * @return The new line.
     *
     * @throws IllegalArgumentException If either endpoint is null or undefined.
     */
    public static Line fromSegment(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "fromSegment", "missingPoint"));
        }

        Vec3 origin = new Vec3(pointA);
        Vec3 direction = new Vec3(pointB.x - pointA.x, pointB.y - pointA.y, pointB.z - pointA.z);

        return new Line(origin, direction);
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
                Logger.logMessage(Logger.ERROR, "Line", "pointAt", "missingResult."));
        }

        result.x = this.origin.x + this.direction.x * distance;
        result.y = this.origin.y + this.direction.y * distance;
        result.z = this.origin.z + this.direction.z * distance;

        return result;
    }


}
