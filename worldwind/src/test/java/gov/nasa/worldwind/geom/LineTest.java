/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log

public class LineTest {

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testConstructor_Default() {
        Line line = new Line();

        assertNotNull(line);
    }

    @Test
    public void testConstructor_Copy() {
        Line line = new Line();

        Line copy = new Line(line);

        assertNotNull("copy", copy);
        assertEquals("copy equal to original", line, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_NullArgument() throws Exception {
        Line copy = new Line(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testConstructor_FromVectors() {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(0, 0, 1);

        Line line = new Line(origin, direction);

        assertNotNull("new line", line);
        assertEquals("origin", origin, line.origin);
        assertEquals("direction", direction, line.direction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_FromVectors_NullFirst() throws Exception {
        Line line = new Line(null, new Vec3());

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_FromVectors_NullSecond() throws Exception {
        Line line = new Line(new Vec3(), null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }


    @Test
    public void testEquals() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(0, 0, 1);

        Line line1 = new Line(origin, direction);
        Line line2 = new Line(origin, direction);

        assertEquals("origin", line1.origin, line2.origin);
        assertEquals("direction", line1.direction, line2.direction);
        assertTrue("equals", line1.equals(line2));
    }

    @Test
    public void testEquals_Null() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(0, 0, 1);

        Line line1 = new Line(origin, direction);

        assertFalse("inequality with null", line1.equals(null));
    }

    @Test
    public void testEquals_Inequality() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(0, 0, 1);

        Line line1 = new Line(origin, direction);
        Line line2 = new Line(direction, origin);   // reversed vectors

        assertFalse("not equals", line1.equals(line2));
    }

    @Test
    public void testHashCode() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction1 = new Vec3(0, 0, 1);
        Vec3 direction2 = new Vec3(0, 1, 0);
        Line line1 = new Line(origin, direction1);
        Line line2 = new Line(line1);
        Line line3 = new Line(origin, direction2);

        int hashCode1 = line1.hashCode();
        int hashCode2 = line2.hashCode();
        int hashCode3 = line3.hashCode();

        assertEquals(hashCode1, hashCode2);
        assertNotEquals(hashCode1, hashCode3);
    }

    @Test
    public void testToString() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(4, 5, 6);
        Line line = new Line(origin, direction);

        String string = line.toString();

        System.out.println(line);
        assertTrue("origin x", string.contains(Double.toString(line.origin.x)));
        assertTrue("origin y", string.contains(Double.toString(line.origin.y)));
        assertTrue("origin z", string.contains(Double.toString(line.origin.z)));
        assertTrue("direction x", string.contains(Double.toString(line.direction.x)));
        assertTrue("direction y", string.contains(Double.toString(line.direction.y)));
        assertTrue("direction z", string.contains(Double.toString(line.direction.z)));
    }

    @Test
    public void testSet() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(0, 0, 1);
        Line line = new Line();

        line.set(origin, direction);

        assertEquals("origin", origin, line.origin);
        assertEquals("direction", direction, line.direction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_NullFirst() throws Exception {
        Line line = new Line();

        line.set(null, new Vec3());

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_NullSecond() throws Exception {
        Line line = new Line();

        line.set(new Vec3(), null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }


    @Test
    public void testSetToSegment() throws Exception {
        Vec3 pointA = new Vec3(1, 2, 3);
        Vec3 pointB = new Vec3(4, 5, 6);
        Vec3 origin = new Vec3(pointA);
        Vec3 direction = new Vec3(pointB).subtract(pointA);
        Line line = new Line();

        line.setToSegment(pointA, pointB);

        assertEquals("origin", origin, line.origin);
        assertEquals("direction", direction, line.direction);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetToSegment_NullFirst() throws Exception {
        Line line = new Line();

        line.setToSegment(null, new Vec3());

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetToSegment_NullSecond() throws Exception {
        Line line = new Line();

        line.setToSegment(new Vec3(), null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testPointAt() throws Exception {
        Vec3 origin = new Vec3(1, 2, 3);
        Vec3 direction = new Vec3(4, 5, 6);
        Line line = new Line(origin, direction);
        double distance = -2.0;
        Vec3 expected = new Vec3(origin).add(new Vec3(direction).multiply(distance));

        Vec3 point = line.pointAt(distance, new Vec3());

        assertEquals("point at", expected, point);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPointAt_NullArgument() throws Exception {
        Line line = new Line();

        line.pointAt(0, null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testPointAt_NaN() throws Exception {
        Line line = new Line();

        Vec3 point = line.pointAt(Double.NaN, new Vec3());

        assertTrue(Double.isNaN(point.x));
        assertTrue(Double.isNaN(point.y));
        assertTrue(Double.isNaN(point.z));
    }

}