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
import gov.nasa.worldwind.util.WWUtil;

// TODO Try accumulating surface tile state (texCoordMatrix, texSampler), loading uniforms once, then loading a uniform
// TODO index to select the state for a surface tile. This reduces the uniform calls when many surface tiles intersect
// TODO one terrain tile.
// TODO Try class representing transform with a specific scale+translate object that can be uploaded to a GLSL vec4
public class SurfaceTileProgram extends GpuProgram {

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected float[] array = new float[18];

    public SurfaceTileProgram(DrawContext dc) throws IOException {
        super(WWUtil.readResourceAsText(dc.getResources(), R.raw.gov_nasa_worldwind_surfacetileprogram_vert),
            WWUtil.readResourceAsText(dc.getResources(), R.raw.gov_nasa_worldwind_surfacetileprogram_frag),
            new String[]{"vertexPoint", "vertexTexCoord"});
        this.init();
    }

    protected void init() {
        int[] prevProgram = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, prevProgram, 0);
        GLES20.glUseProgram(this.programId);

        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        Matrix4 identity4x4 = new Matrix4(); // 4 x 4 identity matrix
        identity4x4.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);

        this.texCoordMatrixId = GLES20.glGetUniformLocation(this.programId, "texCoordMatrix");
        Matrix3 identity3x3 = new Matrix3(); // 3 x 3 identity matrix
        identity3x3.transposeToArray(this.array, 0);
        identity3x3.transposeToArray(this.array, 9);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.array, 0);

        this.texSamplerId = GLES20.glGetUniformLocation(this.programId, "texSampler");
        GLES20.glUniform1i(this.texSamplerId, 0); // GL_TEXTURE0

        GLES20.glUseProgram(prevProgram[0]);
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadTexCoordMatrix(Matrix3[] matrix) {
        matrix[0].transposeToArray(this.array, 0);
        matrix[1].transposeToArray(this.array, 9);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.array, 0);
    }
}
