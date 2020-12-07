/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import android.graphics.Typeface;

import java.util.HashMap;

import gov.nasa.worldwind.render.Color;

/**
 * @author dcollins
 * @version $Id: GraticuleRenderingParams.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class GraticuleRenderingParams extends HashMap<String, Object> {
    static final String KEY_DRAW_LINES = "DrawGraticule";
    static final String KEY_LINE_COLOR = "GraticuleLineColor";
    static final String KEY_LINE_WIDTH = "GraticuleLineWidth";
//    static final String KEY_LINE_STYLE = "GraticuleLineStyle";
//    static final String KEY_LINE_CONFORMANCE = "GraticuleLineConformance";
    static final String KEY_DRAW_LABELS = "DrawLabels";
    static final String KEY_LABEL_COLOR = "LabelColor";
    static final String KEY_LABEL_TYPEFACE = "LabelTypeface";
    static final String KEY_LABEL_SIZE = "LabelSize";
//    static final String VALUE_LINE_STYLE_SOLID = "LineStyleSolid";
//    static final String VALUE_LINE_STYLE_DASHED = "LineStyleDashed";
//    static final String VALUE_LINE_STYLE_DOTTED = "LineStyleDotted";

    boolean isDrawLines() {
        Object value = get(KEY_DRAW_LINES);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    void setDrawLines(boolean drawLines) {
        put(KEY_DRAW_LINES, drawLines);
    }

    Color getLineColor() {
        Object value = get(KEY_LINE_COLOR);
        return value instanceof Color ? (Color) value : null;
    }

    void setLineColor(Color color) {
        put(KEY_LINE_COLOR, color);
    }

    double getLineWidth() {
        Object value = get(KEY_LINE_WIDTH);
        return value instanceof Double ? (Double) value : 0;
    }

    void setLineWidth(double lineWidth) {
        put(KEY_LINE_WIDTH, lineWidth);
    }

//    String getLineStyle() {
//        Object value = get(KEY_LINE_STYLE);
//        return value instanceof String ? (String) value : null;
//    }
//
//    void setLineStyle(String lineStyle) {
//        put(KEY_LINE_STYLE, lineStyle);
//    }

    boolean isDrawLabels() {
        Object value = get(KEY_DRAW_LABELS);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    void setDrawLabels(boolean drawLabels) {
        put(KEY_DRAW_LABELS, drawLabels);
    }

    Color getLabelColor() {
        Object value = get(KEY_LABEL_COLOR);
        return value instanceof Color ? (Color) value : null;
    }

    void setLabelColor(Color color) {
        put(KEY_LABEL_COLOR, color);
    }

    Typeface getLabelTypeface() {
        Object value = get(KEY_LABEL_TYPEFACE);
        return value instanceof Typeface ? (Typeface) value : null;
    }

    void setLabelTypeface(Typeface font) {
        put(KEY_LABEL_TYPEFACE, font);
    }

    Float getLabelSize() {
        Object value = get(KEY_LABEL_SIZE);
        return value instanceof Float ? (Float) value : null;
    }

    void setLabelSize(Float size) {
        put(KEY_LABEL_SIZE, size);
    }

    String getStringValue(String key) {
        Object value = this.get(key);
        return value != null ? value.toString() : null;
    }

    Float getFloatValue(String key) {
        Object o = get(key);
        if (o == null) return null;

        if (o instanceof Float) return (Float) o;

        String v = getStringValue(key);

        if (v == null) return null;
        else return Float.parseFloat(v);
    }
}
