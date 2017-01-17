/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerIdentifier extends XmlModel {

    protected String authority;

    protected String identifier;

    public WmsLayerIdentifier(String namespaceUri) {
        super(namespaceUri);
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getAuthority() {
        return this.authority;
    }

    @Override
    protected void setText(String value) {
        this.identifier = value;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("authority")) {
            this.authority = value.toString();
        }
    }
}
