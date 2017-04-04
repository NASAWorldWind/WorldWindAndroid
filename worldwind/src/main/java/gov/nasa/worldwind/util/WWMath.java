/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Viewport;

/**
 * Collection of static methods for performing common World Wind computations.
 */
public class WWMath {

    /**
     * Restricts a value to the range [min, max] degrees, clamping values outside the range. Values less than min are
     * returned as min, and values greater than max are returned as max. Values within the range are returned
     * unmodified.
     * <p/>
     * The result of this method is undefined if min is greater than max.
     *
     * @param value the values to clamp
     * @param min   the minimum value
     * @param max   the maximum value
     *
     * @return the specified values clamped to the range [min, max] degrees
     */
    public static double clamp(double value, double min, double max) {
        return value > max ? max : (value < min ? min : value);
    }

    /**
     * Restricts an angle to the range [-180, +180] degrees, clamping angles outside the range. Angles less than -180
     * are returned as -180, and angles greater than +180 are returned as +180. Angles within the range are returned
     * unmodified.
     *
     * @param degrees the angle to clamp in degrees
     *
     * @return the specified angle clamped to the range [-180, +180] degrees
     */
    public static double clampAngle180(double degrees) {
        return degrees > 180 ? 180 : (degrees < -180 ? -180 : degrees);
    }

    /**
     * Restricts an angle to the range [0, 360] degrees, clamping angles outside the range. Angles less than 0 are
     * returned as 0, and angles greater than 360 are returned as 360. Angles within the range are returned unmodified.
     *
     * @param degrees the angle to clamp in degrees
     *
     * @return the specified angle clamped to the range [0, 360] degrees
     */
    public static double clampAngle360(double degrees) {
        return degrees > 360 ? 360 : (degrees < 0 ? 0 : degrees);
    }

    /**
     * Returns the fractional part of a specified number
     *
     * @param value the number whose fractional part to compute
     *
     * @return The fractional part of the specified number: value - floor(value)
     */
    public static double fract(double value) {
        return value - Math.floor(value);
    }

    /**
     * Computes the linear interpolation of two values according to a specified fractional amount. The fractional amount
     * is interpreted as a relative proportion of the two values, where 0.0 indicates the first value, 0.5 indicates a
     * 50/50 mix of the two values, and 1.0 indicates the second value.
     * <p/>
     * The result of this method is undefined if the amount is outside the range [0, 1].
     *
     * @param amount the fractional proportion of the two values in the range [0, 1]
     * @param value1 the first value
     * @param value2 the second value
     *
     * @return the interpolated value
     */
    public static double interpolate(double amount, double value1, double value2) {
        return (1 - amount) * value1 + amount * value2;
    }

    /**
     * Computes the linear interpolation of two angles in the range [-180, +180] degrees according to a specified
     * fractional amount. The fractional amount is interpreted as a relative proportion of the two angles, where 0.0
     * indicates the first angle, 0.5 indicates an angle half way between the two angles, and 1.0 indicates the second
     * angle.
     * <p/>
     * The result of this method is undefined if the amount is outside the range [0, 1].
     *
     * @param amount   the fractional proportion of the two angles in the range [0, 1]
     * @param degrees1 the first angle in degrees
     * @param degrees2 the second angle in degrees
     *
     * @return the interpolated angle in the range [-180, +180] degrees
     */
    public static double interpolateAngle180(double amount, double degrees1, double degrees2) {
        // Normalize the two angles to the range [-180, +180].
        double angle1 = normalizeAngle180(degrees1);
        double angle2 = normalizeAngle180(degrees2);

        // If the shortest arc between the two angles crosses the -180/+180 degree boundary, add 360 degrees to the
        // smaller of the two angles then interpolate.
        if (angle1 - angle2 > 180) {
            angle2 += 360;
        } else if (angle1 - angle2 < -180) {
            angle1 += 360;
        }

        // Linearly interpolate between the two angles then normalize the interpolated result. Normalizing the result is
        // necessary when we have added 360 degrees to either angle in order to interpolate along the shortest arc.
        double angle = (1 - amount) * angle1 + amount * angle2;
        return normalizeAngle180(angle);
    }

