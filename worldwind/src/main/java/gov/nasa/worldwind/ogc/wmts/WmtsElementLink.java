/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsElementLink extends XmlModel {

    protected String href;

    protected String format;

    public String getHref() {
        return href;
    }

    public String getFormat() {
        return format;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("href")) {
            this.href = (String) value;
        } else if (keyName.equals("format")) {
            this.format = (String) value;
        }
    }
}
