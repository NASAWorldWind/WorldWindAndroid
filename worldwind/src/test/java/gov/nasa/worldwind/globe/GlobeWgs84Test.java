/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import org.junit.Test;

import gov.nasa.worldwind.WorldWind;

import static org.junit.Assert.*;

/**
 * Tests the GlobeWgs84 definition to ensure it conforms to these ellipsoid parameters obtained from the official WGS84
 * specifications: http://earth-info.nga.mil/GandG/publications/tr8350.2/wgs84fin.pdf
 * <p/>
 * Semi Major Axis: 6378137
 * <p/>
 * Flattening: 298.257223563
 */
public class GlobeWgs84Test {

    /**
     * WGS84 Specification for semi-major axis
     */
    private static final double OFFICIAL_SEMI_MAJOR_AXIS = 6378137;

    /**
     * WGS84 Specification for reciprocal of flattening
     */
    private static final double OFFICIAL_INVERSE_FLATTENING = 298.257223563;


    @Test
    public void testConstructor() throws Exception {
        GlobeWgs84 globe = new GlobeWgs84();

        assertNotNull("GlobeWgs84", globe);

    }

    @Test
    public void testWgs84SemiMajorAxis() throws Exception {

        assertEquals("semi-major axis", OFFICIAL_SEMI_MAJOR_AXIS, WorldWind.WGS84_SEMI_MAJOR_AXIS, 0);

    }

    @Test
    public void testWgs84InverseFlattening() throws Exception {

        assertEquals("inverse flattening", OFFICIAL_INVERSE_FLATTENING, WorldWind.WGS84_INVERSE_FLATTENING, 0);

    }
}