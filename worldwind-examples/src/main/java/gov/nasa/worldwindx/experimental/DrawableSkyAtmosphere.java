/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Pool;

public class DrawableSkyAtmosphere implements Drawable {

    public SkyProgram program;

    public Vec3 lightDirection = new Vec3();

    public double globeRadius;

    public FloatBuffer vertexPoints;

    public ShortBuffer triStripElements;

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
        this.program.loadGlobeRadius(this.globeRadius);

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

        if (this.vertexPoints == null || this.triStripElements == null) {
            return; // vertex data or element data unspecified
        }

        // Use the sky's vertex point attribute.
        GLES20.glVertexAttribPointer(0 /*vertexPoint*/, 3, GLES20.GL_FLOAT, false, 0, this.vertexPoints);

        // Draw the inside of the sky without writing to the depth buffer.
        GLES20.glDepthMask(false);
        GLES20.glFrontFace(GLES20.GL_CW);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.triStripElements.remaining(), GLES20.GL_UNSIGNED_SHORT, this.triStripElements);

        // Restore the default World Wind OpenGL state.
        GLES20.glDepthMask(true);
        GLES20.glFrontFace(GLES20.GL_CCW);
    }
}
