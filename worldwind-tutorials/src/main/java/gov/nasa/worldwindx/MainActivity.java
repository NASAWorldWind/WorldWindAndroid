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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;
import java.util.Locale;

import gov.nasa.worldwind.FrameMetrics;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Logger;

/**
 * This abstract Activity class implements a Navigation Drawer menu shared by all the World Wind Example activities.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected static final int PRINT_METRICS = 1;

    protected static final int PRINT_METRICS_DELAY = 3000;

    protected static final Date sessionTimestamp = new Date();

    protected static int selectedItemId = R.id.nav_basic_globe_activity;

    protected ActionBarDrawerToggle drawerToggle;

    protected NavigationView navigationView;

    protected boolean twoPaneView;

    protected String tutorialUrl;

    protected String aboutBoxTitle = "World Wind Tutorials";        // TODO: use a string resource, e.g., app name

    protected String aboutBoxText = "A collection of tutorials";    // TODO: make this a string resource

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
     * <p>
     * Derived classes must implement this method.
     *
     * @return The WorldWindow GLSurfaceView object
     */
    public WorldWindow getWorldWindow() {
        // TODO: Implement via Fragment Manager and findFragmentById
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateDrawer();

        if (findViewById(R.id.code_container) != null) {
            // The code container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPaneView = true;
        }

        if (!twoPaneView) {
            FloatingActionButton codeViewButton = (FloatingActionButton) findViewById(R.id.fab);
            codeViewButton.setVisibility(View.VISIBLE);    // is set to GONE in layout
            codeViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, CodeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", tutorialUrl);
                    intent.putExtra("arguments", bundle);
                    context.startActivity(intent);
                }
            });
        }
        if (savedInstanceState == null) {
            // savedInstanceState is non-null when there is fragment state
            // saved from previous configurations of this activity
            // (e.g. when rotating the screen from portrait to landscape).
            // In this case, the fragment will automatically be re-added
            // to its container so we don't need to manually add it.
            // For more information, see the Fragments API guide at:
            //
            // http://developer.android.com/guide/components/fragments.html
            //
            loadTutorial(BasicGlobeFragment.class, "file:///android_asset/basic_globe_tutorial.html", R.string.title_basic_globe);
        }
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop printing frame metrics when this activity is paused.
        this.handler.removeMessages(PRINT_METRICS);
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

        WorldWindow wwd = this.getWorldWindow();
        if (wwd == null) {
            return false;
        }
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

            case R.id.nav_basic_globe_activity:
                loadTutorial(BasicGlobeFragment.class, "file:///android_asset/basic_globe_tutorial.html", R.string.title_basic_globe);
                break;
            case R.id.nav_camera_view_activity:
                loadTutorial(CameraViewFragment.class, "file:///android_asset/camera_view_tutorial.html",  R.string.title_camera_view);
                break;
            case R.id.nav_camera_control_activity:
                loadTutorial(CameraControlFragment.class, "file:///android_asset/camera_control_tutorial.html",  R.string.title_camera_controls);
                break;
            case R.id.nav_geopackage_activity:
                loadTutorial(GeoPackageFragment.class, "file:///android_asset/geopackage_tutorial.html", R.string.title_geopackage);
                break;
            case R.id.nav_labels_activity:
                loadTutorial(LabelsFragment.class, "file:///android_asset/labels_tutorial.html", R.string.title_labels);
                break;
            case R.id.nav_look_at_view_activity:
                loadTutorial(LookAtViewFragment.class, "file:///android_asset/look_at_view_tutorial.html", R.string.title_look_at_view);
                break;
            case R.id.nav_navigator_event_activity:
                loadTutorial(NavigatorEventFragment.class, "file:///android_asset/navigator_events_tutorial.html",  R.string.title_navigator_event);
                break;
            case R.id.nav_paths_activity:
                loadTutorial(PathsFragment.class, "file:///android_asset/paths_tutorial.html", R.string.title_paths);
                break;
            case R.id.nav_placemarks_activity:
                loadTutorial(PlacemarksFragment.class, "file:///android_asset/placemarks_tutorial.html", R.string.title_placemarks);
                break;
            case R.id.nav_placemarks_picking_activity:
                loadTutorial(PlacemarksPickingFragment.class, "file:///android_asset/placemarks_picking_tutorial.html", R.string.title_placemarks_picking);
                break;
            case R.id.nav_polygons_activity:
                loadTutorial(PolygonsFragment.class, "file:///android_asset/polygons_tutorial.html", R.string.title_polygons);
                break;
            case R.id.nav_shapes_dash_and_fill:
                loadTutorial(ShapesDashAndFillFragment.class, "file:///android_asset/shapes_dash_and_fill.html", R.string.title_shapes_dash_and_fill);
                break;
            case R.id.nav_show_tessellation_activity:
                loadTutorial(ShowTessellationFragment.class, "file:///android_asset/show_tessellation_tutorial.html", R.string.title_show_tessellation);
                break;
            case R.id.nav_surface_image_activity:
                loadTutorial(SurfaceImageFragment.class, "file:///android_asset/surface_image_tutorial.html", R.string.title_surface_image);
                break;
            case R.id.nav_wms_layer_activity:
                loadTutorial(WmsLayerFragment.class, "file:///android_asset/wms_layer_tutorial.html", R.string.title_wms_layer);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void loadTutorial(Class<? extends Fragment> globeFragment, String url, int titleId) {
        try {
            this.setTitle(titleId);
            Fragment globe = globeFragment.newInstance();
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.globe_container, globe)    // replace (destroy) existing fragment (if any)
                .commit();

            if (this.twoPaneView) {
                Bundle bundle = new Bundle();
                bundle.putString("url", url);

                Fragment code = new CodeFragment();
                code.setArguments(bundle);
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.code_container, code)
                    .commit();
            } else {
                this.tutorialUrl = url;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected static long getSessionTimestamp() {
        return sessionTimestamp.getTime();
    }
}
