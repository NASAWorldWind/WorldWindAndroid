/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import org.junit.Test;

import gov.nasa.worldwind.shape.TextAttributes;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TextCacheKeyTest {

    @Test
    public void testIdentical() {
        // Common Text
        String text = "Testing";

        // Cache Key 1
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, null);
        // Cache Key 2
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, null);

        assertTrue("TextCacheKey equals", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey hashcode", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }

    /**
     * Test identical cache keys are equal and generate identical hash codes. The {@Typeface} class setting is ignored
     * as the Android environment is required for non-null values. This has the effect of testing properties set to null
     * during equivalency testing.
     */
    @Test
    public void testIdenticalWithAttributes() {
        // Common Text Attributes
        TextAttributes attrs = new TextAttributes();
        attrs.setEnableOutline(true);
        attrs.setOutlineColor(new Color(0.5f, 0.2f, 0.3f, 1));
        attrs.setOutlineWidth(15);
        attrs.setTextColor(new Color(1, 1, 0.2f, 1));
        attrs.setTextSize(90);
        // Common Text
        String text = "Testing";

        // Cache Key 1
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, attrs);
        // Cache Key 2
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, attrs);

        assertTrue("TextCacheKey with attributes equals", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey with attributes hashcode", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }

    @Test
    public void testNullToNonNullAttributesSet() {
        String text = "Testing";
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, null);
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, null);

        assertTrue("TextCacheKey null to non-null equals pre", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey null to non-null hashcode pre", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());

        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setOutlineWidth(5);
        textCacheKeyTwo.set(text, textAttributes);

        assertFalse("TextCacheKey null to non-null equals post", textCacheKeyOne.equals(textCacheKeyTwo));
        assertFalse("TextCacheKey null to non-null hashcode post", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }

    @Test
    public void testNullToNullAttributesSet() {
        String text = "Testing";
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, null);
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, null);

        assertTrue("TextCacheKey null to null equals pre", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey null to null hashcode pre", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());

        textCacheKeyTwo.set(text, null);

        assertTrue("TextCacheKey null to null equals post", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey null to null hashcode post", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }

    @Test
    public void testNonNullToNullAttributesSet() {
        String text = "Testing";
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setOutlineWidth(5);
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, textAttributes);
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, textAttributes);

        assertTrue("TextCacheKey non-null to null equals pre", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey non-null to null hashcode pre", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());

        textCacheKeyTwo.set(text, null);

        assertFalse("TextCacheKey non-null to null equals post", textCacheKeyOne.equals(textCacheKeyTwo));
        assertFalse("TextCacheKey non-null to null hashcode post", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }

    @Test
    public void testNonNullToNonNullAttributesSet() {
        String text = "Testing";
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setOutlineWidth(5);
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, textAttributes);
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, textAttributes);

        assertTrue("TextCacheKey non-null to non-null equals pre", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey non-null to non-null hashcode pre", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());

        textCacheKeyTwo.set(text, textAttributes);

        assertTrue("TextCacheKey non-null to non-null equals post", textCacheKeyOne.equals(textCacheKeyTwo));
        assertTrue("TextCacheKey non-null to non-null hashcode post", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }

    @Test
    public void testModifiedAttrs() {
        // Common Text Attributes
        TextAttributes attrs = new TextAttributes();
        attrs.setEnableOutline(true);
        attrs.setOutlineColor(new Color(0.5f, 0.2f, 0.3f, 1));
        attrs.setOutlineWidth(15);
        attrs.setTextColor(new Color(1, 1, 0.2f, 1));
        attrs.setTextSize(90);
        // Common Text
        String text = "Testing";

        // Cache Key 1
        RenderContext.TextCacheKey textCacheKeyOne = new RenderContext.TextCacheKey().set(text, attrs);
        // Modify color property
        Color outlineColor = attrs.getOutlineColor().set(1, 1, 0.3f, 1);
        attrs.setOutlineColor(outlineColor);
        // Cache Key 2
        RenderContext.TextCacheKey textCacheKeyTwo = new RenderContext.TextCacheKey().set(text, attrs);

        assertFalse("TextCacheKey modified attributes equals", textCacheKeyOne.equals(textCacheKeyTwo));
        assertFalse("TextCacheKey modified attributes hashcode", textCacheKeyOne.hashCode() == textCacheKeyTwo.hashCode());
    }
}
