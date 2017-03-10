/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsBoundingBox extends XmlModel {

    protected String crs;

    protected String lowerCorner;

    protected String upperCorner;

    public String getLowerCorner() {
        return this.lowerCorner;
    }

    public String getUpperCorner() {
        return this.upperCorner;
    }

    public String getCrs() {
        return this.crs;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("crs")) {
            this.crs = (String) value;
        } else if (keyName.equals("LowerCorner")) {
            this.lowerCorner = (String) value;
        } else if (keyName.equals("UpperCorner")) {
            this.upperCorner = (String) value;
        }
    }
}
