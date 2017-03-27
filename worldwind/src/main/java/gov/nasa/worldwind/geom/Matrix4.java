/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import java.util.Arrays;

import gov.nasa.worldwind.util.Logger;

/**
 * 4 x 4 matrix in row-major order.
 */
public class Matrix4 {

    /**
     * The components for the 4 x 4 identity matrix, stored in row-major order.
     */
    protected static final double[] identity = new double[]{
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1};

    /**
     * The matrix's components, stored in row-major order. Initialized to the 4 x 4 identity matrix.
     */
    public final double[] m = new double[]{
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1};

    /**
     * Constructs a 4 x 4 identity matrix.
     */
    public Matrix4() {
    }

    /**
     * Constructs a 4 x 4 matrix with specified components.
     *
     * @param m11 matrix element at row 1, column 1
     * @param m12 matrix element at row 1, column 2
     * @param m13 matrix element at row 1, column 3
     * @param m14 matrix element at row 1, column 4
     * @param m21 matrix element at row 2, column 1
     * @param m22 matrix element at row 2, column 2
     * @param m23 matrix element at row 2, column 3
     * @param m24 matrix element at row 2, column 4
     * @param m31 matrix element at row 3, column 1
     * @param m32 matrix element at row 3, column 2
     * @param m33 matrix element at row 3, column 3
     * @param m34 matrix element at row 3, column 4
     * @param m41 matrix element at row 4, column 1
     * @param m42 matrix element at row 4, column 2
     * @param m43 matrix element at row 4, column 3
     * @param m44 matrix element at row 4, column 4
     */
    public Matrix4(double m11, double m12, double m13, double m14,
                   double m21, double m22, double m23, double m24,
                   double m31, double m32, double m33, double m34,
                   double m41, double m42, double m43, double m44) {
        this.m[0] = m11;
        this.m[1] = m12;
        this.m[2] = m13;
        this.m[3] = m14;

        this.m[4] = m21;
        this.m[5] = m22;
        this.m[6] = m23;
        this.m[7] = m24;

        this.m[8] = m31;
        this.m[9] = m32;
        this.m[10] = m33;
        this.m[11] = m34;

        this.m[12] = m41;
        this.m[13] = m42;
        this.m[14] = m43;
        this.m[15] = m44;
    }

    /**
     * Constructs a 4 x 4 matrix with the components of a specified matrix.
     *
     * @param matrix the matrix specifying the new components
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Matrix4(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "constructor", "missingMatrix"));
        }

        System.arraycopy(matrix.m, 0, this.m, 0, 16);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Matrix4 that = (Matrix4) o;
        return this.m[0] == that.m[0]
            && this.m[1] == that.m[1]
            && this.m[2] == that.m[2]
            && this.m[3] == that.m[3]
            && this.m[4] == that.m[4]
            && this.m[5] == that.m[5]
            && this.m[6] == that.m[6]
            && this.m[7] == that.m[7]
            && this.m[8] == that.m[8]
            && this.m[9] == that.m[9]
            && this.m[10] == that.m[10]
            && this.m[11] == that.m[11]
            && this.m[12] == that.m[12]
            && this.m[13] == that.m[13]
            && this.m[14] == that.m[14]
            && this.m[15] == that.m[15];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.m);
    }

    @Override
    public String toString() {
        return "[" + this.m[0] + ", " + this.m[1] + ", " + this.m[2] + ", " + this.m[3] + "], " +
            '[' + this.m[4] + ", " + this.m[5] + ", " + this.m[6] + ", " + this.m[7] + "], " +
            '[' + this.m[8] + ", " + this.m[9] + ", " + this.m[10] + ", " + this.m[11] + "], " +
            '[' + this.m[12] + ", " + this.m[13] + ", " + this.m[14] + ", " + this.m[15] + ']';
    }

    /**
     * Sets this 4 x 4 matrix to specified components.
     *
     * @param m11 matrix element at row 1, column 1
     * @param m12 matrix element at row 1, column 2
     * @param m13 matrix element at row 1, column 3
     * @param m14 matrix element at row 1, column 4
     * @param m21 matrix element at row 2, column 1
     * @param m22 matrix element at row 2, column 2
     * @param m23 matrix element at row 2, column 3
     * @param m24 matrix element at row 2, column 4
     * @param m31 matrix element at row 3, column 1
     * @param m32 matrix element at row 3, column 2
     * @param m33 matrix element at row 3, column 3
     * @param m34 matrix element at row 3, column 4
     * @param m41 matrix element at row 4, column 1
     * @param m42 matrix element at row 4, column 2
     * @param m43 matrix element at row 4, column 3
     * @param m44 matrix element at row 4, column 4
     *
     * @return this matrix set to the specified components
     */
    public Matrix4 set(double m11, double m12, double m13, double m14,
                       double m21, double m22, double m23, double m24,
                       double m31, double m32, double m33, double m34,
                       double m41, double m42, double m43, double m44) {
        this.m[0] = m11;
        this.m[1] = m12;
        this.m[2] = m13;
        this.m[3] = m14;

        this.m[4] = m21;
        this.m[5] = m22;
        this.m[6] = m23;
        this.m[7] = m24;

        this.m[8] = m31;
        this.m[9] = m32;
        this.m[10] = m33;
        this.m[11] = m34;

        this.m[12] = m41;
        this.m[13] = m42;
        this.m[14] = m43;
        this.m[15] = m44;

        return this;
    }

    /**
     * Sets this 4 x 4 matrix to the components of a specified matrix.
     *
     * @param matrix the matrix specifying the new components
     *
     * @return this matrix with its components set to that of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Matrix4 set(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "set", "missingMatrix"));
        }

        System.arraycopy(matrix.m, 0, this.m, 0, 16);

        return this;
    }

    /**
     * Sets the translation components of this matrix to specified values.
     *
     * @param x the X translation component
     * @param y the Y translation component
     * @param z the Z translation component
     *
     * @return this matrix with its translation components set to the specified values and all other components
     * unmodified
     */
    public Matrix4 setTranslation(double x, double y, double z) {
        this.m[3] = x;
        this.m[7] = y;
        this.m[11] = z;

        return this;
    }

    /**
     * Sets the rotation components of this matrix to a specified axis and angle. Positive angles are interpreted as
     * counter-clockwise rotation about the axis when viewed when viewed from the positive end of the axis, looking
     * toward the negative end of the axis.
     * <p/>
     * The result of this method is undefined if the axis components are not a unit vector.
     *
     * @param x            the X component of the rotation axis unit vector
     * @param y            the Y component of the rotation axis unit vector
     * @param z            the Z component of the rotation axis unit vector
     * @param angleDegrees the angle of rotation in degrees
     *
     * @return this matrix with its rotation components set to the specified values and all other components unmodified
     */
    public Matrix4 setRotation(double x, double y, double z, double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        double c = Math.cos(rad);
        double s = Math.sin(rad);

        this.m[0] = c + (1 - c) * x * x;
        this.m[1] = (1 - c) * x * y - s * z;
        this.m[2] = (1 - c) * x * z + s * y;

        this.m[4] = (1 - c) * x * y + s * z;
        this.m[5] = c + (1 - c) * y * y;
        this.m[6] = (1 - c) * y * z - s * x;

        this.m[8] = (1 - c) * x * z - s * y;
        this.m[9] = (1 - c) * y * z + s * x;
        this.m[10] = c + (1 - c) * z * z;

        return this;
    }

