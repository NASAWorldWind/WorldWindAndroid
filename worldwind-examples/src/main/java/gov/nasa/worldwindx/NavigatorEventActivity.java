/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Logger;

public class NavigatorEventActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_navigator_event));
        setAboutBoxText("Demonstrates how to receive notification of navigator events.");

        // Create a simple Navigator Listener that logs navigator events emitted by the World Window.
        NavigatorListener listener = new NavigatorListener() {
            @Override
            public void onNavigatorEvent(WorldWindow wwd, NavigatorEvent event) {
                if (event.getType() == WorldWind.NAVIGATOR_MOVED) {
                    Logger.log(Logger.INFO, "Navigator moved");
                } else if (event.getType() == WorldWind.NAVIGATOR_STOPPED) {
                    Logger.log(Logger.INFO, "Navigator stopped");
                }
            }
        };

        // Register the Navigator Listener with the activity's World Window.
        this.getWorldWindow().addNavigatorListener(listener);
    }
}
