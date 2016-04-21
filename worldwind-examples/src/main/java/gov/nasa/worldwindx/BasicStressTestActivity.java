/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;

import gov.nasa.worldwind.Navigator;

public class BasicStressTestActivity extends BasicGlobeActivity implements Runnable {

    protected Handler animationHandler = new Handler();

    protected boolean pauseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_basic_stress_test));
        this.setAboutBoxText("Continuously moves the navigator in an Easterly direction from a low altitude.");

        // Initialize the Navigator so that it's looking in the direction of movement and the horizon is visible.
        Navigator navigator = this.getWorldWindow().getNavigator();
        navigator.setAltitude(1e3); // 1 km
        navigator.setHeading(90); // looking east
        navigator.setTilt(75); // looking at the horizon

        // Set up an Android Handler to animate the navigator.
        this.animationHandler.postDelayed(this, 500);
    }

    @Override
    public void run() {
        // Move the navigator to continuously bring new tiles into view.
        Navigator navigator = getWorldWindow().getNavigator();
        navigator.setLongitude(navigator.getLongitude() + 1.0e-4);

        // Redraw the World Window to display the above changes.
        this.getWorldWindow().requestRender();

        if (!this.pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            this.animationHandler.postDelayed(this, 30);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the Handler when this activity is paused.
        this.pauseHandler = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the Handler that animates the navigator.
        this.pauseHandler = false;
        this.animationHandler.postDelayed(this, 500);
    }
}
