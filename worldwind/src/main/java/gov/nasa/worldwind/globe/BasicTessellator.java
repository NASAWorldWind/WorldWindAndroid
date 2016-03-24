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

import gov.nasa.worldwind.cache.LruMemoryCache;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class BasicTessellator implements Tessellator, TileFactory {

    protected static final int DEFAULT_TOP_LEVEL_DELTA = 90; // 90 degrees

    protected static final int DEFAULT_NUM_LEVELS = 20; // ~0.6 meter resolution with default tile delta and tile width

    protected static final int DEFAULT_TILE_WIDTH = 32; // 32 x 32 vertices per tile

    protected static final int DEFAULT_DETAIL_CONTROL = 80;

    protected static final int DEFAULT_TILE_CACHE_CAPACITY = 300; // capacity for 300 tiles

    protected LevelSet levels;

    protected double detailControl = DEFAULT_DETAIL_CONTROL;

    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(DEFAULT_TILE_CACHE_CAPACITY);

    protected List<Tile> topLevelTiles = new ArrayList<>();

    protected BasicTerrain currentTerrain = new BasicTerrain();

    public BasicTessellator() {
        //noinspection SuspiciousNameCombination
        this.levels = new LevelSet(new Sector().setFullSphere(), DEFAULT_TOP_LEVEL_DELTA, DEFAULT_NUM_LEVELS,
            DEFAULT_TILE_WIDTH, DEFAULT_TILE_WIDTH);
    }

    public BasicTessellator(double topLevelDelta, int numLevels, int tileWidth, int tileHeight) {
        if (topLevelDelta <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidTileDelta"));
        }

        if (numLevels < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidNumLevels"));
        }

        if (tileWidth < 1 || tileHeight < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "constructor", "invalidWidthOrHeight"));
        }

        this.levels = new LevelSet(new Sector().setFullSphere(), topLevelDelta, numLevels, tileWidth, tileHeight);
    }

    @Override
    public Terrain tessellate(DrawContext dc) {
        this.assembleTiles(dc);
        this.assembleSharedBuffers();

        return this.currentTerrain;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        return new TerrainTile(sector, level, row, column);
    }

    protected void assembleTiles(DrawContext dc) {
        this.currentTerrain.clearTiles();

        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        for (Tile tile : this.topLevelTiles) {
            this.addTileOrDescendants(dc, (TerrainTile) tile);
        }
    }

    protected void createTopLevelTiles() {
        Tile.assembleTilesForLevel(this.levels.firstLevel(), this, this.topLevelTiles);
    }

    protected void addTileOrDescendants(DrawContext dc, TerrainTile tile) {
        if (!tile.intersectsFrustum(dc, dc.getFrustum())) {
            return; // ignore the tile and its descendants if it's not visible
        }

        if (tile.level.isLastLevel() || !tile.mustSubdivide(dc, this.detailControl)) {
            this.addTile(dc, tile);
            return; // use the tile if it does not need to be subdivided
        }

        for (Tile child : tile.subdivideToCache(this, this.tileCache, 4)) { // each tile has a cached size of 1
            this.addTileOrDescendants(dc, (TerrainTile) child); // recursively process the tile's children
        }
    }

    protected void addTile(DrawContext dc, TerrainTile tile) {
        if (tile.mustAssembleTileVertices(dc)) {
            tile.assembleTileVertices(dc); // build the tile's geometry when necessary
        }

        this.currentTerrain.addTile(tile);
    }

    protected void assembleSharedBuffers() {
        int numLat = this.levels.tileHeight;
        int numLon = this.levels.tileWidth;

        if (this.currentTerrain.getTileTexCoords() == null) {
            FloatBuffer buffer = ByteBuffer.allocateDirect(numLat * numLon * 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.assembleTexCoords(numLat, numLon, buffer, 2).rewind();
            this.currentTerrain.setTileTexCoords(buffer);
        }

        if (this.currentTerrain.getTileTriStripIndices() == null) {
            ShortBuffer buffer = this.assembleTriStripIndices(numLat, numLon);
            this.currentTerrain.setTileTriStripIndices(buffer);
        }

        if (this.currentTerrain.getTileLineIndices() == null) {
            ShortBuffer buffer = this.assembleLineIndices(numLat, numLon);
            this.currentTerrain.setTileLineIndices(buffer);
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
