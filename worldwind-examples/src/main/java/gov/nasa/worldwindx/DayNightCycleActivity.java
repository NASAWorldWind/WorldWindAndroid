/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

public class DayNightCycleActivity extends BasicGlobeActivity implements Runnable {

    protected Location sunLocation = new Location(0, -100);

    protected AtmosphereLayer atmosphereLayer;

    protected Handler dayNightHandler = new Handler();

    protected boolean pauseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle = "About the " + getResources().getText(R.string.title_day_night_cycle);
        this.aboutBoxText = "Demonstrates how to display a continuous day-night cycle on the World Wind globe.\n" +
            "This gradually changes both the Navigator's location and the AtmosphereLayer's light location.";

        // Initialize the Atmosphere layer's light location to our custom location. By default the light location is
        // always behind the viewer.
        LayerList layers = this.getWorldWindow().getLayers();
        this.atmosphereLayer = (AtmosphereLayer) layers.getLayer(layers.indexOfLayerNamed("Atmosphere"));
        this.atmosphereLayer.setLightLocation(this.sunLocation);

        // Initialize the Navigator so that the sun is behind the viewer.
        Navigator navigator = this.getWorldWindow().getNavigator();
        navigator.setLatitude(20);
        navigator.setLongitude(this.sunLocation.longitude);

        // Set up an Android Handler to change the day-night cycle.
        this.dayNightHandler.postDelayed(this, 500);
    }

    @Override
    public void run() {
        // Move the navigator to simulate the Earth's rotation about its axis.
        Navigator navigator = getWorldWindow().getNavigator();
        navigator.setLongitude(navigator.getLongitude() - 0.03);

        // Move the sun location to simulate the Sun's rotation about the Earth.
        this.sunLocation.set(this.sunLocation.latitude, this.sunLocation.longitude - 0.1);
        this.atmosphereLayer.setLightLocation(this.sunLocation);

        // Redraw the World Window to display the above changes.
        getWorldWindow().requestRender();

        if (!this.pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            this.dayNightHandler.postDelayed(this, 30);
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
        // Resume the Handler that changes the day-night cycle.
        this.pauseHandler = false;
        this.dayNightHandler.postDelayed(this, 500);
    }
}
