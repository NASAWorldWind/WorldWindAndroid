/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.draw.BasicDrawableTerrain;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BufferObject;
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

    protected float[] levelSetVertexTexCoords;

    protected short[] levelSetLineElements;

    protected short[] levelSetTriStripElements;

    protected BufferObject[] levelSetBufferObjects = new BufferObject[3];

    protected String[] levelSetBufferKeys = {
        this.getClass().getName() + ".levelSetBuffer[0]",
        this.getClass().getName() + ".levelSetBuffer[1]",
        this.getClass().getName() + ".levelSetBuffer[2]"};

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
        this.currentTerrain.clear();
        this.assembleTiles(rc);
        rc.terrain = this.currentTerrain;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        return new TerrainTile(sector, level, row, column);
    }

    protected void assembleTiles(RenderContext rc) {
        // Assemble the terrain buffers and OpenGL buffer objects associated with the level set.
        this.assembleLevelSetBuffers(rc);
        this.currentTerrain.setTriStripElements(this.levelSetTriStripElements);

        // Assemble the tessellator's top level terrain tiles, which we keep permanent references to.
        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        // Subdivide the top level tiles until the desired resolution is achieved in each part of the scene.
        for (int idx = 0, len = this.topLevelTiles.size(); idx < len; idx++) {
            this.addTileOrDescendants(rc, (TerrainTile) this.topLevelTiles.get(idx));
        }

        // Release references to render resources acquired while assembling tiles.
        Arrays.fill(this.levelSetBufferObjects, null);
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
        // Assemble the terrain tile's vertex points when necessary.
        if (this.mustAssembleTilePoints(rc, tile)) {
            this.assembleTilePoints(rc, tile);
        }

        // Add the terrain tile to the currently active terrain.
        this.currentTerrain.addTile(tile);

        // Prepare a drawable for the terrain tile for processing on the OpenGL thread.
        Pool<BasicDrawableTerrain> pool = rc.getDrawablePool(BasicDrawableTerrain.class);
        BasicDrawableTerrain drawable = BasicDrawableTerrain.obtain(pool);
        this.prepareDrawableTerrain(rc, tile, drawable);
        rc.offerDrawableTerrain(drawable);
    }

    protected void invalidateTiles() {
        this.topLevelTiles.clear();
        this.currentTerrain.clear();
        this.tileCache.clear();
        this.levelSetVertexTexCoords = null;
        this.levelSetLineElements = null;
        this.levelSetTriStripElements = null;
    }

    protected void prepareDrawableTerrain(RenderContext rc, TerrainTile tile, BasicDrawableTerrain drawable) {
        // Assemble the drawable's geographic sector and Cartesian vertex origin.
        drawable.sector.set(tile.sector);
        drawable.vertexOrigin.set(tile.vertexOrigin);

        // Assemble the drawable's OpenGL buffer objects.
        drawable.vertexPoints = tile.getVertexPointBuffer(rc);
        drawable.vertexTexCoords = this.levelSetBufferObjects[0];
        drawable.lineElements = this.levelSetBufferObjects[1];
        drawable.triStripElements = this.levelSetBufferObjects[2];
    }

    public boolean mustAssembleTilePoints(RenderContext rc, TerrainTile tile) {
        return tile.getVertexPoints() == null;
    }

    protected void assembleTilePoints(RenderContext rc, TerrainTile tile) {
        int numLat = tile.level.tileWidth;
        int numLon = tile.level.tileHeight;

        Vec3 origin = tile.getVertexOrigin();
        if (origin == null) {
            origin = new Vec3();
        }

        float[] points = tile.getVertexPoints();
        if (points == null) {
            points = new float[numLat * numLon * 3];
        }

        Globe globe = rc.globe;
        globe.geographicToCartesian(tile.sector.centroidLatitude(), tile.sector.centroidLongitude(), 0, origin);
        globe.geographicToCartesianGrid(tile.sector, numLat, numLon, null, origin, points, 3, 0);
        tile.setVertexOrigin(origin);
        tile.setVertexPoints(points);
    }

    protected void assembleLevelSetBuffers(RenderContext rc) {
        int numLat = this.levelSet.tileHeight;
        int numLon = this.levelSet.tileWidth;

        // Assemble the level set's vertex tex coords.
        if (this.levelSetVertexTexCoords == null) {
            this.levelSetVertexTexCoords = new float[numLat * numLon * 2];
            this.assembleVertexTexCoords(numLat, numLon, this.levelSetVertexTexCoords, 2, 0);
        }

        // Assemble the level set's line elements.
        if (this.levelSetLineElements == null) {
            this.levelSetLineElements = this.assembleLineElements(numLat, numLon);
        }

        // Assemble the level set's triangle strip elements.
        if (this.levelSetTriStripElements == null) {
            this.levelSetTriStripElements = this.assembleTriStripElements(numLat, numLon);
        }

        // Retrieve or create the level set's OpenGL vertex tex coord buffer object.
        this.levelSetBufferObjects[0] = rc.getBufferObject(this.levelSetBufferKeys[0]);
        if (this.levelSetBufferObjects[0] == null) {
            int size = this.levelSetVertexTexCoords.length * 4;
            FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(this.levelSetVertexTexCoords).rewind();
            this.levelSetBufferObjects[0] = rc.putBufferObject(this.levelSetBufferKeys[0],
                new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer));
        }

        // Retrieve or create the level set's OpenGL line element buffer object.
        // TODO put line and tri-strip elements in a single buffer, use a range to identify the parts
        this.levelSetBufferObjects[1] = rc.getBufferObject(this.levelSetBufferKeys[1]);
        if (this.levelSetBufferObjects[1] == null) {
            int size = this.levelSetLineElements.length * 2;
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.levelSetLineElements).rewind();
            this.levelSetBufferObjects[1] = rc.putBufferObject(this.levelSetBufferKeys[1],
                new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer));
        }

        // Retrieve or create the level set's OpenGL tri-strip element buffer object.
        this.levelSetBufferObjects[2] = rc.getBufferObject(this.levelSetBufferKeys[2]);
        if (this.levelSetBufferObjects[2] == null) {
            int size = this.levelSetTriStripElements.length * 2;
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.levelSetTriStripElements).rewind();
            this.levelSetBufferObjects[2] = rc.putBufferObject(this.levelSetBufferKeys[2],
                new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer));
        }
    }

    protected float[] assembleVertexTexCoords(int numLat, int numLon, float[] result, int stride, int pos) {
        float ds = 1f / (numLon > 1 ? numLon - 1 : 1);
        float dt = 1f / (numLat > 1 ? numLat - 1 : 1);
        float[] st = new float[2];
        int sIndex, tIndex;

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

                result[pos] = st[0];
                result[pos + 1] = st[1];
                pos += stride;
            }
        }

        return result;
    }

    protected short[] assembleLineElements(int numLat, int numLon) {
        // Allocate a buffer to hold the indices.
        int count = (numLat * (numLon - 1) + numLon * (numLat - 1)) * 2;
        short[] result = new short[count];
        int pos = 0, vertex;

        // Add a line between each row to define the horizontal cell outlines.
        for (int latIndex = 0; latIndex < numLat; latIndex++) {
            for (int lonIndex = 0; lonIndex < numLon - 1; lonIndex++) {
                vertex = lonIndex + latIndex * numLon;
                result[pos++] = (short) vertex;
                result[pos++] = (short) (vertex + 1);
            }
        }

        // Add a line between each column to define the vertical cell outlines.
        for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
            for (int latIndex = 0; latIndex < numLat - 1; latIndex++) {
                vertex = lonIndex + latIndex * numLon;
                result[pos++] = (short) vertex;
                result[pos++] = (short) (vertex + numLon);
            }
        }

        return result;
    }

    protected short[] assembleTriStripElements(int numLat, int numLon) {
        // Allocate a buffer to hold the indices.
        int count = ((numLat - 1) * numLon + (numLat - 2)) * 2;
        short[] result = new short[count];
        int pos = 0, vertex = 0;

        for (int latIndex = 0; latIndex < numLat - 1; latIndex++) {
            // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
            for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
                vertex = lonIndex + latIndex * numLon;
                result[pos++] = (short) (vertex + numLon);
                result[pos++] = (short) vertex;
            }

            // Insert indices to create 2 degenerate triangles:
            // - one for the end of the current row, and
            // - one for the beginning of the next row
            if (latIndex < numLat - 2) {
                result[pos++] = (short) vertex;
                result[pos++] = (short) ((latIndex + 2) * numLon);
            }
        }

        return result;
    }
}
