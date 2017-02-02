/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsRequest extends XmlModel {

    public WmsRequest() {
    }

    protected WmsRequestOperation getCapabilities;

    protected WmsRequestOperation getMap;

    protected WmsRequestOperation getFeatureInfo;

    public WmsRequestOperation getGetCapabilities() {
        return this.getCapabilities;
    }

    public WmsRequestOperation getGetMap() {
        return this.getMap;
    }

    public WmsRequestOperation getGetFeatureInfo() {
        return this.getFeatureInfo;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("GetCapabilities")) {
            this.getCapabilities = (WmsRequestOperation) value;
        } else if (keyName.equals("GetMap")) {
            this.getMap = (WmsRequestOperation) value;
        } else if (keyName.equals("GetFeatureInfo")) {
            this.getFeatureInfo = (WmsRequestOperation) value;
        }
    }
}
