/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsOperationsMetadata extends XmlModel {

    protected OwsOperation getCapabilities;

    protected OwsOperation getTile;

    protected OwsOperation getFeatureInfo;

    public OwsOperation getGetCapabilities() {
        return this.getCapabilities;
    }

    public OwsOperation getGetTile() {
        return this.getTile;
    }

    public OwsOperation getGetFeatureInfo() {
        return this.getFeatureInfo;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Operation")) {
            OwsOperation operation = (OwsOperation) value;
            if (operation.name.equals("GetCapabilities")) {
                this.getCapabilities = operation;
            } else if (operation.name.equals("GetTile")) {
                this.getTile = operation;
            } else if (operation.name.equals("GetFeatureInfo")) {
                this.getFeatureInfo = operation;
            }
        }
    }
}
