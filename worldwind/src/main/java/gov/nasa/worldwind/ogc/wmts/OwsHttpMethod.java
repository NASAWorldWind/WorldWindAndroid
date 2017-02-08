/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsHttpMethod extends XmlModel {

    protected String url;

    protected List<String> allowedValues = new ArrayList<>();

    public String getUrl() {
        return this.url;
    }

    public List<String> getAllowedValues() {
        return this.allowedValues;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("href")) {
            this.url = (String) value;
        } else if (keyName.equals("Constraint")) {
            List<OwsAllowedValues> allowedValues = ((OwsConstraint) value).getAllowedValues();
            for (OwsAllowedValues allowedValue : allowedValues) {
                this.allowedValues.addAll(allowedValue.getAllowedValues());
            }
        }
    }
}
