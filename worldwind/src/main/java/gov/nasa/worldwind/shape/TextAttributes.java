/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Typeface;

import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Logger;

/**
 * Holds attributes applied to text shapes and {@link Placemark} labels.
 */
public class TextAttributes {

    protected Color textColor;

    protected Offset textOffset;

    protected float textSize;

    protected Typeface typeface;

    protected boolean enableOutline;

    protected boolean enableDepthTest;

    protected float outlineWidth;

    public TextAttributes() {
        this.textColor = new Color(1, 1, 1, 1);
        this.textOffset = Offset.bottomCenter();
        this.textSize = 24;
        this.typeface = null;
        this.enableOutline = true;
        this.enableDepthTest = true;
        this.outlineWidth = 3;
    }

    public TextAttributes(TextAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TextAttributes", "constructor", "missingAttributes"));
        }

        this.textColor = new Color(attributes.textColor);
        this.textOffset = new Offset(attributes.textOffset);
        this.textSize = attributes.textSize;
        this.typeface = attributes.typeface;
        this.enableOutline = attributes.enableOutline;
        this.enableDepthTest = attributes.enableDepthTest;
        this.outlineWidth = attributes.outlineWidth;
    }

    public TextAttributes set(TextAttributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TextAttributes", "set", "missingAttributes"));
        }

        this.textColor.set(attributes.textColor);
        this.textOffset.set(attributes.textOffset);
        this.textSize = attributes.textSize;
        this.typeface = attributes.typeface;
        this.enableOutline = attributes.enableOutline;
        this.enableDepthTest = attributes.enableDepthTest;
        this.outlineWidth = attributes.outlineWidth;

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        TextAttributes that = (TextAttributes) o;
        return this.textColor.equals(that.textColor)
            && this.textOffset.equals(that.textOffset)
            && this.textSize == that.textSize
            && ((this.typeface == null) ? (that.typeface == null) : this.typeface.equals(that.typeface))
            && this.enableOutline == that.enableOutline
            && this.enableDepthTest == that.enableDepthTest
            && this.outlineWidth == that.outlineWidth;
    }

    @Override
    public int hashCode() {
        int result = this.textColor.hashCode();
        result = 31 * result + this.textOffset.hashCode();
        result = 31 * result + (this.textSize != +0.0f ? Float.floatToIntBits(this.textSize) : 0);
        result = 31 * result + (this.typeface != null ? this.typeface.hashCode() : 0);
        result = 31 * result + (this.enableOutline ? 1 : 0);
        result = 31 * result + (this.enableDepthTest ? 1 : 0);
        result = 31 * result + (this.outlineWidth != +0.0f ? Float.floatToIntBits(this.outlineWidth) : 0);
        return result;
    }

    public Color getTextColor() {
        return this.textColor;
    }

    public TextAttributes setTextColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TextAttributes", "setTextColor", "missingColor"));
        }

        this.textColor.set(color);
        return this;
    }

    public Offset getTextOffset() {
        return this.textOffset;
    }

    public TextAttributes setTextOffset(Offset offset) {
        if (offset == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TextAttributes", "setTextOffset", "missingOffset"));
        }

        this.textOffset.set(offset);
        return this;
    }

    public float getTextSize() {
        return this.textSize;
    }

    public TextAttributes setTextSize(float size) {
        this.textSize = size;
        return this;
    }

    public Typeface getTypeface() {
        return this.typeface;
    }

    public TextAttributes setTypeface(Typeface typeface) {
        this.typeface = typeface;
        return this;
    }

    public boolean isEnableOutline() {
        return this.enableOutline;
    }

    public TextAttributes setEnableOutline(boolean enable) {
        this.enableOutline = enable;
        return this;
    }

    public boolean isEnableDepthTest() {
        return this.enableDepthTest;
    }

    public TextAttributes setEnableDepthTest(boolean enable) {
        this.enableDepthTest = enable;
        return this;
    }

    public float getOutlineWidth() {
        return this.outlineWidth;
    }

    public TextAttributes setOutlineWidth(float lineWidth) {
        this.outlineWidth = lineWidth;
        return this;
    }
}
