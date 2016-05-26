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
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

public class BasicFrameController implements FrameController {

    private Color pickColor;

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

        if (dc.pickMode) {
            this.resolvePick(dc);
        }
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

    protected void resolvePick(DrawContext dc) {
        if (dc.pickedObjects.count() == 0) {
            return; // no eligible objects; avoid expensive calls to glReadPixels
        }

        // Read the fragment color at the pick point.
        this.pickColor = dc.readPixelColor((int) Math.round(dc.pickPoint.x), (int) Math.round(dc.pickPoint.y), this.pickColor);

        // Convert the fragment color to a picked object ID. This returns zero if the color cannot indicate a picked
        // object ID, in which case no objects have been drawn at the pick point.
        int topObjectId = PickedObject.uniqueColorToIdentifier(this.pickColor);
        if (topObjectId != 0) {
            PickedObject topObject = dc.pickedObjects.pickedObjectWithId(topObjectId);
            if (topObject != null) {
                topObject.markOnTop();
                dc.pickedObjects.clearPickedObjects();
                dc.pickedObjects.offerPickedObject(topObject);
            } else {
                dc.pickedObjects.clearPickedObjects(); // no eligible objects drawn at the pick point
            }
        } else {
            dc.pickedObjects.clearPickedObjects(); // no objects drawn at the pick point
        }
    }
}
