/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

import android.util.SparseArray;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class GpkgTileFactory implements TileFactory {

    protected GpkgContent tiles;

    public GpkgTileFactory(GpkgContent tiles) {
        if (tiles == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "constructor", "missingTiles"));
        }

        this.tiles = tiles;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "missingSector"));
        }

        if (level == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgTileFactory", "createTile", "missingLevel"));
        }

        ImageTile tile = new ImageTile(sector, level, row, column);

        GeoPackage geoPackage = this.tiles.getContainer();
        String tableName = this.tiles.getTableName();
        SparseArray<GpkgTileMatrix> tileMatrixByZoomLevel = geoPackage.getTileMatrix(tableName);
        GpkgTileUserMetrics tileUserMetrics = geoPackage.getTileUserMetrics(tableName);

        // Attempt to find the GeoPackage tile matrix associated with the WorldWind level.
        int zoomLevel = level.levelNumber + tileMatrixByZoomLevel.keyAt(0);
        GpkgTileMatrix tileMatrix = tileMatrixByZoomLevel.get(zoomLevel);

        // Check if content table has any tiles on this zoom level.
        if (tileMatrix != null && tileUserMetrics.hasZoomLevel(zoomLevel)) {
            // Convert the WorldWind tile row to the equivalent GeoPackage tile row.
            int gpkgRow = level.levelHeight / level.tileHeight - row - 1;
            if (column < tileMatrix.getMatrixWidth() && gpkgRow < tileMatrix.getMatrixHeight()) {
                // Configure the tile with a bitmap factory that reads directly from the GeoPackage.
                ImageSource.BitmapFactory bitmapFactory = new GpkgBitmapFactory(this.tiles, zoomLevel, column, gpkgRow);
                tile.setImageSource(ImageSource.fromBitmapFactory(bitmapFactory));
            }
        }

        return tile;
    }
}