    /**
     * Sets the scale components of this matrix to specified values.
     *
     * @param xScale the X scale component
     * @param yScale the Y scale component
     * @param zScale the Z scale component
     *
     * @return this matrix with its scale components set to the specified values and all other components unmodified
     */
    public Matrix4 setScale(double xScale, double yScale, double zScale) {
        this.m[0] = xScale;
        this.m[5] = yScale;
        this.m[10] = zScale;

        return this;
    }

    /**
     * Sets this matrix to the 4 x 4 identity matrix.
     *
     * @return this matrix, set to the identity matrix
     */
    public Matrix4 setToIdentity() {
        System.arraycopy(identity, 0, this.m, 0, 16);

        return this;
    }

    /**
     * Sets this matrix to a translation matrix with specified translation components.
     *
     * @param x the X translation component
     * @param y the Y translation component
     * @param z the Z translation component
     *
     * @return this matrix with its translation components set to those specified and all other components set to that
     * of an identity matrix
     */
    public Matrix4 setToTranslation(double x, double y, double z) {
        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = x;

        this.m[4] = 0;
        this.m[5] = 1;
        this.m[6] = 0;
        this.m[7] = y;

        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = 1;
        this.m[11] = z;

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Sets this matrix to a rotation matrix with a specified axis and angle. Positive angles are interpreted as
     * counter-clockwise rotation about the axis when viewed when viewed from the positive end of the axis, looking
     * toward the negative end of the axis.
     * <p/>
     * The result of this method is undefined if the axis components are not a unit vector.
     *
     * @param x            the X component of the rotation axis unit vector
     * @param y            the Y component of the rotation axis unit vector
     * @param z            the Z component of the rotation axis unit vector
     * @param angleDegrees the angle of rotation in degrees
     *
     * @return this matrix with its rotation components set to those specified and all other components set to that of
     * an identity matrix
     */
    public Matrix4 setToRotation(double x, double y, double z, double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        double c = Math.cos(rad);
        double s = Math.sin(rad);

        this.m[0] = c + (1 - c) * x * x;
        this.m[1] = (1 - c) * x * y - s * z;
        this.m[2] = (1 - c) * x * z + s * y;
        this.m[3] = 0;

        this.m[4] = (1 - c) * x * y + s * z;
        this.m[5] = c + (1 - c) * y * y;
        this.m[6] = (1 - c) * y * z - s * x;
        this.m[7] = 0;

        this.m[8] = (1 - c) * x * z - s * y;
        this.m[9] = (1 - c) * y * z + s * x;
        this.m[10] = c + (1 - c) * z * z;
        this.m[11] = 0;

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Sets this matrix to a scale matrix with specified scale components.
     *
     * @param xScale the X scale component
     * @param yScale the Y scale component
     * @param zScale the Z scale component
     *
     * @return this matrix with its scale components set to those specified and all other components set to that of an
     * identity matrix
     */
    public Matrix4 setToScale(double xScale, double yScale, double zScale) {
        this.m[0] = xScale;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;

        this.m[4] = 0;
        this.m[5] = yScale;
        this.m[6] = 0;
        this.m[7] = 0;

        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = zScale;
        this.m[11] = 0;

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Sets this matrix to the matrix product of two specified matrices.
     *
     * @param a the first matrix multiplicand
     * @param b The second matrix multiplicand
     *
     * @return this matrix set to the product of a x b
     *
     * @throws IllegalArgumentException If either matrix is null
     */
    public Matrix4 setToMultiply(Matrix4 a, Matrix4 b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "setToMultiply", "missingMatrix"));
        }

        double[] ma = a.m;
        double[] mb = b.m;

        this.m[0] = (ma[0] * mb[0]) + (ma[1] * mb[4]) + (ma[2] * mb[8]) + (ma[3] * mb[12]);
        this.m[1] = (ma[0] * mb[1]) + (ma[1] * mb[5]) + (ma[2] * mb[9]) + (ma[3] * mb[13]);
        this.m[2] = (ma[0] * mb[2]) + (ma[1] * mb[6]) + (ma[2] * mb[10]) + (ma[3] * mb[14]);
        this.m[3] = (ma[0] * mb[3]) + (ma[1] * mb[7]) + (ma[2] * mb[11]) + (ma[3] * mb[15]);

        this.m[4] = (ma[4] * mb[0]) + (ma[5] * mb[4]) + (ma[6] * mb[8]) + (ma[7] * mb[12]);
        this.m[5] = (ma[4] * mb[1]) + (ma[5] * mb[5]) + (ma[6] * mb[9]) + (ma[7] * mb[13]);
        this.m[6] = (ma[4] * mb[2]) + (ma[5] * mb[6]) + (ma[6] * mb[10]) + (ma[7] * mb[14]);
        this.m[7] = (ma[4] * mb[3]) + (ma[5] * mb[7]) + (ma[6] * mb[11]) + (ma[7] * mb[15]);

        this.m[8] = (ma[8] * mb[0]) + (ma[9] * mb[4]) + (ma[10] * mb[8]) + (ma[11] * mb[12]);
        this.m[9] = (ma[8] * mb[1]) + (ma[9] * mb[5]) + (ma[10] * mb[9]) + (ma[11] * mb[13]);
        this.m[10] = (ma[8] * mb[2]) + (ma[9] * mb[6]) + (ma[10] * mb[10]) + (ma[11] * mb[14]);
        this.m[11] = (ma[8] * mb[3]) + (ma[9] * mb[7]) + (ma[10] * mb[11]) + (ma[11] * mb[15]);

        this.m[12] = (ma[12] * mb[0]) + (ma[13] * mb[4]) + (ma[14] * mb[8]) + (ma[15] * mb[12]);
        this.m[13] = (ma[12] * mb[1]) + (ma[13] * mb[5]) + (ma[14] * mb[9]) + (ma[15] * mb[13]);
        this.m[14] = (ma[12] * mb[2]) + (ma[13] * mb[6]) + (ma[14] * mb[10]) + (ma[15] * mb[14]);
        this.m[15] = (ma[12] * mb[3]) + (ma[13] * mb[7]) + (ma[14] * mb[11]) + (ma[15] * mb[15]);

        return this;
    }

    /**
     * Sets this matrix to an infinite perspective projection matrix for the specified viewport dimensions, vertical
     * field of view and near clip distance.
     * <p/>
     * An infinite perspective projection matrix maps points in a manner similar to a standard projection matrix, but is
     * not bounded by depth. Objects at any depth greater than or equal to the near distance may be rendered. In
     * addition, this matrix interprets vertices with a w-coordinate of 0 as infinitely far from the camera in the
     * direction indicated by the point's coordinates.
     * <p/>
     * The field of view must be positive and less than 180. The near distance must be positive.
     *
     * @param viewportWidth  the viewport width in screen coordinates
     * @param viewportHeight the viewport height in screen coordinates
     * @param fovyDegrees    the vertical field of view in degrees
     * @param nearDistance   the near clip plane distance in model coordinates
     *
     * @throws IllegalArgumentException If either the width or the height is less than or equal to zero, if the field of
     *                                  view is less than or equal to zero or greater than 180, if the near distance is
     *                                  less than or equal to zero
     */
    public Matrix4 setToInfiniteProjection(double viewportWidth, double viewportHeight, double fovyDegrees,
                                           double nearDistance) {
        if (viewportWidth <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidWidth"));
        }

        if (viewportHeight <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidHeight"));
        }

        if (fovyDegrees <= 0 || fovyDegrees >= 180) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidFieldOfView"));
        }

