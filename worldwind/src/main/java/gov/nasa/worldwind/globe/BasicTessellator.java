/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class BasicTessellator implements Tessellator, TileFactory {

    protected LevelSet levels = new LevelSet(new Sector().setFullSphere(), 90, 20, 32, 32);

    protected List<Tile> topLevelTiles = new ArrayList<>();

    protected BasicTerrain terrain = new BasicTerrain();

    public BasicTessellator() {
    }

    @Override
    public Terrain tessellate(DrawContext dc) {
        this.assembleTiles(dc);
        this.assembleSharedBuffers();

        return this.terrain;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        return new TerrainTile(sector, level, row, column);
    }

    protected void assembleTiles(DrawContext dc) {
        this.terrain.clearTiles();

        if (this.topLevelTiles.isEmpty()) {
            Tile.assembleTilesForLevel(this.levels.firstLevel(), this, this.topLevelTiles);
        }

        for (Tile tile : this.topLevelTiles) {
            TerrainTile tt = (TerrainTile) tile;

            // TODO test visibility against draw context frustum
            // TODO addTileOrDescendants
            // TODO subdivide into lru cache with capacity for 300 tiles, ignoring estimates on actual size

            if (tt.mustAssembleTileVertices(dc)) {
                tt.assembleTileVertices(dc);
            }

            this.terrain.addTile((TerrainTile) tile);
        }
    }

    protected void assembleSharedBuffers() {
        int numLat = this.levels.tileHeight;
        int numLon = this.levels.tileWidth;

        if (this.terrain.getTileTexCoords() == null) {
            FloatBuffer buffer = ByteBuffer.allocateDirect(numLat * numLon * 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.assembleTexCoords(numLat, numLon, buffer, 2).rewind();
            this.terrain.setTileTexCoords(buffer);
        }

        if (this.terrain.getTileTriStripIndices() == null) {
            ShortBuffer buffer = this.assembleTriStripIndices(numLat, numLon);
            this.terrain.setTileTriStripIndices(buffer);
        }

        if (this.terrain.getTileLineIndices() == null) {
            ShortBuffer buffer = this.assembleLineIndices(numLat, numLon);
            this.terrain.setTileLineIndices(buffer);
        }
    }

    protected FloatBuffer assembleTexCoords(int numLat, int numLon, FloatBuffer result, int stride) {

        float ds = 1f / (numLon > 1 ? numLon - 1 : 1);
        float dt = 1f / (numLat > 1 ? numLat - 1 : 1);
        float[] st = new float[2];
        int sIndex, tIndex, pos;

        // Iterate over the number of latitude and longitude vertices, computing the parameterized S and T coordinates
        // corresponding to each vertex.
        for (tIndex = 0, st[1] = 0; tIndex < numLat; tIndex++, st[1] += dt) {
            if (tIndex == numLat - 1) {
                st[1] = 1; // explicitly set the last T coordinate to 1 to ensure alignment
            }

            for (sIndex = 0, st[0] = 0; sIndex < numLon; sIndex++, st[0] += ds) {
                if (sIndex == numLon - 1) {
                    st[0] = 1; // explicitly set the last S coordinate to 1 to ensure alignment
                }

                pos = result.position();
                result.put(st, 0, 2);

                if (result.limit() >= pos + stride) {
                    result.position(pos + stride);
                }
            }
        }

        return result;
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
}
