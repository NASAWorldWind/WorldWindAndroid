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

import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class) // We mock the Logger class to avoid its calls to android.util.log
public class ShapeAttributesTest {

    @Before
    public void setUp() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testConstructor_Default() throws Exception {

        ShapeAttributes shapeAttributes = new ShapeAttributes();

        assertNotNull(shapeAttributes);
        // Assert default values are as expected.
        assertTrue("drawInterior should be true", shapeAttributes.drawInterior);
        assertTrue("drawOutline should be true", shapeAttributes.drawOutline);
        assertFalse("drawVerticals should be false", shapeAttributes.drawVerticals);
        assertTrue("depthTest should be true", shapeAttributes.depthTest);
        assertFalse("enableLighting should be false", shapeAttributes.enableLighting);
        assertEquals("interiorColor should be white", new Color(1, 1, 1, 1), shapeAttributes.interiorColor);
        assertEquals("outlineColor should be red", new Color(1, 0, 0, 1), shapeAttributes.outlineColor);
        assertEquals("outlineWidth should be 1.0", 1.0f, shapeAttributes.outlineWidth, 0.0f);
        assertNull("interiorImageSource should be null", shapeAttributes.interiorImageSource);
        assertNull("outlineImageSource should be null", shapeAttributes.outlineImageSource);
    }

    @Test
    public void testConstructor_Copy() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        attributes.interiorImageSource = ImageSource.fromObject(new Object());
        attributes.outlineImageSource = ImageSource.fromObject(new Object());

        ShapeAttributes copy = new ShapeAttributes(attributes);

        assertNotNull(copy);
        assertEquals(attributes, copy);
        // Ensure we made a deep copy of the colors
        assertTrue(copy.interiorColor != attributes.interiorColor);
        assertTrue(copy.outlineColor != attributes.outlineColor);
        // Ensure we copied the image sources by reference
        assertTrue(copy.interiorImageSource == attributes.interiorImageSource);
        assertTrue(copy.outlineImageSource == attributes.outlineImageSource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_CopyWithNull() throws Exception {

        ShapeAttributes attributes = new ShapeAttributes(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testSet() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        // create another attribute bundle with differing values
        ShapeAttributes other = new ShapeAttributes();
        other.drawInterior = false;
        other.drawOutline = false;
        other.drawVerticals = true;
        other.depthTest = false;
        other.enableLighting = true;
        other.interiorColor = new Color(0, 0, 0, 0);
        other.outlineColor = new Color(0, 1, 1, 0);
        other.outlineWidth = 0.0f;
        other.interiorImageSource = ImageSource.fromObject(new Object());
        other.outlineImageSource = ImageSource.fromObject(new Object());

        attributes.set(other);

        assertEquals(attributes, other);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();

        attributes.set(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testEquals() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        ShapeAttributes same = new ShapeAttributes();

        assertEquals(same.drawInterior, attributes.drawInterior);
        assertEquals(same.drawOutline, attributes.drawOutline);
        assertEquals(same.drawVerticals, attributes.drawVerticals);
        assertEquals(same.depthTest, attributes.depthTest);
        assertEquals(same.enableLighting, attributes.enableLighting);
        assertEquals(same.interiorColor, attributes.interiorColor);
        assertEquals(same.outlineColor, attributes.outlineColor);
        assertEquals(same.outlineWidth, attributes.outlineWidth, 0.0f);
        assertEquals(same.interiorImageSource, attributes.interiorImageSource);
        assertEquals(same.outlineImageSource, attributes.outlineImageSource);
        assertEquals(attributes, attributes);
        assertEquals(attributes, same);
        assertEquals(same, attributes);
    }

    @Test
    public void testInequality() throws Exception {
        ShapeAttributes typical = new ShapeAttributes();
        ShapeAttributes different = new ShapeAttributes();
        different.drawInterior = false;
        different.drawOutline = false;
        different.drawVerticals = true;
        different.depthTest = false;
        different.enableLighting = true;
        different.interiorColor = new Color(0, 0, 0, 0);
        different.outlineColor = new Color(0, 1, 1, 0);
        different.outlineWidth = 0.0f;
        different.interiorImageSource = ImageSource.fromObject(new Object());
        different.outlineImageSource = ImageSource.fromObject(new Object());

        assertNotEquals(different.drawInterior, typical.drawInterior);
        assertNotEquals(different.drawOutline, typical.drawOutline);
        assertNotEquals(different.drawVerticals, typical.drawVerticals);
        assertNotEquals(different.depthTest, typical.depthTest);
        assertNotEquals(different.enableLighting, typical.enableLighting);
        assertNotEquals(different.interiorColor, typical.interiorColor);
        assertNotEquals(different.outlineColor, typical.outlineColor);
        assertNotEquals(different.outlineWidth, typical.outlineWidth, 0.0f);
        assertNotEquals(different.interiorImageSource, typical.interiorImageSource);
        assertNotEquals(different.outlineImageSource, typical.outlineImageSource);
        assertNotEquals(different, typical);
        assertNotEquals(typical, different);
        assertNotEquals(typical, null);
    }

    @Test
    public void testHashCode() throws Exception {
        // Three differing sets of attributes
        ShapeAttributes a = new ShapeAttributes();
        ShapeAttributes b = new ShapeAttributes();
        ShapeAttributes c = new ShapeAttributes();
        b.setDrawInterior(false);
        c.setDrawOutline(false);

        int aHash = a.hashCode();
        int bHash = b.hashCode();
        int cHash = c.hashCode();

        assertNotEquals("a hash vs b hash", bHash, aHash);
        assertNotEquals("b hash vs c hash", cHash, bHash);
        assertNotEquals("c hash vs a hash", aHash, cHash);
    }

    @Test
    public void testIsDrawInterior() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();

        // Assert the getter is returning the changed member
        attributes.drawInterior = true;
        assertTrue(attributes.isDrawInterior());

        attributes.drawInterior = false;
        assertFalse(attributes.isDrawInterior());
    }

    @Test
    public void testSetDrawInterior() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        attributes.drawInterior = true;

        attributes.setDrawInterior(false);

        assertFalse(attributes.drawInterior);
    }

    @Test
    public void testIsDrawOutline() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();

        // Assert the getter is returning the changed member
        attributes.drawOutline = true;
        assertTrue(attributes.isDrawOutline());

        attributes.drawOutline = false;
        assertFalse(attributes.isDrawOutline());
    }

