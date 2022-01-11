/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import android.content.res.Resources;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.shape.ShapeAttributes;

/**
 * @author dcollins
 * @version $Id: GraticuleSupport.java 2372 2014-10-10 18:32:15Z tgaskins $
 */
public class GraticuleSupport {

    private final Map<Renderable, String> renderables = new HashMap<>();
    private final Map<String, GraticuleRenderingParams> namedParams = new HashMap<>();
    private final Map<String, ShapeAttributes> namedShapeAttributes = new HashMap<>();
    private GraticuleRenderingParams defaultParams;

    public void addRenderable(Renderable renderable, String paramsKey) {
        this.renderables.put(renderable, paramsKey);
    }

    void removeAllRenderables() {
        this.renderables.clear();
    }

    public void render(RenderContext rc) {
        this.render(rc, 1);
    }

    public void render(RenderContext rc, double opacity) {

        this.namedShapeAttributes.clear();

        // Render lines and collect text labels
        for (Map.Entry<Renderable, String> entry : this.renderables.entrySet()) {
            Renderable renderable = entry.getKey();
            String paramsKey = entry.getValue();
            GraticuleRenderingParams renderingParams = paramsKey != null ? this.namedParams.get(paramsKey) : null;

            if (renderable instanceof Path) {
                if (renderingParams == null || renderingParams.isDrawLines()) {
                    applyRenderingParams(paramsKey, renderingParams, (Path) renderable, opacity);
                    renderable.render(rc);
                }
            } else if (renderable instanceof Label) {
                if (renderingParams == null || renderingParams.isDrawLabels()) {
                    applyRenderingParams(renderingParams, (Label) renderable, opacity);
                    renderable.render(rc);
                }
            }
        }
    }

    GraticuleRenderingParams getRenderingParams(String key) {
        GraticuleRenderingParams value = this.namedParams.get(key);
        if (value == null) {
            value = new GraticuleRenderingParams();
            initRenderingParams(value);
            if (this.defaultParams != null)
                value.putAll(this.defaultParams);

            this.namedParams.put(key, value);
        }

        return value;
    }

    void setRenderingParams(String key, GraticuleRenderingParams renderingParams) {
        initRenderingParams(renderingParams);
        this.namedParams.put(key, renderingParams);
    }

    public GraticuleRenderingParams getDefaultParams() {
        return this.defaultParams;
    }

    public void setDefaultParams(GraticuleRenderingParams defaultParams) {
        this.defaultParams = defaultParams;
    }

    private void initRenderingParams(GraticuleRenderingParams params) {
        if (params.get(GraticuleRenderingParams.KEY_DRAW_LINES) == null)
            params.put(GraticuleRenderingParams.KEY_DRAW_LINES, Boolean.TRUE);

        if (params.get(GraticuleRenderingParams.KEY_LINE_COLOR) == null)
            params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.WHITE));

        if (params.get(GraticuleRenderingParams.KEY_LINE_WIDTH) == null)
            params.put(GraticuleRenderingParams.KEY_LINE_WIDTH, .5f * Resources.getSystem().getDisplayMetrics().density);

