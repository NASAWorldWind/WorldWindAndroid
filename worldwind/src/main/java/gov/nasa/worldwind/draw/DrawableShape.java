/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.util.Pool;

public class DrawableShape implements Drawable {

    public static final int MAX_PRIMITIVES = 4;

    public BasicShaderProgram program = null;

    public BufferObject vertexBuffer = null;

    public BufferObject elementBuffer = null;

    public Vec3 vertexOrigin = new Vec3();

    public boolean enableDepthTest = true;

    private DrawableElements[] primitives = new DrawableElements[MAX_PRIMITIVES];

    private int primitiveCount;

    private Pool<DrawableShape> pool;

    private Matrix4 scratchMatrix = new Matrix4();

    public DrawableShape() {
        for (int idx = 0; idx < MAX_PRIMITIVES; idx++) {
            this.primitives[idx] = new DrawableElements();
        }
    }

    public static DrawableShape obtain(Pool<DrawableShape> pool) {
        DrawableShape instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableShape().setPool(pool);
    }

    private DrawableShape setPool(Pool<DrawableShape> pool) {
        this.pool = pool;
        return this;
    }

    public DrawableElements addDrawElements(int mode, int count, int type, int offset) {
        return this.primitives[this.primitiveCount++].set(mode, count, type, offset);
    }

    @Override
    public void recycle() {
        this.program = null;
        this.vertexBuffer = null;
        this.elementBuffer = null;
        this.primitiveCount = 0;

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

        if (this.vertexBuffer == null || !this.vertexBuffer.bindBuffer(dc)) {
            return; // vertex buffer unspecified or failed to bind
        }

        if (this.elementBuffer == null || !this.elementBuffer.bindBuffer(dc)) {
            return; // element buffer unspecified or failed to bind
        }

        // Use the draw context's pick mode.
        this.program.enablePickMode(dc.pickMode);

        // Disable texturing.
        this.program.enableTexture(false);

        // Use the draw context's modelview projection matrix, transformed to shape local coordinates.
        this.scratchMatrix.set(dc.modelviewProjection);
        this.scratchMatrix.multiplyByTranslation(this.vertexOrigin.x, this.vertexOrigin.y, this.vertexOrigin.z);
        this.program.loadModelviewProjection(this.scratchMatrix);

        // Disable depth testing if requested.
        if (!this.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        // Disable polygon backface culling in order to draw both sides of the triangles.
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Use the shape's vertex point attribute.
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, 0);

        // Draw the specified primitives.
        for (int idx = 0; idx < this.primitiveCount; idx++) {
            DrawableElements prim = this.primitives[idx];
            this.program.loadColor(prim.color);
            GLES20.glLineWidth(prim.lineWidth);
            GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset);
        }

        // Restore the default World Wind OpenGL state.
        if (!this.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        GLES20.glLineWidth(1);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }
}
