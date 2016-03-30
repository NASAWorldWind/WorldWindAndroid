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
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class BasicTessellator implements Tessellator, TileFactory {

    // ~0.6 meter resolution
    protected LevelSet levelSet = new LevelSet(new Sector().setFullSphere(), 90, 20, 32, 32);

    protected double detailControl = 80;

    protected List<Tile> topLevelTiles = new ArrayList<>();

    protected BasicTerrain currentTerrain = new BasicTerrain();

    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(300); // capacity for 300 tiles

    public BasicTessellator() {
    }

    public LevelSet getLevelSet() {
        return this.levelSet;
    }

    public void setLevelSet(LevelSet levelSet) {
        if (levelSet == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTessellator", "setLevelSet", "missingLevelSet"));
        }

        this.levelSet = levelSet;
        this.invalidateTiles();
    }

    public double getDetailControl() {
        return detailControl;
    }

    public void setDetailControl(double detailControl) {
        this.detailControl = detailControl;
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
        Level firstLevel = this.levelSet.firstLevel();
        if (firstLevel != null) {
            Tile.assembleTilesForLevel(firstLevel, this, this.topLevelTiles);
        }
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

    protected void invalidateTiles() {
        this.topLevelTiles.clear();
        this.currentTerrain.clearTiles();
        this.tileCache.clear();
    }

    protected void assembleSharedBuffers() {
        int numLat = this.levelSet.tileHeight;
        int numLon = this.levelSet.tileWidth;

        if (this.currentTerrain.getTileTexCoords() == null) {
            FloatBuffer buffer = ByteBuffer.allocateDirect(numLat * numLon * 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.assembleTexCoords(numLat, numLon, buffer, 2).rewind();
            this.currentTerrain.setTileTexCoords(buffer);
        }

        // TODO put terrain tri-strip and line indices in a single buffer, use a Range (add it if needed) to identify the parts
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
