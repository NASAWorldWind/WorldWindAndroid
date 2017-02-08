/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTileMatrixSetLink extends XmlModel {

    protected String identifier;

    protected WmtsTileMatrixSetLimits tileMatrixSetLimits;

    public WmtsTileMatrixSetLink() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public WmtsTileMatrixSetLimits getTileMatrixSetLimits() {
        return this.tileMatrixSetLimits;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("TileMatrixSet")) {
            this.identifier = ((WmtsTileMatrixSet) value).getLinkIdentifier();
        } else if (keyName.equals("TileMatrixSetLimits")) {
            this.tileMatrixSetLimits = (WmtsTileMatrixSetLimits) value;
        }
    }
}
