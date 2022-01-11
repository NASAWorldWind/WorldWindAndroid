/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsAttribution extends XmlModel {

    protected String title;

    protected String url;

    protected WmsLogoUrl logoUrl;

    public WmsAttribution() {
    }

    public String getTitle() {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public WmsLogoUrl getLogoURL() {
        return this.logoUrl;
    }

    @Override
    public void parseField(String keyName, Object value) {
        switch (keyName) {
            case "Title":
                this.title = (String) value;
                break;
            case "OnlineResource":
                this.url = ((WmsOnlineResource) value).getUrl();
                break;
            case "LogoURL":
                this.logoUrl = (WmsLogoUrl) value;
                break;
        }
    }
}