    /**
     * Computes the linear interpolation of two angles in the range [0, 360] degrees according to a specified fractional
     * amount. The fractional amount is interpreted as a relative proportion of the two angles, where 0.0 indicates the
     * first angle, 0.5 indicates an angle half way between the two angles, and 1.0 indicates the second angle.
     * <p/>
     * The result of this method is undefined if the amount is outside the range [0, 1].
     *
     * @param amount   the fractional proportion of the two angles in the range [0, 1]
     * @param degrees1 the first angle in degrees
     * @param degrees2 the second angle in degrees
     *
     * @return the interpolated angle in the range [0, 360] degrees
     */
    public static double interpolateAngle360(double amount, double degrees1, double degrees2) {
        // Normalize the two angles to the range [-180, +180].
        double angle1 = normalizeAngle180(degrees1);
        double angle2 = normalizeAngle180(degrees2);

        // If the shortest arc between the two angles crosses the -180/+180 degree boundary, add 360 degrees to the
        // smaller of the two angles then interpolate.
        if (angle1 - angle2 > 180) {
            angle2 += 360;
        } else if (angle1 - angle2 < -180) {
            angle1 += 360;
        }

        // Linearly interpolate between the two angles then normalize the interpolated result. Normalizing the result is
        // necessary when we have added 360 degrees to either angle in order to interpolate along the shortest arc.
        double angle = (1 - amount) * angle1 + amount * angle2;
        return normalizeAngle360(angle);
    }

    /**
     * Returns the integer modulus of a specified number. This differs from the % operator in that the result is
     * always positive when the modulus is positive. For example -1 % 10 = -1, whereas mod(-1, 10) = 1.
     *
     * @param value   the integer number whose modulus to compute
     * @param modulus the modulus
     *
     * @return the remainder after dividing the number by the modulus
     */
    public static int mod(int value, int modulus) {
        return ((value % modulus) + modulus) % modulus;
    }

    /**
     * Restricts an angle to the range [-180, +180] degrees, wrapping angles outside the range. Wrapping takes place as
     * though traversing the edge of a unit circle; angles less than -180 wrap back to +180, while angles greater than
     * +180 wrap back to -180.
     *
     * @param degrees the angle to wrap in degrees
     *
     * @return the specified angle wrapped to [-180, +180] degrees
     */
    public static double normalizeAngle180(double degrees) {
        double angle = degrees % 360;
        return angle > 180 ? angle - 360 : (angle < -180 ? 360 + angle : angle);
    }

    /**
     * Restricts an angle to the range [0, 360] degrees, wrapping angles outside the range. Wrapping takes place as
     * though traversing the edge of a unit circle; angles less than 0 wrap back to 360, while angles greater than 360
     * wrap back to 0.
     *
     * @param degrees the angle to wrap in degrees
     *
     * @return the specified angle wrapped to [0, 360] degrees
     */
    public static double normalizeAngle360(double degrees) {
        double angle = degrees % 360;
        return angle >= 0 ? angle : (angle < 0 ? 360 + angle : 360 - angle);
    }

    /**
     * Computes the bounding rectangle for a unit square after applying a transformation matrix to the square's four
     * corners.
     *
     * @param unitSquareTransform the matrix to apply to the unit square
     * @param result              a pre-allocated Viewport in which to return the computed bounding rectangle
     *
     * @return the result argument set to the computed bounding rectangle
     */
    public static Viewport boundingRectForUnitSquare(Matrix4 unitSquareTransform, Viewport result) {
        if (unitSquareTransform == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WWMath", "boundingRectForUnitSquare", "missingMatrix"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WWMath", "boundingRectForUnitSquare", "missingResult"));
        }

        double[] m = unitSquareTransform.m;

        // transform of (0, 0)
        double x1 = m[3];
        double y1 = m[7];

        // transform of (1, 0)
        double x2 = m[0] + m[3];
        double y2 = m[4] + m[7];

        // transform of (0, 1)
        double x3 = m[1] + m[3];
        double y3 = m[5] + m[7];

        // transform of (1, 1)
        double x4 = m[0] + m[1] + m[3];
        double y4 = m[4] + m[5] + m[7];

        int minX = (int) Math.min(Math.min(x1, x2), Math.min(x3, x4));
        int maxX = (int) Math.max(Math.max(x1, x2), Math.max(x3, x4));
        int minY = (int) Math.min(Math.min(y1, y2), Math.min(y3, y4));
        int maxY = (int) Math.max(Math.max(y1, y2), Math.max(y3, y4));

        return result.set(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Indicates whether a specified value is a power of two.
     *
     * @param value the value to test
     *
     * @return true if the specified value is a power of two, false othwerwise
     */
    public static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & (value - 1)) == 0;
    }

    /**
     * Returns the value that is the nearest power of 2 greater than or equal to the given value.
     *
     * @param value the reference value. The power of 2 returned is greater than or equal to this value.
     *
     * @return the value that is the nearest power of 2 greater than or equal to the reference value
     */
    public static int powerOfTwoCeiling(int value) {
        int pow = (int) Math.floor(Math.log(value) / Math.log(2));
        return 1 << pow;
    }
}
