/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsFormat extends XmlModel {

    protected StringBuilder text = new StringBuilder();

    public WmsFormat() {
    }

    public String getFormat() {
        return this.text.toString().trim().toLowerCase();
    }

    @Override
    protected void parseText(String text) {
        this.text.append(text);
    }
}
