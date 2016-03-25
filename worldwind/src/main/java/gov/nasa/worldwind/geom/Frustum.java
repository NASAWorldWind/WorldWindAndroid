/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a six-sided view frustum in Cartesian coordinates.
 */
public class Frustum {

    protected final Plane left = new Plane(1, 0, 0, 1);

    protected final Plane right = new Plane(-1, 0, 0, 1);

    protected final Plane bottom = new Plane(0, 1, 0, 1);

    protected final Plane top = new Plane(0, -1, 0, 1);

    protected final Plane near = new Plane(0, 0, -1, 1);

    protected final Plane far = new Plane(0, 0, 1, 1);

    protected final Plane[] planes = {this.left, this.right, this.top, this.bottom, this.near, this.far};

    /**
     * Constructs a new unit frustum with each of its planes 1 meter from the center.
     */
    public Frustum() {
    }

    /**
     * Constructs a frustum.
     *
     * @param left   the frustum's left plane
     * @param right  the frustum's right plane
     * @param bottom the frustum's bottom plane
     * @param top    the frustum's top plane
     * @param near   the frustum's near plane
     * @param far    the frustum's far plane
     *
     * @throws IllegalArgumentException If any plane is null
     */
    public Frustum(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far) {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingPlane"));
        }

        // Internal. Intentionally not documented. See property accessors below for public interface.
        this.left.set(left);
        this.right.set(right);
        this.bottom.set(bottom);
        this.top.set(top);
        this.near.set(near);
        this.far.set(far);
    }

    @Override
    public String toString() {
        return "left={" + left +
            "}, right={" + right +
            "}, bottom={" + bottom +
            "}, top={" + top +
            "}, near={" + near +
            "}, far={" + far+ '}';
    }

    /**
     * Sets this frustum to a unit frustum with each of its planes 1 meter from the center
     *
     * @return this frustum, set to a unit frustum
     */
    public Frustum setToUnitFrustum() {
        this.left.set(1, 0, 0, 1);
        this.right.set(-1, 0, 0, 1);
        this.bottom.set(0, 1, 0, 1);
        this.top.set(0, -1, 0, 1);
        this.near.set(0, 0, -1, 1);
        this.far.set(0, 0, 1, 1);

        return this;
    }

    /**
     * Sets this frustum to one appropriate for a specified projection matrix. A projection matrix's view frustum is a
     * Cartesian volume that contains everything visible in a scene displayed using that projection matrix.
     * <p/>
     * This method assumes that the specified matrix represents a projection matrix. If it does not represent a
     * projection matrix the results are undefined.
     *
     * @param matrix the projection matrix to extract the frustum from
     *
     * @return this frustum, with its planes set to the projection matrix's view frustum, in eye coordinates
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Frustum setToProjectionMatrix(Matrix4 matrix) {
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
        this.left.set(x / d, y / d, z / d, w / d);

        // Right Plane = row 4 - row 1:
        x = m[12] - m[0];
        y = m[13] - m[1];
        z = m[14] - m[2];
        w = m[15] - m[3];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        this.right.set(x / d, y / d, z / d, w / d);

        // Bottom Plane = row 4 + row 2:
        x = m[12] + m[4];
        y = m[13] + m[5];
        z = m[14] + m[6];
        w = m[15] + m[7];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        this.bottom.set(x / d, y / d, z / d, w / d);

        // Top Plane = row 4 - row 2:
        x = m[12] - m[4];
        y = m[13] - m[5];
        z = m[14] - m[6];
        w = m[15] - m[7];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        this.top.set(x / d, y / d, z / d, w / d);

        // Near Plane = row 4 + row 3:
        x = m[12] + m[8];
        y = m[13] + m[9];
        z = m[14] + m[10];
        w = m[15] + m[11];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        this.near.set(x / d, y / d, z / d, w / d);

        // Far Plane = row 4 - row 3:
        x = m[12] - m[8];
        y = m[13] - m[9];
        z = m[14] - m[10];
        w = m[15] - m[11];
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        this.far.set(x / d, y / d, z / d, w / d);

        return this;
    }

    /**
     * Transforms this frustum by a specified matrix.
     *
     * @param matrix the matrix to apply to this frustum
     *
     * @return this frustum set to its original value multiplied by the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
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
     * @return this frustum with its planes normalized
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
     * @param pointA the first line segment endpoint
     * @param pointB the second line segment endpoint
     *
     * @return true if the segment intersects or is contained in this frustum, otherwise false
     *
     * @throws IllegalArgumentException If either point is null
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
