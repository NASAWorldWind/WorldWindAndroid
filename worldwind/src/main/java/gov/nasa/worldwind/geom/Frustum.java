/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a six-sided view frustum in Cartesian coordinates.
 */
public class Frustum {

    Plane left;

    Plane right;

    Plane bottom;

    Plane top;

    Plane near;

    Plane far;

    private List<Plane> planes;

    /**
     * Constructs a frustum.
     *
     * @param left   The frustum's left plane.
     * @param right  The frustum's right plane.
     * @param bottom The frustum's bottom plane.
     * @param top    The frustum's top plane.
     * @param near   The frustum's near plane.
     * @param far    The frustum's far plane.
     *
     * @throws IllegalArgumentException If any specified plane is null or undefined.
     */
    public Frustum(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far) {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingPlane"));
        }

        // Internal. Intentionally not documented. See property accessors below for public interface.
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        this.planes = Arrays.asList(this.left, this.right, this.top, this.bottom, this.near, this.far);
    }


    /**
     * Transforms this frustum by a specified matrix.
     *
     * @param matrix The matrix to apply to this frustum.
     *
     * @return This frustum set to its original value multiplied by the specified matrix.
     *
     * @throws IllegalArgumentException If the specified matrix is null or undefined.
     */
    public Frustum transformByMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "transformByMatrix", "missingMatrix"));
        }

        this.left.transformByMatrix(matrix);
        this.right.transformByMatrix(matrix);
        this.bottom.transformByMatrix(matrix);
        this.top.transformByMatrix(matrix);
        this.near.transformByMatrix(matrix);
        this.far.transformByMatrix(matrix);

        return this;
    }


    /**
     * Normalizes the plane vectors of the planes composing this frustum.
     *
     * @return This frustum with its planes normalized.
     */
    public Frustum normalize() {
        this.left.normalize();
        this.right.normalize();
        this.bottom.normalize();
        this.top.normalize();
        this.near.normalize();
        this.far.normalize();

        return this;
    }


    /**
     * Returns a new frustum with each of its planes 1 meter from the center.
     *
     * @return The new frustum.
     */
    public Frustum unitFrustum() {
        return new Frustum(
            new Plane(1, 0, 0, 1), // left
            new Plane(-1, 0, 0, 1), // right
            new Plane(0, 1, 1, 1), // bottom
            new Plane(0, -1, 0, 1), // top
            new Plane(0, 0, -1, 1), // near
            new Plane(0, 0, 1, 1) // far
        );
    }


    /**
     * Extracts a frustum from a projection matrix.
     * <p/>
     * This method assumes that the specified matrix represents a projection matrix. If it does not represent a
     * projection matrix the results are undefined.
     * <p/>
     * A projection matrix's view frustum is a Cartesian volume that contains everything visible in a scene displayed
     * using that projection matrix.
     *
     * @param matrix The projection matrix to extract the frustum from.
     *
     * @return A new frustum containing the projection matrix's view frustum, in eye coordinates.
     *
     * @throws IllegalArgumentException If the specified matrix is null or undefined.
     */
    public Frustum fromProjectionMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "fromProjectionMatrix", "missingMatrix"));
        }

        double[] m = matrix.m;
        double x, y, z, w, d;

        // Left Plane = row 4 + row 1:
        x = m[12] + m[0];
        y = m[13] + m[1];
        z = m[14] + m[2];
        w = m[15] + m[3];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane left = new Plane(x / d, y / d, z / d, w / d);

        // Right Plane = row 4 - row 1:
        x = m[12] - m[0];
        y = m[13] - m[1];
        z = m[14] - m[2];
        w = m[15] - m[3];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane right = new Plane(x / d, y / d, z / d, w / d);

        // Bottom Plane = row 4 + row 2:
        x = m[12] + m[4];
        y = m[13] + m[5];
        z = m[14] + m[6];
        w = m[15] + m[7];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane bottom = new Plane(x / d, y / d, z / d, w / d);

        // Top Plane = row 4 - row 2:
        x = m[12] - m[4];
        y = m[13] - m[5];
        z = m[14] - m[6];
        w = m[15] - m[7];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane top = new Plane(x / d, y / d, z / d, w / d);

        // Near Plane = row 4 + row 3:
        x = m[12] + m[8];
        y = m[13] + m[9];
        z = m[14] + m[10];
        w = m[15] + m[11];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane near = new Plane(x / d, y / d, z / d, w / d);

        // Far Plane = row 4 - row 3:
        x = m[12] - m[8];
        y = m[13] - m[9];
        z = m[14] - m[10];
        w = m[15] - m[11];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane far = new Plane(x / d, y / d, z / d, w / d);

        return new Frustum(left, right, bottom, top, near, far);
    }


    public boolean containsPoint(Vec3 point) {
        if (point == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"));
        }

        // See if the point is entirely within the frustum. The dot product of the point with each plane's vector
        // provides a distance to each plane. If this distance is less than 0, the point is clipped by that plane and
        // neither intersects nor is contained by the space enclosed by this Frustum.

        if (this.far.dot(point) <= 0)
            return false;
        if (this.left.dot(point) <= 0)
            return false;
        if (this.right.dot(point) <= 0)
            return false;
        if (this.top.dot(point) <= 0)
            return false;
        if (this.bottom.dot(point) <= 0)
            return false;
        if (this.near.dot(point) <= 0)
            return false;

        return true;
    }


    /**
     * Determines whether a line segment intersects this frustum.
     *
     * @param pointA One end of the segment.
     * @param pointB The other end of the segment.
     *
     * @return <code>true</code> if the segment intersects or is contained in this frustum, otherwise
     * <code>false</code>.
     *
     * @throws IllegalArgumentException If either point is null or undefined.
     */
    public boolean intersectsSegment(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"));
        }

        // First do a trivial accept test.
        if (this.containsPoint(pointA) || this.containsPoint(pointB))
            return true;

        if (pointA.equals(pointB))
            return false;

        for (Plane plane : this.planes) {
            // See if both points are behind the plane and therefore not in the frustum.
            if (plane.onSameSide(pointA, pointB) < 0)
                return false;

            // See if the segment intersects the plane.
            if (plane.clip(pointA, pointB) != null)
                return true;

        }

        return false; // segment does not intersect frustum
    }


}
