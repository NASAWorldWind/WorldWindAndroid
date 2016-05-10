/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.opengl.GLES20;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.globe.Tessellator;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

public class BasicFrameController implements FrameController {

    protected Camera camera = new Camera();

    protected Matrix4 matrix = new Matrix4();

    public BasicFrameController() {
    }

    @Override
    public void renderFrame(RenderContext rc) {
        this.createViewingState(rc);
        this.createTerrain(rc);
        this.renderLayers(rc);
    }

    protected void createViewingState(RenderContext rc) {

        // Compute the clip plane distances. The near distance is set to a large value that does not clip the globe's
        // surface. The far distance is set to the smallest value that does not clip the atmosphere.
        // TODO adjust the clip plane distances based on the navigator's orientation - shorter distances when the
        // TODO horizon is not in view
        // TODO parameterize the object altitude for horizon distance
        double near = rc.eyePosition.altitude * 0.75;
        double far = rc.globe.horizonDistance(rc.eyePosition.altitude, 160000);

        // Configure a camera object with the draw context's viewing parameters. This is used to compute the draw
        // context's Cartesian modelview matrix.
        this.camera.set(rc.eyePosition.latitude, rc.eyePosition.longitude, rc.eyePosition.altitude, WorldWind.ABSOLUTE,
            rc.heading, rc.tilt, rc.roll);

        // Compute the draw context's Cartesian modelview matrix, eye coordinate projection matrix, and the combined
        // modelview-projection matrix. Extract the Cartesian eye point from the modelview matrix.
        rc.globe.cameraToCartesianTransform(this.camera, rc.modelview).invertOrthonormal();
        rc.modelview.extractEyePoint(rc.eyePoint);
        rc.projection.setToPerspectiveProjection(rc.viewport.width(), rc.viewport.height(), rc.fieldOfView, near, far);
        rc.modelviewProjection.setToMultiply(rc.projection, rc.modelview);

        // Compute the projection's Cartesian frustum, which must be transformed from eye coordinates to world Cartesian
        // coordinates.
        rc.frustum.setToProjectionMatrix(rc.projection);
        rc.frustum.transformByMatrix(this.matrix.transposeMatrix(rc.modelview));
        rc.frustum.normalize();
    }

    protected void createTerrain(RenderContext rc) {
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
