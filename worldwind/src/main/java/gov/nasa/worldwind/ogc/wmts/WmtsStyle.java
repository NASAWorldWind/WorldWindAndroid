/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsStyle extends XmlModel {

    protected String title;

    protected String identifier;

    protected boolean isDefault = false;

    protected WmtsElementLink legendUrl;

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("isDefault")) {
            this.isDefault = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("LegendURL")) {
            this.legendUrl = (WmtsElementLink) value;
        }
    }
}
