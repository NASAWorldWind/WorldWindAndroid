/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;

/**
 * Represents a portion of a globe's terrain. Applications typically do not interact directly with this class.
 */
public class TerrainTile extends Tile {

    protected Vec3 vertexOrigin = new Vec3();

    protected FloatBuffer vertexPoints;

    protected String vertexPointKey;

    /**
     * {@inheritDoc}
     */
    public TerrainTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
        this.vertexPointKey = this.getClass().getName() + ".vertexPoint." + this.tileKey;
    }

    public Vec3 getVertexOrigin() {
        return this.vertexOrigin;
    }

    public void setVertexOrigin(Vec3 vertexOrigin) {
        this.vertexOrigin = vertexOrigin;
    }

    public FloatBuffer getVertexPoints() {
        return this.vertexPoints;
    }

    public void setVertexPoints(FloatBuffer vertexPoints) {
        this.vertexPoints = vertexPoints;
    }

    public BufferObject getVertexPointBuffer(RenderContext rc) {
        BufferObject buffer = rc.getBufferObject(this.vertexPointKey);

        if (buffer == null) {
            buffer = rc.putBufferObject(this.vertexPointKey,
                new BufferObject(GLES20.GL_ARRAY_BUFFER, this.vertexPoints));
        }

        return buffer;
    }
}
