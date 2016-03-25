/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;


import java.util.Arrays;

import gov.nasa.worldwind.util.Logger;

/**
 * 3 x 3 matrix in row-major order.
 */
public class Matrix3 {

    /**
     * The components for the 3 x 3 identity matrix, stored in row-major order.
     */
    protected static final double[] identity = new double[]{
        1, 0, 0,
        0, 1, 0,
        0, 0, 1};

    /**
     * The matrix's components, stored in row-major order. Initialized to the 3 x 3 identity matrix.
     */
    public final double[] m = new double[]{
        1, 0, 0,
        0, 1, 0,
        0, 0, 1};

    /**
     * Constructs a 3 x 3 identity matrix.
     */
    public Matrix3() {
    }

    /**
     * Constructs a 3 x 3 matrix with specified components.
     *
     * @param m11 matrix element at row 1, column 1
     * @param m12 matrix element at row 1, column 2
     * @param m13 matrix element at row 1, column 3
     * @param m21 matrix element at row 2, column 1
     * @param m22 matrix element at row 2, column 2
     * @param m23 matrix element at row 2, column 3
     * @param m31 matrix element at row 3, column 1
     * @param m32 matrix element at row 3, column 2
     * @param m33 matrix element at row 3, column 3
     */
    public Matrix3(double m11, double m12, double m13,
                   double m21, double m22, double m23,
                   double m31, double m32, double m33) {
        this.m[0] = m11;
        this.m[1] = m12;
        this.m[2] = m13;

        this.m[3] = m21;
        this.m[4] = m22;
        this.m[5] = m23;

        this.m[6] = m31;
        this.m[7] = m32;
        this.m[8] = m33;
    }

    /**
     * Constructs a 3 x 3 matrix with the components of a specified matrix.
     *
     * @param matrix the matrix specifying the new components
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Matrix3(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix3", "constructor", "missingMatrix"));
        }

        System.arraycopy(matrix.m, 0, this.m, 0, 9);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Matrix3 that = (Matrix3) o;
        return this.m[0] == that.m[0]
            && this.m[1] == that.m[1]
            && this.m[2] == that.m[2]
            && this.m[3] == that.m[3]
            && this.m[4] == that.m[4]
            && this.m[5] == that.m[5]
            && this.m[6] == that.m[6]
            && this.m[7] == that.m[7]
            && this.m[8] == that.m[8];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.m);
    }

    @Override
    public String toString() {
        return "[" + this.m[0] + ", " + this.m[1] + ", " + this.m[2] + "], " +
            '[' + this.m[3] + ", " + this.m[4] + ", " + this.m[5] + "], " +
            '[' + this.m[6] + ", " + this.m[7] + ", " + this.m[8] + ']';
    }

    /**
     * Sets this 3 x 3 matrix to specified components.
     *
     * @param m11 matrix element at row 1, column 1
     * @param m12 matrix element at row 1, column 2
     * @param m13 matrix element at row 1, column 3
     * @param m21 matrix element at row 2, column 1
     * @param m22 matrix element at row 2, column 2
     * @param m23 matrix element at row 2, column 3
     * @param m31 matrix element at row 3, column 1
     * @param m32 matrix element at row 3, column 2
     * @param m33 matrix element at row 3, column 3
     *
     * @return this matrix set to the specified components
     */
    public Matrix3 set(double m11, double m12, double m13,
                       double m21, double m22, double m23,
                       double m31, double m32, double m33) {
        this.m[0] = m11;
        this.m[1] = m12;
        this.m[2] = m13;

        this.m[3] = m21;
        this.m[4] = m22;
        this.m[5] = m23;

        this.m[6] = m31;
        this.m[7] = m32;
        this.m[8] = m33;

        return this;
    }

    /**
     * Sets this 3 x 3 matrix to the components of a specified matrix.
     *
     * @param matrix the matrix specifying the new components
     *
     * @return this matrix with its components set to that of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Matrix3 set(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix3", "set", "missingMatrix"));
        }

        System.arraycopy(matrix.m, 0, this.m, 0, 9);

        return this;
    }

    /**
     * Sets the translation components of this matrix to specified values.
     *
     * @param x the X translation component
     * @param y the Y translation component
     *
     * @return this matrix with its translation components set to the specified values and all other components
     * unmodified
     */
    public Matrix3 setTranslation(double x, double y) {
        this.m[2] = x;
        this.m[5] = y;

        return this;
    }

