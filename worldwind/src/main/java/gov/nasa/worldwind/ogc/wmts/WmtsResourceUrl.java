/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsResourceUrl extends XmlModel {

    protected String format;

    protected String resourceType;

    protected String template;

    public String getFormat() {
        return this.format;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public String getTemplate() {
        return this.template;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("format")) {
            this.format = (String) value;
        } else if (keyName.equals("resourceType")) {
            this.resourceType = (String) value;
        } else if (keyName.equals("template")) {
            this.template = (String) value;
        }
    }
}
