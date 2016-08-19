/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Pool;

public class DrawableLines implements Drawable {

    public BasicShaderProgram program = null;

    public float[] vertexPoints = new float[6]; // initially sized to store two xyz points

    public Matrix4 mvpMatrix = new Matrix4();

    public Color color = new Color();

    public float lineWidth = 1;

    public boolean enableDepthTest = true;

    private Pool<DrawableLines> pool;

    public DrawableLines() {
    }

    public static DrawableLines obtain(Pool<DrawableLines> pool) {
        DrawableLines instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableLines().setPool(pool);
    }

    private DrawableLines setPool(Pool<DrawableLines> pool) {
        this.pool = pool;
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

    /**
     * Performs the actual rendering of the Placemark.
     *
     * @param dc The current draw context.
     */
    @Override
    public void draw(DrawContext dc) {
        if (this.program == null || !this.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        // Disable texturing.
        this.program.enableTexture(false);

        // Use the leader's color.
        this.program.loadColor(this.color);

        // Use the leader's modelview-projection matrix.
        this.program.loadModelviewProjection(this.mvpMatrix);

        // Disable depth testing if requested.
        if (!this.enableDepthTest) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        }

        // Apply the leader's line width in screen pixels.
        GLES20.glLineWidth(this.lineWidth);

        // Use the leader line as the vertex point attribute.
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        FloatBuffer buffer = dc.scratchBuffer(this.vertexPoints.length * 4).asFloatBuffer();
        buffer.clear();
        buffer.put(this.vertexPoints).flip();
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, buffer);

        // Draw the leader line.
        GLES20.glDrawArrays(GLES20.GL_LINES, 0 /*first*/, (buffer.remaining() / 3) /*count*/);

        // Restore the default World Wind OpenGL state.
        if (!this.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        GLES20.glLineWidth(1);
    }
}
