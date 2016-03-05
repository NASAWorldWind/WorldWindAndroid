/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class WWMathTest {

    @Test
    public void testNormalizeDegrees() throws Exception {
        // Starting at the prime meridian, travel eastward around the globe
        assertEquals("zero", 0.0, WWMath.normalizeDegrees(0.0), 0);
        assertEquals("180", 180.0, WWMath.normalizeDegrees(180.0), 0);
        assertEquals("181", 181.0, WWMath.normalizeDegrees(181.0), 0);
        assertEquals("359", 359.0, WWMath.normalizeDegrees(359.0), 0);
        assertEquals("360", 0.0, WWMath.normalizeDegrees(360.0), 0);
        assertEquals("361", 1.0, WWMath.normalizeDegrees(361.0), 0);
        assertEquals("720", 0.0, WWMath.normalizeDegrees(720.0), 0);
        assertEquals("721", 1.0, WWMath.normalizeDegrees(721.0), 0);
        // Starting at the prime meridian, travel westward around the globe
        assertEquals("-1", 359.0, WWMath.normalizeDegrees(-1.0), 0);
        assertEquals("-359", 1.0, WWMath.normalizeDegrees(-359.0), 0);
        assertEquals("-360", 0.0, WWMath.normalizeDegrees(-360.0), 0);
        assertEquals("-361", 359.0, WWMath.normalizeDegrees(-361.0), 0);
        assertEquals("-719", 1.0, WWMath.normalizeDegrees(-719.0), 0);
        assertEquals("-720", 0.0, WWMath.normalizeDegrees(-720.0), 0);
        assertEquals("-721", 359.0, WWMath.normalizeDegrees(-721.0), 0);
        // NaN should be propagated
        assertTrue("NaN", Double.isNaN(WWMath.normalizeDegrees(Double.NaN)));
    }


}