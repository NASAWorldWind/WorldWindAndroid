/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlAbstractGml extends XmlModel {

    protected String id;

    public GmlAbstractGml() {
    }

    public String getId() {
        return this.id;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        if (keyName.equals("id")) {
            this.id = (String) value;
        }
    }
}
