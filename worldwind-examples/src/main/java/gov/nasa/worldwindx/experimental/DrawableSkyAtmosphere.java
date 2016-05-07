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

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Pool;

public class DrawableSkyAtmosphere implements Drawable {

    public SkyProgram program;

    public Vec3 lightDirection = new Vec3();

    protected int skyWidth = 128;

    protected int skyHeight = 128;

    protected FloatBuffer skyPoints;

    protected ShortBuffer skyTriStrip;

    protected Sector fullSphereSector = new Sector().setFullSphere();

    private Pool<DrawableSkyAtmosphere> pool;

    public DrawableSkyAtmosphere() {
    }

    public static DrawableSkyAtmosphere obtain(Pool<DrawableSkyAtmosphere> pool) {
        DrawableSkyAtmosphere instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableSkyAtmosphere().setPool(pool);
    }

    private DrawableSkyAtmosphere setPool(Pool<DrawableSkyAtmosphere> pool) {
        this.pool = pool;
        return this;
    }

    public DrawableSkyAtmosphere set(SkyProgram program, Vec3 lightDirection) {
        this.program = program;

        if (lightDirection != null) {
            this.lightDirection.set(lightDirection);
        } else {
            this.lightDirection.set(0, 0, 1);
        }

        return this;
    }

    @Override
    public void recycle() {
        this.program = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {

        if (this.program == null) {
            return; // program unspecified
        }

        if (!this.program.useProgram(dc)) {
            return; // program failed to build
        }

        // Use the draw context's globe.
        this.program.loadGlobeRadius(dc.globe.getEquatorialRadius()); // TODO the Globe is rendering state

        // Use the draw context's eye point.
        this.program.loadEyePoint(dc.eyePoint);

        // Use this layer's light direction.
        this.program.loadLightDirection(this.lightDirection);

        // Use the vertex origin for the sky ellipsoid.
        this.program.loadVertexOrigin(0, 0, 0);

        // Use the draw context's modelview projection matrix.
        this.program.loadModelviewProjection(dc.modelviewProjection);

        // Use the sky fragment mode, which assumes the standard premultiplied alpha blending mode.
        this.program.loadFragMode(AtmosphereProgram.FRAGMODE_SKY);

        // Assemble the sky vertex points.
        // TODO the Globe is rendering state
        if (this.skyPoints == null) {
            int count = this.skyWidth * this.skyHeight;
            double[] array = new double[count];
            Arrays.fill(array, this.program.getAltitude());

            this.skyPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
            dc.globe.geographicToCartesianGrid(this.fullSphereSector, this.skyWidth, this.skyHeight, array, null,
                this.skyPoints, 3).rewind();
        }

        // Assemble the sky triangle strip indices.
        if (this.skyTriStrip == null) {
            this.skyTriStrip = assembleTriStripIndices(this.skyWidth, this.skyHeight);
        }

        // Use the sky's vertex point attribute.
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, this.skyPoints);

        // Draw the inside of the sky without writing to the depth buffer.
        GLES20.glDepthMask(false);
        GLES20.glFrontFace(GLES20.GL_CW);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.skyTriStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, this.skyTriStrip);

        // Restore the default World Wind OpenGL state.
        GLES20.glDepthMask(true);
        GLES20.glFrontFace(GLES20.GL_CCW);
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
