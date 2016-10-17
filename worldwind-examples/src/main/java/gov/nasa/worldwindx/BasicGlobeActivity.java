/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ZoomControls;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

/**
 * Creates a simple view of a globe with touch navigation and a few layers.
 */
public class BasicGlobeActivity extends AbstractMainActivity {

    /**
     * This protected member allows derived classes to override the resource used in setContentView.
     */
    protected int layoutResourceId = R.layout.activity_globe;

    /**
     * The WorldWindow (GLSurfaceView) maintained by this activity
     */
    protected WorldWindow wwd;


    /**
     * Creates and initializes the WorldWindow and adds it to the layout.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        initializeLayers();
        initializeZoomControls();
    }

    /**
     * Adds the layers to the globe.
     */
    protected void initializeLayers() {
        this.wwd.getLayers().addLayer(new BackgroundLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        this.wwd.getLayers().addLayer(new AtmosphereLayer());
    }

    /**
     * Adds the zoom controls to the layout.
     */
    protected void initializeZoomControls() {
        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustZoom(-0.6);
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustZoom(0.6);
            }
        });
        zoomControls.setZoomSpeed(50);  // repeat every 50 ms after a long press
    }

    /**
     * Inflates the options menu and synchronizes the Zoom Controls menu item to the controls.
     *
     * @param menu The options menu
     *
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Let the base class inflate the the options menu
        boolean result = super.onCreateOptionsMenu(menu);

        if (result) {
            // Set the initial state of the zoom controls menu item to the controls
            ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
            MenuItem menuItem = menu.findItem(R.id.action_zoom_controls);
            menuItem.setChecked(zoomControls.isShown());
        }
        return result;
    }

    /**
     * Handles show/hide "zoom controls" events
     *
     * @param item The selected menu item.
     *
     * @return true if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_zoom_controls) {
            // Toggle the selection
            item.setChecked(!item.isChecked());
            ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
            if (item.isChecked()) {
                zoomControls.show();
            } else {
                zoomControls.hide();
            }
            return true;
        }
        // Let the base class handle its items
        return super.onOptionsItemSelected(item);
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
     * Gets the WorldWindow attached to this activity.
     *
     * @return The WorldWindow
     */
    @Override
    public WorldWindow getWorldWindow() {
        return this.wwd;
    }

    /**
     * Zooms the WorldWindow by the given zoom step.
     *
     * @param amount Zoom step; negative values zoom in.
     */
     protected void adjustZoom(double amount) {
        Globe globe = this.wwd.getGlobe();
        LookAt lookAt = this.wwd.getNavigator().getAsLookAt(globe, new LookAt());
        double range = lookAt.range;
        double coeff = 0.05;
        double change = coeff * amount;
        double logRange = range != 0 ? Math.log(range) : 0;
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
        // 1) Zooming is slow near the globe, and fast at great distances.
        // 2) Zooming in then immediately zooming out returns the viewer to the same range value.
        lookAt.range = Math.exp(logRange + change);

         this.wwd.getNavigator().setAsLookAt(globe, lookAt);
         this.wwd.requestRedraw();
    }
}
