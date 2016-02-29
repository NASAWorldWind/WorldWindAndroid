/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.view.MotionEvent;
import android.view.View;

public interface NavigatorController {

    Navigator getNavigator();

    void setNavigator(Navigator navigator);

    void onTouch(View view, MotionEvent event);
}
