/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;

public class CameraViewActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle = "About the " + getResources().getText(R.string.title_camera_view);
        this.aboutBoxText = "Demonstrates how to use a Camera to view a position.\n" +
            "This example simulates a view from an aircraft above Oxnard, CA looking at the Point Mugu Naval Air Station.";

        // Create a view of Point Mugu airport as seen from an aircraft above Oxnard, CA.
        Position aircraft = new Position(34.2, -119.2, 3000);           // Above Oxnard CA, altitude in meters
        Position airport = new Position(34.1192744, -119.1195850, 4.0); // KNTD airport, Point Mugu CA, altitude MSL

        // Compute heading and tilt angles from aircraft to airport
        Globe globe = this.getWorldWindow().getGlobe();
        double heading = aircraft.greatCircleAzimuth(airport);
        double distanceRadians = aircraft.greatCircleDistance(airport);
        double distance = distanceRadians * globe.getRadiusAt(aircraft.latitude, aircraft.longitude);
        double tilt = Math.toDegrees(Math.atan(distance / aircraft.altitude));

        // Create the new camera view
        Camera camera = new Camera();
        camera.set(aircraft.latitude, aircraft.longitude, aircraft.altitude, WorldWind.ABSOLUTE, heading, tilt, 0); // No roll

        // Apply the view
        this.getWorldWindow().getNavigator().setAsCamera(globe, camera);

        // This works too!  Using the fluid api to manipulate the Navigator's camera:
//        this.getWorldWindow().getNavigator()
//            .setLatitude(aircraft.latitude)
//            .setLongitude(aircraft.longitude)
//            .setAltitude(aircraft.altitude)
//            .setHeading(heading)
//            .setTilt(tilt);
    }

}
