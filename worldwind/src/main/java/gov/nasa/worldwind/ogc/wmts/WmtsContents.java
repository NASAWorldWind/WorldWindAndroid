/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsContents extends XmlModel {

    protected List<WmtsLayer> layers = new ArrayList<>();

    protected Map<String, WmtsTileMatrixSet> matrixSetMap = new HashMap<>();

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Layer")) {
            this.layers.add((WmtsLayer) value);
        } else if (keyName.equals("TileMatrixSet")) {
            WmtsTileMatrixSet tileMatrixSet = (WmtsTileMatrixSet) value;
            this.matrixSetMap.put(tileMatrixSet.identifier, tileMatrixSet);
        }
    }
}
