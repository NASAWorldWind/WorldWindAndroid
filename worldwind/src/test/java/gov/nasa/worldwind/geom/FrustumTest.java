/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.ProjectionWgs84;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log

public class FrustumTest {

    private Globe globe;

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
        // Create the globe object used by the test
        globe = new Globe(WorldWind.WGS84_ELLIPSOID, new ProjectionWgs84());
    }

    @After
    public void tearDown() throws Exception {
        // Release the globe object
        globe = null;
    }

    @Test
    public void testConstructor_Default() throws Exception {
        // Constructs a new unit frustum with each of its planes 1 meter from the center.
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
        Viewport viewport = new Viewport(1, 2, 3, 4);

        Frustum frustum = new Frustum(left, right, bottom, top, near, far, viewport);

        assertNotNull(frustum);
        assertEquals("left", left, frustum.left);
        assertEquals("right", right, frustum.right);
        assertEquals("bottom", bottom, frustum.bottom);
        assertEquals("top", top, frustum.top);
        assertEquals("near", near, frustum.near);
        assertEquals("far", far, frustum.far);
        assertEquals("viewport", viewport, frustum.viewport);
    }

    @Test
    public void testSetToUnitFrustum() throws Exception {
        Plane left = new Plane(0, 1, 0, 2);
        Plane right = new Plane(0, -1, 0, 2);
        Plane bottom = new Plane(0, 0, 1, 2);
        Plane top = new Plane(0, 0, -1, 2);
        Plane near = new Plane(1, 0, 0, 0);
        Plane far = new Plane(-1, 0, 0, 1.5);
        Viewport viewport = new Viewport(1, 2, 3, 4);
        Frustum frustum = new Frustum(left, right, bottom, top, near, far, viewport);

        frustum.setToUnitFrustum();

        assertEquals("left", new Plane(1, 0, 0, 1), frustum.left);
        assertEquals("right", new Plane(-1, 0, 0, 1), frustum.right);
        assertEquals("bottom", new Plane(0, 1, 0, 1), frustum.bottom);
        assertEquals("top", new Plane(0, -1, 0, 1), frustum.top);
        assertEquals("near", new Plane(0, 0, -1, 1), frustum.near);
        assertEquals("far", new Plane(0, 0, 1, 1), frustum.far);
        assertEquals("viewport", new Viewport(0, 0, 1, 1), frustum.viewport);
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

    @Test
    public void testSetToModelviewProjection() throws Exception {
        // The expected test values were obtained via SystemOut on Frustum object
        // at a time in the development cycle when the setToModelviewProjection
        // was known to be working correctly (via observed runtime behavior).
        // This unit test simply tests for changes in the behavior since that time.

        // Create a Frustum similar to the way the WorldWindow does it.

        // Setup a Navigator, looking near Oxnard Airport.
        LookAt lookAt = new LookAt().set(34.15, -119.15, 0, WorldWind.ABSOLUTE, 2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        Navigator navigator = new Navigator();
        navigator.setAsLookAt(globe, lookAt);

        // Compute a perspective projection matrix given the viewport, field of view, and clip distances.
        Viewport viewport = new Viewport(0, 0, 100, 100);  // screen coordinates
        double nearDistance = navigator.getAltitude() * 0.75;
        double farDistance = globe.horizonDistance(navigator.getAltitude()) + globe.horizonDistance(160000);
        Matrix4 projection = new Matrix4();
        projection.setToPerspectiveProjection(viewport.width, viewport.height, 45d /*fovy*/, nearDistance, farDistance);

        // Compute a Cartesian viewing matrix using this Navigator's properties as a Camera.
        Matrix4 modelview = new Matrix4();
        navigator.getAsViewingMatrix(globe, modelview);

        // Compute the Frustum
        Frustum frustum = new Frustum();
        frustum.setToModelviewProjection(projection, modelview, viewport);

        // Evaluate the results with known values captured on 07/19/2016
        //System.out.println(frustumToString(frustum));
        Plane bottom = new Plane(0.17635740224291638, 0.9793994030381801, 0.09836094754823524, -2412232.453445458);
        Plane left = new Plane(-0.12177864151960982, 0.07203573632653165, 0.9899398038070459, 1737116.8972521012);
        Plane right = new Plane(0.7782605589154529, 0.07203573632653174, -0.6237959242640989, 1737116.8972521003);
        Plane top = new Plane(0.48012451515292665, -0.8353279303851167, 0.2677829319947119, 5886466.24794966);
        Plane near = new Plane(0.8577349603804412, 0.1882384504636923, 0.4783900328269719, 4528686.830908618);
        Plane far = new Plane(-0.8577349603804412, -0.1882384504636923, -0.4783900328269719, -2676528.6881595235);

        assertEquals("left", left, frustum.left);
        assertEquals("right", right, frustum.right);
        assertEquals("bottom", bottom, frustum.bottom);
        assertEquals("top", top, frustum.top);
        assertEquals("near", near, frustum.near);
        assertEquals("far", far, frustum.far);
        assertEquals("viewport", viewport, frustum.viewport);
    }

    @Test
    public void testSetToModelviewProjection_SubViewport() throws Exception {
        // The expected test values were obtained via SystemOut on Frustum object
        // at a time in the development cycle when the setToModelviewProjection
        // was known to be working correctly (via observed runtime behavior).
        // This unit test simply tests for changes in the behavior since that time.

        // Create a Frustum similar to the way the WorldWindow does it when picking

        // Setup a Navigator, looking near Oxnard Airport.
        LookAt lookAt = new LookAt().set(34.15, -119.15, 0, WorldWind.ABSOLUTE, 2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        Navigator navigator = new Navigator();
        navigator.setAsLookAt(globe, lookAt);

        // Compute a perspective projection matrix given the viewport, field of view, and clip distances.
        Viewport viewport = new Viewport(0, 0, 100, 100);  // screen coordinates
        Viewport pickViewport = new Viewport(49, 49, 3, 3); // 3x3 viewport centered on a pick point
        double nearDistance = navigator.getAltitude() * 0.75;
        double farDistance = globe.horizonDistance(navigator.getAltitude()) + globe.horizonDistance(160000);
        Matrix4 projection = new Matrix4();
        projection.setToPerspectiveProjection(viewport.width, viewport.height, 45d /*fovy*/, nearDistance, farDistance);

        // Compute a Cartesian viewing matrix using this Navigator's properties as a Camera.
        Matrix4 modelview = new Matrix4();
        navigator.getAsViewingMatrix(globe, modelview);

        // Compute the Frustum
        Frustum frustum = new Frustum();
        frustum.setToModelviewProjection(projection, modelview, viewport, pickViewport);

        // Evaluate the results with known values captured on 06/03/2016
        //System.out.println(frustumToString(frustum));
        Plane bottom = new Plane(-0.15728647066358287, 0.9836490211411795, -0.0877243942936819, -4453465.7217097925);
        Plane left = new Plane(-0.4799755263103557, 0.001559364875310035, 0.8772804925018466, 37603.54528193692);
        Plane right = new Plane(0.5012403287200531, 0.003118408767628064, -0.8653024953109584, 75199.35019616158);
        Plane top = new Plane(0.17858448447919384, -0.9788701700756626, 0.09960307243927863, 4565806.392885632);
        Plane near = new Plane(0.8577349603809148, 0.18823845046641746, 0.4783900328250505, 4528686.830896157);
        Plane far = new Plane(-0.8577349603804465, -0.1882384504638284, -0.4783900328269087, -2676528.6881588553);

        assertEquals("left", left, frustum.left);
        assertEquals("right", right, frustum.right);
        assertEquals("bottom", bottom, frustum.bottom);
        assertEquals("top", top, frustum.top);
        assertEquals("near", near, frustum.near);
        assertEquals("far", far, frustum.far);
        assertEquals("viewport", pickViewport, frustum.viewport);
    }

    @Test
    public void testIntersectsViewport() throws Exception {
        Plane plane = new Plane(0, 0, 0, 0);
        Viewport viewport1 = new Viewport(1, 2, 3, 4);
        Viewport viewport2 = new Viewport(2, 3, 4, 5);

        Frustum frustum1 = new Frustum(plane, plane, plane, plane, plane, plane, viewport1);

        Assert.assertTrue(frustum1.intersectsViewport(viewport2));
    }

    public static String frustumToString(Frustum frustum) {
        return "Frustum{" +
            "bottom=" + frustum.bottom +
            ", left=" + frustum.left +
            ", right=" + frustum.right +
            ", top=" + frustum.top +
            ", near=" + frustum.near +
            ", far=" + frustum.far +
            ", viewport=" + frustum.viewport +
            '}';
    }
}