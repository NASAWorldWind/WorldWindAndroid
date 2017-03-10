/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsDcp extends XmlModel {

    protected List<OwsHttpMethod> getMethod = new ArrayList<>();

    protected List<OwsHttpMethod> postMethod = new ArrayList<>();

    public OwsDcp() {
    }

    public List<OwsHttpMethod> getGetMethods() {
        return this.getMethod;
    }

    public List<OwsHttpMethod> getPostMethods() {
        return this.postMethod;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("HTTP")) {
            OwsHttp http = (OwsHttp) value;
            this.getMethod.addAll(http.getGetMethods());
            this.postMethod.addAll(http.getPostMethods());
        }
    }
}
