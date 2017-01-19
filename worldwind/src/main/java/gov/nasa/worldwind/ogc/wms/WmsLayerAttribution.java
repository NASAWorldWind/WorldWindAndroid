/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerAttribution extends XmlModel {

    protected String title;

    protected WmsOnlineResource onlineResource;

    protected WmsLogoUrl logoUrl;

    public WmsLayerAttribution() {
    }

    public String getTitle() {
        return this.title;
    }

    public WmsOnlineResource getOnlineResource() {
        return this.onlineResource;
    }

    public WmsLogoUrl getLogoURL() {
        return this.logoUrl;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("OnlineResource")) {
            this.onlineResource = (WmsOnlineResource) value;
        } else if (keyName.equals("LogoURL")) {
            this.logoUrl = (WmsLogoUrl) value;
        }
    }
}
