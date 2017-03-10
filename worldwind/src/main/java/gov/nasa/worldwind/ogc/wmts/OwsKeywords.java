/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsKeywords extends XmlModel {

    protected List<OwsLanguageString> keywords = new ArrayList<>();

    public List<OwsLanguageString> getKeywords() {
        return this.keywords;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Keyword")) {
            this.keywords.add((OwsLanguageString) value);
        }
    }
}
