/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.layer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;
import gov.nasa.worldwindx.ExampleUtil;
import gov.nasa.worldwindx.R;
import gov.nasa.worldwindx.render.AtmosphereProgram;
import gov.nasa.worldwindx.render.GroundProgram;
import gov.nasa.worldwindx.render.SkyProgram;

public class GroundLayer extends AbstractLayer {

    protected int nightImageId = R.drawable.dnb_land_ocean_ice_2012;

    protected int skyWidth = 128;

    protected int skyHeight = 64;

    protected FloatBuffer skyPoints;

    protected ShortBuffer skyTriStrip;

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Sector fullSphereSector = new Sector().setFullSphere();

    public GroundLayer() {
        super("Atmosphere");
    }

    @Override
    protected void doRender(DrawContext dc) {
        this.drawGround(dc);
        this.drawSky(dc);
    }

    protected void drawGround(DrawContext dc) {

        GroundProgram program = (GroundProgram) dc.getGpuObjectCache().retrieveProgram(dc,
            GroundProgram.class);
        if (program == null) {
            return; // program is not in the GPU object cache yet
        }

        // Use this layer's ground GLSL program.
        dc.useProgram(program);

        // Apply the draw context's properties to the program.
        program.loadUniforms(dc);

        // Attempt to use this layer's texture.
        GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.nightImageId);
        if (texture != null) {
            // Bind the texture.
            program.loadTextureSampler(0); // GL_TEXTURE0
            dc.bindTexture(GLES20.GL_TEXTURE0, texture);
        }

        // Get the draw context's tessellated terrain and modelview projection matrix.
        Terrain terrain = dc.getTerrain();
        Matrix4 dcmvp = dc.getModelviewProjection();

        // Set up to use the shared tile tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        for (int tileIdx = 0; tileIdx < terrain.getTileCount(); tileIdx++) {

            // Use the draw context's modelview projection matrix, offset by the tile's origin.
            Vec3 origin = terrain.getTileOrigin(tileIdx);
            this.mvpMatrix.set(dcmvp).multiplyByTranslation(origin.x, origin.y, origin.z);
            program.loadModelviewProjection(this.mvpMatrix);
            program.loadVertexOrigin(origin);

            // Use the texture's transform matrix.
            if (texture != null) {
                this.texCoordMatrix.setToIdentity();
                texture.applyTexCoordTransform(this.texCoordMatrix);
                terrain.applyTexCoordTransform(tileIdx, this.fullSphereSector, this.texCoordMatrix);
                program.loadTextureTransform(this.texCoordMatrix);
            }

            // Use the tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, tileIdx, 0);

            // Draw the tile, multiplying the current fragment color by the program's secondary color.
            program.loadFragColor(GroundProgram.FRAGCOLOR_SECONDARY);
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
            terrain.drawTileTriangles(dc, tileIdx);

            // Draw the tile, adding the current fragment color to the program's primary color.
            program.loadFragColor(GroundProgram.FRAGCOLOR_PRIMARY_TEX_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            terrain.drawTileTriangles(dc, tileIdx);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisableVertexAttribArray(1);
    }

    protected void drawSky(DrawContext dc) {

        AtmosphereProgram program = (AtmosphereProgram) dc.getGpuObjectCache().retrieveProgram(dc,
            SkyProgram.class);
        if (program == null) {
            return; // program is not in the GPU object cache yet
        }

        // Use this layer's sky GLSL program.
        dc.useProgram(program);

        // Use the draw context's modelview projection matrix.
        program.loadModelviewProjection(dc.getModelviewProjection());

        // Apply the draw context's properties to the program.
        program.loadUniforms(dc);

        // Use the sky's vertex point attribute.
        this.useSkyVertexPointAttrib(dc, program.getAltitude(), 0);

        // Draw the inside of the sky without writing to the depth buffer.
        GLES20.glDepthMask(false);
        GLES20.glFrontFace(GLES20.GL_CW);
        this.drawSkyTriangles(dc);

        // Restore the default World Wind OpenGL state.
        GLES20.glDepthMask(true);
        GLES20.glFrontFace(GLES20.GL_CCW);
    }

    protected void useSkyVertexPointAttrib(DrawContext dc, double altitude, int attribLocation) {
        if (this.skyPoints == null) {
            int count = this.skyWidth * this.skyHeight;
            double[] array = new double[count];
            Arrays.fill(array, altitude);

            this.skyPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
            dc.getGlobe().geographicToCartesianGrid(this.fullSphereSector, this.skyWidth, this.skyHeight, array, null,
                this.skyPoints, 3).rewind();
        }

        GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, this.skyPoints);
    }

    protected void drawSkyTriangles(DrawContext dc) {
        if (this.skyTriStrip == null) {
            this.skyTriStrip = ExampleUtil.assembleTriStripIndices(this.skyWidth, this.skyHeight);
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.skyTriStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, this.skyTriStrip);
    }
}