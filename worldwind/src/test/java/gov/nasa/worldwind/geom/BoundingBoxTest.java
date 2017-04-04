/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.ProjectionWgs84;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class BoundingBoxTest {

    /**
     * The globe used in the tests, created in setUp(), released in tearDown().
     */
    private Globe globe = null;

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
        // Create the globe object used by the test
        globe = new Globe(WorldWind.WGS84_ELLIPSOID, new ProjectionWgs84());
    }

    @After
    public void tearDown() throws Exception {
        // Release the globe object
        globe = null;
    }

    @Test
    public void testConstructor() throws Exception {
        BoundingBox bb = new BoundingBox();

        assertNotNull(bb);
    }

    @Test
    public void testSetToSector() throws Exception {
        BoundingBox boundingBox = new BoundingBox();
        double centerLat = 0;
        double centerLon = 0;
        // Create a very, very small sector.
        Sector smallSector = sectorFromCentroid(centerLat, centerLon, 0.0001, 0.0001);
        // Create a large sector.
        Sector largeSector = sectorFromCentroid(centerLat, centerLon, 1d, 1d);
        // Create a point coincident with the sectors' centroids
        Vec3 point = globe.geographicToCartesian(centerLat, centerLon, 0, new Vec3());

        // Set the bounding box to the small sector with no elevation.
        // We expect the center of the bounding box to be very close to our point and the
        // z value should be half of the min/max elevation delta.
        float minElevation = 0;
        float maxElevation = 100;
        boundingBox.setToSector(smallSector, globe, minElevation, maxElevation);

        assertEquals("small center x", point.x, boundingBox.center.x, 1e-1);
        assertEquals("small center y", point.y, boundingBox.center.y, 1e-1);
        assertEquals("small center z", point.z + maxElevation / 2, boundingBox.center.z, 1e-1);

        // Set the bounding box to the large sector with no elevation.
        // We expect the center x,y of the bounding box to be close to the point
        // whereas the z value will be less due to the curvature of the sector's surface.
        minElevation = 0;
        maxElevation = 0;
        boundingBox.setToSector(largeSector, globe, minElevation, maxElevation);

        assertEquals("large center x", point.x, boundingBox.center.x, 1e-1);
        assertEquals("large center y", point.y, boundingBox.center.y, 1e-1);
        assertEquals("large center z", point.z, boundingBox.center.z, 300);
    }

    @Ignore
    @Test
    public void testIntersectsFrustum() throws Exception {
        BoundingBox boundingBox = new BoundingBox();
        float minElevation = 0;
        float maxElevation = 1000;
        Sector sector = Sector.fromDegrees(-0.5, -0.5, 1d, 1d);
        boundingBox.setToSector(sector, globe, minElevation, maxElevation);

        // TODO: create and transform a frustum compatible with modelView
        fail("The test case is a stub.");
    }

    @Test
    public void testDistanceTo() throws Exception {
        BoundingBox boundingBox = new BoundingBox();
        double radius = globe.getEquatorialRadius();
        float minElevation = 0;
        float maxElevation = 1000;
        Sector sector = Sector.fromDegrees(-0.5, -0.5, 1d, 1d);
        boundingBox.setToSector(sector, globe, minElevation, maxElevation);
        Vec3 point = globe.geographicToCartesian(0, 0, 0, new Vec3());

        double result = boundingBox.distanceTo(point);

        assertEquals(boundingBox.center.z - radius, result, 1e-3);
    }

    /**
     * Creates Sector with a centroid set to the specified latitude and longitude.
     *
     * @param centroidLat
     * @param centroidLon
     * @param deltaLat
     * @param deltaLon
     *
     * @return
     */
    private static Sector sectorFromCentroid(double centroidLat, double centroidLon, double deltaLat, double deltaLon) {
        return Sector.fromDegrees(centroidLat - deltaLat / 2, centroidLon - deltaLon / 2, deltaLat, deltaLon);
    }
}