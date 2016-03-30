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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for Matrix3, a 3x3 square matrix in row, column order.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class Matrix3Test {

    @Before
    public void setup() {
        PowerMockito.mockStatic(Logger.class);
    }


    /**
     * @throws Exception
     */
    @Test
    public void testConstructor_Default() throws Exception {
        Matrix3 m1 = new Matrix3();

        assertNotNull(m1);
        assertArrayEquals("identity matrix", Matrix3.identity, m1.m, 0d);
    }

    @Test
    public void testConstructor_Doubles() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        final double[] elements = {11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d}; // identical

        assertNotNull(m1);
        assertArrayEquals("matrix components", elements, m1.m, 0d);
    }

    @Test
    public void testEquals() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d); // identical

        assertEquals("self", m1, m1);
        assertEquals("identical matrix", m2, m1);
    }

    @Test
    public void testEquals_Inequality() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 0d); // last element is different

        assertNotEquals("different matrix", m2, m1);
    }

    @Test
    public void testEquals_WithNull() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);

        assertNotEquals("null matrix", null, m1);
    }

    @Test
    public void testHashCode() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 0d);

        int hashCode1 = m1.hashCode();
        int hashCode2 = m2.hashCode();

        assertNotEquals("hash codes", hashCode1, hashCode2);
    }

    @Test
    public void testToString() throws Exception {
        String string = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d).toString();

        assertTrue("all elements in proper order", string.contains("[11.0, 12.0, 13.0], [21.0, 22.0, 23.0], [31.0, 32.0, 33.0]"));
    }


    @Test
    public void testSet() throws Exception {
        Matrix3 m1 = new Matrix3(); // matrix under test
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);

        Matrix3 m3 = m1.set(m2);

        assertEquals("set method argument", m2, m1);
        assertSame("fluent api result", m3, m1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        //PowerMockito.mockStatic(Logger.class);
        Matrix3 m1 = new Matrix3(); // matrix under test

        m1.set(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testSet_Doubles() throws Exception {
        final double m11 = 11d;
        final double m12 = 12d;
        final double m13 = 13d;
        final double m21 = 21d;
        final double m22 = 22d;
        final double m23 = 23d;
        final double m31 = 31d;
        final double m32 = 32d;
        final double m33 = 33d;
        Matrix3 m1 = new Matrix3(); // matrix under test

        Matrix3 m2 = m1.set(m11, m12, m13, m21, m22, m23, m31, m32, m33);

        assertEquals("m11", m11, m1.m[0], 0);
        assertEquals("m12", m12, m1.m[1], 0);
        assertEquals("m13", m13, m1.m[2], 0);
        assertEquals("m21", m21, m1.m[3], 0);
        assertEquals("m22", m22, m1.m[4], 0);
        assertEquals("m23", m23, m1.m[5], 0);
        assertEquals("m31", m31, m1.m[6], 0);
        assertEquals("m32", m32, m1.m[7], 0);
        assertEquals("m33", m33, m1.m[8], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetTranslation() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);  // identical
        final double dx = 5d;
        final double dy = 7d;

        Matrix3 m3 = m1.setTranslation(dx, dy);

        // Test for translation matrix form
        // [m11  m12  dx ]
        // [m21  m22  dy ]
        // [m31  m32  m33]
        assertEquals("m11", m2.m[0], m1.m[0], 0);
        assertEquals("m12", m2.m[1], m1.m[1], 0);
        assertEquals("m13", dx, m1.m[2], 0);
        assertEquals("m21", m2.m[3], m1.m[3], 0);
        assertEquals("m22", m2.m[4], m1.m[4], 0);
        assertEquals("m23", dy, m1.m[5], 0);
        assertEquals("m31", m2.m[6], m1.m[6], 0);
        assertEquals("m32", m2.m[7], m1.m[7], 0);
        assertEquals("m33", m2.m[8], m1.m[8], 0);
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testSetRotation() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);  // identical
        final double theta = 30d;    // rotation angle degrees
        final double c = Math.cos(Math.toRadians(theta));
        final double s = Math.sin(Math.toRadians(theta));

        Matrix3 m3 = m1.setRotation(theta);

        // Test for Euler rotation matrix
        // [cos(a) -sin(a)  m13]
        // [sin(a)  cos(a)  m23]
        // [  m31    m32    m33]
        assertEquals("m11", c, m1.m[0], 0);
        assertEquals("m12", -s, m1.m[1], 0);
        assertEquals("m13", m2.m[2], m1.m[2], 0);
        assertEquals("m21", s, m1.m[3], 0);
        assertEquals("m22", c, m1.m[4], 0);
        assertEquals("m23", m2.m[5], m1.m[5], 0);
        assertEquals("m31", m2.m[6], m1.m[6], 0);
        assertEquals("m32", m2.m[7], m1.m[7], 0);
        assertEquals("m33", m2.m[8], m1.m[8], 0);
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testSetScale() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);  // identical
        final double sx = 5d;
        final double sy = 7d;

        Matrix3 m3 = m1.setScale(sx, sy);

        // Test for scaling matrix form
        // [sx   m12  m13]
        // [m21  sy   m23]
        // [m31  m32  m33]
        assertEquals("m11", sx, m1.m[0], 0);
        assertEquals("m12", m2.m[1], m1.m[1], 0);
        assertEquals("m13", m2.m[2], m1.m[2], 0);
        assertEquals("m21", m2.m[3], m1.m[3], 0);
        assertEquals("m22", sy, m1.m[4], 0);
        assertEquals("m23", m2.m[5], m1.m[5], 0);
        assertEquals("m31", m2.m[6], m1.m[6], 0);
        assertEquals("m32", m2.m[7], m1.m[7], 0);
        assertEquals("m33", m2.m[8], m1.m[8], 0);
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testSetToIdentity() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);

        Matrix3 m2 = m1.setToIdentity();

        assertArrayEquals("identity matrix", Matrix3.identity, m1.m, 0d);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToTranslation() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        final double dx = 5d;
        final double dy = 7d;

        Matrix3 m2 = m1.setToTranslation(dx, dy);

        // Test for translation matrix form
        // [1 0 x]
        // [0 1 y]
        // [0 0 1]
        assertEquals("m11", 1d, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", dx, m1.m[2], 0);
        assertEquals("m21", 0d, m1.m[3], 0);
        assertEquals("m22", 1d, m1.m[4], 0);
        assertEquals("m23", dy, m1.m[5], 0);
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToRotation() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        final double theta = 30d;    // rotation angle degrees
        final double c = Math.cos(Math.toRadians(theta));
        final double s = Math.sin(Math.toRadians(theta));

        Matrix3 m2 = m1.setToRotation(theta);

        // Test for Euler (pronounced "oiler") rotation matrix
        // [cos(a) -sin(a)  0]
        // [sin(a)  cos(a)  0]
        // [  0       0     1]
        assertEquals("m11", c, m1.m[0], 0);
        assertEquals("m12", -s, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m21", s, m1.m[3], 0);
        assertEquals("m22", c, m1.m[4], 0);
        assertEquals("m23", 0d, m1.m[5], 0);
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToScale() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);
        final double sx = 5d;
        final double sy = 7d;

        Matrix3 m2 = m1.setToScale(sx, sy);

        // Test for scaling matrix form
        // [sx  0  0]
        // [0  sy  0]
        // [0   0  1]
        assertEquals("m11", sx, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m21", 0d, m1.m[3], 0);
        assertEquals("m22", sy, m1.m[4], 0);
        assertEquals("m23", 0d, m1.m[5], 0);
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToVerticalFlip() throws Exception {
        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);

        Matrix3 m2 = m1.setToVerticalFlip();

        // Sets this matrix to one that reflects about the x-axis and translates the y-axis origin.
        // [1  0  0]
        // [0 -1  1] <-- *
        // [0  0  1]
        assertEquals("m11", 1d, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m21", 0d, m1.m[3], 0);
        assertEquals("m22", -1d, m1.m[4], 0);   // *
        assertEquals("m23", 1d, m1.m[5], 0);    // *
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToMultiply() throws Exception {
        Matrix3 m1 = new Matrix3();
        Matrix3 a = new Matrix3(
            11d, 12d, 13d,
            21d, 22d, 23d,
            31d, 32d, 33d);
        Matrix3 b = new Matrix3(
            11d, 12d, 13d,
            21d, 22d, 23d,
            31d, 32d, 33d);

        Matrix3 m2 = m1.setToMultiply(a, b);

        // Test for result of a x b:
        //            1st Column                     2nd Column                     3rd Column
        // [ (a11*b11 + a12*b21 + a13*b31)  (a11*b12 + a12*b22 + a13*b32)  (a11*b13 + a12*b23 + a13*b33) ]
        // [ (a21*b11 + a22*b21 + a23*b31)  (a21*b12 + a22*b22 + a23*b32)  (a21*b13 + a22*b23 + a23*b33) ]
        // [ (a31*b11 + a32*b21 + a33*b31)  (a31*b12 + a32*b22 + a33*b32)  (a31*b13 + a32*b23 + a33*b33) ]
        //
        // 1st Column:
        assertEquals("m11", (a.m[0] * b.m[0]) + (a.m[1] * b.m[3]) + (a.m[2] * b.m[6]), m1.m[0], 0);
        assertEquals("m21", (a.m[3] * b.m[0]) + (a.m[4] * b.m[3]) + (a.m[5] * b.m[6]), m1.m[3], 0);
        assertEquals("m31", (a.m[6] * b.m[0]) + (a.m[7] * b.m[3]) + (a.m[8] * b.m[6]), m1.m[6], 0);
        // 2nd Column:
        assertEquals("m12", (a.m[0] * b.m[1]) + (a.m[1] * b.m[4]) + (a.m[2] * b.m[7]), m1.m[1], 0);
        assertEquals("m22", (a.m[3] * b.m[1]) + (a.m[4] * b.m[4]) + (a.m[5] * b.m[7]), m1.m[4], 0);
        assertEquals("m23", (a.m[6] * b.m[1]) + (a.m[7] * b.m[4]) + (a.m[8] * b.m[7]), m1.m[7], 0);
        // 3rd Column:
        assertEquals("m13", (a.m[0] * b.m[2]) + (a.m[1] * b.m[5]) + (a.m[2] * b.m[8]), m1.m[2], 0);
        assertEquals("m32", (a.m[3] * b.m[2]) + (a.m[4] * b.m[5]) + (a.m[5] * b.m[8]), m1.m[5], 0);
        assertEquals("m33", (a.m[6] * b.m[2]) + (a.m[7] * b.m[5]) + (a.m[8] * b.m[8]), m1.m[8], 0);
        //
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testMultiplyByTranslation() throws Exception {
        Matrix3 m1 = new Matrix3(); // identity matrix
        double dx = 2;
        double dy = 3;

        Matrix3 m2 = m1.multiplyByTranslation(dx, dy);

        // Test for translation matrix form
        // [1 0 x]
        // [0 1 y]
        // [0 0 1]
        assertEquals("m11", 1d, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", dx, m1.m[2], 0);
        assertEquals("m21", 0d, m1.m[3], 0);
        assertEquals("m22", 1d, m1.m[4], 0);
        assertEquals("m23", dy, m1.m[5], 0);
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testMultiplyByRotation() throws Exception {
        Matrix3 m1 = new Matrix3(); // identity matrix
        final double theta = 30d;   // rotation angle degrees
        final double c = Math.cos(Math.toRadians(theta));
        final double s = Math.sin(Math.toRadians(theta));

        Matrix3 m2 = m1.multiplyByRotation(theta);

        // Test for Euler rotation matrix
        // [cos(a) -sin(a)  0]
        // [sin(a)  cos(a)  0]
        // [  0       0     1]
        assertEquals("m11", c, m1.m[0], 0);
        assertEquals("m12", -s, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m21", s, m1.m[3], 0);
        assertEquals("m22", c, m1.m[4], 0);
        assertEquals("m23", 0d, m1.m[5], 0);
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testMultiplyByScale() throws Exception {
        Matrix3 m1 = new Matrix3();
        final double sx = 5d;
        final double sy = 7d;

        Matrix3 m2 = m1.multiplyByScale(sx, sy);

        // Test for scaling matrix form
        // [sx  0  0]
        // [0  sy  0]
        // [0   0  1]
        assertEquals("m11", sx, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m21", 0d, m1.m[3], 0);
        assertEquals("m22", sy, m1.m[4], 0);
        assertEquals("m23", 0d, m1.m[5], 0);
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testMultiplyByVerticalFlip() throws Exception {
        Matrix3 m1 = new Matrix3(); // identity matrix

        Matrix3 m2 = m1.multiplyByVerticalFlip();

        // Sets this matrix to one that reflects about the x-axis and translates the y-axis origin.
        // [1  0  0]
        // [0 -1  1] <-- *
        // [0  0  1]
        assertEquals("m11", 1d, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m21", 0d, m1.m[3], 0);
        assertEquals("m22", -1d, m1.m[4], 0);   // *
        assertEquals("m23", 1d, m1.m[5], 0);    // *
        assertEquals("m31", 0d, m1.m[6], 0);
        assertEquals("m32", 0d, m1.m[7], 0);
        assertEquals("m33", 1d, m1.m[8], 0);
        assertSame("fluent api result", m2, m1);

    }

    @Test
    public void testMultiplyByMatrix() throws Exception {
        Matrix3 m1 = new Matrix3(   // matrix under test
            11d, 12d, 13d,
            21d, 22d, 23d,
            31d, 32d, 33d);
        Matrix3 m2 = new Matrix3(   // multiplier
            11d, 12d, 13d,
            21d, 22d, 23d,
            31d, 32d, 33d);
        Matrix3 copy = new Matrix3();
        copy.set(m1);               // copy of m1 before its mutated

        Matrix3 m3 = m1.multiplyByMatrix(m2);

        // Test for result of a x b:
        //            1st Column                     2nd Column                     3rd Column
        // [ (a11*b11 + a12*b21 + a13*b31)  (a11*b12 + a12*b22 + a13*b32)  (a11*b13 + a12*b23 + a13*b33) ]
        // [ (a21*b11 + a22*b21 + a23*b31)  (a21*b12 + a22*b22 + a23*b32)  (a21*b13 + a22*b23 + a23*b33) ]
        // [ (a31*b11 + a32*b21 + a33*b31)  (a31*b12 + a32*b22 + a33*b32)  (a31*b13 + a32*b23 + a33*b33) ]
        //
        // 1st Column:
        assertEquals("m11", (copy.m[0] * m2.m[0]) + (copy.m[1] * m2.m[3]) + (copy.m[2] * m2.m[6]), m1.m[0], 0);
        assertEquals("m21", (copy.m[3] * m2.m[0]) + (copy.m[4] * m2.m[3]) + (copy.m[5] * m2.m[6]), m1.m[3], 0);
        assertEquals("m31", (copy.m[6] * m2.m[0]) + (copy.m[7] * m2.m[3]) + (copy.m[8] * m2.m[6]), m1.m[6], 0);
        // 2nd Column:
        assertEquals("m12", (copy.m[0] * m2.m[1]) + (copy.m[1] * m2.m[4]) + (copy.m[2] * m2.m[7]), m1.m[1], 0);
        assertEquals("m22", (copy.m[3] * m2.m[1]) + (copy.m[4] * m2.m[4]) + (copy.m[5] * m2.m[7]), m1.m[4], 0);
        assertEquals("m23", (copy.m[6] * m2.m[1]) + (copy.m[7] * m2.m[4]) + (copy.m[8] * m2.m[7]), m1.m[7], 0);
        // 3rd Column:
        assertEquals("m13", (copy.m[0] * m2.m[2]) + (copy.m[1] * m2.m[5]) + (copy.m[2] * m2.m[8]), m1.m[2], 0);
        assertEquals("m32", (copy.m[3] * m2.m[2]) + (copy.m[4] * m2.m[5]) + (copy.m[5] * m2.m[8]), m1.m[5], 0);
        assertEquals("m33", (copy.m[6] * m2.m[2]) + (copy.m[7] * m2.m[5]) + (copy.m[8] * m2.m[8]), m1.m[8], 0);
        //
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testMultiplyByMatrix_Doubles() throws Exception {
        // multipliers
        final double m11 = 11d;
        final double m12 = 12d;
        final double m13 = 13d;
        final double m21 = 21d;
        final double m22 = 22d;
        final double m23 = 23d;
        final double m31 = 31d;
        final double m32 = 32d;
        final double m33 = 33d;
        // matrix under test
        Matrix3 m1 = new Matrix3(   // matrix under test
            11d, 12d, 13d,
            21d, 22d, 23d,
            31d, 32d, 33d);
        Matrix3 copy = new Matrix3();
        copy.set(m1);               // copy of m1 before its mutated

        Matrix3 m3 = m1.multiplyByMatrix(
            m11, m12, m13,
            m21, m22, m23,
            m31, m32, m33);

        // Test for result of a x b:
        //            1st Column                     2nd Column                     3rd Column
        // [ (a11*b11 + a12*b21 + a13*b31)  (a11*b12 + a12*b22 + a13*b32)  (a11*b13 + a12*b23 + a13*b33) ]
        // [ (a21*b11 + a22*b21 + a23*b31)  (a21*b12 + a22*b22 + a23*b32)  (a21*b13 + a22*b23 + a23*b33) ]
        // [ (a31*b11 + a32*b21 + a33*b31)  (a31*b12 + a32*b22 + a33*b32)  (a31*b13 + a32*b23 + a33*b33) ]
        //
        // 1st Column:
        assertEquals("m11", (copy.m[0] * m11) + (copy.m[1] * m21) + (copy.m[2] * m31), m1.m[0], 0);
        assertEquals("m21", (copy.m[3] * m11) + (copy.m[4] * m21) + (copy.m[5] * m31), m1.m[3], 0);
        assertEquals("m31", (copy.m[6] * m11) + (copy.m[7] * m21) + (copy.m[8] * m31), m1.m[6], 0);
        // 2nd Column:
        assertEquals("m12", (copy.m[0] * m12) + (copy.m[1] * m22) + (copy.m[2] * m32), m1.m[1], 0);
        assertEquals("m22", (copy.m[3] * m12) + (copy.m[4] * m22) + (copy.m[5] * m32), m1.m[4], 0);
        assertEquals("m23", (copy.m[6] * m12) + (copy.m[7] * m22) + (copy.m[8] * m32), m1.m[7], 0);
        // 3rd Column:
        assertEquals("m13", (copy.m[0] * m13) + (copy.m[1] * m23) + (copy.m[2] * m33), m1.m[2], 0);
        assertEquals("m32", (copy.m[3] * m13) + (copy.m[4] * m23) + (copy.m[5] * m33), m1.m[5], 0);
        assertEquals("m33", (copy.m[6] * m13) + (copy.m[7] * m23) + (copy.m[8] * m33), m1.m[8], 0);
        //
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testTranspose() throws Exception {
        final double m11 = 11d;
        final double m12 = 12d;
        final double m13 = 13d;
        final double m21 = 21d;
        final double m22 = 22d;
        final double m23 = 23d;
        final double m31 = 31d;
        final double m32 = 32d;
        final double m33 = 33d;
        Matrix3 m1 = new Matrix3(m11, m12, m13, m21, m22, m23, m31, m32, m33); // matrix to be tested/transposed

        Matrix3 m2 = m1.transpose();

        assertEquals("m11", m11, m1.m[0], 0);
        assertEquals("m12", m21, m1.m[1], 0);
        assertEquals("m13", m31, m1.m[2], 0);
        assertEquals("m21", m12, m1.m[3], 0);
        assertEquals("m22", m22, m1.m[4], 0);
        assertEquals("m23", m32, m1.m[5], 0);
        assertEquals("m31", m13, m1.m[6], 0);
        assertEquals("m32", m23, m1.m[7], 0);
        assertEquals("m33", m33, m1.m[8], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testTransposeMatrix() throws Exception {
        final double m11 = 11d;
        final double m12 = 12d;
        final double m13 = 13d;
        final double m21 = 21d;
        final double m22 = 22d;
        final double m23 = 23d;
        final double m31 = 31d;
        final double m32 = 32d;
        final double m33 = 33d;
        Matrix3 m1 = new Matrix3(); // matrix under test
        Matrix3 m2 = new Matrix3(m11, m12, m13, m21, m22, m23, m31, m32, m33); // matrix to be transposed

        Matrix3 m3 = m1.transposeMatrix(m2);

        assertEquals("m11", m11, m1.m[0], 0);
        assertEquals("m12", m21, m1.m[1], 0);
        assertEquals("m13", m31, m1.m[2], 0);
        assertEquals("m21", m12, m1.m[3], 0);
        assertEquals("m22", m22, m1.m[4], 0);
        assertEquals("m23", m32, m1.m[5], 0);
        assertEquals("m31", m13, m1.m[6], 0);
        assertEquals("m32", m23, m1.m[7], 0);
        assertEquals("m33", m33, m1.m[8], 0);
        assertSame("fluent api result", m3, m1);
    }

    @Ignore("invert is not implemented at time of test")
    @Test
    public void testInvert() throws Exception {
        Matrix3 m1 = new Matrix3(   // matrix to be tested/inverted
            -4, -3, 3,
            0, 2, -2,
            1, 4, -1);
        Matrix3 mOriginal = new Matrix3(m1);

        Matrix3 m2 = m1.invert();
        Matrix3 mIdentity = new Matrix3(m1).multiplyByMatrix(mOriginal);

        assertArrayEquals("identity matrix array", Matrix3.identity, mIdentity.m, 0);
        assertSame("fluent api result", m2, m1);
    }

    @Ignore("invertMatrix was not implemented at time of test")
    @Test
    public void testInvertMatrix() throws Exception {
        Matrix3 m1 = new Matrix3();
        Matrix3 m2 = new Matrix3(   // matrix to be inverted
            -4, -3, 3,
            0, 2, -2,
            1, 4, -1);
        double det = computeDeterminant(m2);
        System.out.println(m2);
        System.out.println("Determinate: " + det);

        Matrix3 mInv = m1.invertMatrix(m2);
        Matrix3 mIdentity = mInv.multiplyByMatrix(m2);

        assertArrayEquals("identity matrix array", Matrix3.identity, mIdentity.m, 0);
        assertSame("fluent api result", mInv, m1);
    }

    @Test
    public void testTransposeToArray() throws Exception {

        Matrix3 m1 = new Matrix3(11d, 12d, 13d, 21d, 22d, 23d, 31d, 32d, 33d);

        float[] result = m1.transposeToArray(new float[9], 0);

        double[] expected = m1.transpose().m;
        for (int i = 0; i < 9; i++) {
            assertEquals(Integer.toString(i), expected[i], result[i], 0d);
        }

    }

    //////////////////////
    // Helper methods
    //////////////////////

    static private double computeDeterminant(Matrix3 matrix) {
        // |m11  m12  m13|
        // |m21  m22  m23| = m11(m22*m33 - m23*m32) + m12(m23*m31 - m21*m33) + m13(m21*m32 - m22*m31)
        // |m31  m32  m33|
        double[] m = matrix.m;
        double d
            = (m[0] * (m[4] * m[8] - m[5] * m[7])) //m11(m22*m33 - m23*m32)
            + (m[1] * (m[5] * m[6] - m[3] * m[8])) //m12(m23*m31 - m21*m33)
            + (m[2] * (m[3] * m[7] - m[4] * m[6]));//m13(m21*m32 - m22*m31)

        return d;
    }

    static private void prettyPrint(Matrix3 m) {
        System.out.println("[ " + m.m[0] + "  " + m.m[1] + "  " + m.m[2] + " ]");
        System.out.println("[ " + m.m[3] + "  " + m.m[4] + "  " + m.m[5] + " ]");
        System.out.println("[ " + m.m[6] + "  " + m.m[7] + "  " + m.m[8] + " ]");
    }


}