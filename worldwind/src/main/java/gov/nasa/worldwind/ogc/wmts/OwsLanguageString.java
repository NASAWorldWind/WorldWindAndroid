/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsLanguageString extends XmlModel {

    protected String lang;

    protected String value;

    public OwsLanguageString() {
    }

    public String getLang() {
        return this.lang;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("lang")) {
            this.lang = (String) value;
        }
    }

    @Override
    protected void parseText(String text) {
        this.value = text;
    }
}
