/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for Vec3, a three-component vector.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log
public class Vec3Test {

    final static double TOLERANCE = 1e-10;

    /**
     * Tests default constructor member initialization.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Default() throws Exception {

        Vec3 u = new Vec3();

        assertNotNull(u);
        assertEquals("x", 0d, u.x, 0d);
        assertEquals("y", 0d, u.y, 0d);
        assertEquals("z", 0d, u.z, 0d);
    }

    /**
     * Tests constructor member initialization from doubles.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Doubles() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = 5.3;

        Vec3 u = new Vec3(x1, y1, z1);

        assertNotNull(u);
        assertEquals("x", x1, u.x, 0d);
        assertEquals("y", y1, u.y, 0d);
        assertEquals("y", z1, u.z, 0d);
    }

    /**
     * Ensures equality of object and its members.
     *
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = 5.3;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x1, y1, z1);

        assertEquals("equality: x", x1, u.x, 0);
        assertEquals("equality: y", y1, u.y, 0);
        assertEquals("equality: z", z1, u.z, 0);
        assertEquals("equality", u, u); // equality with self
        assertEquals("equality", u, v); // equality with other
    }

    /**
     * Ensures inequality with null object.
     *
     * @throws Exception
     */
    @Test
    public void testEquals_WithNull() throws Exception {

        Vec3 u = new Vec3();
        Vec3 v = null;

        assertNotEquals("inequality with null", u, v);
    }

    /**
     * Ensures inequality of object and members.
     *
     * @throws Exception
     */
    @Test
    public void testEquals_Inequality() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = 5.3;
        final double val = 17d;

        Vec3 u = new Vec3(x1, y1, z1);
        // Vary a each component to assert equals() tests all components
        Vec3 vx = new Vec3(val, y1, z1);
        Vec3 vy = new Vec3(x1, val, z1);
        Vec3 vz = new Vec3(x1, y1, val);

