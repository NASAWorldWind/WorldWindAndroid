/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    /**
     * Minimum elevation value used by the BasicTessellator to determine the terrain mesh edge extension depth (skirt).
     * This value is scaled by the vertical exaggeration when the terrain is generated.
     */
    protected float minTerrainElevation = -Short.MAX_VALUE;

    protected float[] heights;

    protected float[] points;

    protected Vec3 origin = new Vec3();

    private long heightTimestamp;

    private double verticalExaggeration;

    private String pointBufferKey;

    private static long pointBufferSequence; // must be static to avoid cache collisions when a tile instances is destroyed and re-created

    /**
     * {@inheritDoc}
     */
    public TerrainTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    public double getDistanceToCamera() {
        return distanceToCamera;
    }

    public float[] getHeights() {
        return heights;
    }

    public void setHeights(float[] heights) {
        this.heights = heights;
    }

    public float[] getHeightLimits() {
        return heightLimits;
    }

    public void setHeightLimits(float[] heightLimits) {
        this.heightLimits = heightLimits;
    }

    protected long getHeightTimestamp() {
        return heightTimestamp;
    }

    protected void setHeightTimestamp(long timestampMillis) {
        this.heightTimestamp = timestampMillis;
    }

    public float[] getPoints() {
        return this.points;
    }

    public void setPoints(float[] points) {
        this.points = points;
        this.pointBufferKey = "TerrainTile.points." + this.tileKey + "." + (pointBufferSequence++);
    }

    public Vec3 getOrigin() {
        return this.origin;
    }

    public void setOrigin(Vec3 origin) {
        this.origin = origin;
    }

    protected double getVerticalExaggeration() {
        return verticalExaggeration;
    }

    protected void setVerticalExaggeration(double verticalExaggeration) {
        this.verticalExaggeration = verticalExaggeration;
    }

    public BufferObject getPointBuffer(RenderContext rc) {
        if (this.points == null) {
            return null;
        }

        BufferObject bufferObject = rc.getBufferObject(this.pointBufferKey);
        if (bufferObject != null) {
            return bufferObject;
        }

        // TODO consider a pool of terrain tiles
        // TODO consider a pool of terrain tile vertex buffers
        int size = this.points.length * 4;
        FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(this.points).rewind();

        return rc.putBufferObject(this.pointBufferKey, new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer));
    }
}
