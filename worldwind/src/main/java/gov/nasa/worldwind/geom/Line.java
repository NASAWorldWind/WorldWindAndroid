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

    /**
     * Computes the first intersection of a triangle strip with this line. This line is interpreted as a ray;
     * intersection points behind the line's origin are ignored.
     * <p/>
     * The triangle strip is specified by a list of vertex points and a list of elements indicating the triangle strip
     * tessellation of those vertices. The triangle strip elements are interpreted in the same manner as OpenGL, where
     * each index indicates a vertex position rather than an actual index into the points array (e.g. a triangle strip
     * index of 1 indicates the XYZ tuple starting at array index 3).
     *
     * @param points   an array of points containing XYZ tuples
     * @param stride   the number of coordinates between the first coordinate of adjacent points - must be at least 3
     * @param elements an array of indices into the points defining the triangle strip organization
     * @param count    the number of indices to consider
     * @param result   a pre-allocated Vec3 in which to return the nearest intersection point, if any
     *
     * @return true if this line intersects the triangle strip, otherwise false
     *
     * @throws IllegalArgumentException If either array is null or empty, if the stride is less than 3, if the count is
     *                                  less than 0, or if the result argument is null
     */
    public boolean triStripIntersection(float[] points, int stride, short[] elements, int count, Vec3 result) {
        if (points == null || points.length < stride) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "triStripIntersection", "missingArray"));
        }

        if (stride < 3) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "triStripIntersection", "invalidStride"));
        }

        if (elements == null || elements.length == 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "triStripIntersection", "missingArray"));
        }

        if (count < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "triStripIntersection", "invalidCount"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Line", "triStripIntersection", "missingResult"));
        }

        // Taken from Moller and Trumbore
        // http://www.cs.virginia.edu/~gfx/Courses/2003/ImageSynthesis/papers/Acceleration/Fast%20MinimumStorage%20RayTriangle%20Intersection.pdf

        // Adapted from the original ray-triangle intersection algorithm to optimize for ray-triangle strip
        // intersection. We optimize by reusing constant terms, replacing use of Vec3 with inline primitives, and
        // exploiting the triangle strip organization to reuse computations common to adjacent triangles. These
        // optimizations reduced worst-case terrain picking performance for Web World Wind by approximately 50% in
        // Chrome on a 2010 iMac and a Nexus 9.

        double vx = this.direction.x;
        double vy = this.direction.y;
        double vz = this.direction.z;
        double sx = this.origin.x;
        double sy = this.origin.y;
        double sz = this.origin.z;
        double tMin = Double.POSITIVE_INFINITY;
        final double EPSILON = 0.00001;

        // Get the triangle strip's first vertex.
        int vertex = elements[0] * stride;
        double vert1x = points[vertex++];
        double vert1y = points[vertex++];
        double vert1z = points[vertex];

        // Get the triangle strip's second vertex.
        vertex = elements[1] * stride;
        double vert2x = points[vertex++];
        double vert2y = points[vertex++];
        double vert2z = points[vertex];

        // Compute the intersection of each triangle with the specified ray.
        for (int idx = 2; idx < count; idx++) {
            // Move the last two vertices into the first two vertices. This takes advantage of the triangle strip's
            // structure and avoids redundant reads from points and elements. During the first iteration this places the
            // triangle strip's first three vertices in vert0, vert1 and vert2, respectively.
            double vert0x = vert1x;
            double vert0y = vert1y;
            double vert0z = vert1z;
            vert1x = vert2x;
            vert1y = vert2y;
            vert1z = vert2z;

            // Get the triangle strip's next vertex.
            vertex = elements[idx] * stride;
            vert2x = points[vertex++];
            vert2y = points[vertex++];
            vert2z = points[vertex];

            // find vectors for two edges sharing point a: vert1 - vert0 and vert2 - vert0
            double edge1x = vert1x - vert0x;
            double edge1y = vert1y - vert0y;
            double edge1z = vert1z - vert0z;
            double edge2x = vert2x - vert0x;
            double edge2y = vert2y - vert0y;
            double edge2z = vert2z - vert0z;

            // Compute cross product of line direction and edge2
            double px = (vy * edge2z) - (vz * edge2y);
            double py = (vz * edge2x) - (vx * edge2z);
            double pz = (vx * edge2y) - (vy * edge2x);

            // Get determinant
            double det = edge1x * px + edge1y * py + edge1z * pz; // edge1 dot p
            if (det > -EPSILON && det < EPSILON) { // if det is near zero then ray lies in plane of triangle
                continue;
            }

            double inv_det = 1.0 / det;

            // Compute distance for vertex A to ray origin: origin - vert0
            double tx = sx - vert0x;
            double ty = sy - vert0y;
            double tz = sz - vert0z;

            // Calculate u parameter and test bounds: 1/det * t dot p
            double u = inv_det * (tx * px + ty * py + tz * pz);
            if (u < -EPSILON || u > 1 + EPSILON) {
                continue;
            }

            // Prepare to test v parameter: tvec cross edge1
            double qx = (ty * edge1z) - (tz * edge1y);
            double qy = (tz * edge1x) - (tx * edge1z);
            double qz = (tx * edge1y) - (ty * edge1x);

            // Calculate v parameter and test bounds: 1/det * dir dot q
            double v = inv_det * (vx * qx + vy * qy + vz * qz);
            if (v < -EPSILON || u + v > 1 + EPSILON) {
                continue;
            }

            // Calculate the point of intersection on the line: t = 1/det * edge2 dot q
            double t = inv_det * (edge2x * qx + edge2y * qy + edge2z * qz);
            if (t >= 0 && t < tMin) {
                tMin = t;
            }
        }

        if (tMin != Double.POSITIVE_INFINITY) {
            result.set(sx + vx * tMin, sy + vy * tMin, sz + vz * tMin);
        }

        return tMin != Double.POSITIVE_INFINITY;
    }
}
