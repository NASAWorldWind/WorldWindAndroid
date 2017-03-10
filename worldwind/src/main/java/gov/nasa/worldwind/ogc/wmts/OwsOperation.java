/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsOperation extends XmlModel {

    protected String name;

    protected List<OwsDcp> dcps = new ArrayList<>();

    public OwsOperation() {
    }

    public String getName() {
        return this.name;
    }

    public List<OwsDcp> getDcps() {
        return this.dcps;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("name")) {
            this.name = (String) value;
        } else if (keyName.equals("DCP")) {
            this.dcps.add((OwsDcp) value);
        }
    }
}
