/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuProgram;
import gov.nasa.worldwind.render.GpuTexture;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class SimpleGlobeLayer extends AbstractLayer {

    protected GpuProgram program;

    protected GpuTexture texture;

    protected FloatBuffer vertices;

    protected ShortBuffer triStrip;

    protected ShortBuffer lines;

    protected float[] matrix = new float[16];

    protected float colorWhite[] = {1, 1, 1, 1};

    protected float colorWhiteSemiTransparent[] = {1, 1, 1, 0.9f};

    protected boolean initialized;

    public SimpleGlobeLayer() {
        super("Simple Globe");
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
        // TODO invalid once GL context is lost
        String vertexShaderSource = WWUtil.readRawResourceAsString(dc.getContext(), R.raw.gov_nasa_worldwind_basicprogram_vert);
        String fragmentShaderSource = WWUtil.readRawResourceAsString(dc.getContext(), R.raw.gov_nasa_worldwind_basicprogram_frag);
        this.program = new GpuProgram(vertexShaderSource, fragmentShaderSource, new String[]{"vertexPoint", "vertexTexCoord"});

        // TODO invalid once GL context is lost
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(dc.getContext().getResources(), R.drawable.world_topo_bathy_200405_3, options);
        this.texture = new GpuTexture(bitmap);

        // Initialize the vertex coordinate buffer and wireframe index buffer.
        Sector sector = new Sector();
        sector.minLatitude = -90;
        sector.maxLatitude = 90;
        sector.minLongitude = -180;
        sector.maxLongitude = 180;

        int numLat = 50;
        int numLon = 100;
        this.vertices = this.assembleVertices(dc.getGlobe(), sector, numLat, numLon);
        this.triStrip = this.assembleTriStripIndices(numLat, numLon);
        this.lines = this.assembleLineIndices(numLat, numLon);
    }

    protected FloatBuffer assembleVertices(Globe globe, Sector sector, int numLat, int numLon) {

        int stride = 5;
        int count = numLat * numLon * stride;
        FloatBuffer result = ByteBuffer.allocateDirect(count * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        globe.geographicToCartesianGrid(sector, numLat, numLon, null, null, result, stride);
        result.rewind();

        float ds = 1f / (numLon > 1 ? numLon - 1 : 1);
        float dt = 1f / (numLat > 1 ? numLat - 1 : 1);
        float[] st = new float[2];

        int sIndex, tIndex;

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian
        // point corresponding to each latitude and longitude.
        for (tIndex = 0, st[1] = 0; tIndex < numLat; tIndex++, st[1] += dt) {
            if (tIndex == numLat - 1) {
                st[1] = 1; // explicitly set the last t to 1 to ensure alignment
            }

            for (sIndex = 0, st[0] = 0; sIndex < numLon; sIndex++, st[0] += ds) {
                if (sIndex == numLon - 1) {
                    st[0] = 1; // explicitly set the last s to 1 to ensure alignment
                }

                result.position(result.position() + 3);
                result.put(st, 0, 2);
            }
        }

        return (FloatBuffer) result.rewind();
    }

    protected ShortBuffer assembleTriStripIndices(int numLat, int numLon) {

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
            if (latIndex < numLat - 2) {
                index[0] = (short) vertex; // one for the end of the current row
                index[1] = (short) ((latIndex + 2) * numLon); // one for the beginning of the next row
                result.put(index);
            }
        }

        return (ShortBuffer) result.rewind();
    }

    protected ShortBuffer assembleLineIndices(int numLat, int numLon) {

        // Allocate a buffer to hold the indices.
        int count = (numLat * (numLon - 1) + numLon * (numLat - 1)) * 2;
        ShortBuffer result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        short[] index = new short[2];

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

    protected void draw(DrawContext dc) {

        // Use this layer's OpenGL program.
        dc.useProgram(this.program);

        // Specify the vertex point attribute.
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glEnableVertexAttribArray(1);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 20, this.vertices.position(0));
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 20, this.vertices.position(3));

        // Use the draw context's modelview projection matrix.
        int location = GLES20.glGetUniformLocation(this.program.getObjectId(), "mvpMatrix");
        dc.getModelviewProjection().transposeToArray(this.matrix, 0);
        GLES20.glUniformMatrix4fv(location, 1, false, this.matrix, 0);

        // Use the texture's transform matrix.
        location = GLES20.glGetUniformLocation(this.program.getObjectId(), "texCoordMatrix");
        Matrix3 texCoordMatrix = new Matrix3();
        this.texture.applyTextureTransform(texCoordMatrix);
        texCoordMatrix.transposeToArray(this.matrix, 0);
        GLES20.glUniformMatrix3fv(location, 1, false, this.matrix, 0);

        // Disable the texture.
        location = GLES20.glGetUniformLocation(this.program.getObjectId(), "enableTexture");
        GLES20.glUniform1i(location, 0);

        // Draw the lines in white.
        location = GLES20.glGetUniformLocation(this.program.getObjectId(), "color");
        GLES20.glUniform4fv(location, 1, this.colorWhite, 0);
        GLES20.glDrawElements(GLES20.GL_LINES, this.lines.remaining(), GLES20.GL_UNSIGNED_SHORT, this.lines);

        // Enable the texture.
        location = GLES20.glGetUniformLocation(this.program.getObjectId(), "enableTexture");
        GLES20.glUniform1i(location, 1);
        location = GLES20.glGetUniformLocation(this.program.getObjectId(), "texSampler");
        GLES20.glUniform1i(location, 0); // texture unit 0 (e.g. GL_TEXTURE0)
        dc.bindTexture(GLES20.GL_TEXTURE0, this.texture);

        // Draw the triangle strip in the texture color, pushing the depth values slightly away from the eye.
        location = GLES20.glGetUniformLocation(this.program.getObjectId(), "color");
        GLES20.glUniform4fv(location, 1, this.colorWhiteSemiTransparent, 0);
        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
        GLES20.glPolygonOffset(1, 1);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.triStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, this.triStrip);
        GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL);
        GLES20.glPolygonOffset(0, 0);
    }
}
