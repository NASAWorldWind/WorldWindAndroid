/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * Collection of static methods for performing common World Wind computations.
 */
public class WWMath {

    /**
     * Normalizes a specified value to be within the range of [0, 360] degrees.
     *
     * @param degrees the value to normalize, in degrees.
     *
     * @return the specified value normalized to [0, 360] degrees.
     */
    public static double normalizeDegrees(double degrees) {
        double angle = degrees % 360;
        return angle >= 0 ? angle : angle < 0 ? 360 + angle : 360 - angle;
    }
}
