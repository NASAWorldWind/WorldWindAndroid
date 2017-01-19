/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsKeywords extends XmlModel {

    protected Set<String> keywords = new LinkedHashSet<>();

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Keyword")) {
            this.keywords.add((String) value);
        }
    }
}
