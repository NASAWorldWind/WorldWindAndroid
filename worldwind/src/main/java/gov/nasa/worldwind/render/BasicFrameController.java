/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;

import gov.nasa.worldwind.globe.Tessellator;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.util.Logger;

public class BasicFrameController implements FrameController {

    protected FrameStatistics frameStatistics = new FrameStatistics();

    public BasicFrameController() {
    }

    @Override
    public FrameStatistics getFrameStatistics() {
        return frameStatistics;
    }

    @Override
    public void drawFrame(DrawContext dc) {
        try {
            this.frameStatistics.beginFrame();
            this.doDrawFrame(dc);
        } finally {
            this.frameStatistics.endFrame();
        }
    }

    protected void doDrawFrame(DrawContext dc) {
        this.clearFrame(dc);
        this.createTerrain(dc);
        this.drawLayers(dc);
    }

    protected void clearFrame(DrawContext dc) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void createTerrain(DrawContext dc) {
        Tessellator tess = dc.getGlobe().getTessellator();
        dc.setTerrain(tess.tessellate(dc));
    }

    protected void drawLayers(DrawContext dc) {

        for (Layer layer : dc.getLayers()) {
            if (layer != null) {
                dc.setCurrentLayer(layer);
                try {
                    layer.render(dc);
                } catch (Exception e) {
                    Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawLayers",
                        "Exception while rendering layer \'" + layer.getDisplayName() + "\'", e);
                    // Keep going. Draw the remaining layers.
                }
            }
        }

        dc.setCurrentLayer(null);
    }
}
