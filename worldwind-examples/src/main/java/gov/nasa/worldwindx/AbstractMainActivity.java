/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;
import java.util.Locale;

import gov.nasa.worldwind.FrameMetrics;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.util.Logger;

/**
 * This abstract Activity class implements a Navigation Drawer menu shared by all the World Wind Example activities.
 */
public abstract class AbstractMainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    protected final static String SESSION_TIMESTAMP = "session_timestamp";

    protected final static String CAMERA_LATITUDE = "latitude";

    protected final static String CAMERA_LONGITUDE = "longitude";

    protected final static String CAMERA_ALTITUDE = "altitude";

    protected final static String CAMERA_ALTITUDE_MODE = "altitude_mode";

    protected final static String CAMERA_HEADING = "heading";

    protected final static String CAMERA_TILT = "tilt";

    protected final static String CAMERA_ROLL = "roll";

    protected static final int PRINT_METRICS = 1;

    protected static final int PRINT_METRICS_DELAY = 3000;

    protected static final Date sessionTimestamp = new Date();

    protected static int selectedItemId = R.id.nav_general_globe_activity;

    protected ActionBarDrawerToggle drawerToggle;

    protected NavigationView navigationView;

    protected String aboutBoxTitle = "Title goes here";

    protected String aboutBoxText = "Description goes here;";

    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == PRINT_METRICS) {
                return printMetrics();
            } else {
                return false;
            }
        }
    });

    /**
     * Returns a reference to the WorldWindow.
     * <p/>
     * Derived classes must implement this method.
     *
     * @return The WorldWindow GLSurfaceView object
     */
    abstract public WorldWindow getWorldWindow();

    /**
     * This method should be called by derived classes in their onCreate method.
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        this.onCreateDrawer();
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
        // Update the menu by highlighting the last selected menu item
        this.navigationView.setCheckedItem(selectedItemId);
        // Use this Activity's Handler to periodically print the FrameMetrics.
        this.handler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
        // Restore the navigator's camera state from previously saved session data
        this.restoreNavigatorState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop printing frame metrics when this activity is paused.
        this.handler.removeMessages(PRINT_METRICS);
        // Save the navigator's camera state.
        this.saveNavigatorState();
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
     * Saves the Navigator's camera data to a SharedPreferences object.
     */
    protected void saveNavigatorState() {
        WorldWindow wwd = this.getWorldWindow();
        if (wwd != null) {
            SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            // Write an identifier to the preferences for this session;
            editor.putLong(SESSION_TIMESTAMP, getSessionTimestamp());

            // Write the camera data
            Camera camera = wwd.getNavigator().getAsCamera(wwd.getGlobe(), new Camera());
            editor.putFloat(CAMERA_LATITUDE, (float) camera.latitude);
            editor.putFloat(CAMERA_LONGITUDE, (float) camera.longitude);
            editor.putFloat(CAMERA_ALTITUDE, (float) camera.altitude);
            editor.putFloat(CAMERA_HEADING, (float) camera.heading);
            editor.putFloat(CAMERA_TILT, (float) camera.tilt);
            editor.putFloat(CAMERA_ROLL, (float) camera.roll);
            editor.putInt(CAMERA_ALTITUDE_MODE, camera.altitudeMode);

            editor.apply();
        }
    }

    /**
     * Restores the Navigator's camera state from a SharedPreferences object.
     */
    protected void restoreNavigatorState() {
        WorldWindow wwd = this.getWorldWindow();
        if (wwd != null) {
            SharedPreferences preferences = this.getPreferences(MODE_PRIVATE);

            // We only want to restore preferences from the same session.
            if (preferences.getLong(SESSION_TIMESTAMP, -1) != getSessionTimestamp()) {
                return;
            }
            // Read the camera data
            float lat = preferences.getFloat(CAMERA_LATITUDE, Float.MAX_VALUE);
            float lon = preferences.getFloat(CAMERA_LONGITUDE, Float.MAX_VALUE);
            float alt = preferences.getFloat(CAMERA_ALTITUDE, Float.MAX_VALUE);
            float heading = preferences.getFloat(CAMERA_HEADING, Float.MAX_VALUE);
            float tilt = preferences.getFloat(CAMERA_TILT, Float.MAX_VALUE);
            float roll = preferences.getFloat(CAMERA_ROLL, Float.MAX_VALUE);
            @WorldWind.AltitudeMode int altMode = preferences.getInt(CAMERA_ALTITUDE_MODE, WorldWind.ABSOLUTE);

            if (lat == Float.MAX_VALUE || lon == Float.MAX_VALUE || alt == Float.MAX_VALUE ||
                heading == Float.MAX_VALUE || tilt == Float.MAX_VALUE || roll == Float.MAX_VALUE) {
                return;
            }

            // Restore the camera state.
            Camera camera = new Camera(lat, lon, alt, altMode, heading, tilt, roll);
            wwd.getNavigator().setAsCamera(wwd.getGlobe(), camera);
        }
    }

    protected void setAboutBoxTitle(String title) {
        this.aboutBoxTitle = title;
    }

    protected void setAboutBoxText(String text) {
        this.aboutBoxText = text;
    }

    /**
     * This method is invoked when the About button is selected in the Options menu.
     */
    protected void showAboutBox() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.aboutBoxTitle);
        alertDialogBuilder
            .setMessage(this.aboutBoxText)
            .setCancelable(true)
            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                }
            });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    protected boolean printMetrics() {
        // Assemble the current system memory info.
        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        // Assemble the current World Wind frame metrics.
        FrameMetrics fm = this.getWorldWindow().getFrameMetrics();

        // Print a log message with the system memory, World Wind cache usage, and World Wind average frame time.
        Logger.log(Logger.INFO, String.format(Locale.US, "System memory %,.0f KB    Heap memory %,.0f KB    Render cache %,.0f KB    Frame time %.1f ms + %.1f ms",
            (mi.totalMem - mi.availMem) / 1024.0,
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0,
            fm.getRenderResourceCacheUsedCapacity() / 1024.0,
            fm.getRenderTimeAverage(),
            fm.getDrawTimeAverage()));

        // Reset the accumulated World Wind frame metrics.
        fm.reset();

        // Print the frame metrics again after the configured delay.
        return this.handler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
    }

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

            case R.id.nav_basic_performance_benchmark_activity:
                startActivity(new Intent(getApplicationContext(), BasicPerformanceBenchmarkActivity.class));
                break;
            case R.id.nav_basic_stress_test_activity:
                startActivity(new Intent(getApplicationContext(), BasicStressTestActivity.class));
                break;
            case R.id.nav_day_night_cycle_activity:
                startActivity(new Intent(getApplicationContext(), DayNightCycleActivity.class));
                break;
            case R.id.nav_general_globe_activity:
                startActivity(new Intent(getApplicationContext(), GeneralGlobeActivity.class));
                break;
            case R.id.nav_multi_globe_activity:
                startActivity(new Intent(getApplicationContext(), MultiGlobeActivity.class));
                break;
            case R.id.nav_paths_example:
                startActivity(new Intent(getApplicationContext(), PathsExampleActivity.class));
                break;
            case R.id.nav_paths_and_polygons_activity:
                startActivity(new Intent(getApplicationContext(), PathsPolygonsLabelsActivity.class));
                break;
            case R.id.nav_placemarks_demo_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksDemoActivity.class));
                break;
            case R.id.nav_placemarks_milstd2525_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksMilStd2525Activity.class));
                break;
            case R.id.nav_placemarks_milstd2525_demo_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksMilStd2525DemoActivity.class));
                break;
            case R.id.nav_placemarks_milstd2525_stress_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksMilStd2525StressActivity.class));
                break;
            case R.id.nav_placemarks_select_drag_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksSelectDragActivity.class));
                break;
            case R.id.nav_placemarks_stress_activity:
                startActivity(new Intent(getApplicationContext(), PlacemarksStressTestActivity.class));
                break;
            case R.id.nav_texture_stress_test_activity:
                startActivity(new Intent(getApplicationContext(), TextureStressTestActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected static long getSessionTimestamp() {
        return sessionTimestamp.getTime();
    }
}
