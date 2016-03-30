/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;

public class LookAtViewActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle = "About the " + getResources().getText(R.string.title_look_at_view);
        this.aboutBoxText = "Demonstrates how to use LookAt to view a position.\n" +
            "This example simulates a view from an aircraft above Santa Monica, CA looking at the LAX airport.";

        // Create a view of LAX airport as seen from an aircraft above Santa Monica, CA.
        Position aircraft = new Position(34.0158333, -118.4513056, 2500);   // Aircraft above Santa Monica airport, altitude in meters
        Position airport = new Position(33.9424368, -118.4081222, 38.7);    // LAX airport, Los Angeles CA, altitude MSL

        // Compute heading and distance from aircraft to airport
        Globe globe = this.getWorldWindow().getGlobe();
        double heading = aircraft.greatCircleAzimuth(airport);
        double distanceRadians = aircraft.greatCircleDistance(airport);
        double distance = distanceRadians * globe.getRadiusAt(aircraft.latitude, aircraft.longitude);

        // Compute camera settings
        double altitude = aircraft.altitude - airport.altitude;
        double range = Math.sqrt(altitude * altitude + distance * distance);
        double tilt = Math.toDegrees(Math.atan(distance / aircraft.altitude));

        // Apply the new view
        LookAt lookAt = new LookAt();
        lookAt.set(airport.latitude, airport.longitude, airport.altitude, WorldWind.ABSOLUTE, range, heading, tilt, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(globe, lookAt);
    }

}
