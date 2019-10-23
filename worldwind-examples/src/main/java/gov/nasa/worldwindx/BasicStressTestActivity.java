/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.view.Choreographer;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.layer.ShowTessellationLayer;

public class BasicStressTestActivity extends GeneralGlobeActivity implements Choreographer.FrameCallback {

    protected double cameraDegreesPerSecond = 0.1;

    protected long lastFrameTimeNanos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_basic_stress_test));
        this.setAboutBoxText("Continuously moves the camera in an Easterly direction from a low altitude.");

        // Add the ShowTessellation layer to provide some visual feedback regardless of texture details
        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());

        // Initialize the Camera so that it's looking in the direction of movement and the horizon is visible.
        Camera camera = this.getWorldWindow().getCamera();
        camera.position.altitude = 1e3; // 1 km
        camera.heading = 90; // looking east
        camera.tilt = 75; // looking at the horizon
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.lastFrameTimeNanos != 0) {
            // Compute the frame duration in seconds.
            double frameDurationSeconds = (frameTimeNanos - this.lastFrameTimeNanos) * 1.0e-9;
            double cameraDegrees = (frameDurationSeconds * this.cameraDegreesPerSecond);

            // Move the camera to continuously bring new tiles into view.
            Camera camera = getWorldWindow().getCamera();
            camera.position.longitude += cameraDegrees;

            // Redraw the WorldWindow to display the above changes.
            this.getWorldWindow().requestRedraw();
        }

        Choreographer.getInstance().postFrameCallback(this);
        this.lastFrameTimeNanos = frameTimeNanos;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the animation when this activity is paused.
        Choreographer.getInstance().removeFrameCallback(this);
        this.lastFrameTimeNanos = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Use this Activity's Choreographer to animate the Camera.
        Choreographer.getInstance().postFrameCallback(this);
        this.lastFrameTimeNanos = 0;
    }
}
