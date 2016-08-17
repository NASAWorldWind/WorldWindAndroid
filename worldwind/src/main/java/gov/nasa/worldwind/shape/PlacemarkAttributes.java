/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

/**
 * Holds attributes applied to {@link Placemark} shapes.
 */
public class PlacemarkAttributes {

    protected ImageSource imageSource;

    protected Color imageColor;

    protected Offset imageOffset;

    protected double imageScale;

    protected double minimumImageScale;

    protected boolean drawLeader;

    protected boolean depthTest;

    protected TextAttributes labelAttributes;

    protected ShapeAttributes leaderAttributes;

    /**
     * Constructs a placemark attributes bundle. The defaults indicate a placemark displayed as a white 1x1 pixel square
     * centered on the placemark's geographic position.
     */
    public PlacemarkAttributes() {
        this.imageSource = null;
        this.imageColor = new Color(1, 1, 1, 1); // white
        this.imageOffset = Offset.center();
        this.imageScale = 1;
        this.minimumImageScale = 0;
        this.drawLeader = false;
        this.depthTest = true;
        this.labelAttributes = new TextAttributes();
        this.leaderAttributes = new ShapeAttributes();
    }

    /**
     * Constructs a placemark attribute bundle from the specified attributes. Performs a deep copy of the color, offset,
     * label attributes and leader-line attributes.
     *
     * @param attributes The attributes to be copied.
     *
     * @throws IllegalArgumentException If the location is null
     */
    public PlacemarkAttributes(PlacemarkAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "constructor", "missingAttributes"));
        }

        this.imageSource = attributes.imageSource;
        this.imageColor = new Color(attributes.imageColor);
        this.imageOffset = new Offset(attributes.imageOffset);
        this.imageScale = attributes.imageScale;
        this.minimumImageScale = attributes.minimumImageScale;
        this.drawLeader = attributes.drawLeader;
        this.depthTest = attributes.depthTest;
        this.labelAttributes = attributes.labelAttributes != null ? new TextAttributes(attributes.labelAttributes) : null;
        this.leaderAttributes = attributes.leaderAttributes != null ? new ShapeAttributes(attributes.leaderAttributes) : null;
    }

    public PlacemarkAttributes set(PlacemarkAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "set", "missingAttributes"));
        }

        this.imageSource = attributes.imageSource;
        this.imageColor.set(attributes.imageColor);
        this.imageOffset.set(attributes.imageOffset);
        this.imageScale = attributes.imageScale;
        this.minimumImageScale = attributes.minimumImageScale;
        this.drawLeader = attributes.drawLeader;
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

        if (attributes.leaderAttributes != null) {
            if (this.leaderAttributes == null) {
                this.leaderAttributes = new ShapeAttributes(attributes.leaderAttributes);
            } else {
                this.leaderAttributes.set(attributes.leaderAttributes);
            }
        } else {
            this.leaderAttributes = null;
        }

        return this;
    }

    public static PlacemarkAttributes createWithImage(ImageSource imageSource) {
        return new PlacemarkAttributes().setImageSource(imageSource);
    }

    public static PlacemarkAttributes createWithImageAndLeader(ImageSource imageSource) {
        return new PlacemarkAttributes().setImageSource(imageSource).setDrawLeader(true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        PlacemarkAttributes that = (PlacemarkAttributes) o;
        return ((this.imageSource == null) ? (that.imageSource == null) : this.imageSource.equals(that.imageSource))
            && this.imageColor.equals(that.imageColor)
            && this.imageOffset.equals(that.imageOffset)
            && this.imageScale == that.imageScale
            && this.minimumImageScale == that.minimumImageScale
            && this.drawLeader == that.drawLeader
            && this.depthTest == that.depthTest
            && ((this.labelAttributes == null) ? (that.labelAttributes == null) : this.labelAttributes.equals(that.labelAttributes))
            && ((this.leaderAttributes == null) ? (that.leaderAttributes == null) : this.leaderAttributes.equals(that.leaderAttributes));
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (this.imageSource != null) ? this.imageSource.hashCode() : 0;
        result = 31 * result + this.imageColor.hashCode();
        result = 31 * result + this.imageOffset.hashCode();
        temp = Double.doubleToLongBits(this.imageScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.minimumImageScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (this.drawLeader ? 1 : 0);
        result = 31 * result + (this.depthTest ? 1 : 0);
        result = 31 * result + (this.labelAttributes != null ? this.labelAttributes.hashCode() : 0);
        result = 31 * result + (this.leaderAttributes != null ? this.leaderAttributes.hashCode() : 0);
        return result;
    }

    /**
     * Returns the source of the placemark's image. If null, the placemark is drawn as a square whose width and height
     * are the value of this attribute object's [imageScale]{@link PlacemarkAttributes#getImageScale} property.
     */
    public ImageSource getImageSource() {
        return imageSource;
    }

    public PlacemarkAttributes setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
        return this;
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
     * Returns the location within the placemark's image to align with the placemark's geographic position. The default
     * value centers the image at the geographic position.
     */
    public Offset getImageOffset() {
        return imageOffset;
    }

    /**
     * Sets the location within the placemark's image to align with the placemark's geographic position.
     *
     * @param imageOffset The new location used to align the placemark's image.
     */
    public PlacemarkAttributes setImageOffset(Offset imageOffset) {
        if (imageOffset == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "setImageOffset", "missingOffset"));
        }

        this.imageOffset.set(imageOffset);
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
     * Returns the minimum amount to scale the placemark's image. When a {@link Placemark#isEyeDistanceScaling} is true,
     * this value controls the minimum size of the rendered placemark. A value of 0 allows the placemark to disappear.
     */
    public double getMinimumImageScale() {
        return minimumImageScale;
    }

    /**
     * Sets the minimum amount to scale the placemark's image when {@link Placemark#isEyeDistanceScaling} is true. This
     * value controls the minimum size of the rendered placemark. A value of 0 allows the placemark to disappear.
     *
     * @param minimumImageScale The new image scale value.
     */
    public PlacemarkAttributes setMinimumImageScale(double minimumImageScale) {
        this.minimumImageScale = minimumImageScale;
        return this;
    }

    /**
     * Returns whether to draw a line from the placemark's geographic position to the ground.
     */
    public boolean isDrawLeader() {
        return drawLeader;
    }

    /**
     * Sets whether to draw a line from the placemark's geographic position to the ground.
     *
     * @param drawLeader The new draw leader-line setting.
     */
    public PlacemarkAttributes setDrawLeader(boolean drawLeader) {
        this.drawLeader = drawLeader;
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
     * Returns the attributes to apply to the leader line if it's drawn. If null, the placemark's leader line is not
     * drawn.
     */
    public ShapeAttributes getLeaderAttributes() {
        return leaderAttributes;
    }

    /**
     * Sets the attributes to apply to the leader line if it's drawn. If null, the placemark's leader line is not
     * drawn.
     *
     * @param leaderAttributes The new leader-line attributes. May be null.
     */
    public PlacemarkAttributes setLeaderAttributes(ShapeAttributes leaderAttributes) {
        this.leaderAttributes = leaderAttributes;
        return this;
    }
}
