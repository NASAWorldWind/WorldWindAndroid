/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.view.Choreographer;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

public class DayNightCycleActivity extends BasicGlobeActivity implements Choreographer.FrameCallback {

    protected Location sunLocation = new Location(0, -100);

    protected AtmosphereLayer atmosphereLayer;

    // Animation settings

    protected double cameraDegreesPerSecond = 2.0;

    protected double lightDegreesPerSecond = 6.0;

    protected long lastFrameTimeNanos;

    protected boolean activityPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_day_night_cycle));
        setAboutBoxText("Demonstrates how to display a continuous day-night cycle on the WorldWind globe.\n" +
            "This gradually changes both the Camera's location and the AtmosphereLayer's light location.");

        // Initialize the Atmosphere layer's light location to our custom location. By default the light location is
        // always behind the viewer.
        LayerList layers = this.getWorldWindow().getLayers();
        this.atmosphereLayer = (AtmosphereLayer) layers.getLayer(layers.indexOfLayerNamed("Atmosphere"));
        this.atmosphereLayer.setLightLocation(this.sunLocation);

        // Initialize the Camera so that the sun is behind the viewer.
        Camera camera = this.getWorldWindow().getCamera();
        camera.position.latitude = 20;
        camera.position.longitude = this.sunLocation.longitude;

        // Use this Activity's Choreographer to animate the day-night cycle.
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.lastFrameTimeNanos != 0) {
            // Compute the frame duration in seconds.
            double frameDurationSeconds = (frameTimeNanos - this.lastFrameTimeNanos) * 1.0e-9;
            double cameraDegrees = (frameDurationSeconds * this.cameraDegreesPerSecond);
            double lightDegrees = (frameDurationSeconds * this.lightDegreesPerSecond);

            // Move the camera to simulate the Earth's rotation about its axis.
            Camera camera = getWorldWindow().getCamera();
            camera.position.longitude -= cameraDegrees;

            // Move the sun location to simulate the Sun's rotation about the Earth.
            this.sunLocation.set(this.sunLocation.latitude, this.sunLocation.longitude - lightDegrees);
            this.atmosphereLayer.setLightLocation(this.sunLocation);

            // Redraw the WorldWindow to display the above changes.
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
        // Resume the day-night cycle animation.
        this.activityPaused = false;
        this.lastFrameTimeNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }
}
