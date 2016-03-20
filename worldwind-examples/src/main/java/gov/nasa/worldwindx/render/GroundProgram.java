/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.render;

import android.opengl.GLES20;
import android.support.annotation.IntDef;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.R;

public class GroundProgram extends AtmosphereProgram {

    public static final int FRAGCOLOR_PRIMARY = 1;
    public static final int FRAGCOLOR_SECONDARY = 2;
    public static final int FRAGCOLOR_PRIMARY_TEX_BLEND = 3;

    /**
     * Frag color indicates the atmospheric scattering color components written to the fragment color. Accepted values are {@link
     * #FRAGCOLOR_PRIMARY}, {@link #FRAGCOLOR_SECONDARY} and {@link #FRAGCOLOR_PRIMARY_TEX_BLEND}.
     */
    @IntDef({FRAGCOLOR_PRIMARY, FRAGCOLOR_SECONDARY, FRAGCOLOR_PRIMARY_TEX_BLEND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragColor {

    }

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected int fragColorId;

    public GroundProgram(DrawContext dc) throws IOException {
        super(WWUtil.readResourceAsText(dc.getContext(), R.raw.gov_nasa_worldwind_groundprogram_vert),
            WWUtil.readResourceAsText(dc.getContext(), R.raw.gov_nasa_worldwind_groundprogram_frag),
            new String[]{"vertexPoint", "vertexTexCoord"});
    }

    @Override
    protected void init() {
        super.init();

        int[] prevProgram = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, prevProgram, 0);
        GLES20.glUseProgram(this.programId);

        this.texCoordMatrixId = GLES20.glGetUniformLocation(this.programId, "texCoordMatrix");
        new Matrix3().transposeToArray(this.array, 0); // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);

        this.texSamplerId = GLES20.glGetUniformLocation(this.programId, "texSampler");
        GLES20.glUniform1i(this.texSamplerId, 0);

        this.fragColorId = GLES20.glGetUniformLocation(this.programId, "fragColor");
        GLES20.glUniform1i(this.fragColorId, FRAGCOLOR_PRIMARY);

        GLES20.glUseProgram(prevProgram[0]);
    }

    public void loadTexCoordMatrix(Matrix3 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GroundProgram", "loadTextureTransform", "missingMatrix"));
        }

        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);
    }

    public void loadTextureSampler(int sampler) {
        GLES20.glUniform1i(this.texSamplerId, sampler);
    }

    public void loadFragColor(@FragColor int fragColor) {
        GLES20.glUniform1i(this.fragColorId, fragColor);
    }
}
