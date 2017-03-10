/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsContactInfo extends XmlModel {

    protected OwsPhone phone;

    protected OwsAddress address;

    public OwsPhone getPhone() {
        return this.phone;
    }

    public OwsAddress getAddress() {
        return this.address;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Phone")) {
            this.phone = (OwsPhone) value;
        } else if (keyName.equals("Address")) {
            this.address = (OwsAddress) value;
        }
    }
}
