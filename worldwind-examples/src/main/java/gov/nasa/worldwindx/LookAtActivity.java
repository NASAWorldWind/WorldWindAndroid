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

public class LookAtActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle= "About the " + getResources().getText(R.string.title_look_at);
        this.aboutBoxText="Demonstrates how to use LookAt to \"look at\" a position.\n" +
            "This example simulates a view from an aircraft above Oxnard, CA looking at the LAX airport.";

        // Position the camera so the view is as seen from an aircraft above Oxnard airport enroute to LAX airport.
        Position aircraft = new Position(34.2, -119.2, 5000);   // OXR airport, Oxnard CA, at altitude of 5000m
        Position airport = new Position(33.94, -118.4, 34.7);   // LAX airport, Los Angeles CA, altitude MSL

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
