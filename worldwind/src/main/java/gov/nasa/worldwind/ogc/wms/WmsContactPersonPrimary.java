/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.TextModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsContactPersonPrimary extends XmlModel {

    protected String person;

    protected String organization;

    public WmsContactPersonPrimary() {}

    @Override
    protected void setField(String keyName, Object value) {
        if (keyName.equals("ContactPerson")) {
            TextModel textModel = (TextModel) value;
            this.person = textModel.getValue();
        } else if (keyName.equals("ContactOrganization")) {
            TextModel textModel = (TextModel) value;
            this.organization = textModel.getValue();
        }
    }
}
