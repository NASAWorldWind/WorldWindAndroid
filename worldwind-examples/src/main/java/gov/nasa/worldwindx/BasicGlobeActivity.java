/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

public class BasicGlobeActivity extends BaseActivity {

    /**
     * Allow derived classes to override the resource used in setContentView
     */
    protected int layoutResourceId = R.layout.activity_globe;

    protected WorldWindow wwd;

    public WorldWindow getWorldWindow() {
        return wwd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutResourceId);

        // Create the World Window
        this.wwd = new WorldWindow(this);

        // Add the WorldWindow to the layout
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.content_globe);
        globeLayout.addView(this.wwd);

        RelativeLayout overlayLayout = (RelativeLayout) findViewById(R.id.content_overlay);
        overlayLayout.bringToFront();

        // Setup the World Window's layers.
        this.wwd.getLayers().addLayer(new BackgroundLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        this.wwd.getLayers().addLayer(new AtmosphereLayer());
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

}

