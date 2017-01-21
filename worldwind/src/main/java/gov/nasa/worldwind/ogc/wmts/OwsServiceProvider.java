/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsServiceProvider extends XmlModel {

    protected String providerName;

    protected String siteHref;

    protected OwsServiceContact serviceContact;

    public String getProviderName() {
        return this.providerName;
    }

    public String getSiteHref() {
        return this.siteHref;
    }

    public OwsServiceContact getServiceContact() {
        return this.serviceContact;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("ProviderName")) {
            this.providerName = (String) value;
        }
        if (keyName.equals("ProviderSite")) {
            this.siteHref = ((WmtsElementLink) value).href;
        }
        if (keyName.equals("ServiceContact")) {
            this.serviceContact = (OwsServiceContact) value;
        }
    }
}
