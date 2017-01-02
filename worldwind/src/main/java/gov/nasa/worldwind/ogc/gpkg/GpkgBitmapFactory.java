/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

public class GpkgBitmapFactory implements ImageSource.BitmapFactory {

    protected GpkgContent tiles;

    protected int zoomLevel;

    protected int tileColumn;

    protected int tileRow;

    public GpkgBitmapFactory(GpkgContent tiles, int zoomLevel, int tileColumn, int tileRow) {
        if (tiles == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpkgBitmapFactory", "constructor", "missingTiles"));
        }

        this.tiles = tiles;
        this.zoomLevel = zoomLevel;
        this.tileColumn = tileColumn;
        this.tileRow = tileRow;
    }

    @Override
    public Bitmap createBitmap() {
        // Attempt to read the GeoPackage tile user data, throwing an exception if it cannot be found.
        GeoPackage geoPackage = this.tiles.getContainer();
        GpkgTileUserData tileUserData = geoPackage.readTileUserData(this.tiles, this.zoomLevel, this.tileColumn, this.tileRow);

        // Log a message if the tile user data cannot be found, and return a null bitmap indicating this tile is empty.
        if (tileUserData == null) {
            Logger.logMessage(Logger.WARN, "GpkgBitmapFactory", "createBitmap",
                "The GeoPackage tile cannot be found (zoomLevel=" + this.zoomLevel + ", tileColumn=" + this.tileColumn + ", tileRow=" + this.tileRow + ")");
            return null;
        }

        // Decode the tile user data, either a PNG image or a JPEG image.
        byte[] data = tileUserData.getTileData();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
