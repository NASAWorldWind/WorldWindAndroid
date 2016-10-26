/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
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
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;
import gov.nasa.worldwindx.support.LayerManager;

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
     * The Layer Manager is responsible for rendering the layer manager menu.
     */
    protected LayerManager layerManager;

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
        this.layerManager = new LayerManager(this, this.wwd);

        // Add the WorldWindow view object to the layout that was reserved for the globe.
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.wwd);

        initializeLayers();
        initializeZoomControls();
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

    public LayerManager getLayerManager() {
        return this.layerManager;
    }

    /**
     * Adds the layers to the globe.
     */
    protected void initializeLayers() {

        LayerList layers = new LayerList();
        // Default base layers
        layers.addLayer(new BackgroundLayer());
        layers.addLayer(new BlueMarbleLandsatLayer());

        //////////////////////////////////////////////////////////////
        // Start TMIS tests
        // CADRG map layer on local WMS Server
        WmsLayerConfig config = new WmsLayerConfig();
        config.serviceAddress = "http://10.0.2.7:5000/WmsServer";
        config.wmsVersion = "1.3.0";
        config.coordinateSystem = "CRS:84";
        config.layerNames = "imagery_part1-2.0.0.1-gnc.gpkg";
        double metersPerPixel = 0.00028 * 305.748;// 0.28mm pixel size * max scale denominator
        WmsLayer layer = new WmsLayer(new Sector().setFullSphere() /*bbox*/, 1000 /*meters per pixel*/, config);
        layer.setDisplayName("> TMIS - GNC");
        layer.setEnabled(false);
        layers.addLayer(layer);

        // WSMR map layer on local WMS Server
        config = new WmsLayerConfig();
        config.serviceAddress = "http://10.0.2.7:5000/WmsServer";
        config.wmsVersion = "1.3.0";
        config.coordinateSystem = "CRS:84";
        config.layerNames = "imagery_part2-2.0.0.1-wsmr.gpkg";
        Sector bbox = new Sector(32.695312, -106.171875, // SW corner
            (33.046875 - 32.695312),        // delta lat
            (-105.802312 + 106.171875));   // delta lon
        metersPerPixel = 0.00028 * 2.388657;// 0.28mm pixel size * max scale denominator
        layer = new WmsLayer(bbox, 1 /*meters per pixel*/, config);
        layer.setDisplayName("> TMIS - WSMR");
        layer.setEnabled(false);
        layers.addLayer(layer);

        // Ft Dix map layer on local WMS Server
        config = new WmsLayerConfig();
        config.serviceAddress = "http://10.0.2.7:5000/WmsServer";
        config.wmsVersion = "1.1.1";
        config.layerNames = "Ft_Dix_1.gpkg";
        bbox = new Sector(39.902, -74.531, // SW corner
            (40.2539 - 39.902),     // delta lat
            (-74.179 + 74.531));   // delta lon
        layer = new WmsLayer(bbox, 1 /*meters per pixel*/, config);
        layer.setDisplayName("> TMIS - Fort Dix");
        layer.setEnabled(false);
        layers.addLayer(layer);
        // End TMIS test
        ///////////////////////////////////////////////////////////////////

        // Atmosphere must be added last
        layers.addLayer(new AtmosphereLayer());

        this.layerManager.addAllLayers(layers);
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
     * Synchronizes the state of the Zoom Controls menu item to the controls widget. This is called right before the
     * menu is shown, every time it is shown.
     *
     * @param menu The options menu as last shown or first initialized by onCreateOptionsMenu().
     *
     * @return You must return true for the menu to be displayed; if you return false it will not be shown.
     *
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
        MenuItem menuItem = menu.findItem(R.id.action_zoom_controls);
        menuItem.setChecked(zoomControls.isShown());

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handles show/hide "Zoom Controls" and "Show Layer Manager" events.
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
        } else if (item.getItemId() == R.id.action_layer_manager) {
            this.drawerLayout.openDrawer(GravityCompat.END); /*Opens the Right Drawer*/
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
