/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

/**
 * Displays an omnidirectional sensor's line-of-sight within the WorldWind scene. The sensor's placement and area of
 * potential visibility are represented by a Cartesian sphere with a center position and a range. Terrain features
 * within there sphere are is considered visible if there is a direct line-of-sight between the center position and the
 * terrain point.
 * <p>
 * OmnidirectionalSensor displays an overlay on the WorldWind terrain indicating which terrain features are visible, and
 * which are occluded. Visible terrain features, those having a direct line-of-sight between to sensor's center
 * position, appear in the sensor's normal attributes or its highlight attributes, depending on the sensor's highlight
 * state. Occluded terrain features appear in the sensor's occlude attributes, regardless of highlight state. Terrain
 * features outside the sensor's range are excluded from the overlay.
 */
public class OmnidirectionalSensor extends AbstractRenderable implements Attributable, Highlightable, Movable {

    /**
     * The sensor's center position.
     */
    protected Position position = new Position();

    /**
     * The sensor's altitude mode. See {@link gov.nasa.worldwind.WorldWind.AltitudeMode}
     */
    @WorldWind.AltitudeMode
    protected int altitudeMode = WorldWind.ABSOLUTE;

    /**
     * The sensor's range from its center position in meters.
     */
    protected double range;

    /**
     * The attributes to use for visible features, when the sensor is not highlighted.
     */
    protected ShapeAttributes attributes;

    /**
     * The attributes to use for visible features, when the sensor is highlighted.
     */
    protected ShapeAttributes highlightAttributes;

    /**
     * The attributes to use for occluded features.
     */
    protected ShapeAttributes occludeAttributes;

    /**
     * The attributes to use for visible features during the current render pass.
     */
    protected ShapeAttributes activeAttributes;

    /**
     * Determines whether the normal or highlighted attributes should be used for visible features.
     */
    protected boolean highlighted;

