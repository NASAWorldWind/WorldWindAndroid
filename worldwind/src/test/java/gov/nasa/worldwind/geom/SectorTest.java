/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

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
}

