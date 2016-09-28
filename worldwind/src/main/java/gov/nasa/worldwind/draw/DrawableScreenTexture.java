/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;

public class DrawableScreenTexture implements Drawable {

    public BasicShaderProgram program = null;

    public Matrix4 unitSquareTransform = new Matrix4();

    public Color color = new Color();

    public Texture texture = null;

    public boolean enableDepthTest = true;

    private Pool<DrawableScreenTexture> pool;

    private Matrix4 mvpMatrix = new Matrix4();

    public DrawableScreenTexture() {
    }

    public static DrawableScreenTexture obtain(Pool<DrawableScreenTexture> pool) {
        DrawableScreenTexture instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableScreenTexture().setPool(pool);
    }

    private DrawableScreenTexture setPool(Pool<DrawableScreenTexture> pool) {
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
        if (this.program == null || !this.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        if (!dc.unitSquareBuffer().bindBuffer(dc)) {
            return; // vertex buffer failed to bind
        }

        // Use the draw context's pick mode and use the drawable's color.
        this.program.enablePickMode(dc.pickMode);

        // Make multi-texture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        // Disable writing to the depth buffer.
        GLES20.glDepthMask(false);

        // Use a unit square as the vertex point and vertex tex coord attributes.
        GLES20.glEnableVertexAttribArray(1 /*vertexTexCoord*/); // only vertexPoint is enabled by default
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glVertexAttribPointer(1 /*vertexTexCoord*/, 2, GLES20.GL_FLOAT, false, 0, 0);

        // Draw this DrawableScreenTextures.
        this.doDraw(dc, this);

        // Draw all DrawableScreenTextures adjacent in the queue that share the same GLSL program.
        Drawable next;
        while ((next = dc.peekDrawable()) != null && this.canBatchWith(next)) { // check if the drawable at the front of the queue can be batched
            DrawableScreenTexture drawable = (DrawableScreenTexture) dc.pollDrawable(); // take it off the queue
            this.doDraw(dc, drawable);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glDepthMask(true);
        GLES20.glDisableVertexAttribArray(1 /*vertexTexCoord*/); // only vertexPoint is enabled by default
    }

    protected void doDraw(DrawContext dc, DrawableScreenTexture drawable) {
        // Use the drawable's color.
        drawable.program.loadColor(drawable.color);

        // Attempt to bind the drawable's texture, configuring the shader program appropriately if there is no texture
        // or if the texture failed to bind.
        if (drawable.texture != null && drawable.texture.bindTexture(dc)) {
            drawable.program.enableTexture(true);
            drawable.program.loadTexCoordMatrix(drawable.texture.getTexCoordTransform());
        } else {
            drawable.program.enableTexture(false);
        }

        // Use a modelview-projection matrix that transforms the unit square to screen coordinates.
        drawable.mvpMatrix.setToMultiply(dc.screenProjection, drawable.unitSquareTransform);
        drawable.program.loadModelviewProjection(drawable.mvpMatrix);

        // Disable depth testing if requested.
        if (!drawable.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        // Draw the unit square as triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Restore the default World Wind OpenGL state.
        if (!drawable.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
    }

    protected boolean canBatchWith(Drawable that) {
        return this.getClass() == that.getClass() && this.program == ((DrawableScreenTexture) that).program;
    }
}
