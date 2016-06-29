/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.widget.FrameLayout;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

/**
 * Creates a simple view of a globe with touch navigation and a few layers.
 */
public class BasicGlobeActivity extends AbstractMainActivity {

    protected final static String CAMERA_LATITUDE = "latitude";

    protected final static String CAMERA_LONGITUDE = "longitude";

    protected final static String CAMERA_ALTITUDE = "altitude";

    protected final static String CAMERA_ALTITUDE_MODE = "altitude_mode";

    protected final static String CAMERA_HEADING = "heading";

    protected final static String CAMERA_TILT = "tilt";

    protected final static String CAMERA_ROLL = "roll";

    /**
     * This protected member allows derived classes to override the resource used in setContentView.
     */
    protected int layoutResourceId = R.layout.activity_globe;

    /**
     * The WorldWindow (GLSurfaceView) maintained by this activity
     */
    protected WorldWindow wwd;

    /**
     * A cached reference to the Bundle passed into onCreate() and used in onStart()
     */
    protected Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;

        // Establish the activity content
        setContentView(this.layoutResourceId);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_basic_globe));
        setAboutBoxText("Demonstrates how to construct a WorldWindow with a few layers.\n" +
            "The globe uses the default navigation gestures: \n" +
            " - one-finger pan moves the camera,\n" +
            " - two-finger pinch-zoom adjusts the range to the look at position, \n" +
            " - two-finger rotate arcs the camera horizontally around the look at position,\n" +
            " - three-finger tilt arcs the camera vertically around the look at position.");

        // Create the World Window (a GLSurfaceView) which displays the globe.
        this.wwd = new WorldWindow(this);

        // Add the WorldWindow view object to the layout that was reserved for the globe.
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.wwd);

        // Setup the World Window's layers.
        this.wwd.getLayers().addLayer(new BackgroundLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        this.wwd.getLayers().addLayer(new AtmosphereLayer());
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (this.savedInstanceState != null) {
            this.restoreNavigatorState(this.savedInstanceState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.wwd.onPause(); // pauses the rendering thread
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.wwd.onResume(); // resumes a paused rendering thread
    }

    /**
     * Called by the OS when it kills the activity; saves the navigator state.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the WorldWindow's current navigator state
        this.saveNavigatorState(savedInstanceState);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Saves the Navigator's camera data to a Bundle.
     *
     * @param savedInstanceState The object the camera data is written to
     */
    protected void saveNavigatorState(Bundle savedInstanceState) {
        WorldWindow wwd = getWorldWindow();
        if (wwd != null) {
            Camera camera = wwd.getNavigator().getAsCamera(wwd.getGlobe(), new Camera());
            // Write the camera data
            savedInstanceState.putDouble(CAMERA_LATITUDE, camera.latitude);
            savedInstanceState.putDouble(CAMERA_LONGITUDE, camera.longitude);
            savedInstanceState.putDouble(CAMERA_ALTITUDE, camera.altitude);
            savedInstanceState.putDouble(CAMERA_HEADING, camera.heading);
            savedInstanceState.putDouble(CAMERA_TILT, camera.tilt);
            savedInstanceState.putDouble(CAMERA_ROLL, camera.roll);
            savedInstanceState.putInt(CAMERA_ALTITUDE_MODE, camera.altitudeMode);
        }
    }

    /**
     * Restores the Navigator's camera state from a Bundle.
     *
     * @param savedInstanceState The object the camera data is read from
     */
    protected void restoreNavigatorState(Bundle savedInstanceState) {
        WorldWindow wwd = getWorldWindow();
        if (wwd != null) {
            // Read the camera data
            double lat = savedInstanceState.getDouble(CAMERA_LATITUDE);
            double lon = savedInstanceState.getDouble(CAMERA_LONGITUDE);
            double alt = savedInstanceState.getDouble(CAMERA_ALTITUDE);
            double heading = savedInstanceState.getDouble(CAMERA_HEADING);
            double tilt = savedInstanceState.getDouble(CAMERA_TILT);
            double roll = savedInstanceState.getDouble(CAMERA_ROLL);
            @WorldWind.AltitudeMode int altMode = savedInstanceState.getInt(CAMERA_ALTITUDE_MODE);

            // Restore the camera state.
            Camera camera = new Camera(lat, lon, alt, altMode, heading, tilt, roll);
            wwd.getNavigator().setAsCamera(wwd.getGlobe(), camera);
        }
    }

    @Override
    public WorldWindow getWorldWindow() {
        return this.wwd;
    }
}
