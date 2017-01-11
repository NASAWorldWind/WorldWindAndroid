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

    protected List<GpkgSpatialReferenceSystem> spatialReferenceSystem = new ArrayList<>();

    protected List<GpkgContent> content = new ArrayList<>();

    protected List<GpkgTileMatrixSet> tileMatrixSet = new ArrayList<>();

    protected List<GpkgTileMatrix> tileMatrix = new ArrayList<>();

    protected List<GpkgTileUserMetrics> tileUserMetrics = new ArrayList<>();

    protected SparseIntArray srsIdIndex = new SparseIntArray();

    protected Map<String, Integer> tileMatrixSetIndex = new HashMap<>();

    protected Map<String, SparseArray<GpkgTileMatrix>> tileMatrixIndex = new HashMap<>();

    protected Map<String, GpkgTileUserMetrics> tileUserMetricsIndex = new HashMap<>();

    public GeoPackage(String pathName) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GeoPackage", "constructor", "missingPathName"));
        }

        // TODO verify its a GeoPackage container
        // TODO select specific columns
        // TODO parameterize table names and column names as constants
        this.connection = new SQLiteConnection(pathName, SQLiteDatabase.OPEN_READONLY, 60, TimeUnit.SECONDS);
        this.readSpatialReferenceSystem();
        this.readContent();
        this.readTileMatrixSet();
        this.readTileMatrix();
        this.readTileUserMetrics();
    }

    public List<GpkgSpatialReferenceSystem> getSpatialReferenceSystem() {
        return this.spatialReferenceSystem;
    }

    public GpkgSpatialReferenceSystem getSpatialReferenceSystem(int id) {
        int index = this.srsIdIndex.get(id, -1); // -1 if not found; the default is 0, a valid index
        return (index < 0) ? null : this.spatialReferenceSystem.get(index);
    }

    public List<GpkgContent> getContent() {
        return this.content;
    }

    public List<GpkgTileMatrixSet> getTileMatrixSet() {
        return this.tileMatrixSet;
    }

    public GpkgTileMatrixSet getTileMatrixSet(String tableName) {
        Integer index = this.tileMatrixSetIndex.get(tableName);
        return (index == null) ? null : this.tileMatrixSet.get(index);
    }

    public List<GpkgTileMatrix> getTileMatrix() {
        return this.tileMatrix;
    }

    public SparseArray<GpkgTileMatrix> getTileMatrix(String tableName) {
        return this.tileMatrixIndex.get(tableName);
    }

    public List<GpkgTileUserMetrics> getTileUserMetrics() {
        return this.tileUserMetrics;
    }

    public GpkgTileUserMetrics getTileUserMetrics(String tableName) {
        return this.tileUserMetricsIndex.get(tableName);
    }

    public GpkgTileUserData readTileUserData(GpkgContent tiles, int zoomLevel, int tileColumn, int tileRow) {
        return (tiles == null) ? null : this.readTileUserData(tiles.getTableName(), zoomLevel, tileColumn, tileRow);
    }

    protected void readSpatialReferenceSystem() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM 'gpkg_spatial_ref_sys'", null /*selectionArgs*/);

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
                int index = this.spatialReferenceSystem.size();
                this.spatialReferenceSystem.add(srs);
                this.srsIdIndex.put(srs.getSrsId(), index);
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readContent() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM 'gpkg_contents'", null /*selectionArgs*/);

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
                GpkgContent content = new GpkgContent();
                content.setContainer(this);
                content.setTableName(cursor.getString(table_name));
                content.setDataType(cursor.getString(data_type));
                content.setIdentifier(cursor.getString(identifier));
                content.setDescription(cursor.getString(description));
                content.setLastChange(cursor.getString(last_change));
                content.setMinX(cursor.getDouble(min_x));
                content.setMinY(cursor.getDouble(min_y));
                content.setMaxX(cursor.getDouble(max_x));
                content.setMaxY(cursor.getDouble(max_y));
                content.setSrsId(cursor.getInt(srs_id));
                this.content.add(content);
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readTileMatrixSet() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM 'gpkg_tile_matrix_set'", null /*selectionArgs*/);

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
                int index = this.tileMatrixSet.size();
                this.tileMatrixSet.add(tileMatrixSet);
                this.tileMatrixSetIndex.put(tileMatrixSet.getTableName(), index);
                this.tileMatrixIndex.put(tileMatrixSet.getTableName(), new SparseArray<GpkgTileMatrix>());
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readTileMatrix() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM 'gpkg_tile_matrix'", null /*selectionArgs*/);

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
                this.tileMatrix.add(tileMatrix);
                this.tileMatrixIndex.get(tileMatrix.getTableName()).put(tileMatrix.getZoomLevel(), tileMatrix);
            }
        } finally {
            WWUtil.closeSilently(cursor);
            WWUtil.closeSilently(database);
        }
    }

    protected void readTileUserMetrics() {
        for (int idx = 0, len = this.content.size(); idx < len; idx++) {
            GpkgContent content = this.content.get(idx);

            if (content.getTableName() == null) {
                continue;
            }

            if (content.getDataType() == null || !content.getDataType().equalsIgnoreCase("tiles")) {
                continue;
            }

            SQLiteDatabase database = null;
            Cursor cursor = null;
            try {
                database = this.connection.openDatabase();
                cursor = database.rawQuery("SELECT DISTINCT zoom_level FROM '" + content.getTableName() + "' ORDER BY zoom_level ASC", null /*selectionArgs*/);

                int zoom_level = cursor.getColumnIndex("zoom_level");
                int[] zoomLevels = new int[cursor.getCount()];

                for (int pos = 0; cursor.moveToNext(); pos++) {
                    zoomLevels[pos] = cursor.getInt(zoom_level);
                }

                GpkgTileUserMetrics userMetrics = new GpkgTileUserMetrics();
                userMetrics.setContainer(this);
                userMetrics.setZoomLevels(zoomLevels);
                this.tileUserMetrics.add(userMetrics);
                this.tileUserMetricsIndex.put(content.getTableName(), userMetrics);
            } finally {
                WWUtil.closeSilently(cursor);
                WWUtil.closeSilently(database);
            }
        }
    }

    protected GpkgTileUserData readTileUserData(String tableName, int zoomLevel, int tileColumn, int tileRow) {
        // TODO SQLiteDatabase is ambiguous on whether the call to rawQuery and Cursor usage are thread safe
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{Integer.toString(zoomLevel), Integer.toString(tileColumn), Integer.toString(tileRow)};
            database = this.connection.openDatabase();
            cursor = database.rawQuery("SELECT * FROM '" + tableName + "' WHERE zoom_level=? AND tile_column=? AND tile_row=? LIMIT 1", selectionArgs);

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
