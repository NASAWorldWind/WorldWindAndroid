/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Pool;

public class DrawableTessellation implements Drawable {

    public BasicShaderProgram program;

    public Color color = new Color();

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix4 offsetMvpMatrix = new Matrix4();

    private Pool<DrawableTessellation> pool;

    public DrawableTessellation() {
    }

    public static DrawableTessellation obtain(Pool<DrawableTessellation> pool) {
        DrawableTessellation instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableTessellation().setPool(pool);
    }

    private DrawableTessellation setPool(Pool<DrawableTessellation> pool) {
        this.pool = pool;
        return this;
    }

    public DrawableTessellation set(BasicShaderProgram program, Color color) {
        this.program = program;

        if (color != null) {
            this.color.set(color);
        } else {
            this.color.set(1, 1, 1, 1);
        }

        return this;
    }

    @Override
    public void recycle() {
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

        // Use the draw context's pick mode.
        this.program.enablePickMode(dc.pickMode);

        // Configure the program to draw the specified color.
        this.program.enableTexture(false);
        this.program.loadColor(this.color);

        // Suppress writes to the OpenGL depth buffer.
        GLES20.glDepthMask(false);

        // Compute the portion of the modelview projection matrix that remains constant for each tile.
        this.offsetMvpMatrix.set(dc.projection);
        this.offsetMvpMatrix.offsetProjectionDepth(-1.0e-3); // offset this layer's depth values toward the eye
        this.offsetMvpMatrix.multiplyByMatrix(dc.modelview);

        for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) {
            // Get the drawable terrain associated with the draw context.
            DrawableTerrain terrain = dc.getDrawableTerrain(idx);

            // Use the terrain's vertex point attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                continue; // vertex buffer failed to bind
            }

            // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
            Vec3 terrainOrigin = terrain.getVertexOrigin();
            this.mvpMatrix.set(this.offsetMvpMatrix);
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.program.loadModelviewProjection(this.mvpMatrix);

            // Draw the terrain as lines.
            terrain.drawLines(dc);
        }

        // Restore default World Wind OpenGL state.
        GLES20.glDepthMask(true);
    }
}
