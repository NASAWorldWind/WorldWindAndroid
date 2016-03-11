/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.view.MotionEvent;

import gov.nasa.worldwind.render.DrawContext;

public class BasicNavigatorController implements NavigatorController {

    protected WorldWindow wwd;

    public BasicNavigatorController() {
    }

    @Override
    public WorldWindow getWorldWindow() {
        return wwd;
    }

    @Override
    public void setWorldWindow(WorldWindow wwd) {
        this.wwd = wwd;
    }

    @Override
    public void windowWillDraw(DrawContext dc) {

    }

    @Override
    public void windowDidDraw(DrawContext dc) {

    }

    @Override
    public void onTouch(MotionEvent event) {

    }
}
