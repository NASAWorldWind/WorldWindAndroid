/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class AbstractShapeTest {

    @Before
    public void setUp() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testConstructor_Default() throws Exception {
        AbstractShape shape = new AbstractShape();
        assertNotNull(shape);
        assertNotNull(shape.attributes);
        assertNull(shape.highlightAttributes);
    }

    @Test
    public void testConstructor_WithAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShape(attributes);
        assertNotNull(shape);
        assertTrue(attributes == shape.attributes);
        assertNull(shape.highlightAttributes);
    }

    @Test
    public void testGetAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShape(attributes);

        assertTrue(attributes == shape.getAttributes());
    }

    @Test
    public void testSetAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShape();

        shape.setAttributes(attributes);

        assertTrue(attributes == shape.attributes);
    }

    @Test
    public void testGetHighlightAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShape();
        shape.highlightAttributes = attributes;

        assertTrue(attributes == shape.getHighlightAttributes());
    }

    @Test
    public void testSetHighlightAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShape();

        shape.setHighlightAttributes(attributes);

        assertTrue(attributes == shape.highlightAttributes);
    }

    @Test
    public void testIsHighlighted() throws Exception {
        AbstractShape shape = new AbstractShape();
        shape.highlighted = true;

        assertTrue(shape.isHighlighted());
    }

    @Test
    public void testSetHighlighted() throws Exception {
        AbstractShape shape = new AbstractShape();

        shape.setHighlighted(true);

        assertTrue(shape.highlighted);
    }

    @Test
    public void testGetAltitudeMode() throws Exception {
        AbstractShape shape = new AbstractShape();
        shape.altitudeMode = WorldWind.CLAMP_TO_GROUND;

        assertEquals(WorldWind.CLAMP_TO_GROUND, shape.getAltitudeMode());
    }

    @Test
    public void testSetAltitudeMode() throws Exception {
        AbstractShape shape = new AbstractShape();

        shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        assertEquals(WorldWind.CLAMP_TO_GROUND, shape.altitudeMode);
    }

    @Test
    public void testGetPathType() throws Exception {
        AbstractShape shape = new AbstractShape();
        shape.pathType = WorldWind.RHUMB_LINE;

        assertEquals(WorldWind.RHUMB_LINE, shape.getPathType());
    }

    @Test
    public void testSetPathType() throws Exception {
        AbstractShape shape = new AbstractShape();
        shape.setPathType(WorldWind.RHUMB_LINE);

        assertEquals(WorldWind.RHUMB_LINE, shape.pathType);
    }
}