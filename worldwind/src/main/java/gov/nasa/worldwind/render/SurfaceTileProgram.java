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

public class SurfaceTileProgram extends GpuProgram {

    protected boolean enableTexMask;

    protected int enableTexMaskId;

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected float[] array = new float[18];

    public SurfaceTileProgram(DrawContext dc) throws IOException {
        super(WWUtil.readResourceAsText(dc.getContext(), R.raw.gov_nasa_worldwind_surfacetileprogram_vert),
            WWUtil.readResourceAsText(dc.getContext(), R.raw.gov_nasa_worldwind_surfacetileprogram_frag),
            new String[]{"vertexPoint", "vertexTexCoord"});
        this.init();
    }

    protected void init() {
        int[] prevProgram = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, prevProgram, 0);
        GLES20.glUseProgram(this.programId);

        this.enableTexMaskId = GLES20.glGetUniformLocation(this.programId, "enableTexMask");
        GLES20.glUniform1i(this.enableTexMaskId, this.enableTexMask ? 1 : 0); // disable texture mask

        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        new Matrix4().transposeToArray(this.array, 0); // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);

        this.texCoordMatrixId = GLES20.glGetUniformLocation(this.programId, "texCoordMatrix");
        new Matrix3().transposeToArray(this.array, 0); // 3 x 3 identity matrix
        new Matrix3().transposeToArray(this.array, 9); // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.array, 0);

        this.texSamplerId = GLES20.glGetUniformLocation(this.programId, "texSampler");
        GLES20.glUniform1i(this.texSamplerId, 0); // GL_TEXTURE0

        GLES20.glUseProgram(prevProgram[0]);
    }

    public void enableTexMask(boolean enable) {
        this.enableTexMask = enable;
        GLES20.glUniform1i(this.enableTexMaskId, enable ? 1 : 0);
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicProgram", "loadModelviewProjection", "missingMatrix"));
        }

        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadTexCoordMatrix(Matrix3[] matrix) {
        if (matrix == null || matrix.length < 2 || matrix[0] == null || matrix[1] == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicProgram", "loadTexCoordMatrix", "missingMatrix"));
        }

        if (this.enableTexMask) {
            matrix[0].transposeToArray(this.array, 0);
            matrix[1].transposeToArray(this.array, 9);
            GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.array, 0);
        } else {
            matrix[0].transposeToArray(this.array, 0);
            GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);
        }
    }
}
