/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.view.MotionEvent;

public interface WorldWindowController {

    WorldWindow getWorldWindow();

    void setWorldWindow(WorldWindow wwd);

    boolean onTouchEvent(MotionEvent event);
}
