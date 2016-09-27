/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Typeface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest({Logger.class, Typeface.class}) // We mock the Logger class to avoid its calls to android.util.log
public class TextAttributesTest {

    @Before
    public void setUp() throws Exception {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);

        // Mock the static Typeface methods used by TextAttributes
        Typeface defaultTypeface = PowerMockito.mock(Typeface.class);
        PowerMockito.mockStatic(Typeface.class);
        PowerMockito.when(Typeface.class, "defaultFromStyle", Typeface.NORMAL).thenReturn(defaultTypeface);
    }

    @Test
    public void testConstructor_Default() throws Exception {

        TextAttributes attributes = new TextAttributes();

        assertNotNull(attributes);
        // Assert default values are as expected.
        assertEquals("textColor should be white", new Color(1, 1, 1, 1), attributes.textColor);
        assertEquals("textOffset should be bottom center", Offset.bottomCenter(), attributes.textOffset);
        assertEquals("textSize should be 24.0", 24.0f, attributes.textSize, 0.0f);
        assertEquals("typeface should be null", null, attributes.typeface);
        assertEquals("enableOutline should be true", true, attributes.enableOutline);
        assertEquals("enableDepthTest should be true", true, attributes.enableDepthTest);
        assertEquals("outlineWidth should be 3.0", 3.0f, attributes.outlineWidth, 0.0f);
    }

    @Test
    public void testConstructor_Copy() throws Exception {
        TextAttributes attributes = new TextAttributes();
        attributes.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

        TextAttributes copy = new TextAttributes(attributes);

        assertNotNull(copy);
        assertEquals(attributes, copy);
        // Ensure we made a deep copy of the colors
        assertTrue(copy.textColor != attributes.textColor);
        assertTrue(copy.textOffset != attributes.textOffset);
        // Ensure we copied the typeface by reference
        assertTrue(copy.typeface == attributes.typeface);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_CopyWithNull() throws Exception {

        new TextAttributes(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testSet() throws Exception {
        TextAttributes attributes = new TextAttributes();
        // create another attribute bundle with differing values
        TextAttributes other = new TextAttributes();
        other.textColor = new Color(0, 0, 0, 0);
        other.textOffset = new Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_PIXELS, 0);
        other.textSize = 0.0f;
        other.typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        other.enableOutline = false;
        other.enableDepthTest = false;
        other.outlineWidth = 0.0f;

        attributes.set(other);

        assertEquals(attributes, other);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_WithNull() throws Exception {
        TextAttributes attributes = new TextAttributes();

        attributes.set(null);

        fail("Expected an IllegalArgumentException to be thrown.");
    }

    @Test
    public void testEquals() throws Exception {
        TextAttributes attributes = new TextAttributes();
        TextAttributes same = new TextAttributes();

        assertEquals(same.textColor, attributes.textColor);
        assertEquals(same.textOffset, attributes.textOffset);
        assertEquals(same.textSize, attributes.textSize, 0.0f);
        assertEquals(same.typeface, attributes.typeface);
        assertEquals(same.enableOutline, attributes.enableOutline);
        assertEquals(same.enableDepthTest, attributes.enableDepthTest);
        assertEquals(same.outlineWidth, attributes.outlineWidth, 0.0f);
        assertEquals(attributes, attributes);
        assertEquals(attributes, same);
        assertEquals(same, attributes);
    }

    @Test
    public void testInequality() throws Exception {
        TextAttributes typical = new TextAttributes();
        TextAttributes different = new TextAttributes();
        different.textColor = new Color(0, 0, 0, 0);
        different.textOffset = new Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_PIXELS, 0);
        different.textSize = 0.0f;
        different.typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        different.enableOutline = false;
        different.enableDepthTest = false;
        different.outlineWidth = 0.0f;

        assertNotEquals(different.textColor, typical.textColor);
        assertNotEquals(different.textOffset, typical.textOffset);
        assertNotEquals(different.textSize, typical.textSize);
        assertNotEquals(different.typeface, typical.typeface);
        assertNotEquals(different.enableOutline, typical.enableOutline);
        assertNotEquals(different.enableDepthTest, typical.enableDepthTest);
        assertNotEquals(different.outlineWidth, typical.outlineWidth);
        assertNotEquals(different, typical);
        assertNotEquals(typical, different);
        assertNotEquals(typical, null);
    }

    @Test
    public void testHashCode() throws Exception {
        // Three differing sets of attributes
        TextAttributes a = new TextAttributes();
        TextAttributes b = new TextAttributes();
        TextAttributes c = new TextAttributes();
        b.setTextColor(new Color(0, 0, 0, 0));
        c.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

        int aHash = a.hashCode();
        int bHash = b.hashCode();
        int cHash = c.hashCode();

        assertNotEquals("a hash vs b hash", bHash, aHash);
        assertNotEquals("b hash vs c hash", cHash, bHash);
        assertNotEquals("c hash vs a hash", aHash, cHash);
    }

    @Test
    public void testGetTextColor() throws Exception {
        TextAttributes attributes = new TextAttributes();
        Color black = new Color(0, 0, 0, 1);
        attributes.textColor = black;

        assertEquals(black, attributes.getTextColor());
    }

    @Test
    public void testSetTextColor() throws Exception {
        TextAttributes attributes = new TextAttributes();
        Color black = new Color(0, 0, 0, 1);

        attributes.setTextColor(black);

        // Verify that the object is an equivalent deep copy.
        assertEquals(black, attributes.textColor);
        assertTrue(black != attributes.textColor);
    }

    @Test
    public void testGetTextOffset() throws Exception {
        TextAttributes attributes = new TextAttributes();
        Offset lowerLeft = new Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_PIXELS, 0);
        attributes.textOffset = lowerLeft;

        assertEquals(lowerLeft, attributes.getTextOffset());
    }

    @Test
    public void testSetTextOffset() throws Exception {
        TextAttributes attributes = new TextAttributes();
        Offset lowerLeft = new Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_PIXELS, 0);

        attributes.setTextOffset(lowerLeft);

        // Verify that the object is an equivalent deep copy.
        assertEquals(lowerLeft, attributes.textOffset);
        assertTrue(lowerLeft != attributes.textOffset);
    }

    @Test
    public void testGetTextSize() throws Exception {
        TextAttributes attributes = new TextAttributes();
        float size = 2.5f;
        attributes.textSize = size;

        assertEquals(size, attributes.getTextSize(), 1.0e-15);
    }

    @Test
    public void testSetTextSize() throws Exception {
        TextAttributes attributes = new TextAttributes();
        float size = 2.5f;

        attributes.setTextSize(size);

        assertEquals(size, attributes.textSize, 0.0f);
    }

    @Test
    public void testGetTypeface() throws Exception {
        TextAttributes attributes = new TextAttributes();
        Typeface typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        attributes.typeface = typeface;

        assertEquals(typeface, attributes.getTypeface());
    }

    @Test
    public void testSetTypeface() throws Exception {
        TextAttributes attributes = new TextAttributes();
        Typeface typeface = Typeface.defaultFromStyle(Typeface.NORMAL);

        attributes.setTypeface(typeface);

        assertEquals(typeface, attributes.typeface);
    }

    @Test
    public void testGetTypeface_Null() throws Exception {
        TextAttributes attributes = new TextAttributes();

        attributes.typeface = null;

        assertEquals(null, attributes.getTypeface());
    }

    @Test
    public void testSetTypeface_Null() throws Exception {
        TextAttributes attributes = new TextAttributes();

        attributes.setTypeface(null);

        assertEquals(null, attributes.typeface);
    }

    @Test
    public void testGetEnableOutline() throws Exception {
        TextAttributes attributes = new TextAttributes();
        boolean enable = false;
        attributes.enableOutline = enable;

        assertEquals(enable, attributes.isEnableOutline());
    }

    @Test
    public void testSetEnableOutline() throws Exception {
        TextAttributes attributes = new TextAttributes();
        boolean enable = false;

        attributes.setEnableOutline(enable);

        assertEquals(enable, attributes.enableOutline);
    }

    @Test
    public void testGetEnableDepthTest() throws Exception {
        TextAttributes attributes = new TextAttributes();
        boolean enable = false;
        attributes.enableDepthTest = enable;

        assertEquals(enable, attributes.isEnableDepthTest());
    }

    @Test
    public void testSetEnableDepthTest() throws Exception {
        TextAttributes attributes = new TextAttributes();
        boolean enable = false;

        attributes.setEnableDepthTest(enable);

        assertEquals(enable, attributes.enableDepthTest);
    }

    @Test
    public void testGetOutlineWidth() throws Exception {
        TextAttributes attributes = new TextAttributes();
        float width = 0.0f;
        attributes.outlineWidth = width;

        assertEquals(width, attributes.getOutlineWidth(), 1.0e-15);
    }

    @Test
    public void testSetOutlineWidth() throws Exception {
        TextAttributes attributes = new TextAttributes();
        float width = 0.0f;

        attributes.setOutlineWidth(width);

        assertEquals(width, attributes.outlineWidth, 0.0f);
    }
}