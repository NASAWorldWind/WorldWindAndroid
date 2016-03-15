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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwindx.ExampleUtil;
import gov.nasa.worldwindx.render.AtmosphereProgram;
import gov.nasa.worldwindx.render.SkyProgram;

public class SkyLayer extends AbstractLayer {

    protected FloatBuffer vertices;

    protected ShortBuffer triStrip;

    public SkyLayer() {
        super("Atmosphere (Sky)");
    }

    @Override
    protected void doRender(DrawContext dc) {

        AtmosphereProgram program = (AtmosphereProgram) dc.getGpuObjectCache().retrieveProgram(dc,
            SkyProgram.class);
        if (program == null) {
            return;
        }

        if (this.vertices == null) {
            this.assembleGeometry(dc, program.getAltitude());
        }

        // Use this layer's OpenGL program.
        dc.useProgram(program);

        // Use the draw context's modelview projection matrix.
        program.loadModelviewProjection(dc.getModelviewProjection());

        // Apply the draw context's properties to the atmosphere program.
        program.loadUniforms(dc);

        // Specify the vertex point attribute.
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, this.vertices);

        // Draw the inside of the atmosphere ellipsoid without writing to the depth buffer.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CW);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.triStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, this.triStrip);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CCW);
    }

    protected void assembleGeometry(DrawContext dc, double altitude) {
        int numLat = 50;
        int numLon = 100;
        int count = numLat * numLon;

        Sector sector = new Sector().setFullSphere();
        double[] array = new double[count];
        Arrays.fill(array, altitude);

        this.vertices = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
        dc.getGlobe().geographicToCartesianGrid(sector, numLat, numLon, array, null, this.vertices, 3).rewind();

        this.triStrip = ExampleUtil.assembleTriStripIndices(numLat, numLon);
    }
}
