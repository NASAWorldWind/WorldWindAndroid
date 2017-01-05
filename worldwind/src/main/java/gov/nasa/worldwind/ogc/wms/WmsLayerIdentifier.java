/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerIdentifier extends XmlModel {

    protected QName authority;

    public WmsLayerIdentifier(String namespaceUri) {
        super(namespaceUri);
        this.initialize();
    }

    protected void initialize() {
        this.authority = new QName("", "authority");
    }

    public String getIdentifier() {
        return this.getCharactersContent();
    }

    public String getAuthority() {
        Object o = this.getField(this.authority);
        if (o instanceof String) {
            return (String) o;
        } else {
            return null;
        }
    }
}
