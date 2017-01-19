/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerIdentifier extends XmlModel {

    protected String authority;

    protected String identifier;

    protected StringBuilder text;

    public WmsLayerIdentifier() {
    }

    public String getAuthority() {
        return this.authority;
    }

    public String getIdentifier() {
        if (this.text != null) {
            this.identifier = this.text.toString().trim();
        }

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
        if (text == null) {
            return; // nothing to parse
        }

        if (this.text == null) {
            this.text = new StringBuilder();
        }
        this.text.append(text);
    }
}
