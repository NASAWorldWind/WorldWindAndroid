/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for the Location class.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log
public class LocationTest {

    static final double THETA = 34.2;    // arbitrary latitude, using KOXR airport

    static final double PHI = -119.2;    // arbitrarylongitude, using KOXR airport

    static final double TOLERANCE = 1e-10;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Logger.class);
    }

    /**
     * Tests default constructor's member initialization.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Default() throws Exception {

        Location location = new Location();

        assertNotNull(location);
        assertEquals("latitude", 0.0, location.latitude, 0);
        assertEquals("longitude", 0.0, location.longitude, 0);
    }

    /**
     * Tests constructor from degrees member  initialization.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Degrees() throws Exception {

        Location location = new Location(THETA, PHI);

        assertNotNull(location);
        assertEquals("latitude", THETA, location.latitude, 0);
        assertEquals("longitude", PHI, location.longitude, 0);
    }

    /**
     * Tests the copy constructor.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Copy() throws Exception {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        Location oxr = new Location(lat, lon);

        Location copy = new Location(oxr);

        assertNotNull(oxr);
        assertEquals("latitude", lat, copy.latitude, 0);
        assertEquals("longitude", lon, copy.longitude, 0);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNull() throws Exception {

        new Location(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Tests factory method's member initialization from degrees.
     *
     * @throws Exception
     */
    @Test
    public void testFromDegrees() throws Exception {

        Location location = Location.fromDegrees(THETA, PHI);

        assertEquals("latitude", THETA, location.latitude, Double.MIN_VALUE);
        assertEquals("longitude", PHI, location.longitude, Double.MIN_VALUE);
    }

    /**
     * Test factory method's member initialization from radians
     *
     * @throws Exception
     */
    @Test
    public void testFromRadians() throws Exception {

        Location location = Location.fromRadians(THETA, PHI);

        assertEquals("latitude", Math.toDegrees(THETA), location.latitude, Double.MIN_VALUE);
        assertEquals("longitude", Math.toDegrees(PHI), location.longitude, Double.MIN_VALUE);
    }

    /**
     * Ensures normalizeLatitude returns a correct value within the range of +/- 90 degrees. A typical use case is to
     * normalize the result of a latitude used in an arithmetic function.
     *
     * @throws Exception
     */
    @Test
    public void testNormalizeLatitude() throws Exception {
        // Orbit the globe, starting at the equator and move north
        // testing the sign at the hemisphere boundaries.
        assertEquals("zero", 0.0, Location.normalizeLatitude(0.0), 0);
        assertEquals("1", 1.0, Location.normalizeLatitude(1.0), 0);
        // Test at the north pole
        assertEquals("89", 89.0, Location.normalizeLatitude(89.0), 0);
        assertEquals("90", 90.0, Location.normalizeLatitude(90.0), 0);
        assertEquals("91", 89.0, Location.normalizeLatitude(91.0), 0);

        // Test at the equator, continue moving south
        assertEquals("179", 1.0, Location.normalizeLatitude(179.0), 0);
        assertEquals("180", 0.0, Location.normalizeLatitude(180.0), 0);
        assertEquals("181", -1.0, Location.normalizeLatitude(181.0), 0);

        // Test at the south pole
        assertEquals("269", -89.0, Location.normalizeLatitude(269.0), 0);
        assertEquals("270", -90.0, Location.normalizeLatitude(270.0), 0);
        assertEquals("271", -89.0, Location.normalizeLatitude(271.0), 0);

        // Test at the prime meridian
        assertEquals("359", -1.0, Location.normalizeLatitude(359.0), 0);
        assertEquals("360", 0.0, Location.normalizeLatitude(360.0), 0);
        assertEquals("361", 1.0, Location.normalizeLatitude(361.0), 0);
        assertEquals("721", 1.0, Location.normalizeLatitude(721.0), 0);

        // Test negative values
        assertEquals("-1", -1.0, Location.normalizeLatitude(-1.0), 0);
        assertEquals("-89", -89.0, Location.normalizeLatitude(-89.0), 0);
        assertEquals("-90", -90.0, Location.normalizeLatitude(-90.0), 0);
        assertEquals("-91", -89.0, Location.normalizeLatitude(-91.0), 0);
        assertEquals("-179", -1.0, Location.normalizeLatitude(-179.0), 0);
        assertEquals("-180", 0.0, Location.normalizeLatitude(-180.0), 0);
        assertEquals("-181", 1.0, Location.normalizeLatitude(-181.0), 0);
        assertEquals("-269", 89.0, Location.normalizeLatitude(-269.0), 0);
        assertEquals("-270", 90.0, Location.normalizeLatitude(-270.0), 0);
        assertEquals("-271", 89.0, Location.normalizeLatitude(-271.0), 0);
        assertEquals("-359", 1.0, Location.normalizeLatitude(-359.0), 0);
        assertEquals("-360", 0.0, Location.normalizeLatitude(-360.0), 0);
        assertEquals("-361", -1.0, Location.normalizeLatitude(-361.0), 0);
        assertEquals("-719", 1.0, Location.normalizeLatitude(-719.0), 0);
        assertEquals("-721", -1.0, Location.normalizeLatitude(-721.0), 0);

        // NaN is propagated.
        assertTrue("NaN", Double.isNaN(Location.normalizeLatitude(Double.NaN)));
    }

    /**
     * Ensures normalizeLongitude returns a correct value within the range of +/- 180 degrees. A typical use case is to
     * normalize the result of a longitude used in an arithmetic function.
     *
     * @throws Exception
     */
    @Test
    public void testNormalizeLongitude() throws Exception {
        // Test "normal" data
        assertEquals("zero", 0.0, Location.normalizeLongitude(0.0), 0);
        assertEquals("270", -90.0, Location.normalizeLongitude(270.0), 0);
        assertEquals("-270", 90.0, Location.normalizeLongitude(-270.0), 0);

        // Test int'l date line boundaries
        assertEquals("179", 179.0, Location.normalizeLongitude(179.0), 0);
        assertEquals("180", 180.0, Location.normalizeLongitude(180.0), 0);
        assertEquals("181", -179.0, Location.normalizeLongitude(181.0), 0);

        // Test prime meridian boundaries
        assertEquals("1", 1.0, Location.normalizeLongitude(1.0), 0);
        assertEquals("-1", -1.0, Location.normalizeLongitude(-1.0), 0);
        assertEquals("359", -1.0, Location.normalizeLongitude(359.0), 0);
        assertEquals("360", 0.0, Location.normalizeLongitude(360.0), 0);
        assertEquals("361", 1.0, Location.normalizeLongitude(361.0), 0);
        assertEquals("719", -1.0, Location.normalizeLongitude(719.0), 0);
        assertEquals("720", 0.0, Location.normalizeLongitude(720.0), 0);
        assertEquals("721", 1.0, Location.normalizeLongitude(721.0), 0);

        // Assert negative -180 retains sign
        assertEquals("-180", -180.0, Location.normalizeLongitude(-180.0), 0);

        // Propagate NaNs
        assertTrue("NaN", Double.isNaN(Location.normalizeLongitude(Double.NaN)));
    }

    /**
     * Ensures clampLatitude clamps to +/-90 degrees.
     *
     * @throws Exception
     */
    @Test
    public void testClampLatitude() throws Exception {
        // Test "normal" data
        assertEquals("0", 0.0, Location.clampLatitude(0.0), 0);
        assertEquals("THETA", THETA, Location.clampLatitude(THETA), 0);
        assertEquals("-THETA", -THETA, Location.clampLatitude(-THETA), 0);

        // Test boundaries
        assertEquals("90", 90.0, Location.clampLatitude(90.0), 0);
        assertEquals("-90", -90.0, Location.clampLatitude(-90.0), 0);

        // Test clamping
        assertEquals("91", 90.0, Location.clampLatitude(91.0), 0);
        assertEquals("-91", -90.0, Location.clampLatitude(-91.0), 0);
    }

    /**
     * Ensures clampLatitude clamps to +/-180 degrees.
     *
     * @throws Exception
     */
    @Test
    public void testClampLongitude() throws Exception {
        // Test "normal" data
        assertEquals("0", 0.0, Location.clampLongitude(0.0), 0);
        assertEquals("PHI", PHI, Location.clampLongitude(PHI), 0);
        assertEquals("-PHI", -PHI, Location.clampLongitude(-PHI), 0);

        // Test boundaries
        assertEquals("180", 180.0, Location.clampLongitude(180.0), 0);
        assertEquals("-180", -180.0, Location.clampLongitude(-180.0), 0);

        // Test clamping
        assertEquals("181", 180.0, Location.clampLongitude(181.0), 0);
        assertEquals("-181", -180.0, Location.clampLongitude(-181.0), 0);
    }

    /**
     * Tests equality.
     *
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {
        double lat = 34.2;
        double lon = -119.2;

        Location a = new Location(lat, lon);
        Location b = new Location(lat, lon);

        // Assert that each member is checked for equality
        assertEquals("equality: latitude", b.latitude, a.latitude, 0);
        assertEquals("equality: longitude", b.longitude, a.longitude, 0);
        assertEquals("equality", a, a); // equality with self
        assertEquals("equality", a, b);
    }

    /**
     * Tests inequality.
     *
     * @throws Exception
     */
    @Test
    public void testEquals_Inequality() throws Exception {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        Location a = new Location(lat, lon);
        Location b = new Location(lat, lat);
        Location c = new Location(lon, lon);

        assertNotEquals("inequality", a, b);
        assertNotEquals("inequality", a, c);
        assertNotEquals("inequality", a, null);
    }

    /**
     * Ensures hash codes are unique.
     *
     * @throws Exception
     */
    @Test
    public void testHashCode() throws Exception {
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location jfk = Location.fromRadians(0.709185, -1.287762);

        int laxHash = lax.hashCode();
        int jfkHash = jfk.hashCode();

        assertNotEquals("jfk hash vs lax hash", jfkHash, laxHash);
    }

    /**
     * Ensures string output contains member representations.
     *
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        // KOXR Airport
        double lat = 34.2;
        double lon = -119.2;
        Location oxr = new Location(lat, lon);

        String string = oxr.toString();

        assertTrue("lat", string.contains(Double.toString(lat)));
        assertTrue("lon", string.contains(Double.toString(lon)));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLocationsCrossAntimeridian() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, 165d));
        locations.add(new Location(0d, -165d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertTrue("expected to cross", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_Antipodal() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, -90d));
        locations.add(new Location(0d, 90d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("antipodal", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_AlmostAntipodal_DoesCross() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, -90.0000001d));
        locations.add(new Location(0d, 90d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertTrue("nearly antipodal", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_AlmostAntipodal_DoesNotCross() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, -89.9999999d));
        locations.add(new Location(0d, 90d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("nearly antipodal", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OnAntimeridian_SameSideWest() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(90d, -180d));
        locations.add(new Location(-90d, -180d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("coincident with antimerdian, west side", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OnAntimeridian_SameSideEast() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(90d, 180d));
        locations.add(new Location(-90d, 180d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("coincident with antimerdian, east side", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OnAntimeridian_OppositeSides() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, -180d));
        locations.add(new Location(0d, 180d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("coincident with antimerdian, opposite sides", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OutsideNormalRange() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, -181d));
        locations.add(new Location(0d, 181d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertTrue("181(-179) to -181(179) expected to cross", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OutsideNormalRangeWest() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, -179d));
        locations.add(new Location(0d, -181d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertTrue("-179 to -181(179) expected to cross", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OutsideNormalRangeEast() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, 179d));
        locations.add(new Location(0d, 181d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertTrue("179 to 181(-179) expected to cross", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_NaN() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(Double.NaN, Double.NaN));
        locations.add(new Location(0d, -165d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("NaN", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_OneLocation() throws Exception {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(0d, 165d));

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("list of one location is not expected to cross", isCrossed);
    }

    @Test
    public void testLocationsCrossAntimeridian_NoLocations() throws Exception {
        List<Location> locations = new ArrayList<>();

        boolean isCrossed = Location.locationsCrossAntimeridian(locations);

        assertFalse("empty list of locations is not expected to cross", isCrossed);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLocationsCrossAntimeridian_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Location.locationsCrossAntimeridian(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures empty list argument is handled correctly.
     *
     * @throws Exception
     */
    @Test
    public void testLocationsCrossAntimeridian_EmptyList() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        boolean isCrossed = Location.locationsCrossAntimeridian(new ArrayList<Location>());

        assertFalse("empty list", isCrossed);
    }

    /**
     * Tests that we read back the same doubles we set.
     *
     * @throws Exception
     */
    @Test
    public void testSet_WithDoubles() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        Location location = new Location();

        location.set(lat, lon);

        assertEquals("latitude", lat, location.latitude, 0);
        assertEquals("longitude", lon, location.longitude, 0);
    }

    /**
     * Test that we read back the same Location data that we set.
     *
     * @throws Exception
     */
    @Test
    public void testSet() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        Location oxr = new Location(lat, lon);
        Location location = new Location();

        location.set(oxr);

        assertEquals("latitude", oxr.latitude, location.latitude, 0);
        assertEquals("longitude", oxr.longitude, location.longitude, 0);
    }

    /**
     * Tests that we handled a null argument correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        Location location = new Location();

        location.set(null);

        fail("Expected a InvalidArgumentException.");
    }

    /**
     * Tests the great circle path interpolation. Ensures the interpolated location lies on the great circle path
     * between start and end.
     *
     * @throws Exception
     */
    @Test
    public void testInterpolateAlongPath() throws Exception {
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location jfk = Location.fromRadians(0.709185, -1.287762);
        double distanceToJfk = lax.greatCircleDistance(jfk);
        double azimuthToJfk = lax.greatCircleAzimuth(jfk);
        double amount = 0.25; // percent

        Location result = lax.interpolateAlongPath(jfk, WorldWind.GREAT_CIRCLE, amount, new Location());

        double distanceToResult = lax.greatCircleDistance(result);
        Location test = lax.greatCircleLocation(azimuthToJfk, distanceToResult, new Location());
        assertEquals("interpolated distance", distanceToJfk * amount, distanceToResult, TOLERANCE);
        assertEquals("latitude", test.latitude, result.latitude, 0);
        assertEquals("longitude", test.longitude, result.longitude, 0);
    }

    /**
     * Tests the rhumbline path interpolation. Ensures the interpolated location lies on the rhumb line path between
     * start and end.
     *
     * @throws Exception
     */
    @Test
    public void testInterpolateAlongPath_Rhumbline() throws Exception {
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location jfk = Location.fromRadians(0.709185, -1.287762);
        double distanceToJfk = lax.rhumbDistance(jfk);
        double azimuthToJfk = lax.rhumbAzimuth(jfk);
        double amount = 0.25; // percent

        Location result = lax.interpolateAlongPath(jfk, WorldWind.RHUMB_LINE, amount, new Location());

        double distanceToResult = lax.rhumbDistance(result);
        Location test = lax.rhumbLocation(azimuthToJfk, distanceToResult, new Location());
        assertEquals("interpolated distance", distanceToJfk * amount, distanceToResult, TOLERANCE);
        assertEquals("latitude", test.latitude, result.latitude, 0);
        assertEquals("longitude", test.longitude, result.longitude, 0);
    }

    /**
     * Tests the linear path interpolation. Ensures the interpolated location lies on the linear path between start and
     * end.
     *
     * @throws Exception
     */
    @Test
    public void testInterpolateAlongPath_Linear() throws Exception {
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location oxr = Location.fromDegrees(34.2, -119.2);
        double distanceToOxr = lax.linearDistance(oxr);
        double azimuthToOxr = lax.linearAzimuth(oxr);
        double amount = 0.25; // percent

        Location result = lax.interpolateAlongPath(oxr, WorldWind.LINEAR, amount, new Location());

        double distanceToResult = lax.linearDistance(result);
        Location test = lax.linearLocation(azimuthToOxr, distanceToResult, new Location());
        assertEquals("interpolated distance", distanceToOxr * amount, distanceToResult, TOLERANCE);
        assertEquals("latitude", test.latitude, result.latitude, 0);
        assertEquals("longitude", test.longitude, result.longitude, 0);
    }

    /**
     * Tests the path interpolation using coincident start and end points.
     *
     * @throws Exception
     */
    @Test
    public void testInterpolateAlongPath_Coincident() throws Exception {
        Location start = Location.fromDegrees(34.2, -119.2);
        Location end = new Location(start);
        double amount = 0.25; // percent

        Location result = start.interpolateAlongPath(end, WorldWind.LINEAR, amount, new Location());

        assertEquals(result, end);
    }

    /**
     * Tests that we handle a null argument correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateAlongPath_NullEnd() throws Exception {
        Location location = new Location();

        location.interpolateAlongPath(null, WorldWind.GREAT_CIRCLE, 0.25, new Location());

        fail("Expected a InvalidArgumentException.");
    }

    /**
     * Tests that we handled a null argument correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInterpolateAlongPath_NullResult() throws Exception {
        Location location = new Location();

        location.interpolateAlongPath(location, WorldWind.GREAT_CIRCLE, 0.25, null);

        fail("Expected a InvalidArgumentException.");
    }

    /**
     * Ensures azimuth to north pole is 90.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleAzimuth_North() throws Exception {
        Location origin = new Location();
        Location northPole = new Location(90, 0);

        double azimuth = origin.greatCircleAzimuth(northPole);

        assertEquals("north to pole", Location.normalizeLongitude(0), azimuth, TOLERANCE);
    }

    /**
     * Ensures azimuth to south pole is -90.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleAzimuth_South() throws Exception {
        Location origin = new Location();
        Location southPole = new Location(-90, 0);

        double azimuth = origin.greatCircleAzimuth(southPole);

        assertEquals("south to pole", Location.normalizeLongitude(180), azimuth, TOLERANCE);
    }

    /**
     * Ensures eastward azimuth to dateline is 180.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleAzimuth_East() throws Exception {
        Location origin = new Location();
        Location east = new Location(0, 180);

        double azimuth = origin.greatCircleAzimuth(east);

        assertEquals("east to dateline", 90, azimuth, TOLERANCE);
    }

    /**
     * Ensures westward azimuth to dateline is -180.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleAzimuth_West() throws Exception {
        Location origin = new Location();
        Location west = new Location(0, -180);

        double azimuth = origin.greatCircleAzimuth(west);

        assertEquals("west to dateline", Location.normalizeLongitude(270), azimuth, TOLERANCE);
    }

    /**
     * Ensures azimuth is NaN with NaN arguments.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testGreatCircleAzimuth_WithNaN() throws Exception {
        Location origin = new Location(THETA, PHI);
        Location nanLat = new Location(Double.NaN, PHI);
        Location nanBoth = new Location(Double.NaN, Double.NaN);

        // White box testing of conditional with equal longitudes
        assertTrue("expecting NaN with equal longitudes", Double.isNaN(origin.greatCircleAzimuth(nanLat)));

        assertTrue("expecting NaN", Double.isNaN(origin.greatCircleAzimuth(nanBoth)));
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGreatCircleAzimuth_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);
        Location begin = new Location(34.2, -119.2); // KOXR

        begin.greatCircleAzimuth(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures correct distance to known location.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleDistance() throws Exception {
        Location begin = Location.fromDegrees(90.0, 45.0);
        Location end = Location.fromDegrees(36.0, 180.0);

        double distance = Math.toDegrees(begin.greatCircleDistance(end));

        assertEquals("known spherical distance", 54.0, distance, TOLERANCE);
    }

    /**
     * Ensures distance from prime meridian to dateline is +/- 180.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleDistance_AlongEquator() throws Exception {
        // Assert accurate max distances along the equator
        Location origin = new Location();
        double eastToDateLine = origin.greatCircleDistance(new Location(0, 180));
        assertEquals("prime meridian to dateline east", Math.toRadians(180), eastToDateLine, TOLERANCE);

        double westToDateLine = origin.greatCircleDistance(new Location(0, -180));
        assertEquals("prime meridian to dateline west ", Math.toRadians(180), westToDateLine, TOLERANCE);

        Location west = new Location(0, -22.5);
        double sideToSide = west.greatCircleDistance(new Location(0, 22.5));
        assertEquals("22.5 east to 22.5 west", Math.toRadians(45), sideToSide, TOLERANCE);
    }

    /**
     * Ensures distance correct distance across prime meridian.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleDistance_AcrossMeridian() throws Exception {
        Location west = new Location(0, -22.5);
        Location east = new Location(0, 22.5);

        double sideToSide = west.greatCircleDistance(east);

        assertEquals("22.5 east to 22.5 west", Math.toRadians(45), sideToSide, TOLERANCE);
    }

    /**
     * Ensures distance correct distance across prime meridian.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleDistance_AcrossDateline() throws Exception {
        Location west = new Location(0, -157.5);
        Location east = new Location(0, 157.5);

        double sideToSide = west.greatCircleDistance(east);

        assertEquals("157.5 east to 157.5 west", Math.toRadians(45), sideToSide, TOLERANCE);
    }

    /**
     * Ensures distance from equator to poles is +/- 90.
     *
     * @throws Exception
     */
    @Test
    public void testGreatCircleDistance_AlongMeridians() throws Exception {
        // Assert accurate max distances along lines of longitude
        Location origin = new Location();

        // Equator to North pole
        double northToPole = origin.greatCircleDistance(new Location(90, 0));
        assertEquals("equator to north pole", Math.toRadians(90), northToPole, TOLERANCE);

        // Equator to South pole
        double southToPole = origin.greatCircleDistance(new Location(-90, 0));
        assertEquals("equator to south pole", Math.toRadians(90), southToPole, TOLERANCE);

        // South pole to North Pole
        Location southPole = new Location(-90, 0);
        Location northPole = new Location(90, 0);
        double poleToPole = southPole.greatCircleDistance(northPole);
        assertEquals("south pole to north pole", Math.toRadians(180), poleToPole, TOLERANCE);

        Location south = new Location(-22.5, 0);
        Location north = new Location(22.5, 0);
        double southToNorth = south.greatCircleDistance(north);
        assertEquals("22.5 deg south to 22.5 north", Math.toRadians(45), southToNorth, TOLERANCE);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGreatCircleDistance_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);
        Location begin = new Location(34.2, -119.2); // KOXR

        begin.greatCircleDistance(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures distance is NaN when NaN members are used.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testGreatCircleDistance_WithNaN() throws Exception {
        Location location = new Location(Double.NaN, Double.NaN);

        double nanDistance = location.greatCircleDistance(new Location(34.2, -119.2));

        assertTrue("expecting NaN", Double.isNaN(nanDistance));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGreatCircleLocation_NorthPole() throws Exception {
        // Trivial tests along prime meridian
        Location origin = new Location();
        Location result = new Location();

        origin.greatCircleLocation(0, Math.toRadians(90), result);

        assertEquals("north pole latitude", 90, result.latitude, Double.MIN_VALUE);
    }

    @Test
    public void testGreatCircleLocation_SouthPole() throws Exception {
        // Trivial tests along prime meridian
        Location origin = new Location();
        Location result = new Location();

        origin.greatCircleLocation(180, Math.toRadians(90), result);

        assertEquals("south pole latitude", -90, result.latitude, Double.MIN_VALUE);
    }

    /**
     * Ensures the correct azimuth to a known location.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbAzimuth() throws Exception {
        // From Ed Williams Aviation Formulary:
        //  LAX is 33deg 57min N, 118deg 24min W (0.592539, -2.066470),
        //  JFK is 40deg 38min N,  73deg 47min W (0.709185, -1.287762),
        //  LAX to JFK rhumb line course of 79.3 degrees (1.384464 radians)
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location jfk = Location.fromRadians(0.709185, -1.287762);
        final double courseRadians = 1.384464;

        double azimuth = lax.rhumbAzimuth(jfk);

        assertEquals("lax to jfk", courseRadians, Math.toRadians(azimuth), 1e-6);
    }

    /**
     * Ensures the correct azimuth along the equator.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbAzimuth_AlongEquator() throws Exception {
        Location origin = new Location(0, 0);
        Location east = new Location(0, 1);
        Location west = new Location(0, -1);

        double azimuthEast = origin.rhumbAzimuth(east);
        double azimuthWest = origin.rhumbAzimuth(west);

        assertEquals("expecting 90", 90d, azimuthEast, 0);
        assertEquals("expecting -90", -90d, azimuthWest, 0);
    }

    /**
     * Ensures the correct azimuth along a meridian.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbAzimuth_AlongMeridian() throws Exception {
        Location begin = new Location(0, 0);
        Location north = new Location(1, 0);
        Location south = new Location(-1, 0);

        double azimuthNorth = begin.rhumbAzimuth(north);
        double azimuthSouth = begin.rhumbAzimuth(south);

        assertEquals("expecting 0", 0d, azimuthNorth, 0);
        assertEquals("expecting 180", 180d, azimuthSouth, 0);
    }

    /**
     * Ensures the correct azimuth from poles.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbAzimuth_FromPoles() throws Exception {
        Location northPole = new Location(90, 0);
        Location southPole = new Location(-90, 0);
        Location end = new Location(0, 0);

        double azimuthNorth = southPole.rhumbAzimuth(end);
        double azimuthSouth = northPole.rhumbAzimuth(end);

        assertEquals("expecting 0", 0d, azimuthNorth, 0);
        assertEquals("expecting 180", 180d, azimuthSouth, 0);
    }

    /**
     * Ensures the correct azimuth (shortest distance) across the +/-180 meridian.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbAzimuth_AcrossDateline() throws Exception {
        Location end = new Location(45d, 165d);
        Location begin = new Location(45d, -165d);

        double azimuth = begin.rhumbAzimuth(end);

        // Expecting an east course from +165 to -165
        assertEquals("expecting -90", -90d, azimuth, 0);
    }

    /**
     * Ensures a zero azimuth for coincident locations.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbAzimuth_CoincidentLocations() throws Exception {
        Location begin = new Location(34.2, -119.2);  // KOXR
        Location end = new Location(34.2, -119.2);

        double azimuth = begin.rhumbAzimuth(end);

        assertEquals("expecting zero", 0d, azimuth, 0);
    }

    /**
     * Ensures azimuth is NaN when NaN members are used.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testRhumbAzimuth_WithNaN() throws Exception {
        Location begin = new Location(Double.NaN, Double.NaN);
        Location end = new Location(34.2, -119.2);  // KOXR

        double azimuth = begin.rhumbAzimuth(end);

        assertTrue("expecting NaN", Double.isNaN(azimuth));
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRhumbAzimuth_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);
        Location begin = new Location(34.2, -119.2); // KOXR

        begin.rhumbAzimuth(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures correct distance between known locations.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbDistance() throws Exception {
        // From Ed Williams Aviation Formulary:
        //  LAX is 33deg 57min N, 118deg 24min W (0.592539, -2.066470),
        //  JFK is 40deg 38min N,  73deg 47min W (0.709185, -1.287762),
        //  LAX to JFK rhumbline course of 79.3 degrees (1.384464 radians)
        //  LAX to JFK rhumbline distance of 2164.6nm (0.629650 radians)
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location jfk = Location.fromRadians(0.709185, -1.287762);
        final double distanceFromLaxToJfk = 0.629650;

        double distance = lax.rhumbDistance(jfk);   // radians

        assertEquals("lax to jfk", distanceFromLaxToJfk, distance, 1e-6);
    }

    /**
     * Ensures correct distance across the +/- 180 meridian.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbDistance_AcrossDateline() throws Exception {
        Location end = new Location(0d, 165d);
        Location begin = new Location(0d, -165d);

        double distance = begin.rhumbDistance(end);

        assertEquals("expecting 30 degrees", 30d, Math.toDegrees(distance), TOLERANCE);
    }

    /**
     * Ensures distance is NaN when NaN members are used.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testRhumbDistance_WithNaN() throws Exception {
        Location begin = new Location(Double.NaN, Double.NaN);
        Location end = new Location(34.2, -119.2);  // KOXR

        double distance = begin.rhumbDistance(end);

        assertTrue("expecting NaN", Double.isNaN(distance));
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRhumbDistance_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);
        Location begin = new Location(34.2, -119.2); // KOXR

        begin.rhumbDistance(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures a zero distance for coincident locations.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbDistance_CoincidentLocations() throws Exception {
        Location begin = new Location(34.2, -119.2);  // KOXR
        Location end = new Location(34.2, -119.2);

        double distance = begin.rhumbDistance(end);

        assertEquals("expecting zero", 0d, distance, 0);
    }

    /**
     * Ensures the correct location using a known azimuth and distance.
     *
     * @throws Exception
     */
    @Test
    public void testRhumbLocation() throws Exception {
        // From Ed Williams Aviation Formulary:
        //  LAX is 33deg 57min N, 118deg 24min W (0.592539, -2.066470),
        //  JFK is 40deg 38min N,  73deg 47min W (0.709185, -1.287762),
        //  LAX to JFK rhumbline course of 79.3 degrees (1.384464 radians)
        //  LAX to JFK rhumbline distance of 2164.6nm (0.629650 radians)
        Location lax = Location.fromRadians(0.592539, -2.066470);
        Location jfk = Location.fromRadians(0.709185, -1.287762);
        final double distanceFromLaxToJfk = 0.6296498957149533;
        final double course = 79.32398087460811;

        Location location = new Location();
        lax.rhumbLocation(course, distanceFromLaxToJfk, location);

        assertEquals("jfk latitude", jfk.latitude, location.latitude, TOLERANCE);
        assertEquals("jfk longitude", jfk.longitude, location.longitude, TOLERANCE);
    }

    /**
     * Ensures the proper handling of NaN values.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testRhumbLocation_WithNaN() throws Exception {
        Location begin1 = Location.fromRadians(0.592539, -2.066470); // LAX
        Location begin2 = new Location(Double.NaN, Double.NaN);
        final double distance = 0.6296498957149533; // LAX to JFK
        final double course = 79.32398087460811;    // LAX to JFK

        Location end1 = new Location();
        Location end2 = new Location();
        Location end3 = new Location();
        begin1.rhumbLocation(Double.NaN, distance, end1);
        begin1.rhumbLocation(course, Double.NaN, end2);
        begin2.rhumbLocation(course, distance, end3);

        assertTrue("expecting NaN latitude from NaN course", Double.isNaN(end1.latitude));
        assertTrue("expecting NaN longitude from NaN course", Double.isNaN(end1.longitude));
        assertTrue("expecting NaN latitude from NaN distance", Double.isNaN(end2.latitude));
        assertTrue("expecting NaN longitude from NaN distance", Double.isNaN(end2.longitude));
        assertTrue("expecting NaN latitude from NaN origin latitude", Double.isNaN(end3.latitude));
        assertTrue("expecting NaN longitude from NaN origin latitude", Double.isNaN(end3.longitude));
    }

    /**
     * Tests the linear azimuth (flat-earth approximate) using a well known right triangle.
     *
     * @throws Exception
     */
    @Test
    public void testLinearAzimuth() throws Exception {
        // Create a 30-60-90 right triangle with a ratio of 1:2:sqrt(3)
        Location begin = Location.fromDegrees(0d, 0d);
        Location end = Location.fromDegrees(1d, Math.sqrt(3d));

        double azimuth = begin.linearAzimuth(end);

        assertEquals("linear azimuth", 60d, azimuth, TOLERANCE);
    }

    /**
     * Tests the linear azimuth across the +/- 180 meridian.
     *
     * @throws Exception
     */
    @Test
    public void testLinearAzimuth_AcrossDateline() throws Exception {
        // Create a 30-60-90 right triangle with a ratio of 1:2:sqrt(3)
        Location begin = Location.fromDegrees(0, 179.5d);
        Location end = Location.fromDegrees(Math.sqrt(3d), -179.5);

        double azimuth = begin.linearAzimuth(end);

        assertEquals("linear azimuth", 30d, azimuth, TOLERANCE);
    }

    /**
     * Ensures linear azimuth is NaN when NaN members are used.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testLinearAzimuth_WithNaN() throws Exception {
        Location begin = new Location(Double.NaN, Double.NaN);
        Location end = new Location(34.2, -119.2);  // KOXR

        double azimuth = begin.linearAzimuth(end);

        assertTrue("expecting NaN", Double.isNaN(azimuth));
    }

    @Test
    public void testLinearDistance() throws Exception {
        // Create a 30-60-90 right triangle with a ratio of 1:2:sqrt(3)
        Location begin = Location.fromDegrees(0d, 0d);
        Location end = Location.fromDegrees(1d, Math.sqrt(3d));

        double distance = begin.linearDistance(end);

        assertEquals("linear distance", 2d, Math.toDegrees(distance), TOLERANCE);
    }

    /**
     * Ensures linear distance is NaN when NaN members are used.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testLinearDistance_WithNaN() throws Exception {
        Location begin = new Location(Double.NaN, Double.NaN);
        Location end = new Location(34.2, -119.2);  // KOXR

        double distance = begin.linearDistance(end);

        assertTrue("expecting NaN", Double.isNaN(distance));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLinearLocation() throws Exception {
        // Create a 30-60-90 right triangle with a ratio of 1:2:sqrt(3)
        Location begin = Location.fromDegrees(34.2, -119.2);    // KOXR
        final double height = begin.latitude + 1d;
        final double base = begin.longitude + Math.sqrt(3);
        final double distance = Math.toRadians(2.0);
        final double azimuth = 60d;

        Location end = new Location();
        begin.linearLocation(azimuth, distance, end);

        assertEquals("longitude", base, end.longitude, TOLERANCE);
        assertEquals("latitude", height, end.latitude, TOLERANCE);
    }

    /**
     * Ensures the proper handling of NaN values.
     *
     * @throws Exception
     */
    @Ignore("NaN behavior TBD")
    @Test
    public void testLinearLocation_WithNaN() throws Exception {
        Location begin1 = Location.fromRadians(0.592539, -2.066470); // LAX
        Location begin2 = new Location(Double.NaN, Double.NaN);
        final double distance = 0.6296498957149533; // LAX to JFK
        final double course = 79.32398087460811;    // LAX to JFK

        Location end1 = new Location();
        Location end2 = new Location();
        Location end3 = new Location();
        begin1.linearLocation(Double.NaN, distance, end1);
        begin1.linearLocation(course, Double.NaN, end2);
        begin2.linearLocation(course, distance, end3);

        assertTrue("expecting NaN latitude from NaN course", Double.isNaN(end1.latitude));
        assertTrue("expecting NaN longitude from NaN course", Double.isNaN(end1.longitude));
        assertTrue("expecting NaN latitude from NaN distance", Double.isNaN(end2.latitude));
        assertTrue("expecting NaN longitude from NaN distance", Double.isNaN(end2.longitude));
        assertTrue("expecting NaN latitude from NaN origin latitude", Double.isNaN(end3.latitude));
        assertTrue("expecting NaN longitude from NaN origin latitude", Double.isNaN(end3.longitude));
    }
    // ---------------------------------------------------
    // The following tests were copied from WorldWind Java
    // ---------------------------------------------------

    //////////////////////////////////////////////////////////
    // Test equivalent points. Distance should always be 0.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsA() {
        Location begin = Location.fromDegrees(0.0, 0.0);
        Location end = Location.fromDegrees(0.0, 0.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial equivalent points A", 0.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsB() {
        Location begin = Location.fromDegrees(0.0, -180.0);
        Location end = Location.fromDegrees(0.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial equivalent points B", 0.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_TrivialEquivalentPointsC() {
        Location begin = Location.fromDegrees(0.0, 0.0);
        Location end = Location.fromDegrees(0.0, 360.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial equivalent points C", 0.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_EquivalentPoints() {
        Location begin = Location.fromDegrees(53.0902505, 112.8935442);
        Location end = Location.fromDegrees(53.0902505, 112.8935442);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Equivalent points", 0.0, distance, TOLERANCE);
    }

    //////////////////////////////////////////////////////////
    // Test antipodal points. Distance should always be 180.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsA() {
        Location begin = Location.fromDegrees(0.0, 0.0);
        Location end = Location.fromDegrees(0.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial antipodal points A", 180.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsB() {
        Location begin = Location.fromDegrees(-90.0, 0.0);
        Location end = Location.fromDegrees(90.0, 0.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial antipodal points B", 180.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_TrivialAntipodalPointsC() {
        Location begin = Location.fromDegrees(-90.0, -180.0);
        Location end = Location.fromDegrees(90.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial antipodal points C", 180.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_AntipodalPointsA() {
        Location begin = Location.fromDegrees(53.0902505, 112.8935442);
        Location end = Location.fromDegrees(-53.0902505, -67.1064558);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Antipodal points A", 180.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_AntipodalPointsB() {
        Location begin = Location.fromDegrees(-12.0, 87.0);
        Location end = Location.fromDegrees(12.0, -93.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Antipodal points B", 180.0, distance, TOLERANCE);
    }

    //////////////////////////////////////////////////////////
    // Test points known to be a certain angular distance apart.
    //////////////////////////////////////////////////////////

    @Test
    public void testGreatCircleDistance_KnownDistance() {
        Location begin = Location.fromDegrees(90.0, 45.0);
        Location end = Location.fromDegrees(36.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Known spherical distance", 54.0, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_KnownDistanceCloseToZero() {
        Location begin = Location.fromDegrees(-12.0, 87.0);
        Location end = Location.fromDegrees(-12.0000001, 86.9999999);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Known spherical distance (close to zero)", 1.3988468832247915e-7, distance, TOLERANCE);
    }

    @Test
    public void testGreatCircleDistance_KnownDistanceCloseTo180() {
        Location begin = Location.fromDegrees(-12.0, 87.0);
        Location end = Location.fromDegrees(11.9999999, -93.0000001);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Known spherical distance (close to 180)", 180.0, distance, TOLERANCE);
    }

    //////////////////////////////////////////////////////////
    // Test points that have caused problems.
    //////////////////////////////////////////////////////////
    @Test
    public void testGreatCircleDistance_ProblemPointsA() {
        Location begin = Location.fromDegrees(36.0, -118.0);
        Location end = Location.fromDegrees(36.0, -117.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Problem points A", 0.8090134466773318, distance, TOLERANCE);
    }

    @Test
    public void testRhumbLocation_ProblemPointsA() {
        // Compute location along/near equator
        double azimuth = 90.0;
        double distance = 0.08472006153859046;
        Location begin = Location.fromDegrees(2.892251645338908, -100.43740218868658);
        Location end = begin.rhumbLocation(azimuth, distance, new Location());

        // delta longitude
        double result = end.longitude - begin.longitude;
        double expected = 4.860293056378467;

        assertEquals("Delta Longitude", expected, result, 1e-15);

//        // This loop was used to test and identify the tolerance used in rhumbLocation.
//        double startLat =  2.892251645338908;
//        double latitude = startLat;
//        double longitude = -100.0;
//        double distance = 0.08472006153859046;
//        double azimuth = 90.0;
//        Location begin = new Location();
//        Location end = new Location();
//
//        while (latitude > startLat - 1e-16) {
//            begin.set(latitude, longitude);
//            begin.rhumbLocation(azimuth, distance, end);
//            double dLon = end.longitude - begin.longitude;
//
//            assertTrue("Delta Longitude @ [" + latitude + "] (" + dLon + ") < 10", dLon < 10);
//
//            latitude -= 1e-18;
//        }
    }

    @Test
    public void testRhumbDistance_ProblemPointsA() {
        // Compute location along/near equator
        Location begin = Location.fromDegrees(2.892251645338908, -100.43740218868658);
        Location end = Location.fromDegrees(2.892251645338908 + 1e-15, -95.57710913230811);

        double result = begin.rhumbDistance(end);
        double expected = 0.08472006153859046;

        assertEquals("Rhumb distance", expected, result, 1e-15);
    }
}