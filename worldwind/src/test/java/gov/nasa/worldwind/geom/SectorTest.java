/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class SectorTest {

    @Test
    public void testConstructor_default() throws Exception {
        Sector sector = new Sector();
        assertNotNull(sector);
        assertEquals("maxLatitude", 0d, sector.maxLatitude, 0d);
        assertEquals("maxLongitude", 0d, sector.maxLongitude, 0d);
        assertEquals("minLatitude", 0d, sector.minLatitude, 0d);
        assertEquals("minLongitude", 0d, sector.minLongitude, 0d);
    }

    @Ignore("not implemented")
    @Test
    public void testConstructor_Copy() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testFromDegrees() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testFromRadians() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testEquals() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testHashCode() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testIsEmpty() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testDeltaLatitude() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testDeltaLongitude() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testCentroidLatitude() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testCentroidLongitude() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testCentroid() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testSet() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testSet1() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testSetEmpty() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testSetFullSphere() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testIntersects() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testIntersects1() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testIntersect() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testIntersect1() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testContains() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testContains1() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testContains2() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testUnion() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testUnion1() throws Exception {

        fail("The test case is a stub.");

    }
}

