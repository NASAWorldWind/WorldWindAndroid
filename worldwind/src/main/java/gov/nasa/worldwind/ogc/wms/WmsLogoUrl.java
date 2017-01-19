/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLogoUrl extends XmlModel {

    protected Set<String> formats = new LinkedHashSet<>();

    protected WmsOnlineResource onlineResource;

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
        return Collections.unmodifiableSet(this.formats);
    }

    public WmsOnlineResource getOnlineResource() {
        return this.onlineResource;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Format")) {
            this.formats.add((String) value);
        } else if (keyName.equals("OnlineResource")) {
            this.onlineResource = (WmsOnlineResource) value;
        } else if (keyName.equals("width")) {
            this.width = Integer.parseInt((String) value);
        } else if (keyName.equals("height")) {
            this.height = Integer.parseInt((String) value);
        }
    }
}
