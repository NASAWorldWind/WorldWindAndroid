/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a plane in Cartesian coordinates. The plane's X, Y and Z components indicate the plane's normal vector.
 * The distance component indicates the plane's distance from the origin relative to its unit normal. The components are
 * expected to be normalized.
 */
public class Plane {

    /**
     * The normal vector to the plane.
     */
    Vec3 normal;

    /**
     * The plane's distance from the origin.
     */
    double distance;

    /**
     * Constructs a plane. This constructor does not normalize the components. It assumes that a unit normal vector is
     * provided.
     *
     * @param x        The X coordinate of the plane's unit normal vector.
     * @param y        The Y coordinate of the plane's unit normal vector.
     * @param z        The Z coordinate of the plane's unit normal vector.
     * @param distance The plane's distance from the origin.
     */
    public Plane(double x, double y, double z, double distance) {
        /**
         * The normal vector to the plane.
         * @type {Vec3}
         */
        this.normal = new Vec3(x, y, z);

        /**
         * The plane's distance from the origin.
         * @type {Number}
         */
        this.distance = distance;
    }



    /**
     * Computes a plane that passes through the specified three points. The plane's normal is the cross product of the
     * two vectors from pb to pa and pc to pa, respectively. The returned plane is undefined if any of the specified
     * points are colinear.
     *
     * @param pa The first point.
     * @param pb The second point.
     * @param pc The third point.
     *
     * @return A plane passing through the specified points.
     *
     * @throws IllegalArgumentException If the specified vector is null or undefined.
     */
    public static Plane fromPoints(Vec3 pa, Vec3 pb, Vec3 pc) {
        if (pa == null || pb == null || pc == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Plane", "fromPoints", "missingVector"));
        }

        Vec3 vab = new Vec3(pb.x, pb.y, pb.z);
        vab.subtract(pa);
        Vec3 vac = new Vec3(pc.x, pc.y, pc.z);
        vac.subtract(pa);
        vab.cross(vac);
        vab.normalize();
        double d = -vab.dot(pa);

        return new Plane(vab.x, vab.y, vab.z, d);
    }



    /**
     * Computes the dot product of this plane's normal vector with a specified vector. Since the plane was defined with
     * a unit normal vector, this function returns the distance of the vector from the plane.
     *
     * @param vector The vector to dot with this plane's normal vector.
     *
     * @throws IllegalArgumentException If the specified vector is null or undefined.
     * @return The computed dot product.
     */
    public double dot(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Plane", "dot", "missingVector"));
        }

        return this.normal.dot(vector) + this.distance;
    }



    /**
     * Computes the distance between this plane and a point.
     *
     * @param point The point whose distance to compute.
     *
     * @throws IllegalArgumentException If the specified vector is null or undefined.
     * @return The computed distance.
     */
    public double distanceToPoint(Vec3 point) {
        return this.dot(point);
    }



    /**
     * Transforms this plane by a specified matrix.
     *
     * @param matrix The matrix to apply to this plane.
     *
     * @throws IllegalArgumentException If the specified vector is null or undefined.
     * @return This plane transformed by the specified matrix.
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

        return this;
    }



    /**
     * Normalizes the components of this plane.
     *
     * @return This plane with its components normalized.
     */
    public Plane normalize() {
        double magnitude = this.normal.magnitude();

        if (magnitude == 0)
            return this;

        this.normal.divide(magnitude);
        this.distance /= magnitude;

        return this;
    }



    /**
     * Determines whether a specified line segment intersects this plane.
     *
     * @param endPoint1 The first end point of the line segment.
     * @param endPoint2 The second end point of the line segment.
     *
     * @return true if the line segment intersects this plane, otherwise false.
     */
    public boolean intersectsSegment(Vec3 endPoint1, Vec3 endPoint2) {
        double distance1 = this.dot(endPoint1);
        double distance2 = this.dot(endPoint2);

        return distance1 * distance2 <= 0;
    }



    /**
     * Computes the intersection point of this plane with a specified line segment.
     *
     * @param endPoint1 The first end point of the line segment.
     * @param endPoint2 The second end point of the line segment.
     * @param result    A variable in which to return the intersection point of the line segment with this plane.
     *
     * @return true If the line segment intersects this plane, otherwise false.
     */
    public boolean intersectsSegmentAt(Vec3 endPoint1, Vec3 endPoint2, Vec3 result) {
        // Compute the distance from the end-points.
        double distance1 = this.dot(endPoint1);
        double distance2 = this.dot(endPoint2);

        // If both points points lie on the plane, ...
        if (distance1 == 0 && distance2 == 0) {
            // Choose an arbitrary endpoint as the intersection.
            result.x = endPoint1.x;
            result.y = endPoint1.y;
            result.z = endPoint1.z;

            return true;
        } else if (distance1 == distance2) {
            // The intersection is undefined.
            return false;
        }

        double weight1 = -distance1 / (distance2 - distance1);
        double weight2 = 1 - weight1;

        result.x = weight1 * endPoint1.x + weight2 * endPoint2.x;
        result.y = weight1 * endPoint1.y + weight2 * endPoint2.y;
        result.z = weight1 * endPoint1.z + weight2 * endPoint2.z;

        return distance1 * distance2 <= 0;
    }



    /**
     * Determines whether two points are on the same side of this plane.
     *
     * @param pointA the first point.
     * @param pointB the second point.
     *
     * @return -1 If both points are on the negative side of this plane, +1 if both points are on the positive side of
     * this plane, 0 if the points are on opposite sides of this plane.
     *
     * @throws IllegalArgumentException If either point is null or undefined.
     */
    public int onSameSide(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Plane", "onSameSide", "missingPoint"));
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
     * Clips a line segment to this plane.
     *
     * @param pointA The first line segment endpoint.
     * @param pointB The second line segment endpoint.
     *
     * @throws IllegalArgumentException If either point is null or undefined.
     * @return An array of two points both on the positive side of the plane. If the direction of the line formed by
     * the two points is positive with respect to this plane's normal vector, the first point in the array will be the
     * intersection point on the plane, and the second point will be the original segment end point. If the direction of
     * the line is negative with respect to this plane's normal vector, the first point in the array will be the
     * original segment's begin point, and the second point will be the intersection point on the plane. If the segment
     * does not intersect the plane, null is returned. If the segment is coincident with the plane, the input points are
     * returned, in their input order.
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
        Line line = Line.fromSegment(pointA, pointB);
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

}
