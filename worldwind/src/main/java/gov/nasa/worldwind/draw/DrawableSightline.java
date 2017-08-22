/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.Framebuffer;
import gov.nasa.worldwind.render.SightlineProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;

public class DrawableSightline implements Drawable {

    public Matrix4 centerTransform = new Matrix4();

    public float range;

    public Color visibleColor = new Color(0, 0, 0, 0);

    public Color occludedColor = new Color(0, 0, 0, 0);

    public SightlineProgram program = null;

    private Matrix4 sightlineView = new Matrix4();

    private Matrix4 matrix = new Matrix4();

    private Matrix4 cubeMapProjection = new Matrix4();

    private Matrix4[] cubeMapFace = {
        new Matrix4().setToRotation(0, 0, 1, -90).multiplyByRotation(1, 0, 0, 90), // positive X
        new Matrix4().setToRotation(0, 0, 1, 90).multiplyByRotation(1, 0, 0, 90), // negative X
        new Matrix4().setToRotation(1, 0, 0, 90), // positive Y
        new Matrix4().setToRotation(0, 0, 1, 180).multiplyByRotation(1, 0, 0, 90), // negative Y
        /*new Matrix4().setToRotation(1, 0, 0, 180),*/ // positive Z, intentionally omitted as terrain is never visible when looking up
        new Matrix4() // negative Z
    };

    private Pool<DrawableSightline> pool;

    public DrawableSightline() {
    }

    public static DrawableSightline obtain(Pool<DrawableSightline> pool) {
        DrawableSightline instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableSightline().setPool(pool);
    }

    private DrawableSightline setPool(Pool<DrawableSightline> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.visibleColor.set(0, 0, 0, 0);
        this.occludedColor.set(0, 0, 0, 0);
        this.program = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null || !this.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        // Use the drawable's color.
        this.program.loadRange(this.range);
        this.program.loadColor(this.visibleColor, this.occludedColor);

        // Configure the cube map projection matrix to capture one face of the cube map as far as the sightline's range.
        this.cubeMapProjection.setToPerspectiveProjection(1, 1, 90, 1, this.range);

        // TODO accumulate only the visible terrain, which can be used in both passes
        // TODO give terrain a bounding box, test with a frustum set using depthviewProjection

        for (int idx = 0, len = this.cubeMapFace.length; idx < len; idx++) {
            this.sightlineView.set(this.centerTransform);
            this.sightlineView.multiplyByMatrix(this.cubeMapFace[idx]);
            this.sightlineView.invertOrthonormal();

            if (this.drawSceneDepth(dc)) {
                this.drawSceneOcclusion(dc);
            }
        }
    }

    protected boolean drawSceneDepth(DrawContext dc) {
        try {
            Framebuffer framebuffer = dc.scratchFramebuffer();
            if (!framebuffer.bindFramebuffer(dc)) {
                return false; // framebuffer failed to bind
            }

            // Clear the framebuffer.
            Texture depthTexture = framebuffer.getAttachedTexture(GLES20.GL_DEPTH_ATTACHMENT);
            GLES20.glViewport(0, 0, depthTexture.getWidth(), depthTexture.getHeight());
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

            // Draw only depth values offset slightly away from the viewer.
            GLES20.glColorMask(false, false, false, false);
            GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
            GLES20.glPolygonOffset(4, 4);

            for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) {
                // Get the drawable terrain associated with the draw context.
                DrawableTerrain terrain = dc.getDrawableTerrain(idx);
                Vec3 terrainOrigin = terrain.getVertexOrigin();

                // Use the terrain's vertex point attribute.
                if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                    continue; // vertex buffer failed to bind
                }

                // Draw the terrain onto one face of the cube map, from the sightline's point of view.
                this.matrix.setToMultiply(this.cubeMapProjection, this.sightlineView);
                this.matrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
                this.program.loadModelviewProjection(this.matrix);

                // Draw the terrain as triangles.
                terrain.drawTriangles(dc);
            }
        } finally {
            // Restore the default World Wind OpenGL state.
            dc.bindFramebuffer(0);
            GLES20.glViewport(dc.viewport.x, dc.viewport.y, dc.viewport.width, dc.viewport.height);
            GLES20.glColorMask(true, true, true, true);
            GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);
            GLES20.glPolygonOffset(0, 0);
        }

        return true;
    }

    protected void drawSceneOcclusion(DrawContext dc) {
        // Make multi-texture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        Texture depthTexture = dc.scratchFramebuffer().getAttachedTexture(GLES20.GL_DEPTH_ATTACHMENT);
        if (!depthTexture.bindTexture(dc)) {
            return; // framebuffer texture failed to bind
        }

        for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) {
            // Get the drawable terrain associated with the draw context.
            DrawableTerrain terrain = dc.getDrawableTerrain(idx);
            Vec3 terrainOrigin = terrain.getVertexOrigin();

            // Use the terrain's vertex point attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                continue; // vertex buffer failed to bind
            }

            // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
            this.matrix.set(dc.modelviewProjection);
            this.matrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.program.loadModelviewProjection(this.matrix);

            // Map the terrain into one face of the cube map, from the sightline's point of view.
            this.matrix.set(this.sightlineView);
            this.matrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.program.loadSightlineProjection(this.cubeMapProjection, this.matrix);

            // Draw the terrain as triangles.
            terrain.drawTriangles(dc);
        }
    }
}
