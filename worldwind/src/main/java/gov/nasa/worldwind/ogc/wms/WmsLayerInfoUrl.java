/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerInfoUrl extends XmlModel {

    protected String type;

    protected Set<String> formats = new LinkedHashSet<>();

    protected WmsOnlineResource onlineResource;

    public WmsLayerInfoUrl() {
    }

    public WmsOnlineResource getOnlineResource() {
        return this.onlineResource;
    }

    public Set<String> getFormats() {
        return Collections.unmodifiableSet(this.formats);
    }

    public String getType() {
        return this.type;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Format")) {
            this.formats.add((String) value);
        } else if (keyName.equals("OnlineResource")) {
            this.onlineResource = (WmsOnlineResource) value;
        } else if (keyName.equals("type")) {
            this.type = (String) value;
        }
    }
}
