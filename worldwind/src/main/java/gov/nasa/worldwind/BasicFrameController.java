/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.opengl.GLES20;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.globe.Tessellator;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logger;

public class BasicFrameController implements FrameController {

    protected Camera camera = new Camera();

    protected Matrix4 matrix = new Matrix4();

    public BasicFrameController() {
    }

    @Override
    public void renderFrame(DrawContext dc) {
        this.createViewingState(dc);
        this.createTerrain(dc);
        this.renderLayers(dc);
    }

    protected void createViewingState(DrawContext dc) {

        // Compute the clip plane distances. The near distance is set to a large value that does not clip the globe's
        // surface. The far distance is set to the smallest value that does not clip the atmosphere.
        // TODO adjust the clip plane distances based on the navigator's orientation - shorter distances when the
        // TODO horizon is not in view
        // TODO parameterize the object altitude for horizon distance
        double near = dc.eyePosition.altitude * 0.75;
        double far = dc.globe.horizonDistance(dc.eyePosition.altitude, 160000);

        // Configure a camera object with the draw context's viewing parameters. This is used to compute the draw
        // context's Cartesian modelview matrix.
        this.camera.set(dc.eyePosition.latitude, dc.eyePosition.longitude, dc.eyePosition.altitude, WorldWind.ABSOLUTE,
            dc.heading, dc.tilt, dc.roll);

        // Compute the draw context's Cartesian modelview matrix, eye coordinate projection matrix, and the combined
        // modelview-projection matrix. Extract the Cartesian eye point from the modelview matrix.
        dc.globe.cameraToCartesianTransform(this.camera, dc.modelview).invertOrthonormal();
        dc.modelview.extractEyePoint(dc.eyePoint);
        dc.projection.setToPerspectiveProjection(dc.viewport.width(), dc.viewport.height(), dc.fieldOfView, near, far);
        dc.modelviewProjection.setToMultiply(dc.projection, dc.modelview);
        dc.screenProjection.setToScreenProjection(dc.viewport.width(), dc.viewport.height());

        // Compute the projection's Cartesian frustum, which must be transformed from eye coordinates to world Cartesian
        // coordinates.
        dc.frustum.setToProjectionMatrix(dc.projection);
        dc.frustum.transformByMatrix(this.matrix.transposeMatrix(dc.modelview));
        dc.frustum.normalize();
    }

    protected void createTerrain(DrawContext dc) {
        Tessellator tess = dc.globe.getTessellator();
        dc.terrain = tess.tessellate(dc);
    }

    protected void renderLayers(DrawContext dc) {
        LayerList layers = dc.layers;
        for (int idx = 0, len = layers.count(); idx < len; idx++) {
            dc.currentLayer = layers.getLayer(idx);
            try {
                dc.currentLayer.render(dc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawLayers",
                    "Exception while rendering layer \'" + dc.currentLayer.getDisplayName() + "\'", e);
                // Keep going. Draw the remaining layers.
            }
        }

        dc.currentLayer = null;
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
        dc.sortDrawables();

        Drawable next;
        while ((next = dc.pollDrawable()) != null) {
            try {
                next.draw(dc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawDrawables",
                    "Exception while drawing \'" + next + "\'");
                // Keep going. Draw the remaining drawables.
            }
        }
    }
}
