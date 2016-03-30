/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import android.opengl.GLES20;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logger;

public class BasicTerrain implements Terrain {

    protected List<TerrainTile> tiles = new ArrayList<>();

    protected Sector sector = new Sector();

    protected FloatBuffer tileTexCoords;

    protected ShortBuffer tileTriStripIndices;

    protected ShortBuffer tileLineIndices;

    public BasicTerrain() {
    }

    public void addTile(TerrainTile tile) {
        if (tile == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "addTile", "missingTile"));
        }

        this.tiles.add(tile);
        this.sector.union(tile.sector);
    }

    public void clearTiles() {
        this.tiles.clear();
        this.sector.setEmpty();
    }

    public FloatBuffer getTileTexCoords() {
        return tileTexCoords;
    }

    public void setTileTexCoords(FloatBuffer buffer) {
        this.tileTexCoords = buffer;
    }

    public ShortBuffer getTileTriStripIndices() {
        return tileTriStripIndices;
    }

    public void setTileTriStripIndices(ShortBuffer buffer) {
        this.tileTriStripIndices = buffer;
    }

    public ShortBuffer getTileLineIndices() {
        return tileLineIndices;
    }

    public void setTileLineIndices(ShortBuffer buffer) {
        this.tileLineIndices = buffer;
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public int getTileCount() {
        return this.tiles.size();
    }

    @Override
    public Sector getTileSector(int index) {
        if (index < 0 || index >= this.tiles.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "getTileSector", "invalidIndex"));
        }

        return this.tiles.get(index).sector;
    }

    @Override
    public Vec3 getTileVertexOrigin(int index) {
        if (index < 0 || index >= this.tiles.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "getTileOrigin", "invalidIndex"));
        }

        return this.tiles.get(index).tileOrigin;
    }


    @Override
    public void applyTexCoordTransform(int index, Sector dst, Matrix3 result) {
        if (index < 0 || index >= this.tiles.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "applyTexCoordTransform", "invalidIndex"));
        }

        if (dst == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "applyTexCoordTransform", "missingSector"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "applyTexCoordTransform", "missingResult"));
        }

        Sector src = this.tiles.get(index).sector;
        result.multiplyByTileTransform(src, dst);
    }

    @Override
    public void useVertexPointAttrib(DrawContext dc, int index, int attribLocation) {
        if (index < 0 || index >= this.tiles.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "useTileVertexPointAttrib", "invalidIndex"));
        }

        // TODO use GL vertex buffer objects here and throughout the GL commands specifying vertex attribs and elements
        Buffer buffer = this.tiles.get(index).tileVertices;
        if (buffer != null) {
            GLES20.glVertexAttribPointer(attribLocation, 3, GLES20.GL_FLOAT, false, 0, buffer);
        }
    }

    @Override
    public void useVertexTexCoordAttrib(DrawContext dc, int attribLocation) {
        Buffer buffer = this.tileTexCoords;
        if (buffer != null) {
            GLES20.glVertexAttribPointer(attribLocation, 2, GLES20.GL_FLOAT, false, 0, buffer);
        }
    }

    @Override
    public void drawTileTriangles(DrawContext dc, int index) {
        if (index < 0 || index >= this.tiles.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "drawTileTriangles", "invalidIndex"));
        }

        Buffer buffer = this.tileTriStripIndices;
        if (buffer != null) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, buffer.remaining(), GLES20.GL_UNSIGNED_SHORT, buffer);
        }
    }

    @Override
    public void drawTileLines(DrawContext dc, int index) {
        if (index < 0 || index >= this.tiles.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "drawTileTriangles", "invalidIndex"));
        }

        Buffer buffer = this.tileLineIndices;
        if (buffer != null) {
            GLES20.glDrawElements(GLES20.GL_LINES, buffer.remaining(), GLES20.GL_UNSIGNED_SHORT, buffer);
        }
    }

    @Override
    public Vec3 geographicToCartesian(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "geographicToCartesian", "missingResult"));
        }

        return null; // TODO
    }
}
