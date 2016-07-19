/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RangeTest {

    @Test
    public void testConstructor_Default() throws Exception {
        Range range = new Range();

        assertNotNull(range);
        assertEquals("lower", 0, range.lower);
        assertEquals("upper", 0, range.upper);
    }

    @Test
    public void testConstructor_Copy() throws Exception {
        Range original = new Range(1, 2);
        Range range = new Range(original);

        assertNotNull(range);
        assertEquals("lower", 1, range.lower);
        assertEquals("upper", 2, range.upper);
    }

    @Test
    public void testConstructor_Parameters() throws Exception {
        Range range = new Range(1, 2);

        assertNotNull(range);
        assertEquals("lower", 1, range.lower);
        assertEquals("upper", 2, range.upper);
    }

    @Test
    public void testEquals() throws Exception {
        Range range1 = new Range(1, 2);
        Range range2 = new Range(1, 2);

        assertTrue("equals", range1.equals(range2));
    }

    @Test
    public void testEquals_Null() throws Exception {
        Range range = new Range(1, 2);

        assertFalse("inequality with null", range.equals(null));
    }

    @Test
    public void testEquals_Inequality() throws Exception {
        Range range1 = new Range(1, 2);
        Range range2 = new Range(2, 1);

        assertFalse("not equals", range1.equals(range2));
    }

    @Test
    public void testHashCode() throws Exception {
        Range range1 = new Range(1, 2);
        Range range2 = new Range(range1);
        Range range3 = new Range(2, 1);

        int hashCode1 = range1.hashCode();
        int hashCode2 = range2.hashCode();
        int hashCode3 = range3.hashCode();

        assertEquals(hashCode1, hashCode2);
        assertNotEquals(hashCode1, hashCode3);
    }

    @Test
    public void testToString() throws Exception {
        Range range = new Range(1, 2);
        String string = range.toString();

        //System.out.println(range);
        assertTrue("lower", string.contains(Integer.toString(range.lower)));
        assertTrue("upper", string.contains(Integer.toString(range.upper)));
    }

    @Test
    public void testSet_Parameters() throws Exception {
        Range range = new Range();
        range.set(1, 2);

        assertEquals("lower", 1, range.lower);
        assertEquals("upper", 2, range.upper);
    }

    @Test
    public void testSet_Copy() throws Exception {
        Range original = new Range(1, 2);
        Range range = new Range();
        range.set(original);

        assertTrue("not the same reference", original != range);
        assertEquals("lower", 1, range.lower);
        assertEquals("upper", 2, range.upper);
    }

    @Test
    public void testSetEmpty() throws Exception {
        Range range = new Range(1, 2);
        range.setEmpty();

        assertEquals("lower", 0, range.lower);
        assertEquals("upper", 0, range.upper);
    }

    @Test
    public void testIsEmpty() throws Exception {
        Range range1 = new Range();
        Range range2 = new Range(1, 2);

        assertTrue("range is empty", range1.isEmpty());
        assertFalse("range is not empty", range2.isEmpty());
    }

    @Test
    public void testLength() throws Exception {
        Range range = new Range(1, 2);

        assertEquals("length", 1, range.length());
    }

    @Test
    public void testLength_Empty() throws Exception {
        Range range = new Range();

        assertEquals("length", 0, range.length());
    }
}
