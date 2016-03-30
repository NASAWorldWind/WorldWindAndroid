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

/**
 * Unit tests for Vec2: a two-component vector.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log
public class Vec2Test {

    final static double X = Math.PI;

    final static double Y = Math.E;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Logger.class);
    }

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
     * Tests constructor member initialization from doubles.
     *
     * @throws Exception
     */
    @Test
    public void testConstructor_Copy() throws Exception {
        Vec2 u = new Vec2(X, Y);
        Vec2 copy = new Vec2(u);

        assertNotNull(copy);
        assertEquals("x", X, copy.x, 0d);
        assertEquals("y", Y, copy.y, 0d);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_WithNull() throws Exception {
        Vec2 u = new Vec2(null);

        fail("Expected an IllegalArgumentException to be thrown.");
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

        assertNotEquals("inequality", u, null);
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
     * Ensures array elements match converted vector components.
     *
     * @throws Exception
     */
    @Test
    public void testToArray() throws Exception {
        Vec2 u = new Vec2(X, Y);

        float[] a = u.toArray(new float[2], 0);

        assertEquals("u.x", (float) u.x, a[0], 0f);
        assertEquals("u.y", (float) u.y, a[1], 0f);
    }

    /**
     * Ensures the correct offset is written to the array and that the other elements are not altered.
     *
     * @throws Exception
     */
    @Test
    public void testToArray_Offset() throws Exception {
        Vec2 u = new Vec2(X, Y);
        int offset = 2;
        float[] array = new float[6];

        u.toArray(array, offset);

        for (int i = 0; i < offset; i++) {
            assertEquals("element = 0", 0, array[i], 0f);
        }
        assertEquals("u.x", (float) u.x, array[offset], 0f);
        assertEquals("u.y", (float) u.y, array[offset + 1], 0f);
        for (int i = offset + 2; i < array.length; i++) {
            assertEquals("element = 0", 0, array[i], 0f);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToArray_ArrayTooSmall() throws Exception {
        Vec2 u = new Vec2(X, Y);

        u.toArray(new float[2], 1);

        fail("expected IllegalArgumentException");
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
     * Tests the limits of a really small vector.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_ReallySmall() throws Exception {
        // Between 1e-154 and 1e-162, the accuracy of the magnitude drops off
        Vec2 small = new Vec2(1e-154, 0);
        Vec2 tooSmall = new Vec2(1e-155, 0);
        Vec2 wayTooSmall = new Vec2(1e-162, 0);

        assertEquals("small: magnitude = x", small.x, small.magnitude(), 0);
        assertNotEquals("too small: magnitude <> x", tooSmall.x, tooSmall.magnitude(), 0);
        assertEquals("way too small: magnitude = 0", 0, wayTooSmall.magnitude(), 0);
    }

    /**
     * Tests the limits of a really big vector.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitude_ReallyBig() throws Exception {
        Vec2 big = new Vec2(1e-154, 0);
        Vec2 tooBig = new Vec2(1e-155, 0);

        assertEquals("big: magnitude = x", big.x, big.magnitude(), 0);
        assertNotEquals("too big: magnitude = Infinity", Double.POSITIVE_INFINITY, tooBig.magnitude(), 0);
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
     * Tests the squared length of a vector to a well known right-triangle.
     *
     * @throws Exception
     */
    @Test
    public void testMagnitudeSquared() throws Exception {
        Vec2 u = new Vec2(3, 4);

        double magnitudeSquared = u.magnitudeSquared();

        assertEquals("3,4,5 hypotenuse squared", 25d, magnitudeSquared, Double.MIN_VALUE);
    }

    @Test
    public void testMagnitudeSquared_NaN() throws Exception {
        Vec2 u = new Vec2(3d, Double.NaN);
        Vec2 v = new Vec2(Double.NaN, 4d);

        double uMagnitudeSquared = u.magnitudeSquared();
        double vMagnitudeSquared = v.magnitudeSquared();

        assertTrue("u NaN", Double.isNaN(uMagnitudeSquared));
        assertTrue("v NaN", Double.isNaN(vMagnitudeSquared));
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
     * Ensures propagation of NaN values.
     *
     * @throws Exception
     */
    @Test
    public void testDistanceToSquared_NaN() throws Exception {
        Vec2 u = new Vec2(3, 4);

        assertTrue("1st NaN", Double.isNaN(u.distanceToSquared(new Vec2(Double.NaN, 4d))));
        assertTrue("2nd NaN", Double.isNaN(u.distanceToSquared(new Vec2(3d, Double.NaN))));
    }


    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDistanceToSquared_NullArgument() throws Exception {
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

        Vec2 v = u.set(new Vec2(X, Y));

        assertEquals("x", X, u.x, 0d);
        assertEquals("y", Y, u.y, 0d);
        assertEquals("v == u", u, v);
    }

    @Test
    public void testSet_Doubles() throws Exception {
        Vec2 u = new Vec2();

        Vec2 v = u.set(X, Y);

        assertEquals("x", X, u.x, 0d);
        assertEquals("y", Y, u.y, 0d);
        assertEquals("v == u", u, v);
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        Vec2 u = new Vec2();

        u.set(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }


    /**
     * Ensures the components of the two vectors are swapped and the fluent API is maintained.
     *
     * @throws Exception
     */
    @Test
    public void testSwap() throws Exception {
        final double ux = 3d;
        final double uy = 4d;
        final double vx = 5d;
        final double vy = 6d;

        Vec2 u = new Vec2(ux, uy);
        Vec2 v = new Vec2(vx, vy);
        Vec2 w = u.swap(v);

        assertEquals("u.x", vx, u.x, 0d);
        assertEquals("u.y", vy, u.y, 0d);
        assertEquals("v.x", ux, v.x, 0d);
        assertEquals("v.y", uy, v.y, 0d);
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
        final double ux = 3d;
        final double uy = 4d;
        final double vx = 5d;
        final double vy = 6d;

        Vec2 u = new Vec2(ux, uy);
        Vec2 v = new Vec2(vx, vy);
        Vec2 w = u.add(v);

        assertEquals("u.x", ux + vx, u.x, 0d);
        assertEquals("u.y", uy + vy, u.y, 0d);
        // Assert v is not altered
        assertEquals("v.x", vx, v.x, 0d);
        assertEquals("v.y", vy, v.y, 0d);
        // Assert fluent API returns u
        assertEquals("w == u", u, w);
    }

    @Test
    public void testAdd_NaN() throws Exception {
        Vec2 u = new Vec2();
        Vec2 v = new Vec2();

        assertTrue("1st Nan", hasNaN(u.add(new Vec2(Double.NaN, 4d))));
        assertTrue("2nd Nan", hasNaN(v.add(new Vec2(3d, Double.NaN))));
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAdd_WithNull() throws Exception {
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

    @Test
    public void testSubtract_NaN() throws Exception {
        Vec2 u = new Vec2();
        Vec2 v = new Vec2();

        assertTrue("1st Nan", hasNaN(u.subtract(new Vec2(Double.NaN, 4d))));
        assertTrue("2nd Nan", hasNaN(v.subtract(new Vec2(3d, Double.NaN))));
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubtract_WithNull() throws Exception {
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

    @Test
    public void testMultiply_NaN() throws Exception {

        assertTrue("1st Nan", hasNaN(new Vec2(3, 4).multiply(Double.NaN)));
        assertTrue("2nd Nan", hasNaN(new Vec2(Double.NaN, 4).multiply(5)));
        assertTrue("2nd Nan", hasNaN(new Vec2(3, Double.NaN).multiply(5)));
    }


    /**
     * Ensures the correct vector component values after it is by a rotation and translation matrix
     *
     * @throws Exception
     */
    @Test
    public void testMultiplyByMatrix() throws Exception {
        double theta = 30d;
        double x = 2;
        double y = 3;
        Matrix3 m = new Matrix3().multiplyByRotation(theta).setTranslation(x, y);

        // Rotate and translate a unit vector
        Vec2 u = new Vec2(1, 0).multiplyByMatrix(m);

        assertEquals("acos u.x", theta, Math.toDegrees(Math.acos(u.x - x)), 1e-10);
        assertEquals("asin u.y", theta, Math.toDegrees(Math.asin(u.y - y)), 1e-10);
    }

    @Test
    public void testMultiplyByMatrix_NaN() throws Exception {
        Matrix3 m = new Matrix3().multiplyByRotation(Double.NaN);

        assertTrue("Rotation NaN", hasNaN(new Vec2(1, 0).multiplyByMatrix(m)));
    }

    /**
     * Ensures null argument is handled correctly.
     *
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testMultiplyByMatrix_WithNull() throws Exception {
        Vec2 u = new Vec2();

        u.multiplyByMatrix(null);

        fail("Expected an IllegalArgumentException to be thrown.");
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
        assertEquals("v == u", u, v);
    }

    @Test
    public void testDivide_ByZero() throws Exception {
        assertTrue("Infinity", hasInfinite(new Vec2(3, 4).divide(0)));
    }

    @Test
    public void testDivide_NaN() throws Exception {

        assertTrue("1st Nan", hasNaN(new Vec2(3, 4).divide(Double.NaN)));
        assertTrue("2nd Nan", hasNaN(new Vec2(Double.NaN, 4).divide(5)));
        assertTrue("2nd Nan", hasNaN(new Vec2(3, Double.NaN).divide(5)));
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

        assertEquals("u.x", (1 / length) * x1, u.x, 1e-15);
        assertEquals("u.y", (1 / length) * y1, u.y, 1e-15);
        assertEquals("magnitude", 1.0, magnitude, 1e-15);
        assertEquals("v == u", u, v);
    }

    /**
     * Tests the limits of normalizing a really small vector along the x axis. The length limit is 1e-154.
     *
     * @throws Exception
     */
    @Test
    public void testNormalize_ReallySmall() throws Exception {

        Vec2 small = new Vec2(1e-154, 0).normalize();
        Vec2 tooSmall = new Vec2(1e-155, 0).normalize();

        assertEquals("small: normal = 1.0", 1.0, small.x, 0);
        assertNotEquals("too small: normal <> 1.0", 1.0, tooSmall.x, 0);
    }

    /**
     * Tests the limits of normalizing a really big vector long the x axis. The length limit is 1e154.
     *
     * @throws Exception
     */
    @Test
    public void testNormalize_ReallyBig() throws Exception {

        Vec2 big = new Vec2(1e154, 0).normalize();
        Vec2 tooBig = new Vec2(1e155, 0).normalize();

        assertEquals("big: normal = 1.0", 1.0, big.x, 0);
        assertEquals("too big: normal = 0.0", 0.0, tooBig.x, 0);
    }


    @Test
    public void testNormalize_BigAndSmallComponents() throws Exception {

        Vec2 extreme = new Vec2(1e154, 1e-154).normalize();
        Vec2 tooExtreme = new Vec2(1e155, 1e-155).normalize();

        assertEquals("extreme: normal = 1.0", 1.0, extreme.x, 0);
        assertEquals("too extreme: normal = 0.0", 0.0, tooExtreme.x, 0);
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
        assertEquals("v == u", u, v);
    }

    @Test
    public void testNegate_NaN() throws Exception {

        assertTrue("2nd Nan", hasNaN(new Vec2(Double.NaN, 4).negate()));
        assertTrue("2nd Nan", hasNaN(new Vec2(3, Double.NaN).negate()));
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
        final double weight = 5d;

        Vec2 u = new Vec2();
        u.mix(null, weight);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    ////////////////////////////////////////////
    //            Helper Methods
    ////////////////////////////////////////////

    private static boolean hasNaN(Vec2 v) {
        return Double.isNaN(v.x) || Double.isNaN(v.y);
    }

    private static boolean hasInfinite(Vec2 v) {
        return Double.isInfinite(v.x) || Double.isInfinite(v.y);
    }
}

