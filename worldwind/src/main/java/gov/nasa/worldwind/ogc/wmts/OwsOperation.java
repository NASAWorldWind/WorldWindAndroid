/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsOperation extends XmlModel {

    protected String name;

    protected OwsDcp dcp;

    public String getName() {
        return this.name;
    }

    public OwsDcp getDcp() {
        return this.dcp;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("name")) {
            this.name = (String) value;
        } else if (keyName.equals("DCP")) {
            this.dcp = (OwsDcp) value;
        }
    }
}
