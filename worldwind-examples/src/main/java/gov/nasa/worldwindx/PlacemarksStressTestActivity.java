/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;

import java.util.Random;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

import static java.lang.Math.asin;
import static java.lang.Math.toDegrees;

public class PlacemarksStressTestActivity extends BasicGlobeActivity implements Runnable {

    protected Handler animationHandler = new Handler();

    protected boolean pauseHandler;

    static final int DELAY_TIME = 100;

    static final int NUM_PLACEMARKS = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_stress_test));
        setAboutBoxText("Demonstrates a LOT of Placemarks.");

        // Turn off all layers while debugging/profiling memory allocations...
        for (Layer l : this.getWorldWindow().getLayers()) {
            l.setEnabled(false);
        }
        // ... and add the tessellation layer instead
        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());

        // Create a Renderable layer for the placemarks and add it to the WorldWindow
        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        this.getWorldWindow().getLayers().addLayer(placemarksLayer);

        // Create some placemarks at a known locations
        Placemark origin = new Placemark(Position.fromDegrees(0, 0, 1e5),
            PlacemarkAttributes.withImageAndLabelAndLeaderLine(ImageSource.fromResource(R.drawable.pushpin_plain_yellow)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "Origin");
        Placemark northPole = new Placemark(Position.fromDegrees(90, 0, 1e5),
            PlacemarkAttributes.withImageAndLabelAndLeaderLine(ImageSource.fromResource(R.drawable.pushpin_plain_white)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "North Pole");
        Placemark southPole = new Placemark(Position.fromDegrees(-90, 0, 0),
            PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.pushpin_plain_black)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "South Pole");
        Placemark antiMeridian = new Placemark(Position.fromDegrees(0, 180, 0),
            PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.pushpin_plain_green)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "Anti-meridian");

        placemarksLayer.addRenderable(origin);
        placemarksLayer.addRenderable(northPole);
        placemarksLayer.addRenderable(southPole);
        placemarksLayer.addRenderable(antiMeridian);


        // Create a random number generator with an arbitrary seed
        // that will generate the same numbers between runs.
        Random random = new Random(123);

        // Create pushpins anchored at the "pinpoints" with eye distance scaling
        PlacemarkAttributes attributes = PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.pushpin_plain_red))
            .setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN);
        for (int i = 0; i < NUM_PLACEMARKS; i++) {
            // Create an even distribution of latitude and longitudes across the globe.
            // Use a random sin value to generate latitudes without clustering at the poles.
            double lat = toDegrees(asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
            double lon = 180d - (random.nextDouble() * 360);
            Position pos = Position.fromDegrees(lat, lon, 0);

            Placemark placemark = new Placemark(pos,
                new PlacemarkAttributes(attributes).setMinimumImageScale(0.5)).setEyeDistanceScaling(true);
            placemark.setDisplayName(placemark.getPosition().toString());

            placemarksLayer.addRenderable(placemark);
        }
    }


    @Override
    public void run() {
        // Move the navigator to simulate the Earth's rotation about its axis.
        Navigator navigator = getWorldWindow().getNavigator();
        navigator.setLongitude(navigator.getLongitude() - 0.03);

        // Redraw the World Window to display the above changes.
        getWorldWindow().requestRender();

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
        // Resume the Handler that changes earth's rotation.
        this.pauseHandler = false;
        this.animationHandler.postDelayed(this, DELAY_TIME);
    }
}
