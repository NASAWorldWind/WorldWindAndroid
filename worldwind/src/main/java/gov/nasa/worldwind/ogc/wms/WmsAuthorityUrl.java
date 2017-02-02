/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

public class WmsAuthorityUrl extends WmsInfoUrl {

    protected String name;

    public WmsAuthorityUrl() {
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
        if (keyName.equals("name")) {
            this.name = (String) value;
        }
    }
}
