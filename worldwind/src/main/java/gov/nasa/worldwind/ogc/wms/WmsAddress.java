/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsAddress extends XmlModel
{
    protected QName ADDRESS_TYPE;
    protected QName ADDRESS;
    protected QName CITY;
    protected QName STATE_OR_PROVINCE;
    protected QName POST_CODE;
    protected QName COUNTRY;

    protected String addressType;
    protected String address;
    protected String city;
    protected String stateOrProvince;
    protected String postCode;
    protected String country;

    public WmsAddress(String namespaceURI)
    {
        super(namespaceURI);

        this.initialize();
    }

    protected void initialize()
    {
        ADDRESS_TYPE = new QName(this.getNamespaceUri(), "AddressType");
        ADDRESS = new QName(this.getNamespaceUri(), "Address");
        CITY = new QName(this.getNamespaceUri(), "City");
        STATE_OR_PROVINCE = new QName(this.getNamespaceUri(), "StateOrProvince");
        POST_CODE = new QName(this.getNamespaceUri(), "PostCode");
        COUNTRY = new QName(this.getNamespaceUri(), "Country");
    }

    @Override
    protected void doParseEventContent(XmlPullParserContext ctx)
        throws XmlPullParserException, IOException
    {

        XmlPullParser xpp = ctx.getParser();

        if (ctx.isStartElement(this.ADDRESS_TYPE)) {
            if (xpp.next() == XmlPullParser.TEXT) {
                this.setAddressType(xpp.getText().trim());
            }
        } else if (ctx.isStartElement(this.ADDRESS)) {
            if (xpp.next() == XmlPullParser.TEXT) {
                this.setAddress(xpp.getText().trim());
            }
        } else if (ctx.isStartElement(this.CITY)) {
            if (xpp.next() == XmlPullParser.TEXT) {
                this.setCity(xpp.getText().trim());
            }
        } else if (ctx.isStartElement(this.STATE_OR_PROVINCE)) {
            if (xpp.next() == XmlPullParser.TEXT) {
                this.setStateOrProvince(xpp.getText().trim());
            }
        } else if (ctx.isStartElement(this.POST_CODE)) {
            if (xpp.next() == XmlPullParser.TEXT) {
                this.setPostCode(xpp.getText().trim());
            }
        } else if (ctx.isStartElement(this.COUNTRY)) {
            if (xpp.next() == XmlPullParser.TEXT) {
                this.setCountry(xpp.getText().trim());
            }
        }
    }

    public String getAddressType()
    {
        return addressType;
    }

    protected void setAddressType(String addressType)
    {
        this.addressType = addressType;
    }

    public String getAddress()
    {
        return address;
    }

    protected void setAddress(String address)
    {
        this.address = address;
    }

    public String getCity()
    {
        return city;
    }

    protected void setCity(String city)
    {
        this.city = city;
    }

    public String getStateOrProvince()
    {
        return stateOrProvince;
    }

    protected void setStateOrProvince(String stateOrProvince)
    {
        this.stateOrProvince = stateOrProvince;
    }

    public String getPostCode()
    {
        return postCode;
    }

    protected void setPostCode(String postCode)
    {
        this.postCode = postCode;
    }

    public String getCountry()
    {
        return country;
    }

    protected void setCountry(String country)
    {
        this.country = country;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("AddressType: ").append(this.addressType != null ? this.addressType : "none").append(" ");
        sb.append("Address: ").append(this.address != null ? this.address : "none").append(" ");
        sb.append("City: ").append(this.city != null ? this.city : "none").append(" ");
        sb.append("StateOrProvince: ").append(this.stateOrProvince != null ? this.stateOrProvince : "none").append(" ");
        sb.append("PostCode: ").append(this.postCode != null ? this.postCode : "none").append(" ");
        sb.append("Country: ").append(this.country != null ? this.country : "none");

        return sb.toString();
    }
}
