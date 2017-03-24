/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import org.junit.Test;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Ellipsoid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests World Wind's WGS84 definition to ensure it conforms to these ellipsoid parameters obtained from the official
 * WGS84 specifications: http://earth-info.nga.mil/GandG/publications/NGA_STND_0036_1_0_0_WGS84/NGA.STND.0036_1.0.0_WGS84.pdf
 * <p/>
 * Semi Major Axis: 6378137
 * <p/>
 * Flattening: 298.257223563
 */
public class Wgs84Test {

    private static final double OFFICIAL_WGS84_SEMI_MAJOR_AXIS = 6378137.0;

    private static final double OFFICIAL_WGS84_SEMI_MINOR_AXIS = 6356752.3142;

    private static final double OFFICIAL_WGS84_INVERSE_FLATTENING = 298.257223563;

    private static final double OFFICIAL_WGS84_EC2 = 6.694379990141e-3;

    @Test
    public void testWgs84SemiMajorAxis() throws Exception {

        assertEquals("WGS84 semi-major axis", OFFICIAL_WGS84_SEMI_MAJOR_AXIS, WorldWind.WGS84_SEMI_MAJOR_AXIS, 0);
    }

    @Test
    public void testWgs84InverseFlattening() throws Exception {

        assertEquals("WGS84 inverse flattening", OFFICIAL_WGS84_INVERSE_FLATTENING, WorldWind.WGS84_INVERSE_FLATTENING, 0);
    }

    @Test
    public void testWgs84Ellipsoid() throws Exception {
        Ellipsoid ellipsoid = WorldWind.WGS84_ELLIPSOID;

        assertNotNull("WGS84 ellipsoid not null", ellipsoid);

        assertEquals("WGS84 ellipsoid semi-major axis", OFFICIAL_WGS84_SEMI_MAJOR_AXIS, ellipsoid.semiMajorAxis(), 0);

        assertEquals("WGS84 ellipsoid inverse flattening", OFFICIAL_WGS84_INVERSE_FLATTENING, ellipsoid.inverseFlattening(), 0);

        // WGS84 official value:  6356752.3142
        // Actual computed value: 6356752.314245179
        assertEquals("WGS84 ellipsoid semi-minor axis", OFFICIAL_WGS84_SEMI_MINOR_AXIS, ellipsoid.semiMinorAxis(), 1.0e-4);

        // Official value:        6.694379990141e-3
        // Actual computed value: 6.6943799901413165e-3
        assertEquals("WGS84 ellipsoid eccentricity squared ", OFFICIAL_WGS84_EC2, ellipsoid.eccentricitySquared(), 1.0e-15);
    }
}