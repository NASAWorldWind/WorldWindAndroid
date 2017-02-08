/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsDcp extends XmlModel {

    protected OwsHttpMethod getMethod;

    protected OwsHttpMethod postMethod;

    public OwsDcp() {
    }

    public OwsHttpMethod getGetMethod() {
        return this.getMethod;
    }

    public OwsHttpMethod getPostMethod() {
        return this.postMethod;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("HTTP")) {
            OwsHttp http = (OwsHttp) value;
            this.getMethod = http.getGetMethod();
            this.postMethod = http.getPostMethod();
        }
    }
}
