/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.render.DrawContext;

public interface FrameController {

    void renderFrame(DrawContext dc);

    void drawFrame(DrawContext dc);
}
