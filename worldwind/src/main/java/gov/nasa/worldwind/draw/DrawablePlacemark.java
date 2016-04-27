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
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Texture;

/**
 * DrawablePlacemark is the delegate responsible for drawing a Placemark renderable with eye distance ordering.
 */
public class DrawablePlacemark implements Drawable {


    private static FloatBuffer unitQuadBuffer2 = null;

    private static FloatBuffer unitQuadBuffer3 = null;

    private static FloatBuffer leaderBuffer = null;

    private static float[] leaderPoints = null;  // will be lazily created if a leader will be drawn

    public String label = null;

    public float leaderWidth = 1;

    public Color imageColor = new Color(1, 1, 1, 1); // white

    public Color leaderColor = null;    // must be allocated if a leader line must be drawn

    public Color labelColor = null;     // must be allocated if a label must be drawn

    public boolean drawLeader = false;

    public boolean drawLabel = false;

    public boolean enableImageDepthTest = true;

    public boolean enableLeaderDepthTest = true;

    public boolean enableLabelDepthTest = true;

    public boolean enableLeaderPicking = false;

    public Vec3 screenPlacePoint = new Vec3(0, 0, 0);

    public Vec3 screenGroundPoint = null;    // must be created if a leader line must be drawn

    public double actualRotation = 0;

    public double actualTilt = 0;

    public Texture activeTexture = null;  // must be allocated if image texture will be used

    public Texture labelTexture = null;   // must be allocated a label texture will be used

    public Matrix4 imageTransform = new Matrix4();

    public Matrix4 labelTransform = null;    // will be lazily created if a label must be drawn

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Matrix4 mvpMatrix = new Matrix4();


    /**
     * Returns a buffer containing a unit quadrilateral expressed as four 2D vertices at (0, 1), (0, 0), (1, 1) and (1,
     * 0). The four vertices are in the order required by a triangle strip. The buffer is created on first use and
     * cached. Subsequent calls to this method return the cached buffer.
     */
    static public FloatBuffer getUnitQuadBuffer2D() {
        if (unitQuadBuffer2 == null) {
            float[] points = new float[]{
                0, 1,   // upper left corner
                0, 0,   // lower left corner
                1, 1,   // upper right corner
                1, 0};  // lower right corner
            int size = points.length * 4;
            unitQuadBuffer2 = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            unitQuadBuffer2.put(points).rewind();
        }
        return unitQuadBuffer2;
    }

    /**
     * Returns a buffer containing a unit quadrilateral expressed as four 3D vertices at (0, 1, 0), (0, 0, 0), (1, 1, 0)
     * and (1, 0, 0). The four vertices are in the order required by a triangle strip. The buffer is created on first
     * use and cached. Subsequent calls to this method return the cached buffer.
     *
     * @return
     */
    static public FloatBuffer getUnitQuadBuffer3D() {
        if (unitQuadBuffer3 == null) {
            float[] points = new float[]{
                0, 1, 0,    // upper left corner
                0, 0, 0,    // lower left corner
                1, 1, 0,    // upper right corner
                1, 0, 0};   // lower right corner
            int size = points.length * 4;
            unitQuadBuffer3 = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            unitQuadBuffer3.put(points).rewind();
        }
        return unitQuadBuffer3;
    }

    static public FloatBuffer getLeaderBuffer(Vec3 groundPoint, Vec3 placePoint) {
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
        // Use World Wind's basic GLSL program.
        BasicShaderProgram program = (BasicShaderProgram) dc.getShaderProgram(BasicShaderProgram.KEY);
        if (program == null) {
            program = (BasicShaderProgram) dc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(dc.resources));
        }

        if (!program.useProgram(dc)) {
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

        // Allocate or get a unit-quad buffer for the image coordinates
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, getUnitQuadBuffer3D());

        // Set up to use the shared tex attribute.
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, getUnitQuadBuffer2D());

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
        program.loadModelviewProjection(this.mvpMatrix);

        // Bind the texture
        if (this.activeTexture != null) {
            // Make multi-texture unit 0 active.
            dc.activeTextureUnit(GLES20.GL_TEXTURE0);
            textureBound = this.activeTexture.bindTexture(dc);
            if (textureBound) {
                program.enableTexture(true);
                // Perform a vertical flip of the bound texture to match
                // the reversed Y-axis of the screen coordinate system
                this.texCoordMatrix.setToIdentity();
                this.activeTexture.applyTexCoordTransform(this.texCoordMatrix);
                program.loadTexCoordMatrix(this.texCoordMatrix);
            }
        }

        // Load the color used for the image
        program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.imageColor); // TODO: pickColor

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
        GLES20.glEnableVertexAttribArray(1);    // vertexTexCoord
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        ///////////////////////////////////
        // Draw the label
        ///////////////////////////////////

        if (this.drawLabel) { // TODO: drawLabel
//            program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity * this.currentVisibility);
//
//            Placemark.matrix.copy(dc.screenProjection);
//            Placemark.matrix.multiplyMatrix(this.labelTransform);
//            program.loadModelviewProjection(gl, Placemark.matrix);
//
//            if (!dc.pickingMode && this.labelTexture) {
//                this.texCoordMatrix.setToIdentity();
//                this.texCoordMatrix.multiplyByTextureTransform(this.labelTexture);
//
//                program.loadTextureMatrix(gl, this.texCoordMatrix);
//                program.loadColor(gl, this.attributes.labelAttributes.color);
//
//                textureBound = this.labelTexture.bind(dc);
//                program.loadTextureEnabled(gl, textureBound);
//            } else {
//                program.loadTextureEnabled(gl, false);
//                program.loadColor(gl, this.pickColor);
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
            program.enableTexture(false);
        }
        GLES20.glDisableVertexAttribArray(1);

    }

}
