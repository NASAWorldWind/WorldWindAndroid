/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log

public class FrustumTest {

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testConstructor_Default() throws Exception {
        /**
         * Constructs a new unit frustum with each of its planes 1 meter from the center.
         */
        Frustum frustum = new Frustum();

        assertNotNull(frustum);
        assertEquals("left", 1, frustum.left.normal.magnitude(), 0);
        assertEquals("right", 1, frustum.right.normal.magnitude(), 0);
        assertEquals("bottom", 1, frustum.bottom.normal.magnitude(), 0);
        assertEquals("top", 1, frustum.top.normal.magnitude(), 0);
        assertEquals("near", 1, frustum.near.normal.magnitude(), 0);
        assertEquals("far", 1, frustum.far.normal.magnitude(), 0);

        assertEquals("left", new Plane(1, 0, 0, 1), frustum.left);
        assertEquals("right", new Plane(-1, 0, 0, 1), frustum.right);
        assertEquals("bottom", new Plane(0, 1, 0, 1), frustum.bottom);
        assertEquals("top", new Plane(0, -1, 0, 1), frustum.top);
        assertEquals("near", new Plane(0, 0, -1, 1), frustum.near);
        assertEquals("far", new Plane(0, 0, 1, 1), frustum.far);
    }

    @Test
    public void testConstructor() throws Exception {
        Plane left = new Plane(0, 1, 0, 2);
        Plane right = new Plane(0, -1, 0, 2);
        Plane bottom = new Plane(0, 0, 1, 2);
        Plane top = new Plane(0, 0, -1, 2);
        Plane near = new Plane(1, 0, 0, 0);
        Plane far = new Plane(-1, 0, 0, 1.5);

        Frustum frustum = new Frustum(left, right, bottom, top, near, far);

        assertNotNull(frustum);
        assertEquals("left", left, frustum.left);
        assertEquals("right", right, frustum.right);
        assertEquals("bottom", bottom, frustum.bottom);
        assertEquals("top", top, frustum.top);
        assertEquals("near", near, frustum.near);
        assertEquals("far", far, frustum.far);
    }

    @Test
    public void testSetToUnitFrustum() throws Exception {
        Plane left = new Plane(0, 1, 0, 2);
        Plane right = new Plane(0, -1, 0, 2);
        Plane bottom = new Plane(0, 0, 1, 2);
        Plane top = new Plane(0, 0, -1, 2);
        Plane near = new Plane(1, 0, 0, 0);
        Plane far = new Plane(-1, 0, 0, 1.5);
        Frustum frustum = new Frustum(left, right, bottom, top, near, far);

        frustum.setToUnitFrustum();

        assertEquals("left", new Plane(1, 0, 0, 1), frustum.left);
        assertEquals("right", new Plane(-1, 0, 0, 1), frustum.right);
        assertEquals("bottom", new Plane(0, 1, 0, 1), frustum.bottom);
        assertEquals("top", new Plane(0, -1, 0, 1), frustum.top);
        assertEquals("near", new Plane(0, 0, -1, 1), frustum.near);
        assertEquals("far", new Plane(0, 0, 1, 1), frustum.far);
    }

    @Test
    public void testSetToProjectionMatrix() throws Exception {
        Frustum f = new Frustum();
        Matrix4 m = new Matrix4().setToPerspectiveProjection(1000, 1000, 60d, 10, 100);  // arbitrary perspective

        f.setToProjectionMatrix(m);

        // Assert that the planes are oriented like a projection matrix
        assertTrue(f.left.normal.x > 0);
        assertTrue(f.left.normal.y == 0);
        assertTrue(f.left.normal.z < 0);
        assertTrue(f.right.normal.x < 0);
        assertTrue(f.right.normal.y == 0);
        assertTrue(f.right.normal.z < 0);
        assertTrue(f.bottom.normal.x == 0);
        assertTrue(f.bottom.normal.y > 0);
        assertTrue(f.bottom.normal.z < 0);
        assertTrue(f.top.normal.x == 0);
        assertTrue(f.top.normal.y < 0);
        assertTrue(f.top.normal.z < 0);
        assertTrue(f.near.normal.x == 0);
        assertTrue(f.near.normal.y == 0);
        assertTrue(f.near.normal.z == -1);
        assertTrue(f.far.normal.x == 0);
        assertTrue(f.far.normal.y == 0);
        assertTrue(f.far.normal.z == 1);

        // Assert sides go through origin
        assertTrue(f.left.distance == 0);
        assertTrue(f.right.distance == 0);
        assertTrue(f.right.distance == 0);
        assertTrue(f.right.distance == 0);

        // Assert near and far are away from origin
        assertTrue(f.near.distance < 0);
        assertTrue(f.far.distance > 0);
    }

