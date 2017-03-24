/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for Globe. Tests ensure the proper calculation of the globe's radius using WGS84 specifications, and
 * simple parameter passing to the underlying projection.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class GlobeTest {

    private static final double OFFICIAL_WGS84_SEMI_MAJOR_AXIS = 6378137.0;

    private static final double OFFICIAL_WGS84_SEMI_MINOR_AXIS = 6356752.3142;

    private static final double OFFICIAL_WGS84_EC2 = 6.694379990141e-3;

    /**
     * The globe used in the tests, created in setUp(), released in tearDown().
     */
    private Globe globe = null;

    @Before
    public void setUp() {
        // Mock all the static methods in Logger
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
    public void testConstructor() {
        assertNotNull(globe);
    }

    /**
     * Ensures the equatorial radius matches the semi-major axis used to define the globe.
     *
     * @throws Exception
     */
    @Test
    public void testGetEquatorialRadius() throws Exception {
        double equatorialRadius = globe.getEquatorialRadius();

        assertEquals("equatorial radius", OFFICIAL_WGS84_SEMI_MAJOR_AXIS, equatorialRadius, 0);
    }

    /**
     * Ensures the polar radius matches the value derived from the globe definition.
     *
     * @throws Exception
     */
    @Test
    public void testGetPolarRadius() throws Exception {
        double polarRadius = globe.getPolarRadius();

        // WGS84 official value:  6356752.3142
        // Actual computed value: 6356752.314245179
        assertEquals("polar radius", OFFICIAL_WGS84_SEMI_MINOR_AXIS, polarRadius, 1.0e-4);
    }

    /**
     * Ensures the correct calculation of the ellipsoidal radius at a geographic latitude.
     *
     * @throws Exception
     */
    @Test
    public void testGetRadiusAt() throws Exception {
        // Test all whole number latitudes
        for (double lat = -90; lat <= 90; lat += 1.0) {
            double radiusExpected = computeRadiusOfEllipsoid(lat);
            double radiusActual = globe.getRadiusAt(lat, 0);

            assertEquals(Double.toString(lat), radiusExpected, radiusActual, 1.0e-8);
        }
    }

    /**
     * Ensures the eccentricity squared matches the value derived from the globe definition.
     *
     * @throws Exception
     */
    @Test
    public void testGetEccentricitySquared() throws Exception {
        double eccentricitySquared = globe.getEllipsoid().eccentricitySquared();

        // Official value:        6.694379990141e-3
        // Actual computed value: 6.6943799901413165e-3
        assertEquals("eccentricity squared", OFFICIAL_WGS84_EC2, eccentricitySquared, 1.0e-15);
    }

    ////////////////////
    // Helper Methods
    ////////////////////

    /**
     * Returns the radius of ellipsoid at the specified geographic latitude.
     *
     * @param geographicLat a geographic (geodetic) latitude.
     *
     * @return The radius in meters.
     */
    private static double computeRadiusOfEllipsoid(double geographicLat) {
        // From Radius of the Earth - Radii Used in Geodesy
        // J. Clynch, Naval Post Graduate School, 2002
        double sinLatSquared = pow(sin(toRadians(geographicLat)), 2);
        double cosLatSquared = pow(cos(toRadians(geographicLat)), 2);
        double a = OFFICIAL_WGS84_SEMI_MAJOR_AXIS;
        double eSquared = OFFICIAL_WGS84_EC2;
        double radius = a * sqrt(pow(1 - eSquared, 2.0) * sinLatSquared + cosLatSquared);
        radius /= sqrt(1 - eSquared * sinLatSquared);
        return radius;
    }
}