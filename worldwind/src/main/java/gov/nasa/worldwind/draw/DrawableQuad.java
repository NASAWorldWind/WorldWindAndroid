/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;

public class DrawableQuad implements Drawable {

    public BasicShaderProgram program = null;

    public Matrix4 mvpMatrix = new Matrix4();

    public Color color = new Color();

    public Texture texture = null;

    public boolean enableDepthTest = true;

    private Pool<DrawableQuad> pool;

    public DrawableQuad() {
    }

    public static DrawableQuad obtain(Pool<DrawableQuad> pool) {
        DrawableQuad instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableQuad().setPool(pool);
    }

    private DrawableQuad setPool(Pool<DrawableQuad> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.program = null;
        this.texture = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null) {
            return; // program unspecified
        }

        if (!this.program.useProgram(dc)) {
            return; // program failed to build
        }

        // Use the drawable's color.
        this.program.loadColor(this.color);

        // Attempt to bind the icon's texture to multi-texture unit 0, configuring the shader program appropriately
        // if there is no texture or the texture failed to bind.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);
        if (this.texture != null && this.texture.bindTexture(dc)) {
            this.program.enableTexture(true);
            this.program.loadTexCoordMatrix(this.texture.getTexCoordTransform());
        } else {
            this.program.enableTexture(false);
        }

        // Disable writing to the depth buffer, and disable depth testing if requested.
        GLES20.glDepthMask(false);
        if (!this.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        // Use a 2D unit quad as the vertex point and vertex tex coord attributes.
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, dc.unitQuadBuffer());
        GLES20.glEnableVertexAttribArray(1); // enable vertex attrib 1; vertex attrib 0 is enabled by default
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glVertexAttribPointer(1 /*vertexTexCoord*/, 2, GLES20.GL_FLOAT, false, 0, 0);

        // Use the drawable's modelview-projection matrix.
        this.program.loadModelviewProjection(this.mvpMatrix);

        // Draw the 2D unit quad as triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        Drawable next;
        while ((next = dc.peekDrawable()) != null && this.canBatchWith(next)) { // check if the drawable at the front of the queue can be batched
            // Use the next drawable's modelview-projection matrix.
            DrawableQuad drawable = (DrawableQuad) dc.pollDrawable(); // take it off the queue
            this.program.loadModelviewProjection(drawable.mvpMatrix);

            // Draw the 2D unit quad as triangles.
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glDepthMask(true);
        if (!this.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDisableVertexAttribArray(1);
    }

    protected boolean canBatchWith(Drawable that) {
        return this.getClass() == that.getClass()
            && this.program == ((DrawableQuad) that).program
            && this.color.equals(((DrawableQuad) that).color)
            && this.texture == ((DrawableQuad) that).texture
            && this.enableDepthTest == ((DrawableQuad) that).enableDepthTest;
    }
}
