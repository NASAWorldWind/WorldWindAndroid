/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.opengl.GLES20;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class BasicShaderProgram extends ShaderProgram {

    public static final Object KEY = BasicShaderProgram.class;

    protected int enablePickModeId;

    protected int enableTextureId;

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected int colorId;

    private float[] array = new float[16];

    public BasicShaderProgram(Resources resources) {
        try {
            String vs = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_basicprogram_vert);
            String fs = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_basicprogram_frag);
            this.setProgramSources(vs, fs);
            this.setAttribBindings("vertexPoint", "vertexTexCoord");
        } catch (Exception logged) {
            Logger.logMessage(Logger.ERROR, "BasicShaderProgram", "constructor", "errorReadingProgramSource", logged);
        }
    }

    protected void initProgram(DrawContext dc) {
        this.enablePickModeId = GLES20.glGetUniformLocation(this.programId, "enablePickMode");
        GLES20.glUniform1i(this.enablePickModeId, 0); // disable pick mode

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
    }

    public void enablePickMode(boolean enable) {
        GLES20.glUniform1i(this.enablePickModeId, enable ? 1 : 0);
    }

    public void enableTexture(boolean enable) {
        GLES20.glUniform1i(this.enableTextureId, enable ? 1 : 0);
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadTexCoordMatrix(Matrix3 matrix) {
        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);
    }

    public void loadColor(Color color) {
        this.loadColor(color.red, color.green, color.blue, color.alpha);
    }

    public void loadColor(float r, float g, float b, float a) {
        GLES20.glUniform4f(this.colorId, r * a, g * a, b * a, a);
    }
}
