/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class GeoPackage {

    protected SQLiteConnection connection;

    protected List<GpkgSpatialReferenceSystem> spatialReferenceSystems = new ArrayList<>();

    protected List<GpkgContents> contents = new ArrayList<>();

    protected List<GpkgTileMatrixSet> tileMatrixSets = new ArrayList<>();

    protected List<GpkgTileMatrix> tileMatrices = new ArrayList<>();

    protected SparseIntArray srsIdIndex = new SparseIntArray();

    protected Map<String, Integer> tileMatrixSetIndex = new HashMap<>();

    protected Map<String, SparseArray<GpkgTileMatrix>> tileMatrixIndex = new HashMap<>();

    public GeoPackage(String pathName) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GeoPackage", "constructor", "missingPathName"));
        }

        this.connection = new SQLiteConnection(pathName, SQLiteDatabase.OPEN_READONLY, 60, TimeUnit.SECONDS);
        // TODO verify its a GeoPackage container

        // TODO select specific columns
        // TODO parameterize table names and column names as constants
        this.readSpatialReferenceSystems();
        this.readContents();
        this.readTileMatrixSets();
        this.readTileMatrices();
    }

    public List<GpkgSpatialReferenceSystem> getSpatialReferenceSystems() {
        return this.spatialReferenceSystems;
    }

    public GpkgSpatialReferenceSystem getSpatialReferenceSystem(int id) {
        int index = this.srsIdIndex.get(id, -1); // -1 if not found; the default is 0, a valid index
        return (index < 0) ? null : this.spatialReferenceSystems.get(index);
    }

    public List<GpkgContents> getContents() {
        return this.contents;
    }

    public List<GpkgTileMatrixSet> getTileMatrixSets() {
        return this.tileMatrixSets;
    }

    public GpkgTileMatrixSet getTileMatrixSet(String tableName) {
        Integer index = this.tileMatrixSetIndex.get(tableName);
        return (index == null) ? null : this.tileMatrixSets.get(index);
    }

    public List<GpkgTileMatrix> getTileMatrices() {
        return this.tileMatrices;
    }

    public SparseArray<GpkgTileMatrix> getTileMatrices(String tableName) {
        return this.tileMatrixIndex.get(tableName);
    }

    public GpkgTileUserData getTileUserData(GpkgContents tiles, int zoomLevel, int tileColumn, int tileRow) {
        return (tiles == null) ? null : this.readTileUserData(tiles.getTableName(), zoomLevel, tileColumn, tileRow);
    }

    protected void readSpatialReferenceSystems() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM gpkg_spatial_ref_sys", null /*selectionArgs*/);

            int srs_name = cursor.getColumnIndex("srs_name");
            int srs_id = cursor.getColumnIndex("srs_id");
            int organization = cursor.getColumnIndex("organization");
            int organization_coordsys_id = cursor.getColumnIndex("organization_coordsys_id");
            int definition = cursor.getColumnIndex("definition");
            int description = cursor.getColumnIndex("description");

            while (cursor.moveToNext()) {
                GpkgSpatialReferenceSystem srs = new GpkgSpatialReferenceSystem();
                srs.setContainer(this);
                srs.setSrsName(cursor.getString(srs_name));
                srs.setSrsId(cursor.getInt(srs_id));
                srs.setOrganization(cursor.getString(organization));
                srs.setOrganizationCoordSysId(cursor.getInt(organization_coordsys_id));
                srs.setDefinition(cursor.getString(definition));
                srs.setDescription(cursor.getString(description));

                int index = this.spatialReferenceSystems.size();
                this.spatialReferenceSystems.add(srs);
                this.srsIdIndex.put(srs.getSrsId(), index);
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readContents() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM gpkg_contents", null /*selectionArgs*/);

            int table_name = cursor.getColumnIndex("table_name");
            int data_type = cursor.getColumnIndex("data_type");
            int identifier = cursor.getColumnIndex("identifier");
            int description = cursor.getColumnIndex("description");
            int last_change = cursor.getColumnIndex("last_change");
            int min_x = cursor.getColumnIndex("min_x");
            int min_y = cursor.getColumnIndex("min_y");
            int max_x = cursor.getColumnIndex("max_x");
            int max_y = cursor.getColumnIndex("max_y");
            int srs_id = cursor.getColumnIndex("srs_id");

            while (cursor.moveToNext()) {
                GpkgContents contents = new GpkgContents();
                contents.setContainer(this);
                contents.setTableName(cursor.getString(table_name));
                contents.setDataType(cursor.getString(data_type));
                contents.setIdentifier(cursor.getString(identifier));
                contents.setDescription(cursor.getString(description));
                contents.setLastChange(cursor.getString(last_change));
                contents.setMinX(cursor.getDouble(min_x));
                contents.setMinY(cursor.getDouble(min_y));
                contents.setMaxX(cursor.getDouble(max_x));
                contents.setMaxY(cursor.getDouble(max_y));
                contents.setSrsId(cursor.getInt(srs_id));

                this.contents.add(contents);
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readTileMatrixSets() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM gpkg_tile_matrix_set", null /*selectionArgs*/);

            int table_name = cursor.getColumnIndex("table_name");
            int srs_id = cursor.getColumnIndex("srs_id");
            int min_x = cursor.getColumnIndex("min_x");
            int min_y = cursor.getColumnIndex("min_y");
            int max_x = cursor.getColumnIndex("max_x");
            int max_y = cursor.getColumnIndex("max_y");

            while (cursor.moveToNext()) {
                GpkgTileMatrixSet tileMatrixSet = new GpkgTileMatrixSet();
                tileMatrixSet.setContainer(this);
                tileMatrixSet.setTableName(cursor.getString(table_name));
                tileMatrixSet.setSrsId(cursor.getInt(srs_id));
                tileMatrixSet.setMinX(cursor.getDouble(min_x));
                tileMatrixSet.setMinY(cursor.getDouble(min_y));
                tileMatrixSet.setMaxX(cursor.getDouble(max_x));
                tileMatrixSet.setMaxY(cursor.getDouble(max_y));

                int index = this.tileMatrixSets.size();
                this.tileMatrixSets.add(tileMatrixSet);
                this.tileMatrixSetIndex.put(tileMatrixSet.getTableName(), index);
                this.tileMatrixIndex.put(tileMatrixSet.getTableName(), new SparseArray<GpkgTileMatrix>());
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readTileMatrices() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM gpkg_tile_matrix", null /*selectionArgs*/);

            int table_name = cursor.getColumnIndex("table_name");
            int zoom_level = cursor.getColumnIndex("zoom_level");
            int matrix_width = cursor.getColumnIndex("matrix_width");
            int matrix_height = cursor.getColumnIndex("matrix_height");
            int tile_width = cursor.getColumnIndex("tile_width");
            int tile_height = cursor.getColumnIndex("tile_height");
            int pixel_x_size = cursor.getColumnIndex("pixel_x_size");
            int pixel_y_size = cursor.getColumnIndex("pixel_y_size");

            while (cursor.moveToNext()) {
                GpkgTileMatrix tileMatrix = new GpkgTileMatrix();
                tileMatrix.setContainer(this);
                tileMatrix.setTableName(cursor.getString(table_name));
                tileMatrix.setZoomLevel(cursor.getInt(zoom_level));
                tileMatrix.setMatrixWidth(cursor.getInt(matrix_width));
                tileMatrix.setMatrixHeight(cursor.getInt(matrix_height));
                tileMatrix.setTileWidth(cursor.getInt(tile_width));
                tileMatrix.setTileHeight(cursor.getInt(tile_height));
                tileMatrix.setPixelXSize(cursor.getDouble(pixel_x_size));
                tileMatrix.setPixelYSize(cursor.getDouble(pixel_y_size));

                this.tileMatrices.add(tileMatrix);
                this.tileMatrixIndex.get(tileMatrix.getTableName()).put(tileMatrix.getZoomLevel(), tileMatrix);
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected GpkgTileUserData readTileUserData(String tableName, int zoomLevel, int tileColumn, int tileRow) {
        // TODO SQLiteDatabase is ambiguous on whether the call to rawQuery and Cursor usage are thread safe
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{Integer.toString(zoomLevel), Integer.toString(tileColumn), Integer.toString(tileRow)};
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE zoom_level=? AND tile_column=? AND tile_row=? LIMIT 1", selectionArgs);

            int id = cursor.getColumnIndex("id");
            int zoom_level = cursor.getColumnIndex("zoom_level");
            int tile_column = cursor.getColumnIndex("tile_column");
            int tile_row = cursor.getColumnIndex("tile_row");
            int tile_data = cursor.getColumnIndex("tile_data");

            if (cursor.moveToNext()) {
                GpkgTileUserData userData = new GpkgTileUserData();
                userData.setContainer(this);
                userData.setId(cursor.getInt(id));
                userData.setZoomLevel(cursor.getInt(zoom_level));
                userData.setTileColumn(cursor.getInt(tile_column));
                userData.setTileRow(cursor.getInt(tile_row));
                userData.setTileData(cursor.getBlob(tile_data));
                return userData;
            } else {
                return null;
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }
}
