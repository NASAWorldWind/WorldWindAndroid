/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.opengl.GLES20;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuProgram;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class SimpleGlobeLayer extends AbstractLayer {

    protected GpuProgram program;

    protected FloatBuffer vertices;

    protected ShortBuffer triStrip;

    protected ShortBuffer lines;

    protected float[] mvpMatrix = new float[16];

    protected float frontColor[] = new float[4];

    protected float backColor[] = new float[4];

    protected boolean initialized;

    public SimpleGlobeLayer() {
        super("Wireframe Globe");
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (!this.initialized) {
            try {
                this.init(dc);
            } catch (Exception e) {
                Logger.log(Logger.ERROR, "Exception initializing layer " + this.getDisplayName(), e);
            } finally {
                this.initialized = true;
            }
        }

        this.draw(dc);
    }

    protected void init(DrawContext dc) throws IOException {
        // Initialize the OpenGL program.
        // DO NOT COMMIT invalid once GL context is lost
        String vertexShaderSource = WWUtil.readRawResourceAsString(dc.getContext(), R.raw.gov_nasa_worldwind_basicprogram_vert);
        String fragmentShaderSource = WWUtil.readRawResourceAsString(dc.getContext(), R.raw.gov_nasa_worldwind_basicprogram_frag);
        this.program = new GpuProgram(vertexShaderSource, fragmentShaderSource, null);

        // Initialize the vertex coordinate buffer and wireframe index buffer.
        Sector sector = new Sector();
        sector.minLatitude = -90;
        sector.maxLatitude = 90;
        sector.minLongitude = -180;
        sector.maxLongitude = 180;

        int numLat = 8;
        int numLon = 16;
        this.vertices = this.assembleVertices(dc.getGlobe(), sector, numLat, numLon);
        this.triStrip = this.assembleTriStripIndices(numLat, numLon);
        this.lines = this.assembleLineIndices(numLat, numLon);

        // Initialize the front and back colors.
        int color = dc.getContext().getResources().getColor(R.color.colorTriangleFront);
        this.colorToArray(color, this.frontColor);

        color = dc.getContext().getResources().getColor(R.color.colorTriangleBack);
        this.colorToArray(color, this.backColor);
    }

    protected FloatBuffer assembleVertices(Globe globe, Sector sector, int numLat, int numLon) {

        int count = numLat * numLon * 3;
        FloatBuffer result = ByteBuffer.allocateDirect(count * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        globe.geographicToCartesianGrid(sector, numLat, numLon, null, null, result);

        return (FloatBuffer) result.rewind();
    }

    protected ShortBuffer assembleTriStripIndices(int numLat, int numLon) {
        for (var lonIndex = 1; lonIndex < numLonVertices - 2; lonIndex += 1) {
            for (var latIndex = 1; latIndex < numLatVertices - 1; latIndex += 1) {
                vertexIndex = lonIndex + latIndex * numLonVertices;

                // Create a triangle strip joining each adjacent column of vertices, starting in the top left corner and
                // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create a
                // counterclockwise winding order.
                indices[index++] = vertexIndex;
                indices[index++] = vertexIndex + 1;
            }

            // Insert indices to create 2 degenerate triangles:
            //      one for the end of the current row, and
            //      one for the beginning of the next row.
            indices[index++] = vertexIndex + 1;
            vertexIndex = (lonIndex + 1) + 1 * numLonVertices;
            indices[index++] = vertexIndex;
        }
    }

    protected ShortBuffer assembleLineIndices(int numLat, int numLon) {

        // Allocate an array to hold the computed indices.
        int count = (numLat * (numLon - 1) + numLon * (numLat - 1)) * 2;
        short[] index = new short[2];
        ShortBuffer result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer();

        // Add a line between each row to define the horizontal cell outlines.
        for (int latIndex = 0; latIndex < numLat; latIndex++) {
            for (int lonIndex = 0; lonIndex < numLon - 1; lonIndex++) {
                int vertex = lonIndex + latIndex * numLon;
                index[0] = (short) vertex;
                index[1] = (short) (vertex + 1);
                result.put(index);
            }
        }

        // Add a line between each column to define the vertical cell outlines.
        for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
            for (int latIndex = 0; latIndex < numLat - 1; latIndex++) {
                int vertex = lonIndex + latIndex * numLon;
                index[0] = (short) vertex;
                index[1] = (short) (vertex + numLon);
                result.put(index);
            }
        }

        return (ShortBuffer) result.rewind();
    }

    protected void colorToArray(int argb, float[] result) {
        result[0] = (argb >> 16 & 0xFF) / 255f;
        result[1] = (argb >> 8 & 0xFF) / 255f;
        result[2] = (argb & 0xFF) / 255f;
        result[3] = (argb >> 24 & 0xFF) / 255f;
    }

    protected void draw(DrawContext dc) {

        // Use this layer's OpenGL program.
        dc.useProgram(this.program);

        // Use the globe coordinates as the vertex point.
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, this.vertices);

        // Use the draw context's modelview projection matrix.
        int location = GLES20.glGetUniformLocation(this.program.getProgramId(), "mvpMatrix");
        dc.getModelviewProjection().transposeToArray(this.mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(location, 1, false, this.mvpMatrix, 0);

        // Draw the wireframe globe.
        location = GLES20.glGetUniformLocation(this.program.getProgramId(), "color");
        GLES20.glUniform4fv(location, 1, this.color, 0);
        GLES20.glDrawElements(GLES20.GL_LINES, this.lines.remaining(), GLES20.GL_UNSIGNED_SHORT, this.lines);


    }
}
