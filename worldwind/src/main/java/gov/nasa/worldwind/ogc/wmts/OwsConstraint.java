/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsConstraint extends XmlModel {

    protected String name;

    protected List<String> allowedValues = new ArrayList<>();

    public OwsConstraint() {
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAllowedValues() {
        return this.allowedValues;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("name")) {
            this.name = (String) value;
        } else if (keyName.equals("AllowedValues")) {
            this.allowedValues.addAll(((OwsAllowedValues) value).getAllowedValues());
        } else if (keyName.equals("AnyValue")) {
            this.allowedValues.add("AnyValue");
        }
    }
}
