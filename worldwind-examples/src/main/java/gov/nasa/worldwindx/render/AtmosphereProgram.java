/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.render;

import android.opengl.GLES20;

import java.util.Arrays;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuProgram;
import gov.nasa.worldwind.util.Logger;

// TODO Correctly compute the atmosphere color for eye positions beneath the atmosphere
// TODO Merge GroundProgram and SkyProgram into AtmosphereProgram, including the GLSL sources
// TODO Test the effect of working in local coordinates (reference point) on the GLSL atmosphere programs
public class AtmosphereProgram extends GpuProgram {

    protected double altitude;

    protected int mvpMatrixId;

    protected int vertexOriginId;

    protected int eyePointId;

    protected int eyeMagnitudeId;

    protected int eyeMagnitude2Id;

    protected int lightDirectionId;

    protected int invWavelengthId;

    protected int atmosphereRadiusId;

    protected int atmosphereRadius2Id;

    protected int globeRadiusId;

    protected int globeRadius2Id;

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

    protected float[] array = new float[16];

    public AtmosphereProgram(String vertexShaderSource, String fragmentShaderSource, String[] attributeBindings) {
        super(vertexShaderSource, fragmentShaderSource, attributeBindings);
        this.init();
    }

    protected void init() {
        int[] prevProgram = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_CURRENT_PROGRAM, prevProgram, 0);
        GLES20.glUseProgram(this.programId);

        this.altitude = 160000;
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

        this.eyePointId = GLES20.glGetUniformLocation(this.getObjectId(), "eyePoint");
        this.eyeMagnitudeId = GLES20.glGetUniformLocation(this.getObjectId(), "eyeMagnitude");
        this.eyeMagnitude2Id = GLES20.glGetUniformLocation(this.getObjectId(), "eyeMagnitude2");
        this.lightDirectionId = GLES20.glGetUniformLocation(this.getObjectId(), "lightDirection");
        this.atmosphereRadiusId = GLES20.glGetUniformLocation(this.getObjectId(), "atmosphereRadius");
        this.atmosphereRadius2Id = GLES20.glGetUniformLocation(this.getObjectId(), "atmosphereRadius2");
        this.globeRadiusId = GLES20.glGetUniformLocation(this.getObjectId(), "globeRadius");
        this.globeRadius2Id = GLES20.glGetUniformLocation(this.getObjectId(), "globeRadius2");

        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        new Matrix4().transposeToArray(this.array, 0); // 4 x 4 identity matrix
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);

        this.vertexOriginId = GLES20.glGetUniformLocation(this.programId, "vertexOrigin");
        Arrays.fill(this.array, 0);
        GLES20.glUniform3fv(this.vertexOriginId, 1, this.array, 0);

        // Configure the program's constant uniform variables.
        this.invWavelengthId = GLES20.glGetUniformLocation(this.getObjectId(), "invWavelength");
        invWavelength.toArray(this.array, 0);
        GLES20.glUniform3fv(this.invWavelengthId, 1, this.array, 0);

        this.KrESunId = GLES20.glGetUniformLocation(this.getObjectId(), "KrESun");
        GLES20.glUniform1f(this.KrESunId, (float) (Kr * ESun));

        this.KmESunId = GLES20.glGetUniformLocation(this.getObjectId(), "KmESun");
        GLES20.glUniform1f(this.KmESunId, (float) (Km * ESun));

        this.Kr4PIId = GLES20.glGetUniformLocation(this.getObjectId(), "Kr4PI");
        GLES20.glUniform1f(this.Kr4PIId, (float) (Kr * 4 * Math.PI));

        this.Km4PIId = GLES20.glGetUniformLocation(this.getObjectId(), "Km4PI");
        GLES20.glUniform1f(this.Km4PIId, (float) (Km * 4 * Math.PI));

        this.scaleId = GLES20.glGetUniformLocation(this.getObjectId(), "scale");
        GLES20.glUniform1f(this.scaleId, (float) (1 / this.altitude));

        this.scaleDepthId = GLES20.glGetUniformLocation(this.getObjectId(), "scaleDepth");
        GLES20.glUniform1f(this.scaleDepthId, (float) (rayleighScaleDepth));

        this.scaleOverScaleDepthId = GLES20.glGetUniformLocation(this.getObjectId(), "scaleOverScaleDepth");
        GLES20.glUniform1f(this.scaleOverScaleDepthId, (float) ((1 / this.altitude) / rayleighScaleDepth));

        this.gId = GLES20.glGetUniformLocation(this.getObjectId(), "g");
        GLES20.glUniform1f(this.gId, (float) (g));

        this.g2Id = GLES20.glGetUniformLocation(this.getObjectId(), "g2");
        GLES20.glUniform1f(this.g2Id, (float) (g * g));

        this.exposureId = GLES20.glGetUniformLocation(this.getObjectId(), "exposure");
        GLES20.glUniform1f(this.exposureId, (float) exposure);

        GLES20.glUseProgram(prevProgram[0]);
    }

    public double getAltitude() {
        return altitude;
    }

    public void loadModelviewProjection(Matrix4 matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AtmosphereProgram", "loadModelviewProjection", "missingMatrix"));
        }

        matrix.transposeToArray(this.array, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.array, 0);
    }

    public void loadVertexOrigin(Vec3 vector) {
        if (vector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GroundProgram", "loadVertexOrigin", "missingVector"));
        }

        vector.toArray(this.array, 0);
        GLES20.glUniform3fv(this.vertexOriginId, 1, this.array, 0);
    }

    public void loadUniforms(DrawContext dc) {
        // Use the draw context's eye point.
        dc.getEyePoint().toArray(this.array, 0);
        GLES20.glUniform3fv(this.eyePointId, 1, this.array, 0);
        GLES20.glUniform1f(this.eyeMagnitudeId, (float) dc.getEyePoint().magnitude());
        GLES20.glUniform1f(this.eyeMagnitude2Id, (float) dc.getEyePoint().magnitudeSquared());

        // Use the draw context's light direction.
        ((Vec3) dc.getUserProperty("lightDirection")).toArray(this.array, 0);
        GLES20.glUniform3fv(this.lightDirectionId, 1, this.array, 0);

        // Use this program's atmosphere altitude.
        double r = dc.getGlobe().getEquatorialRadius() + this.altitude;
        GLES20.glUniform1f(this.atmosphereRadiusId, (float) r);
        GLES20.glUniform1f(this.atmosphereRadius2Id, (float) (r * r));

        // Use the draw context's globe radius.
        r = dc.getGlobe().getEquatorialRadius();
        GLES20.glUniform1f(this.globeRadiusId, (float) r);
        GLES20.glUniform1f(this.globeRadius2Id, (float) (r * r));
    }
}
