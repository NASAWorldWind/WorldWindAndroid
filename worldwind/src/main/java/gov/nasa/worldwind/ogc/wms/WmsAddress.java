/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsAddress extends XmlModel
{

    protected QName addressType;

    protected QName address;

    protected QName city;

    protected QName stateOrProvince;

    protected QName postCode;

    protected QName country;

    public WmsAddress(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    protected void initialize()
    {
        this.addressType = new QName(this.getNamespaceUri(), "AddressType");
        this.address = new QName(this.getNamespaceUri(), "Address");
        this.city = new QName(this.getNamespaceUri(), "City");
        this.stateOrProvince = new QName(this.getNamespaceUri(), "StateOrProvince");
        this.postCode = new QName(this.getNamespaceUri(), "PostCode");
        this.country = new QName(this.getNamespaceUri(), "Country");
    }

    public String getAddressType()
    {
        return this.getChildCharacterValue(this.addressType);
    }

    protected void setAddressType(String addressType)
    {
        this.setChildCharacterValue(this.addressType, addressType);
    }

    public String getAddress()
    {
        return this.getChildCharacterValue(this.address);
    }

    protected void setAddress(String address)
    {
        this.setChildCharacterValue(this.address, address);
    }

    public String getCity()
    {
        return this.getChildCharacterValue(this.city);
    }

    protected void setCity(String city)
    {
        this.setChildCharacterValue(this.city, city);
    }

    public String getStateOrProvince()
    {
        return this.getChildCharacterValue(this.stateOrProvince);
    }

    protected void setStateOrProvince(String stateOrProvince)
    {
        this.setChildCharacterValue(this.stateOrProvince, stateOrProvince);
    }

    public String getPostCode()
    {
        return this.getChildCharacterValue(this.postCode);
    }

    protected void setPostCode(String postCode)
    {
        this.setChildCharacterValue(this.postCode, postCode);
    }

    public String getCountry()
    {
        return this.getChildCharacterValue(this.country);
    }

    protected void setCountry(String country)
    {
        this.setChildCharacterValue(this.country, country);
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
