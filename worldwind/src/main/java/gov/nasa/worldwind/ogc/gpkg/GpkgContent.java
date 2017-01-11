/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

public class GpkgContent extends GpkgEntry {

    protected String tableName;

    protected String dataType;

    protected String identifier;

    protected String description;

    protected String lastChange;

    protected double minX;

    protected double minY;

    protected double maxX;

    protected double maxY;

    protected int srsId;

    public GpkgContent() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String name) {
        this.tableName = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String text) {
        this.dataType = text;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String text) {
        this.identifier = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String text) {
        this.description = text;
    }

    public String getLastChange() {
        return lastChange;
    }

    public void setLastChange(String dateString) {
        this.lastChange = dateString;
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

    public int getSrsId() {
        return srsId;
    }

    public void setSrsId(int id) {
        this.srsId = id;
    }
}
