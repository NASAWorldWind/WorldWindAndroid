/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Texture;

/**
 * DrawablePlacemark is the delegate responsible for drawing a Placemark renderable with eye distance ordering.
 */
public class DrawablePlacemark implements Drawable {

    protected static FloatBuffer leaderBuffer;

    public float leaderWidth = 1;

    public Color iconColor = new Color();

    public Color leaderColor = null; // must be assigned if a leader line will be drawn

    public boolean drawLeader = false;

    public boolean enableIconDepthTest = true;

    public boolean enableLeaderDepthTest = true;

    public boolean enableLeaderPicking = false;

    public BasicShaderProgram program = null; // must be assigned in order to draw the placemark

    public Texture iconTexture = null;  // must be assigned if an image texture will be used

    public Matrix3 iconTexCoordMatrix = new Matrix3();

    public Matrix4 iconMvpMatrix = new Matrix4();

    public Matrix4 leaderMvpMatrix = null; // must be assigned if a leader line will be drawn

    public float[] leaderVertexPoint = null; // must be assigned if a leader line will be drawn

    protected boolean enableDepthTest = true;

    protected static FloatBuffer getLeaderBuffer(float[] points) {
        int size = points.length;
        if (leaderBuffer == null || leaderBuffer.capacity() < size) {
            leaderBuffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        leaderBuffer.clear();
        leaderBuffer.put(points).flip();

        return leaderBuffer;
    }

    /**
     * Performs the actual rendering of the Placemark.
     *
     * @param dc The current draw context.
     */
    @Override
    public void draw(DrawContext dc) {
        if (this.program == null) {
            return; // no program assigned
        }

        if (!this.program.useProgram(dc)) {
            return; // program failed to build
        }

        // this.program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity); // TODO: opacity

        // Initialize vars used to track GL states that may need to be restored.
        this.enableDepthTest = true; // default

        // Draw the placemark's leader line first so that the icon and label display on top.
        if (this.drawLeader) {
            this.drawLeader(dc);
        }

        // Draw the placemark icon, either textured or a simple colored square.
        this.drawIcon(dc);

        // Restore the default World Wind OpenGL state.
        if (!this.enableDepthTest) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
    }

    @Override
    public void recycle() {
        this.program = null;
        this.iconTexture = null;
    }

    protected void drawIcon(DrawContext dc) {
        // Attempt to bind the icon's texture to multi-texture unit 0.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);
        if (this.iconTexture != null && this.iconTexture.bindTexture(dc)) {
            // Enable texturing if the icon texture successfully bound.
            this.program.enableTexture(true);
            this.program.loadTexCoordMatrix(this.iconTexCoordMatrix);
        } else {
            // Disable texturing if the icon has no texture, or if the texture failed to bind.
            this.program.enableTexture(false);
        }

        // Use the icon's color.
        this.program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.iconColor); // TODO: pickColor

        // Use the icon's modelview-projection matrix.
        this.program.loadModelviewProjection(this.iconMvpMatrix);

        // Disable icon depth testing if requested. The icon and leader line have their own depth-test controls.
        this.enableDepthTest(this.enableIconDepthTest);

        // Disable writing to the depth buffer.
        GLES20.glDepthMask(false);

        // Use a 2D unit quad as the vertex point and vertex tex coord attributes.
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, dc.unitQuadBuffer());
        GLES20.glEnableVertexAttribArray(1); // enable vertex attrib 1; vertex attrib 0 is enabled by default
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, 0);

        // Draw the 2D unit quad as triangles.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // Restore the default World Wind OpenGL state.
        dc.bindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDepthMask(true);
        GLES20.glDisableVertexAttribArray(1);
    }

    protected void drawLeader(DrawContext dc) {
        // Disable texturing.
        this.program.enableTexture(false);

        // Use the leader's color.
        this.program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.leaderColor); // TODO: pickColor

        // Use the leader's modelview-projection matrix.
        this.program.loadModelviewProjection(this.leaderMvpMatrix);

        // Disable leader depth testing if requested. The icon and leader line have their own depth-test controls.
        this.enableDepthTest(this.enableLeaderDepthTest);

        // Apply the leader's line width in screen pixels.
        GLES20.glLineWidth(this.leaderWidth);

        // Use the leader line as the vertex point attribute.
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getLeaderBuffer(this.leaderVertexPoint));

        // Draw the leader line.
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, this.leaderVertexPoint.length / 3);
    }

    protected void enableDepthTest(boolean enable) {
        if (this.enableDepthTest != enable) {
            this.enableDepthTest = enable;
            if (enable) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            } else {
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            }
        }
    }
}
