/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerInfoUrl extends XmlModel {

    protected QName format;

    protected QName onlineResource;

    public WmsLayerInfoUrl(String namespaceUri) {
        super(namespaceUri);
        this.initialize();
    }

    private void initialize() {
        this.format = new QName(this.getNamespaceUri(), "Format");
        this.onlineResource = new QName(this.getNamespaceUri(), "OnlineResource");
    }

    public WmsOnlineResource getOnlineResource() {
        Object o = this.getField(this.onlineResource);
        if (o instanceof WmsOnlineResource) {
            return (WmsOnlineResource) o;
        } else {
            return null;
        }
    }

    public String getFormat() {
        Object o = this.getField(this.format);
        if (o instanceof XmlModel) {
            return ((XmlModel) o).getCharactersContent();
        } else {
            return null;
        }
    }
}
