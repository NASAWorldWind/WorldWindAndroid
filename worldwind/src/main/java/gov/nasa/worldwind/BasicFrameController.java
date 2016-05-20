/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.opengl.GLES20;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.globe.Tessellator;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

public class BasicFrameController implements FrameController {

    public BasicFrameController() {
    }

    @Override
    public void renderFrame(RenderContext rc) {
        this.tessellateTerrain(rc);
        this.renderLayers(rc);
        this.prepareDrawables(rc);
    }

    protected void tessellateTerrain(RenderContext rc) {
        Tessellator tess = rc.globe.getTessellator();
        tess.tessellate(rc);
    }

    protected void renderLayers(RenderContext rc) {
        LayerList layers = rc.layers;
        for (int idx = 0, len = layers.count(); idx < len; idx++) {
            rc.currentLayer = layers.getLayer(idx);
            try {
                rc.currentLayer.render(rc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawLayers",
                    "Exception while rendering layer \'" + rc.currentLayer.getDisplayName() + "\'", e);
                // Keep going. Draw the remaining layers.
            }
        }

        rc.currentLayer = null;
    }

    protected void prepareDrawables(RenderContext rc) {
        rc.sortDrawables();
    }

    @Override
    public void drawFrame(DrawContext dc) {
        this.clearFrame(dc);
        this.drawDrawables(dc);
    }

    protected void clearFrame(DrawContext dc) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void drawDrawables(DrawContext dc) {
        dc.rewindDrawables();

        Drawable next;
        while ((next = dc.pollDrawable()) != null) {
            try {
                next.draw(dc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawDrawables",
                    "Exception while drawing \'" + next + "\'", e);
                // Keep going. Draw the remaining drawables.
            }
        }
    }
}
