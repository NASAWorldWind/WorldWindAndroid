/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;

public class NavigatorEventFragment extends BasicGlobeFragment {

    // UI elements
    protected TextView latView;

    protected TextView lonView;

    protected TextView altView;

    protected ImageView crosshairs;

    protected ViewGroup overlay;

    // Use pre-allocated navigator state objects to avoid per-event memory allocations
    private LookAt lookAt = new LookAt();

    private Camera camera = new Camera();

    // Track the navigation event time so the overlay refresh rate can be throttled
    private long lastEventTime;

    // Animation object used to fade the overlays
    private AnimatorSet animatorSet;

    private boolean crosshairsActive;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Let the super class (BasicGlobeFragment) create the view and the WorldWindow
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        // Initialize the UI elements that we'll update upon the navigation events
        this.crosshairs = (ImageView) rootView.findViewById(R.id.globe_crosshairs);
        this.overlay = (ViewGroup) rootView.findViewById(R.id.globe_status);
        this.crosshairs.setVisibility(View.VISIBLE);
        this.overlay.setVisibility(View.VISIBLE);
        this.latView = (TextView) rootView.findViewById(R.id.lat_value);
        this.lonView = (TextView) rootView.findViewById(R.id.lon_value);
        this.altView = (TextView) rootView.findViewById(R.id.alt_value);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(this.crosshairs, "alpha", 0f).setDuration(1500);
        fadeOut.setStartDelay((long) 500);
        this.animatorSet = new AnimatorSet();
        this.animatorSet.play(fadeOut);

        // Create a simple Navigator Listener that logs navigator events emitted by the World Window.
        NavigatorListener listener = new NavigatorListener() {
            @Override
            public void onNavigatorEvent(WorldWindow wwd, NavigatorEvent event) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastEventTime;
                int eventAction = event.getAction();
                boolean receivedUserInput = (eventAction == WorldWind.NAVIGATOR_MOVED && event.getLastInputEvent() != null);

                // Update the status overlay views whenever the navigator stops moving,
                // and also it is moving but at an (arbitrary) maximum refresh rate of 20 Hz.
                if (eventAction == WorldWind.NAVIGATOR_STOPPED || elapsedTime > 50) {

                    // Get the current navigator state to apply to the overlays
                    event.getNavigator().getAsLookAt(wwd.getGlobe(), lookAt);
                    event.getNavigator().getAsCamera(wwd.getGlobe(), camera);

                    // Update the overlays
                    updateOverlayContents(lookAt, camera);
                    updateOverlayColor(eventAction);

                    lastEventTime = currentTime;
                }

                // Show the crosshairs while the user is gesturing and fade them out after the user stops
                if (receivedUserInput) {
                    showCrosshairs();
                } else {
                    fadeCrosshairs();
                }
            }
        };

        // Register the Navigator Listener with the activity's World Window.
        this.getWorldWindow().addNavigatorListener(listener);

        return rootView;
    }

    /**
     * Makes the crosshairs visible.
     */
    protected void showCrosshairs() {
        if (this.animatorSet.isStarted()) {
            this.animatorSet.cancel();
        }
        this.crosshairs.setAlpha(1.0f);
        this.crosshairsActive = true;
    }

    /**
     * Fades the crosshairs using animation.
     */
    protected void fadeCrosshairs() {
        if (this.crosshairsActive) {
            this.crosshairsActive = false;
            if (!this.animatorSet.isStarted()) {
                this.animatorSet.start();
            }
        }
    }

    /**
     * Displays navigator state information in the status overlay views.
     *
     * @param lookAt Where the navigator is looking
     * @param camera Where the camera is positioned
     */
    protected void updateOverlayContents(LookAt lookAt, Camera camera) {
        latView.setText(formatLatitude(lookAt.latitude));
        lonView.setText(formatLongitude(lookAt.longitude));
        altView.setText(formatAltitude(camera.altitude));
    }

    /**
     * Brightens the colors of the overlay views when when user input occurs.
     *
     * @param eventAction The action associated with this navigator event
     */
    protected void updateOverlayColor(@WorldWind.NavigatorAction int eventAction) {
        int color = (eventAction == WorldWind.NAVIGATOR_STOPPED) ? 0xA0FFFF00 /*semi-transparent yellow*/ : Color.YELLOW;
        latView.setTextColor(color);
        lonView.setTextColor(color);
        altView.setTextColor(color);
    }

    protected String formatLatitude(double latitude) {
        int sign = (int) Math.signum(latitude);
        return String.format("%6.3f°%s", (latitude * sign), (sign >= 0.0 ? "N" : "S"));
    }

    protected String formatLongitude(double longitude) {
        int sign = (int) Math.signum(longitude);
        return String.format("%7.3f°%s", (longitude * sign), (sign >= 0.0 ? "E" : "W"));
    }

    protected String formatAltitude(double altitude) {
        return String.format("Eye: %,.0f %s",
            (altitude < 100000 ? altitude : altitude / 1000),
            (altitude < 100000 ? "m" : "km"));
    }
}
