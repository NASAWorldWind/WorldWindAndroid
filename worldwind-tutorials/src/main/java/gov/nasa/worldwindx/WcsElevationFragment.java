/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.ElevationCoverage;
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
        Sector coverageSector = Sector.fromDegrees(25.0, -125.0, 25.0, 60.0);
        // Specify the number of levels to match data resolution
        int numberOfLevels = 12;
        // Specify the version 1.0.0 WCS address
        String serviceAddress = "https://worldwind26.arc.nasa.gov/wcs";
        // Specify the coverage name
        String coverage = "USGS-NED";

        // Create an elevation coverage from a version 1.0.0 WCS
        ElevationCoverage usgsNed = new Wcs100ElevationCoverage(coverageSector, numberOfLevels, serviceAddress, coverage);

        // Remove any existing coverages from the Globe
        wwd.getGlobe().getElevationModel().clearCoverages();

        // Add the coverage to the Globes elevation model
        wwd.getGlobe().getElevationModel().addCoverage(usgsNed);

        // Position the camera to look at Mt. Rainier
        this.positionView(wwd);

        return wwd;
    }

    protected void positionView(WorldWindow wwd) {

        Position mtRainier = new Position(46.852886, -121.760374, 4392.0);
        Position eye = new Position(46.912, -121.527, 2000.0);

        // Compute heading and distance from peak to eye
        Globe globe = wwd.getGlobe();
        double heading = eye.greatCircleAzimuth(mtRainier);
        double distanceRadians = mtRainier.greatCircleDistance(eye);
        double distance = distanceRadians * globe.getRadiusAt(mtRainier.latitude, mtRainier.longitude);

        // Compute camera settings
        double altitude = eye.altitude - mtRainier.altitude;
        double range = Math.sqrt(altitude * altitude + distance * distance);
        double tilt = Math.toDegrees(Math.atan(distance / eye.altitude));

        // Apply the new view
        Camera camera = new Camera();
        camera.set(eye.latitude, eye.longitude, eye.altitude, WorldWind.ABSOLUTE, heading, tilt, 0.0 /*roll*/);

        wwd.getNavigator().setAsCamera(globe, camera);
    }
}
