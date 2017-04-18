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

    public double pixelSpanX;

    public double pixelSpanY;

    public TileMatrix() {
    }

    public Sector tileSector(int row, int column) {
        double matrixMinLat = this.sector.maxLatitude();
        double matrixMinLon = this.sector.minLongitude();
        double deltaLat = this.tileHeight * this.pixelSpanY;
        double deltaLon = this.tileWidth * this.pixelSpanX;
        double minLat = matrixMinLat - deltaLat * (row + 1);
        double minLon = matrixMinLon + deltaLon * column;

        return new Sector(minLat, minLon, deltaLat, deltaLon);
    }
}
