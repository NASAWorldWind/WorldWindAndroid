/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Three-component vector with X, Y and Z coordinates.
 */
public class Vec3 {

    /**
     * The vector's X component.
     */
    public double x;

    /**
     * The vector's Y component.
     */
    public double y;

    /**
     * The vector's Z component.
     */
    public double z;

    /**
     * Constructs a three-component vector with X, Y and Z all 0.
     */
    public Vec3() {
    }

    /**
     * Constructs a three-component vector with a specified X, Y and Z.
     *
     * @param x the vector's X component
     * @param y the vector's Y component
     * @param z the vector's Z component
     */
    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructs a three-component vector with the X, Y and Z of a specified vector.
     *
     * @param vector the vector specifying the components
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "constructor", "missingVector"));
        }

        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Vec3 that = (Vec3) o;
        return this.x == that.x
            && this.y == that.y
            && this.z == that.z;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return this.x + ", " + this.y + ", " + this.z;
    }

    /**
     * Copies this vector's components to the specified single precision array. The result is compatible with GLSL
     * uniform vectors, and can be passed to the function glUniform3fv.
     *
     * @param result a pre-allocated array of length 3 in which to return the components
     *
     * @return the result argument set to this vector's components
     */
    public float[] toArray(float[] result, int offset) {
        if (result == null || result.length - offset < 3) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "toArray", "missingResult"));
        }

        result[offset++] = (float) this.x;
        result[offset++] = (float) this.y;
        result[offset] = (float) this.z;

        return result;
    }

    /**
     * Computes the magnitude of this vector.
     *
     * @return the magnitude of this vector
     */
    public double magnitude() {
        return Math.sqrt(this.magnitudeSquared());
    }

    /**
     * Computes the squared magnitude of this vector. This is equivalent to squaring the result of
     * <code>magnitude</code> but is potentially much more efficient.
     *
     * @return the squared magnitude of this vector
     */
    public double magnitudeSquared() {
        return this.x * this.x +
            this.y * this.y +
            this.z * this.z;
    }

    /**
     * Computes the distance from this vector to another vector.
     *
     * @param vector The vector to compute the distance to
     *
     * @return the distance between the vectors
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public double distanceTo(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "distanceTo", "missingVector"));
        }

        return Math.sqrt(this.distanceToSquared(vector));
    }

    /**
     * Computes the squared distance from this vector to a specified vector. This is equivalent to squaring the result
     * of <code>distanceTo</code> but is potentially much more efficient.
     *
     * @param vector the vector to compute the distance to
     *
     * @return the squared distance between the vectors
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public double distanceToSquared(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "distanceToSquared", "missingVector"));
        }

        double dx = this.x - vector.x;
        double dy = this.y - vector.y;
        double dz = this.z - vector.z;

        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Sets this vector to the specified X, Y and Z.
     *
     * @param x the new X component
     * @param y the new Y component
     * @param z the new Z component
     *
     * @return this vector set to the specified values
     */
    public Vec3 set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    /**
     * Sets this vector to the X, Y and Z of a specified vector.
     *
     * @param vector the vector specifying the new components
     *
     * @return this vector with its X, Y and Z set to that of the specified vector
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3 set(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "set", "missingVector"));
        }

        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;

        return this;
    }

    /**
     * Swaps this vector with the specified vector. This vector's components are set to the values of the specified
     * vector's components, and the specified vector's components are set to the values of this vector's components.
     *
     * @param vector the vector to swap with this vector
     *
     * @return this vector set to the values of the specified vector
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3 swap(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "swap", "missingVector"));
        }

        double tmp = this.x;
        this.x = vector.x;
        vector.x = tmp;

        tmp = this.y;
        this.y = vector.y;
        vector.y = tmp;

        tmp = this.z;
        this.z = vector.z;
        vector.z = tmp;

        return this;
    }

    /**
     * Adds a specified vector to this vector.
     *
     * @param vector the vector to add
     *
     * @return this vector after adding the specified vector to it
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3 add(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "add", "missingVector"));
        }

        this.x += vector.x;
        this.y += vector.y;
        this.z += vector.z;

        return this;
    }

    /**
     * Subtracts a specified vector from this vector.
     *
     * @param vector the vector to subtract
     *
     * @return this vector after subtracting the specified vector from it
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3 subtract(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "subtract", "missingVector"));
        }

        this.x -= vector.x;
        this.y -= vector.y;
        this.z -= vector.z;

        return this;
    }

    /**
     * Multiplies this vector by a scalar.
     *
     * @param scalar the scalar to multiply this vector by
     *
     * @return this vector multiplied by the specified scalar
     */
    public Vec3 multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;

        return this;
    }

    /**
     * Multiplies this vector by a 4x4 matrix. The multiplication is performed with an implicit W component of 1. The
     * resultant W component of the product is then divided through the X, Y, and Z components.
     *
     * @param matrix the matrix to multiply this vector by
     *
     * @return this vector multiplied by the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Vec3 multiplyByMatrix(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "multiplyByMatrix", "missingMatrix"));
        }

        double[] m = matrix.m;
        double x = (m[0] * this.x) + (m[1] * this.y) + (m[2] * this.z) + m[3];
        double y = (m[4] * this.x) + (m[5] * this.y) + (m[6] * this.z) + m[7];
        double z = (m[8] * this.x) + (m[9] * this.y) + (m[10] * this.z) + m[11];
        double w = (m[12] * this.x) + (m[13] * this.y) + (m[14] * this.z) + m[15];

        this.x = x / w;
        this.y = y / w;
        this.z = z / w;

        return this;
    }

    /**
     * Divides this vector by a scalar.
     *
     * @param divisor the scalar to divide this vector by
     *
     * @return this vector divided by the specified scalar
     */
    public Vec3 divide(double divisor) {
        this.x /= divisor;
        this.y /= divisor;
        this.z /= divisor;

        return this;
    }

    /**
     * Negates the components of this vector.
     *
     * @return this vector, negated
     */
    public Vec3 negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;

        return this;
    }

    /**
     * Normalizes this vector to a unit vector.
     *
     * @return this vector, normalized
     */
    public Vec3 normalize() {
        double magnitude = this.magnitude();
        if (magnitude != 0) {
            this.x /= magnitude;
            this.y /= magnitude;
            this.z /= magnitude;
        }

        return this;
    }

    /**
     * Computes the scalar dot product of this vector and a specified vector.
     *
     * @param vector the vector to multiply
     *
     * @return the dot product of the two vectors
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public double dot(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "dot", "missingVector"));
        }

        return this.x * vector.x +
            this.y * vector.y +
            this.z * vector.z;
    }

    /**
     * Computes the cross product of this vector and a specified vector, modifying this vector.
     *
     * @param vector the vector to cross with this vector
     *
     * @return this vector set to the cross product of itself and the specified vector
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3 cross(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "cross", "missingVector"));
        }

        double x = this.y * vector.z - this.z * vector.y;
        double y = this.z * vector.x - this.x * vector.z;
        double z = this.x * vector.y - this.y * vector.x;

        this.x = x;
        this.y = y;
        this.z = z;

        return this;
    }

    /**
     * Computes the cross product of two vectors, setting this vector to the result.
     *
     * @param a the first vector
     * @param b the second vector
     *
     * @return this vector set to the cross product of the two specified vectors
     *
     * @throws IllegalArgumentException If either vector is null
     */
    public Vec3 cross(Vec3 a, Vec3 b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "cross", "missingVector"));
        }

        this.x = a.y * b.z - a.z * b.y;
        this.y = a.z * b.x - a.x * b.z;
        this.z = a.x * b.y - a.y * b.x;

        return this;
    }

    /**
     * Mixes (interpolates) a specified vector with this vector, modifying this vector.
     *
     * @param vector The vector to mix with this one
     * @param weight The relative weight of this vector, typically in the range [0,1]
     *
     * @return this vector modified to the mix of itself and the specified vector
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec3 mix(Vec3 vector, double weight) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec3", "mix", "missingVector"));
        }

        double w0 = 1 - weight;
        double w1 = weight;

        this.x = this.x * w0 + vector.x * w1;
        this.y = this.y * w0 + vector.y * w1;
        this.z = this.z * w0 + vector.z * w1;

        return this;
    }
}
