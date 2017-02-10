/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsHttp extends XmlModel {

    protected List<OwsHttpMethod> get = new ArrayList<>();

    protected List<OwsHttpMethod> post = new ArrayList<>();

    public OwsHttp() {
    }

    public List<OwsHttpMethod> getGetMethods() {
        return this.get;
    }

    public List<OwsHttpMethod> getPostMethods() {
        return this.post;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Get")) {
            this.get.add((OwsHttpMethod) value);
        } else if (keyName.equals("Post")) {
            this.post.add((OwsHttpMethod) value);
        }
    }
}
