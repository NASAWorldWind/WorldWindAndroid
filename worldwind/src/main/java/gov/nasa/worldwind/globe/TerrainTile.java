/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;

/**
 * Represents a portion of a globe's terrain. Applications typically do not interact directly with this class.
 */
public class TerrainTile extends Tile {

    protected Vec3 vertexOrigin = new Vec3();

    protected FloatBuffer vertexPoints;

    /**
     * {@inheritDoc}
     */
    public TerrainTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
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
}
