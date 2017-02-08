/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsOnlineResource extends XmlModel {

    protected String type;

    protected String url;

    public WmsOnlineResource() {
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("type")) {
            this.type = (String) value;
        } else if (keyName.equals("href")) {
            this.url = (String) value;
        }
    }
}
