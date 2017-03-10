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
        if (keyName.equals("TileMatrix")) {
            this.tileMatrixIdentifier = ((WmtsTileMatrix) value).getLimitIdentifier();
        } else if (keyName.equals("MinTileRow")) {
            this.minTileRow = Integer.parseInt((String) value);
        } else if (keyName.equals("MaxTileRow")) {
            this.maxTileRow = Integer.parseInt((String) value);
        } else if (keyName.equals("MinTileCol")) {
            this.minTileCol = Integer.parseInt((String) value);
        } else if (keyName.equals("MaxTileCol")) {
            this.maxTileCol = Integer.parseInt((String) value);
        }
    }
}
