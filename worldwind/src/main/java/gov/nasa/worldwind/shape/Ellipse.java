/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

/**
 * Ellipse shape defined by a geographic center position and radii for the semi-major and semi-minor axes.
 * <p>
 * <h3>Axes and Heading</h3>
 * <p>
 * Ellipse axes, by default, are oriented such that the semi-major axis points East and the semi-minor axis points
 * North. Ellipse provides an optional heading, which when set to anything other than 0.0 rotates the semi-major and
 * semi-minor axes about the center position, while retaining the axes relative relationship to one another. Heading is
 * defined in degrees clockwise from North. Configuring ellipse with a heading of 45.0 results in the semi-major axis
 * pointing Southeast and the semi-minor axis pointing Northeast.
 * <p>
 * <h3>Display Granularity</h3>
 * <p>
 * Ellipse's representation on screen is composed of discrete segments which approximate the ellipse's geometry. This
 * approximation is chosen such that the screen representation appears to be a continuous smooth ellipse. Applications
 * can control the maximum number of angular intervals used in this representation with {@link
 * #setMaximumIntervals(int)}.
 */
public class Ellipse extends AbstractShape {

    protected static final int MIN_INTERVALS = 8;

    /**
     * The ellipse's geographic center position.
     */
    protected Position center;

    /**
     * The ellipse's radius perpendicular to it's heading, in meters.
     */
    protected double majorRadius;

    /**
     * The ellipse's radius parallel to it's heading, in meters.
     */
    protected double minorRadius;

    /**
     * The ellipse's heading in degrees clockwise from North.
     */
    protected double heading;

    /**
     * The maximum number of angular intervals that may be used to assemble the ellipse's geometry for rendering.
     */
    protected int maximumIntervals = 64;

    /**
     * Constructs an ellipse with a null center position, and with major- and minor-radius both 0.0. This ellipse does
     * not display until the center position is defined and the radii are both greater than 0.0.
     */
    public Ellipse() {
    }

    /**
     * Constructs an ellipse with a null center position, and with major- and minor-radius both 0.0. This ellipse does
     * not display until the center position is defined and the radii are both greater than 0.0.
     *
     * @param attributes the shape attributes applied to the ellipse
     */
    public Ellipse(ShapeAttributes attributes) {
        super(attributes);
    }

