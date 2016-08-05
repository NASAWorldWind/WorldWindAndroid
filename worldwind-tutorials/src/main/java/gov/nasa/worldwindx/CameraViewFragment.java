/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;

public class CameraViewFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow with its camera positioned at a given location and configured to point in a given
     * direction.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a view of Point Mugu airport as seen from an aircraft above Oxnard, CA.
        Position aircraft = new Position(34.2, -119.2, 3000);           // Above Oxnard CA, altitude in meters
        Position airport = new Position(34.1192744, -119.1195850, 4.0); // KNTD airport, Point Mugu CA, altitude MSL

        // Compute heading and tilt angles from aircraft to airport
        Globe globe = wwd.getGlobe();
        double heading = aircraft.greatCircleAzimuth(airport);
        double distanceRadians = aircraft.greatCircleDistance(airport);
        double distance = distanceRadians * globe.getRadiusAt(aircraft.latitude, aircraft.longitude);
        double tilt = Math.toDegrees(Math.atan(distance / aircraft.altitude));

        // Create the new camera view
        Camera camera = new Camera();
        camera.set(aircraft.latitude, aircraft.longitude, aircraft.altitude, WorldWind.ABSOLUTE, heading, tilt, 0); // No roll

        // Apply the view
        wwd.getNavigator().setAsCamera(globe, camera);

        // This works too!  Using the fluid api to manipulate the Navigator's camera:
//        wwd.getNavigator()
//            .setLatitude(aircraft.latitude)
//            .setLongitude(aircraft.longitude)
//            .setAltitude(aircraft.altitude)
//            .setHeading(heading)
//            .setTilt(tilt);

        return wwd;
    }
}
