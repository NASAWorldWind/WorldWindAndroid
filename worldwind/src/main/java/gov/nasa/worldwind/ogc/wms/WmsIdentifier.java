/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsIdentifier extends XmlModel {

    protected String authority;

    protected String identifier;

    public WmsIdentifier() {
    }

    public String getAuthority() {
        return this.authority;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("authority")) {
            this.authority = (String) value;
        }
    }

    @Override
    protected void parseText(String text) {
        this.identifier = text;
    }
}
