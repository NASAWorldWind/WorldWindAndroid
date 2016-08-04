/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

/**
 * Interface to control a shape's highlighting. Shapes implementing this interface have their own highlighting behaviors
 * and attributes and the means for setting them.
 */
public interface Highlightable {

    /**
     * Indicates whether the shape is highlighted.
     *
     * @return true if the shape is highlighted, otherwise false
     */
    boolean isHighlighted();

    /**
     * Specifies whether to highlight the shape.
     *
     * @param highlighted true to highlight the shape, otherwise false
     */
    void setHighlighted(boolean highlighted);
}
