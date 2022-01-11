/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class AbstractShapeTest {

    @Before
    public void setUp() {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);
    }

    /**
     * A simple concrete implementation of AbstractShape for testing.
     */
    private static class AbstractShapeImpl extends AbstractShape {

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
    public void testConstructor_Default() {

        AbstractShape shape = new AbstractShapeImpl();

        assertNotNull(shape);
        assertNotNull(shape.attributes);
        assertNull(shape.highlightAttributes);
    }

    @Test
    public void testConstructor_WithAttributes() {
        ShapeAttributes attributes = new ShapeAttributes();

        AbstractShape shape = new AbstractShapeImpl(attributes);

        assertNotNull(shape);
        assertSame(attributes, shape.attributes);
        assertNull(shape.highlightAttributes);
    }

    @Test
    public void testGetAttributes() {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl(attributes);

        ShapeAttributes result = shape.getAttributes();

        assertSame(attributes, result);
    }

    @Test
    public void testSetAttributes() {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl();

        shape.setAttributes(attributes);

        assertSame(attributes, shape.attributes);
    }

    @Test
    public void testGetHighlightAttributes() {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl();
        shape.highlightAttributes = attributes;

        ShapeAttributes result = shape.getHighlightAttributes();

        assertSame(attributes, result);
    }

    @Test
    public void testSetHighlightAttributes() {
        ShapeAttributes attributes = new ShapeAttributes();
        AbstractShape shape = new AbstractShapeImpl();

        shape.setHighlightAttributes(attributes);

        assertSame(attributes, shape.highlightAttributes);
    }

    @Test
    public void testIsHighlighted() {
        AbstractShape shape = new AbstractShapeImpl();
        shape.highlighted = true;

        boolean result = shape.isHighlighted();

        assertTrue(result);
    }

    @Test
    public void testSetHighlighted() {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setHighlighted(true);

        assertTrue(shape.highlighted);
    }

    @Test
    public void testGetAltitudeMode() {
        AbstractShape shape = new AbstractShapeImpl();
        shape.altitudeMode = WorldWind.CLAMP_TO_GROUND;

        int result = shape.getAltitudeMode();

        assertEquals(WorldWind.CLAMP_TO_GROUND, result);
    }

    @Test
    public void testSetAltitudeMode() {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        assertEquals(WorldWind.CLAMP_TO_GROUND, shape.altitudeMode);
    }

    @Test
    public void testGetPathType() {
        AbstractShape shape = new AbstractShapeImpl();
        shape.pathType = WorldWind.RHUMB_LINE;

        int result = shape.getPathType();

        assertEquals(WorldWind.RHUMB_LINE, result);
    }

    @Test
    public void testSetPathType() {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setPathType(WorldWind.RHUMB_LINE);

        assertEquals(WorldWind.RHUMB_LINE, shape.pathType);
    }

    @Test
    public void testGetMaximumIntermediatePoints() {
        AbstractShape shape = new AbstractShapeImpl();
        shape.maximumIntermediatePoints = 123;

        int result = shape.getMaximumIntermediatePoints();

        assertEquals(123, result);
    }

    @Test
    public void testSetMaximumIntermediatePoints() {
        AbstractShape shape = new AbstractShapeImpl();

        shape.setMaximumIntermediatePoints(123);

        assertEquals(123, shape.maximumIntermediatePoints);
    }
}