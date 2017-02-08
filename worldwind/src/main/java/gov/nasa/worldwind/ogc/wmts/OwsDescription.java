/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsDescription extends XmlModel {

    protected List<OwsLanguageString> titles = new ArrayList<>();

    protected List<OwsLanguageString> abstracts = new ArrayList<>();

    protected List<OwsLanguageString> keywords = new ArrayList<>();

    public List<OwsLanguageString> getTitles() {
        return this.titles;
    }

    public List<OwsLanguageString> getAbstracts() {
        return this.abstracts;
    }

    public List<OwsLanguageString> getKeywords() {
        return this.keywords;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.titles.add((OwsLanguageString) value);
        } else if (keyName.equals("Abstract")) {
            this.abstracts.add((OwsLanguageString) value);
        } else if (keyName.equals("Keywords")) {
            this.keywords.addAll(((OwsKeywords) value).getKeywords());
        }
    }
}
