/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsAddress extends XmlModel {

    protected String addressType;

    protected String address;

    protected String city;

    protected String stateOrProvince;

    protected String postCode;

    protected String country;

    public WmsAddress() {
    }

    public String getAddressType() {
        return this.addressType;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCity() {
        return this.city;
    }

    public String getStateOrProvince() {
        return this.stateOrProvince;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public String getCountry() {
        return this.country;
    }

    @Override
    public void parseField(String keyName, Object value) {
        switch (keyName) {
            case "Address":
                this.address = (String) value;
                break;
            case "AddressType":
                this.addressType = (String) value;
                break;
            case "City":
                this.city = (String) value;
                break;
            case "StateOrProvince":
                this.stateOrProvince = (String) value;
                break;
            case "PostCode":
                this.postCode = (String) value;
                break;
            case "Country":
                this.country = (String) value;
                break;
        }
    }
}