    /**
     * Constructs an ellipse with a specified center position and radii. The ellipse displays in the default shape
     * attributes, which may be specified using {@link #setAttributes(ShapeAttributes)}. The ellipse does not display if
     * the center position is null, or both radii are 0.0.
     *
     * @param center      geographic position at the ellipse's center. May be null.
     * @param majorRadius radius of the semi-major axis, in meters.
     * @param minorRadius radius of the semi-minor axis, in meters.
     *
     * @throws IllegalArgumentException If either radius is negative
     */
    public Ellipse(Position center, double majorRadius, double minorRadius) {
        if (majorRadius < 0 || minorRadius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalidRadius"));
        }

        if (center != null) {
            this.center = new Position(center);
        }

        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Constructs an ellipse with a specified center position, radii, and shape attributes. The ellipse displays in the
     * specified shape attributes, which may be modifies using {@link #setAttributes(ShapeAttributes)}. The ellipse does
     * not display if the center position is null, or both radii are 0.0.
     *
     * @param center      geographic position at the ellipse's center; may be null
     * @param majorRadius radius of the semi-major axis, in meters.
     * @param minorRadius radius of the semi-minor axis, in meters.
     * @param attributes  the shape attributes applied to the ellipse
     *
     * @throws IllegalArgumentException If either radius is negative
     */
    public Ellipse(Position center, double majorRadius, double minorRadius, ShapeAttributes attributes) {
        super(attributes);

        if (majorRadius < 0 || minorRadius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalidRadius"));
        }

        if (center != null) {
            this.center = new Position(center);
        }

        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Indicates the geographic position of this ellipse's center. The position may be null, in which case the ellipse
     * does not display.
     *
     * @return this ellipse's geographic center; may be null
     */
    public Position getCenter() {
        return this.center;
    }

    /**
     * Sets the geographic position of this ellipse's center. The position may be null, in which case the ellipse does
     * not display.
     *
     * @param position the new center position; may be null
     *
     * @return this ellipse with its center position set to the specified position
     */
    public Ellipse setCenter(Position position) {
        if (position == null) {
            this.center = null;
        } else if (this.center == null) {
            this.center = new Position(position);
        } else {
            this.center.set(position);
        }
        this.reset();

        return this;
    }

    /**
     * Indicates the radius of this globe's semi-major axis. When the ellipse's heading is 0.0, the semi-major axis
     * points East.
     *
     * @return the radius, in meters
     */
    public double getMajorRadius() {
        return this.majorRadius;
    }

    /**
     * Sets the radius of this globe's semi-major axis. When the ellipse's heading is 0.0, the semi-major axis points
     * East.
     *
     * @param radius the new radius, in meters
     *
     * @return this ellipse with the radius of its semi-major axis set to the specified value
     *
     * @throws IllegalArgumentException If the radius is negative
     */
    public Ellipse setMajorRadius(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMajorRadius", "invalidRadius"));
        }

        this.majorRadius = radius;
        this.reset();
        return this;
    }

    /**
     * Indicates the radius of this globe's semi-minor axis. When the ellipse's heading is 0.0, the semi-minor axis
     * points North.
     *
     * @return the radius, in meters
     */
    public double getMinorRadius() {
        return this.minorRadius;
    }

    /**
     * Sets the radius of this globe's semi-minor axis. When the ellipse's heading is 0.0, the semi-minor axis points
     * North.
     *
     * @param radius the new radius, in meters
     *
     * @return this ellipse with the radius of its semi-minor axis set to the specified value
     *
     * @throws IllegalArgumentException If the radius is negative
     */
    public Ellipse setMinorRadius(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMinorRadius", "invalidRadius"));
        }

        this.minorRadius = radius;
        this.reset();
        return this;
    }

    /**
     * Indicates this ellipse's heading. When ellipse's heading is 0.0, the
     * semi-major axis points East and the semi-minor axis points North. Headings other than 0.0 rotate the axes about
     * the ellipse's center position, while retaining the axes relative relationship to one another.
     *
     * @return this ellipse's heading, in degrees clockwise from North
     */
    public double getHeading() {
        return heading;
    }

    /**
     * Sets this ellipse's heading in degrees clockwise from North. When ellipse's heading is 0.0, the
     * semi-major axis points East and the semi-minor axis points North. Headings other than 0.0 rotate the axes about
     * the ellipse's center position, while retaining the axes relative relationship to one another.
     *
     * @param degrees the new heading, in degrees clockwise from North
     *
     * @return this ellipse, with its heading set to the specified value
     */
    public Ellipse setHeading(double degrees) {
        this.heading = degrees;
        this.reset();
        return this;
    }

    /**
     * Indicates the maximum number of angular intervals that may be used to approximate this ellipse's geometry on
     * screen.
     *
     * @return the number of angular intervals
     */
    public int getMaximumIntervals() {
        return this.maximumIntervals;
    }

    /**
     * Sets the maximum number of angular intervals that may be used to approximate this ellipse's geometry on
     * screen.
     *
     * @param numIntervals the number of angular intervals; must be at least 8
     *
     * @return this ellipse with its number of angular intervals set to the specified value
     *
     * @throws IllegalArgumentException If the number of intervals is less than 8
     */
    public Ellipse setMaximumIntervals(int numIntervals) {
        if (numIntervals < MIN_INTERVALS) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMaximumIntervals", "invalidNumIntervals"));
        }

        this.maximumIntervals = numIntervals;
        this.reset();
        return this;
    }

    @Override
    protected void reset() {
        // placeholder for responding to shape property changes
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.center == null) {
            return; // nothing to draw
        }

        if (this.majorRadius == 0 && this.minorRadius == 0) {
            return; // nothing to draw
        }

        // placeholder for drawing code
    }
}
