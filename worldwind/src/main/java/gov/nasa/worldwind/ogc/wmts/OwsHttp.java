/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsHttp extends XmlModel {

    protected OwsHttpMethod get;

    protected OwsHttpMethod post;

    public OwsHttpMethod getGetMethod() {
        return this.get;
    }

    public OwsHttpMethod getPostMethod() {
        return this.post;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Get")) {
            this.get = (OwsHttpMethod) value;
        } else if (keyName.equals("Post")) {
            this.post = (OwsHttpMethod) value;
        }
    }
}
