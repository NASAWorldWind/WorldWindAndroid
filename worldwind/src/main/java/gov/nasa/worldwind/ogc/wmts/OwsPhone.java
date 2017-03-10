/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsPhone extends XmlModel {

    protected String voice;

    protected String fax;

    public String getVoice() {
        return this.voice;
    }

    public String getFax() {
        return this.fax;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Voice")) {
            this.voice = (String) value;
        } else if (keyName.equals("Facsimile")) {
            this.fax = (String) value;
        }
    }
}
