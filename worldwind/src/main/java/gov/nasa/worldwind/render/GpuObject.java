/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

/**
 * Handle to an GPU object, such as a program, texture or vertex buffer object.
 */
public interface GpuObject {

    /**
     * Returns the value that is used to reference the associated GPU object. Returns zero if the GPU object has been
     * disposed.
     *
     * @return a non-zero value used to reference the object, or zero if the object has been disposed.
     */
    int getObjectId();

    /**
     * Frees any GPU resources associated with this instance. After this method returns the referenced GPU object is
     * invalid, and <code>getObjectId</code> returns zero.
     *
     * @param dc the current draw context
     */
    void dispose(DrawContext dc);
}
