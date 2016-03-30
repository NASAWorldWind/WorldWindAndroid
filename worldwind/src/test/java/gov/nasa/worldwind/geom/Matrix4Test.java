/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.After;
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
 * Unit tests for Matrix4, a 4x4 square matrix in row, column order.
 */
@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class Matrix4Test {

    static final double M_11 = 11d;

    static final double M_12 = 12d;

    static final double M_13 = 13d;

    static final double M_14 = 14d;

    static final double M_21 = 21d;

    static final double M_22 = 22d;

    static final double M_23 = 23d;

    static final double M_24 = 24d;

    static final double M_31 = 31d;

    static final double M_32 = 32d;

    static final double M_33 = 33d;

    static final double M_34 = 34d;

    static final double M_41 = 41d;

    static final double M_42 = 42d;

    static final double M_43 = 43d;

    static final double M_44 = 44d;

    static final double TOLERANCE = 1e-10;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Logger.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor_Default() throws Exception {
        Matrix4 m1 = new Matrix4();

        assertNotNull("matrix not null", m1);
        assertArrayEquals("identity matrix", Matrix4.identity, m1.m, 0d);
    }

    @Test
    public void testConstructor_Copy() throws Exception {
        Matrix4 original = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 copy = new Matrix4(original);   // matrix under test

        assertNotNull("matrix not null", copy);
        assertArrayEquals("copy array equals original", original.m, copy.m, 0d);
        assertEquals("copy equals original", original, copy);
    }

    @Test
    public void testConstructor_Doubles() throws Exception {
        Matrix4 m1 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        assertNotNull("matrix not null", m1);
        assertEquals("m11", M_11, m1.m[0], 0);
        assertEquals("m12", M_12, m1.m[1], 0);
        assertEquals("m13", M_13, m1.m[2], 0);
        assertEquals("m14", M_14, m1.m[3], 0);
        assertEquals("m21", M_21, m1.m[4], 0);
        assertEquals("m22", M_22, m1.m[5], 0);
        assertEquals("m23", M_23, m1.m[6], 0);
        assertEquals("m24", M_24, m1.m[7], 0);
        assertEquals("m31", M_31, m1.m[8], 0);
        assertEquals("m32", M_32, m1.m[9], 0);
        assertEquals("m33", M_33, m1.m[10], 0);
        assertEquals("m34", M_34, m1.m[11], 0);
        assertEquals("m41", M_41, m1.m[12], 0);
        assertEquals("m42", M_42, m1.m[13], 0);
        assertEquals("m43", M_43, m1.m[14], 0);
        assertEquals("m44", M_44, m1.m[15], 0);
    }

    @Test
    public void testEquals() throws Exception {
        Matrix4 m1 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 m2 = new Matrix4(   // identical
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        assertEquals("self", m1, m1);
        assertArrayEquals("identical array", m2.m, m1.m, 0d);
        assertEquals("identical matrix", m2, m1);
    }

    @Test
    public void testHashCode() throws Exception {
        final double x44 = -44d; // different value
        Matrix4 m1 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 m2 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, x44);

        int hashCode1 = m1.hashCode();
        int hashCode2 = m2.hashCode();

        assertNotEquals("hash codes", hashCode1, hashCode2);
    }

    @Ignore("not implemented")
    @Test
    public void testToString() throws Exception {

        fail("The test case is a stub.");

    }

    @Test
    public void testSet() throws Exception {
        Matrix4 m1 = new Matrix4(); // matrix under test

        Matrix4 m2 = m1.set(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        assertEquals("m11", M_11, m1.m[0], 0);
        assertEquals("m12", M_12, m1.m[1], 0);
        assertEquals("m13", M_13, m1.m[2], 0);
        assertEquals("m14", M_14, m1.m[3], 0);
        assertEquals("m21", M_21, m1.m[4], 0);
        assertEquals("m22", M_22, m1.m[5], 0);
        assertEquals("m23", M_23, m1.m[6], 0);
        assertEquals("m24", M_24, m1.m[7], 0);
        assertEquals("m31", M_31, m1.m[8], 0);
        assertEquals("m32", M_32, m1.m[9], 0);
        assertEquals("m33", M_33, m1.m[10], 0);
        assertEquals("m34", M_34, m1.m[11], 0);
        assertEquals("m41", M_41, m1.m[12], 0);
        assertEquals("m42", M_42, m1.m[13], 0);
        assertEquals("m43", M_43, m1.m[14], 0);
        assertEquals("m44", M_44, m1.m[15], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSet_FromMatrix() throws Exception {
        Matrix4 m1 = new Matrix4(); // matrix under test
        Matrix4 m2 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 m2Copy = new Matrix4(m2);

        Matrix4 m3 = m1.set(m2);

        assertArrayEquals("identical array", m2.m, m1.m, 0d);
        assertEquals("identical matrix", m2, m1);
        assertEquals("matrix not mutated", m2, m2Copy);
        assertSame("fluent api result", m3, m1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        Matrix4 m1 = new Matrix4(); // matrix under test

        m1.set(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testSetTranslation() throws Exception {
        final double dx = 3d;
        final double dy = 5d;
        final double dz = 7d;
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 m2 = m1.setTranslation(dx, dy, dz);

        // Test for proper translation matrix
        // [m11  m12  m13  dx ]
        // [m21  m22  m23  dy ]
        // [m31  m32  m33  dz ]
        // [m41  m42  m43  m44]
        //
        // row 1
        assertEquals("m11", M_11, m1.m[0], 0);
        assertEquals("m12", M_12, m1.m[1], 0);
        assertEquals("m13", M_13, m1.m[2], 0);
        assertEquals("m14", dx, m1.m[3], 0);
        // row 2
        assertEquals("m21", M_21, m1.m[4], 0);
        assertEquals("m22", M_22, m1.m[5], 0);
        assertEquals("m23", M_23, m1.m[6], 0);
        assertEquals("m24", dy, m1.m[7], 0);
        // row 3
        assertEquals("m31", M_31, m1.m[8], 0);
        assertEquals("m32", M_32, m1.m[9], 0);
        assertEquals("m33", M_33, m1.m[10], 0);
        assertEquals("m34", dz, m1.m[11], 0);
        // row 4
        assertEquals("m41", M_41, m1.m[12], 0);
        assertEquals("m42", M_42, m1.m[13], 0);
        assertEquals("m43", M_43, m1.m[14], 0);
        assertEquals("m44", M_44, m1.m[15], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetRotation() throws Exception {
        Matrix4 m1 = new Matrix4();
        Vec3 u = new Vec3(0, 1, 0);   // unit vector on y axis

        // Set the rotation matrix three times,
        // each time we'll rotate a unit vector
        // used to validate the values in the matrix.
        // Cumulatively, we'll end up rotating the
        // unit vector -90deg cw around z.
        m1.setRotation(1, 0, 0, 30);    // rotate 30deg ccw around x
        u.multiplyByMatrix(m1);
        m1.setRotation(0, 1, 0, 90);    // rotate 90deg ccw around y
        u.multiplyByMatrix(m1);
        m1.setRotation(0, 0, 1, -60);   // rotate -60deg cw around z
        u.multiplyByMatrix(m1);

        // We should have a unit vector on the x axis
        assertEquals("u.x", 1.0, u.x, TOLERANCE);
        assertEquals("u.y", 0.0, u.y, TOLERANCE);
        assertEquals("u.z", 0.0, u.z, TOLERANCE);
    }

    @Test
    public void testSetScale() throws Exception {
        final double sx = 3d;
        final double sy = 5d;
        final double sz = 7d;
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 m2 = m1.setScale(sx, sy, sz);

        // Test for proper scale matrix
        // [sx   m12  m13  m14]
        // [m21  sy   m23  m24]
        // [m31  m32  sz   m34]
        // [m41  m42  m43  m44]
        //
        // row 1
        assertEquals("m11", sx, m1.m[0], 0);
        assertEquals("m12", M_12, m1.m[1], 0);
        assertEquals("m13", M_13, m1.m[2], 0);
        assertEquals("m14", M_14, m1.m[3], 0);
        // row 2
        assertEquals("m21", M_21, m1.m[4], 0);
        assertEquals("m22", sy, m1.m[5], 0);
        assertEquals("m23", M_23, m1.m[6], 0);
        assertEquals("m24", M_24, m1.m[7], 0);
        // row 3
        assertEquals("m31", M_31, m1.m[8], 0);
        assertEquals("m32", M_32, m1.m[9], 0);
        assertEquals("m33", sz, m1.m[10], 0);
        assertEquals("m34", M_34, m1.m[11], 0);
        // row 4
        assertEquals("m41", M_41, m1.m[12], 0);
        assertEquals("m42", M_42, m1.m[13], 0);
        assertEquals("m43", M_43, m1.m[14], 0);
        assertEquals("m44", M_44, m1.m[15], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToIdentity() throws Exception {
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 m2 = m1.setToIdentity();

        assertArrayEquals("identity matrix array", Matrix4.identity, m1.m, 0);
        assertSame(m2, m1);
    }

    @Test
    public void testSetToTranslation() throws Exception {
        final double dx = 3d;
        final double dy = 5d;
        final double dz = 7d;
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 m2 = m1.setToTranslation(dx, dy, dz);

        // Test for translation matrix form
        // [1  0  0  dx]
        // [0  1  0  dy]
        // [0  0  1  dz]
        // [0  0  0  1 ]
        //
        // row 1
        assertEquals("m11", 1, m1.m[0], 0);
        assertEquals("m12", 0, m1.m[1], 0);
        assertEquals("m13", 0, m1.m[2], 0);
        assertEquals("m14", dx, m1.m[3], 0);
        // row 2
        assertEquals("m21", 0, m1.m[4], 0);
        assertEquals("m22", 1, m1.m[5], 0);
        assertEquals("m23", 0, m1.m[6], 0);
        assertEquals("m24", dy, m1.m[7], 0);
        // row 3
        assertEquals("m31", 0, m1.m[8], 0);
        assertEquals("m32", 0, m1.m[9], 0);
        assertEquals("m33", 1, m1.m[10], 0);
        assertEquals("m34", dz, m1.m[11], 0);
        // row 4
        assertEquals("m41", 0, m1.m[12], 0);
        assertEquals("m42", 0, m1.m[13], 0);
        assertEquals("m43", 0, m1.m[14], 0);
        assertEquals("m44", 1, m1.m[15], 0);

        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToRotation() throws Exception {
        Matrix4 mx = new Matrix4();
        Matrix4 my = new Matrix4();
        Matrix4 mz = new Matrix4();

        mx.setToRotation(1, 0, 0, 30);  // rotate ccw around x
        my.setToRotation(0, 1, 0, 90);  // rotate ccw around y
        mz.setToRotation(0, 0, 1, -60); // rotate cw around z

        // Rotate a unit vector from 0,1,0 to 1,0,0
        Vec3 u = new Vec3(0, 1, 0);
        u.multiplyByMatrix(mx);
        u.multiplyByMatrix(my);
        u.multiplyByMatrix(mz);
        assertEquals("u.x", 1.0, u.x, TOLERANCE);
        assertEquals("u.y", 0.0, u.y, TOLERANCE);
        assertEquals("u.z", 0.0, u.z, TOLERANCE);
    }

    @Test
    public void testSetToScale() throws Exception {
        final double sx = 3d;
        final double sy = 5d;
        final double sz = 7d;
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44
        );

        Matrix4 m2 = m1.setToScale(sx, sy, sz);

        // Test for scale matrix form
        // [sx 0  0  0]
        // [0  sy 0  0]
        // [0  0  sz 0]
        // [0  0  0  1]
        // row 1
        assertEquals("m11", sx, m1.m[0], 0);
        assertEquals("m12", 0, m1.m[1], 0);
        assertEquals("m13", 0, m1.m[2], 0);
        assertEquals("m14", 0, m1.m[3], 0);
        // row 2
        assertEquals("m21", 0, m1.m[4], 0);
        assertEquals("m22", sy, m1.m[5], 0);
        assertEquals("m23", 0, m1.m[6], 0);
        assertEquals("m24", 0, m1.m[7], 0);
        // row 3
        assertEquals("m31", 0, m1.m[8], 0);
        assertEquals("m32", 0, m1.m[9], 0);
        assertEquals("m33", sz, m1.m[10], 0);
        assertEquals("m34", 0, m1.m[11], 0);
        // row 4
        assertEquals("m41", 0, m1.m[12], 0);
        assertEquals("m42", 0, m1.m[13], 0);
        assertEquals("m43", 0, m1.m[14], 0);
        assertEquals("m44", 1, m1.m[15], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testSetToMultiply() throws Exception {
        Matrix4 m1 = new Matrix4(); // matrix under test
        Matrix4 a = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 b = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 aCopy = new Matrix4(a);
        Matrix4 bCopy = new Matrix4(b);

        Matrix4 m2 = m1.setToMultiply(a, b);

        // Test for result of a x b:
        //                 1st Column                                2nd Column                               3rd Column                               4th Column
        // [ (a11*b11 + a12*b21 + a13*b31 + a14*b41)  (a11*b12 + a12*b22 + a13*b32 + a14*b42)  (a11*b13 + a12*b23 + a13*b33 + a14*b43)  (a11*b14 + a12*b24 + a13*b34 + a14*b44) ]
        // [ (a21*b11 + a22*b21 + a23*b31 + a24*b41)  (a21*b12 + a22*b22 + a23*b32 + a24*b42)  (a21*b13 + a22*b23 + a23*b33 + a24*b43)  (a21*b14 + a22*b24 + a23*b34 + a24*b44) ]
        // [ (a31*b11 + a32*b21 + a33*b31 + a34*b41)  (a31*b12 + a32*b22 + a33*b32 + a34*b42)  (a31*b13 + a32*b23 + a33*b33 + a34*b43)  (a31*b14 + a32*b24 + a33*b34 + a34*b44) ]
        // [ (a41*b11 + a42*b21 + a43*b31 + a44*b41)  (a41*b12 + a42*b22 + a43*b32 + a44*b42)  (a41*b13 + a42*b23 + a43*b33 + a44*b43)  (a41*b14 + a42*b24 + a43*b34 + a44*b44) ]
        //
        // 1st Column:
        assertEquals("m11", (a.m[0] * b.m[0]) + (a.m[1] * b.m[4]) + (a.m[2] * b.m[8]) + (a.m[3] * b.m[12]), m1.m[0], 0);
        assertEquals("m21", (a.m[4] * b.m[0]) + (a.m[5] * b.m[4]) + (a.m[6] * b.m[8]) + (a.m[7] * b.m[12]), m1.m[4], 0);
        assertEquals("m31", (a.m[8] * b.m[0]) + (a.m[9] * b.m[4]) + (a.m[10] * b.m[8]) + (a.m[11] * b.m[12]), m1.m[8], 0);
        assertEquals("m41", (a.m[12] * b.m[0]) + (a.m[13] * b.m[4]) + (a.m[14] * b.m[8]) + (a.m[15] * b.m[12]), m1.m[12], 0);
        // 2nd Column:
        assertEquals("m12", (a.m[0] * b.m[1]) + (a.m[1] * b.m[5]) + (a.m[2] * b.m[9]) + (a.m[3] * b.m[13]), m1.m[1], 0);
        assertEquals("m22", (a.m[4] * b.m[1]) + (a.m[5] * b.m[5]) + (a.m[6] * b.m[9]) + (a.m[7] * b.m[13]), m1.m[5], 0);
        assertEquals("m32", (a.m[8] * b.m[1]) + (a.m[9] * b.m[5]) + (a.m[10] * b.m[9]) + (a.m[11] * b.m[13]), m1.m[9], 0);
        assertEquals("m42", (a.m[12] * b.m[1]) + (a.m[13] * b.m[5]) + (a.m[14] * b.m[9]) + (a.m[15] * b.m[13]), m1.m[13], 0);
        // 3rd Column:
        assertEquals("m13", (a.m[0] * b.m[2]) + (a.m[1] * b.m[6]) + (a.m[2] * b.m[10]) + (a.m[3] * b.m[14]), m1.m[2], 0);
        assertEquals("m23", (a.m[4] * b.m[2]) + (a.m[5] * b.m[6]) + (a.m[6] * b.m[10]) + (a.m[7] * b.m[14]), m1.m[6], 0);
        assertEquals("m33", (a.m[8] * b.m[2]) + (a.m[9] * b.m[6]) + (a.m[10] * b.m[10]) + (a.m[11] * b.m[14]), m1.m[10], 0);
        assertEquals("m43", (a.m[12] * b.m[2]) + (a.m[13] * b.m[6]) + (a.m[14] * b.m[10]) + (a.m[15] * b.m[14]), m1.m[14], 0);
        // 4th Column:
        assertEquals("m14", (a.m[0] * b.m[3]) + (a.m[1] * b.m[7]) + (a.m[2] * b.m[11]) + (a.m[3] * b.m[15]), m1.m[3], 0);
        assertEquals("m24", (a.m[4] * b.m[3]) + (a.m[5] * b.m[7]) + (a.m[6] * b.m[11]) + (a.m[7] * b.m[15]), m1.m[7], 0);
        assertEquals("m34", (a.m[8] * b.m[3]) + (a.m[9] * b.m[7]) + (a.m[10] * b.m[11]) + (a.m[11] * b.m[15]), m1.m[11], 0);
        assertEquals("m44", (a.m[12] * b.m[3]) + (a.m[13] * b.m[7]) + (a.m[14] * b.m[11]) + (a.m[15] * b.m[15]), m1.m[15], 0);
        //
        assertSame("fluent api result", m2, m1);
        assertEquals("a not mutated", aCopy, a);
        assertEquals("b not mutated", bCopy, b);
    }

    @Ignore("not implemented")
    @Test
    public void testSetToPerspectiveProjection() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testSetToScreenProjection() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testSetToCovarianceOfBuffer() throws Exception {

        fail("The test case is a stub.");

    }

    @Test
    public void testMultiplyByTranslation() throws Exception {
        Matrix4 m1 = new Matrix4(); // identity matrix
        double dx = 3;
        double dy = 5;
        double dz = 7;

        Matrix4 m2 = m1.multiplyByTranslation(dx, dy, dz);

        // Test for translation matrix form
        // [1 0 0 x]
        // [0 1 0 y]
        // [0 0 1 z]
        // [0 0 0 1]
        assertEquals("m11", 1d, m1.m[0], 0);
        assertEquals("m12", 0d, m1.m[1], 0);
        assertEquals("m13", 0d, m1.m[2], 0);
        assertEquals("m14", dx, m1.m[3], 0);
        assertEquals("m21", 0d, m1.m[4], 0);
        assertEquals("m22", 1d, m1.m[5], 0);
        assertEquals("m23", 0d, m1.m[6], 0);
        assertEquals("m24", dy, m1.m[7], 0);
        assertEquals("m31", 0d, m1.m[8], 0);
        assertEquals("m32", 0d, m1.m[9], 0);
        assertEquals("m33", 1d, m1.m[10], 0);
        assertEquals("m34", dz, m1.m[11], 0);
        assertEquals("m41", 0d, m1.m[12], 0);
        assertEquals("m42", 0d, m1.m[13], 0);
        assertEquals("m43", 0d, m1.m[14], 0);
        assertEquals("m44", 1d, m1.m[15], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testMultiplyByRotation() throws Exception {
        // Rotate a unit vectors
        Vec3 r = new Vec3(0, 0, 1);
        Matrix4 m = new Matrix4();

        m.multiplyByRotation(1, 0, 0, 30); // rotate ccw around x
        m.multiplyByRotation(0, 1, 0, 90); // rotate ccw around y
        m.multiplyByRotation(0, 0, 1, -60); // rotate cw around z

        r.multiplyByMatrix(m);
        assertEquals("u.x", 1.0, r.x, TOLERANCE);
        assertEquals("u.y", 0.0, r.y, TOLERANCE);
        assertEquals("u.z", 0.0, r.z, TOLERANCE);

        final double theta = 30d;   // rotation angle degrees
        final double c = Math.cos(Math.toRadians(theta));
        final double s = Math.sin(Math.toRadians(theta));
        Matrix4 m1 = new Matrix4().multiplyByRotation(1, 0, 0, theta);
        Matrix4 m2 = new Matrix4().multiplyByRotation(0, 1, 0, theta);
        Matrix4 m3 = new Matrix4().multiplyByRotation(0, 0, 1, theta);
        // X
        // [  1       0       0     0]
        // [  0     cos(a) -sin(a)  0]
        // [  0     sin(a)  cos(a)  0]
        // [  0       0     0       1]
        assertEquals("m11", 1d, m1.m[0], TOLERANCE);
        assertEquals("m12", 0d, m1.m[1], TOLERANCE);
        assertEquals("m13", 0d, m1.m[2], TOLERANCE);
        assertEquals("m14", 0d, m1.m[3], TOLERANCE);
        assertEquals("m21", 0d, m1.m[4], TOLERANCE);
        assertEquals("m22", c, m1.m[5], TOLERANCE);
        assertEquals("m23", -s, m1.m[6], TOLERANCE);
        assertEquals("m24", 0d, m1.m[7], TOLERANCE);
        assertEquals("m31", 0d, m1.m[8], TOLERANCE);
        assertEquals("m32", s, m1.m[9], TOLERANCE);
        assertEquals("m33", c, m1.m[10], TOLERANCE);
        assertEquals("m44", 0d, m1.m[11], TOLERANCE);
        assertEquals("m41", 0d, m1.m[12], TOLERANCE);
        assertEquals("m42", 0d, m1.m[13], TOLERANCE);
        assertEquals("m43", 0d, m1.m[14], TOLERANCE);
        assertEquals("m44", 1d, m1.m[15], TOLERANCE);

        // Y
        // [ cos(a)   0   sin(a) 0]
        // [  0       1     0    0]
        // [-sin(a)   0  cos(a)  0]
        // [  0       0     0    1]
        assertEquals("m11", c, m2.m[0], TOLERANCE);
        assertEquals("m12", 0d, m2.m[1], TOLERANCE);
        assertEquals("m13", s, m2.m[2], TOLERANCE);
        assertEquals("m14", 0d, m2.m[3], TOLERANCE);
        assertEquals("m21", 0d, m2.m[4], TOLERANCE);
        assertEquals("m22", 1d, m2.m[5], TOLERANCE);
        assertEquals("m23", 0d, m2.m[6], TOLERANCE);
        assertEquals("m24", 0d, m2.m[7], TOLERANCE);
        assertEquals("m31", -s, m2.m[8], TOLERANCE);
        assertEquals("m32", 0d, m2.m[9], TOLERANCE);
        assertEquals("m33", c, m2.m[10], TOLERANCE);
        assertEquals("m44", 0d, m2.m[11], TOLERANCE);
        assertEquals("m41", 0d, m2.m[12], TOLERANCE);
        assertEquals("m42", 0d, m2.m[13], TOLERANCE);
        assertEquals("m43", 0d, m2.m[14], TOLERANCE);
        assertEquals("m44", 1d, m2.m[15], TOLERANCE);
        
        // Z
        // [cos(a) -sin(a)  0    0]
        // [sin(a)  cos(a)  0    0]
        // [  0       0     1    0]
        // [  0       0     0    1]
        assertEquals("m11", c, m3.m[0], TOLERANCE);
        assertEquals("m12", -s, m3.m[1], TOLERANCE);
        assertEquals("m13", 0d, m3.m[2], TOLERANCE);
        assertEquals("m14", 0d, m3.m[3], TOLERANCE);
        assertEquals("m21", s, m3.m[4], TOLERANCE);
        assertEquals("m22", c, m3.m[5], TOLERANCE);
        assertEquals("m23", 0d, m3.m[6], TOLERANCE);
        assertEquals("m24", 0d, m3.m[7], TOLERANCE);
        assertEquals("m31", 0d, m3.m[8], TOLERANCE);
        assertEquals("m32", 0d, m3.m[9], TOLERANCE);
        assertEquals("m33", 1d, m3.m[10], TOLERANCE);
        assertEquals("m44", 0d, m3.m[11], TOLERANCE);
        assertEquals("m41", 0d, m3.m[12], TOLERANCE);
        assertEquals("m42", 0d, m3.m[13], TOLERANCE);
        assertEquals("m43", 0d, m3.m[14], TOLERANCE);
        assertEquals("m44", 1d, m3.m[15], TOLERANCE);

    }

    @Test
    public void testMultiplyByScale() throws Exception {
        Matrix4 m1 = new Matrix4(); // identity matrix
        double sx = 3;
        double sy = 5;
        double sz = 7;

        Matrix4 m2 = m1.multiplyByScale(sx, sy, sz);

        // Test for scale matrix form
        // [sx 0  0  0]
        // [0  sy 0  0]
        // [0  0  sz 0]
        // [0  0  0  1]
        // row 1
        assertEquals("m11", sx, m1.m[0], 0);
        assertEquals("m12", 0, m1.m[1], 0);
        assertEquals("m13", 0, m1.m[2], 0);
        assertEquals("m14", 0, m1.m[3], 0);
        // row 2
        assertEquals("m21", 0, m1.m[4], 0);
        assertEquals("m22", sy, m1.m[5], 0);
        assertEquals("m23", 0, m1.m[6], 0);
        assertEquals("m24", 0, m1.m[7], 0);
        // row 3
        assertEquals("m31", 0, m1.m[8], 0);
        assertEquals("m32", 0, m1.m[9], 0);
        assertEquals("m33", sz, m1.m[10], 0);
        assertEquals("m34", 0, m1.m[11], 0);
        // row 4
        assertEquals("m41", 0, m1.m[12], 0);
        assertEquals("m42", 0, m1.m[13], 0);
        assertEquals("m43", 0, m1.m[14], 0);
        assertEquals("m44", 1, m1.m[15], 0);
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testMultiplyByMatrix() throws Exception {
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 m2 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 m1Copy = new Matrix4(m1);
        Matrix4 m2Copy = new Matrix4(m2);
        double[] a = m1Copy.m;
        double[] b = m2.m;

        Matrix4 m3 = m1.multiplyByMatrix(m2);

        // Test for result of a x b:
        //                 1st Column                                2nd Column                               3rd Column                               4th Column
        // [ (a11*b11 + a12*b21 + a13*b31 + a14*b41)  (a11*b12 + a12*b22 + a13*b32 + a14*b42)  (a11*b13 + a12*b23 + a13*b33 + a14*b43)  (a11*b14 + a12*b24 + a13*b34 + a14*b44) ]
        // [ (a21*b11 + a22*b21 + a23*b31 + a24*b41)  (a21*b12 + a22*b22 + a23*b32 + a24*b42)  (a21*b13 + a22*b23 + a23*b33 + a24*b43)  (a21*b14 + a22*b24 + a23*b34 + a24*b44) ]
        // [ (a31*b11 + a32*b21 + a33*b31 + a34*b41)  (a31*b12 + a32*b22 + a33*b32 + a34*b42)  (a31*b13 + a32*b23 + a33*b33 + a34*b43)  (a31*b14 + a32*b24 + a33*b34 + a34*b44) ]
        // [ (a41*b11 + a42*b21 + a43*b31 + a44*b41)  (a41*b12 + a42*b22 + a43*b32 + a44*b42)  (a41*b13 + a42*b23 + a43*b33 + a44*b43)  (a41*b14 + a42*b24 + a43*b34 + a44*b44) ]
        //
        // 1st Column:
        assertEquals("m11", (a[0] * b[0]) + (a[1] * b[4]) + (a[2] * b[8]) + (a[3] * b[12]), m1.m[0], 0);
        assertEquals("m21", (a[4] * b[0]) + (a[5] * b[4]) + (a[6] * b[8]) + (a[7] * b[12]), m1.m[4], 0);
        assertEquals("m31", (a[8] * b[0]) + (a[9] * b[4]) + (a[10] * b[8]) + (a[11] * b[12]), m1.m[8], 0);
        assertEquals("m41", (a[12] * b[0]) + (a[13] * b[4]) + (a[14] * b[8]) + (a[15] * b[12]), m1.m[12], 0);
        // 2nd Column:
        assertEquals("m12", (a[0] * b[1]) + (a[1] * b[5]) + (a[2] * b[9]) + (a[3] * b[13]), m1.m[1], 0);
        assertEquals("m22", (a[4] * b[1]) + (a[5] * b[5]) + (a[6] * b[9]) + (a[7] * b[13]), m1.m[5], 0);
        assertEquals("m32", (a[8] * b[1]) + (a[9] * b[5]) + (a[10] * b[9]) + (a[11] * b[13]), m1.m[9], 0);
        assertEquals("m42", (a[12] * b[1]) + (a[13] * b[5]) + (a[14] * b[9]) + (a[15] * b[13]), m1.m[13], 0);
        // 3rd Column:
        assertEquals("m13", (a[0] * b[2]) + (a[1] * b[6]) + (a[2] * b[10]) + (a[3] * b[14]), m1.m[2], 0);
        assertEquals("m23", (a[4] * b[2]) + (a[5] * b[6]) + (a[6] * b[10]) + (a[7] * b[14]), m1.m[6], 0);
        assertEquals("m33", (a[8] * b[2]) + (a[9] * b[6]) + (a[10] * b[10]) + (a[11] * b[14]), m1.m[10], 0);
        assertEquals("m43", (a[12] * b[2]) + (a[13] * b[6]) + (a[14] * b[10]) + (a[15] * b[14]), m1.m[14], 0);
        // 4th Column:
        assertEquals("m14", (a[0] * b[3]) + (a[1] * b[7]) + (a[2] * b[11]) + (a[3] * b[15]), m1.m[3], 0);
        assertEquals("m24", (a[4] * b[3]) + (a[5] * b[7]) + (a[6] * b[11]) + (a[7] * b[15]), m1.m[7], 0);
        assertEquals("m34", (a[8] * b[3]) + (a[9] * b[7]) + (a[10] * b[11]) + (a[11] * b[15]), m1.m[11], 0);
        assertEquals("m44", (a[12] * b[3]) + (a[13] * b[7]) + (a[14] * b[11]) + (a[15] * b[15]), m1.m[15], 0);
        //
        assertEquals("not mutated", m2Copy, m2);
        assertSame("fluent api result", m3, m1);
    }


    @Test
    public void testMultiplyByMatrix_Doubles() throws Exception {
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);
        Matrix4 m1Copy = new Matrix4(m1);
        double[] a = m1Copy.m;
        double[] b = m1Copy.m;

        Matrix4 m2 = m1.multiplyByMatrix(
            b[0], b[1], b[2], b[3],
            b[4], b[5], b[6], b[7],
            b[8], b[9], b[10], b[11],
            b[12], b[13], b[14], b[15]);

        // Test for result of a x b:
        //                 1st Column                                2nd Column                               3rd Column                               4th Column
        // [ (a11*b11 + a12*b21 + a13*b31 + a14*b41)  (a11*b12 + a12*b22 + a13*b32 + a14*b42)  (a11*b13 + a12*b23 + a13*b33 + a14*b43)  (a11*b14 + a12*b24 + a13*b34 + a14*b44) ]
        // [ (a21*b11 + a22*b21 + a23*b31 + a24*b41)  (a21*b12 + a22*b22 + a23*b32 + a24*b42)  (a21*b13 + a22*b23 + a23*b33 + a24*b43)  (a21*b14 + a22*b24 + a23*b34 + a24*b44) ]
        // [ (a31*b11 + a32*b21 + a33*b31 + a34*b41)  (a31*b12 + a32*b22 + a33*b32 + a34*b42)  (a31*b13 + a32*b23 + a33*b33 + a34*b43)  (a31*b14 + a32*b24 + a33*b34 + a34*b44) ]
        // [ (a41*b11 + a42*b21 + a43*b31 + a44*b41)  (a41*b12 + a42*b22 + a43*b32 + a44*b42)  (a41*b13 + a42*b23 + a43*b33 + a44*b43)  (a41*b14 + a42*b24 + a43*b34 + a44*b44) ]
        //
        // 1st Column:
        assertEquals("m11", (a[0] * b[0]) + (a[1] * b[4]) + (a[2] * b[8]) + (a[3] * b[12]), m1.m[0], 0);
        assertEquals("m21", (a[4] * b[0]) + (a[5] * b[4]) + (a[6] * b[8]) + (a[7] * b[12]), m1.m[4], 0);
        assertEquals("m31", (a[8] * b[0]) + (a[9] * b[4]) + (a[10] * b[8]) + (a[11] * b[12]), m1.m[8], 0);
        assertEquals("m41", (a[12] * b[0]) + (a[13] * b[4]) + (a[14] * b[8]) + (a[15] * b[12]), m1.m[12], 0);
        // 2nd Column:
        assertEquals("m12", (a[0] * b[1]) + (a[1] * b[5]) + (a[2] * b[9]) + (a[3] * b[13]), m1.m[1], 0);
        assertEquals("m22", (a[4] * b[1]) + (a[5] * b[5]) + (a[6] * b[9]) + (a[7] * b[13]), m1.m[5], 0);
        assertEquals("m32", (a[8] * b[1]) + (a[9] * b[5]) + (a[10] * b[9]) + (a[11] * b[13]), m1.m[9], 0);
        assertEquals("m42", (a[12] * b[1]) + (a[13] * b[5]) + (a[14] * b[9]) + (a[15] * b[13]), m1.m[13], 0);
        // 3rd Column:
        assertEquals("m13", (a[0] * b[2]) + (a[1] * b[6]) + (a[2] * b[10]) + (a[3] * b[14]), m1.m[2], 0);
        assertEquals("m23", (a[4] * b[2]) + (a[5] * b[6]) + (a[6] * b[10]) + (a[7] * b[14]), m1.m[6], 0);
        assertEquals("m33", (a[8] * b[2]) + (a[9] * b[6]) + (a[10] * b[10]) + (a[11] * b[14]), m1.m[10], 0);
        assertEquals("m43", (a[12] * b[2]) + (a[13] * b[6]) + (a[14] * b[10]) + (a[15] * b[14]), m1.m[14], 0);
        // 4th Column:
        assertEquals("m14", (a[0] * b[3]) + (a[1] * b[7]) + (a[2] * b[11]) + (a[3] * b[15]), m1.m[3], 0);
        assertEquals("m24", (a[4] * b[3]) + (a[5] * b[7]) + (a[6] * b[11]) + (a[7] * b[15]), m1.m[7], 0);
        assertEquals("m34", (a[8] * b[3]) + (a[9] * b[7]) + (a[10] * b[11]) + (a[11] * b[15]), m1.m[11], 0);
        assertEquals("m44", (a[12] * b[3]) + (a[13] * b[7]) + (a[14] * b[11]) + (a[15] * b[15]), m1.m[15], 0);
        //
        assertSame("fluent api result", m2, m1);
    }

    @Test
    public void testTranspose() throws Exception {
        Matrix4 m1 = new Matrix4(
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 m2 = m1.transpose();

        // row 1
        assertEquals("m11", M_11, m1.m[0], 0);
        assertEquals("m12", M_21, m1.m[1], 0);
        assertEquals("m13", M_31, m1.m[2], 0);
        assertEquals("m14", M_41, m1.m[3], 0);
        // row 2
        assertEquals("m21", M_12, m1.m[4], 0);
        assertEquals("m22", M_22, m1.m[5], 0);
        assertEquals("m23", M_32, m1.m[6], 0);
        assertEquals("m24", M_42, m1.m[7], 0);
        // row 3
        assertEquals("m31", M_13, m1.m[8], 0);
        assertEquals("m32", M_23, m1.m[9], 0);
        assertEquals("m33", M_33, m1.m[10], 0);
        assertEquals("m34", M_43, m1.m[11], 0);
        // row 4
        assertEquals("m41", M_14, m1.m[12], 0);
        assertEquals("m42", M_24, m1.m[13], 0);
        assertEquals("m43", M_34, m1.m[14], 0);
        assertEquals("m44", M_44, m1.m[15], 0);
        //
        assertSame("fluent api result", m2, m1);
    }


    @Test
    public void testTransposeMatrix() throws Exception {
        Matrix4 m1 = new Matrix4(); // matrix under test
        Matrix4 m2 = new Matrix4(   // matrix to be transposed    
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        Matrix4 m3 = m1.transposeMatrix(m2);

        // row 1
        assertEquals("m11", M_11, m1.m[0], 0);
        assertEquals("m12", M_21, m1.m[1], 0);
        assertEquals("m13", M_31, m1.m[2], 0);
        assertEquals("m14", M_41, m1.m[3], 0);
        // row 2
        assertEquals("m21", M_12, m1.m[4], 0);
        assertEquals("m22", M_22, m1.m[5], 0);
        assertEquals("m23", M_32, m1.m[6], 0);
        assertEquals("m24", M_42, m1.m[7], 0);
        // row 3
        assertEquals("m31", M_13, m1.m[8], 0);
        assertEquals("m32", M_23, m1.m[9], 0);
        assertEquals("m33", M_33, m1.m[10], 0);
        assertEquals("m34", M_43, m1.m[11], 0);
        // row 4
        assertEquals("m41", M_14, m1.m[12], 0);
        assertEquals("m42", M_24, m1.m[13], 0);
        assertEquals("m43", M_34, m1.m[14], 0);
        assertEquals("m44", M_44, m1.m[15], 0);
        //
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testInvert() throws Exception {
        Matrix4 m1 = new Matrix4(   // matrix to be inverted/tested
            3, -2, 0, 0,
            1, 4, -3, 0,
            -1, 0, 2, 0,
            0, 0, 0, 1);
        Matrix4 m1Original = new Matrix4(m1);
        Matrix4 m2 = null;
        // Sanity check
        double d = computeDeterminant(m1);
        assertNotEquals("matrix is singular", d, 0, 1e-16);
        try {

            m2 = m1.invert();   // system under test

        } catch (Exception e) {
            fail(e.getClass().getName());
        }

        // multiplying a matrix by its inverse should result in an identity matrix
        Matrix4 mIdentity = m1.multiplyByMatrix(m1Original);
        assertArrayEquals("identity matrix array", Matrix4.identity, mIdentity.m, 1e-10);
        assertSame("fluent api result", m2, m1);
    }


    @Test
    public void testInvertMatrix() throws Exception {
        Matrix4 m1 = new Matrix4(); // matrix under test
        Matrix4 m2 = new Matrix4(   // matrix to be inverted
            3, -2, 0, 0,
            1, 4, -3, 0,
            -1, 0, 2, 0,
            0, 0, 0, 1);
        Matrix4 m3 = null;
        // Sanity check
        double d = computeDeterminant(m2);
        assertNotEquals("matrix is singular", d, 0, 1e-16);
        try {

            m3 = m1.invertMatrix(m2);   // system under test

        } catch (Exception e) {
            fail(e.getClass().getName());
        }

        // multiplying a matrix by its inverse should result in an identity matrix
        Matrix4 mIdentity = m1.multiplyByMatrix(m2);
        assertArrayEquals("identity matrix array", Matrix4.identity, mIdentity.m, 1e-10);
        assertSame("fluent api result", m3, m1);
    }

    /**
     * Tests the inverting of an orthogonal matrix whose columns and rows are orthogonal unit vectors (i.e., orthonormal
     * vectors).
     *
     * @throws Exception
     */
    @Test
    public void testInvertOrthonormalMatrix() throws Exception {
        double dx = 2;
        double dy = 3;
        double dz = 5;
        Matrix4 m1 = new Matrix4(); // matrix under test
        Matrix4 mOrthonormal = new Matrix4(   // an orthonormal matrix without translation
            0.5, 0.866025, 0, 0,
            0.866025, -0.5, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);
        Matrix4 mOrthonormalTranslation = new Matrix4(mOrthonormal).setTranslation(dx, dy, dz);


        Matrix4 m3 = m1.invertOrthonormalMatrix(mOrthonormalTranslation);


        // Independently compute orthonormal inverse with translation.
        Matrix4 mOrthonormalInverse = new Matrix4(mOrthonormal).transpose();
        Vec3 u = new Vec3(dx, dy, dz).multiplyByMatrix(mOrthonormalInverse);
        mOrthonormalInverse.setTranslation(-u.x, -u.y, -u.z);
        // Compare arrays of the matrix under test with our computed matrix
        assertArrayEquals("", mOrthonormalInverse.m, m1.m, TOLERANCE);
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testInvertOrthonormal() throws Exception {
        double dx = 2;
        double dy = 3;
        double dz = 5;
        Matrix4 mOrthonormal = new Matrix4( // orthonormal matrix without translation
            0.5, 0.866025, 0, 0,
            0.866025, -0.5, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);
        Matrix4 m1 = new Matrix4(mOrthonormal).setTranslation(dx, dy, dz);


        Matrix4 m3 = m1.invertOrthonormal();    // system under test


        // Independently compute orthonormal inverse with translation.
        Matrix4 mOrthonormalInverse = new Matrix4(mOrthonormal).transpose();
        Vec3 u = new Vec3(dx, dy, dz).multiplyByMatrix(mOrthonormalInverse);
        mOrthonormalInverse.setTranslation(-u.x, -u.y, -u.z);
        // Compare arrays of the matrix under test with our computed matrix
        assertArrayEquals("", mOrthonormalInverse.m, m1.m, TOLERANCE);
        assertSame("fluent api result", m3, m1);
    }

    @Test
    public void testTransposeToArray() throws Exception {
        Matrix4 m1 = new Matrix4(   // matrix under test
            M_11, M_12, M_13, M_14,
            M_21, M_22, M_23, M_24,
            M_31, M_32, M_33, M_34,
            M_41, M_42, M_43, M_44);

        float[] result = m1.transposeToArray(new float[16], 0);

        double[] expected = m1.transpose().m;
        for (int i = 0; i < 16; i++) {
            assertEquals(Integer.toString(i), expected[i], result[i], 0d);
        }

    }


    @Ignore("not implemented")
    @Test
    public void testOffsetProjectionDepth() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testExtractEyePoint() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testExtractForwardVector() throws Exception {

        fail("The test case is a stub.");

    }

    @Ignore("not implemented")
    @Test
    public void testEigensystemFromSymmetricMatrix() throws Exception {

        fail("The test case is a stub.");

    }


    /////////////////////
    //  Helper methods
    /////////////////////

    static private double compute3x3Determinant(Matrix4 matrix) {
        final double m11 = matrix.m[0];
        final double m12 = matrix.m[1];
        final double m13 = matrix.m[2];
        final double m21 = matrix.m[4];
        final double m22 = matrix.m[5];
        final double m23 = matrix.m[6];
        final double m31 = matrix.m[8];
        final double m32 = matrix.m[9];
        final double m33 = matrix.m[10];
        // |m11  m12  m13| m14
        // |m21  m22  m23| m24  = m11(m22*m33 - m23*m32) + m12(m23*m31 - m21*m33) + m13(m21*m32 - m22*m31)
        // |m31  m32  m33| m34
        //  m41  m42  m43  m44
        double[] m = matrix.m;
        double d
            = (m11 * (m22 * m33 - m23 * m32)) //m11(m22*m33 - m23*m32)
            + (m12 * (m23 * m31 - m21 * m33)) //m12(m23*m31 - m21*m33)
            + (m13 * (m21 * m32 - m22 * m31));//m13(m21*m32 - m22*m31)

        return d;
    }

    static private Matrix3 extract3x3Matrix(Matrix4 matrix) {
        final double m11 = matrix.m[0];
        final double m12 = matrix.m[1];
        final double m13 = matrix.m[2];
        final double m21 = matrix.m[4];
        final double m22 = matrix.m[5];
        final double m23 = matrix.m[6];
        final double m31 = matrix.m[8];
        final double m32 = matrix.m[9];
        final double m33 = matrix.m[10];
        return new Matrix3(m11, m12, m13, m21, m22, m23, m31, m32, m33);
    }


    static private double computeDeterminant(Matrix4 matrix) {
        final double m11 = matrix.m[0];
        final double m12 = matrix.m[1];
        final double m13 = matrix.m[2];
        final double m14 = matrix.m[3];
        final double m21 = matrix.m[4];
        final double m22 = matrix.m[5];
        final double m23 = matrix.m[6];
        final double m24 = matrix.m[7];
        final double m31 = matrix.m[8];
        final double m32 = matrix.m[9];
        final double m33 = matrix.m[10];
        final double m34 = matrix.m[11];
        final double m41 = matrix.m[12];
        final double m42 = matrix.m[13];
        final double m43 = matrix.m[14];
        final double m44 = matrix.m[15];
        // |m11  m12  m13  m14|
        // |m21  m22  m23  m24|
        // |m31  m32  m33  m34| =
        // |m41  m42  m43  m44|
        //
        //        |m22  m23  m24|         |m21  m23  m24|          |m21  m22  m24|         |m21  m22  m23|
        //  m11 * |m32  m33  m34| - m12 * |m31  m33  m34| +  m13 * |m31  m32  m34| - m14 * |m31  m32  m33|
        //        |m42  m43  m44|         |m41  m43  m44|          |m41  m42  m44|         |m41  m42  m43|
        //
        double determinant
            = m11 * (m22 * (m33 * m44 - m34 * m43) + m23 * (m34 * m42 - m32 * m44) + m24 * (m32 * m43 - m33 * m42))
            - m12 * (m21 * (m33 * m44 - m34 * m43) + m23 * (m34 * m41 - m31 * m44) + m24 * (m31 * m43 - m33 * m41))
            + m13 * (m21 * (m32 * m44 - m34 * m42) + m22 * (m34 * m41 - m31 * m44) + m24 * (m31 * m42 - m32 * m41))
            - m14 * (m21 * (m32 * m43 - m33 * m42) + m22 * (m33 * m41 - m31 * m43) + m23 * (m31 * m42 - m32 * m41));

        return determinant;
    }

    static private void prettyPrint(Matrix4 m) {
        System.out.println("Matrix4:");
        System.out.println("[ " + m.m[0] + "  " + m.m[1] + "  " + m.m[2] + "  " + m.m[3] + " ]");
        System.out.println("[ " + m.m[4] + "  " + m.m[5] + "  " + m.m[6] + "  " + m.m[7] + " ]");
        System.out.println("[ " + m.m[8] + "  " + m.m[9] + "  " + m.m[10] + "  " + m.m[11] + " ]");
        System.out.println("[ " + m.m[12] + "  " + m.m[13] + "  " + m.m[14] + "  " + m.m[15] + " ]");
    }

}