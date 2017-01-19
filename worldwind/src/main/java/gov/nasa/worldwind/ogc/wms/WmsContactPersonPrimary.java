/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsContactPersonPrimary extends XmlModel {

    protected String person;

    protected String organization;

    public WmsContactPersonPrimary() {
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("ContactPerson")) {
            this.person = (String) value;
        } else if (keyName.equals("ContactOrganization")) {
            this.organization = (String) value;
        }
    }
}
