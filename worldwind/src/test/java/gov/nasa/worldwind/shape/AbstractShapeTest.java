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
import gov.nasa.worldwind.render.RenderContext;
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

    /**
     * A simple concrete implementation of AbstractShape for testing.
     */
    private class AbstractShapeImpl extends AbstractShape {

        public AbstractShapeImpl() {
            super();
        }

        public AbstractShapeImpl(ShapeAttributes attributes) {
            super(attributes);
        }

        protected void reset() {
        }

        protected void makeDrawable(RenderContext rc) {
        }
    }

    @Test
    public void testConstructor_Default() throws Exception {

        AbstractShape shape = new AbstractShapeImpl();

        assertNotNull(shape);
        assertNotNull(shape.attributes);
        assertNull(shape.highlightAttributes);
    }

    @Test
    public void testConstructor_WithAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();

        AbstractShape shape = new AbstractShapeImpl(attributes);

        assertNotNull(shape);
        assertTrue(attributes == shape.attributes);
        assertNull(shape.highlightAttributes);
    }

    @Test
    public void testGetAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl(attributes);

        ShapeAttributes result = shape.getAttributes();

        assertTrue(attributes == result);
    }

    @Test
    public void testSetAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl();

        shape.setAttributes(attributes);

        assertTrue(attributes == shape.attributes);
    }

    @Test
    public void testGetHighlightAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl();
        shape.highlightAttributes = attributes;

        ShapeAttributes result = shape.getHighlightAttributes();

        assertTrue(attributes == result);
    }

    @Test
    public void testSetHighlightAttributes() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl();

        shape.setHighlightAttributes(attributes);

        assertTrue(attributes == shape.highlightAttributes);
    }

    @Test
    public void testIsHighlighted() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();
        shape.highlighted = true;

        boolean result = shape.isHighlighted();

        assertTrue(result);
    }

    @Test
    public void testSetHighlighted() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setHighlighted(true);

        assertTrue(shape.highlighted);
    }

    @Test
    public void testGetAltitudeMode() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();
        shape.altitudeMode = WorldWind.CLAMP_TO_GROUND;

        int result = shape.getAltitudeMode();

        assertEquals(WorldWind.CLAMP_TO_GROUND, result);
    }

    @Test
    public void testSetAltitudeMode() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        assertEquals(WorldWind.CLAMP_TO_GROUND, shape.altitudeMode);
    }

    @Test
    public void testGetPathType() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();
        shape.pathType = WorldWind.RHUMB_LINE;

        int result = shape.getPathType();

        assertEquals(WorldWind.RHUMB_LINE, result);
    }

    @Test
    public void testSetPathType() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setPathType(WorldWind.RHUMB_LINE);

        assertEquals(WorldWind.RHUMB_LINE, shape.pathType);
    }

    @Test
    public void testGetMaximumIntermediatePoints() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();
        shape.maximumIntermediatePoints = 123;

        int result = shape.getMaximumIntermediatePoints();

        assertEquals(123, result);
    }

    @Test
    public void testSetMaximumIntermediatePoints() throws Exception {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setMaximumIntermediatePoints(123);

        assertEquals(123, shape.maximumIntermediatePoints);
    }
}