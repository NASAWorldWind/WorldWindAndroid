/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Test;

import static org.junit.Assert.*;

public class Vec3Test {
    @Test
    public void testConstructor_default() throws Exception {
        Vec3 vec3 = new Vec3();
        assertNotNull(vec3);
        assertEquals("x", 0d, vec3.x, 0d);
        assertEquals("y", 0d, vec3.y, 0d);
    }
}

