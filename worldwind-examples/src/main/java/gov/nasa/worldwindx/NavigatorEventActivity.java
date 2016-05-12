/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;

public class NavigatorEventActivity extends BasicGlobeActivity {

    protected TextView latView;

    protected TextView lonView;

    protected TextView altView;

    private LookAt lookAt = new LookAt();

    private Camera camera = new Camera();

    private double[] dms = new double[3];

    private long lastEventTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_navigator_event));
        setAboutBoxText("Demonstrates how to receive notification of navigator events.");

        // Enable the status bar and crosshairs (their default visibility is 'gone')
        findViewById(R.id.globe_status).setVisibility(View.VISIBLE);
        findViewById(R.id.globe_crosshairs).setVisibility(View.VISIBLE);

        // Initialize the text views used to display the Navigator state
        this.latView = (TextView) findViewById(R.id.lat_value);
        this.lonView = (TextView) findViewById(R.id.lon_value);
        this.altView = (TextView) findViewById(R.id.alt_value);


        // Create a simple Navigator Listener that logs navigator events emitted by the World Window.
        NavigatorListener listener = new NavigatorListener() {
            @Override
            public void onNavigatorEvent(WorldWindow wwd, NavigatorEvent event) {

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastEventTime;

                // Update the status overlay whenever the navigator stops moving,
                // otherwise at an arbitrary maximum refresh rate of 20 Hz.
                if (event.getAction() == WorldWind.NAVIGATOR_STOPPED || elapsedTime > 50) {
                    // Get the current navigator state
                    event.getNavigator().getAsLookAt(wwd.getGlobe(), lookAt);
                    event.getNavigator().getAsCamera(wwd.getGlobe(), camera);

                    // Update the status overlay from the navigator state
                    latView.setText(formatLatitude(lookAt.latitude));
                    lonView.setText(formatLongitude(lookAt.longitude));
                    altView.setText(formatAltitude(camera.altitude));

                    // Set the text color based on the 'moved' vs 'stopped' action
                    setTextColor(event.getAction());

                    lastEventTime = currentTime;
                }
            }
        };

        // Register the Navigator Listener with the activity's World Window.
        this.getWorldWindow().addNavigatorListener(listener);
    }

    private void setTextColor(@WorldWind.NavigatorAction int action) {
        int color = Color.WHITE;
        if (action == WorldWind.NAVIGATOR_MOVED) {
            color = Color.RED;
        } else if (action == WorldWind.NAVIGATOR_STOPPED) {
            color = Color.YELLOW;
        }
        latView.setTextColor(color);
        lonView.setTextColor(color);
        altView.setTextColor(color);
    }

    private String formatLatitude(double latitude) {
        angleToDms(latitude, dms);
        int sign = (int) Math.signum(dms[0]);
        return String.format("%2d\u00B0 %02d\' %04.1f\"%s",
            (int) dms[0],
            (int) dms[1],
            dms[2],
            (sign >= 0.0 ? "N" : "S"));
    }

    private String formatLongitude(double longitude) {
        angleToDms(longitude, dms);
        int sign = (int) Math.signum(longitude);
        return String.format("%3d\u00B0 %02d\' %04.1f\"%s",
            (int) dms[0],
            (int) dms[1],
            dms[2],
            (sign >= 0.0 ? "E" : "W"));
    }

    private String formatAltitude(double altitude) {
        return String.format("Eye: %,.0f %s",
            altitude < 100000 ? altitude : altitude / 1000,
            altitude < 100000 ? "m" : "km");
    }

    private static double[] angleToDms(double angle, double[] dms) {
        double temp = Math.abs(angle);
        int d = (int) Math.floor(temp);
        temp = (temp - d) * 60d;
        int m = (int) Math.floor(temp);
        temp = (temp - m) * 60d;
        double s = temp;
        if (s == 60) {
            m++;
            s = 0;
        } // Fix rounding errors
        if (m == 60) {
            d++;
            m = 0;
        }
        dms[0] = d;
        dms[1] = m;
        dms[2] = s;

        return dms;
    }

}
