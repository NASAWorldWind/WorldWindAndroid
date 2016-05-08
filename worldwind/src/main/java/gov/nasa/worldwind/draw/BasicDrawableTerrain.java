/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Pool;

public class BasicDrawableTerrain implements DrawableTerrain {

    public Sector sector = new Sector();

    public Vec3 vertexOrigin = new Vec3();

    public FloatBuffer vertexPoints;

    public FloatBuffer vertexTexCoords;

    public ShortBuffer lineElements;

    public ShortBuffer triStripElements;

    private Pool<BasicDrawableTerrain> pool;

    protected BasicDrawableTerrain() {
    }

    public static BasicDrawableTerrain obtain(Pool<BasicDrawableTerrain> pool) {
        BasicDrawableTerrain instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new BasicDrawableTerrain().setPool(pool);
    }

    private BasicDrawableTerrain setPool(Pool<BasicDrawableTerrain> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.vertexPoints = null;
        this.vertexTexCoords = null;
        this.lineElements = null;
        this.triStripElements = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public Vec3 getVertexOrigin() {
        return this.vertexOrigin;
    }

    @Override
    public void useVertexPointAttrib(DrawContext dc, int attribLocation) {
        if (this.vertexPoints != null) {
            GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, this.vertexPoints);
        }
    }

    @Override
    public void useVertexTexCoordAttrib(DrawContext dc, int attribLocation) {
        if (this.vertexTexCoords != null) {
            GLES20.glVertexAttribPointer(attribLocation, 2, GLES20.GL_FLOAT, false, 0, this.vertexTexCoords);
        }
    }

    @Override
    public void drawLines(DrawContext dc) {
        if (this.lineElements != null) {
            GLES20.glDrawElements(GLES20.GL_LINES, this.lineElements.remaining(), GLES20.GL_UNSIGNED_SHORT, this.lineElements);
        }
    }

    @Override
    public void drawTriangles(DrawContext dc) {
        if (this.triStripElements != null) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.triStripElements.remaining(), GLES20.GL_UNSIGNED_SHORT, this.triStripElements);
        }
    }

    @Override
    public void draw(DrawContext dc) {
        this.drawTriangles(dc);
    }
}
