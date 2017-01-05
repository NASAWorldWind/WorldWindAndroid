/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerAttribution extends XmlModel {

    protected QName title;

    protected QName onlineResource;

    protected QName logoUrl;

    public WmsLayerAttribution(String namespaceURI) {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize() {
        this.title = new QName(this.getNamespaceUri(), "Title");
        this.onlineResource = new QName(this.getNamespaceUri(), "OnlineResource");
        this.logoUrl = new QName(this.getNamespaceUri(), "LogoURL");
    }

    public String getTitle() {
        return this.getChildCharacterValue(this.title);
    }

    public WmsOnlineResource getOnlineResource() {
        Object o = this.getField(this.onlineResource);
        if (o instanceof WmsOnlineResource) {
            return (WmsOnlineResource) o;
        } else {
            return null;
        }
    }

    public WmsLogoUrl getLogoURL() {
        Object o = this.getField(this.logoUrl);
        if (o instanceof WmsLogoUrl) {
            return (WmsLogoUrl) o;
        } else {
            return null;
        }
    }
}
