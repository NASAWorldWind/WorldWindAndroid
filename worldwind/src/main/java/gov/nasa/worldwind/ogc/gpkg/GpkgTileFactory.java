/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

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

        String tableName = this.tiles.getTableName();
        int zoomLevel = level.levelNumber;

        // Attempt to find the GeoPackage tile matrix associated with the World Wind level. Assumes that the World Wind
        // levels match the GeoPackage tile matrix zoom levels. If there's no match then the GeoPackage contains no
        // tiles for this level and this tile has no image source.
        GeoPackage geoPackage = this.tiles.getContainer();
        GpkgTileMatrix tileMatrix = geoPackage.getTileMatrix(tableName).get(zoomLevel);
        GpkgTileUserMetrics tileUserMetrics = geoPackage.getTileUserMetrics(tableName);

        if (tileMatrix != null && tileUserMetrics.hasZoomLevel(zoomLevel)) {
            // Convert the World Wind tile address to the equivalent GeoPackage tile address. Assumes that the World
            // Wind level set matchs the GeoPackage tile matrix set, with the exception of tile rows which are inverted.
            int gpkgRow = tileMatrix.getMatrixHeight() - row - 1;
            // Configure the tile with a bitmap factory that reads directly from the GeoPackage.
            ImageSource.BitmapFactory bitmapFactory = new GpkgBitmapFactory(this.tiles, zoomLevel, column, gpkgRow);
            tile.setImageSource(ImageSource.fromBitmapFactory(bitmapFactory));
        }

        return tile;
    }
}
