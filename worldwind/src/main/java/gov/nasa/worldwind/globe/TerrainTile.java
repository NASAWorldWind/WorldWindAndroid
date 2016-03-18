/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;

/**
 * Represents a portion of a globe's terrain. Applications typically do not interact directly with this class.
 */
public class TerrainTile extends Tile {

    /**
     *
     */
    public Vec3 tileOrigin;

    /**
     *
     */
    public FloatBuffer tileVertices;

    /**
     * {@inheritDoc}
     */
    public TerrainTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    public boolean mustAssembleTileVertices(DrawContext dc) {
        return this.tileVertices == null;
    }

    public void assembleTileVertices(DrawContext dc) {
        int numLat = this.level.tileWidth;
        int numLon = this.level.tileHeight;

        if (this.tileOrigin == null) {
            this.tileOrigin = new Vec3();
        }

        if (this.tileVertices == null) {
            this.tileVertices = ByteBuffer.allocateDirect(numLat * numLon * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        Globe globe = dc.getGlobe();
        globe.geographicToCartesian(this.sector.centroidLatitude(), this.sector.centroidLongitude(), 0, this.tileOrigin);
        globe.geographicToCartesianGrid(this.sector, numLat, numLon, null, this.tileOrigin, this.tileVertices, 3).rewind();
    }
}