    @Test
    public void testTransformByMatrix() throws Exception {

        // An arbitrary transformation matrix
        double theta = 30d;
        double c = Math.cos(Math.toRadians(theta));
        double s = Math.sin(Math.toRadians(theta));
        double x = 3;
        Matrix4 m = new Matrix4().multiplyByRotation(1, 0, 0, theta).multiplyByTranslation(x, 0, 0);
        Frustum f = new Frustum();

        f.transformByMatrix(m);

        assertEquals(x + 1, f.left.normal.x, 0);
        assertEquals(0, f.left.normal.y, 0);
        assertEquals(0, f.left.normal.z, 0);
        assertEquals(x - 1, f.right.normal.x, 0);
        assertEquals(0, f.right.normal.y, 0);
        assertEquals(0, f.right.normal.z, 0);
        assertEquals(x, f.bottom.normal.x, 0);
        assertEquals(c, f.bottom.normal.y, 0);
        assertEquals(s, f.bottom.normal.z, 0);
        assertEquals(x, f.top.normal.x, 0);
        assertEquals(-c, f.top.normal.y, 0);
        assertEquals(-s, f.top.normal.z, 0);
        assertEquals(x, f.near.normal.x, 0);
        assertEquals(s, f.near.normal.y, 0);
        assertEquals(-c, f.near.normal.z, -1);
        assertEquals(x, f.far.normal.x, 0);
        assertEquals(-s, f.far.normal.y, 0);
        assertEquals(c, f.far.normal.z, 1);
    }

    @Test
    public void testNormalize() throws Exception {
        Plane left = new Plane(0, 2, 0, 2);
        Plane right = new Plane(0, -2, 0, 2);
        Plane bottom = new Plane(0, 0, 2, 2);
        Plane top = new Plane(0, 0, -2, 2);
        Plane near = new Plane(2, 0, 0, 0);
        Plane far = new Plane(-2, 0, 0, 1.5);
        Frustum frustum = new Frustum(left, right, bottom, top, near, far);

        frustum.normalize();

        assertEquals("left", new Plane(0, 1, 0, 1), frustum.left);
        assertEquals("right", new Plane(0, -1, 0, 1), frustum.right);
        assertEquals("bottom", new Plane(0, 0, 1, 1), frustum.bottom);
        assertEquals("top", new Plane(0, 0, -1, 1), frustum.top);
        assertEquals("near", new Plane(1, 0, 0, 0), frustum.near);
        assertEquals("far", new Plane(-1, 0, 0, 0.75), frustum.far);
    }

    @Test
    public void testContainsPoint() throws Exception {
        // Simple test using a unit frustum
        Frustum frustum = new Frustum();

        assertTrue("origin", frustum.containsPoint(new Vec3(0, 0, 0)));
        assertTrue("inside near", frustum.containsPoint(new Vec3(0, 0, -0.999999)));
        assertTrue("inside far", frustum.containsPoint(new Vec3(0, 0, 0.999999)));
        assertTrue("inside left", frustum.containsPoint(new Vec3(0.9999999, 0, 0)));
        assertTrue("inside right", frustum.containsPoint(new Vec3(-0.9999999, 0, 0)));
        assertTrue("inside bottom", frustum.containsPoint(new Vec3(0, -0.9999999, 0)));
        assertTrue("inside top", frustum.containsPoint(new Vec3(0, 0.9999999, 0)));

        assertFalse("outside left", frustum.containsPoint(new Vec3(1.0000001, 0, 0)));
        assertFalse("outside right", frustum.containsPoint(new Vec3(-1.0000001, 0, 0)));
        assertFalse("outside bottom", frustum.containsPoint(new Vec3(0, -1.0000001, 0)));
        assertFalse("outside top", frustum.containsPoint(new Vec3(0, 1.0000001, 0)));
        assertFalse("outside near", frustum.containsPoint(new Vec3(0, 0, -1.0000001)));
        assertFalse("outside far", frustum.containsPoint(new Vec3(0, 0, 1.0000001)));

        assertFalse("on left side", frustum.containsPoint(new Vec3(1, 0, 0)));
        assertFalse("on bottom side", frustum.containsPoint(new Vec3(0, -1, 0)));
        assertFalse("on top side", frustum.containsPoint(new Vec3(0, 1, 0)));
        assertFalse("on right side", frustum.containsPoint(new Vec3(-1, 0, 0)));
        assertFalse("on near", frustum.containsPoint(new Vec3(0, 0, -1)));
        assertFalse("on far", frustum.containsPoint(new Vec3(0, 0, 1)));

    }

