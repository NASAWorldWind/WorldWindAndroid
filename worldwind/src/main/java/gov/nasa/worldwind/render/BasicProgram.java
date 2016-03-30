/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;

import java.io.IOException;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class BasicProgram extends GpuProgram {

    protected int enableTextureId;

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected int colorId;

    protected float[] array = new float[16];

    public BasicProgram(DrawContext dc) throws IOException {
        super(WWUtil.readResourceAsText(dc.getResources(), R.raw.gov_nasa_worldwind_basicprogram_vert),
            WWUtil.readResourceAsText(dc.getResources(), R.raw.gov_nasa_worldwind_basicprogram_frag),
            new String[]{"vertexPoint", "vertexTexCoord"});
        this.init();
    }

    protected void init() {
        int[] prevProgram = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, prevProgram, 0);
        GLES20.glUseProgram(this.programId);

        this.enableTextureId = GLES20.glGetUniformLocation(this.programId, "enableTexture");
        GLES20.glUniform1i(this.enableTextureId, 0); // disable texture

        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        new Matrix4().transposeToArray(this.array, 0); // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);

        this.texCoordMatrixId = GLES20.glGetUniformLocation(this.programId, "texCoordMatrix");
        new Matrix3().transposeToArray(this.array, 0); // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);

        this.colorId = GLES20.glGetUniformLocation(this.programId, "color");
        GLES20.glUniform4f(this.colorId, 1, 1, 1, 1); // opaque white

        this.texSamplerId = GLES20.glGetUniformLocation(this.programId, "texSampler");
        GLES20.glUniform1i(this.texSamplerId, 0); // GL_TEXTURE0

        GLES20.glUseProgram(prevProgram[0]);
    }

    public void enableTexture(boolean enable) {
        GLES20.glUniform1i(this.enableTextureId, enable ? 1 : 0);
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicProgram", "loadModelviewProjection", "missingMatrix"));
        }

        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadTexCoordMatrix(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicProgram", "loadTexCoordMatrix", "missingMatrix"));
        }

        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);
    }

    public void loadColor(float r, float g, float b, float a) {
        GLES20.glUniform4f(this.colorId, r * a, g * a, b * a, a);
    }
}
