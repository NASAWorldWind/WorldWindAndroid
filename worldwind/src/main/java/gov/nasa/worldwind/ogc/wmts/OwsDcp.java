/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsDcp extends XmlModel {

    protected String getHref;

    protected String postHref;

    public String getGetHref() {
        return this.getHref;
    }

    public String getPostHref() {
        return this.postHref;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("HTTP")) {
            OwsHttp http = (OwsHttp) value;
            this.getHref = http.getHref;
            this.postHref = http.postHref;
        }
    }
}