    /**
     * Constructs a sensor that displays the line-of-sight from a specified center position and range. Visible features
     * are displayed in white, while occluded features are displayed in red.
     *
     * @param position the position where the sensor is centered
     * @param range    the sensor's range in meters from its position
     *
     * @throws IllegalArgumentException If the position is null, or if the range is negative
     */
    public OmnidirectionalSensor(Position position, double range) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "OmnidirectionalSensor", "constructor", "missingPosition"));
        }

        if (range < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "OmnidirectionalSensor", "constructor", "invalidRange"));
        }

        this.position.set(position);
        this.range = range;
        this.attributes = new ShapeAttributes();
        this.occludeAttributes = new ShapeAttributes();
        this.occludeAttributes.setInteriorColor(new Color(1, 0, 0, 1)); // red
    }

    /**
     * Constructs a sensor that displays the line-of-sight from a specified center position and range. Visible features
     * are displayed in the specified attributes, while occluded features are displayed in red.
     *
     * @param position   the position where the sensor is centered
     * @param range      the sensor's range in meters from its position
     * @param attributes a reference to an attributes bundle used by this sensor when not highlighted
     *
     * @throws IllegalArgumentException If the position is null, or if the range is negative
     */
    public OmnidirectionalSensor(Position position, double range, ShapeAttributes attributes) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "OmnidirectionalSensor", "constructor", "missingPosition"));
        }

        if (range < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "OmnidirectionalSensor", "constructor", "invalidRange"));
        }

        this.position.set(position);
        this.range = range;
        this.attributes = attributes;
        this.occludeAttributes = new ShapeAttributes();
        this.occludeAttributes.setInteriorColor(new Color(1, 0, 0, 1)); // red
    }

    /**
     * Indicates the geographic position where this sensor is centered.
     *
     * @return this sensor's geographic position
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Sets this sensor's geographic position to the values in the supplied position.
     *
     * @param position the new position where this sensor is centered
     *
     * @return this sensor, with its position set to the specified value
     *
     * @throws IllegalArgumentException If the position is null
     */
    public OmnidirectionalSensor setPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "OmnidirectionalSensor", "setPosition", "missingPosition"));
        }

        this.position.set(position);
        return this;
    }

    /**
     * Indicates the altitude mode associated with this sensor's position.
     *
     * @return the altitude mode, see {@link gov.nasa.worldwind.WorldWind.AltitudeMode} for possible
     */
    @WorldWind.AltitudeMode
    public int getAltitudeMode() {
        return this.altitudeMode;
    }

    /**
     * Sets the altitude mode associated with this sensor's position.
     *
     * @param altitudeMode the new altitude mode, see {@link gov.nasa.worldwind.WorldWind.AltitudeMode} for acceptable
     *                     values
     *
     * @return this sensor with its altitude mode set to the specified value
     */
    public OmnidirectionalSensor setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        return this;
    }

    /**
     * Indicates this sensor's range. Range represents the sensor's transmission distance in meters from its center
     * position.
     *
     * @return this sensor's range in meters.
     */
    public double getRange() {
        return this.range;
    }

    /**
     * Sets this sensor's range. Range represents the sensor's transmission distance in meters from its center
     * position.
     *
     * @param meters this sensor's range in meters
     *
     * @return this sensor with its range set to the specified value
     *
     * @throws IllegalArgumentException If the range is negative
     */
    public OmnidirectionalSensor setRange(double meters) {
        if (meters < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "OmnidirectionalSensor", "setRange", "invalidRange"));
        }

        this.range = meters;
        return this;
    }

    /**
     * Indicates this sensor's "normal" attributes. These attributes are used for the sensor's overlay when the
     * highlighted flag is false, and there is a direct line-of-sight from the sensor's center position to a terrain
     * feature. If null and this sensor is not highlighted, visible terrain features are excluded from
     * the sensor's overlay.
     *
     * @return a reference to this sensor's attributes bundle
     */
    public ShapeAttributes getAttributes() {
        return this.attributes;
    }

    /**
     * Sets this sensor's "normal" attributes to the supplied attributes bundle. These attributes are used for the
     * sensor's overlay when the highlighted flag is false, and there is a direct line-of-sight from the sensor's center
     * position to a terrain feature. If null and this sensor is not highlighted, visible terrain features are excluded
     * from the sensor's overlay.
     * <p/>
     * It is permissible to share attribute bundles between sensors.
     *
     * @param attributes a reference to an attributes bundle used by this sensor when not highlighted
     */
    public void setAttributes(ShapeAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Indicates this sensor's "highlight" attributes. These attributes are used for the sensor's overlay when the
     * highlighted flag is true, and there is a direct line-of-sight from the sensor's center position to a terrain
     * feature. If null and the highlighted flag is true, this sensor's normal attributes are used. If they, too, are
     * null, visible terrain features are excluded from the sensor's overlay.
     *
     * @return a reference to this sensor's highlight attributes bundle
     */
    public ShapeAttributes getHighlightAttributes() {
        return this.highlightAttributes;
    }

    /**
     * Sets this sensor's "highlight" attributes. These attributes are used for the sensor's overlay when the
     * highlighted flag is true, and there is a direct line-of-sight from the sensor's center position to a terrain
     * feature. If null and the highlighted flag is true, this sensor's normal attributes are used. If they, too, are
     * null, visible terrain features are excluded from the sensor's overlay.
     * <p/>
     * It is permissible to share attribute bundles between sensors.
     *
     * @param highlightAttributes a reference to the attributes bundle used by this sensor when highlighted
     */
    public void setHighlightAttributes(ShapeAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
    }

    /**
     * Indicates this sensor's "occlude" attributes. These attributes are used for the sensor's overlay when there's no
     * direct line-of-sight from the sensor's center position to a terrain feature. If null, occluded terrain features
     * are excluded from the sensor's overlay.
     *
     * @return a reference to this sensor's occlude attributes bundle
     */
    public ShapeAttributes getOccludeAttributes() {
        return this.occludeAttributes;
    }

    /**
     * Sets this sensor's "occlude" attributes. These attributes are used for the sensor's overlay when there's no
     * direct line-of-sight from the sensor's center position to a terrain feature. If null, occluded terrain features
     * are excluded from the sensor's overlay.
     * <p>
     * It is permissible to share attribute bundles between sensors.
     *
     * @param occludeAttributes a reference to an attributes bundle used by this sensor when occluded
     */
    public void setOccludeAttributes(ShapeAttributes occludeAttributes) {
        this.occludeAttributes = occludeAttributes;
    }

    /**
     * Indicates whether this sensor's overlay uses its highlight attributes rather than its normal attributes for
     * visible features.
     *
     * @return true if this sensor is highlighted, and false otherwise
     */
    @Override
    public boolean isHighlighted() {
        return this.highlighted;
    }

    /**
     * Sets the highlighted state of this sensor, which indicates whether this sensor's overlay uses its highlight
     * attributes rather than its normal attributes for visible features.
     *
     * @param highlighted true to highlight this sensor, and false otherwise
     */
    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * A position associated with the object that indicates its aggregate geographic position. For an
     * OmnidirectionalSensor, this is simply it's position property.
     *
     * @return {@link #getPosition()}
     */
    @Override
    public Position getReferencePosition() {
        return this.getPosition();
    }

    /**
     * Moves the sensor over the globe's surface. For an OmnidirectionalSensor, this simply calls {@link
     * OmnidirectionalSensor#setPosition(Position)}.
     *
     * @param globe    not used.
     * @param position the new position of the sensor's reference position.
     */
    @Override
    public void moveTo(Globe globe, Position position) {
        this.setPosition(position);
    }

    @Override
    protected void doRender(RenderContext rc) {

    }
}
