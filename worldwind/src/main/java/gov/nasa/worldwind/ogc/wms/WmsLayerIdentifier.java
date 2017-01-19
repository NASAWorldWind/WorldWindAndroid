/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerIdentifier extends XmlModel {

    protected String authority;

    protected StringBuilder text = new StringBuilder();

    public WmsLayerIdentifier() {
    }

    public String getAuthority() {
        return this.authority;
    }

    public String getIdentifier() {
        return this.text.toString();
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("authority")) {
            this.authority = value.toString();
        }
    }

    @Override
    protected void parseText(String text) {
        this.text.append(text);
    }
}
