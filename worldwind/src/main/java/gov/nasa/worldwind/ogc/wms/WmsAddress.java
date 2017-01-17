/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.TextModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsAddress extends XmlModel
{

    protected String addressType;

    protected String address;

    protected String city;

    protected String stateOrProvince;

    protected String postCode;

    protected String country;

    public WmsAddress(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getAddressType()
    {
        return this.addressType;
    }

    public String getAddress()
    {
        return this.address;
    }

    public String getCity()
    {
        return this.city;
    }

    public String getStateOrProvince()
    {
        return this.stateOrProvince;
    }

    public String getPostCode()
    {
        return this.postCode;
    }

    public String getCountry()
    {
        return this.country;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("Address")) {
            this.address = ((TextModel) value).getValue();
        } else if (keyName.equals("AddressType")) {
            this.addressType = ((TextModel) value).getValue();
        } else if (keyName.equals("City")) {
            this.city = ((TextModel) value).getValue();
        } else if (keyName.equals("StateOrProvince")) {
            this.stateOrProvince = ((TextModel) value).getValue();
        } else if (keyName.equals("PostCode")) {
            this.postCode = ((TextModel) value).getValue();
        } else if (keyName.equals("Country")) {
            this.country = ((TextModel) value).getValue();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("AddressType: ").append(this.getAddressType() != null ? this.getAddressType() : "none").append(" ");
        sb.append("Address: ").append(this.getAddress() != null ? this.getAddress() : "none").append(" ");
        sb.append("City: ").append(this.getCity() != null ? this.getCity() : "none").append(" ");
        sb.append("StateOrProvince: ").append(this.getStateOrProvince() != null ? this.getStateOrProvince() : "none").append(" ");
        sb.append("PostCode: ").append(this.getPostCode() != null ? this.getPostCode() : "none").append(" ");
        sb.append("Country: ").append(this.getCountry() != null ? this.getCountry() : "none");

        return sb.toString();
    }
}
