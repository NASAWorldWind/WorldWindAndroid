/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsKeywords extends XmlModel {

    protected List<String> keywords = new ArrayList<>();

    public WmsKeywords() {
    }

    public List<String> getKeywords() {
        return this.keywords;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Keyword")) {
            this.keywords.add((String) value);
        }
    }
}
