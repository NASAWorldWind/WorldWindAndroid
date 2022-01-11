/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsRequestOperation extends XmlModel {

    protected String name;

    protected final List<String> formats = new ArrayList<>();

    protected String getUrl;

    protected String postUrl;

    public WmsRequestOperation() {
    }

    public List<String> getFormats() {
        return this.formats;
    }

    public String getName() {
        return this.name;
    }

    public String getGetUrl() {
        return this.getUrl;
    }

    public String getPostUrl() {
        return this.postUrl;
    }

    @Override
    public void parseField(String keyName, Object value) {
        switch (keyName) {
            case "name":
                this.name = (String) value;
                break;
            case "Format":
                this.formats.add((String) value);
                break;
            case "DCPType":
                WmsDcpType dcpType = (WmsDcpType) value;
                this.getUrl = dcpType.getGetHref();
                this.postUrl = dcpType.getPostHref();
                break;
        }
    }
}
