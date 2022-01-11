/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsInfoUrl extends XmlModel {

    protected String type;

    protected final List<String> formats = new ArrayList<>();

    protected String url;

    public WmsInfoUrl() {
    }

    public String getUrl() {
        return this.url;
    }

    public List<String> getFormats() {
        return this.formats;
    }

    public String getType() {
        return this.type;
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
            case "type":
                this.type = (String) value;
                break;
        }
    }
}
