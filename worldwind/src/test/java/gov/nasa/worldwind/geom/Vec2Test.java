/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.*;

/**
 * Unit tests for Vec2: a two-component vector.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log
public class Vec2Test {

    final static double X = Math.PI;

    final static double Y = Math.E;

    /**
     * Tests default constructor member initialization.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Default() throws Exception {
        Vec2 u = new Vec2();
        assertNotNull(u);
        assertEquals("x", 0d, u.x, 0d);
        assertEquals("y", 0d, u.y, 0d);
    }


    /**
     * Tests constructor member initialization from doubles.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Doubles() throws Exception {
        Vec2 u = new Vec2(X, Y);

        assertNotNull(u);
        assertEquals("x", X, u.x, 0d);
        assertEquals("y", Y, u.y, 0d);
    }

    /**
     * Ensures equality of object and its members.
     *
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {

        Vec2 u = new Vec2(X, Y);
        Vec2 v = new Vec2(X, Y);

        assertEquals("equality", u, u); // equality with self
        assertEquals("equality", u, v); // equality with other
        assertEquals("equality: x", X, u.x, 0);
        assertEquals("equality: y", Y, u.y, 0);
    }

    /**
     * Ensures inequality of object and members.
     *
     * @throws Exception
     */
    @Test
    public void testEquals_Inequality() throws Exception {

        Vec2 u = new Vec2(X, Y);
        Vec2 v = new Vec2(X, X);
        Vec2 w = new Vec2(Y, Y);

        assertNotEquals("inequality", u, v);
        assertNotEquals("inequality: y", u, v);
        assertNotEquals("inequality: x", u, w);
    }

    /**
     * Ensures string output contains member representations.
     *
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {

        Vec2 u = new Vec2(X, Y);
        String string = u.toString();

        assertTrue("x", string.contains(Double.toString(X)));
        assertTrue("y", string.contains(Double.toString(Y)));
    }

    /**
     * Tests the length of the vector with well known right-triangle hypotenuse.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude() throws Exception {

        Vec2 u = new Vec2(3d, 4d);
        double magnitude = u.magnitude();

        assertEquals("hypotenuse", 5d, magnitude, Double.MIN_VALUE);
    }

    /**
     * Ensures positive length with negative members.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_NegativeValues() throws Exception {

        Vec2 u = new Vec2(-3d, 4d);
        Vec2 v = new Vec2(-3d, -4d);

        double uMagnitude = u.magnitude();
        double vMagnitude = v.magnitude();

        assertEquals("negative x", 5d, uMagnitude, Double.MIN_VALUE);
        assertEquals("negative x and y", 5d, vMagnitude, Double.MIN_VALUE);
    }

    /**
     * Ensures zero length from default constructor.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_ZeroLength() throws Exception {

        Vec2 u = new Vec2();
        double magnitude = u.magnitude();

        assertEquals("zero length", 0d, magnitude, Double.MIN_VALUE);
    }

    /**
     * Ensures length is NaN when a member is NaN.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_NaN() throws Exception {

        Vec2 u = new Vec2(Double.NaN, 0);
        double magnitude = u.magnitude();

        assertTrue("Nan", Double.isNaN(magnitude));
    }

    /**
     * Tests the squared length of a vector with a well known right-triangle.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitudeSquared() throws Exception {

        Vec2 u = new Vec2(3, 4);
        double magnitudeSquared = u.magnitudeSquared();

        assertEquals("3,4,5 hypotenuse squared", 25d, magnitudeSquared, Double.MIN_VALUE);
    }

    /**
     * Tests the distance (or displacement) between two opposing position-vectors using a well known right triangle.
     *
     * @throws Exception
     */
    @Test
    public void testDistanceTo() throws Exception {

        Vec2 u = new Vec2(3, 4);
        Vec2 v = new Vec2(-3, -4);

        double distanceTo = u.distanceTo(v);
        assertEquals("3,4,5 hypotenuse length doubled", 10d, distanceTo, Double.MIN_VALUE);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDistanceTo_NullArgument() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);

        Vec2 u = new Vec2();
        double distanceTo = u.distanceTo(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Tests the squared distance (or displacement) between two opposing position-vectors using a well known right
     * triangle.
     *
     * @throws Exception
     */
    @Test
    public void testDistanceToSquared() throws Exception {

        Vec2 u = new Vec2(3, 4);
        Vec2 v = new Vec2(-3, -4);

        double distanceTo = u.distanceToSquared(v);
        assertEquals("3,4,5 hypotenuse length doubled and squared", 100d, distanceTo, Double.MIN_VALUE);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDistanceToSquared_NullArgument() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);

        Vec2 u = new Vec2();
        double distanceToSquared = u.distanceToSquared(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures the members are equal to the set method arguments.
     *
     * @throws Exception
     */
    @Test
    public void testSet() throws Exception {

        Vec2 u = new Vec2();
        Vec2 v = u.set(X, Y);

        assertEquals("x", X, u.x, 0d);
        assertEquals("y", Y, u.y, 0d);
        // Assert fluent API returns u
        assertEquals("v == u", u, v);
    }

    /**
     * Ensures the components of the two vectors are swapped and the fluent API is maintained.
     *
     * @throws Exception
     */
    @Test
    public void testSwap() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double x2 = 5d;
        final double y2 = 6d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = new Vec2(x2, y2);
        Vec2 w = u.swap(v);

        assertEquals("u.x", x2, u.x, 0d);
        assertEquals("u.y", y2, u.y, 0d);
        assertEquals("v.x", x1, v.x, 0d);
        assertEquals("v.y", y1, v.y, 0d);
        // Assert fluent API returns u
        assertEquals("w == u", u, w);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSwap_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec2 u = new Vec2();
        u.swap(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures the correct addition of two vectors, arguments are not mutated, and the proper fluent API result.
     *
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double x2 = 5d;
        final double y2 = 6d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = new Vec2(x2, y2);
        Vec2 w = u.add(v);

        assertEquals("u.x", x1 + x2, u.x, 0d);
        assertEquals("u.y", y1 + y2, u.y, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        // Assert fluent API returns u
        assertEquals("w == u", u, w);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec2 u = new Vec2();
        u.add(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }


    /**
     * Ensures the correct subtraction of two vectors, arguments are not mutated, and the proper fluent API result.
     *
     * @throws Exception
     */
    @Test
    public void testSubtract() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double x2 = 5d;
        final double y2 = 6d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = new Vec2(x2, y2);
        Vec2 w = u.subtract(v);

        assertEquals("u.x", x1 - x2, u.x, 0d);
        assertEquals("u.y", y1 - y2, u.y, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        // Assert fluent API returns u
        assertEquals("w == u", u, w);

    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec2 u = new Vec2();
        u.subtract(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures the correct multiplication of a vector and a scalar, and the proper fluent API result.
     *
     * @throws Exception
     */
    @Test
    public void testMultiply() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double scalar = 5d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = u.multiply(scalar);

        assertEquals("u.x", x1 * scalar, u.x, 0d);
        assertEquals("u.y", y1 * scalar, u.y, 0d);
        // Assert fluent API returns u
        assertEquals("v == u", u, v);

    }


    @Ignore("not implemented")
    @Test
    public void testMultiplyByMatrix() throws Exception {

        fail("The test case is a stub.");

    }

    /**
     * Ensures the correct division of a vector by a divisor, and the proper fluent API result.
     *
     * @throws Exception
     */
    @Test
    public void testDivide() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double divisor = 5d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = u.divide(divisor);

        assertEquals("u.x", x1 / divisor, u.x, 0d);
        assertEquals("u.y", y1 / divisor, u.y, 0d);
        // Assert fluent API returns u
        assertEquals("v == u", u, v);

    }

    /**
     * Ensures the correct negation of the components and the proper fluent API result.
     *
     * @throws Exception
     */
    @Test
    public void testNegate() throws Exception {
        final double x1 = 3d;
        final double y1 = -4d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = u.negate();

        assertEquals("u.x", -x1, u.x, 0d);
        assertEquals("u.y", -y1, u.y, 0d);
        // Assert fluent API returns u
        assertEquals("v == u", u, v);

    }

    /**
     * Ensures the correct unit vector components and length and the proper fluent API result.
     *
     * @throws Exception
     */
    @Test
    public void testNormalize() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double length = 5d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = u.normalize();
        double magnitude = u.magnitude();

        assertEquals("u.x", (1 / length) * x1, u.x, 0d);
        assertEquals("u.y", (1 / length) * y1, u.y, 0d);
        assertEquals("magnitude", 1.0, magnitude, 0d);
        // Assert fluent API returns u
        assertEquals("v == u", u, v);
    }

    /**
     * Ensures the correct dot product of two vectors and vectors are not mutated.
     *
     * @throws Exception
     */
    @Test
    public void testDot() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double x2 = 5d;
        final double y2 = 6d;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = new Vec2(x2, y2);
        double dot = u.dot(v);

        assertEquals("dot", x1 * x2 + y1 * y2, dot, 0d);
        // Assert u is not altered
        assertEquals("u.x", x1, u.x, 0d);
        assertEquals("u.y", y1, u.y, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDot_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec2 u = new Vec2();
        double dot = u.dot(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures the correct interpolation between two vectors, arguments are not mutated, and the proper fluent API
     * result.
     *
     * @throws Exception
     */
    @Test
    public void testMix() throws Exception {
        final double x1 = 3d;
        final double y1 = 4d;
        final double x2 = 5d;
        final double y2 = 6d;
        final double weight = 0.75;

        Vec2 u = new Vec2(x1, y1);
        Vec2 v = new Vec2(x2, y2);
        Vec2 w = u.mix(v, weight);

        assertEquals("u.x", x1 + ((x2 - x1) * weight), u.x, 0d);
        assertEquals("u.y", y1 + ((y2 - y1) * weight), u.y, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        // Assert fluent API returns u
        assertEquals("w == u", u, w);

    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMix_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);
        final double weight = 5d;

        Vec2 u = new Vec2();
        u.mix(null, weight);

        fail("Expected an IllegalArgumentException to be thrown.");
    }
}

