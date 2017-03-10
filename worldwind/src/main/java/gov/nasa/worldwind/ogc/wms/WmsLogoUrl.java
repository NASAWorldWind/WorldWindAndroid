/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLogoUrl extends XmlModel {

    protected Set<String> formats = new LinkedHashSet<>();

    protected String url;

    protected Integer width;

    protected Integer height;

    public WmsLogoUrl() {
    }

    public Integer getWidth() {
        return this.width;
    }

    public Integer getHeight() {
        return this.height;
    }

    public Set<String> getFormats() {
        return this.formats;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Format")) {
            this.formats.add((String) value);
        } else if (keyName.equals("OnlineResource")) {
            this.url = ((WmsOnlineResource) value).getUrl();
        } else if (keyName.equals("width")) {
            this.width = Integer.parseInt((String) value);
        } else if (keyName.equals("height")) {
            this.height = Integer.parseInt((String) value);
        }
    }
}
