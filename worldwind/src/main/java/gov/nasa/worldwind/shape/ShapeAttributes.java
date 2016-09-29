/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

/**
 * Holds attributes applied to geographic shapes.
 */
public class ShapeAttributes {

    protected boolean drawInterior;

    protected boolean drawOutline;

    protected boolean drawVerticals;

    protected boolean depthTest;

    protected boolean enableLighting;

    protected Color interiorColor;

    protected Color outlineColor;

    protected float outlineWidth;

    protected ImageSource interiorImageSource;

    protected ImageSource outlineImageSource;

    public ShapeAttributes() {
        this.drawInterior = true;
        this.drawOutline = true;
        this.drawVerticals = false;
        this.depthTest = true;
        this.enableLighting = false;
        this.interiorColor = new Color(1, 1, 1, 1); // white
        this.outlineColor = new Color(1, 0, 0, 1); // red
        this.outlineWidth = 1.0f;
        this.interiorImageSource = null;
        this.outlineImageSource = null;
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
        this.interiorImageSource = attributes.interiorImageSource;
        this.outlineImageSource = attributes.outlineImageSource;
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
        this.interiorImageSource = attributes.interiorImageSource;
        this.outlineImageSource = attributes.outlineImageSource;
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
            && ((this.interiorImageSource == null) ? (that.interiorImageSource == null) : this.interiorImageSource.equals(that.interiorImageSource))
            && ((this.outlineImageSource == null) ? (that.outlineImageSource == null) : this.outlineImageSource.equals(that.outlineImageSource));
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
        result = 31 * result + (this.interiorImageSource != null ? this.interiorImageSource.hashCode() : 0);
        result = 31 * result + (this.outlineImageSource != null ? this.outlineImageSource.hashCode() : 0);
        return result;
    }

    /**
     * Indicates whether shape interiors are enabled.
     *
     * @return true if shape interiors are enabled, and false otherwise
     */
    public boolean isDrawInterior() {
        return this.drawInterior;
    }

    /**
     * Sets whether to enable shape interiors.
     *
     * @param enable true to enable shape interiors, and false otherwise
     */
    public ShapeAttributes setDrawInterior(boolean enable) {
        this.drawInterior = enable;
        return this;
    }

    /**
     * Indicates whether shape outlines are enabled.
     *
     * @return true if shape outlines are enabled, and false otherwise
     */
    public boolean isDrawOutline() {
        return this.drawOutline;
    }

    /**
     * Sets whether to enable shape outlines.
     *
     * @param enable true to enable shape outlines, and false otherwise
     */
    public ShapeAttributes setDrawOutline(boolean enable) {
        this.drawOutline = enable;
        return this;
    }

    /**
     * Indicates whether shape vertical outlines are enabled. Not all shapes display vertical outlines. Those that do
     * not ignore this property. When enabled, those that do display vertical lines extending from the shape's specified
     * positions to the ground.
     *
     * @return true if shape vertical outlines are enabled, and false otherwise
     */
    public boolean isDrawVerticals() {
        return this.drawVerticals;
    }

    /**
     * Sets whether to enable shape vertical outlines. Not all shapes display vertical outlines. Those that do not
     * ignore this property. When enabled, those that do display vertical lines extending from the shape's specified
     * positions to the ground.
     *
     * @param enable true to enable shape vertical outlines, and false otherwise
     */
    public ShapeAttributes setDrawVerticals(boolean enable) {
        this.drawVerticals = enable;
        return this;
    }

    /**
     * Indicates whether shape depth-testing is enabled. When true, shapes may be occluded by terrain and other shapes
     * in certain viewing situations. When false, shapes will not be occluded by terrain and other shapes.
     *
     * @return true if enable shape depth-testing is enabled, and false otherwise
     */
    public boolean isDepthTest() {
        return this.depthTest;
    }

    /**
     * Sets whether to enable shape depth-testing. When true, shapes may be occluded by terrain and other shapes in
     * certain viewing situations. When false, shapes will not be occluded by terrain and other shapes.
     *
     * @param enable true to enable shape depth-testing, and false otherwise
     */
    public ShapeAttributes setDepthTest(boolean enable) {
        this.depthTest = enable;
        return this;
    }

