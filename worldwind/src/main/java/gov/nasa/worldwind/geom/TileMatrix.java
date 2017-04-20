/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

public class TileMatrix {

    public Sector sector = new Sector();

    public int ordinal;

    public int matrixWidth;

    public int matrixHeight;

    public int tileWidth;

    public int tileHeight;

    public TileMatrix() {
    }

    public double degreesPerPixel() {
        return this.sector.deltaLatitude() / (this.matrixHeight * this.tileHeight);
    }

    public Sector tileSector(int row, int column) {
        double deltaLat = this.sector.deltaLatitude() / this.matrixHeight;
        double deltaLon = this.sector.deltaLongitude() / this.matrixWidth;
        double minLat = this.sector.maxLatitude() - deltaLat * (row + 1);
        double minLon = this.sector.minLongitude() + deltaLon * column;

        return new Sector(minLat, minLon, deltaLat, deltaLon);
    }
}
