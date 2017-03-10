/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

public class WmsAuthorityUrl extends WmsInfoUrl {

    protected String name;

    protected String type;

    protected List<String> formats = new ArrayList<>();

    protected String url;

    public String getUrl() {
        return this.url;
    }

    public List<String> getFormats() {
        return this.formats;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Format")) {
            this.formats.add((String) value);
        } else if (keyName.equals("OnlineResource")) {
            this.url = ((WmsOnlineResource) value).getUrl();
        } else if (keyName.equals("type")) {
            this.type = (String) value;
        } else if (keyName.equals("name")) {
            this.name = (String) value;
        }
    }
}
