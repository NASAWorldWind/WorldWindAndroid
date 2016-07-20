/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import android.opengl.GLES20;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.ShaderProgram;

// TODO Correctly compute the atmosphere color for eye positions beneath the atmosphere
// TODO Test the effect of working in local coordinates (reference point) on the GLSL atmosphere programs
public class AtmosphereProgram extends ShaderProgram {

    /**
     * Frag color indicates the atmospheric scattering color components written to the fragment color. Accepted values
     * are {@link #FRAGMODE_PRIMARY}, {@link #FRAGMODE_SECONDARY} and {@link #FRAGMODE_PRIMARY_TEX_BLEND}.
     */
    @IntDef({FRAGMODE_PRIMARY, FRAGMODE_SECONDARY, FRAGMODE_PRIMARY_TEX_BLEND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FragMode {

    }

    public static final int FRAGMODE_PRIMARY = 1;

    public static final int FRAGMODE_SECONDARY = 2;

    public static final int FRAGMODE_PRIMARY_TEX_BLEND = 3;

    protected double altitude = 160000;

    protected int fragModeId;

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected int vertexOriginId;

    protected int eyePointId;

    protected int eyeMagnitudeId;

    protected int eyeMagnitude2Id;

    protected int lightDirectionId;

    protected int invWavelengthId;

    protected int atmosphereRadiusId;

    protected int atmosphereRadius2Id;

    protected int globeRadiusId;

    protected int KrESunId;

    protected int KmESunId;

    protected int Kr4PIId;

    protected int Km4PIId;

    protected int scaleId;

    protected int scaleDepthId;

    protected int scaleOverScaleDepthId;

    protected int gId;

    protected int g2Id;

    protected int exposureId;

    private float[] array = new float[16];

    public AtmosphereProgram() {
    }

    protected void initProgram(DrawContext dc) {
        Vec3 invWavelength = new Vec3(
            1 / Math.pow(0.650, 4),  // 650 nm for red
            1 / Math.pow(0.570, 4),  // 570 nm for green
            1 / Math.pow(0.475, 4)); // 475 nm for blue
        double rayleighScaleDepth = 0.25;
        double Kr = 0.0025;        // Rayleigh scattering constant
        double Km = 0.0010;        // Mie scattering constant
        double ESun = 20.0;        // Sun brightness constant
        double g = -0.990;        // The Mie phase asymmetry factor
        double exposure = 2;

        this.fragModeId = GLES20.glGetUniformLocation(this.programId, "fragMode");
        GLES20.glUniform1i(this.fragModeId, FRAGMODE_PRIMARY);

        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        new Matrix4().transposeToArray(this.array, 0); // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);

        this.texCoordMatrixId = GLES20.glGetUniformLocation(this.programId, "texCoordMatrix");
        new Matrix3().transposeToArray(this.array, 0); // 3 x 3 identity matrix
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);

        this.texSamplerId = GLES20.glGetUniformLocation(this.programId, "texSampler");
        GLES20.glUniform1i(this.texSamplerId, 0); // GL_TEXTURE0

        this.vertexOriginId = GLES20.glGetUniformLocation(this.programId, "vertexOrigin");
        Arrays.fill(this.array, 0);
        GLES20.glUniform3fv(this.vertexOriginId, 1, this.array, 0);

        this.eyePointId = GLES20.glGetUniformLocation(this.programId, "eyePoint");
        Arrays.fill(this.array, 0);
        GLES20.glUniform3fv(this.eyePointId, 1, this.array, 0);

        this.eyeMagnitudeId = GLES20.glGetUniformLocation(this.programId, "eyeMagnitude");
        GLES20.glUniform1f(this.eyeMagnitudeId, 0);

        this.eyeMagnitude2Id = GLES20.glGetUniformLocation(this.programId, "eyeMagnitude2");
        GLES20.glUniform1f(this.eyeMagnitude2Id, 0);

        this.lightDirectionId = GLES20.glGetUniformLocation(this.programId, "lightDirection");
        Arrays.fill(this.array, 0);
        GLES20.glUniform3fv(this.lightDirectionId, 1, this.array, 0);

        this.invWavelengthId = GLES20.glGetUniformLocation(this.programId, "invWavelength");
        invWavelength.toArray(this.array, 0);
        GLES20.glUniform3fv(this.invWavelengthId, 1, this.array, 0);

        this.atmosphereRadiusId = GLES20.glGetUniformLocation(this.programId, "atmosphereRadius");
        GLES20.glUniform1f(this.atmosphereRadiusId, 0);

        this.atmosphereRadius2Id = GLES20.glGetUniformLocation(this.programId, "atmosphereRadius2");
        GLES20.glUniform1f(this.atmosphereRadius2Id, 0);

        this.globeRadiusId = GLES20.glGetUniformLocation(this.programId, "globeRadius");
        GLES20.glUniform1f(this.globeRadiusId, 0);

        this.KrESunId = GLES20.glGetUniformLocation(this.programId, "KrESun");
        GLES20.glUniform1f(this.KrESunId, (float) (Kr * ESun));

        this.KmESunId = GLES20.glGetUniformLocation(this.programId, "KmESun");
        GLES20.glUniform1f(this.KmESunId, (float) (Km * ESun));

        this.Kr4PIId = GLES20.glGetUniformLocation(this.programId, "Kr4PI");
        GLES20.glUniform1f(this.Kr4PIId, (float) (Kr * 4 * Math.PI));

        this.Km4PIId = GLES20.glGetUniformLocation(this.programId, "Km4PI");
        GLES20.glUniform1f(this.Km4PIId, (float) (Km * 4 * Math.PI));

        this.scaleId = GLES20.glGetUniformLocation(this.programId, "scale");
        GLES20.glUniform1f(this.scaleId, (float) (1 / this.altitude));

        this.scaleDepthId = GLES20.glGetUniformLocation(this.programId, "scaleDepth");
        GLES20.glUniform1f(this.scaleDepthId, (float) (rayleighScaleDepth));

        this.scaleOverScaleDepthId = GLES20.glGetUniformLocation(this.programId, "scaleOverScaleDepth");
        GLES20.glUniform1f(this.scaleOverScaleDepthId, (float) ((1 / this.altitude) / rayleighScaleDepth));

        this.gId = GLES20.glGetUniformLocation(this.programId, "g");
        GLES20.glUniform1f(this.gId, (float) (g));

        this.g2Id = GLES20.glGetUniformLocation(this.programId, "g2");
        GLES20.glUniform1f(this.g2Id, (float) (g * g));

        this.exposureId = GLES20.glGetUniformLocation(this.programId, "exposure");
        GLES20.glUniform1f(this.exposureId, (float) exposure);
    }

    public double getAltitude() {
        return altitude;
    }

    public void loadFragMode(@FragMode int fragMode) {
        GLES20.glUniform1i(this.fragModeId, fragMode);
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadTexCoordMatrix(Matrix3 matrix) {
        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 1, false, this.array, 0);
    }

    public void loadVertexOrigin(Vec3 origin) {
        origin.toArray(this.array, 0);
        GLES20.glUniform3fv(this.vertexOriginId, 1, this.array, 0);
    }

    public void loadVertexOrigin(double x, double y, double z) {
        GLES20.glUniform3f(this.vertexOriginId, (float) x, (float) y, (float) z);
    }

    public void loadLightDirection(Vec3 direction) {
        direction.toArray(this.array, 0);
        GLES20.glUniform3fv(this.lightDirectionId, 1, this.array, 0);
    }

    public void loadEyePoint(Vec3 eyePoint) {
        eyePoint.toArray(this.array, 0);
        GLES20.glUniform3fv(this.eyePointId, 1, this.array, 0);
        GLES20.glUniform1f(this.eyeMagnitudeId, (float) eyePoint.magnitude());
        GLES20.glUniform1f(this.eyeMagnitude2Id, (float) eyePoint.magnitudeSquared());
    }

    public void loadGlobeRadius(double equatorialRadius) {
        double gr = equatorialRadius;
        double ar = gr + this.altitude;
        GLES20.glUniform1f(this.globeRadiusId, (float) gr);
        GLES20.glUniform1f(this.atmosphereRadiusId, (float) ar);
        GLES20.glUniform1f(this.atmosphereRadius2Id, (float) (ar * ar));
    }
}
