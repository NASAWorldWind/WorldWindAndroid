/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Logger;

/**
 * Holds attributes applied to {@link Placemark} shapes.
 */
public class PlacemarkAttributes {

    /**
     * An offset for anchoring a 64x64 classic pushpin image's "point" on the geographic position.
     */
    public static final Offset OFFSET_PUSHPIN = new Offset(WorldWind.OFFSET_FRACTION, 19d / 64d, WorldWind.OFFSET_FRACTION, 4d / 64d);

    protected Color imageColor;

    protected Offset imageOffset;

    protected double imageScale;

    protected Object imageSource;

    protected boolean depthTest;

    protected TextAttributes labelAttributes;

    protected boolean drawLeaderLine;

    protected ShapeAttributes leaderLineAttributes;

    /**
     * Constructs a placemark attributes bundle. The defaults indicate a placemark displayed as a white 1x1 pixel square
     * centered on the placemark's geographic position.
     */
    public PlacemarkAttributes() {
        this.imageColor = new Color(1, 1, 1, 1);
        this.imageOffset = new Offset(Offset.CENTER);
        this.imageScale = 1;
        this.imageSource = null;
        this.labelAttributes = null;
        this.leaderLineAttributes = null;
        this.drawLeaderLine = false;
        this.depthTest = true;
    }


