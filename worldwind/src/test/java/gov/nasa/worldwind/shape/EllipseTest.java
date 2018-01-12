/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)
public class EllipseTest {

    @Before
    public void setUp() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testDefaultConstructor() {
        Ellipse ellipse = new Ellipse();

        assertTrue("default null position", ellipse.getCenter() == null);
        assertEquals("default zero major radius", 0, ellipse.majorRadius, 1e-9);
        assertEquals("default zero minor radius", 0, ellipse.minorRadius, 1e-9);
        assertEquals("default zero heading", 0, ellipse.heading, 1e-9);
    }

    @Test
    public void testConstructor() {
        Position position = new Position(12, 34, 56);
        double majorRadius = 1000;
        double minorRadius = 500;

        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius);

        assertEquals("constructor position", position, ellipse.center);
        assertEquals("constructor major radius", majorRadius, ellipse.majorRadius, 1e-9);
        assertEquals("constructor minor radius", minorRadius, ellipse.minorRadius, 1e-9);
    }

    @Test
    public void testConstructorWithAttriubutes() {
        Position position = new Position(12, 34, 56);
        double majorRadius = 1000;
        double minorRadius = 500;
        ShapeAttributes attrs = new ShapeAttributes();
        attrs.setInteriorColor(new Color(0, 1, 0, 1));
        attrs.setDrawOutline(false);

        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius, attrs);

        assertEquals("constructor position", position, ellipse.center);
        assertEquals("constructor major radius", majorRadius, ellipse.majorRadius, 1e-9);
        assertEquals("constructor minor radius", minorRadius, ellipse.minorRadius, 1e-9);
        assertEquals("constructor attributes", attrs, ellipse.attributes);
    }

    @Test
    public void testCenterGetter() {
        Position position = new Position(12, 34, 56);
        double majorRadius = 1000;
        double minorRadius = 500;
        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius);

        Position actualPosition = ellipse.getCenter();

        assertEquals("center getter", position, actualPosition);
    }

    @Test
    public void testCenterSetterNonNull() {
        Position position = new Position(12, 34, 56);
        Position newPosition = new Position(24, 68, 10);
        double majorRadius = 1000;
        double minorRadius = 500;
        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius);

        ellipse.setCenter(newPosition);

        assertEquals("non-null center setter", newPosition, ellipse.center);
    }

    @Test
    public void testCenterSetterNull() {
        Position newPosition = new Position(24, 68, 10);
        Ellipse ellipse = new Ellipse();

        ellipse.setCenter(newPosition);

        assertEquals("null center setter", newPosition, ellipse.center);
    }

    @Test
    public void testCenterSetterToNull() {
        Position position = new Position(12, 34, 56);
        double majorRadius = 1000;
        double minorRadius = 500;
        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius);

        ellipse.setCenter(null);

        assertEquals("set to null center setter", null, ellipse.center);
    }

    @Test
    public void testMajorRadiusGetter() {
        Position position = new Position(12, 34, 56);
        double majorRadius = 1000;
        double minorRadius = 500;
        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius);

        double actualMajorRadius = ellipse.getMajorRadius();

        assertEquals("major radius getter", majorRadius, actualMajorRadius, 1e-9);
    }

    @Test
    public void testMajorRadiusSetterValid() {
        Ellipse ellipse = new Ellipse();
        double majorRadius = 15000;

        ellipse.setMajorRadius(majorRadius);

        assertEquals("major radius setter valid", majorRadius, ellipse.majorRadius, 1e-9);
    }

    @Test
    public void testMajorRadiusSetterError() {
        Ellipse ellipse = new Ellipse();

        try {
            ellipse.setMajorRadius(-1);
            fail("invalid setter value for major radius");
        } catch (Exception ex) {
            assertTrue("major radius invalid setter exception type", ex instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testMinorRadiusGetter() {
        Position position = new Position(12, 34, 56);
        double majorRadius = 1000;
        double minorRadius = 500;
        Ellipse ellipse = new Ellipse(position, majorRadius, minorRadius);

        double actualMinorRadius = ellipse.getMinorRadius();

        assertEquals("minor radius getter", minorRadius, actualMinorRadius, 1e-9);
    }

    @Test
    public void testMinorRadiusSetterValid() {
        Ellipse ellipse = new Ellipse();
        double minorRadius = 15000;

        ellipse.setMinorRadius(minorRadius);

        assertEquals("minor radius setter valid", minorRadius, ellipse.minorRadius, 1e-9);
    }

    @Test
    public void testMinorRadiusSetterError() {
        Ellipse ellipse = new Ellipse();

        try {
            ellipse.setMinorRadius(-1);
            fail("invalid setter value for minor radius");
        } catch (Exception ex) {
            assertTrue("minor radius invalid setter exception type", ex instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testHeadingGetter() {
        Ellipse ellipse = new Ellipse();
        double heading = 64.3;
        ellipse.heading = heading;

        double actualHeading = ellipse.getHeading();

        assertEquals("heading getter", heading, actualHeading, 1e-9);
    }

    @Test
    public void testHeadingSetter() {
        Ellipse ellipse = new Ellipse();
        double heading = 64.2;

        ellipse.setHeading(heading);

        assertEquals("heading setter", heading, ellipse.heading, 1e-9);
    }

    @Test
    public void testMaxIntervalGetter() {
        Ellipse ellipse = new Ellipse();

        int actualMaxNumberIntervals = ellipse.getMaximumIntervals();

        assertEquals("default number of intervals", ellipse.maximumIntervals, actualMaxNumberIntervals);
    }

    @Test
    public void testMaxIntervalSetterValid() {
        Ellipse ellipse = new Ellipse();
        int maxNumberIntervals = 146;

        ellipse.setMaximumIntervals(maxNumberIntervals);

        assertEquals("max interval setter even", maxNumberIntervals, ellipse.maximumIntervals);
    }

    @Test
    public void testMaxIntervalSetterInvalid() {
        Ellipse ellipse = new Ellipse();

        try {
            ellipse.setMaximumIntervals(-8);
            fail("invalid max interval setting");
        } catch (Exception e) {
            assertTrue("maximum interval exception type", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testCircumference_Circle() {
        Ellipse ellipse = new Ellipse();
        double major = 50;
        double minor = 50;
        ellipse.setMajorRadius(major);
        ellipse.setMinorRadius(minor);
        double expectedCircumference = 2 * Math.PI * major;

        double actualCircumference = ellipse.computeCircumference();

        assertEquals("circle circumference", expectedCircumference, actualCircumference, 1e-9);
    }
}
