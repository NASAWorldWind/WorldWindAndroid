/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.WorldWind;

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
     * Creates a new copy of this offset with identical property values.
     */
    public Offset(Offset copy) {
        this.x = copy.x;
        this.y = copy.y;
        this.xUnits = copy.xUnits;
        this.yUnits = copy.yUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Offset offset = (Offset) o;

        if (Double.compare(offset.x, x) != 0) return false;
        if (Double.compare(offset.y, y) != 0) return false;
        if (xUnits != offset.xUnits) return false;
        return yUnits == offset.yUnits;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + xUnits;
        result = 31 * result + yUnits;
        return result;
    }

    @Override
    public String toString() {
        return "{" +
            "x=" + x +
            ", y=" + y +
            ", xUnits=" + xUnits +
            ", yUnits=" + yUnits +
            '}';
    }

    /**
     * Returns this offset's absolute X and Y coordinates in pixels for a rectangle of a specified size in pixels. The
     * returned offset is in pixels relative to the rectangle's origin, and is defined in the coordinate system used by
     * the caller.
     *
     * @param width  The rectangle's width in pixels.
     * @param height The rectangles height in pixels.
     *
     * @return The computed offset relative to the rectangle's origin.
     */
    public Vec2 offsetForSize(double width, double height) {
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

        return new Vec2(x, y);
    }

}