        assertNotEquals("inequality: x component", u, vx);
        assertNotEquals("inequality: y component", u, vy);
        assertNotEquals("inequality: z component", u, vz);
    }

    /**
     * Ensures string output contains member representations.
     *
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = 5.3;

        Vec3 u = new Vec3(x1, y1, z1);
        String string = u.toString();

        assertTrue("x", string.contains(Double.toString(x1)));
        assertTrue("y", string.contains(Double.toString(y1)));
        assertTrue("z", string.contains(Double.toString(z1)));
    }

    /**
     * Ensures the correct computation of vector's magnitude, or length..
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = -5.3;

        Vec3 u = new Vec3(x1, y1, z1);
        double magnitude = u.magnitude();

        assertEquals("magnitude", Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1), magnitude, Double.MIN_VALUE);
    }

    /**
     * Ensures a zero length vector from default constructor.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_ZeroLength() throws Exception {

        Vec3 u = new Vec3();
        double magnitude = u.magnitude();

        assertEquals("zero length", 0d, magnitude, 0);
    }

    /**
     * Ensures length is NaN when a member is NaN.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_NaN() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = Double.NaN;

        Vec3 u = new Vec3(x1, y1, z1);
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
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = -5.3;

        Vec3 u = new Vec3(x1, y1, z1);
        double magnitudeSquared = u.magnitudeSquared();

        assertEquals("magnitude squared", x1 * x1 + y1 * y1 + z1 * z1, magnitudeSquared, Double.MIN_VALUE);
    }

    /**
     * Tests the distance (or displacement) between two opposing position-vectors.
     *
     * @throws Exception
     */
    @Test
    public void testDistanceTo() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = -5.3;
        final double x2 = -x1;
        final double y2 = -y1;
        final double z2 = -z1;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        double magnitude = u.magnitude();

        double distanceTo = u.distanceTo(v);
        assertEquals("distance", magnitude * 2, distanceTo, Double.MIN_VALUE);
    }

    /**
     * Ensures distanceTo with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDistanceTo_NullArgument() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
        double distanceTo = u.distanceTo(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Tests the squared distance (or displacement) between two opposing position-vectors.
     *
     * @throws Exception
     */
    @Test
    public void testDistanceToSquared() throws Exception {
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = -5.3;
        final double x2 = -x1;
        final double y2 = -y1;
        final double z2 = -z1;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        double magnitude = u.magnitude();

        double distanceToSquared = u.distanceToSquared(v);
        assertEquals("distance squared", Math.pow(magnitude * 2, 2), distanceToSquared, Double.MIN_VALUE);
    }

    /**
     * Ensures distanceToSquared with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDistanceToSquared_NullArgument() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
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
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = -5.3;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = u.set(x1, y1, z1);

        assertEquals("x", x1, u.x, 0d);
        assertEquals("y", y1, u.y, 0d);
        assertEquals("z", z1, u.z, 0d);
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
        final double x1 = 3.1;
        final double y1 = 4.2;
        final double z1 = -5.3;
        final double x2 = -6.4;
        final double y2 = -7.5;
        final double z2 = -8.6;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        Vec3 w = u.swap(v);

        assertEquals("u.x", x2, u.x, 0d);
        assertEquals("u.y", y2, u.y, 0d);
        assertEquals("u.z", z2, u.z, 0d);
        assertEquals("v.x", x1, v.x, 0d);
        assertEquals("v.y", y1, v.y, 0d);
        assertEquals("v.z", z1, v.z, 0d);
        // Assert fluent API returns u
        assertSame("w == u", u, w);
    }

    /**
     * Ensures swap with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSwap_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
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
        final double x1 = 3.1d;
        final double y1 = 4.3d;
        final double z1 = 5.5d;
        final double x2 = 6.2d;
        final double y2 = 7.4d;
        final double z2 = 8.6d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        Vec3 w = u.add(v);

        assertEquals("u.x", x1 + x2, u.x, 0d);
        assertEquals("u.y", y1 + y2, u.y, 0d);
        assertEquals("u.z", z1 + z2, u.z, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        assertEquals("v.z", z2, v.z, 0d);
        // Assert fluent API returns u
        assertSame("w == u", u, w);
    }

    /**
     * Ensures add with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
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
        final double x1 = 3.1d;
        final double y1 = 4.3d;
        final double z1 = 5.5d;
        final double x2 = 6.2d;
        final double y2 = 7.4d;
        final double z2 = 8.6d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        Vec3 w = u.subtract(v);

        assertEquals("u.x", x1 - x2, u.x, 0d);
        assertEquals("u.y", y1 - y2, u.y, 0d);
        assertEquals("u.z", z1 - z2, u.z, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        assertEquals("v.z", z2, v.z, 0d);
        // Assert fluent API returns u
        assertSame("w == u", u, w);
    }

    /**
     * Ensures subtract with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
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
        final double z1 = 5d;
        final double scalar = 6d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = u.multiply(scalar);

        assertEquals("u.x", x1 * scalar, u.x, 0d);
        assertEquals("u.y", y1 * scalar, u.y, 0d);
        assertEquals("u.z", z1 * scalar, u.z, 0d);
        // Assert fluent API returns u
        assertSame("v == u", u, v);
    }

    @Test
    public void testMultiplyByMatrix() throws Exception {
        double theta = 30d;
        double x = 2;
        double y = 3;
        double z = 0;
        // Rotate and translate a unit vector
        Matrix4 m = new Matrix4().multiplyByRotation(0, 0, 1, theta).setTranslation(x, y, z);

        Vec3 u = new Vec3(1, 0, 0).multiplyByMatrix(m);

        assertEquals("acos u.x", theta, Math.toDegrees(Math.acos(u.x - x)), 1e-10);
        assertEquals("asin u.y", theta, Math.toDegrees(Math.asin(u.y - y)), 1e-10);
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
        final double z1 = 5d;
        final double divisor = 6d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = u.divide(divisor);

        assertEquals("u.x", x1 / divisor, u.x, 0d);
        assertEquals("u.y", y1 / divisor, u.y, 0d);
        assertEquals("u.z", z1 / divisor, u.z, 0d);
        // Assert fluent API returns u
        assertSame("v == u", u, v);
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
        final double z1 = 5d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = u.negate();

        assertEquals("u.x", -x1, u.x, 0d);
        assertEquals("u.y", -y1, u.y, 0d);
        assertEquals("u.z", -z1, u.z, 0d);
        // Assert fluent API returns u
        assertSame("v == u", u, v);
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
        final double z1 = 5d;
        final double length = Math.sqrt(50);

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = u.normalize();
        double magnitude = u.magnitude();

        assertEquals("u.x", (1 / length) * x1, u.x, 0d);
        assertEquals("u.y", (1 / length) * y1, u.y, 0d);
        assertEquals("u.z", (1 / length) * z1, u.z, 0d);
        assertEquals("unit length", 1.0, magnitude, TOLERANCE);
        // Assert fluent API returns u
        assertSame("v == u", u, v);
    }

    /**
     * Ensures the correct dot product (or inner product) of two vectors and vectors are not mutated.
     *
     * @throws Exception
     */
    @Test
    public void testDot() throws Exception {
        final double x1 = 3.1d;
        final double y1 = 4.3d;
        final double z1 = 5.5d;
        final double x2 = 6.2d;
        final double y2 = 7.4d;
        final double z2 = 8.6d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);

        double dot = u.dot(v);

        assertEquals("dot", x1 * x2 + y1 * y2 + z1 * z2, dot, 0d);
        // Assert u is not altered
        assertEquals("u.x", x1, u.x, 0d);
        assertEquals("u.y", y1, u.y, 0d);
        assertEquals("u.z", z1, u.z, 0d);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        assertEquals("v.z", z2, v.z, 0d);
    }

    /**
     * Ensures dot with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDot_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
        double dot = u.dot(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    /**
     * Ensures the correct cross product (or outer product), arguments are not mutated, and the fluent API is
     * maintained.
     *
     * @throws Exception
     */
    @Test
    public void testCross() throws Exception {
        final double x1 = 1d;
        final double y1 = 3d;
        final double z1 = -4d;
        final double x2 = 2d;
        final double y2 = -5d;
        final double z2 = 8d;
        // expected result
        final double x3 = 4d;
        final double y3 = -16d;
        final double z3 = -11d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        Vec3 r = new Vec3(x3, y3, z3);
        Vec3 w = u.cross(v);

        assertEquals("u.x", y1 * z2 - z1 * y2, u.x, 0d);
        assertEquals("u.y", z1 * x2 - x1 * z2, u.y, 0d);
        assertEquals("u.z", x1 * y2 - y1 * x2, u.z, 0d);
        assertEquals("u == r", r, u);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        assertEquals("v.z", z2, v.z, 0d);
        // Assert fluent API returns u
        assertSame("w == u", u, w);
    }

    /**
     * Ensures cross with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCross_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);

        Vec3 u = new Vec3(3, 4, 5);
        u.cross(null);

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
        final double weight = 0.75;
        final double x1 = 3.1d;
        final double y1 = 4.3d;
        final double z1 = 5.5d;
        final double x2 = 6.2d;
        final double y2 = 7.4d;
        final double z2 = 8.6d;

        Vec3 u = new Vec3(x1, y1, z1);
        Vec3 v = new Vec3(x2, y2, z2);
        Vec3 w = u.mix(v, weight);

        assertEquals("u.x", x1 + ((x2 - x1) * weight), u.x, TOLERANCE);
        assertEquals("u.y", y1 + ((y2 - y1) * weight), u.y, TOLERANCE);
        assertEquals("u.z", z1 + ((z2 - z1) * weight), u.z, TOLERANCE);
        // Assert v is not altered
        assertEquals("v.x", x2, v.x, 0d);
        assertEquals("v.y", y2, v.y, 0d);
        assertEquals("v.z", z2, v.z, 0d);
        // Assert fluent API returns u
        assertSame("w == u", u, w);
    }

    /**
     * Ensures mix with null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMix_WithNull() throws Exception {
        PowerMockito.mockStatic(Logger.class);
        final double weight = 5d;

        Vec3 u = new Vec3(3, 4, 5);
        u.mix(null, weight);

        fail("Expected an IllegalArgumentException to be thrown.");
    }
}

