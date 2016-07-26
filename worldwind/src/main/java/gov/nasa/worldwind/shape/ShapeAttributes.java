/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

public class ShapeAttributes {

    protected boolean drawInterior;

    protected boolean drawOutline;

    protected boolean drawVerticals;

    protected boolean depthTest;

    protected boolean enableLighting;

    protected Color interiorColor;

    protected Color outlineColor;

    protected float outlineWidth;

    protected int outlineStippleFactor;

    protected short outlineStipplePattern;

    protected ImageSource imageSource;

    public ShapeAttributes() {
        this.drawInterior = true;
        this.drawOutline = true;
        this.drawVerticals = false;
        this.depthTest = true;
        this.enableLighting = false;
        this.interiorColor = new Color(1, 1, 1, 1); // white
        this.outlineColor = new Color(1, 0, 0, 1); // red
        this.outlineWidth = 1.0f;
        this.outlineStippleFactor = 0;
        this.outlineStipplePattern = (short) 0xF0F0;
        this.imageSource = null;
    }

    public ShapeAttributes(ShapeAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ShapeAttributes", "constructor", "missingAttributes"));
        }

        this.drawInterior = attributes.drawInterior;
        this.drawOutline = attributes.drawOutline;
        this.drawVerticals = attributes.drawVerticals;
        this.depthTest = attributes.depthTest;
        this.enableLighting = attributes.enableLighting;
        this.interiorColor = new Color(attributes.interiorColor);
        this.outlineColor = new Color(attributes.outlineColor);
        this.outlineWidth = attributes.outlineWidth;
        this.outlineStippleFactor = attributes.outlineStippleFactor;
        this.outlineStipplePattern = attributes.outlineStipplePattern;
        this.imageSource = attributes.imageSource;
    }

    public ShapeAttributes set(ShapeAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ShapeAttributes", "set", "missingAttributes"));
        }

        this.drawInterior = attributes.drawInterior;
        this.drawOutline = attributes.drawOutline;
        this.drawVerticals = attributes.drawVerticals;
        this.depthTest = attributes.depthTest;
        this.enableLighting = attributes.enableLighting;
        this.interiorColor.set(attributes.interiorColor);
        this.outlineColor.set(attributes.outlineColor);
        this.outlineWidth = attributes.outlineWidth;
        this.outlineStippleFactor = attributes.outlineStippleFactor;
        this.outlineStipplePattern = attributes.outlineStipplePattern;
        this.imageSource = attributes.imageSource;
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

        ShapeAttributes that = (ShapeAttributes) o;
        return this.drawInterior == that.drawInterior
            && this.drawOutline == that.drawOutline
            && this.drawVerticals == that.drawVerticals
            && this.depthTest == that.depthTest
            && this.enableLighting == that.enableLighting
            && this.interiorColor.equals(that.interiorColor)
            && this.outlineColor.equals(that.outlineColor)
            && this.outlineWidth == that.outlineWidth
            && this.outlineStippleFactor == that.outlineStippleFactor
            && this.outlineStipplePattern == that.outlineStipplePattern
            && (this.imageSource == null) ? (that.imageSource == null) : this.imageSource.equals(that.imageSource);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (this.drawInterior ? 1 : 0);
        result = 31 * result + (this.drawOutline ? 1 : 0);
        result = 31 * result + (this.depthTest ? 1 : 0);
        result = 31 * result + (this.drawVerticals ? 1 : 0);
        result = 31 * result + (this.enableLighting ? 1 : 0);
        result = 31 * result + this.interiorColor.hashCode();
        result = 31 * result + this.outlineColor.hashCode();
        temp = Double.doubleToLongBits(this.outlineWidth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + this.outlineStippleFactor;
        result = 31 * result + (int) outlineStipplePattern;
        result = 31 * result + (this.imageSource != null ? this.imageSource.hashCode() : 0);
        return result;
    }

    /**
     * Indicates whether the interior of the associated shape is drawn.
     */
    public boolean isDrawInterior() {
        return this.drawInterior;
    }

    /**
     * Indicates whether the interior of the associated shape is drawn.
     */
    public ShapeAttributes setDrawInterior(boolean draw) {
        this.drawInterior = draw;
        return this;
    }

    /**
     * Indicates whether the outline of the associated shape is drawn
     */
    public boolean isDrawOutline() {
        return this.drawOutline;
    }

    /**
     * Indicates whether the outline of the associated shape is drawn
     */
    public ShapeAttributes setDrawOutline(boolean enable) {
        this.drawOutline = enable;
        return this;
    }

    /**
     * Indicates whether this shape should draw vertical lines extending from its specified positions to the ground.
     */
    public boolean isDrawVerticals() {
        return this.drawVerticals;
    }

    /**
     * Indicates whether this shape should draw vertical lines extending from its specified positions to the ground.
     */
    public ShapeAttributes setDrawVerticals(boolean enable) {
        this.drawVerticals = enable;
        return this;
    }

    /**
     * Indicates whether the shape should be depth-tested against other objects in the scene. If true, the shape may be
     * occluded by terrain and other objects in certain viewing situations. If false, the shape will not be occluded by
     * terrain and other objects.
     */
    public boolean isDepthTest() {
        return this.depthTest;
    }

    /**
     * Indicates whether the shape should be depth-tested against other objects in the scene. If true, the shape may be
     * occluded by terrain and other objects in certain viewing situations. If false, the shape will not be occluded by
     * terrain and other objects.
     */
    public ShapeAttributes setDepthTest(boolean enable) {
        this.depthTest = enable;
        return this;
    }

    /**
     * Indicates whether lighting is applied to the associated shape.
     */
    public boolean isEnableLighting() {
        return this.enableLighting;
    }

    /**
     * Indicates whether lighting is applied to the associated shape.
     */
    public ShapeAttributes setEnableLighting(boolean enable) {
        this.enableLighting = enable;
        return this;
    }

    /**
     * Indicates the associated shape's interior color and opacity.
     */
    public Color getInteriorColor() {
        return this.interiorColor;
    }

    /**
     * Indicates the associated shape's interior color and opacity.
     */
    public ShapeAttributes setInteriorColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ShapeAttributes", "setInteriorColor", "missingColor"));
        }

        this.interiorColor.set(color);
        return this;
    }

    /**
     * Indicates the associated shape's outline color and opacity.
     */
    public Color getOutlineColor() {
        return this.outlineColor;
    }

    /**
     * Indicates the associated shape's outline color and opacity.
     */

    public ShapeAttributes setOutlineColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ShapeAttributes", "setOutlineColor", "missingColor"));
        }

        this.outlineColor.set(color);
        return this;
    }

    /**
     * Indicates the associated shape's outline color and opacity.
     */
    public float getOutlineWidth() {
        return this.outlineWidth;
    }

    /**
     * Indicates the associated shape's outline color and opacity.
     */

    public ShapeAttributes setOutlineWidth(float lineWidth) {
        this.outlineWidth = lineWidth;
        return this;
    }

    /**
     * Indicates the associated shape's outline stipple factor. Specifies the number of times each bit in the outline
     * stipple pattern is repeated before the next bit is used. For example, if the outline stipple factor is 3, each
     * bit is repeated three times before using the next bit. The specified factor must be either 0 or an integer
     * greater than 0. A stipple factor of 0 indicates no stippling.
     */
    public int getOutlineStippleFactor() {
        return this.outlineStippleFactor;
    }

    /**
     * Indicates the associated shape's outline stipple factor. Specifies the number of times each bit in the outline
     * stipple pattern is repeated before the next bit is used. For example, if the outline stipple factor is 3, each
     * bit is repeated three times before using the next bit. The specified factor must be either 0 or an integer
     * greater than 0. A stipple factor of 0 indicates no stippling.
     */
    public ShapeAttributes setOutlineStippleFactor(int stippleFactor) {
        this.outlineStippleFactor = stippleFactor;
        return this;
    }

    /**
     * Indicates the associated shape's outline stipple pattern. Specifies a number whose lower 16 bits define a pattern
     * of which pixels in the outline are rendered and which are suppressed. Each bit corresponds to a pixel in the
     * shape's outline, and the pattern repeats after every n*16 pixels, where n is the [stipple factor]{@link
     * ShapeAttributes#outlineStippleFactor}. For example, if the outline stipple factor is 3, each bit in the stipple
     * pattern is repeated three times before using the next bit.
     * <p>
     * To disable outline stippling, either specify a stipple factor of 0 or specify a stipple pattern of all 1 bits,
     * i.e., 0xFFFF.
     */
    public short getOutlineStipplePattern() {
        return this.outlineStipplePattern;
    }

    /**
     * Indicates the associated shape's outline stipple pattern. Specifies a number whose lower 16 bits define a pattern
     * of which pixels in the outline are rendered and which are suppressed. Each bit corresponds to a pixel in the
     * shape's outline, and the pattern repeats after every n*16 pixels, where n is the [stipple factor]{@link
     * ShapeAttributes#outlineStippleFactor}. For example, if the outline stipple factor is 3, each bit in the stipple
     * pattern is repeated three times before using the next bit.
     * <p>
     * To disable outline stippling, either specify a stipple factor of 0 or specify a stipple pattern of all 1 bits,
     * i.e., 0xFFFF.
     */
    public ShapeAttributes setOutlineStipplePattern(short stipplePattern) {
        this.outlineStipplePattern = stipplePattern;
        return this;
    }

    /**
     * Indicates the associated shape's image source. May be null, in which case no image is applied to the shape.
     */
    public Object getImageSource() {
        return this.imageSource;
    }

    /**
     * Indicates the associated shape's image source. May be null, in which case no image is applied to the shape.
     */
    public ShapeAttributes setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
        return this;
    }
}
