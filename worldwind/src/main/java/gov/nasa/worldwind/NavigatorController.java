/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.view.MotionEvent;

import gov.nasa.worldwind.render.DrawContext;

public interface NavigatorController {

    WorldWindow getWorldWindow();

    void setWorldWindow(WorldWindow wwd);

    void windowWillDraw(DrawContext dc);

    void windowDidDraw(DrawContext dc);

    void onTouch(MotionEvent event);
}