    @Test
    public void testIntersectsSegment() throws Exception {
        // Perform simple tests with a unit frustum using segments with an endpoint at the origin
        Frustum frustum = new Frustum();
        Vec3 origin = new Vec3(0, 0, 0);

        assertTrue("origin", frustum.intersectsSegment(origin, new Vec3(0, 0, 0)));
        assertTrue("inside near", frustum.intersectsSegment(origin, new Vec3(0, 0, -0.999999)));
        assertTrue("inside far", frustum.intersectsSegment(origin, new Vec3(0, 0, 0.999999)));
        assertTrue("inside left", frustum.intersectsSegment(origin, new Vec3(0.9999999, 0, 0)));
        assertTrue("inside right", frustum.intersectsSegment(origin, new Vec3(-0.9999999, 0, 0)));
        assertTrue("inside bottom", frustum.intersectsSegment(origin, new Vec3(0, -0.9999999, 0)));
        assertTrue("inside top", frustum.intersectsSegment(origin, new Vec3(0, 0.9999999, 0)));

        assertTrue("intersect left", frustum.intersectsSegment(origin, new Vec3(1.0000001, 0, 0)));
        assertTrue("intersect right", frustum.intersectsSegment(origin, new Vec3(-1.0000001, 0, 0)));
        assertTrue("intersect bottom", frustum.intersectsSegment(origin, new Vec3(0, -1.0000001, 0)));
        assertTrue("intersect top", frustum.intersectsSegment(origin, new Vec3(0, 1.0000001, 0)));
        assertTrue("intersect near", frustum.intersectsSegment(origin, new Vec3(0, 0, -1.0000001)));
        assertTrue("intersect far", frustum.intersectsSegment(origin, new Vec3(0, 0, 1.0000001)));

        assertTrue("touch left", frustum.intersectsSegment(new Vec3(1, 0, 0), new Vec3(1.0000001, 0, 0)));
        assertTrue("touch right", frustum.intersectsSegment(new Vec3(-1, 0, 0), new Vec3(-1.0000001, 0, 0)));
        assertTrue("touch bottom", frustum.intersectsSegment(new Vec3(0, -1, 0), new Vec3(0, -1.0000001, 0)));
        assertTrue("touch top", frustum.intersectsSegment(new Vec3(0, 1, 0), new Vec3(0, 1.0000001, 0)));
        assertTrue("touch near", frustum.intersectsSegment(new Vec3(0, 0, -1), new Vec3(0, 0, -1.0000001)));
        assertTrue("touch far", frustum.intersectsSegment(new Vec3(0, 0, 1), new Vec3(0, 0, 1.0000001)));

        assertFalse("outside left", frustum.intersectsSegment(new Vec3(2, 0, 0), new Vec3(1.0000001, 0, 0)));
        assertFalse("outside right", frustum.intersectsSegment(new Vec3(-2, 0, 0), new Vec3(-1.0000001, 0, 0)));
        assertFalse("outside bottom", frustum.intersectsSegment(new Vec3(0, -2, 0), new Vec3(0, -1.0000001, 0)));
        assertFalse("outside top", frustum.intersectsSegment(new Vec3(0, 2, 0), new Vec3(0, 1.0000001, 0)));
        assertFalse("outside near", frustum.intersectsSegment(new Vec3(0, 0, -2), new Vec3(0, 0, -1.0000001)));
        assertFalse("outside far", frustum.intersectsSegment(new Vec3(0, 0, 2), new Vec3(0, 0, 1.0000001)));

    }
}