/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsAllowedValues extends XmlModel {

    protected List<String> allowedValues = new ArrayList<>();

    public OwsAllowedValues() {
    }

    public List<String> getAllowedValues() {
        return this.allowedValues;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Value")) {
            this.allowedValues.add((String) value);
        }
    }
}
