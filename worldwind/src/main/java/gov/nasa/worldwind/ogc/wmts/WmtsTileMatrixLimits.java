/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTileMatrixLimits extends XmlModel {

    protected String tileMatrixIdentifier;

    protected int minTileRow;

    protected int maxTileRow;

    protected int minTileCol;

    protected int maxTileCol;

    public WmtsTileMatrixLimits() {
    }

    public String getTileMatrixIdentifier() {
        return this.tileMatrixIdentifier;
    }

    public int getMinTileRow() {
        return this.minTileRow;
    }

    public int getMaxTileRow() {
        return this.maxTileRow;
    }

    public int getMinTileCol() {
        return this.minTileCol;
    }

    public int getMaxTileCol() {
        return this.maxTileCol;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        switch (keyName) {
            case "TileMatrix":
                this.tileMatrixIdentifier = ((WmtsTileMatrix) value).getLimitIdentifier();
                break;
            case "MinTileRow":
                this.minTileRow = Integer.parseInt((String) value);
                break;
            case "MaxTileRow":
                this.maxTileRow = Integer.parseInt((String) value);
                break;
            case "MinTileCol":
                this.minTileCol = Integer.parseInt((String) value);
                break;
            case "MaxTileCol":
                this.maxTileCol = Integer.parseInt((String) value);
                break;
        }
    }
}
