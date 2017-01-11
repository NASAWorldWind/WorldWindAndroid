/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

public class GpkgTileMatrix extends GpkgEntry {

    protected String tableName;

    protected int zoomLevel;

    protected int matrixWidth;

    protected int matrixHeight;

    protected int tileWidth;

    protected int tileHeight;

    protected double pixelXSize;

    protected double pixelYSize;

    public GpkgTileMatrix() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String name) {
        this.tableName = name;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int level) {
        this.zoomLevel = level;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public void setMatrixWidth(int width) {
        this.matrixWidth = width;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public void setMatrixHeight(int height) {
        this.matrixHeight = height;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int width) {
        this.tileWidth = width;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int height) {
        this.tileHeight = height;
    }

    public double getPixelXSize() {
        return pixelXSize;
    }

    public void setPixelXSize(double xSize) {
        this.pixelXSize = xSize;
    }

    public double getPixelYSize() {
        return pixelYSize;
    }

    public void setPixelYSize(double ySize) {
        this.pixelYSize = ySize;
    }
}
