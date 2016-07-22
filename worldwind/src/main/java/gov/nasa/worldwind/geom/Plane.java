/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a plane in Cartesian coordinates. The plane's X, Y and Z components indicate the plane's normal vector.
 * The distance component indicates the plane's distance from the origin relative to its unit normal.
 */
public class Plane {

    protected final static double NEAR_ZERO_THRESHOLD = 1e-10;

    /**
     * The normal vector to the plane.
     */
    protected final Vec3 normal = new Vec3();

    /**
     * The plane's distance from the origin.
     */
    protected double distance;

    /**
     * Constructs a plane in the X-Y plane with its unit normal pointing along the Z axis.
     */
    public Plane() {
        this.normal.z = 1.0;
    }

    /**
     * Constructs a plane with specified normal vector components and distance from the origin. This constructor
     * normalizes the components, ensuring that the plane has a unit normal vector.
     *
     * @param x        the X component of the plane's normal vector
     * @param y        the Y component of the plane's normal vector
     * @param z        the Z component of the plane's normal vector
     * @param distance the plane's distance from the origin
     */
    public Plane(double x, double y, double z, double distance) {
        this.normal.x = x;
        this.normal.y = y;
        this.normal.z = z;
        this.distance = distance;
        this.normalizeIfNeeded();
    }

    /**
     * Constructs a plane with the normal vector and distance from a specified plane.
     *
     * @param plane the plane specifying the normal vector and distance
     *
     * @throws IllegalArgumentException If the plane is null
     */
    public Plane(Plane plane) {
        if (plane == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Plane", "constructor", "missingPlane"));
        }

        // Assumes the specified plane's parameters are normalized.
        this.normal.set(plane.normal);
        this.distance = plane.distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Plane that = (Plane) o;
        return this.normal.equals(that.normal) && this.distance == that.distance;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = this.normal.hashCode();
        temp = Double.doubleToLongBits(this.distance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "normal=[" + this.normal + "], distance=" + this.distance;
    }

    /**
     * Computes the distance between this plane and a point.
     *
     * @param point the point whose distance to compute
     *
     * @return the computed distance
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public double distanceToPoint(Vec3 point) {
        return this.dot(point);
    }

    /**
     * Sets this plane's specified normal vector and distance to specified values. This normalizes the components,
     * ensuring that the plane has a unit normal vector.
     *
     * @param x        the X component of the plane's normal vector
     * @param y        the Y component of the plane's normal vector
     * @param z        the Z component of the plane's normal vector
     * @param distance the plane's distance from the origin
     *
     * @return this plane with its normal vector and distance set to specified values
     */
    public Plane set(double x, double y, double z, double distance) {
        this.normal.x = x;
        this.normal.y = y;
        this.normal.z = z;
        this.distance = distance;
        this.normalizeIfNeeded();
        return this;
    }

    /**
     * Sets this plane's normal vector and distance to that of a specified plane.
     *
     * @param plane the plane specifying the normal vector and distance
     *
     * @return this plane with its normal vector and distance set to those of the specified plane
     *
     * @throws IllegalArgumentException If the plane is null
     */
    public Plane set(Plane plane) {
        if (plane == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Plane", "set", "missingPlane"));
        }

        // Assumes the specified plane's parameters are normalized.
        this.normal.set(plane.normal);
        this.distance = plane.distance;
        return this;
    }

    /**
     * Transforms this plane by a specified matrix.
     *
     * @param matrix the matrix to apply to this plane
     *
     * @return this plane transformed by the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Plane transformByMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Plane", "transformByMatrix", "missingMatrix"));
        }

        double[] m = matrix.m;
        double x = m[0] * this.normal.x + m[1] * this.normal.y + m[2] * this.normal.z + m[3] * this.distance;
        double y = m[4] * this.normal.x + m[5] * this.normal.y + m[6] * this.normal.z + m[7] * this.distance;
        double z = m[8] * this.normal.x + m[9] * this.normal.y + m[10] * this.normal.z + m[11] * this.distance;
        double distance = m[12] * this.normal.x + m[13] * this.normal.y + m[14] * this.normal.z + m[15] * this.distance;

        this.normal.x = x;
        this.normal.y = y;
        this.normal.z = z;
        this.distance = distance;
        this.normalizeIfNeeded();

        return this;
    }

    /**
     * Computes the dot product of this plane's components with a specified vector. Since the plane was defined with a
     * unit normal vector, this function returns the distance of the vector from the plane.
     *
     * @param vector the vector to dot with this plane's components
     *
     * @return the computed dot product
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public double dot(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Plane", "dot", "missingVector"));
        }

        return this.normal.dot(vector) + this.distance;
    }

    /**
     * Determines whether a specified line segment intersects this plane.
     *
     * @param endPoint1 the line segment's first end point
     * @param endPoint2 the line segment's second end point
     *
     * @return true if the line segment intersects this plane, otherwise false
     */
    public boolean intersectsSegment(Vec3 endPoint1, Vec3 endPoint2) {
        if (endPoint1 == null || endPoint2 == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Plane", "intersectsSegment", "missingPoint"));
        }

        double distance1 = this.dot(endPoint1);
        double distance2 = this.dot(endPoint2);

        return distance1 * distance2 <= 0;
    }

    /**
     * Determines whether two points are on the same side of this plane.
     *
     * @param pointA the first point
     * @param pointB the second point
     *
     * @return -1 if both points are on the negative side of this plane, +1 if both points are on the positive side of
     * this plane, 0 if the points are on opposite sides of this plane
     *
     * @throws IllegalArgumentException If either point is null
     */
    public int onSameSide(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Plane", "onSameSide", "missingPoint"));
        }

        double da = this.distanceToPoint(pointA);
        double db = this.distanceToPoint(pointB);

        if (da < 0 && db < 0)
            return -1;

        if (da > 0 && db > 0)
            return 1;

        return 0;
    }

