/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.SecureCacheResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.*;

/**
 * Unit tests for the Sector class.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log
public class SectorTest {

    static final double TOLERANCE = 1e-10;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Logger.class);
    }


    @Test
    public void reviewNaNBehavior() {
        // Note: Two NaNs are NOT equal to each other
        // Implication: Two "empty" sectors will not be equal to each other based on member equality.
        assertFalse("equality", Double.NaN == Double.NaN);
        assertTrue("inequality", Double.NaN != Double.NaN);

        assertFalse("less than", Double.NaN < Double.NaN);
        assertFalse("greater than", Double.NaN > Double.NaN);

        // Examine how NaN values are used in hashCode
        long temp = Double.doubleToLongBits(Double.NaN);
        //System.out.println("doubleToLongBits:" + temp);

    }

    @Test
    public void testConstructor_Default() throws Exception {
        Sector sector = new Sector();
        assertNotNull(sector);
        assertTrue("NaN minLatitude", Double.isNaN(sector.minLatitude()));
        assertTrue("NaN minLongitude", Double.isNaN(sector.minLongitude()));
        assertTrue("NaN maxLatitude", Double.isNaN(sector.maxLatitude()));
        assertTrue("NaN maxLongitude", Double.isNaN(sector.maxLongitude()));
        assertTrue("NaN deltaLatitude", Double.isNaN(sector.deltaLatitude()));
        assertTrue("NaN deltaLongitude", Double.isNaN(sector.deltaLongitude()));
    }

    @Test
    public void testConstructor_Typical() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = new Sector(lat, lon, dLat, dLon);

        assertNotNull(sector);
        assertEquals("minLatitude", lat, sector.minLatitude(), TOLERANCE);
        assertEquals("minLongitude", lon, sector.minLongitude(), TOLERANCE);
        assertEquals("maxLatitude", lat + dLat, sector.maxLatitude(), TOLERANCE);
        assertEquals("maxLongitude", lon + dLon, sector.maxLongitude(), TOLERANCE);
        assertEquals("deltaLatitude", dLat, sector.deltaLatitude(), TOLERANCE);
        assertEquals("deltaLongitude", dLon, sector.deltaLongitude(), TOLERANCE);
    }

    @Test
    public void testConstructor_Copy() throws Exception {
        Sector sector = new Sector(34.2, -119.2, 1.0, 2.0);

        Sector copy = new Sector(sector);

        assertNotNull(copy);
        assertEquals(sector, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_CopyWithNull() throws Exception {

        Sector sector = new Sector(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testFromDegrees() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertNotNull(sector);
        assertEquals("minLatitude", lat, sector.minLatitude(), TOLERANCE);
        assertEquals("minLongitude", lon, sector.minLongitude(), TOLERANCE);
        assertEquals("maxLatitude", lat + dLat, sector.maxLatitude(), TOLERANCE);
        assertEquals("maxLongitude", lon + dLon, sector.maxLongitude(), TOLERANCE);
        assertEquals("deltaLatitude", dLat, sector.deltaLatitude(), TOLERANCE);
        assertEquals("deltaLongitude", dLon, sector.deltaLongitude(), TOLERANCE);
    }

    @Test
    public void testFromRadians() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);
        double dLatRad = Math.toRadians(dLat);
        double dLonRad = Math.toRadians(dLon);

        Sector sector = Sector.fromRadians(latRad, lonRad, dLatRad, dLonRad);

        assertNotNull(sector);
        assertEquals("minLatitude", lat, sector.minLatitude(), TOLERANCE);
        assertEquals("minLongitude", lon, sector.minLongitude(), TOLERANCE);
        assertEquals("maxLatitude", lat + dLat, sector.maxLatitude(), TOLERANCE);
        assertEquals("maxLongitude", lon + dLon, sector.maxLongitude(), TOLERANCE);
        assertEquals("deltaLatitude", dLat, sector.deltaLatitude(), TOLERANCE);
        assertEquals("deltaLongitude", dLon, sector.deltaLongitude(), TOLERANCE);

    }

    @Test
    public void testEquals() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector1 = Sector.fromDegrees(lat, lon, dLat, dLon);
        Sector sector2 = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("minLatitude", sector2.minLatitude(), sector1.minLatitude(), 0);
        assertEquals("minLongitude", sector2.minLongitude(), sector1.minLongitude(), 0);
        assertEquals("maxLatitude", sector2.maxLatitude(), sector1.maxLatitude(), 0);
        assertEquals("maxLongitude", sector2.maxLongitude(), sector1.maxLongitude(), 0);
        assertEquals("deltaLatitude", sector2.deltaLatitude(), sector1.deltaLatitude(), 0);
        assertEquals("deltaLongitude", sector2.deltaLongitude(), sector1.deltaLongitude(), 0);
        assertEquals(sector1, sector1);
        assertEquals(sector1, sector2);
    }

    @Test
    public void testEquals_Inequality() throws Exception {
        Sector empty = new Sector();
        Sector other = new Sector();
        Sector typical = Sector.fromDegrees(34.2, -119.2, 1.0, 2.0);
        Sector another = Sector.fromDegrees(33.94, -118.4, 1.0, 2.0);

        assertNotEquals(empty, empty);
        assertNotEquals(empty, other);
        assertNotEquals(empty, typical);
        assertNotEquals(empty, null);
        assertNotEquals(typical, another);
        assertNotEquals(typical, null);
    }

    @Test
    public void testHashCode() throws Exception {
        Sector a = new Sector();
        Sector b = Sector.fromDegrees(34.2, -119.2, 1.0, 2.0);
        Sector c = Sector.fromDegrees(33.94, -118.4, 1.0, 2.0);

        int aHash = a.hashCode();
        int bHash = b.hashCode();
        int cHash = c.hashCode();

        assertNotEquals("a hash vs b hash", bHash, aHash);
        assertNotEquals("b hash vs c hash", cHash, bHash);
    }

    @Test
    public void testIsEmpty() throws Exception {
        Sector empty = new Sector();
        Sector noDim = Sector.fromDegrees(34.2, -119.2, 0, 0);
        Sector noWidth = Sector.fromDegrees(34.2, -119.2, 1.0, 0);
        Sector noHeight = Sector.fromDegrees(34.2, -119.2, 0, 1.0);
        Sector noLat = Sector.fromDegrees(Double.NaN, -119.2, 1.0, 1.0);
        Sector noLon = Sector.fromDegrees(34.2, Double.NaN, 1.0, 1.0);
        Sector typical = Sector.fromDegrees(34.2, -119.2, 1.0, 1.0);

        assertTrue("default is empty", empty.isEmpty());
        assertTrue("no dimension is empty", noDim.isEmpty());
        assertTrue("no width is empty", noWidth.isEmpty());
        assertTrue("no height is empty", noHeight.isEmpty());
        assertTrue("no latitude is empty", noLat.isEmpty());
        assertTrue("no longitude is empty", noLon.isEmpty());
        assertFalse("typical is not empty", typical.isEmpty());
    }

    @Test
    public void testMinLatitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("minLatitude", lat, sector.minLatitude(), 0);
    }

    @Test
    public void testMaxLatitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("maxLatitude", lat + dLat, sector.maxLatitude(), 0);
    }


    @Test
    public void testMinLongitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("minLongitude", lon, sector.minLongitude(), 0);
    }

    @Test
    public void testMaxLongitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("maxLongitude", lon + dLon, sector.maxLongitude(), 0);
    }


    @Test
    public void testDeltaLatitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("deltaLatitude", dLat, sector.deltaLatitude(), 0);
    }

    @Test
    public void testDeltaLongitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;

        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        assertEquals("deltaLongitude", dLon, sector.deltaLongitude(), 0);
    }

    @Test
    public void testCentroidLatitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 2.0;
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        double latitude = sector.centroidLatitude();

        assertEquals("centroid latitude", lat + dLat * 0.5, latitude, TOLERANCE);
    }


    @Test
    public void testCentroidLatitude_NoDimension() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 0.0;  // No height!
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        double latitude = sector.centroidLatitude();

        assertTrue("NaN centroid latitude", Double.isNaN(latitude));
    }

    @Test
    public void testCentroidLatitude_NoLocation() throws Exception {
        double lat = Double.NaN;
        double lon = -119.2;
        double dLat = 2.0;
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        double latitude = sector.centroidLatitude();

        assertTrue("NaN centroid latitude", Double.isNaN(latitude));
    }

    @Test
    public void testCentroidLongitude() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 2.0;
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        double longitude = sector.centroidLongitude();

        assertEquals("centroid longitude", lon + dLon * 0.5, longitude, TOLERANCE);
    }

    @Test
    public void testCentroidLongitude_NoDimension() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 2.0;
        double dLon = 0.0;  // No width!
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        double longitude = sector.centroidLongitude();

        assertTrue("NaN centroid longitude", Double.isNaN(longitude));
    }

    @Test
    public void testCentroidLongitude_NoLocation() throws Exception {
        double lat = 34.2;
        double lon = Double.NaN;
        double dLat = 2.0;
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        double longitude = sector.centroidLongitude();

        assertTrue("NaN centroid longitude", Double.isNaN(longitude));
    }

    @Test
    public void testCentroid() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 2.0;
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        Location centroid = sector.centroid(new Location());

        assertEquals("centroid longitude", lat + dLat * 0.5, centroid.latitude, TOLERANCE);
        assertEquals("centroid longitude", lon + dLon * 0.5, centroid.longitude, TOLERANCE);
    }


    @Test
    public void testCentroid_NoDimension() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 2.0;
        double dLon = 0.0;  // No width!
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        Location centroid = sector.centroid(new Location());

        assertTrue("NaN centroid longitude", Double.isNaN(centroid.longitude));
    }

    @Test
    public void testCentroid_NoLocation() throws Exception {
        double lat = 34.2;
        double lon = Double.NaN;
        double dLat = 2.0;
        double dLon = 2.0;
        Sector sector = Sector.fromDegrees(lat, lon, dLat, dLon);

        Location centroid = sector.centroid(new Location());

        assertTrue("NaN centroid longitude", Double.isNaN(centroid.longitude));
    }


    @Test
    public void testSet_Doubles() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;
        Sector a = new Sector();

        Sector b = a.set(lat, lon, dLat, dLon);

        assertEquals("minLatitude", lat, a.minLatitude(), TOLERANCE);
        assertEquals("minLongitude", lon, a.minLongitude(), TOLERANCE);
        assertEquals("deltaLatitude", dLat, a.deltaLatitude(), TOLERANCE);
        assertEquals("deltaLongitude", dLon, a.deltaLongitude(), TOLERANCE);
        assertSame(a, b);
    }

    @Test
    public void testSet() throws Exception {
        double lat = 34.2;
        double lon = -119.2;
        double dLat = 1.0;
        double dLon = 2.0;
        Sector a = new Sector();

        Sector b = a.set(new Sector(lat, lon, dLat, dLon));

        assertEquals("minLatitude", lat, a.minLatitude(), TOLERANCE);
        assertEquals("minLongitude", lon, a.minLongitude(), TOLERANCE);
        assertEquals("deltaLatitude", dLat, a.deltaLatitude(), TOLERANCE);
        assertEquals("deltaLongitude", dLon, a.deltaLongitude(), TOLERANCE);
        assertSame(a, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        Sector a = new Sector();

        a.set(null);

        fail("Expected IllegalArgumentException");
    }

    @Test
    public void testSetEmpty() throws Exception {
        Sector a = new Sector(34.2, -119.2, 1.0, 2.0);

        a.setEmpty();

        assertTrue("empty", a.isEmpty());
    }

    @Test
    public void testSetFullSphere() throws Exception {
        Sector a = new Sector(34.2, -119.2, 1.0, 2.0);

        a.setFullSphere();

        assertEquals("minLatitude", -90.0, a.minLatitude(), 0);
        assertEquals("maxLatitude", 90.0, a.maxLatitude(), 0);
        assertEquals("minLongitude", -180.0, a.minLongitude(), 0);
        assertEquals("maxLongitude", 180.0, a.maxLongitude(), 0);
        assertEquals("deltaLatitude", 180.0, a.deltaLatitude(), 0);
        assertEquals("deltaLongitude", 360.0, a.deltaLongitude(), 0);
    }

    @Test
    public void testIntersects_Doubles() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector copy = new Sector(a);

        assertTrue("inside", a.intersects(31.0, 101.0, 1d, 1d));
        assertTrue("overlap east", a.intersects(31.0, 102.0, 1d, 2d));
        assertTrue("overlap west", a.intersects(31.0, 99.0, 1d, 2d));
        assertTrue("overlap north", a.intersects(32.0, 101.0, 2d, 1d));
        assertTrue("overlap south", a.intersects(29.0, 101.0, 2d, 1d));
        assertEquals("no mutation", copy, a);
    }

    @Test
    public void testIntersects_DoublesEmpty() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);

        assertFalse("no location", a.intersects(Double.NaN, Double.NaN, 0d, 0d));
        assertFalse("no lat", a.intersects(Double.NaN, 101.0, 5d, 5d));
        assertFalse("no lon", a.intersects(31.0, Double.NaN, 5d, 5d));
        assertFalse("no dimension", a.intersects(31.0, 101.0, 0d, 0d));
        assertFalse("no width", a.intersects(31.0, 101.0, 5d, 0d));
        assertFalse("no height", a.intersects(31.0, 101.0, 0d, 5d));

    }

    @Test
    public void testIntersects_DoublesCoincident() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 1.0, 1.0);

        assertTrue("coincident", a.intersects(30.0, 100.0, 1d, 1d));
        assertFalse("coincident east edge", a.intersects(30.0, 101.0, 1d, 1d));
        assertFalse("coincident west edge", a.intersects(30.0, 99.0, 1d, 1d));
        assertFalse("coincident north edge", a.intersects(31.0, 100.0, 1d, 1d));
        assertFalse("coincident south edge", a.intersects(29.0, 100.0, 1d, 1d));
        assertFalse("coincident ne point", a.intersects(31.0, 101.0, 1d, 1d));
        assertFalse("coincident se point", a.intersects(29.0, 101.0, 1d, 1d));
        assertFalse("coincident nw point", a.intersects(31.0, 99.0, 1d, 1d));
        assertFalse("coincident sw point", a.intersects(29.0, 99.0, 1d, 1d));
    }

    @Test
    public void testIntersects() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector copy = new Sector(a);

        assertTrue("inside", a.intersects(new Sector(31.0, 101.0, 1d, 1d)));
        assertTrue("overlap east", a.intersects(new Sector(31.0, 102.0, 1d, 2d)));
        assertTrue("overlap west", a.intersects(new Sector(31.0, 99.0, 1d, 2d)));
        assertTrue("overlap north", a.intersects(new Sector(32.0, 101.0, 2d, 1d)));
        assertTrue("overlap south", a.intersects(new Sector(29.0, 101.0, 2d, 1d)));
        assertEquals("no mutation", copy, a);

    }

    @Test
    public void testIntersects_Empty() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);

        assertFalse("empty", a.intersects(new Sector()));
        assertFalse("no dimension", a.intersects(new Sector(31.0, 101.0, 0d, 0d)));
        assertFalse("no dimension from union", a.intersects(new Sector().union(31.0, 101.0)));
        assertFalse("no lat", a.intersects(new Sector(Double.NaN, 101.0, 5d, 5d)));
        assertFalse("no lon", a.intersects(new Sector(31.0, Double.NaN, 5d, 5d)));
        assertFalse("no width", a.intersects(new Sector(31.0, 101.0, 5d, 0d)));
        assertFalse("no height", a.intersects(new Sector(31.0, 101.0, 0d, 5d)));

    }

    @Test
    public void testIntersects_Coincident() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 1.0, 1.0);

        assertTrue("coincident", a.intersects(new Sector(30.0, 100.0, 1d, 1d)));
        assertFalse("coincident east edge", a.intersects(new Sector(30.0, 101.0, 1d, 1d)));
        assertFalse("coincident west edge", a.intersects(new Sector(30.0, 99.0, 1d, 1d)));
        assertFalse("coincident north edge", a.intersects(new Sector(31.0, 100.0, 1d, 1d)));
        assertFalse("coincident south edge", a.intersects(new Sector(29.0, 100.0, 1d, 1d)));
        assertFalse("coincident ne point", a.intersects(new Sector(31.0, 101.0, 1d, 1d)));
        assertFalse("coincident se point", a.intersects(new Sector(29.0, 101.0, 1d, 1d)));
        assertFalse("coincident nw point", a.intersects(new Sector(31.0, 99.0, 1d, 1d)));
        assertFalse("coincident sw point", a.intersects(new Sector(29.0, 99.0, 1d, 1d)));
    }


    @Test
    public void testIntersect_Doubles() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector inside = Sector.fromDegrees(31, 101, 1.0, 1.0);

        boolean intersected = a.intersect(31, 101, 1.0, 1.0);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", inside, a);
    }


    @Test
    public void testIntersect_DoublesNE() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 2.0, 2.0);
        Sector northeast = Sector.fromDegrees(31, 101, 1.0, 1.0);

        boolean intersected = a.intersect(31, 101, 2.0, 2.0);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", northeast, a);
    }

    @Test
    public void testIntersect_DoublesNW() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 2.0, 2.0);
        Sector northwest = Sector.fromDegrees(31, 100, 1.0, 1.0);

        boolean intersected = a.intersect(31, 99, 2.0, 2.0);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", northwest, a);
    }

    @Test
    public void testIntersect_DoublesSW() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 2.0, 2.0);
        Sector southwest = Sector.fromDegrees(30, 100, 1.0, 1.0);

        boolean intersected = a.intersect(29, 99, 2.0, 2.0);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", southwest, a);
    }

    @Test
    public void testIntersect_DoublesSE() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 2.0, 2.0);
        Sector southeast = Sector.fromDegrees(30, 101, 1.0, 1.0);

        boolean intersected = a.intersect(29, 101, 2.0, 2.0);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", southeast, a);
    }

    @Test
    public void testIntersect_DoublesAdjacent() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 1.0, 1.0);
        Sector copy = new Sector(a);

        boolean intersected = a.intersect(30, 101, 1.0, 1.0); // adjacent

        assertFalse("no intersection", intersected);
        assertEquals("no change", copy, a);
    }

    @Test
    public void testIntersect() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 2.0, 2.0);
        Sector b = Sector.fromDegrees(31, 101, 2.0, 2.0);
        Sector northeast = Sector.fromDegrees(31, 101, 1.0, 1.0);

        boolean intersected = a.intersect(b);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", northeast, a);
    }

    @Test
    public void testIntersect_Inside() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector inside = Sector.fromDegrees(31, 101, 1.0, 1.0);

        boolean intersected = a.intersect(inside);

        assertTrue("interesecting", intersected);
        assertEquals("inside, intersection is interior sector", inside, a);
    }

    @Test
    public void testIntersect_East() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector east = Sector.fromDegrees(31, 102, 1.0, 2.0);
        Sector expected = Sector.fromDegrees(31, 102, 1.0, 1.0);

        boolean intersected = a.intersect(east);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_West() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector west = Sector.fromDegrees(31, 99, 1.0, 2.0);
        Sector expected = Sector.fromDegrees(31, 100, 1.0, 1.0);

        boolean intersected = a.intersect(west);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_North() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector north = Sector.fromDegrees(32, 101, 2.0, 1.0);
        Sector expected = Sector.fromDegrees(32, 101, 1.0, 1.0);

        boolean intersected = a.intersect(north);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_South() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector south = Sector.fromDegrees(29, 101, 2.0, 1.0);
        Sector expected = Sector.fromDegrees(30, 101, 1.0, 1.0);

        boolean intersected = a.intersect(south);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }


    @Test
    public void testIntersect_AdjacentEast() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector adjacentEast = Sector.fromDegrees(31, 103, 1.0, 1.0);
        Sector copy = new Sector(a);

        boolean intersected = a.intersect(adjacentEast);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_AdjacentWest() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector adjacentWest = Sector.fromDegrees(31, 99, 1.0, 1.0);
        Sector copy = new Sector(a);

        boolean intersected = a.intersect(adjacentWest);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_AdjacentNorth() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector adjacentNorth = Sector.fromDegrees(33, 101, 1.0, 1.0);
        Sector copy = new Sector(a);

        boolean intersected = a.intersect(adjacentNorth);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_AdjacentSouth() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);
        Sector adjacentSouth = Sector.fromDegrees(29, 101, 1.0, 1.0);
        Sector copy = new Sector(a);

        boolean intersected = a.intersect(adjacentSouth);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }


    @Test
    public void testContains() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 1.0, 1.0);

        assertTrue("inside", a.contains(30.5, 100.5));
        assertTrue("northeast point", a.contains(31, 101));
        assertTrue("northwest point", a.contains(31, 100));
        assertTrue("southwest point", a.contains(30, 100));
        assertTrue("southeast point", a.contains(30, 101));
        assertFalse("outside", a.contains(-30, -100));
    }

    @Test
    public void testContains_Empty() throws Exception {
        Sector a = new Sector();

        assertFalse("empty doesn't contain", a.contains(31, 101));
    }

    @Test
    public void testContains_Doubles() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);

        assertTrue("coincident", a.contains(30, 100, 3.0, 3.0));
        assertTrue("inside", a.contains(31, 101, 1.0, 1.0));
    }

    @Test
    public void testContains_Sector() throws Exception {
        Sector a = Sector.fromDegrees(30, 100, 3.0, 3.0);

        assertTrue("coincident", a.contains(new Sector(30, 100, 3.0, 3.0)));
        assertTrue("inside", a.contains(new Sector(31, 101, 1.0, 1.0)));
    }

    @Test
    public void testUnion() throws Exception {
        double latOxr = 34.2;
        double lonOxr = -119.2;
        double latLax = 33.94;
        double lonLax = -118.4;
        Sector a = new Sector();

        Sector b = a.union(latOxr, lonOxr);
        assertTrue(a.isEmpty());

        a.union(latLax, lonLax);
        assertFalse(a.isEmpty());

        assertEquals("min lat", latLax, a.minLatitude(), 0);
        assertEquals("min lon", lonOxr, a.minLongitude(), 0);
        assertEquals("max lat", latOxr, a.maxLatitude(), 0);
        assertEquals("max lon", lonLax, a.maxLongitude(), 0);
        assertSame(a, b);
    }

    @Test
    public void testUnion_ArrayOfLocations() throws Exception {
        float[] array = {
            -119.2f, 34.2f, // OXR airport
            -118.4f, 33.94f, // LAX airport
            -118.45f, 34.02f // SMO airport
        };
        Sector a = new Sector();

        Sector b = a.union(array, array.length, 2 /*stride*/);

        assertFalse(a.isEmpty());
        assertEquals("min lat", 33.94f /*LAX lat*/, a.minLatitude(), 0);
        assertEquals("min lon", -119.2f /*OXR lon*/, a.minLongitude(), 0);
        assertEquals("max lat", 34.2f /*OXR lat*/, a.maxLatitude(), 0);
        assertEquals("max lon", -118.4f /*LAX lon*/, a.maxLongitude(), 0);
        assertSame(a, b);
    }

    @Test
    public void testUnion_Doubles() throws Exception {
        Sector a = Sector.fromDegrees(-30, -100, 1.0, 1.0);

        Sector b = a.union(40, 110, 1.0, 1.0);

        assertFalse(a.isEmpty());
        assertEquals("min lat", -30, a.minLatitude(), 0);
        assertEquals("min lon", -100, a.minLongitude(), 0);
        assertEquals("max lat", 41, a.maxLatitude(), 0);
        assertEquals("max lon", 111, a.maxLongitude(), 0);
        assertSame(a, b);
    }

    @Test
    public void testUnion_Sector() throws Exception {
        Sector a = Sector.fromDegrees(-30, -100, 1.0, 1.0);

        Sector b = a.union(new Sector(40, 110, 1.0, 1.0));

        assertFalse(a.isEmpty());
        assertEquals("min lat", -30, a.minLatitude(), 0);
        assertEquals("min lon", -100, a.minLongitude(), 0);
        assertEquals("max lat", 41, a.maxLatitude(), 0);
        assertEquals("max lon", 111, a.maxLongitude(), 0);
        assertSame(a, b);
    }
}