    /**
     * Constructs a placemark attribute bundle from the specified attributes. Performs a deep copy of the color, offset,
     * label attributes and leader-line attributes.
     *
     * @param copy The attributes to be copied.
     *
     * @throws IllegalArgumentException If the location is null
     */
    public PlacemarkAttributes(PlacemarkAttributes copy) {
        if (copy == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "constructor", "missingAttributes"));
        }

        this.imageColor = new Color(copy.imageColor);
        this.imageOffset = new Offset(copy.imageOffset);
        this.imageScale = copy.imageScale;
        this.imageSource = copy.imageSource;  // TODO: resolve shallow or deep copy of imageSource
        this.depthTest = copy.depthTest;
        this.labelAttributes = copy.labelAttributes != null ? new TextAttributes(copy.labelAttributes) : null;
        this.drawLeaderLine = copy.drawLeaderLine;
        this.leaderLineAttributes = copy.leaderLineAttributes != null ? new ShapeAttributes(copy.leaderLineAttributes) : null;
    }

    public PlacemarkAttributes set(PlacemarkAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "set", "missingAttributes"));
        }

        this.imageColor.set(attributes.imageColor);
        this.imageOffset.set(attributes.imageOffset);
        this.imageScale = attributes.imageScale;
        this.imageSource = attributes.imageSource;  // TODO: resolve shallow or deep copy of imageSource
        this.depthTest = attributes.depthTest;
        if (attributes.labelAttributes != null) {
            if (this.labelAttributes == null) {
                this.labelAttributes = new TextAttributes(attributes.labelAttributes);
            } else {
                this.labelAttributes.set(attributes.labelAttributes);
            }
        } else {
            this.labelAttributes = null;
        }
        this.drawLeaderLine = attributes.drawLeaderLine;
        if (attributes.leaderLineAttributes != null) {
            if (this.leaderLineAttributes == null) {
                this.leaderLineAttributes = new ShapeAttributes(attributes.leaderLineAttributes);
            } else {
                this.leaderLineAttributes.set(attributes.leaderLineAttributes);
            }
        } else {
            this.leaderLineAttributes = null;
        }
        return this;
    }

    public static PlacemarkAttributes defaults() {
        return new PlacemarkAttributes();
    }

    public static PlacemarkAttributes defaultsAndLabel() {
        return new PlacemarkAttributes().setLabelAttributes(new TextAttributes());
    }

    public static PlacemarkAttributes defaultsAndLeaderLine() {
        return new PlacemarkAttributes().setLeaderLineAttributes(new ShapeAttributes()).setDrawLeaderLine(true);
    }

    public static PlacemarkAttributes defaultsAndLabelAndLeaderLine() {
        return new PlacemarkAttributes().setLabelAttributes(new TextAttributes()).setLeaderLineAttributes(new ShapeAttributes()).setDrawLeaderLine(true);
    }

    public static PlacemarkAttributes withImage(Object imageSource) {
        return new PlacemarkAttributes().setImageSource(imageSource);
    }

    public static PlacemarkAttributes withImageAndLabel(Object imageSource) {
        return new PlacemarkAttributes().setImageSource(imageSource).setLabelAttributes(new TextAttributes());
    }

    public static PlacemarkAttributes withImageAndLeaderLine(Object imageSource) {
        return new PlacemarkAttributes().setImageSource(imageSource).setLeaderLineAttributes(new ShapeAttributes()).setDrawLeaderLine(true);
    }

    public static PlacemarkAttributes withImageAndLabelAndLeaderLine(Object imageSource) {
        return new PlacemarkAttributes().setImageSource(imageSource).setLabelAttributes(new TextAttributes()).setLeaderLineAttributes(new ShapeAttributes()).setDrawLeaderLine(true);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlacemarkAttributes that = (PlacemarkAttributes) o;

        if (Double.compare(that.imageScale, imageScale) != 0) return false;
        if (depthTest != that.depthTest) return false;
        if (drawLeaderLine != that.drawLeaderLine) return false;
        if (imageColor != null ? !imageColor.equals(that.imageColor) : that.imageColor != null) return false;
        if (imageOffset != null ? !imageOffset.equals(that.imageOffset) : that.imageOffset != null) return false;
        if (imageSource != null ? !imageSource.equals(that.imageSource) : that.imageSource != null) return false;
        if (labelAttributes != null ? !labelAttributes.equals(that.labelAttributes) : that.labelAttributes != null)
            return false;
        return !(leaderLineAttributes != null ? !leaderLineAttributes.equals(that.leaderLineAttributes) : that.leaderLineAttributes != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = imageColor != null ? imageColor.hashCode() : 0;
        result = 31 * result + (imageOffset != null ? imageOffset.hashCode() : 0);
        temp = Double.doubleToLongBits(imageScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (imageSource != null ? imageSource.hashCode() : 0);
        result = 31 * result + (depthTest ? 1 : 0);
        result = 31 * result + (labelAttributes != null ? labelAttributes.hashCode() : 0);
        result = 31 * result + (drawLeaderLine ? 1 : 0);
        result = 31 * result + (leaderLineAttributes != null ? leaderLineAttributes.hashCode() : 0);
        return result;
    }

    /**
     * Returns the image color. When this attribute bundle has a valid image path the placemark's image is composed with
     * this image color to achieve the final placemark color. Otherwise the placemark is drawn in this color. The color
     * white, the default, causes the image to be drawn in its native colors.
     */
    public Color getImageColor() {
        return imageColor;
    }

    /**
     * Sets the image color. When this attribute bundle has a valid image path the placemark's image is composed with
     * this image color to achieve the final placemark color. Otherwise the placemark is drawn in this color. The color
     * white, the default, causes the image to be drawn in its native colors.
     *
     * @param imageColor The new image color
     */
    public PlacemarkAttributes setImageColor(Color imageColor) {
        if (imageColor == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "setImageColor", "missingColor"));
        }
        this.imageColor = imageColor;
        return this;
    }


    /**
     * Returns the location within the placemark's image to align with the placemark's geographic position. May be null,
     * in which case the image's bottom-left corner is placed at the geographic position. The default value centers the
     * image at the geographic position.
     */
    public Offset getImageOffset() {
        return imageOffset;
    }

    /**
     * Sets the location within the placemark's image to align with the placemark's geographic position. May be null, in
     * which case the image's bottom-left corner is placed at the geographic position.
     *
     * @param imageOffset The new location used to align the placemark's image.
     */
    public PlacemarkAttributes setImageOffset(Offset imageOffset) {
        this.imageOffset = imageOffset;
        return this;
    }

    /**
     * Returns the amount to scale the placemark's image. When this attribute bundle has a valid image path the scale is
     * applied to the image's dimensions. Otherwise the scale indicates the dimensions in pixels of a square drawn at
     * the placemark's geographic position. A scale of 0 causes the placemark to disappear; however, the placemark's
     * label, if any, is still drawn.
     */
    public double getImageScale() {
        return imageScale;
    }

    /**
     * Sets the amount to scale the placemark's image. When this attribute bundle has a valid image path the scale is
     * applied to the image's dimensions. Otherwise the scale indicates the dimensions in pixels of a square drawn at
     * the placemark's geographic position. A scale of 0 causes the placemark to disappear; however, the placemark's
     * label, if any, is still drawn.
     *
     * @param imageScale The new image scale value.
     */
    public PlacemarkAttributes setImageScale(double imageScale) {
        this.imageScale = imageScale;
        return this;
    }

    /**
     * Returns the source of the placemark's image. May be either a string giving the URL of the image, or an {@link
     * ImageSource} object identifying an Image created dynamically. If null, the placemark is drawn as a square whose
     * width and height are the value of this attribute object's [imageScale]{@link PlacemarkAttributes#getImageScale}
     * property.
     */
    public Object getImageSource() {
        return imageSource;
    }

    public PlacemarkAttributes setImageSource(Object imageSource) {
        this.imageSource = imageSource;
        return this;
    }

    /**
     * Returns whether the placemark should be depth-tested against other objects in the scene. If true, the placemark
     * may be occluded by terrain and other objects in certain viewing situations. If false, the placemark will not be
     * occluded by terrain and other objects. If this value is true, the placemark's label, if any, has an independent
     * depth-test control.
     */
    public boolean isDepthTest() {
        return depthTest;
    }

    /**
     * Sets whether the placemark should be depth-tested against other objects in the scene. If true, the placemark may
     * be occluded by terrain and other objects in certain viewing situations. If false, the placemark will not be
     * occluded by terrain and other objects. If this value is true, the placemark's label, if any, has an independent
     * depth-test control.
     *
     * @param depthTest The new depth test setting.
     */
    public PlacemarkAttributes setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
        return this;
    }

    /**
     * Returns the attributes to apply to the placemark's label, if any. If null, the placemark's label is not drawn.
     */
    public Object getLabelAttributes() {
        return labelAttributes;
    }

    /**
     * Sets the attributes to apply to the placemark's label, if any. If null, the placemark's label is not drawn.
     *
     * @param labelAttributes The new label attributes for the placemark. May be null.
     */
    public PlacemarkAttributes setLabelAttributes(TextAttributes labelAttributes) {
        this.labelAttributes = labelAttributes;
        return this;
    }

    /**
     * Returns whether to draw a line from the placemark's geographic position to the ground.
     */
    public boolean isDrawLeaderLine() {
        return drawLeaderLine;
    }

    /**
     * Sets whether to draw a line from the placemark's geographic position to the ground.
     *
     * @param drawLeaderLine The new draw leader-line setting.
     */
    public PlacemarkAttributes setDrawLeaderLine(boolean drawLeaderLine) {
        this.drawLeaderLine = drawLeaderLine;
        return this;
    }

    /**
     * Returns the attributes to apply to the leader line if it's drawn. If null, the placemark's leader line is not
     * drawn.
     */
    public ShapeAttributes getLeaderLineAttributes() {
        return leaderLineAttributes;
    }

    /**
     * Sets the attributes to apply to the leader line if it's drawn. If null, the placemark's leader line is not
     * drawn.
     *
     * @param leaderLineAttributes The new leader-line attributes. May be null.
     */
    public PlacemarkAttributes setLeaderLineAttributes(ShapeAttributes leaderLineAttributes) {
        this.leaderLineAttributes = leaderLineAttributes;
        return this;
    }
}
