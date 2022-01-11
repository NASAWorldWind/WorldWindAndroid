/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the Location class.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log

public class PositionTest {

    static final double TOLERANCE = 1e-10;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Logger.class);
    }

    /**
     * Tests default constructor's member initialization.
     */
    @Test
    public void testConstructor_Default() {

        Position position = new Position();

        assertNotNull(position);
        assertEquals("latitude", 0.0, position.latitude, 0);
        assertEquals("longitude", 0.0, position.longitude, 0);
        assertEquals("altitude", 0.0, position.altitude, 0);
    }

    /**
     * Tests constructor from degrees member  initialization.
     */
    @Test
    public void testConstructor_Degrees() {
        double lat = 34.2; // KOXR Airport
        double lon = -119.2;
        double elev = 13.7; // 45'

        Position oxr = new Position(lat, lon, elev);

        assertNotNull(oxr);
        assertEquals("latitude", lat, oxr.latitude, 0);
        assertEquals("longitude", lon, oxr.longitude, 0);
        assertEquals("altitude", elev, oxr.altitude, 0);
    }

    /**
     * Tests the copy constructor.
     */
    @Test
    public void testConstructor_Copy() {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        double elev = 13.7; // 45'
        Position oxr = new Position(lat, lon, elev);

        Position copy = new Position(oxr);

        assertNotNull(oxr);
        assertEquals("latitude", lat, copy.latitude, 0);
        assertEquals("longitude", lon, copy.longitude, 0);
        assertEquals("altitude", elev, copy.altitude, 0);
    }

    /**
     * Ensures null argument is handled correctly.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNull() {

        new Position(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }


    /**
     * Tests the factory method using decimal degrees and meters.
     */
    @Test
    public void testFromDegrees() {
        double lat = 34.2; // KOXR Airport
        double lon = -119.2;
        double elev = 13.7; // 45'

        Position oxr = Position.fromDegrees(lat, lon, elev);

        assertNotNull(oxr);
        assertEquals("latitude", lat, oxr.latitude, 0);
        assertEquals("longitude", lon, oxr.longitude, 0);
        assertEquals("altitude", elev, oxr.altitude, 0);

    }

    /**
     * Tests the factory method using radians and meters.
     */
    @Test
    public void testFromRadians() {
        double lat = 34.2; // KOXR Airport
        double lon = -119.2;
        double elev = 13.7; // 45'
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        Position oxr = Position.fromRadians(latRad, lonRad, elev);

        assertNotNull(oxr);
        assertEquals("latitude", lat, oxr.latitude, TOLERANCE);
        assertEquals("longitude", lon, oxr.longitude, TOLERANCE);
        assertEquals("altitude", elev, oxr.altitude, 0);
    }

    /**
     * Tests equality.
     */
    @Test
    public void testEquals() {
        double lat = 34.2; // KOXR Airport
        double lon = -119.2;
        double elev = 13.7; // 45'

        Position a = new Position(lat, lon, elev);
        Position b = new Position(lat, lon, elev);

        // Assert that each member is checked for equality
        assertEquals("equality: latitude", b.latitude, a.latitude, 0);
        assertEquals("equality: longitude", b.longitude, a.longitude, 0);
        assertEquals("equality: altitude", b.altitude, a.altitude, 0);
        assertEquals("equality", a, b);
    }

    /**
     * Tests inequality.
     */
    @Test
    public void testEquals_Inequality() {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        double elev = 13.7; // 45'
        Position oxr = new Position(lat, lon, elev);

        Position a = new Position(lat, lat, elev);
        Position b = new Position(lon, lon, elev);
        Position c = new Position(lat, lon, 0);

        assertNotEquals("inequality", oxr, a);
        assertNotEquals("inequality", oxr, b);
        assertNotEquals("inequality", oxr, c);
        assertNotEquals("inequality", oxr, null);
    }

    /**
     * Ensures hash codes are unique.
     */
    @Test
    public void testHashCode() {
        Position oxr = Position.fromDegrees(34.2, -119.2, 13.7);
        Position lax = Position.fromDegrees(33.94, -118.4, 38.7);

        int oxrHash = oxr.hashCode();
        int laxHash = lax.hashCode();

        assertNotEquals("oxr hash vs lax hash", oxrHash, laxHash);
    }

    /**
     * Ensures string output contains member representations.
     */
    @Test
    public void testToString() {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        double elev = 13.7; // 45'
        Position oxr = new Position(lat, lon, elev);

        String string = oxr.toString();

        assertTrue("lat", string.contains(Double.toString(lat)));
        assertTrue("lon", string.contains(Double.toString(lon)));
        assertTrue("alt", string.contains(Double.toString(elev)));
    }


    /**
     * Test that we read back the same Location data that we set.
     */
    @Test
    public void testSet() {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        double elev = 13.7; // 45'
        Position oxr = new Position(lat, lon, elev);
        Position other = new Position();

        other.set(oxr);

        assertEquals("latitude", oxr.latitude, other.latitude, 0);
        assertEquals("longitude", oxr.longitude, other.longitude, 0);
        assertEquals("altitude", oxr.altitude, other.altitude, 0);
    }

    /**
     * Tests that we read back the same doubles we set.
     */
    @Test
    public void testSet_WithDoubles() {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        double elev = 13.7; // 45'
        Position pos = new Position();

        pos.set(lat, lon, elev);

        assertEquals("latitude", lat, pos.latitude, 0);
        assertEquals("longitude", lon, pos.longitude, 0);
        assertEquals("altitude", elev, pos.altitude, 0);
    }

    /**
     * Tests that we handled a null argument correctly.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() {
        Position position = new Position();

        position.set(null);

        fail("Expected a InvalidArgumentException.");
    }

    /**
     * Tests the great circle path interpolation. Ensures the interpolated position lies on the great circle path
     * between start and end.
     */
    @Test
    public void testInterpolateAlongPath() {
        Position lax = Position.fromDegrees(33.94, -118.4, 38.7);
        Position oxr = Position.fromDegrees(34.2, -119.2, 13.7);
        double distanceToOxr = lax.greatCircleDistance(oxr);
        double azimuthToOxr = lax.greatCircleAzimuth(oxr);
        double amount = 0.25; // percent

        Position result = lax.interpolateAlongPath(oxr, WorldWind.GREAT_CIRCLE, amount, new Position());

        double distanceToResult = lax.greatCircleDistance(result);
        Location test = lax.greatCircleLocation(azimuthToOxr, distanceToResult, new Location());
        assertEquals("interpolated distance", distanceToOxr * amount, distanceToResult, TOLERANCE);
        assertEquals("latitude", test.latitude, result.latitude, 0);
        assertEquals("longitude", test.longitude, result.longitude, 0);
    }

    /**
     * Tests the rhumbline path interpolation. Ensures the interpolated position lies on the rhumb line path between
     * start and end.
     */
    @Test
    public void testInterpolateAlongPath_Rhumbline() {
        Position lax = Position.fromDegrees(33.94, -118.4, 38.7);
        Position oxr = Position.fromDegrees(34.2, -119.2, 13.7);
        double distanceToOxr = lax.rhumbDistance(oxr);
        double azimuthToOxr = lax.rhumbAzimuth(oxr);
        double amount = 0.25; // percent

        Position result = lax.interpolateAlongPath(oxr, WorldWind.RHUMB_LINE, amount, new Position());

        double distanceToResult = lax.rhumbDistance(result);
        Location test = lax.rhumbLocation(azimuthToOxr, distanceToResult, new Location());
        assertEquals("interpolated distance", distanceToOxr * amount, distanceToResult, TOLERANCE);
        assertEquals("latitude", test.latitude, result.latitude, TOLERANCE);
        assertEquals("longitude", test.longitude, result.longitude, TOLERANCE);
    }

    /**
     * Tests the linear path interpolation. Ensures the interpolated position lies on the linear path between start and
     * end.
     */
    @Test
    public void testInterpolateAlongPath_Linear() {
        Position lax = Position.fromDegrees(33.94, -118.4, 38.7);
        Position oxr = Position.fromDegrees(34.2, -119.2, 13.7);
        double distanceToOxr = lax.linearDistance(oxr);
        double azimuthToOxr = lax.linearAzimuth(oxr);
        double amount = 0.25; // percent

        Position result = lax.interpolateAlongPath(oxr, WorldWind.LINEAR, amount, new Position());

        double distanceToResult = lax.linearDistance(result);
        Location test = lax.linearLocation(azimuthToOxr, distanceToResult, new Location());
        assertEquals("interpolated distance", distanceToOxr * amount, distanceToResult, TOLERANCE);
        assertEquals("latitude", test.latitude, result.latitude, TOLERANCE);
        assertEquals("longitude", test.longitude, result.longitude, TOLERANCE);
    }


    /**
     * Tests the handling of a null end point.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateAlongPath_NullEnd() {
        Position lax = Position.fromDegrees(33.94, -118.4, 38.7);
        double amount = 0.25; // percent

        lax.interpolateAlongPath(null, WorldWind.LINEAR, amount, new Position());

        fail("Expected a InvalidArgumentException.");
    }

    /**
     * Tests the handling of a null result.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateAlongPath_NullResult() {
        Position lax = Position.fromDegrees(33.94, -118.4, 38.7);
        Position oxr = Position.fromDegrees(34.2, -119.2, 13.7);
        double amount = 0.25; // percent

        lax.interpolateAlongPath(oxr, WorldWind.LINEAR, amount, null);

        fail("Expected a InvalidArgumentException.");
    }


}