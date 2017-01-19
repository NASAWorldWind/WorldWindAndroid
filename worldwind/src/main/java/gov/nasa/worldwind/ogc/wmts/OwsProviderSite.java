/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsProviderSite extends XmlModel {

    protected String href;

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("href")) {
            this.href = (String) value;
        }
    }
}
