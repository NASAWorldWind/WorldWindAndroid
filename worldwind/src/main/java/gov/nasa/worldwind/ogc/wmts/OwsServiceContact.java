/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsServiceContact extends XmlModel {

    protected String individualName;

    protected String positionName;

    protected OwsContactInfo contactInfo;

    public String getIndividualName() {
        return this.individualName;
    }

    public String getPositionName() {
        return this.positionName;
    }

    public OwsContactInfo getContactInfo() {
        return this.contactInfo;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("IndividualName")) {
            this.individualName = (String) value;
        } else if (keyName.equals("PositionName")) {
            this.positionName = (String) value;
        } else if (keyName.equals("ContactInfo")) {
            this.contactInfo = (OwsContactInfo) value;
        }
    }
}
