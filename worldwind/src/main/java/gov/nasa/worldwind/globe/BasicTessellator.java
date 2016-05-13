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

import gov.nasa.worldwind.draw.BasicDrawableTerrain;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class BasicTessellator implements Tessellator, TileFactory {

    // ~0.6 meter resolution
    protected LevelSet levelSet = new LevelSet(new Sector().setFullSphere(), 90, 20, 32, 32);

    protected double detailControl = 80;

    protected List<Tile> topLevelTiles = new ArrayList<>();

    protected BasicTerrain currentTerrain = new BasicTerrain();

    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(300); // capacity for 300 tiles

    protected FloatBuffer tileVertexTexCoords;

    protected ShortBuffer tileLineElements;

    protected ShortBuffer tileTriStripElements;

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
    public void tessellate(RenderContext rc) {
        this.assembleTiles(rc);
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        return new TerrainTile(sector, level, row, column);
    }

    protected void assembleTiles(RenderContext rc) {
        this.currentTerrain.clearTiles();

        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        for (int idx = 0, len = this.topLevelTiles.size(); idx < len; idx++) {
            this.addTileOrDescendants(rc, (TerrainTile) this.topLevelTiles.get(idx));
        }

        rc.terrain = this.currentTerrain;
    }

    protected void createTopLevelTiles() {
        Level firstLevel = this.levelSet.firstLevel();
        if (firstLevel != null) {
            Tile.assembleTilesForLevel(firstLevel, this, this.topLevelTiles);
        }
    }

    protected void addTileOrDescendants(RenderContext rc, TerrainTile tile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(rc, rc.frustum)) {
            return; // ignore the tile and its descendants if it's not needed or not visible
        }

        if (tile.level.isLastLevel() || !tile.mustSubdivide(rc, this.detailControl)) {
            this.addTile(rc, tile);
            return; // use the tile if it does not need to be subdivided
        }

        for (Tile child : tile.subdivideToCache(this, this.tileCache, 4)) { // each tile has a cached size of 1
            this.addTileOrDescendants(rc, (TerrainTile) child); // recursively process the tile's children
        }
    }

    protected void addTile(RenderContext rc, TerrainTile tile) {
        int numLat = this.levelSet.tileHeight;
        int numLon = this.levelSet.tileWidth;

        // Assemble the tile's vertex points when necessary.
        if (this.mustAssembleVertexPoints(rc, tile)) {
            this.assembleVertexPoints(rc, tile);
        }

        // Assemble the shared vertex tex coord buffer.
        if (this.tileVertexTexCoords == null) {
            this.tileVertexTexCoords = ByteBuffer.allocateDirect(numLat * numLon * 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.assembleVertexTexCoords(numLat, numLon, this.tileVertexTexCoords, 2).rewind();
        }

        // Assemble the shared line element buffer.
        // TODO put line and tri-strip elements in a single buffer, use a range to identify the parts
        if (this.tileLineElements == null) {
            this.tileLineElements = this.assembleLineElements(numLat, numLon);
        }

        // Assemble the shared triangle strip element buffer.
        if (this.tileTriStripElements == null) {
            this.tileTriStripElements = this.assembleTriStripElements(numLat, numLon);
        }

        // Add the terrain tile to the currently active terrain.
        this.currentTerrain.addTile(tile);

        // Prepare a drawable for the terrain tile for processing on the OpenGL thread.
        // TODO set up the drawable with the terrain tile's vertex attributes and dimensions (for elements)
        // TODO thread safety can be ensured by performing a copy here; could multiple copies be reduced by
        // TODO storing them in a BufferObject?
        Pool<BasicDrawableTerrain> pool = rc.getDrawablePool(BasicDrawableTerrain.class);
        BasicDrawableTerrain drawable = BasicDrawableTerrain.obtain(pool);
        drawable.sector.set(tile.sector);
        drawable.vertexOrigin.set(tile.vertexOrigin);
        drawable.vertexPoints = tile.vertexPoints;
        drawable.vertexTexCoords = this.tileVertexTexCoords;
        drawable.triStripElements = this.tileTriStripElements;
        drawable.lineElements = this.tileLineElements;
        rc.offerDrawableTerrain(drawable);
    }

    protected void invalidateTiles() {
        this.topLevelTiles.clear();
        this.currentTerrain.clearTiles();
        this.tileCache.clear();
        this.tileVertexTexCoords = null;
        this.tileLineElements = null;
        this.tileTriStripElements = null;
    }

    public boolean mustAssembleVertexPoints(RenderContext rc, TerrainTile tile) {
        return tile.getVertexPoints() == null;
    }

    protected void assembleVertexPoints(RenderContext rc, TerrainTile tile) {
        int numLat = tile.level.tileWidth;
        int numLon = tile.level.tileHeight;

        Vec3 origin = tile.getVertexOrigin();
        if (origin == null) {
            origin = new Vec3();
        }

        FloatBuffer buffer = tile.getVertexPoints();
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(numLat * numLon * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        Globe globe = rc.globe;
        globe.geographicToCartesian(tile.sector.centroidLatitude(), tile.sector.centroidLongitude(), 0, origin);
        globe.geographicToCartesianGrid(tile.sector, numLat, numLon, null, origin, buffer, 3).rewind();
        tile.setVertexOrigin(origin);
        tile.setVertexPoints(buffer);
    }

    protected FloatBuffer assembleVertexTexCoords(int numLat, int numLon, FloatBuffer result, int stride) {

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

    protected ShortBuffer assembleLineElements(int numLat, int numLon) {

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

    protected ShortBuffer assembleTriStripElements(int numLat, int numLon) {

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
}
