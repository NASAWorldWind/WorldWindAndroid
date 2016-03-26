/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

public class AtmosphereLayer extends AbstractLayer {

    //protected int nightImageId = R.drawable.dnb_land_ocean_ice_2012;

    protected int skyWidth = 128;

    protected int skyHeight = 64;

    protected FloatBuffer skyPoints;

    protected ShortBuffer skyTriStrip;

    protected Vec3 lightDirection = new Vec3();

    protected Matrix4 mvpMatrix = new Matrix4();

    //protected Matrix3 texCoordMatrix = new Matrix3();

    protected Sector fullSphereSector = new Sector().setFullSphere();

    public AtmosphereLayer() {
        super("Atmosphere");
    }

    @Override
    protected void doRender(DrawContext dc) {
        // TODO light/sun direction should be a property of the WorldWindow attached to the DrawContext each frame
        if (!dc.hasUserProperty("lightDirection")) {
            this.lightDirection.set(dc.getEyePoint()).normalize();
            dc.putUserProperty("lightDirection", this.lightDirection);
        }

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
        // TODO handle missing texture correctly
        //GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.nightImageId);
        //if (texture != null) {
        //    // Bind the texture.
        //    program.loadTextureSampler(0); // GL_TEXTURE0
        //    dc.bindTexture(GLES20.GL_TEXTURE0, texture);
        //}

        // Get the draw context's tessellated terrain and modelview projection matrix.
        Terrain terrain = dc.getTerrain();

        // Set up to use the shared tile tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Use the draw context's modelview projection matrix, transformed to the tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(dc.getModelviewProjection());
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            program.loadModelviewProjection(this.mvpMatrix);
            program.loadVertexOrigin(terrainOrigin);

            // Use the texture's transform matrix.
            // TODO handle missing texture correctly
            //if (texture != null) {
            //    this.texCoordMatrix.setToIdentity();
            //    texture.applyTexCoordTransform(this.texCoordMatrix);
            //    terrain.applyTexCoordTransform(idx, this.fullSphereSector, this.texCoordMatrix);
            //    program.loadTexCoordMatrix(this.texCoordMatrix);
            //}

            // Use the tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            // Draw the tile, multiplying the current fragment color by the program's secondary color.
            program.loadFragColor(GroundProgram.FRAGCOLOR_SECONDARY);
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
            terrain.drawTileTriangles(dc, idx);

            // Draw the tile, adding the current fragment color to the program's primary color.
            program.loadFragColor(GroundProgram.FRAGCOLOR_PRIMARY); // TODO handle missing texture correctly
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            terrain.drawTileTriangles(dc, idx);
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
            this.skyTriStrip = assembleTriStripIndices(this.skyWidth, this.skyHeight);
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.skyTriStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, this.skyTriStrip);
    }

    // TODO move this into a basic tessellator implementation in World Wind
    // TODO tessellator and atmosphere needs the TriStripIndices - could we add these to BasicGlobe (needs to be on a static context)
    // TODO may need to switch the tessellation method anyway - geographic grid may produce artifacts at the poles
    protected static ShortBuffer assembleTriStripIndices(int numLat, int numLon) {

        // Allocate a buffer to hold the indices.
        int count = ((numLat - 1) * numLon + (numLat - 2)) * 2;
        ShortBuffer result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        short[] index = new short[2];
        int vertex = 0;

        for (int latIndex = 0; latIndex < numLat - 1; latIndex++) {
            // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
            for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
                vertex = lonIndex + latIndex * numLon;
                index[0] = (short) (vertex + numLon);
                index[1] = (short) vertex;
                result.put(index);
            }

            // Insert indices to create 2 degenerate triangles:
            // - one for the end of the current row, and
            // - one for the beginning of the next row
            if (latIndex < numLat - 2) {
                index[0] = (short) vertex;
                index[1] = (short) ((latIndex + 2) * numLon);
                result.put(index);
            }
        }

        return (ShortBuffer) result.rewind();
    }
}