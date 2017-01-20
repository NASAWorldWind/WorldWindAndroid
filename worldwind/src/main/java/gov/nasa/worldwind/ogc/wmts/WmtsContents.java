/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsContents extends XmlModel {

    protected List<WmtsLayer> layers = new ArrayList<>();

    // protected List<WmtsTileMatrixSet> matrixSets = new ArrayList<>();

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Layer")) {
            this.layers.add((WmtsLayer) value);
        }
    }
}