    /**
     * Sets the rotation components of this matrix to a specified angle. Positive angles are interpreted as
     * counter-clockwise rotation.
     *
     * @param angleDegrees the angle of rotation in degrees
     *
     * @return this matrix with its rotation components set to the specified values and all other components unmodified
     */
    public Matrix3 setRotation(double angleDegrees) {
        double c = Math.cos(Math.toRadians(angleDegrees));
        double s = Math.sin(Math.toRadians(angleDegrees));

        this.m[0] = c;
        this.m[1] = -s;

        this.m[3] = s;
        this.m[4] = c;

        return this;
    }

    /**
     * Sets the scale components of this matrix to specified values.
     *
     * @param xScale the X scale component
     * @param yScale the Y scale component
     *
     * @return this matrix with its scale components set to the specified values and all other components unmodified
     */
    public Matrix3 setScale(double xScale, double yScale) {
        this.m[0] = xScale;
        this.m[4] = yScale;

        return this;
    }

    /**
     * Sets this matrix to the 3 x 3 identity matrix.
     *
     * @return this matrix, set to the identity matrix
     */
    public Matrix3 setToIdentity() {
        System.arraycopy(identity, 0, this.m, 0, 9);

        return this;
    }

    /**
     * Sets this matrix to a translation matrix with specified translation components.
     *
     * @param x the X translation component
     * @param y the Y translation component
     *
     * @return this matrix with its translation components set to those specified and all other components set to that
     * of an identity matrix
     */
    public Matrix3 setToTranslation(double x, double y) {
        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = x;

        this.m[3] = 0;
        this.m[4] = 1;
        this.m[5] = y;

        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 1;

        return this;
    }

    /**
     * Sets this matrix to a rotation matrix with a specified angle. Positive angles are interpreted as
     * counter-clockwise rotation.
     *
     * @param angleDegrees the angle of rotation in degrees
     *
     * @return this matrix with its rotation components set to those specified and all other components set to that of
     * an identity matrix
     */
    public Matrix3 setToRotation(double angleDegrees) {
        double c = Math.cos(Math.toRadians(angleDegrees));
        double s = Math.sin(Math.toRadians(angleDegrees));

        this.m[0] = c;
        this.m[1] = -s;
        this.m[2] = 0;

        this.m[3] = s;
        this.m[4] = c;
        this.m[5] = 0;

        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 1;

        return this;
    }

    /**
     * Sets this matrix to a scale matrix with specified scale components.
     *
     * @param xScale the X scale component
     * @param yScale the Y scale component
     *
     * @return this matrix with its scale components set to those specified and all other components set to that of an
     * identity matrix
     */
    public Matrix3 setToScale(double xScale, double yScale) {

        this.m[0] = xScale;
        this.m[1] = 0;
        this.m[2] = 0;

        this.m[3] = 0;
        this.m[4] = yScale;
        this.m[5] = 0;

        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 1;

        return this;
    }

    /**
     * Sets this matrix to one that flips and shifts the y-axis. The resultant matrix maps Y=0 to Y=1 and Y=1 to Y=0.
     * All existing values are overwritten. This matrix is usually used to change the coordinate origin from an upper
     * left coordinate origin to a lower left coordinate origin.
     * <p/>
     * This matrix is typically necessary to align the coordinate system of images (top-left origin) with that of OpenGL
     * (bottom-left origin).
     *
     * @return this matrix set to values described above
     */
    public Matrix3 setToVerticalFlip() {

        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = 0;

        this.m[3] = 0;
        this.m[4] = -1;
        this.m[5] = 1;

        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 1;

        return this;
    }

