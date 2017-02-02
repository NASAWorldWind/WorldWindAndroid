/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsContactPersonPrimary extends XmlModel {

    protected String contactPerson;

    protected String contactOrganization;

    public String getContactPerson() {
        return this.contactPerson;
    }

    public String getContactOrganization() {
        return this.contactOrganization;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("ContactPerson")) {
            this.contactPerson = (String) value;
        } else if (keyName.equals("ContactOrganization")) {
            this.contactOrganization = (String) value;
        }
    }
}
