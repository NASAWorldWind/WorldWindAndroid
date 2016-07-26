/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

/**
 * Interface to controlling a shape's attributes. Shapes implementing this interface use the {@link ShapeAttributes}
 * bundle for specifying the normal attributes and the highlight attributes.
 */
public interface Attributable {

    /**
     * Indicates the shape's normal (non-highlight) attributes.
     *
     * @return the shape's normal attributes
     */
    ShapeAttributes getAttributes();

    /**
     * Specifies the shape's normal (non-highlight) attributes. If null and this shape is not highlighted, this shape is
     * not drawn.
     * <p>
     * It is permissible to share attribute bundles between shapes.
     *
     * @param attributes a reference to the shape's new normal attributes
     */
    void setAttributes(ShapeAttributes attributes);

    /**
     * Indicates the shape's highlight attributes.
     *
     * @return the shape's highlight attributes
     */
    ShapeAttributes getHighlightAttributes();

    /**
     * Specifies the shape's highlight attributes. If null and this shape is highlighted, this shape's normal attributes
     * are used. If they in turn are null, this shape is not drawn.
     * <p>
     * It is permissible to share attribute bundles between shapes.
     *
     * @param highlightAttributes a reference to the shape's new highlight attributes
     */
    void setHighlightAttributes(ShapeAttributes highlightAttributes);
}