    /**
     * Clips a line segment to this plane, returning an two-point array indicating the clipped segment. If the direction
     * of the line formed by the two points is positive with respect to this plane's normal vector, the first point in
     * the array will be the intersection point on the plane, and the second point will be the original segment end
     * point. If the direction of the line is negative with respect to this plane's normal vector, the first point in
     * the array will be the original segment's begin point, and the second point will be the intersection point on the
     * plane. If the segment does not intersect the plane, null is returned. If the segment is coincident with the
     * plane, the input points are returned, in their input order.
     *
     * @param pointA the first line segment endpoint
     * @param pointB the second line segment endpoint
     *
     * @return an array of two points both on the positive side of the plane, or null if the segment does not intersect
     * this plane
     *
     * @throws IllegalArgumentException If either point is null
     */
    public Vec3[] clip(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Plane", "clip", "missingPoint"));
        }

        if (pointA.equals(pointB)) {
            return null;
        }

        // Get the projection of the segment onto the plane.
        Line line = new Line().setToSegment(pointA, pointB);
        double lDotV = this.normal.dot(line.direction);

        // Are the line and plane parallel?
        if (lDotV == 0) { // line and plane are parallel and may be coincident.
            double lDotS = this.dot(line.origin);
            if (lDotS == 0) {
                return new Vec3[]{pointA, pointB}; // line is coincident with the plane
            } else {
                return null; // line is not coincident with the plane.
            }
        }

        // Not parallel so the line intersects. But does the segment intersect?
        double t = -this.dot(line.origin) / lDotV; // lDotS / lDotV
        if (t < 0 || t > 1) { // segment does not intersect
            return null;
        }

        Vec3 p = line.pointAt(t, new Vec3());
        if (lDotV > 0) {
            return new Vec3[]{p, pointB};
        } else {
            return new Vec3[]{pointA, p};
        }
    }

    protected void normalizeIfNeeded() {
        // Compute the plane normal's magnitude in order to determine whether or not the plane needs normalization.
        double magnitude = this.normal.magnitude();

        // Don't normalize a zero vector; the result is NaN when it should be 0.0.
        if (magnitude == 0) {
            return;
        }

        // Don't normalize a unit vector, this indicates that the caller has already normalized the vector, but floating
        // point roundoff results in a length not exactly 1.0. Since we're normalizing on the caller's behalf, we want
        // to avoid unnecessary any normalization that modifies the specified values.
        if (magnitude >= 1 - NEAR_ZERO_THRESHOLD && magnitude <= 1 + NEAR_ZERO_THRESHOLD) {
            return;
        }

        // Normalize the caller-specified plane coordinates.
        this.normal.x /= magnitude;
        this.normal.y /= magnitude;
        this.normal.z /= magnitude;
        this.distance /= magnitude;
    }
}