    /**
     * Sets whether shape lighting is enabled. When true, the appearance of a shape's color and image source may be
     * modified by shading applied from a global light source.
     *
     * @return true if shape lighting is enabled, and false otherwise
     */
    public boolean isEnableLighting() {
        return this.enableLighting;
    }

    /**
     * Sets whether to enable shape lighting. When true, the appearance of a shape's color and image source may be
     * modified by shading applied from a global light source.
     *
     * @param enable true to enable shape lighting, and false otherwise
     */
    public ShapeAttributes setEnableLighting(boolean enable) {
        this.enableLighting = enable;
        return this;
    }

    /**
     * Indicates the color and opacity of shape interiors.
     *
     * @return the RGBA color used for shape interiors
     */
    public Color getInteriorColor() {
        return this.interiorColor;
    }

    /**
     * Sets shape interior color and opacity.
     *
     * @param color the new RGBA color to use for shape interiors
     *
     * @throws IllegalArgumentException If the color is null
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
     * Indicates the color and opacity of shape outlines.
     *
     * @return the RGBA color used for shape outlines
     */
    public Color getOutlineColor() {
        return this.outlineColor;
    }

    /**
     * Sets shape outline color and opacity.
     *
     * @param color the new RGBA color to use for shape outlines
     *
     * @throws IllegalArgumentException If the color is null
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
     * Indicates the width of shape outlines.
     *
     * @return the line width, in screen pixels.
     */
    public float getOutlineWidth() {
        return this.outlineWidth;
    }

    /**
     * Sets shape outline width.
     *
     * @param lineWidth the new line width, in screen pixels
     */
    public ShapeAttributes setOutlineWidth(float lineWidth) {
        this.outlineWidth = lineWidth;
        return this;
    }

    /**
     * Indicates the image source applied to shape interiors. When null, shape interiors are displayed in the interior
     * color. When non-null, image pixels appear in shape interiors, with each image pixel multiplied by the interior
     * RGBA color. Use a white interior color to display unmodified image pixels.
     * <p/>
     * By default, interior image sources are displayed as a repeating pattern across shape interiors. The pattern
     * matches image pixels to screen pixels, such that the image appears to repeat in screen coordinates.
     *
     * @return a reference to the interior image source; may be null
     */
    public ImageSource getInteriorImageSource() {
        return this.interiorImageSource;
    }

    /**
     * Sets the image source to apply to shape interiors. When null, shape interiors are displayed in the interior
     * color. When non-null, image pixels appear in shape interiors, with each image pixel multiplied by the interior
     * RGBA color. Use a white interior color to display the original image pixels.
     * <p/>
     * Interior images appear as a two-dimensional repeating pattern that fills each shape's interior. The image is
     * repeated both horizontally and vertically in screen coordinates, with camera perspective applied to the pattern.
     *
     * @param imageSource a reference to the new interior image source; may be null
     */
    public ShapeAttributes setInteriorImageSource(ImageSource imageSource) {
        this.interiorImageSource = imageSource;
        return this;
    }

    /**
     * Indicates the image source applied to shape outlines.
     *
     * @return a reference to the outline image source; may be null
     */
    public ImageSource getOutlineImageSource() {
        return this.outlineImageSource;
    }

    /**
     * Sets the image source to apply to shape outlines. When null, shape outlines are displayed in the outline color.
     * When non-null, image pixels appear along shape outlines, with each image pixel multiplied by the outline RGBA
     * color. Use a white outline color to display the original image pixels.
     * <p/>
     * Outline images appear as a one-dimensional repeating pattern that fills each shape's outline. The first row of
     * image pixels is repeated linearly along shape outlines in screen coordinates, with camera perspective applied to
     * the pattern.
     * <p/>
     * Dashed shape outlines are accomplished with a one-dimensional outline image containing a mix of a transparent
     * pixels and white pixels. The image width indicates the pattern length in screen coordinates, while the
     * transparent pixels and white pixels indicate the dashing pattern. To display dashed lines similar to OpenGL's
     * classic line stippling feature, see {@link ImageSource#fromLineStipple(int, short)}.
     *
     * @param imageSource a reference to the new outline image source; may be null
     */
    public ShapeAttributes setOutlineImageSource(ImageSource imageSource) {
        this.outlineImageSource = imageSource;
        return this;
    }
}
