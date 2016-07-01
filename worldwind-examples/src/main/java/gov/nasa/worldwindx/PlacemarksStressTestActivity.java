/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.view.Choreographer;

import java.util.Random;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class PlacemarksStressTestActivity extends GeneralGlobeActivity implements Choreographer.FrameCallback {

    protected static final int NUM_PLACEMARKS = 10000;

    protected boolean activityPaused;

    protected double cameraDegreesPerSecond = 2.0;

    protected long lastFrameTimeNanos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_placemarks_stress_test));
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
            PlacemarkAttributes.createWithImageAndLeader(ImageSource.fromResource(R.drawable.airport)),
            "Origin");
        Placemark northPole = new Placemark(Position.fromDegrees(90, 0, 1e5),
            PlacemarkAttributes.createWithImageAndLeader(ImageSource.fromResource(R.drawable.airport_terminal)),
            "North Pole");
        Placemark southPole = new Placemark(Position.fromDegrees(-90, 0, 0),
            PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.airplane)),
            "South Pole");
        Placemark antiMeridian = new Placemark(Position.fromDegrees(0, 180, 0),
            PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.ic_menu_home)),
            "Anti-meridian");

        placemarksLayer.addRenderable(origin);
        placemarksLayer.addRenderable(northPole);
        placemarksLayer.addRenderable(southPole);
        placemarksLayer.addRenderable(antiMeridian);

        // Create a random number generator with an arbitrary seed
        // that will generate the same numbers between runs.
        Random random = new Random(123);

        // Create pushpins anchored at the "pinpoints" with eye distance scaling
        PlacemarkAttributes attributes = PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.aircraft_fixwing));

        for (int i = 0; i < NUM_PLACEMARKS; i++) {
            // Create an even distribution of latitude and longitudes across the globe.
            // Use a random sin value to generate latitudes without clustering at the poles.
            double lat = Math.toDegrees(Math.asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
            double lon = 180d - (random.nextDouble() * 360);
            Position pos = Position.fromDegrees(lat, lon, 0);

            Placemark placemark = new Placemark(pos,
                new PlacemarkAttributes(attributes).setMinimumImageScale(0.5)).setEyeDistanceScaling(true);
            placemark.setDisplayName(placemark.getPosition().toString());

            placemarksLayer.addRenderable(placemark);
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.lastFrameTimeNanos != 0) {
            // Compute the frame duration in seconds.
            double frameDurationSeconds = (frameTimeNanos - this.lastFrameTimeNanos) * 1.0e-9;
            double cameraDegrees = (frameDurationSeconds * this.cameraDegreesPerSecond);

            // Move the navigator to simulate the Earth's rotation about its axis.
            Navigator navigator = getWorldWindow().getNavigator();
            navigator.setLongitude(navigator.getLongitude() - cameraDegrees);

            // Redraw the World Window to display the above changes.
            this.getWorldWindow().requestRedraw();
        }

        if (!this.activityPaused) { // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this);
        }

        this.lastFrameTimeNanos = frameTimeNanos;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the animation when this activity is paused.
        this.activityPaused = true;
        this.lastFrameTimeNanos = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the earth rotation animation
        this.activityPaused = false;
        this.lastFrameTimeNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }
}