//        if (params.get(GraticuleRenderingParams.KEY_LINE_STYLE) == null)
//            params.put(GraticuleRenderingParams.KEY_LINE_STYLE, GraticuleRenderingParams.VALUE_LINE_STYLE_SOLID);

        if (params.get(GraticuleRenderingParams.KEY_DRAW_LABELS) == null)
            params.put(GraticuleRenderingParams.KEY_DRAW_LABELS, Boolean.TRUE);

        if (params.get(GraticuleRenderingParams.KEY_LABEL_COLOR) == null)
            params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.WHITE));

        if (params.get(GraticuleRenderingParams.KEY_LABEL_TYPEFACE) == null)
            params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));

        if (params.get(GraticuleRenderingParams.KEY_LABEL_SIZE) == null)
            params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 12f * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    private void applyRenderingParams(GraticuleRenderingParams params, Label text, double opacity) {
        if (params != null && text != null) {
            // Apply "label" properties to the Label.
            Object o = params.get(GraticuleRenderingParams.KEY_LABEL_COLOR);
            if (o instanceof Color) {
                Color color = applyOpacity((Color) o, opacity);
                float[] compArray = new float[3];
                android.graphics.Color.colorToHSV(color.toColorInt(), compArray);
                float colorValue = compArray[2] < .5f ? 1f : 0f;
                text.getAttributes().setTextColor(color);
                text.getAttributes().setOutlineColor(new Color(colorValue, colorValue, colorValue, color.alpha));
            }

            o = params.get(GraticuleRenderingParams.KEY_LABEL_TYPEFACE);
            if (o instanceof Typeface) {
                text.getAttributes().setTypeface((Typeface) o);
            }

            o = params.get(GraticuleRenderingParams.KEY_LABEL_SIZE);
            if (o instanceof Float) {
                text.getAttributes().setTextSize((Float) o);
            }
        }
    }

    private void applyRenderingParams(String key, GraticuleRenderingParams params, Path path, double opacity) {
        if (key != null && params != null && path != null) {
            path.setAttributes(this.getLineShapeAttributes(key, params, opacity));
        }
    }

    private ShapeAttributes getLineShapeAttributes(String key, GraticuleRenderingParams params, double opacity) {
        ShapeAttributes attrs = this.namedShapeAttributes.get(key);
        if (attrs == null) {
            attrs = createLineShapeAttributes(params, opacity);
            this.namedShapeAttributes.put(key, attrs);
        }
        return attrs;
    }

    private ShapeAttributes createLineShapeAttributes(GraticuleRenderingParams params, double opacity) {
        ShapeAttributes attrs = new ShapeAttributes();
        attrs.setDrawInterior(false);
        attrs.setDrawOutline(true);
        if (params != null) {
            // Apply "line" properties.
            Object o = params.get(GraticuleRenderingParams.KEY_LINE_COLOR);
            if (o instanceof Color) {
                attrs.setOutlineColor(applyOpacity((Color) o, opacity));
            }

            Float lineWidth = params.getFloatValue(GraticuleRenderingParams.KEY_LINE_WIDTH);
            if (lineWidth != null) {
                attrs.setOutlineWidth(lineWidth);
            }

//            String s = params.getStringValue(GraticuleRenderingParams.KEY_LINE_STYLE);
//            // Draw a solid line.
//            if (GraticuleRenderingParams.VALUE_LINE_STYLE_SOLID.equalsIgnoreCase(s)) {
//                attrs.setOutlineStipplePattern((short) 0xAAAA);
//                attrs.setOutlineStippleFactor(0);
//            }
//            // Draw the line as longer strokes with space in between.
//            else if (GraticuleRenderingParams.VALUE_LINE_STYLE_DASHED.equalsIgnoreCase(s)) {
//                int baseFactor = (int) (lineWidth != null ? Math.round(lineWidth) : 1.0);
//                attrs.setOutlineStipplePattern((short) 0xAAAA);
//                attrs.setOutlineStippleFactor(3 * baseFactor);
//            }
//            // Draw the line as a evenly spaced "square" dots.
//            else if (GraticuleRenderingParams.VALUE_LINE_STYLE_DOTTED.equalsIgnoreCase(s)) {
//                int baseFactor = (int) (lineWidth != null ? Math.round(lineWidth) : 1.0);
//                attrs.setOutlineStipplePattern((short) 0xAAAA);
//                attrs.setOutlineStippleFactor(baseFactor);
//            }
        }
        return attrs;
    }

    private Color applyOpacity(Color color, double opacity) {
        return opacity >= 1 ? color : new Color(color.red, color.green, color.blue, color.alpha * (float) opacity);
    }
}
