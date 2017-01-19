/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsKeywords extends XmlModel {

    protected Set<String> keywords = new LinkedHashSet<>();

    public WmsKeywords() {
    }

    public Set<String> getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Keyword")) {
            this.keywords.add((String) value);
        }
    }
}
