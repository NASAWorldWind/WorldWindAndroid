/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTileMatrixSetLimits extends XmlModel {

    protected final List<WmtsTileMatrixLimits> tileMatrixLimits = new ArrayList<>();

    public WmtsTileMatrixSetLimits() {
    }

    public List<WmtsTileMatrixLimits> getTileMatrixLimits() {
        return this.tileMatrixLimits;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("TileMatrixLimits")) {
            this.tileMatrixLimits.add((WmtsTileMatrixLimits) value);
        }
    }
}
