/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableTessellation;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;

public class ShowTessellationLayer extends AbstractLayer {

    protected Color color = new Color();

    public ShowTessellationLayer() {
        super("Terrain Tessellation");
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ShowTessellationLayer", "setColor", "missingColor"));
        }

        this.color.set(color);
    }

    @Override
    protected void doRender(DrawContext dc) {

        if (dc.terrain.getSector().isEmpty()) {
            return; // no terrain to render
        }

        // Use World Wind's basic GLSL program.
        BasicShaderProgram program = (BasicShaderProgram) dc.getShaderProgram(BasicShaderProgram.KEY);
        if (program == null) {
            program = (BasicShaderProgram) dc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(dc.resources));
        }

        Pool<DrawableTessellation> pool = dc.getDrawablePool(DrawableTessellation.class);
        Drawable drawable = DrawableTessellation.obtain(pool).set(program, this.color);
        dc.offerSurfaceDrawable(drawable, 1.0 /*z-order after surface textures*/);
    }
}
