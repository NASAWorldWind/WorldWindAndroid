/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsConstraint extends XmlModel {

    protected String encoding;

    protected List<OwsAllowedValues> allowedValues = new ArrayList<>();

    public String getEncoding() {
        return this.encoding;
    }

    public List<OwsAllowedValues> getAllowedValues() {
        return Collections.unmodifiableList(this.allowedValues);
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("encoding")) {
            this.encoding = (String) value;
        } else if (keyName.equals("AllowedValues")) {
            this.allowedValues.add((OwsAllowedValues) value);
        }
    }
}
