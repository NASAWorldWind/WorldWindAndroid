/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsAddress extends XmlModel {

    protected final List<String> deliveryPoint = new ArrayList<>();

    protected String city;

    protected String administrativeArea;

    protected final List<String> postalCode = new ArrayList<>();

    protected final List<String> country = new ArrayList<>();

    protected final List<String> email = new ArrayList<>();

    public OwsAddress() {
    }

    public List<String> getDeliveryPoints() {
        return this.deliveryPoint;
    }

    public String getCity() {
        return this.city;
    }

    public String getAdministrativeArea() {
        return this.administrativeArea;
    }

    public List<String> getPostalCodes() {
        return this.postalCode;
    }

    public List<String> getCountries() {
        return this.country;
    }

    public List<String> getElectronicMailAddresses() {
        return this.email;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        switch (keyName) {
            case "DeliveryPoint":
                this.deliveryPoint.add((String) value);
                break;
            case "City":
                this.city = (String) value;
                break;
            case "AdministrativeArea":
                this.administrativeArea = (String) value;
                break;
            case "PostalCode":
                this.postalCode.add((String) value);
                break;
            case "Country":
                this.country.add((String) value);
                break;
            case "ElectronicMailAddress":
                this.email.add((String) value);
                break;
        }
    }
}
