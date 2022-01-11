/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsDescription extends XmlModel {

    protected final List<OwsLanguageString> titles = new ArrayList<>();

    protected final List<OwsLanguageString> abstracts = new ArrayList<>();

    protected final List<OwsLanguageString> keywords = new ArrayList<>();

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
        switch (keyName) {
            case "Title":
                this.titles.add((OwsLanguageString) value);
                break;
            case "Abstract":
                this.abstracts.add((OwsLanguageString) value);
                break;
            case "Keywords":
                this.keywords.addAll(((OwsKeywords) value).getKeywords());
                break;
        }
    }
}
