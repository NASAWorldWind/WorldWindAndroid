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
    }

    protected void initialize() {
        this.authority = new QName("", "authority");
    }

    public String getIdentifier() {
        return this.getCharactersContent();
    }

    public String getAuthority() {
        return this.getField(this.authority).toString();
    }
}
