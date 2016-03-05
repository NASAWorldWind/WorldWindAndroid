/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class LocationTest {

    static final double THETA = Math.PI;    // latitude, arbitrary floating point num

    static final double PHI = Math.E;       // longitude, arbitrary floating point num

    static final double THRESHOLD = 1e-10;

    @Test
    public void testConstructor_Default() throws Exception {
        Location location = new Location();
        assertNotNull(location);
        assertEquals("latitude", 0.0, location.latitude, 0);
        assertEquals("longitude", 0.0, location.longitude, 0);
    }

    @Test
    public void testConstructor_Degrees() throws Exception {
        Location location = new Location(THETA, PHI);
        assertNotNull(location);
        assertEquals("latitude", THETA, location.latitude, 0);
        assertEquals("longitude", PHI, location.longitude, 0);
    }

    @Ignore("TODO: determine if NaNs will be allowed.")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNaNLatitude() throws Exception {
        Location location = new Location(Double.NaN, PHI);
    }

    @Ignore("TODO: determine if NaNs will be allowed.")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNaNLongitude() throws Exception {
        Location location = new Location(THETA, Double.NaN);
    }

    @Test
    public void testFromDegrees() throws Exception {
        Location location = Location.fromDegrees(THETA, PHI);
        assertEquals("latitude", THETA, location.latitude, Double.MIN_VALUE);
        assertEquals("longitude", PHI, location.longitude, Double.MIN_VALUE);
    }

    @Test
    public void testFromRadians() throws Exception {
        Location location = Location.fromRadians(THETA, PHI);
        assertEquals("latitude", Math.toDegrees(THETA), location.latitude, Double.MIN_VALUE);
        assertEquals("longitude", Math.toDegrees(PHI), location.longitude, Double.MIN_VALUE);
    }


    @Test
    public void testNormalizeLatitude() throws Exception {
        // Orbit the globe, starting at the equator and move north
        // testing the sign at the hemisphere boundaries.
        assertEquals("zero", 0.0, Location.normalizeLatitude(0.0), 0);
        assertEquals("1", 1.0, Location.normalizeLatitude(1.0), 0);
        assertEquals("89", 89.0, Location.normalizeLatitude(89.0), 0);
        assertEquals("90", 90.0, Location.normalizeLatitude(90.0), 0);
        // At the north pole, start moving south
        assertEquals("91", 89.0, Location.normalizeLatitude(91.0), 0);
        assertEquals("179", 1.0, Location.normalizeLatitude(179.0), 0);
        assertEquals("180", 0.0, Location.normalizeLatitude(180.0), 0);
        // At the equator, continue moving south
        assertEquals("181", -1.0, Location.normalizeLatitude(181.0), 0);
        assertEquals("269", -89.0, Location.normalizeLatitude(269.0), 0);
        assertEquals("270", -90.0, Location.normalizeLatitude(270.0), 0);
        assertEquals("271", -89.0, Location.normalizeLatitude(271.0), 0);
        assertEquals("359", -1.0, Location.normalizeLatitude(359.0), 0);
        assertEquals("360", 0.0, Location.normalizeLatitude(360.0), 0);
        assertEquals("361", 1.0, Location.normalizeLatitude(361.0), 0);
        // Back at the equator, now reverse direction
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

        // NaN is propagated.
        assertTrue("NaN", Double.isNaN(Location.normalizeLatitude(Double.NaN)));
    }


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
        assertEquals("-1", -1.0, Location.normalizeLongitude(-1.0), 0);
        assertEquals("359", -1.0, Location.normalizeLongitude(359.0), 0);
        assertEquals("360", 0.0, Location.normalizeLongitude(360.0), 0);
        assertEquals("361", 1.0, Location.normalizeLongitude(361.0), 0);
        assertEquals("1", 1.0, Location.normalizeLongitude(1.0), 0);

        // Assert negative -180 retains sign
        assertEquals("-180", -180.0, Location.normalizeLongitude(-180.0), 0);

        // Propagate NaNs
        assertTrue("NaN", Double.isNaN(Location.normalizeLongitude(Double.NaN)));
    }

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

    @Test
    public void testEquals() throws Exception {
        Location a = new Location(THETA, PHI);
        Location b = new Location(THETA, PHI);
        assertEquals("equality", a, b);
    }

    @Test
    public void testEquals_Inequality() throws Exception {
        // Assert that each member is checked for equality
        Location a = new Location(THETA, PHI);
        Location b = new Location(THETA, THETA);
        Location c = new Location(PHI, PHI);
        assertNotEquals("inequality", a, b);
        assertNotEquals("inequality", a, c);
    }


    @Ignore("not implemented")
    @Test
    public void testLocationsCrossAntimeridian() throws Exception {

        fail("The test case is a stub.");

    }

    @Test
    public void testSet_WithDoubles() throws Exception {
        // Assert that we read back the same data we set
        Location location = new Location();
        location.set(THETA, PHI);
        assertEquals("latitude", THETA, location.latitude, 0);
        assertEquals("longitude", PHI, location.longitude, 0);
    }

    @Test
    public void testSet_WithLocation() throws Exception {
        // Assert that we read back the same data we set
        Location location = new Location();
        Location other = new Location(THETA, PHI);
        location.set(other);
        assertEquals("latitude", other.latitude, location.latitude, 0);
        assertEquals("longitude", other.longitude, location.longitude, 0);

    }

    @Ignore
    @Test
    public void testInterpolateAlongPath() throws Exception {

        fail("The test case is a stub.");

    }

    @Test
    public void testGreatCircleAzimuth_North() throws Exception {
        Location origin = new Location();
        Location northPole = new Location(90, 0);
        double azimuth = origin.greatCircleAzimuth(northPole);
        assertEquals("north to pole", Location.normalizeLongitude(0), azimuth, THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_South() throws Exception {
        Location origin = new Location();
        Location southPole = new Location(-90, 0);
        double azimuth = origin.greatCircleAzimuth(southPole);
        assertEquals("south to pole", Location.normalizeLongitude(180), azimuth, THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_East() throws Exception {
        Location origin = new Location();
        Location east = new Location(0, 180);
        double azimuth = origin.greatCircleAzimuth(east);
        assertEquals("east to date line", 90, azimuth, THRESHOLD);
    }

    @Test
    public void testGreatCircleAzimuth_West() throws Exception {
        Location origin = new Location();
        Location west = new Location(0, -180);
        double azimuth = origin.greatCircleAzimuth(west);
        assertEquals("west to date line", Location.normalizeLongitude(270), azimuth, THRESHOLD);
    }


    @Test
    public void testGreatCircleDistance_AlongEquator() throws Exception {
        // Assert accurate max distances along the equator
        Location origin = new Location();
        double eastToDateLine = origin.greatCircleDistance(new Location(0, 180));
        assertEquals("prime meridian to date line east", Math.toRadians(180), eastToDateLine, THRESHOLD);

        double westToDateLine = origin.greatCircleDistance(new Location(0, -180));
        assertEquals("prime meridian to date line west ", Math.toRadians(180), westToDateLine, THRESHOLD);

        Location west = new Location(0, -22.5);
        double sideToSide = west.greatCircleDistance(new Location(0, 22.5));
        assertEquals("22.5 east to 22.5 west", Math.toRadians(45), sideToSide, THRESHOLD);
    }

    @Test
    public void testGreatCircleDistance_AlongMeridians() throws Exception {
        // Assert accurate max distances along lines of longitude
        Location origin = new Location();

        // Equator to North pole
        double northToPole = origin.greatCircleDistance(new Location(90, 0));
        assertEquals("equator to north pole", Math.toRadians(90), northToPole, THRESHOLD);

        // Equator to South pole
        double southToPole = origin.greatCircleDistance(new Location(-90, 0));
        assertEquals("equator to south pole", Math.toRadians(90), southToPole, THRESHOLD);

        // South pole to North Pole
        Location southPole = new Location(-90, 0);
        Location northPole = new Location(90, 0);
        double poleToPole = southPole.greatCircleDistance(northPole);
        assertEquals("south pole to north pole", Math.toRadians(180), poleToPole, THRESHOLD);

        Location south = new Location(-22.5, 0);
        Location north = new Location(22.5, 0);
        double southToNorth = south.greatCircleDistance(north);
        assertEquals("22.5 deg south to 22.5 north", Math.toRadians(45), southToNorth, THRESHOLD);
    }


    @Ignore("TODO: determine which is correct, NaN or zero?")
    @Test
    public void testGreatCircleDistance_WithNaN() throws Exception {
        Location location = new Location(Double.NaN, Double.NaN);
        double nanDistance = location.greatCircleDistance(new Location(34.2, -119.2));
        assertTrue("expecting NaN", Double.isNaN(nanDistance));
    }

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

    @Ignore("not implemented")
    @Test
    public void testRhumbAzimuth() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testRhumbDistance() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testRhumbLocation() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testLinearAzimuth() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testLinearDistance() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testLinearLocation() throws Exception {

        fail("The test case is a stub.");

    }

    // The following tests were copied from WorldWind Java

    //////////////////////////////////////////////////////////
    // Test equivalent points. Distance should always be 0.
    //////////////////////////////////////////////////////////

    @Test
    public void testTrivialEquivalentPointsA() {
        Location begin = Location.fromDegrees(0.0, 0.0);
        Location end = Location.fromDegrees(0.0, 0.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial equivalent points A", 0.0, distance, THRESHOLD);
    }

    @Test
    public void testTrivialEquivalentPointsB() {
        Location begin = Location.fromDegrees(0.0, -180.0);
        Location end = Location.fromDegrees(0.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial equivalent points B", 0.0, distance, THRESHOLD);
    }

    @Test
    public void testTrivialEquivalentPointsC() {
        Location begin = Location.fromDegrees(0.0, 0.0);
        Location end = Location.fromDegrees(0.0, 360.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial equivalent points C", 0.0, distance, THRESHOLD);
    }

    @Test
    public void testEquivalentPoints() {
        Location begin = Location.fromDegrees(53.0902505, 112.8935442);
        Location end = Location.fromDegrees(53.0902505, 112.8935442);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Equivalent points", 0.0, distance, THRESHOLD);
    }

    //////////////////////////////////////////////////////////
    // Test antipodal points. Distance should always be 180.
    //////////////////////////////////////////////////////////

    @Test
    public void testTrivialAntipodalPointsA() {
        Location begin = Location.fromDegrees(0.0, 0.0);
        Location end = Location.fromDegrees(0.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial antipodal points A", 180.0, distance, THRESHOLD);
    }

    @Test
    public void testTrivialAntipodalPointsB() {
        Location begin = Location.fromDegrees(-90.0, 0.0);
        Location end = Location.fromDegrees(90.0, 0.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial antipodal points B", 180.0, distance, THRESHOLD);
    }

    @Test
    public void testTrivialAntipodalPointsC() {
        Location begin = Location.fromDegrees(-90.0, -180.0);
        Location end = Location.fromDegrees(90.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Trivial antipodal points C", 180.0, distance, THRESHOLD);
    }

    @Test
    public void testAntipodalPointsA() {
        Location begin = Location.fromDegrees(53.0902505, 112.8935442);
        Location end = Location.fromDegrees(-53.0902505, -67.1064558);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Antipodal points A", 180.0, distance, THRESHOLD);
    }

    @Test
    public void testAntipodalPointsB() {
        Location begin = Location.fromDegrees(-12.0, 87.0);
        Location end = Location.fromDegrees(12.0, -93.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Antipodal points B", 180.0, distance, THRESHOLD);
    }

    //////////////////////////////////////////////////////////
    // Test points known to be a certain angular distance apart.
    //////////////////////////////////////////////////////////

    @Test
    public void testKnownDistance() {
        Location begin = Location.fromDegrees(90.0, 45.0);
        Location end = Location.fromDegrees(36.0, 180.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Known spherical distance", 54.0, distance, THRESHOLD);
    }

    @Test
    public void testKnownDistanceCloseToZero() {
        Location begin = Location.fromDegrees(-12.0, 87.0);
        Location end = Location.fromDegrees(-12.0000001, 86.9999999);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Known spherical distance (close to zero)", 1.3988468832247915e-7, distance, THRESHOLD);
    }

    @Test
    public void testKnownDistanceCloseTo180() {
        Location begin = Location.fromDegrees(-12.0, 87.0);
        Location end = Location.fromDegrees(11.9999999, -93.0000001);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Known spherical distance (close to 180)", 180.0, distance, THRESHOLD);
    }

    //////////////////////////////////////////////////////////
    // Test points that have caused problems.
    //////////////////////////////////////////////////////////
    @Test
    public void testProblemPointsA() {
        Location begin = Location.fromDegrees(36.0, -118.0);
        Location end = Location.fromDegrees(36.0, -117.0);
        double distance = Math.toDegrees(begin.greatCircleDistance(end));
        assertEquals("Problem points A", 0.8090134466773318, distance, THRESHOLD);
    }

}