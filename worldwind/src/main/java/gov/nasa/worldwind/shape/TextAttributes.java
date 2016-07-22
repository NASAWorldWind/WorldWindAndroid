/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Typeface;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.Color;

/**
 * Holds attributes applied to shapes text and {@link Placemark} labels.
 */
public class TextAttributes {

    protected Color color;

    protected Typeface font;

    protected Offset offset;

    protected double scale;

    protected boolean depthTest;

    public TextAttributes() {
        this.color = new Color(1, 1, 1, 1);
        this.font = Typeface.DEFAULT;
        this.offset = new Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.0);
        this.scale = 1d;
        this.depthTest = false;
    }

    /**
     * Constructs a text attributes bundle.
     *
     * @param attributes Attributes to initialize this attributes instance to. May be null, in which
     *                         case the new instance contains default attributes.
     */
    public TextAttributes(TextAttributes attributes) {
        this.color = new Color(attributes.color);
        this.font = attributes.font;
        this.offset = new Offset(attributes.offset);
        this.scale = attributes.scale;
        this.depthTest = attributes.depthTest;
    }

    public TextAttributes set(TextAttributes attributes) {
        this.color.set(attributes.color);
        this.font = attributes.font;
        this.offset.set(attributes.offset);
        this.scale = attributes.scale;
        this.depthTest = attributes.depthTest;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextAttributes that = (TextAttributes) o;

        if (Double.compare(that.scale, scale) != 0) return false;
        if (depthTest != that.depthTest) return false;
        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (font != null ? !font.equals(that.font) : that.font != null) return false;
        return !(offset != null ? !offset.equals(that.offset) : that.offset != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = color != null ? color.hashCode() : 0;
        result = 31 * result + (font != null ? font.hashCode() : 0);
        result = 31 * result + (offset != null ? offset.hashCode() : 0);
        temp = Double.doubleToLongBits(scale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (depthTest ? 1 : 0);
        return result;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Typeface getFont() {
        return font;
    }

    public void setFont(Typeface font) {
        this.font = font;
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public void setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
    }
}
