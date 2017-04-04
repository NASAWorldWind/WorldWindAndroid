/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.ogc.Wcs100ElevationCoverage;

public class WcsElevationFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a WCS Elevation Coverage
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Specify the bounding sector - provided by the WCS
        Sector coverageSector = Sector.fromDegrees(-83.0, -180.0, 173.0, 360.0);
        // Specify the number of levels to match data resolution
        int numberOfLevels = 12;
        // Specify the version 1.0.0 WCS address
        String serviceAddress = "https://worldwind26.arc.nasa.gov/wcs";
        // Specify the coverage name
        String coverage = "aster_v2";

        // Create an elevation coverage from a version 1.0.0 WCS
        Wcs100ElevationCoverage aster = new Wcs100ElevationCoverage(coverageSector, numberOfLevels, serviceAddress, coverage);

        // Remove any existing coverages from the Globe
        wwd.getGlobe().getElevationModel().clearCoverages();

        // Add the coverage to the Globes elevation model
        wwd.getGlobe().getElevationModel().addCoverage(aster);

        // Position the camera to look at the Sangre de Cristo Mountains
        this.positionView(wwd);

        return wwd;
    }

    protected void positionView(WorldWindow wwd) {

        Position blancaPeak = new Position(37.577227, -105.485845, 4374);
        Position eye = new Position(37.5, -105.4, 5000);

        // Compute heading and distance from peak to eye
        Globe globe = wwd.getGlobe();
        double heading = eye.greatCircleAzimuth(blancaPeak);
        double distanceRadians = blancaPeak.greatCircleDistance(eye);
        double distance = distanceRadians * globe.getRadiusAt(blancaPeak.latitude, blancaPeak.longitude);

        // Compute camera settings
        double altitude = eye.altitude - blancaPeak.altitude;
        double range = Math.sqrt(altitude * altitude + distance * distance);
        double tilt = Math.toDegrees(Math.atan(distance / eye.altitude));

        // Apply the new view
        LookAt lookAt = new LookAt();
        lookAt.set(blancaPeak.latitude, blancaPeak.longitude, blancaPeak.altitude, WorldWind.ABSOLUTE, range, heading, tilt, 0 /*roll*/);
        wwd.getNavigator().setAsLookAt(globe, lookAt);
    }
}
