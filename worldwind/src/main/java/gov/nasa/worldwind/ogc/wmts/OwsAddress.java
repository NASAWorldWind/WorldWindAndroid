/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsAddress extends XmlModel {

    protected String deliveryPoint;

    protected String city;

    protected String administrativeArea;

    protected String postalCode;

    protected String country;

    protected String email;

    public String getDeliveryPoint() {
        return this.deliveryPoint;
    }

    public String getCity() {
        return this.city;
    }

    public String getAdministrativeArea() {
        return this.administrativeArea;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public String getCountry() {
        return this.country;
    }

    public String getEmail() {
        return this.email;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("DeliveryPoint")) {
            this.deliveryPoint = (String) value;
        } else if (keyName.equals("City")) {
            this.city = (String) value;
        } else if (keyName.equals("AdministrativeArea")) {
            this.administrativeArea = (String) value;
        } else if (keyName.equals("PostalCode")) {
            this.postalCode = (String) value;
        } else if (keyName.equals("Country")) {
            this.country = (String) value;
        } else if (keyName.equals("ElectronicMailAddress")) {
            this.email = (String) value;
        }
    }
}
