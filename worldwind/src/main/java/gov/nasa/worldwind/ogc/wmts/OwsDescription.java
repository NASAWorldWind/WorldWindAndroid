/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsDescription extends XmlModel {

    protected Map<String, String> titles = new HashMap<>();

    protected Map<String, String> abstracts = new HashMap<>();

    public Map<String, String> getTitles() {
        return this.titles;
    }

    public Map<String, String> getAbstracts() {
        return this.abstracts;
    }

    public String getTitle(String language) {
        return this.titles.get(language);
    }

    public String getAbstract(String language) {
        return this.abstracts.get(language);
    }

    public String getDefaultTitle() {
        return this.titles.get(null);
    }

    public String getDefaultAbstract() {
        return this.abstracts.get(null);
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            OwsDescriptionType field = (OwsDescriptionType) value;
            this.titles.put(field.getLang(), field.getValue());
        } else if (keyName.equals("Abstract")) {
            OwsDescriptionType field = (OwsDescriptionType) value;
            this.abstracts.put(field.getLang(), field.getValue());
        }
    }
}
