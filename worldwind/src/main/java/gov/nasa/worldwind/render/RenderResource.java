/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.draw.DrawContext;

/**
 * Handle to a rendering resource, such as a GLSL program, GL texture or GL vertex buffer object.
 */
public interface RenderResource {

    /**
     * Frees any resources associated with this instance. After this method returns the rendering resource is invalid,
     * and any associated GL object is deleted.
     *
     * @param dc the current draw context
     */
    void release(DrawContext dc);
}
