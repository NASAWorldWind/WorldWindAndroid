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

    public String label;

    public float leaderWidth = 1;

    public Color imageColor = new Color(1, 1, 1, 1); // white

    public Color leaderColor; // must be assigned if a leader line must be drawn

    public Color labelColor; // must be assigned if a label must be drawn

    public boolean drawLeader;

    public boolean drawLabel;

    public boolean enableImageDepthTest = true;

    public boolean enableLeaderDepthTest = true;

    public boolean enableLabelDepthTest = true;

    public boolean enableLeaderPicking = false;

    public Vec3 screenPlacePoint = new Vec3(0, 0, 0);

    public Vec3 screenGroundPoint;    // must be created if a leader line must be drawn

    public double actualRotation = 0;

    public double actualTilt = 0;

    public BasicShaderProgram program;

    public Texture activeTexture;  // must be assigned if an image texture will be used

    public Texture labelTexture;   // must be assigned a label texture will be used

    public Matrix4 imageTransform = new Matrix4();

    public Matrix4 labelTransform;    // will be lazily created if a label must be drawn

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Matrix4 mvpMatrix = new Matrix4();

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

        // program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity); // TODO: opacity

        // Initialize vars used to track GL states that may need to be restored
        boolean depthTesting = true;    // default
        boolean textureBound = false;

        ///////////////////////////////////
        // Draw the optional leader-line
        ///////////////////////////////////

        // Draw the leader line first so that the image and label have visual priority.
        if (this.drawLeader) {
            // TODO: Must evaluate the effectiveness of using screen coordinates with depth offsets for the leaderline
            // TODO: when terrain is used.  I suspect there will be an issue with the ground point.
            // TODO: Perhaps the ground screen point should not have a depth offset.
            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getLeaderBuffer(this.screenGroundPoint, this.screenPlacePoint));

            this.mvpMatrix.set(dc.screenProjection);
            program.loadModelviewProjection(this.mvpMatrix);
            program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.leaderColor); // TODO: pickColor

            // Toggle depth testing if necessary
            if (this.enableLeaderDepthTest != depthTesting) {
                depthTesting = !depthTesting;
                if (depthTesting) {
                    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                } else {
                    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                }
            }
            GLES20.glLineWidth(this.leaderWidth);
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        }

        ///////////////////////////////////
        // Draw the image
        ///////////////////////////////////

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
        this.mvpMatrix.multiplyByRotation(0, 0, 1, this.actualRotation);
        this.mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0);
        // ... and perform the tilt so that the image tilts back from its base into the view volume.
        this.mvpMatrix.multiplyByRotation(-1, 0, 0, this.actualTilt);
        // Now load the MVP matrix
        this.program.loadModelviewProjection(this.mvpMatrix);

        // Bind the texture
        if (this.activeTexture != null) {
            // Make multi-texture unit 0 active.
            dc.activeTextureUnit(GLES20.GL_TEXTURE0);
            textureBound = this.activeTexture.bindTexture(dc);
            if (textureBound) {
                this.program.enableTexture(true);
                // Perform a vertical flip of the bound texture to match
                // the reversed Y-axis of the screen coordinate system
                this.texCoordMatrix.setToIdentity();
                this.activeTexture.applyTexCoordTransform(this.texCoordMatrix);
                this.program.loadTexCoordMatrix(this.texCoordMatrix);
            }
        }

        // Load the color used for the image
        this.program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.imageColor); // TODO: pickColor

        // Turn off depth testing for the placemark image if requested.
        // Note the placemark label and leader line have their own depth-test controls.
        if (this.enableImageDepthTest != depthTesting) {
            depthTesting = !depthTesting;
            if (depthTesting) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            } else {
                GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            }
        }

        // Draw the placemark's image quad.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        ///////////////////////////////////
        // Draw the label
        ///////////////////////////////////

        if (this.drawLabel) { // TODO: drawLabel
//            this.program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity * this.currentVisibility);
//
//            Placemark.matrix.copy(dc.screenProjection);
//            Placemark.matrix.multiplyMatrix(this.labelTransform);
//            this.program.loadModelviewProjection(gl, Placemark.matrix);
//
//            if (!dc.pickingMode && this.labelTexture) {
//                this.texCoordMatrix.setToIdentity();
//                this.texCoordMatrix.multiplyByTextureTransform(this.labelTexture);
//
//                this.program.loadTextureMatrix(gl, this.texCoordMatrix);
//                this.program.loadColor(gl, this.attributes.labelAttributes.color);
//
//                textureBound = this.labelTexture.bind(dc);
//                this.program.loadTextureEnabled(gl, textureBound);
//            } else {
//                this.program.loadTextureEnabled(gl, false);
//                this.program.loadColor(gl, this.pickColor);
//            }
//
//            if (this.attributes.labelAttributes.depthTest && depthTest) {
//                    depthTest = true;
//                    gl.enable(gl.DEPTH_TEST);
//            } else {
//                depthTest = false;
//                gl.disable(gl.DEPTH_TEST);
//            }
//
//            gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
        }

        // Restore the default World Wind OpenGL state.
        if (!depthTesting) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }
        if (textureBound) {
            this.program.enableTexture(false);
        }
        GLES20.glDisableVertexAttribArray(1);
    }
}