        if (nearDistance <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToInfiniteProjection",
                "invalidClipDistance"));
        }

        // Compute the dimensions of the near rectangle given the specified parameters.
        double aspect = viewportWidth / viewportHeight;
        double tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5));
        double nearHeight = 2 * nearDistance * tanfovy_2;
        double nearWidth = nearHeight * aspect;
        double near = nearDistance;

        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition, equation 4.52.

        this.m[0] = (2 * near) / nearWidth;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;

        this.m[4] = 0;
        this.m[5] = (2 * near) / nearHeight;
        this.m[6] = 0;
        this.m[7] = 0;

        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = -1;
        this.m[11] = -2 * near;

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = -1;
        this.m[15] = 0;

        return this;
    }

    /**
     * Sets this matrix to a perspective projection matrix for the specified viewport dimensions, vertical field of view
     * and clip distances.
     * <p/>
     * A perspective projection matrix maps points in eye coordinates into clip coordinates in a way that causes distant
     * objects to appear smaller, and preserves the appropriate depth information for each point. In model coordinates,
     * a perspective projection is defined by frustum originating at the eye position and extending outward in the
     * viewer's direction. The near distance and the far distance identify the minimum and maximum distance,
     * respectively, at which an object in the scene is visible.
     * <p/>
     * The field of view must be positive and less than 180. Near and far distances must be positive and must not be
     * equal to one another.
     *
     * @param viewportWidth  the viewport width in screen coordinates
     * @param viewportHeight the viewport height in screen coordinates
     * @param fovyDegrees    the vertical field of view in degrees
     * @param nearDistance   the near clip plane distance in model coordinates
     * @param farDistance    the far clip plane distance in model coordinates
     *
     * @throws IllegalArgumentException If either the width or the height is less than or equal to zero, if the field of
     *                                  view is less than or equal to zero or greater than 180, if the near and far
     *                                  distances are equal, or if either the near or far distance are less than or
     *                                  equal to zero
     */
    public Matrix4 setToPerspectiveProjection(double viewportWidth, double viewportHeight, double fovyDegrees,
                                              double nearDistance, double farDistance) {
        if (viewportWidth <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                "invalidWidth"));
        }

        if (viewportHeight <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                "invalidHeight"));
        }

        if (fovyDegrees <= 0 || fovyDegrees >= 180) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                "invalidFieldOfView"));
        }

        if (nearDistance == farDistance) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                "invalidClipDistance"));
        }

        if (nearDistance <= 0 || farDistance <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToPerspectiveProjection",
                "invalidClipDistance"));
        }

        // Compute the dimensions of the near rectangle given the specified parameters.
        double aspect = viewportWidth / viewportHeight;
        double tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5));
        double nearHeight = 2 * nearDistance * tanfovy_2;
        double nearWidth = nearHeight * aspect;
        double near = nearDistance;
        double far = farDistance;

        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition, equation 4.52.

        this.m[0] = (2 * near) / nearWidth;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;

        this.m[4] = 0;
        this.m[5] = (2 * near) / nearHeight;
        this.m[6] = 0;
        this.m[7] = 0;

        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = -(far + near) / (far - near);
        this.m[11] = -(2 * near * far) / (far - near);

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = -1;
        this.m[15] = 0;

        return this;
    }

    /**
     * Sets this matrix to a screen projection matrix for the specified viewport dimensions.
     * <p/>
     * A screen projection matrix is an orthographic projection that interprets points in model coordinates as
     * representing a screen XY and a Z depth. Screen projection matrices therefore map coordinates directly into screen
     * coordinates without modification. A point's XY coordinates are interpreted as literal screen coordinates and must
     * be in the viewport to be visible. A point's Z coordinate is interpreted as a depth value that ranges from 0 to 1.
     * Additionally, the screen projection matrix preserves the depth value returned by
     * <code>RenderContext.project</code>.
     *
     * @param viewportWidth  the viewport width in screen coordinates
     * @param viewportHeight the viewport height in screen coordinates
     *
     * @throws IllegalArgumentException If either the width or the height is less than or equal to zero
     */
    public Matrix4 setToScreenProjection(double viewportWidth, double viewportHeight) {
        if (viewportWidth <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToScreenProjection",
                "invalidWidth"));
        }

        if (viewportHeight <= 0) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Matrix4", "setToScreenProjection",
                "invalidHeight"));
        }

        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition, equation 4.57.
        // Simplified to assume that the viewport origin is (0, 0).
        //
        // The third row of this projection matrix is configured so that points with z coordinates representing
        // depth values ranging from 0 to 1 are not modified after transformation into window coordinates. This
        // projection matrix maps z values in the range [0, 1] to the range [-1, 1] by applying the following
        // function to incoming z coordinates:
        //
        // zp = z0 * 2 - 1
        //
        // Where 'z0' is the point's z coordinate and 'zp' is the projected z coordinate. The GPU then maps the
        // projected z coordinate into window coordinates in the range [0, 1] by applying the following function:
        //
        // zw = zp * 0.5 + 0.5
        //
        // The result is that a point's z coordinate is effectively passed to the GPU without modification.

        double width = viewportWidth;
        double height = viewportHeight;

        this.m[0] = 2 / width;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = -1;

        this.m[4] = 0;
        this.m[5] = 2 / height;
        this.m[6] = 0;
        this.m[7] = -1;

        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = 2;
        this.m[11] = -1;

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Sets this matrix to the symmetric covariance Matrix computed from an array of points.
     * <p/>
     * The computed covariance matrix represents the correlation between each pair of x-, y-, and z-coordinates as
     * they're distributed about the point array's arithmetic mean. Its layout is as follows:
     * <p/>
     * <code> C(x, x)  C(x, y)  C(x, z) <br/> C(x, y)  C(y, y)  C(y, z) <br/> C(x, z)  C(y, z)  C(z, z) </code>
     * <p/>
     * C(i, j) is the covariance of coordinates i and j, where i or j are a coordinate's dispersion about its mean
     * value. If any entry is zero, then there's no correlation between the two coordinates defining that entry. If the
     * returned matrix is diagonal, then all three coordinates are uncorrelated, and the specified point is distributed
     * evenly about its mean point.
     *
     * @param array  the array of points to consider
     * @param count  the number of array elements to consider
     * @param stride the number of coordinates between the first coordinate of adjacent points - must be at least 3
     *
     * @return this matrix set to the covariance matrix for the specified array of points
     *
     * @throws IllegalArgumentException If the array is null or empty, if the count is less than 0, or if the stride is
     *                                  less than 3
     */
    public Matrix4 setToCovarianceOfPoints(float[] array, int count, int stride) {
        if (array == null || array.length < stride) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "setToCovarianceOfPoints", "missingArray"));
        }

        if (count < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "setToCovarianceOfPoints", "invalidCount"));
        }

        if (stride < 3) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "setToCovarianceOfPoints", "invalidStride"));
        }

        double dx, dy, dz;
        double mx = 0, my = 0, mz = 0;
        double c11 = 0, c22 = 0, c33 = 0, c12 = 0, c13 = 0, c23 = 0;
        double numPoints = 0;

        for (int idx = 0; idx < count; idx += stride) {
            mx += array[idx];
            my += array[idx + 1];
            mz += array[idx + 2];
            numPoints++;
        }

        mx /= numPoints;
        my /= numPoints;
        mz /= numPoints;

        for (int idx = 0; idx < count; idx += stride) {
            dx = array[idx] - mx;
            dy = array[idx + 1] - my;
            dz = array[idx + 2] - mz;

            c11 += dx * dx;
            c22 += dy * dy;
            c33 += dz * dz;
            c12 += dx * dy; // c12 = c21
            c13 += dx * dz; // c13 = c31
            c23 += dy * dz; // c23 = c32
        }

        this.m[0] = c11 / numPoints;
        this.m[1] = c12 / numPoints;
        this.m[2] = c13 / numPoints;
        this.m[3] = 0;

        this.m[4] = c12 / numPoints;
        this.m[5] = c22 / numPoints;
        this.m[6] = c23 / numPoints;
        this.m[7] = 0;

        this.m[8] = c13 / numPoints;
        this.m[9] = c23 / numPoints;
        this.m[10] = c33 / numPoints;
        this.m[11] = 0;

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 0;

        return this;
    }

    /**
     * Multiplies this matrix by a translation matrix with specified translation values.
     *
     * @param x the X translation component
     * @param y the Y translation component
     * @param z the Z translation component
     *
     * @return this matrix multiplied by the translation matrix implied by the specified values
     */
    public Matrix4 multiplyByTranslation(double x, double y, double z) {

        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //     1, 0, 0, x,
        //     0, 1, 0, y,
        //     0, 0, 1, z,
        //     0, 0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the translation matrix's components,
        // reducing the total number of primitive operations from 144 to 24.

        double[] m = this.m;

        m[3] += (m[0] * x) + (m[1] * y) + (m[2] * z);
        m[7] += (m[4] * x) + (m[5] * y) + (m[6] * z);
        m[11] += (m[8] * x) + (m[9] * y) + (m[10] * z);
        m[15] += (m[12] * x) + (m[13] * y) + (m[14] * z);

        return this;
    }

    /**
     * Multiplies this matrix by a rotation matrix about a specified axis and angle. Positive angles are interpreted as
     * counter-clockwise rotation about the axis.
     *
     * @param x            the X component of the rotation axis
     * @param y            the Y component of the rotation axis
     * @param z            the Z component of the rotation axis
     * @param angleDegrees the angle of rotation in degrees
     *
     * @return this matrix multiplied by the rotation matrix implied by the specified values
     */
    public Matrix4 multiplyByRotation(double x, double y, double z, double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        double c = Math.cos(rad);
        double s = Math.sin(rad);

        this.multiplyByMatrix(
            c + (1 - c) * x * x, (1 - c) * x * y - s * z, (1 - c) * x * z + s * y, 0,
            (1 - c) * x * y + s * z, c + (1 - c) * y * y, (1 - c) * y * z - s * x, 0,
            (1 - c) * x * z - s * y, (1 - c) * y * z + s * x, c + (1 - c) * z * z, 0,
            0, 0, 0, 1);

        return this;
    }

    /**
     * Multiplies this matrix by a scale matrix with specified values.
     *
     * @param xScale the X scale component
     * @param yScale the Y scale component
     * @param zScale the Z scale component
     *
     * @return this matrix multiplied by the scale matrix implied by the specified values
     */
    public Matrix4 multiplyByScale(double xScale, double yScale, double zScale) {

        // This is equivalent to the following operation, but is potentially much faster:
        //
        // this.multiplyByMatrix(
        //     xScale, 0, 0, 0,
        //     0, yScale, 0, 0,
        //     0, 0, zScale, 0,
        //     0, 0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the scale matrix's components,
        // reducing the total number of primitive operations from 144 to 12.

        double[] m = this.m;

        m[0] *= xScale;
        m[4] *= xScale;
        m[8] *= xScale;
        m[12] *= xScale;

        m[1] *= yScale;
        m[5] *= yScale;
        m[9] *= yScale;
        m[13] *= yScale;

        m[2] *= zScale;
        m[6] *= zScale;
        m[10] *= zScale;
        m[14] *= zScale;

        return this;
    }

    /**
     * Multiplies this matrix by a specified matrix.
     *
     * @param matrix the matrix to multiply with this matrix
     *
     * @return this matrix after multiplying it by the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Matrix4 multiplyByMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "multiplyByMatrix", "missingMatrix"));
        }

        double[] ma = this.m;
        double[] mb = matrix.m;
        double ma0, ma1, ma2, ma3;

        ma0 = ma[0];
        ma1 = ma[1];
        ma2 = ma[2];
        ma3 = ma[3];
        ma[0] = (ma0 * mb[0]) + (ma1 * mb[4]) + (ma2 * mb[8]) + (ma3 * mb[12]);
        ma[1] = (ma0 * mb[1]) + (ma1 * mb[5]) + (ma2 * mb[9]) + (ma3 * mb[13]);
        ma[2] = (ma0 * mb[2]) + (ma1 * mb[6]) + (ma2 * mb[10]) + (ma3 * mb[14]);
        ma[3] = (ma0 * mb[3]) + (ma1 * mb[7]) + (ma2 * mb[11]) + (ma3 * mb[15]);

        ma0 = ma[4];
        ma1 = ma[5];
        ma2 = ma[6];
        ma3 = ma[7];
        ma[4] = (ma0 * mb[0]) + (ma1 * mb[4]) + (ma2 * mb[8]) + (ma3 * mb[12]);
        ma[5] = (ma0 * mb[1]) + (ma1 * mb[5]) + (ma2 * mb[9]) + (ma3 * mb[13]);
        ma[6] = (ma0 * mb[2]) + (ma1 * mb[6]) + (ma2 * mb[10]) + (ma3 * mb[14]);
        ma[7] = (ma0 * mb[3]) + (ma1 * mb[7]) + (ma2 * mb[11]) + (ma3 * mb[15]);

        ma0 = ma[8];
        ma1 = ma[9];
        ma2 = ma[10];
        ma3 = ma[11];
        ma[8] = (ma0 * mb[0]) + (ma1 * mb[4]) + (ma2 * mb[8]) + (ma3 * mb[12]);
        ma[9] = (ma0 * mb[1]) + (ma1 * mb[5]) + (ma2 * mb[9]) + (ma3 * mb[13]);
        ma[10] = (ma0 * mb[2]) + (ma1 * mb[6]) + (ma2 * mb[10]) + (ma3 * mb[14]);
        ma[11] = (ma0 * mb[3]) + (ma1 * mb[7]) + (ma2 * mb[11]) + (ma3 * mb[15]);

        ma0 = ma[12];
        ma1 = ma[13];
        ma2 = ma[14];
        ma3 = ma[15];
        ma[12] = (ma0 * mb[0]) + (ma1 * mb[4]) + (ma2 * mb[8]) + (ma3 * mb[12]);
        ma[13] = (ma0 * mb[1]) + (ma1 * mb[5]) + (ma2 * mb[9]) + (ma3 * mb[13]);
        ma[14] = (ma0 * mb[2]) + (ma1 * mb[6]) + (ma2 * mb[10]) + (ma3 * mb[14]);
        ma[15] = (ma0 * mb[3]) + (ma1 * mb[7]) + (ma2 * mb[11]) + (ma3 * mb[15]);

        return this;
    }

    /**
     * Multiplies this matrix by a matrix specified by individual components.
     *
     * @param m11 matrix element at row 1, column 1
     * @param m12 matrix element at row 1, column 2
     * @param m13 matrix element at row 1, column 3
     * @param m14 matrix element at row 1, column 4
     * @param m21 matrix element at row 2, column 1
     * @param m22 matrix element at row 2, column 2
     * @param m23 matrix element at row 2, column 3
     * @param m24 matrix element at row 2, column 4
     * @param m31 matrix element at row 3, column 1
     * @param m32 matrix element at row 3, column 2
     * @param m33 matrix element at row 3, column 3
     * @param m34 matrix element at row 3, column 4
     * @param m41 matrix element at row 4, column 1
     * @param m42 matrix element at row 4, column 2
     * @param m43 matrix element at row 4, column 3
     * @param m44 matrix element at row 4, column 4
     *
     * @return this matrix with its components multiplied by the specified values
     */
    public Matrix4 multiplyByMatrix(double m11, double m12, double m13, double m14,
                                    double m21, double m22, double m23, double m24,
                                    double m31, double m32, double m33, double m34,
                                    double m41, double m42, double m43, double m44) {

        double[] m = this.m;
        double mr1, mr2, mr3, mr4;

        mr1 = m[0];
        mr2 = m[1];
        mr3 = m[2];
        mr4 = m[3];
        m[0] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31) + (mr4 * m41);
        m[1] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32) + (mr4 * m42);
        m[2] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33) + (mr4 * m43);
        m[3] = (mr1 * m14) + (mr2 * m24) + (mr3 * m34) + (mr4 * m44);

        mr1 = m[4];
        mr2 = m[5];
        mr3 = m[6];
        mr4 = m[7];
        m[4] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31) + (mr4 * m41);
        m[5] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32) + (mr4 * m42);
        m[6] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33) + (mr4 * m43);
        m[7] = (mr1 * m14) + (mr2 * m24) + (mr3 * m34) + (mr4 * m44);

        mr1 = m[8];
        mr2 = m[9];
        mr3 = m[10];
        mr4 = m[11];
        m[8] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31) + (mr4 * m41);
        m[9] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32) + (mr4 * m42);
        m[10] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33) + (mr4 * m43);
        m[11] = (mr1 * m14) + (mr2 * m24) + (mr3 * m34) + (mr4 * m44);

        mr1 = m[12];
        mr2 = m[13];
        mr3 = m[14];
        mr4 = m[15];
        m[12] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31) + (mr4 * m41);
        m[13] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32) + (mr4 * m42);
        m[14] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33) + (mr4 * m43);
        m[15] = (mr1 * m14) + (mr2 * m24) + (mr3 * m34) + (mr4 * m44);

        return this;
    }

    /**
     * Transposes this matrix in place.
     *
     * @return this matrix, transposed.
     */
    public Matrix4 transpose() {

        double[] m = this.m;
        double tmp = m[1];
        m[1] = m[4];
        m[4] = tmp;

        tmp = m[2];
        m[2] = m[8];
        m[8] = tmp;

        tmp = m[3];
        m[3] = m[12];
        m[12] = tmp;

        tmp = m[6];
        m[6] = m[9];
        m[9] = tmp;

        tmp = m[7];
        m[7] = m[13];
        m[13] = tmp;

        tmp = m[11];
        m[11] = m[14];
        m[14] = tmp;

        return this;
    }

    /**
     * Sets this matrix to the transpose of a specified matrix.
     *
     * @param matrix the matrix whose transpose is to be computed
     *
     * @return this matrix with its values set to the transpose of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix in null
     */
    public Matrix4 transposeMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "transposeMatrix", "missingMatrix"));
        }

        this.m[0] = matrix.m[0];
        this.m[1] = matrix.m[4];
        this.m[2] = matrix.m[8];
        this.m[3] = matrix.m[12];

        this.m[4] = matrix.m[1];
        this.m[5] = matrix.m[5];
        this.m[6] = matrix.m[9];
        this.m[7] = matrix.m[13];

        this.m[8] = matrix.m[2];
        this.m[9] = matrix.m[6];
        this.m[10] = matrix.m[10];
        this.m[11] = matrix.m[14];

        this.m[12] = matrix.m[3];
        this.m[13] = matrix.m[7];
        this.m[14] = matrix.m[11];
        this.m[15] = matrix.m[15];

        return this;
    }

    /**
     * Transposes this matrix, storing the result in the specified single precision array. The result is compatible with
     * GLSL uniform matrices, and can be passed to the function glUniformMatrix4fv.
     *
     * @param result a pre-allocated array of length 16 in which to return the transposed components
     *
     * @return the result argument set to the transponsed components
     */
    public float[] transposeToArray(float[] result, int offset) {
        if (result == null || result.length - offset < 16) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "transposeToArray", "missingResult"));
        }

        result[offset++] = (float) this.m[0];
        result[offset++] = (float) this.m[4];
        result[offset++] = (float) this.m[8];
        result[offset++] = (float) this.m[12];

        result[offset++] = (float) this.m[1];
        result[offset++] = (float) this.m[5];
        result[offset++] = (float) this.m[9];
        result[offset++] = (float) this.m[13];

        result[offset++] = (float) this.m[2];
        result[offset++] = (float) this.m[6];
        result[offset++] = (float) this.m[10];
        result[offset++] = (float) this.m[14];

        result[offset++] = (float) this.m[3];
        result[offset++] = (float) this.m[7];
        result[offset++] = (float) this.m[11];
        result[offset] = (float) this.m[15];

        return result;
    }

    /**
     * Inverts this matrix in place.
     * <p/>
     * This throws an exception if this matrix is singular.
     *
     * @return this matrix, inverted
     *
     * @throws IllegalArgumentException If this matrix cannot be inverted
     */
    public Matrix4 invert() {

        boolean success = invert(this.m, this.m); // passing the same array as src and dst is supported

        if (!success) { // the matrix is singular
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertMatrix", "singularMatrix"));
        }

        return this;
    }

    /**
     * Inverts the specified matrix and stores the result in this matrix.
     * <p/>
     * This throws an exception if the specified matrix is singular.
     * <p/>
     * The result of this method is undefined if this matrix is passed in as the matrix to invert.
     *
     * @param matrix the matrix whose inverse is computed
     *
     * @return this matrix set to the inverse of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null or cannot be inverted
     */
    public Matrix4 invertMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertMatrix", "missingMatrix"));
        }

        boolean success = invert(matrix.m, this.m); // store inverse of matrix in this matrix

        if (!success) { // the matrix is singular
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertMatrix", "singularMatrix"));
        }

        return this;
    }

    /**
     * Inverts this orthonormal transform matrix in place. This matrix's upper 3x3 is transposed, then its fourth column
     * is transformed by the transposed upper 3x3 and negated.
     * <p/>
     * The result of this method is undefined if this matrix's values are not consistent with those of an orthonormal
     * transform.
     *
     * @return this matrix, inverted
     */
    public Matrix4 invertOrthonormal() {

        // This is assumed to contain matrix 3D transformation matrix. The upper 3x3 is transposed, the translation
        // components are multiplied by the transposed-upper-3x3 and negated.

        double[] m = this.m;
        double tmp = m[1];
        m[1] = m[4];
        m[4] = tmp;

        tmp = m[2];
        m[2] = m[8];
        m[8] = tmp;

        tmp = m[6];
        m[6] = m[9];
        m[9] = tmp;

        double x = m[3];
        double y = m[7];
        double z = m[11];
        m[3] = -(m[0] * x) - (m[1] * y) - (m[2] * z);
        m[7] = -(m[4] * x) - (m[5] * y) - (m[6] * z);
        m[11] = -(m[8] * x) - (m[9] * y) - (m[10] * z);

        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;

        return this;
    }

    /**
     * Inverts the specified orthonormal transform matrix and stores the result in 'this' matrix. The specified matrix's
     * upper 3x3 is transposed, then its fourth column is transformed by the transposed upper 3x3 and negated.  The
     * result is stored in 'this' matrix.
     * <p/>
     * The result of this method is undefined if this matrix is passed in as the matrix to invert, or if the matrix's
     * values are not consistent with those of an orthonormal transform.
     *
     * @param matrix the matrix whose inverse is computed. The matrix is assumed to represent an orthonormal transform
     *               matrix.
     *
     * @return this matrix set to the inverse of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Matrix4 invertOrthonormalMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "invertOrthonormalMatrix", "missingMatrix"));
        }

        // The matrix is assumed to contain matrix 3D transformation matrix. The upper 3x3 is transposed, the translation
        // components are multiplied by the transposed-upper-3x3 and negated.

        this.m[0] = matrix.m[0];
        this.m[1] = matrix.m[4];
        this.m[2] = matrix.m[8];
        this.m[3] = -(matrix.m[0] * matrix.m[3]) - (matrix.m[4] * matrix.m[7]) - (matrix.m[8] * matrix.m[11]);

        this.m[4] = matrix.m[1];
        this.m[5] = matrix.m[5];
        this.m[6] = matrix.m[9];
        this.m[7] = -(matrix.m[1] * matrix.m[3]) - (matrix.m[5] * matrix.m[7]) - (matrix.m[9] * matrix.m[11]);

        this.m[8] = matrix.m[2];
        this.m[9] = matrix.m[6];
        this.m[10] = matrix.m[10];
        this.m[11] = -(matrix.m[2] * matrix.m[3]) - (matrix.m[6] * matrix.m[7]) - (matrix.m[10] * matrix.m[11]);

        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Applies a specified depth offset to this projection matrix. The depth offset may be any real number and is
     * typically used to draw geometry slightly closer to the user's eye in order to give those shapes visual priority
     * over nearby or geometry. An offset of zero has no effect. An offset less than zero brings depth values closer to
     * the eye, while an offset greater than zero pushes depth values away from the eye.
     * <p/>
     * The result of this method is undefined if this matrix is not a projection matrix. Projection matrices can be
     * created by calling <code>setToPerspectiveProjection</code> or <code>setToScreenProjection</code>
     * <p/>
     * Depth offset may be applied to both perspective and screen projection matrices. The effect on each type is
     * outlined here:
     * <p/>
     * <strong>Perspective Projection</strong>
     * <p/>
     * The effect of depth offset on a perspective projection increases exponentially with distance from the eye. This
     * has the effect of adjusting the offset for the loss in depth precision with geometry drawn further from the eye.
     * Distant geometry requires a greater offset to differentiate itself from nearby geometry, while close geometry
     * does not.
     * <p/>
     * <strong>Screen Projection</strong>
     * <p/>
     * The effect of depth offset on an screen projection increases linearly with distance from the eye. While it is
     * reasonable to apply a depth offset to an screen projection, the effect is most appropriate when applied to the
     * projection used to draw the scene. For example, when an object's coordinates are projected by a perspective
     * projection into screen coordinates then drawn using a screen projection, it is best to apply the offset to the
     * original perspective projection. The method <code>RenderContext.project</code> performs the correct behavior for
     * the projection type used to draw the scene.
     *
     * @param depthOffset the amount of offset to apply
     *
     * @return this matrix with its components adjusted to account for the specified depth offset
     */
    public Matrix4 offsetProjectionDepth(double depthOffset) {

        this.m[10] *= 1 + depthOffset;

        return this;
    }

    /**
     * Returns this viewing matrix's eye point. In model coordinates, a viewing matrix's eye point is the point the
     * viewer is looking from and maps to the center of the screen.
     * <p/>
     * The result of this method is undefined if this matrix is not a viewing matrix.
     *
     * @param result a pre-allocated <code>Vec3</code> in which to return the extracted value
     *
     * @return the specified result argument containing the viewing matrix's eye point
     *
     * @throws IllegalArgumentException If the result argument is null
     */
    public Vec3 extractEyePoint(Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "extractEyePoint", "missingResult"));
        }

        // The eye point of a modelview matrix is computed by transforming the origin (0, 0, 0, 1) by the matrix's
        // inverse. This is equivalent to transforming the inverse of this matrix's translation components in the
        // rightmost column by the transpose of its upper 3x3 components.
        result.x = -(this.m[0] * this.m[3]) - (this.m[4] * this.m[7]) - (this.m[8] * this.m[11]);
        result.y = -(this.m[1] * this.m[3]) - (this.m[5] * this.m[7]) - (this.m[9] * this.m[11]);
        result.z = -(this.m[2] * this.m[3]) - (this.m[6] * this.m[7]) - (this.m[10] * this.m[11]);

        return result;
    }

    /**
     * Returns this viewing matrix's forward vector.
     * <p/>
     * The result of this method is undefined if this matrix is not a viewing matrix.
     *
     * @param result a pre-allocated <code>Vec3</code> in which to return the extracted value
     *
     * @return the specified result argument containing the viewing matrix's forward vector
     *
     * @throws IllegalArgumentException If the result argument is null
     */
    public Vec3 extractForwardVector(Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "extractForwardVector", "missingResult"));
        }

        // The forward vector of a modelview matrix is computed by transforming the negative Z axis (0, 0, -1, 0) by the
        // matrix's inverse. We have pre-computed the result inline here to simplify this computation.
        result.x = -this.m[8];
        result.y = -this.m[9];
        result.z = -this.m[10];

        return result;
    }

    /**
     * Returns this viewing matrix's heading angle in degrees. The roll argument enables the caller to disambiguate
     * heading and roll when the two rotation axes for heading and roll are parallel, causing gimbal lock.
     * <p>
     * The result of this method is undefined if this matrix is not a viewing matrix.
     *
     * @param roll the viewing matrix's roll angle in degrees, or 0 if the roll angle is unknown
     *
     * @return the extracted heading angle in degrees
     */
    public double extractHeading(double roll) {
        double rad = Math.toRadians(roll);
        double cr = Math.cos(rad);
        double sr = Math.sin(rad);

        double ch = (cr * this.m[0]) - (sr * this.m[4]);
        double sh = (sr * this.m[5]) - (cr * this.m[1]);
        return Math.toDegrees(Math.atan2(sh, ch));
    }

    /**
     * Returns this viewing matrix's tilt angle in degrees.
     * <p>
     * The result of this method is undefined if this matrix is not a viewing matrix.
     *
     * @return the extracted heading angle in degrees
     */
    public double extractTilt() {
        double ct = this.m[10];
        double st = Math.sqrt(m[2] * m[2] + m[6] * m[6]);
        return Math.toDegrees(Math.atan2(st, ct));
    }

    /**
     * Returns this symmetric matrix's eigenvectors. The eigenvectors are returned in the specified result arguments in
     * order of descending magnitude (most prominent to least prominent). Each eigenvector has length equal to its
     * corresponding eigenvalue.
     * <p/>
     * This method returns false if this matrix is not a symmetric matrix.
     *
     * @param result1 a pre-allocated Vec3 in which to return the most prominent eigenvector
     * @param result2 a pre-allocated Vec3 in which to return the second most prominent eigenvector
     * @param result3 a pre-allocated Vec3 in which to return the least prominent eigenvector
     *
     * @return true if this matrix is symmetric and its eigenvectors can be determined, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null or if this matrix is not symmetric
     */
    public boolean extractEigenvectors(Vec3 result1, Vec3 result2, Vec3 result3) {
        if (result1 == null || result2 == null || result3 == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "extractEigenvectors", "missingResult"));
        }

        if (this.m[1] != this.m[4] || this.m[2] != this.m[8] || this.m[6] != this.m[9]) {
            return false; // matrix is not symmetric
        }

        // Taken from Mathematics for 3D Game Programming and Computer Graphics, Second Edition,
        // listing 14.6.

        final double EPSILON = 1.0e-10;
        final int MAX_SWEEPS = 32;

        // Since the matrix is symmetric m12=m21, m13=m31 and m23=m32, therefore we can ignore the values m21,
        // m32 and m32.
        double m11 = this.m[0];
        double m12 = this.m[1];
        double m13 = this.m[2];
        double m22 = this.m[5];
        double m23 = this.m[6];
        double m33 = this.m[10];

        double[][] r = new double[3][3];
        r[0][0] = r[1][1] = r[2][2] = 1d;

        for (int a = 0; a < MAX_SWEEPS; a++) {
            // Exit if off-diagonal entries small enough
            if (Math.abs(m12) < EPSILON && Math.abs(m13) < EPSILON && Math.abs(m23) < EPSILON) {
                break;
            }

            // Annihilate (1,2) entry.
            if (m12 != 0) {
                double u = (m22 - m11) * 0.5 / m12;
                double u2 = u * u;
                double u2p1 = u2 + 1;
                double t = (u2p1 != u2) ? ((u < 0) ? -1 : 1) * (Math.sqrt(u2p1) - Math.abs(u)) : 0.5 / u;
                double c = 1 / Math.sqrt(t * t + 1);
                double s = c * t;

                m11 -= t * m12;
                m22 += t * m12;
                m12 = 0;

                double temp = c * m13 - s * m23;
                m23 = s * m13 + c * m23;
                m13 = temp;

                for (int i = 0; i < 3; i++) {
                    temp = c * r[i][0] - s * r[i][1];
                    r[i][1] = s * r[i][0] + c * r[i][1];
                    r[i][0] = temp;
                }
            }

            // Annihilate (1,3) entry.
            if (m13 != 0) {
                double u = (m33 - m11) * 0.5 / m13;
                double u2 = u * u;
                double u2p1 = u2 + 1;
                double t = (u2p1 != u2) ? ((u < 0) ? -1 : 1) * (Math.sqrt(u2p1) - Math.abs(u)) : 0.5 / u;
                double c = 1 / Math.sqrt(t * t + 1);
                double s = c * t;

                m11 -= t * m13;
                m33 += t * m13;
                m13 = 0;

                double temp = c * m12 - s * m23;
                m23 = s * m12 + c * m23;
                m12 = temp;

                for (int i = 0; i < 3; i++) {
                    temp = c * r[i][0] - s * r[i][2];
                    r[i][2] = s * r[i][0] + c * r[i][2];
                    r[i][0] = temp;
                }
            }

            // Annihilate (2,3) entry.
            if (m23 != 0) {
                double u = (m33 - m22) * 0.5 / m23;
                double u2 = u * u;
                double u2p1 = u2 + 1;
                double t = (u2p1 != u2) ? ((u < 0) ? -1 : 1) * (Math.sqrt(u2p1) - Math.abs(u)) : 0.5 / u;
                double c = 1 / Math.sqrt(t * t + 1);
                double s = c * t;

                m22 -= t * m23;
                m33 += t * m23;
                m23 = 0;

                double temp = c * m12 - s * m13;
                m13 = s * m12 + c * m13;
                m12 = temp;

                for (int i = 0; i < 3; i++) {
                    temp = c * r[i][1] - s * r[i][2];
                    r[i][2] = s * r[i][1] + c * r[i][2];
                    r[i][1] = temp;
                }
            }
        }

        // Sort the eigenvectors by descending magnitude.

        int i1 = 0;
        int i2 = 1;
        int i3 = 2;
        int itemp;
        double temp;

        if (m11 < m22) {
            temp = m11;
            m11 = m22;
            m22 = temp;

            itemp = i1;
            i1 = i2;
            i2 = itemp;
        }

        if (m22 < m33) {
            temp = m22;
            m22 = m33;
            m33 = temp;

            itemp = i2;
            i2 = i3;
            i3 = itemp;
        }

        if (m11 < m22) {
            temp = m11;
            m11 = m22;
            m22 = temp;

            itemp = i1;
            i1 = i2;
            i2 = itemp;
        }

        result1.set(r[0][i1], r[1][i1], r[2][i1]);
        result2.set(r[0][i2], r[1][i2], r[2][i2]);
        result3.set(r[0][i3], r[1][i3], r[2][i3]);

        result1.normalize();
        result2.normalize();
        result3.normalize();

        result1.multiply(m11);
        result2.multiply(m22);
        result3.multiply(m33);

        return true;
    }

    /**
     * Projects a Cartesian point to screen coordinates. This method assumes this matrix represents an inverse
     * modelview-projection matrix. The result of this method is undefined if this matrix is not an inverse
     * modelview-projection matrix.
     * <p/>
     * The resultant screen point is in OpenGL screen coordinates, with the origin in the bottom-left corner and axes
     * that extend up and to the right from the origin.
     * <p/>
     * This stores the projected point in the result argument, and returns a boolean value indicating whether or not the
     * projection is successful. This returns false if the Cartesian point is clipped by the near clipping plane or the
     * far clipping plane.
     *
     * @param x        the Cartesian point's X component
     * @param y        the Cartesian point's y component
     * @param z        the Cartesian point's z component
     * @param viewport the viewport defining the screen point's coordinate system
     * @param result   a pre-allocated {@link Vec3} in which to return the projected point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public boolean project(double x, double y, double z, Viewport viewport, Vec3 result) {
        if (viewport == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "project", "missingViewport"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "project", "missingResult"));
        }

        // Transform the model point from model coordinates to eye coordinates then to clip coordinates. This inverts
        // the Z axis and stores the negative of the eye coordinate Z value in the W coordinate.
        double[] m = this.m;
        double sx = m[0] * x + m[1] * y + m[2] * z + m[3];
        double sy = m[4] * x + m[5] * y + m[6] * z + m[7];
        double sz = m[8] * x + m[9] * y + m[10] * z + m[11];
        double sw = m[12] * x + m[13] * y + m[14] * z + m[15];

        if (sw == 0) {
            return false;
        }

        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1].
        sx /= sw;
        sy /= sw;
        sz /= sw;

        // Clip the point against the near and far clip planes.
        if (sz < -1 || sz > 1) {
            return false;
        }

        // Convert the point from clip coordinate to the range [0,1]. This enables the X and Y coordinates to be
        // converted to screen coordinates, and the Z coordinate to represent a depth value in the range[0,1].
        sx = sx * 0.5 + 0.5;
        sy = sy * 0.5 + 0.5;
        sz = sz * 0.5 + 0.5;

        // Convert the X and Y coordinates from the range [0,1] to screen coordinates.
        sx = sx * viewport.width + viewport.x;
        sy = sy * viewport.height + viewport.y;

        result.x = sx;
        result.y = sy;
        result.z = sz;

        return true;
    }

    /**
     * Un-projects a screen coordinate point to Cartesian coordinates at the near clip plane and the far clip plane.
     * This method assumes this matrix represents an inverse modelview-projection matrix. The result of this method is
     * undefined if this matrix is not an inverse modelview-projection matrix.
     * <p/>
     * The screen point is understood to be in OpenGL screen coordinates, with the origin in the bottom-left corner and
     * axes that extend up and to the right from the origin.
     * <p/>
     * This function stores the un-projected points in the result argument, and a boolean value indicating whether the
     * un-projection is successful.
     *
     * @param x          the screen point's X component
     * @param y          the screen point's Y component
     * @param viewport   the viewport defining the screen point's coordinate system
     * @param nearResult a pre-allocated {@link Vec3} in which to return the un-projected near clip plane point
     * @param farResult  a pre-allocated {@link Vec3} in which to return the un-projected far clip plane point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public boolean unProject(double x, double y, Viewport viewport, Vec3 nearResult, Vec3 farResult) {
        if (viewport == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "unProject", "missingViewport"));
        }

        if (nearResult == null || farResult == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "unProject", "missingResult"));
        }

        // Convert the XY screen coordinates to coordinates in the range [0, 1]. This enables the XY coordinates to
        // be converted to clip coordinates.
        double sx = (x - viewport.x) / viewport.width;
        double sy = (y - viewport.y) / viewport.height;

        // Convert from coordinates in the range [0, 1] to clip coordinates in the range [-1, 1].
        sx = sx * 2 - 1;
        sy = sy * 2 - 1;

        // Transform the screen point from clip coordinates to model coordinates. This is a partial transformation that
        // factors out the contribution from the screen point's X and Y components. The contribution from the Z
        // component, which is both -1 and +1, is included next.
        double[] m = this.m;
        double mx = (m[0] * sx) + (m[1] * sy) + m[3];
        double my = (m[4] * sx) + (m[5] * sy) + m[7];
        double mz = (m[8] * sx) + (m[9] * sy) + m[11];
        double mw = (m[12] * sx) + (m[13] * sy) + m[15];

        // Transform the screen point at the near clip plane (z = -1) to model coordinates.
        double nx = mx - m[2];
        double ny = my - m[6];
        double nz = mz - m[10];
        double nw = mw - m[14];

        // Transform the screen point at the far clip plane (z = +1) to model coordinates.
        double fx = mx + m[2];
        double fy = my + m[6];
        double fz = mz + m[10];
        double fw = mw + m[14];

        if (nw == 0 || fw == 0) {
            return false;
        }

        // Complete the conversion from near clip coordinates to model coordinates by dividing by the W component.
        nearResult.x = nx / nw;
        nearResult.y = ny / nw;
        nearResult.z = nz / nw;

        // Complete the conversion from far clip coordinates to model coordinates by dividing by the W component.
        farResult.x = fx / fw;
        farResult.y = fy / fw;
        farResult.z = fz / fw;

        return true;
    }

    /**
     * Inverts a 4 x 4 matrix, storing the result in a destination argument. The source and destination arguments
     * represent a 4 x 4 matrix with a one-dimensional array in row-major order. The source and destination may
     * reference the same array.
     *
     * @param src the matrix components to invert in row-major order
     * @param dst the inverted components in row-major order
     *
     * @return true if the matrix was successfully inverted, false otherwise
     */
    protected static boolean invert(double[] src, double[] dst) {
        // Copy the specified matrix into a mutable two-dimensional array.
        double[][] A = new double[4][4];
        A[0][0] = src[0];
        A[0][1] = src[1];
        A[0][2] = src[2];
        A[0][3] = src[3];
        A[1][0] = src[4];
        A[1][1] = src[5];
        A[1][2] = src[6];
        A[1][3] = src[7];
        A[2][0] = src[8];
        A[2][1] = src[9];
        A[2][2] = src[10];
        A[2][3] = src[11];
        A[3][0] = src[12];
        A[3][1] = src[13];
        A[3][2] = src[14];
        A[3][3] = src[15];

        int[] index = new int[4];
        double d = ludcmp(A, index);

        // Compute the matrix's determinant.
        for (int i = 0; i < 4; i += 1) {
            d *= A[i][i];
        }

        // The matrix is singular if its determinant is zero or very close to zero.
        final double NEAR_ZERO_THRESHOLD = 1.0e-8;
        if (Math.abs(d) < NEAR_ZERO_THRESHOLD) {
            return false;
        }

        double[][] Y = new double[4][4];
        double[] col = new double[4];
        for (int j = 0; j < 4; j += 1) {
            for (int i = 0; i < 4; i += 1) {
                col[i] = 0.0;
            }

            col[j] = 1.0;
            lubksb(A, index, col);

            for (int i = 0; i < 4; i += 1) {
                Y[i][j] = col[i];
            }
        }

        dst[0] = Y[0][0];
        dst[1] = Y[0][1];
        dst[2] = Y[0][2];
        dst[3] = Y[0][3];

        dst[4] = Y[1][0];
        dst[5] = Y[1][1];
        dst[6] = Y[1][2];
        dst[7] = Y[1][3];

        dst[8] = Y[2][0];
        dst[9] = Y[2][1];
        dst[10] = Y[2][2];
        dst[11] = Y[2][3];

        dst[12] = Y[3][0];
        dst[13] = Y[3][1];
        dst[14] = Y[3][2];
        dst[15] = Y[3][3];

        return true;
    }

    /**
     * Utility method to perform an LU factorization of a matrix. Algorithm derived from "Numerical Recipes in C", Press
     * et al., 1988.
     *
     * @param A     matrix to be factored
     * @param index permutation vector
     *
     * @return condition number of matrix
     */
    protected static double ludcmp(double[][] A, int[] index) {
        final double TINY = 1.0e-20;
        double[] vv = new double[4];
        double d = 1;
        double temp, sum;

        for (int i = 0; i < 4; i += 1) {
            double big = 0;
            for (int j = 0; j < 4; j += 1) {
                if ((temp = Math.abs(A[i][j])) > big) {
                    big = temp;
                }
            }

            if (big == 0) {
                return 0; // Matrix is singular if the entire row contains zero.
            } else {
                vv[i] = 1 / big;
            }
        }

        for (int j = 0; j < 4; j += 1) {
            for (int i = 0; i < j; i += 1) {
                sum = A[i][j];
                for (int k = 0; k < i; k += 1) {
                    sum -= A[i][k] * A[k][j];
                }

                A[i][j] = sum;
            }

            double big = 0;
            double dum;
            int imax = -1;

            for (int i = j; i < 4; i += 1) {
                sum = A[i][j];
                for (int k = 0; k < j; k++) {
                    sum -= A[i][k] * A[k][j];
                }

                A[i][j] = sum;

                if ((dum = vv[i] * Math.abs(sum)) >= big) {
                    big = dum;
                    imax = i;
                }
            }

            if (j != imax) {
                for (int k = 0; k < 4; k += 1) {
                    dum = A[imax][k];
                    A[imax][k] = A[j][k];
                    A[j][k] = dum;
                }

                d = -d;
                vv[imax] = vv[j];
            }

            index[j] = imax;
            if (A[j][j] == 0.0)
                A[j][j] = TINY;

            if (j != 3) {
                dum = 1.0 / A[j][j];
                for (int i = j + 1; i < 4; i += 1) {
                    A[i][j] *= dum;
                }
            }
        }

        return d;
    }

    /**
     * Utility method to solve a linear system with an LU factorization of a matrix. Solves Ax=b, where A is in LU
     * factorized form. Algorithm derived from "Numerical Recipes in C", Press et al., 1988
     *
     * @param A     an LU factorization of a matrix
     * @param index permutation vector of that LU factorization
     * @param b     vector to be solved
     */
    protected static void lubksb(double[][] A, int[] index, double[] b) {
        int ii = -1;

        for (int i = 0; i < 4; i += 1) {
            int ip = index[i];
            double sum = b[ip];
            b[ip] = b[i];

            if (ii != -1) {
                for (int j = ii; j <= i - 1; j += 1) {
                    sum -= A[i][j] * b[j];
                }
            } else if (sum != 0.0) {
                ii = i;
            }

            b[i] = sum;
        }

        for (int i = 3; i >= 0; i -= 1) {
            double sum = b[i];
            for (int j = i + 1; j < 4; j += 1) {
                sum -= A[i][j] * b[j];
            }

            b[i] = sum / A[i][i];
        }
    }
}
