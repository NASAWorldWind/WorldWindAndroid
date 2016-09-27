/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

/**
 * Specifies an offset relative to a rectangle. Used by renderable shapes.
 */
public class Offset {

    /**
     * The offset in the X dimension, interpreted according to this instance's xUnits argument.
     */
    public double x;

    /**
     * The offset in the Y dimension, interpreted according to this instance's yUnits argument.
     */
    public double y;

    /**
     * The units of this instance's X offset. See this class' constructor description for a list of the possible
     * values.
     */
    @WorldWind.OffsetMode
    public int xUnits;

    /**
     * The units of this instance's Y offset. See this class' constructor description for a list of the possible
     * values.
     */
    @WorldWind.OffsetMode
    public int yUnits;

    /**
     * Constructs an offset instance given specified units and offsets.
     *
     * @param xUnits The type of units specified for the X dimension. May be one of the following: <ul>
     *               <li>[WorldWind.OFFSET_FRACTION]{@link WorldWind#OFFSET_FRACTION}</li>
     *               <li>[WorldWind.OFFSET_INSET_PIXELS]{@link WorldWind#OFFSET_INSET_PIXELS}</li>
     *               <li>[WorldWind.OFFSET_PIXELS]{@link WorldWind#OFFSET_PIXELS}</li> </ul>
     * @param x      The offset in the X dimension.
     * @param yUnits The type of units specified for the Y dimension, assuming a lower-left Y origin. May be one of the
     *               following: <ul> <li>[WorldWind.OFFSET_FRACTION]{@link WorldWind#OFFSET_FRACTION}</li>
     *               <li>[WorldWind.OFFSET_INSET_PIXELS]{@link WorldWind#OFFSET_INSET_PIXELS}</li>
     *               <li>[WorldWind.OFFSET_PIXELS]{@link WorldWind#OFFSET_PIXELS}</li> </ul>
     * @param y      The offset in the Y dimension.
     */
    public Offset(@WorldWind.OffsetMode int xUnits, double x, @WorldWind.OffsetMode int yUnits, double y) {
        this.x = x;
        this.y = y;
        this.xUnits = xUnits;
        this.yUnits = yUnits;
    }

    /**
     * Creates a new offset of this offset with identical property values.
     */
    public Offset(Offset offset) {
        if (offset == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Offset", "constructor", "missingOffset"));
        }

        this.x = offset.x;
        this.y = offset.y;
        this.xUnits = offset.xUnits;
        this.yUnits = offset.yUnits;
    }

    /**
     * This factory method returns a new offset used for anchoring a rectangle to its center.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5)
     */
    public static Offset center() {
        return new Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5);
    }

    /**
     * This factory method returns a new offset used for anchoring a rectangle to its bottom-left corner.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 0.0)
     */
    public static Offset bottomLeft() {
        return new Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 0.0);
    }

    /**
     * This factory method returns a new offset for anchoring a rectangle to its center of its bottom edge.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.0)
     */
    public static Offset bottomCenter() {
        return new Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.0);
    }

    /**
     * This factory method returns a new offset for anchoring a rectangle to its bottom-right corner.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 0.0)
     */
    public static Offset bottomRight() {
        return new Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 0.0);
    }

    /**
     * This factory method returns a new offset for anchoring a rectangle its top-left corner.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 1.0)
     */
    public static Offset topLeft() {
        return new Offset(WorldWind.OFFSET_FRACTION, 0.0, WorldWind.OFFSET_FRACTION, 1.0);
    }

    /**
     * This factory method returns a new offset for anchoring a rectangle to the center of its top edge.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 1.0)
     */
    public static Offset topCenter() {
        return new Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 1.0);
    }

    /**
     * This factory method returns a new offset for anchoring a rectangle to its top-right corner.
     *
     * @return Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 1.0)
     */
    public static Offset topRight() {
        return new Offset(WorldWind.OFFSET_FRACTION, 1.0, WorldWind.OFFSET_FRACTION, 1.0);
    }

    /**
     * Sets this offset to identical property values of the specified offset.
     */
    public Offset set(Offset offset) {
        if (offset == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Offset", "set", "missingOffset"));
        }

        this.x = offset.x;
        this.y = offset.y;
        this.xUnits = offset.xUnits;
        this.yUnits = offset.yUnits;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Offset that = (Offset) o;
        return this.x == that.x
            && this.y == that.y
            && this.xUnits == that.xUnits
            && this.yUnits == that.yUnits;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + this.xUnits;
        result = 31 * result + this.yUnits;
        return result;
    }

    @Override
    public String toString() {
        return "{" +
            "x=" + this.x +
            ", y=" + this.y +
            ", xUnits=" + this.xUnits +
            ", yUnits=" + this.yUnits +
            '}';
    }

    /**
     * Returns this offset's absolute X and Y coordinates in pixels for a rectangle of a specified size in pixels. The
     * returned offset is in pixels relative to the rectangle's origin, and is defined in the coordinate system used by
     * the caller.
     *
     * @param width  the rectangle's width in pixels
     * @param height the rectangles height in pixels
     * @param result a pre-allocated Vec2 in which to return the computed offset relative to the rectangle's origin
     *
     * @return the result argument set to the computed offset
     */
    public Vec2 offsetForSize(double width, double height, Vec2 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Offset", "offsetForSize", "missingResult"));
        }

        double x, y;

        if (this.xUnits == WorldWind.OFFSET_FRACTION) {
            x = width * this.x;
        } else if (this.xUnits == WorldWind.OFFSET_INSET_PIXELS) {
            x = width - this.x;
        } else { // default to OFFSET_PIXELS
            x = this.x;
        }

        if (this.yUnits == WorldWind.OFFSET_FRACTION) {
            y = height * this.y;
        } else if (this.yUnits == WorldWind.OFFSET_INSET_PIXELS) {
            y = height - this.y;
        } else { // default to OFFSET_PIXELS
            y = this.y;
        }

        return result.set(x, y);
    }
}
