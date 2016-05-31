/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.util.Pool;

public class BasicDrawableTerrain implements DrawableTerrain {

    public Sector sector = new Sector();

    public Vec3 vertexOrigin = new Vec3();

    public BufferObject vertexPoints;

    public BufferObject vertexTexCoords;

    public BufferObject lineElements;

    public BufferObject triStripElements;

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
            this.vertexPoints.bindBuffer(dc);
            GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
        }
    }

    @Override
    public void useVertexTexCoordAttrib(DrawContext dc, int attribLocation) {
        if (this.vertexTexCoords != null) {
            this.vertexTexCoords.bindBuffer(dc);
            GLES20.glVertexAttribPointer(attribLocation, 2, GLES20.GL_FLOAT, false, 0, 0);
        }
    }

    @Override
    public void drawLines(DrawContext dc) {
        if (this.lineElements != null) {
            this.lineElements.bindBuffer(dc);
            GLES20.glDrawElements(GLES20.GL_LINES, this.lineElements.getBufferLength(), GLES20.GL_UNSIGNED_SHORT, 0);
        }
    }

    @Override
    public void drawTriangles(DrawContext dc) {
        if (this.triStripElements != null) {
            this.triStripElements.bindBuffer(dc);
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, this.triStripElements.getBufferLength(), GLES20.GL_UNSIGNED_SHORT, 0);
        }
    }

    @Override
    public void draw(DrawContext dc) {
        this.drawTriangles(dc);
    }
}
