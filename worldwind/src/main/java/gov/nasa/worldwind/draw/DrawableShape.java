/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.util.Pool;

public class DrawableShape implements Drawable {

    public DrawShapeState drawState = new DrawShapeState();

    private Matrix4 mvpMatrix = new Matrix4();

    private Pool<DrawableShape> pool;

    public DrawableShape() {
    }

    public static DrawableShape obtain(Pool<DrawableShape> pool) {
        DrawableShape instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableShape().setPool(pool);
    }

    private DrawableShape setPool(Pool<DrawableShape> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.drawState.reset();

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {
        // TODO shape batching
        if (this.drawState.program == null || !this.drawState.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        if (this.drawState.vertexBuffer == null || !this.drawState.vertexBuffer.bindBuffer(dc)) {
            return; // vertex buffer unspecified or failed to bind
        }

        if (this.drawState.elementBuffer == null || !this.drawState.elementBuffer.bindBuffer(dc)) {
            return; // element buffer unspecified or failed to bind
        }

        // Use the draw context's pick mode.
        this.drawState.program.enablePickMode(dc.pickMode);

        // Use the draw context's modelview projection matrix, transformed to shape local coordinates.
        if (this.drawState.depthOffset != 0) {
            this.mvpMatrix.set(dc.projection).offsetProjectionDepth(this.drawState.depthOffset);
            this.mvpMatrix.multiplyByMatrix(dc.modelview);
        } else {
            this.mvpMatrix.set(dc.modelviewProjection);
        }
        this.mvpMatrix.multiplyByTranslation(this.drawState.vertexOrigin.x, this.drawState.vertexOrigin.y, this.drawState.vertexOrigin.z);
        this.drawState.program.loadModelviewProjection(this.mvpMatrix);

        // Disable triangle backface culling if requested.
        if (!this.drawState.enableCullFace) {
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        // Disable depth testing if requested.
        if (!this.drawState.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        // Make multi-texture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        // Use the shape's vertex point attribute and vertex texture coordinate attribute.
        GLES20.glEnableVertexAttribArray(1 /*vertexTexCoord*/);
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, this.drawState.vertexStride, 0 /*offset*/);

        // Draw the specified primitives.
        for (int idx = 0; idx < this.drawState.primCount; idx++) {
            DrawShapeState.DrawElements prim = this.drawState.prims[idx];
            this.drawState.program.loadColor(prim.color);

            if (prim.texture != null && prim.texture.bindTexture(dc)) {
                this.drawState.program.loadTexCoordMatrix(prim.texCoordMatrix);
                this.drawState.program.enableTexture(true);
            } else {
                this.drawState.program.enableTexture(false);
            }

            GLES20.glVertexAttribPointer(1 /*vertexTexCoord*/, prim.texCoordAttrib.size, GLES20.GL_FLOAT, false, this.drawState.vertexStride, prim.texCoordAttrib.offset);
            GLES20.glLineWidth(prim.lineWidth);
            GLES20.glDrawElements(prim.mode, prim.count, prim.type, prim.offset);
        }

        // Restore the default World Wind OpenGL state.
        if (!this.drawState.enableCullFace) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
        if (!this.drawState.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        GLES20.glLineWidth(1);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisableVertexAttribArray(1 /*vertexTexCoord*/);
    }
}