    /**
     * Sets this matrix to one that transforms normalized coordinates from a source sector to a destination sector.
     * Normalized coordinates within a sector range from 0 to 1, with (0, 0) indicating the lower left corner and (1, 1)
     * indicating the upper right. The resultant matrix maps a normalized source coordinate (X, Y) to its corresponding
     * normalized destination coordinate (X', Y').
     * <p/>
     * This matrix typically necessary to transform texture coordinates from one geographic region to another. For
     * example, the texture coordinates for a terrain tile spanning one region must be transformed to coordinates
     * appropriate for an image tile spanning a potentially different region.
     *
     * @param src the source sector
     * @param dst the destination sector
     *
     * @return this matrix set to values described above
     */
    public Matrix3 setToTileTransform(Sector src, Sector dst) {
        if (src == null || dst == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "setToNormalizedGeographicTransform", "missingSector"));
        }

        double srcDeltaLat = src.deltaLatitude();
        double srcDeltaLon = src.deltaLongitude();
        double dstDeltaLat = dst.deltaLatitude();
        double dstDeltaLon = dst.deltaLongitude();

        double xs = srcDeltaLon / dstDeltaLon;
        double ys = srcDeltaLat / dstDeltaLat;
        double xt = (src.minLongitude - dst.minLongitude) / dstDeltaLon;
        double yt = (src.minLatitude - dst.minLatitude) / dstDeltaLat;

        this.m[0] = xs;
        this.m[1] = 0;
        this.m[2] = xt;

        this.m[3] = 0;
        this.m[4] = ys;
        this.m[5] = yt;

        this.m[6] = 0;
        this.m[7] = 0;
        this.m[8] = 1;

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
    public Matrix3 setToMultiply(Matrix3 a, Matrix3 b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix3", "setToMultiply", "missingMatrix"));
        }

        double[] ma = a.m;
        double[] mb = b.m;

        this.m[0] = (ma[0] * mb[0]) + (ma[1] * mb[3]) + (ma[2] * mb[6]);
        this.m[1] = (ma[0] * mb[1]) + (ma[1] * mb[4]) + (ma[2] * mb[7]);
        this.m[2] = (ma[0] * mb[2]) + (ma[1] * mb[5]) + (ma[2] * mb[8]);

        this.m[3] = (ma[3] * mb[0]) + (ma[4] * mb[3]) + (ma[5] * mb[6]);
        this.m[4] = (ma[3] * mb[1]) + (ma[4] * mb[4]) + (ma[5] * mb[7]);
        this.m[5] = (ma[3] * mb[2]) + (ma[4] * mb[5]) + (ma[5] * mb[8]);

        this.m[6] = (ma[6] * mb[0]) + (ma[7] * mb[3]) + (ma[8] * mb[6]);
        this.m[7] = (ma[6] * mb[1]) + (ma[7] * mb[4]) + (ma[8] * mb[7]);
        this.m[8] = (ma[6] * mb[2]) + (ma[7] * mb[5]) + (ma[8] * mb[8]);

        return this;
    }

    /**
     * Multiplies this matrix by a translation matrix with specified translation values.
     *
     * @param x the X translation component
     * @param y the Y translation component
     *
     * @return this matrix multiplied by the translation matrix implied by the specified values
     */
    public Matrix3 multiplyByTranslation(double x, double y) {

        this.multiplyByMatrix(
            1, 0, x,
            0, 1, y,
            0, 0, 1);

        return this;
    }

    /**
     * Multiplies this matrix by a rotation matrix about a specified axis and angle. Positive angles are interpreted as
     * counter-clockwise rotation.
     *
     * @param angleDegrees the angle of rotation in degrees
     *
     * @return this matrix multiplied by the rotation matrix implied by the specified values
     */
    public Matrix3 multiplyByRotation(double angleDegrees) {

        double c = Math.cos(Math.toRadians(angleDegrees));
        double s = Math.sin(Math.toRadians(angleDegrees));

        this.multiplyByMatrix(
            c, -s, 0,
            s, c, 0,
            0, 0, 1);

        return this;
    }

    /**
     * Multiplies this matrix by a scale matrix with specified values.
     *
     * @param xScale the X scale component
     * @param yScale the Y scale component
     *
     * @return this matrix multiplied by the scale matrix implied by the specified values
     */
    public Matrix3 multiplyByScale(double xScale, double yScale) {

        this.multiplyByMatrix(
            xScale, 0, 0,
            0, yScale, 0,
            0, 0, 1);

        return this;
    }

    /**
     * Multiplies this matrix by a matrix that flips and shifts the y-axis. The vertical flip matrix maps Y=0 to Y=1 and
     * Y=1 to Y=0. This matrix is usually used to change the coordinate origin from an upper left coordinate origin to a
     * lower left coordinate origin.
     * <p/>
     * This is typically necessary to align the coordinate system of images (top-left origin) with that of OpenGL
     * (bottom-left origin).
     *
     * @return this matrix multiplied by a vertical flip matrix implied by values described above
     */
    public Matrix3 multiplyByVerticalFlip() {

        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //     1, 0, 0
        //     0, -1, 1,
        //     0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the matrix's components, reducing
        // the total number of primitive operations from 63 to 6.

        double[] m = this.m;

        // Must be done before modifying m1, m4, m7 below.
        m[2] += m[1];
        m[5] += m[4];
        m[8] += m[7];

        m[1] = -m[1];
        m[4] = -m[4];
        m[7] = -m[7];

        return this;
    }

    /**
     * Multiplies this matrix by a matrix that transforms normalized coordinates from a source sector to a destination
     * sector. Normalized coordinates within a sector range from 0 to 1, with (0, 0) indicating the lower left corner
     * and (1, 1) indicating the upper right. The resultant matrix maps a normalized source coordinate (X, Y) to its
     * corresponding normalized destination coordinate (X', Y').
     * <p/>
     * This matrix typically necessary to transform texture coordinates from one geographic region to another. For
     * example, the texture coordinates for a terrain tile spanning one region must be transformed to coordinates
     * appropriate for an image tile spanning a potentially different region.
     *
     * @param src the source sector
     * @param dst the destination sector
     *
     * @return this matrix multiplied by the transform matrix implied by values described above
     */
    public Matrix3 multiplyByTileTransform(Sector src, Sector dst) {
        if (src == null || dst == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "multiplyByNormalizedGeographicTransform", "missingSector"));
        }

        double srcDeltaLat = src.deltaLatitude();
        double srcDeltaLon = src.deltaLongitude();
        double dstDeltaLat = dst.deltaLatitude();
        double dstDeltaLon = dst.deltaLongitude();

        double xs = srcDeltaLon / dstDeltaLon;
        double ys = srcDeltaLat / dstDeltaLat;
        double xt = (src.minLongitude - dst.minLongitude) / dstDeltaLon;
        double yt = (src.minLatitude - dst.minLatitude) / dstDeltaLat;

        // This is equivalent to the following operation, but is potentially much faster:
        //
        // multiplyByMatrix(
        //     xs, 0, xt
        //     0, ys, yt,
        //     0, 0, 1);
        //
        // This inline version eliminates unnecessary multiplication by 1 and 0 in the matrix's components, reducing
        // the total number of primitive operations from 63 to 18.

        double[] m = this.m;

        // Must be done before modifying m0, m1, etc. below.
        m[2] += (m[0] * xt) + (m[1] * yt);
        m[5] += (m[3] * xt) + (m[4] * yt);
        m[8] += (m[6] * xt) + (m[6] * yt);

        m[0] *= xs;
        m[1] *= ys;

        m[3] *= xs;
        m[4] *= ys;

        m[6] *= xs;
        m[7] *= ys;

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
    public Matrix3 multiplyByMatrix(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix3", "multiplyByMatrix", "missingMatrix"));
        }

        double[] ma = this.m;
        double[] mb = matrix.m;
        double ma0, ma1, ma2;

        ma0 = ma[0];
        ma1 = ma[1];
        ma2 = ma[2];
        ma[0] = (ma0 * mb[0]) + (ma1 * mb[3]) + (ma2 * mb[6]);
        ma[1] = (ma0 * mb[1]) + (ma1 * mb[4]) + (ma2 * mb[7]);
        ma[2] = (ma0 * mb[2]) + (ma1 * mb[5]) + (ma2 * mb[8]);

        ma0 = ma[3];
        ma1 = ma[4];
        ma2 = ma[5];
        ma[3] = (ma0 * mb[0]) + (ma1 * mb[3]) + (ma2 * mb[6]);
        ma[4] = (ma0 * mb[1]) + (ma1 * mb[4]) + (ma2 * mb[7]);
        ma[5] = (ma0 * mb[2]) + (ma1 * mb[5]) + (ma2 * mb[8]);

        ma0 = ma[6];
        ma1 = ma[7];
        ma2 = ma[8];
        ma[6] = (ma0 * mb[0]) + (ma1 * mb[3]) + (ma2 * mb[6]);
        ma[7] = (ma0 * mb[1]) + (ma1 * mb[4]) + (ma2 * mb[7]);
        ma[8] = (ma0 * mb[2]) + (ma1 * mb[5]) + (ma2 * mb[8]);

        return this;
    }

    /**
     * Multiplies this matrix by a matrix specified by individual components.
     *
     * @param m11 matrix element at row 1, column 1
     * @param m12 matrix element at row 1, column 2
     * @param m13 matrix element at row 1, column 3
     * @param m21 matrix element at row 2, column 1
     * @param m22 matrix element at row 2, column 2
     * @param m23 matrix element at row 2, column 3
     * @param m31 matrix element at row 3, column 1
     * @param m32 matrix element at row 3, column 2
     * @param m33 matrix element at row 3, column 3
     *
     * @return this matrix with its components multiplied by the specified values
     */
    public Matrix3 multiplyByMatrix(double m11, double m12, double m13,
                                    double m21, double m22, double m23,
                                    double m31, double m32, double m33) {

        double[] m = this.m;
        double mr1, mr2, mr3;

        mr1 = m[0];
        mr2 = m[1];
        mr3 = m[2];
        m[0] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31);
        m[1] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32);
        m[2] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33);

        mr1 = m[3];
        mr2 = m[4];
        mr3 = m[5];
        m[3] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31);
        m[4] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32);
        m[5] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33);

        mr1 = m[6];
        mr2 = m[7];
        mr3 = m[8];
        m[6] = (mr1 * m11) + (mr2 * m21) + (mr3 * m31);
        m[7] = (mr1 * m12) + (mr2 * m22) + (mr3 * m32);
        m[8] = (mr1 * m13) + (mr2 * m23) + (mr3 * m33);

        return this;
    }

    /**
     * Transposes this matrix in place.
     *
     * @return this matrix, transposed.
     */
    public Matrix3 transpose() {

        double[] m = this.m;
        double tmp = m[1];
        m[1] = m[3];
        m[3] = tmp;

        tmp = m[2];
        m[2] = m[6];
        m[6] = tmp;

        tmp = m[5];
        m[5] = m[7];
        m[7] = tmp;

        return this;
    }

    /**
     * Transposes the specified matrix and stores the result in this matrix.
     *
     * @param matrix the matrix whose transpose is computed
     *
     * @return this matrix set to the transpose of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix in null
     */
    public Matrix3 transposeMatrix(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix3", "transposeMatrix", "missingMatrix"));
        }

        this.m[0] = matrix.m[0];
        this.m[1] = matrix.m[3];
        this.m[2] = matrix.m[6];

        this.m[3] = matrix.m[1];
        this.m[4] = matrix.m[4];
        this.m[5] = matrix.m[7];

        this.m[6] = matrix.m[2];
        this.m[7] = matrix.m[5];
        this.m[8] = matrix.m[8];

        return this;
    }

    /**
     * Transposes this matrix, storing the result in the specified single precision array. The result is compatible with
     * GLSL uniform matrices, and can be passed to the function glUniformMatrix3fv.
     *
     * @param result a pre-allocated array of length 9 in which to return the transposed components
     *
     * @return the result argument set to the transponsed components
     */
    public float[] transposeToArray(float[] result, int offset) {
        if (result == null || result.length - offset < 9) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix4", "transposeToArray", "missingResult"));
        }

        result[offset++] = (float) this.m[0];
        result[offset++] = (float) this.m[3];
        result[offset++] = (float) this.m[6];

        result[offset++] = (float) this.m[1];
        result[offset++] = (float) this.m[4];
        result[offset++] = (float) this.m[7];

        result[offset++] = (float) this.m[2];
        result[offset++] = (float) this.m[5];
        result[offset] = (float) this.m[8];

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
    public Matrix3 invert() {

        throw new UnsupportedOperationException("Matrix3.invert is not implemented"); // TODO
    }

    /**
     * Inverts the specified matrix and stores the result in this matrix.
     * <p/>
     * This throws an exception if the matrix is singular.
     * <p/>
     * The result of this method is undefined if this matrix is passed in as the matrix to invert.
     *
     * @param matrix the matrix whose inverse is computed
     *
     * @return this matrix set to the inverse of the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null or cannot be inverted
     */
    public Matrix3 invertMatrix(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Matrix3", "invertMatrix", "missingMatrix"));
        }

        throw new UnsupportedOperationException("Matrix3.invertMatrix is not implemented"); // TODO
    }
}
