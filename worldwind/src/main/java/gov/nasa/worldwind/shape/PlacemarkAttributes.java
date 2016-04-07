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
     * The image color. When this attribute bundle has a valid image path the placemark's image is composed with this
     * image color to achieve the final placemark color. Otherwise the placemark is drawn in this color. The color
     * white, the default, causes the image to be drawn in its native colors.
     */
    Color imageColor;

    /**
     * Indicates the location within the placemark's image to align with the placemark's geographic position. May be
     * null, in which case the image's bottom-left corner is placed at the geographic position.
     */
    Offset imageOffset;

    /**
     * Indicates the amount to scale the placemark's image. When this attribute bundle has a valid image path the scale
     * is applied to the image's dimensions. Otherwise the scale indicates the dimensions in pixels of a square drawn at
     * the placemark's geographic position. A scale of 0 causes the placemark to disappear; however, the placemark's
     * label, if any, is still drawn.
     */
    double imageScale;

    /**
     * The image source of the placemark's image. May be either a string giving the URL of the image, or an {@link
     * ImageSource} object identifying an Image created dynamically. If null, the placemark is drawn as a square whose
     * width and height are the value of this attribute object's [imageScale]{@link PlacemarkAttributes#imageScale}
     * property.
     */
    Object imageSource;

    /**
     * Indicates whether the placemark should be depth-tested against other objects in the scene. If true, the placemark
     * may be occluded by terrain and other objects in certain viewing situations. If false, the placemark will not be
     * occluded by terrain and other objects. If this value is true, the placemark's label, if any, has an independent
     * depth-test control.
     */
    boolean depthTest;

    /**
     * Indicates the attributes to apply to the placemark's label, if any. If null, the placemark's label is not drawn.
     *
     * @type {TextAttributes}
     */
    Object labelAttributes;

    /**
     * Indicates whether to draw a line from the placemark's geographic position to the ground.
     */
    boolean drawLeaderLine;

    /**
     * The attributes to apply to the leader line if it's drawn. If null, the placemark's leader line is not drawn.
     *
     * @type {ShapeAttributes}
     */

    Object leaderLineAttributes;

    /**
     * Constructs a placemark attributes bundle. The defaults indicate a placemark displayed as a white 1x1 pixel square
     * centered on the placemark's geographic position.
     */
    public PlacemarkAttributes() {
        this.imageColor = Color.YELLOW;  // TODO: Set imageColor to WHITE
        this.imageOffset = new Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5);
        this.imageScale = 100;   // TODO: Restore imageScale to 1
        this.imageSource = null;
        this.depthTest = false; // TODO: Set depthTest to true
        this.labelAttributes = null;
        this.drawLeaderLine = false;
        this.leaderLineAttributes = null;
    }


    /**
     * Constructs a placemark attribute bundle from the specified attributes.
     *
     * @param attributes the attributes to be copied.
     *
     * @throws IllegalArgumentException If the location is null
     */
    public PlacemarkAttributes(PlacemarkAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PlacemarkAttributes", "constructor", "missingAttributes"));
        }

        this.imageColor = attributes.imageColor;
        this.imageOffset = attributes.imageOffset;
        this.imageScale = attributes.imageScale;
        this.imageSource = attributes.imageSource;
        this.depthTest = attributes.depthTest;
        this.labelAttributes = attributes.labelAttributes;
        this.drawLeaderLine = attributes.drawLeaderLine;
        this.leaderLineAttributes = attributes.leaderLineAttributes;
    }


}
