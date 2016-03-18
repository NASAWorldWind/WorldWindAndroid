/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Two-component vector with X and Y coordinates.
 */
public class Vec2 {

    /**
     * The vector's X component.
     */
    public double x;

    /**
     * The vector's Y component.
     */
    public double y;

    /**
     * Constructs a two-component vector with X and Y both 0.
     */
    public Vec2() {
    }

    /**
     * Constructs a two-component vector with a specified X and Y.
     *
     * @param x the vector's X component
     * @param y the vector's Y component
     */
    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a two-component vector with the X and Y of a specified vector.
     *
     * @param vector the vector specifying the components
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec2(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "constructor", "missingVector"));
        }

        this.x = vector.x;
        this.y = vector.y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Vec2 that = (Vec2) o;
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return this.x + ", " + this.y;
    }

    /**
     * Copies this vector's components to the specified single precision array. The result is compatible with GLSL
     * uniform vectors, and can be passed to the function glUniform2fv.
     *
     * @param result a pre-allocated array of length 2 in which to return the components
     *
     * @return the result argument set to this vector's components
     */
    public float[] toArray(float[] result, int offset) {
        if (result == null || result.length - offset < 2) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "toArray", "missingResult"));
        }

        result[offset++] = (float) this.x;
        result[offset] = (float) this.y;

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
        return this.x * this.x + this.y * this.y;
    }

    /**
     * Computes the distance from this vector to another vector.
     *
     * @param vector the vector to compute the distance to
     *
     * @return the distance between the vectors
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public double distanceTo(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "distanceTo", "missingVector"));
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
    public double distanceToSquared(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "distanceToSquared", "missingVector"));
        }

        double dx = this.x - vector.x;
        double dy = this.y - vector.y;

        return dx * dx + dy * dy;
    }

    /**
     * Sets this vector to the specified X and Y.
     *
     * @param x the new X component
     * @param y the new Y component
     *
     * @return this vector set to the specified values
     */
    public Vec2 set(double x, double y) {
        this.x = x;
        this.y = y;

        return this;
    }

    /**
     * Sets this vector to the X and Y of a specified vector.
     *
     * @param vector the vector specifying the new components
     *
     * @return this vector with its X and Y set to that of the specified vector
     *
     * @throws IllegalArgumentException If the vector is null
     */
    public Vec2 set(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "set", "missingVector"));
        }

        this.x = vector.x;
        this.y = vector.y;

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
    public Vec2 swap(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "swap", "missingVector"));
        }

        double tmp = this.x;
        this.x = vector.x;
        vector.x = tmp;

        tmp = this.y;
        this.y = vector.y;
        vector.y = tmp;

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
    public Vec2 add(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "add", "missingVector"));
        }

        this.x += vector.x;
        this.y += vector.y;

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
    public Vec2 subtract(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "subtract", "missingVector"));
        }

        this.x -= vector.x;
        this.y -= vector.y;

        return this;
    }

    /**
     * Multiplies this vector by a scalar.
     *
     * @param scalar the scalar to multiply this vector by
     *
     * @return this vector multiplied by the specified scalar
     */
    public Vec2 multiply(double scalar) {
        this.x *= scalar;
        this.y *= scalar;

        return this;
    }

    /**
     * Multiplies this vector by a 3x3 matrix. The multiplication is performed with an implicit Z component of 1. The
     * resultant Z component of the product is then divided through the X and Y components.
     *
     * @param matrix the matrix to multiply this vector by
     *
     * @return this vector multiplied by the specified matrix
     *
     * @throws IllegalArgumentException If the matrix is null
     */
    public Vec2 multiplyByMatrix(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "multiplyByMatrix", "missingMatrix"));
        }

        double[] m = matrix.m;
        double x = (m[0] * this.x) + (m[1] * this.y) + m[2];
        double y = (m[3] * this.x) + (m[4] * this.y) + m[5];
        double z = (m[6] * this.x) + (m[7] * this.y) + m[8];

        this.x = x / z;
        this.y = y / z;

        return this;
    }

    /**
     * Divides this vector by a scalar.
     *
     * @param divisor the scalar to divide this vector by
     *
     * @return this vector divided by the specified scalar
     */
    public Vec2 divide(double divisor) {
        this.x /= divisor;
        this.y /= divisor;

        return this;
    }

    /**
     * Negates the components of this vector.
     *
     * @return this vector, negated
     */
    public Vec2 negate() {
        this.x = -this.x;
        this.y = -this.y;

        return this;
    }

    /**
     * Normalizes this vector to a unit vector.
     *
     * @return this vector, normalized
     */
    public Vec2 normalize() {
        double magnitude = this.magnitude();
        if (magnitude != 0) {
            this.x /= magnitude;
            this.y /= magnitude;
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
    public double dot(Vec2 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "dot", "missingVector"));
        }

        return this.x * vector.x + this.y * vector.y;
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
    public Vec2 mix(Vec2 vector, double weight) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Vec2", "mix", "missingVector"));
        }

        double w0 = 1 - weight;
        double w1 = weight;

        this.x = this.x * w0 + vector.x * w1;
        this.y = this.y * w0 + vector.y * w1;

        return this;
    }
}
