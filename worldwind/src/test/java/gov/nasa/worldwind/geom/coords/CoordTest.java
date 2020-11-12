/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import org.junit.Test;

import gov.nasa.worldwind.geom.Location;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CoordTest {
    
    private static boolean isClose(double x, double y, double limit) {
        return (Math.abs(x - y) < limit);
    }

    private static boolean isClose(Location a, Location b) {
        double epsilonRad = Math.toRadians(9.0e-6);
        return isClose(a, b, epsilonRad);
    }

    private static boolean isClose(Location a, Location b, double limit) {
        return isClose(Math.toRadians(a.latitude), Math.toRadians(b.latitude), limit)
            && isClose(Math.toRadians(a.longitude), Math.toRadians(b.longitude), limit);
    }
    
    private static final Location[] TEST_POSITIONS = {
        Location.fromDegrees(-74.37916, 155.02235),
        Location.fromDegrees(0, 0),
        Location.fromDegrees(0.13, -0.2324),
        Location.fromDegrees(-45.6456, 23.3545),
        Location.fromDegrees(-12.7650, -33.8765),
        Location.fromDegrees(23.4578, -135.4545),
        Location.fromDegrees(77.3450, 156.9876)
    };
    
    @Test
    public void utmConstructionTest() {
        for (Location input : TEST_POSITIONS) {
            UTMCoord fromLocation = UTMCoord.fromLatLon(input.latitude, input.longitude);
            UTMCoord utmCoord = UTMCoord.fromUTM(fromLocation.getZone(), fromLocation.getHemisphere(), fromLocation.getEasting(), fromLocation.getNorthing());
            Location position = Location.fromDegrees(utmCoord.getLatitude(), utmCoord.getLongitude());
            assertTrue(isClose(input, position));
        }
    }

    @Test
    public void mgrsConstructionTest() {
        for (Location input : TEST_POSITIONS) {
            MGRSCoord fromLocation = MGRSCoord.fromLatLon(input.latitude, input.longitude);
            MGRSCoord fromString = MGRSCoord.fromString(fromLocation.toString());
            Location position = Location.fromDegrees(fromString.getLatitude(), fromString.getLongitude());
            assertTrue(isClose(input, position, 0.0002));
        }
    }
    
    private static final Location[] MGRS_ONLY_POSITIONS = {
        Location.fromDegrees(-89.3454, -48.9306),
        Location.fromDegrees(-80.5434, -170.6540),
    };
    
    @Test
    public void mgrsOnlyConstructionTest() {
        for (Location input : MGRS_ONLY_POSITIONS) {
            MGRSCoord fromLocation = MGRSCoord.fromLatLon(input.latitude, input.longitude);
            MGRSCoord fromString = MGRSCoord.fromString(fromLocation.toString());
            Location position = Location.fromDegrees(fromString.getLatitude(), fromString.getLongitude());
            assertTrue(isClose(input, position, 0.0002));
        }
    }
    
    private static final Location[] NO_INVERSE_POSITIONS = {
        Location.fromDegrees(90.0, 177.0),
        Location.fromDegrees(-90.0, -177.0),
        Location.fromDegrees(90.0, 3.0)
    };
    
    private static final String[] NO_INVERSE_TO_MGRS = {
        "ZAH 00000 00000", "BAN 00000 00000", "ZAH 00000 00000"
    };
    
    @Test
    public void noInverseToMGRSTest() {
        for (int i = 0; i < NO_INVERSE_POSITIONS.length; i++) {
            Location input = NO_INVERSE_POSITIONS[i];
            MGRSCoord fromLocation = MGRSCoord.fromLatLon(input.latitude, input.longitude);
            String mgrsString = fromLocation.toString().trim();
            assertEquals(mgrsString, NO_INVERSE_TO_MGRS[i]);
        }
    }
}