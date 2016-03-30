/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BasicGlobe. Tests ensure the proper calculation of the globe's radius using WGS84 specifications,
 * and simple parameter passing to the underlying projection.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class BasicGlobeTest {

    private static final double OFFICIAL_SEMI_MAJOR_AXIS = 6378137.0;

    private static final double OFFICIAL_SEMI_MINOR_AXIS = 6356752.314245;

    private static final double OFFICIAL_EC2 = 6.69437999014E-3;

    private static final double INVERSE_FLATTENING = 298.257223563;

    private static final double TOLERANCE = 1e-6;

    /**
     * A common mocked GeographicProjection used in the tests.
     */
    @Mock
    private GeographicProjection mockedProjection;

    /**
     * Another projection used to test setProjection.
     */
    @Mock
    private GeographicProjection anotherProjection;

    /**
     * The globe used in the tests, created in setUp(), released in tearDown().
     */
    private BasicGlobe globe = null;


    @Before
    public void setUp() {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);
        // Create the globe object used by the test
        globe = new BasicGlobe(OFFICIAL_SEMI_MAJOR_AXIS, INVERSE_FLATTENING, mockedProjection);
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

        assertEquals("equatorial radius", OFFICIAL_SEMI_MAJOR_AXIS, equatorialRadius, 0);
    }

    /**
     * Ensures the polar radius matches the value derived from the globe definition.
     *
     * @throws Exception
     */
    @Test
    public void testGetPolarRadius() throws Exception {
        double polarRadius = globe.getPolarRadius();

        // WGS84 official value:  6356752.314245
        // Actual computed value: 6356752.314245179
        assertEquals("polar radius", OFFICIAL_SEMI_MINOR_AXIS, polarRadius, 1e-6);
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

            assertEquals(Double.toString(lat), radiusExpected, radiusActual, TOLERANCE);

        }
    }

    /**
     * Ensures the eccentricity squared matches the value derived from the globe definition.
     *
     * @throws Exception
     */
    @Test
    public void testGetEccentricitySquared() throws Exception {
        double eccentricitySquared = globe.getEccentricitySquared();

        // Official value:        6356752.314 245
        // Actual computed value: 6356752.314 245 179
        assertEquals("eccentricity squared", OFFICIAL_EC2, eccentricitySquared, 1e-14);
    }

    /**
     * Ensures the getter returns the proper value.
     *
     * @throws Exception
     */
    @Test
    public void testGetProjection() throws Exception {
        GeographicProjection projection = globe.getProjection();

        assertEquals(mockedProjection, projection);
    }

    /**
     * Ensure the setter writes the proper value.
     *
     * @throws Exception
     */
    @Test
    public void testSetProjection() throws Exception {

        globe.setProjection(anotherProjection);

        GeographicProjection newProjection = globe.getProjection();
        assertEquals("new projection", newProjection, anotherProjection);
        assertNotEquals("old projection", mockedProjection, anotherProjection);
    }

    /**
     * Ensures the proper passing of parameters to the projection object.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesian() throws Exception {
        double latitude = 34.2;
        double longitude = -119.2;
        double altitude = 1000; // meters
        Vec3 result = new Vec3();

        this.globe.geographicToCartesian(latitude, longitude, altitude, result);

        verify(mockedProjection).geographicToCartesian(globe, latitude, longitude, altitude, null, result);
    }

    /**
     * Ensures the proper passing of parameters to the projection object.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesianNormal() throws Exception {
        double latitude = 34.2;
        double longitude = -119.2;
        Vec3 result = new Vec3();

        this.globe.geographicToCartesianNormal(latitude, longitude, result);

        verify(mockedProjection).geographicToCartesianNormal(globe, latitude, longitude, result);
    }

    /**
     * Ensures the proper passing of parameters to the projection object.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesianTransform() throws Exception {
        double latitude = 34.2;
        double longitude = -119.2;
        double altitude = 1000; // meters
        Matrix4 result = new Matrix4();

        globe.geographicToCartesianTransform(latitude, longitude, altitude, result);

        verify(mockedProjection).geographicToCartesianTransform(globe, latitude, longitude, altitude, null, result);
    }

    /**
     * Ensures the proper passing of parameters to the projection object.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesianGrid() throws Exception {
        int stride = 5;
        int numLat = 17;
        int numLon = 33;
        int count = numLat * numLon * stride;
        double[] elevations = new double[count];
        Sector sector = new Sector();
        Vec3 referencePoint = new Vec3();
        FloatBuffer result = ByteBuffer.allocateDirect(count * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        globe.geographicToCartesianGrid(sector, numLat, numLon, elevations, referencePoint, result, stride);

        verify(mockedProjection).geographicToCartesianGrid(globe, sector, numLat, numLon, elevations,
            referencePoint, null, result, stride);
    }

    /**
     * Ensures the proper passing of parameters to the projection object.
     *
     * @throws Exception
     */
    @Test
    public void testCartesianToGeographic() throws Exception {
        double x = 11111d;
        double y = 22222d;
        double z = 33333d;
        Position result = new Position();

        globe.cartesianToGeographic(x, y, z, result);

        verify(mockedProjection).cartesianToGeographic(globe, x, y, z, null, result);
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
        double a = OFFICIAL_SEMI_MAJOR_AXIS;
        double eSquared = OFFICIAL_EC2;
        double radius = a * sqrt(pow(1 - eSquared, 2.0) * sinLatSquared + cosLatSquared);
        radius /= sqrt(1 - eSquared * sinLatSquared);
        return radius;
    }
}