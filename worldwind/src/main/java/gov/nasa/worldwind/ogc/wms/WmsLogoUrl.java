/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLogoUrl extends XmlModel {

    protected final Set<String> formats = new LinkedHashSet<>();

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
        switch (keyName) {
            case "Format":
                this.formats.add((String) value);
                break;
            case "OnlineResource":
                this.url = ((WmsOnlineResource) value).getUrl();
                break;
            case "width":
                this.width = Integer.parseInt((String) value);
                break;
            case "height":
                this.height = Integer.parseInt((String) value);
                break;
        }
    }
}
