/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.widget.RelativeLayout;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.BlueMarbleBackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.layer.BlueMarbleLayer;
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

//        // Add support for a Toolbar and set to act as the ActionBar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        // Add support for the navigation drawer full of examples
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//

        // Create the World Window and set it as the content view for this activity.
        this.wwd = new WorldWindow(this);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_globe);
        layout.addView(this.wwd);

        // Setup the World Window's layers.
        this.wwd.getLayers().addLayer(new BlueMarbleBackgroundLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        this.wwd.getLayers().addLayer(new AtmosphereLayer());

        // Configure the Blue Marble & Landsat layer to appear after zooming in.
        this.wwd.getLayers().getLayer(2).setMaxActiveAltitude(1.0e6);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        switch (item.getItemId()) {
//
//            case R.id.nav_basic_globe_activity:
//                startActivity(new Intent(getApplicationContext(), BasicGlobeActivity.class));
//                break;
//            case R.id.nav_wms_layer_activity:
//                startActivity(new Intent(getApplicationContext(), WmsLayerActivity.class));
//                break;
//            case R.id.nav_surface_image_activity:
//                startActivity(new Intent(getApplicationContext(), SurfaceImageActivity.class));
//                break;
//        }
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
}