    @Test
    public void testSetDrawOutline() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        attributes.drawOutline = true;

        attributes.setDrawOutline(false);

        assertFalse(attributes.drawOutline);
    }

    @Test
    public void testIsDrawVerticals() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        // Assert the getter is returning the changed member
        attributes.drawVerticals = true;
        assertTrue(attributes.isDrawVerticals());

        attributes.drawVerticals = false;
        assertFalse(attributes.isDrawVerticals());
    }

    @Test
    public void testSetDrawVerticals() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        attributes.drawVerticals = true;

        attributes.setDrawVerticals(false);

        assertFalse(attributes.drawVerticals);
    }

    @Test
    public void testIsDepthTest() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        // Assert the getter is returning the changed member
        attributes.depthTest = true;
        assertTrue(attributes.isDepthTest());

        attributes.depthTest = false;
        assertFalse(attributes.isDepthTest());
    }

    @Test
    public void testSetDepthTest() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        attributes.depthTest = true;

        attributes.setDepthTest(false);

        assertFalse(attributes.depthTest);
    }

    @Test
    public void testIsEnableLighting() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        // Assert the getter is returning the changed member
        attributes.enableLighting = true;
        assertTrue(attributes.isEnableLighting());

        attributes.enableLighting = false;
        assertFalse(attributes.isEnableLighting());
    }

    @Test
    public void testSetEnableLighting() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        attributes.enableLighting = true;

        attributes.setEnableLighting(false);

        assertFalse(attributes.enableLighting);
    }

    @Test
    public void testGetInteriorColor() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        Color black = new Color(0, 0, 0, 1);
        attributes.interiorColor = black;

        assertEquals(black, attributes.getInteriorColor());
    }

    @Test
    public void testSetInteriorColor() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        Color black = new Color(0, 0, 0, 1);

        attributes.setInteriorColor(black);

        // Verify that the object is an equivalent deep copy.
        assertEquals(black, attributes.interiorColor);
        assertTrue(black != attributes.interiorColor);
    }

    @Test
    public void testGetOutlineColor() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        Color black = new Color(0, 0, 0, 1);
        attributes.outlineColor = black;

        assertEquals(black, attributes.getOutlineColor());
    }

    @Test
    public void testSetOutlineColor() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        Color black = new Color(0, 0, 0, 1);

        attributes.setOutlineColor(black);

        // Verify that the object is an equivalent deep copy.
        assertEquals(black, attributes.outlineColor);
        assertTrue(black != attributes.outlineColor);
    }

    @Test
    public void testGetOutlineWidth() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        float width = 2.5f;
        attributes.outlineWidth = width;

        assertEquals(width, attributes.getOutlineWidth(), 1e-15);
    }

    @Test
    public void testSetOutlineWidth() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        float width = 2.5f;

        attributes.setOutlineWidth(width);

        assertEquals(width, attributes.outlineWidth, 1e-15);
    }

    @Test
    public void testGetInteriorImageSource() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        ImageSource imageSource = ImageSource.fromObject(new Object());
        attributes.interiorImageSource = imageSource;

        assertEquals(imageSource, attributes.getInteriorImageSource());
    }

    @Test
    public void testSetInteriorImageSource() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        ImageSource imageSource = ImageSource.fromObject(new Object());

        attributes.setInteriorImageSource(imageSource);

        assertEquals(imageSource, attributes.interiorImageSource);
    }

    @Test
    public void testGetOutlineImageSource() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        ImageSource imageSource = ImageSource.fromObject(new Object());
        attributes.outlineImageSource = imageSource;

        assertEquals(imageSource, attributes.getOutlineImageSource());
    }

    @Test
    public void testSetOutlineImageSource() throws Exception {
        ShapeAttributes attributes = new ShapeAttributes();
        ImageSource imageSource = ImageSource.fromObject(new Object());

        attributes.setOutlineImageSource(imageSource);

        assertEquals(imageSource, attributes.outlineImageSource);
    }
}