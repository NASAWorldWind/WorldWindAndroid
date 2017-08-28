/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.opengl.GLES20;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class SightlineProgram extends ShaderProgram {

    public static final Object KEY = SightlineProgram.class;

    protected int mvpMatrixId;

    protected int slpMatrixId;

    protected int rangeId;

    protected int depthSamplerId;

    protected int colorId;

    private float[] array = new float[32];

    public SightlineProgram(Resources resources) {
        try {
            String vs = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_sightlineprogram_vert);
            String fs = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_sightlineprogram_frag);
            this.setProgramSources(vs, fs);
            this.setAttribBindings("vertexPoint");
        } catch (Exception logged) {
            Logger.logMessage(Logger.ERROR, "SightlineProgram", "constructor", "errorReadingProgramSource", logged);
        }
    }

    protected void initProgram(DrawContext dc) {
        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);

        this.slpMatrixId = GLES20.glGetUniformLocation(this.programId, "slpMatrix");
        GLES20.glUniformMatrix4fv(this.slpMatrixId, 2, false, this.array, 0);

        this.rangeId = GLES20.glGetUniformLocation(this.programId, "range");
        GLES20.glUniform1f(this.rangeId, 0);

        this.colorId = GLES20.glGetUniformLocation(this.programId, "color");
        GLES20.glUniform4f(this.colorId, 1, 1, 1, 1);

        this.depthSamplerId = GLES20.glGetUniformLocation(this.programId, "depthSampler");
        GLES20.glUniform1i(this.depthSamplerId, 0); // GL_TEXTURE0
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadSightlineProjection(Matrix4 projection, Matrix4 sightline) {
        projection.transposeToArray(this.array, 0);
        sightline.transposeToArray(this.array, 16);
        GLES20.glUniformMatrix4fv(this.slpMatrixId, 2, false, this.array, 0);
    }

    public void loadRange(float range) {
        GLES20.glUniform1f(this.rangeId, range);
    }

    public void loadColor(Color visibleColor, Color occludedColor) {
        visibleColor.premultiplyToArray(this.array, 0);
        occludedColor.premultiplyToArray(this.array, 4);
        GLES20.glUniform4fv(this.colorId, 2, this.array, 0);
    }
}
