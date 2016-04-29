/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Texture;

/**
 * DrawablePlacemark is the delegate responsible for drawing a Placemark renderable with eye distance ordering.
 */
public class DrawablePlacemark implements Drawable {

    protected static FloatBuffer unitQuadBuffer;

    protected static FloatBuffer leaderBuffer;

    protected static float[] leaderPoints; // will be lazily created if a leader will be drawn

    public float leaderWidth = 1;

    public Color imageColor = new Color();

    public Color leaderColor; // must be assigned if a leader line must be drawn

    public boolean drawLeader;

    public boolean enableImageDepthTest = true;

    public boolean enableLeaderDepthTest = true;

    public boolean enableLeaderPicking = false;

    public Vec3 screenPlacePoint = new Vec3();

    public Vec3 screenGroundPoint; // must be assigned if a leader line must be drawn

    public double rotation = 0;

    public double tilt = 0;

    public BasicShaderProgram program;

    public Texture iconTexture;  // must be assigned if an image texture will be used

    public Matrix4 imageTransform = new Matrix4();

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Matrix4 mvpMatrix = new Matrix4();

    protected boolean enableDepthTest = true;

    /**
     * Returns a buffer containing a unit quadrilateral expressed as four 2D vertices at (0, 1), (0, 0), (1, 1) and (1,
     * 0). The four vertices are in the order required by a triangle strip. The buffer is created on first use and
     * cached. Subsequent calls to this method return the cached buffer.
     */
    protected static FloatBuffer getUnitQuadBuffer() {
        if (unitQuadBuffer == null) {
            float[] points = new float[]{
                0, 1,   // upper left corner
                0, 0,   // lower left corner
                1, 1,   // upper right corner
                1, 0};  // lower right corner
            int size = points.length * 4;
            unitQuadBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            unitQuadBuffer.put(points).rewind();
        }

        return unitQuadBuffer;
    }

    protected static FloatBuffer getLeaderBuffer(Vec3 groundPoint, Vec3 placePoint) {
        if (leaderBuffer == null) {
            leaderPoints = new float[6];
            int size = leaderPoints.length * 4;
            leaderBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        // TODO: consider whether these assignments should be inlined.
        leaderPoints[0] = (float) groundPoint.x;
        leaderPoints[1] = (float) groundPoint.y;
        leaderPoints[2] = (float) groundPoint.z;
        leaderPoints[3] = (float) placePoint.x;
        leaderPoints[4] = (float) placePoint.y;
        leaderPoints[5] = (float) placePoint.z;
        leaderBuffer.put(leaderPoints).rewind();

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

        // Initialize vars used to track GL states that may need to be restored
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

    protected void drawIcon(DrawContext dc) {
        // Set up a 2D unit quad as the source of vertex points and texture coordinates.
        Buffer unitQuadBuffer = getUnitQuadBuffer();
        GLES20.glEnableVertexAttribArray(1); // enable vertex attrib 1; vertex attrib 0 is enabled by default
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, unitQuadBuffer);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, unitQuadBuffer);

        // Compute and specify the MVP matrix...
        this.mvpMatrix.set(dc.screenProjection);
        this.mvpMatrix.multiplyByMatrix(this.imageTransform);
        // ... perform image rotation
        this.mvpMatrix.multiplyByTranslation(0.5, 0.5, 0);
        this.mvpMatrix.multiplyByRotation(0, 0, 1, this.rotation);
        this.mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0);
        // ... and perform the tilt so that the image tilts back from its base into the view volume.
        this.mvpMatrix.multiplyByRotation(-1, 0, 0, this.tilt);
        // Now load the MVP matrix
        this.program.loadModelviewProjection(this.mvpMatrix);

        // Make multi-texture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        // Attempt to bind the texture.
        if (this.iconTexture != null && this.iconTexture.bindTexture(dc)) {
            this.program.enableTexture(true);
            // Perform a vertical flip of the bound texture to match
            // the reversed Y-axis of the screen coordinate system
            this.texCoordMatrix.setToIdentity();
            this.iconTexture.applyTexCoordTransform(this.texCoordMatrix);
            this.program.loadTexCoordMatrix(this.texCoordMatrix);
        } else {
            this.program.enableTexture(false);
        }

        // Load the color used for the image
        this.program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.imageColor); // TODO: pickColor

        // Disable icon depth testing if requested. Note the icon and leader line have their own depth-test controls.
        this.enableDepthTest(this.enableImageDepthTest);

        // Draw the placemark's image quad.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(1);
    }

    protected void drawLeader(DrawContext dc) {
        // TODO: Must evaluate the effectiveness of using screen coordinates with depth offsets for the leaderline
        // TODO: when terrain is used.  I suspect there will be an issue with the ground point.
        // TODO: Perhaps the ground screen point should not have a depth offset.
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getLeaderBuffer(this.screenGroundPoint, this.screenPlacePoint));

        this.program.enableTexture(false);
        this.program.loadModelviewProjection(dc.screenProjection);
        this.program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.leaderColor); // TODO: pickColor

        // Disable leader depth testing if requested.
        this.enableDepthTest(this.enableLeaderDepthTest);

        GLES20.glLineWidth(this.leaderWidth);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
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
