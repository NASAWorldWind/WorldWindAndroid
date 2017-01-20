/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTileMatrixSetLink extends XmlModel {

    protected String linkIdentifier;

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("TileMatrixSet")) {
            this.linkIdentifier = ((WmtsTileMatrixSet) value).linkIdentifier;
        }
    }
}
