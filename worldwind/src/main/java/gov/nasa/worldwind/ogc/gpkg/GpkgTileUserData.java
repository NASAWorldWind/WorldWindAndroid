/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

public class GpkgTileUserData extends GpkgEntry {

    protected int id;

    protected int zoomLevel;

    protected int tileColumn;

    protected int tileRow;

    protected byte[] tileData;

    public GpkgTileUserData() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(int leve) {
        this.zoomLevel = leve;
    }

    public int getTileColumn() {
        return tileColumn;
    }

    public void setTileColumn(int column) {
        this.tileColumn = column;
    }

    public int getTileRow() {
        return tileRow;
    }

    public void setTileRow(int row) {
        this.tileRow = row;
    }

    public byte[] getTileData() {
        return tileData;
    }

    public void setTileData(byte[] data) {
        this.tileData = data;
    }
}
