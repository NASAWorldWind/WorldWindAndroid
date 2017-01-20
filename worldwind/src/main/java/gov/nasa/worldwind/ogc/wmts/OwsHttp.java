/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsHttp extends XmlModel {

    protected String getHref;

    protected String postHref;

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Get")) {
            this.getHref = ((WmtsElementLink) value).href;
        } else if (keyName.equals("Post")) {
            this.postHref = ((WmtsElementLink) value).href;
        }
    }
}
