/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Continuous interval in a one-dimensional coordinate system expressed as a lower bound an an upper bound, inclusive.
 */
public class Range {

    /**
     * The range's lower bound, inclusive.
     */
    public int lower;

    /**
     * The range's upper bound, inclusive.
     */
    public int upper;

    /**
     * Constructs an empty range with lower and upper both zero.
     */
    public Range() {
    }

    /**
     * Constructs a range with a specified lower bound and upper bound.
     *
     * @param lower the lower bound, inclusive
     * @param upper the upper bound, inclusive
     */
    public Range(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Constructs a range with the lower bound and upper bound of a specified range.
     *
     * @param range the range specifying the values
     *
     * @throws IllegalArgumentException If the range is null
     */
    public Range(Range range) {
        if (range == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Range", "constructor", "missingRange"));
        }

        this.lower = range.lower;
        this.upper = range.upper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Range that = (Range) o;
        return this.lower == that.lower && this.upper == that.upper;
    }

    @Override
    public int hashCode() {
        return 31 * this.lower + this.upper;
    }

    /**
     * Returns the length of the interval between this range's lower bound and upper bound, or 0 if this range is empty.
     * @return the interval length associated with this range
     */
    public int length() {
        return (this.upper > this.lower) ? (this.upper - this.lower) : 0;
    }

    /**
     * Sets this range to the specified lower bound and upper bound.
     *
     * @param lower the new lower bound, inclusive
     * @param upper the new upper bound, inclusive
     *
     * @return this range set to the specified values
     */
    public Range set(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
        return this;
    }

    /**
     * Sets this range to the lower bound and upper bound of a specified range.
     *
     * @param range the range specifying the new values
     *
     * @return this range with its lower bound and upper bound set to that of the specified range
     *
     * @throws IllegalArgumentException If the range is null
     */
    public Range set(Range range) {
        if (range == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Range", "set", "missingRange"));
        }

        this.lower = range.lower;
        this.upper = range.upper;
        return this;
    }

    /**
     * Sets this range to an empty range.
     *
     * @return this range with its lower bound and upper bound both set to zero
     */
    public Range setEmpty() {
        this.lower = 0;
        this.upper = 0;
        return this;
    }

    /**
     * Indicates whether or not this range is empty. An range is empty when its lower bound is greater than or equal to
     * its upper bound.
     *
     * @return true if this range is empty, false otherwise
     */
    public boolean isEmpty() {
        return this.lower >= this.upper;
    }
}
