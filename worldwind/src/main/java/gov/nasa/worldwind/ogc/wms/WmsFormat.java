/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsFormat extends XmlModel {

    protected String format;

    public WmsFormat() {
    }

    public String getFormat() {
        return this.format;
    }

    @Override
    protected void setField(String keyName, Object value) {
        if (keyName.equals(CHARACTERS_FIELD)) {
            this.format = value.toString().trim().toLowerCase();
        }
    }
}
