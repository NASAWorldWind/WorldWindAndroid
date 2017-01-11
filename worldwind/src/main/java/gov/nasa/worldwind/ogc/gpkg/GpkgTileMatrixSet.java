/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

public class GpkgTileMatrixSet extends GpkgEntry {

    protected String tableName;

    protected int srsId;

    protected double minX;

    protected double minY;

    protected double maxX;

    protected double maxY;

    public GpkgTileMatrixSet() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String name) {
        this.tableName = name;
    }

    public int getSrsId() {
        return srsId;
    }

    public void setSrsId(int id) {
        this.srsId = id;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double x) {
        this.minX = x;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double y) {
        this.minY = y;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double x) {
        this.maxX = x;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double y) {
        this.maxY = y;
    }
}
