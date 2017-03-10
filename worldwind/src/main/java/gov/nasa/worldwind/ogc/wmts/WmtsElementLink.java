/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsElementLink extends XmlModel {

    protected String url;

    protected String format;

    public String getUrl() {
        return this.url;
    }

    public String getFormat() {
        return this.format;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("href")) {
            this.url = (String) value;
        } else if (keyName.equals("format")) {
            this.format = (String) value;
        }
    }
}
