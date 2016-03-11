/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.BasicNavigatorController;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.DrawContext;

public class SimpleNavigatorController extends BasicNavigatorController {

    public SimpleNavigatorController() {
    }

    @Override
    public void windowWillDraw(DrawContext dc) {

        WorldWindow wwd = this.getWorldWindow();
        wwd.getNavigator().getPosition().longitude += 0.5;
        dc.requestRender();
    }
}
