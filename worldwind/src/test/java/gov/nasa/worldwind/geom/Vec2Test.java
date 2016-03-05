/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Test;

import static org.junit.Assert.*;

public class Vec2Test {
    @Test
    public void testConstructor_default() throws Exception {
        Vec2 vec2 = new Vec2();
        assertNotNull(vec2);
        assertEquals("x", 0d, vec2.x, 0d);
        assertEquals("y", 0d, vec2.y, 0d);
    }
}

