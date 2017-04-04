/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Oblate ellipsoid with semi-major axis and inverse flattening.
 */
public class Ellipsoid {

    /**
     * One half of the ellipsoid's major axis length in meters, which runs through the center to opposite points on the
     * equator.
     */
    protected double semiMajorAxis = 1;

    /**
     * Measure of the ellipsoid's compression. Indicates how much the ellipsoid's semi-minor axis is compressed relative
     * to the semi-major axis. Expressed as {@code 1/f}, where {@code f = (a - b) / a}, given the semi-major axis {@code
     * a} and the semi-minor axis {@code b}.
     */
    protected double inverseFlattening = 1;

    /**
     * Constructs an ellipsoid with semi-major axis and inverse flattening both 1.0.
     */
    public Ellipsoid() {
    }

    /**
     * Constructs an ellipsoid with a specified semi-major axis and inverse flattening.
     *
     * @param semiMajorAxis     one half of the ellipsoid's major axis length in meters, which runs through the center
     *                          to opposite points on the equator
     * @param inverseFlattening measure of the ellipsoid's compression, indicating how much the semi-minor axis is
     *                          compressed relative to the semi-major axis
     */
    public Ellipsoid(double semiMajorAxis, double inverseFlattening) {
        this.semiMajorAxis = semiMajorAxis;
        this.inverseFlattening = inverseFlattening;
    }

    /**
     * Constructs an ellipsoid with the semi-major axis and inverse flattening of a specified ellipsoid.
     *
     * @param ellipsoid the ellipsoid specifying the values
     *
     * @throws IllegalArgumentException If the ellipsoid is null
     */
    public Ellipsoid(Ellipsoid ellipsoid) {
        if (ellipsoid == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipsoid", "constructor", "missingEllipsoid"));
        }

        this.semiMajorAxis = ellipsoid.semiMajorAxis;
        this.inverseFlattening = ellipsoid.inverseFlattening;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Ellipsoid that = (Ellipsoid) o;
        return this.semiMajorAxis == that.semiMajorAxis
            && this.inverseFlattening == that.inverseFlattening;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.semiMajorAxis);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.inverseFlattening);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "semiMajorAxis=" + this.semiMajorAxis + ", inverseFlattening=" + this.inverseFlattening;
    }

    /**
     * Sets this ellipsoid to a specified semi-major axis and inverse flattening.
     *
     * @param semiMajorAxis     the new semi-major axis length in meters, one half of the ellipsoid's major axis, which
     *                          runs through the center to opposite points on the equator
     * @param inverseFlattening the new inverse flattening, a measure of the ellipsoid's compression, indicating how
     *                          much the semi-minor axis is compressed relative to the semi-major axis
     *
     * @return this ellipsoid with its semi-major axis and inverse flattening set to the specified values
     */
    public Ellipsoid set(double semiMajorAxis, double inverseFlattening) {
        this.semiMajorAxis = semiMajorAxis;
        this.inverseFlattening = inverseFlattening;
        return this;
    }

    /**
     * Sets this ellipsoid to the semi-major axis and inverse flattening of a specified ellipsoid.
     *
     * @param ellipsoid the ellipsoid specifying the new values
     *
     * @return this ellipsoid with its semi-major axis and inverse flattening set to that of the specified ellipsoid
     *
     * @throws IllegalArgumentException If the ellipsoid is null
     */
    public Ellipsoid set(Ellipsoid ellipsoid) {
        if (ellipsoid == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipsoid", "set", "missingEllipsoid"));
        }

        this.semiMajorAxis = ellipsoid.semiMajorAxis;
        this.inverseFlattening = ellipsoid.inverseFlattening;
        return this;
    }

    /**
     * Computes this ellipsoid's semi-major axis length in meters. The semi-major axis is one half of the ellipsoid's
     * major axis, which runs through the center to opposite points on the equator.
     *
     * @return this ellipsoid's semi-major axis length in meters
     */
    public double semiMajorAxis() {
        return this.semiMajorAxis;
    }

    /**
     * Computes this ellipsoid's semi-minor length axis in meters. The semi-minor axis is one half of the ellipsoid's
     * minor axis, which runs through the center to opposite points on the poles.
     *
     * @return this ellipsoid's semi-minor axis length in meters
     */
    public double semiMinorAxis() {
        double f = 1 / inverseFlattening;
        return this.semiMajorAxis * (1 - f);
    }

    /**
     * Computes this ellipsoid's inverse flattening, a measure of an ellipsoid's compression. The returned value is
     * equivalent to {@code a / (a - b)}, where {@code a} and {@code b} indicate this ellipsoid's semi-major axis and
     * semi-minor axis, respectively.
     *
     * @return this ellipsoid's inverse flattening
     */
    public double inverseFlattening() {
        return this.inverseFlattening;
    }

    /**
     * Computes this ellipsoid's eccentricity squared. The returned value is equivalent to {@code 2*f - f*f},
     * where {@code f} is this ellipsoid's flattening.
     *
     * @return this ellipsoid's eccentricity squared
     */
    public double eccentricitySquared() {
        double f = 1 / this.inverseFlattening;
        return (2 * f) - (f * f);
    }
}
