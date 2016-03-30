/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This abstract activity class implements a Navigation Drawer menu shared by all the World Wind Example activities.
 */
public abstract class AbstractNavDrawerActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    static private int selectedItemId = R.id.nav_basic_globe_activity;

    private ActionBarDrawerToggle drawerToggle;

    private NavigationView navigationView;

    protected String aboutBoxTitle = "Title goes here";

    protected String aboutBoxText = "Description goes here;";

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        onCreateDrawer();
    }

    protected void onCreateDrawer() {

        // Add support for a Toolbar and set to act as the ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add support for the navigation drawer full of examples
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.drawerToggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(this.drawerToggle);
        this.drawerToggle.syncState();

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        this.navigationView.setCheckedItem(selectedItemId);
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.navigationView.setCheckedItem(selectedItemId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAboutBox();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is invoked when the About button is selected in the Options menu.
     */
    abstract protected void showAboutBox();

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Persist the selected item between Activities
        selectedItemId = item.getItemId();

        // Handle navigation view item clicks here.
        switch (selectedItemId) {

            case R.id.nav_basic_globe_activity:
                startActivity(new Intent(getApplicationContext(), BasicGlobeActivity.class));
                break;
            case R.id.nav_camera_view_activity:
                startActivity(new Intent(getApplicationContext(), CameraViewActivity.class));
                break;
            case R.id.nav_camera_control_activity:
                startActivity(new Intent(getApplicationContext(), CameraControlActivity.class));
                break;
            case R.id.nav_look_at_view_activity:
                startActivity(new Intent(getApplicationContext(), LookAtViewActivity.class));
                break;
            case R.id.nav_show_tessellation_activity:
                startActivity(new Intent(getApplicationContext(), ShowTessellationActivity.class));
                break;
            case R.id.nav_surface_image_activity:
                startActivity(new Intent(getApplicationContext(), SurfaceImageActivity.class));
                break;
            case R.id.nav_wms_layer_activity:
                startActivity(new Intent(getApplicationContext(), WmsLayerActivity.class));
                break;
            case R.id.nav_day_night_cycle_activity:
                startActivity(new Intent(getApplicationContext(), DayNightCycleActivity.class));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

