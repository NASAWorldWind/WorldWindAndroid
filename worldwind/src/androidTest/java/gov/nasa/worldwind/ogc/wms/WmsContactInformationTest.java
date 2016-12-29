/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsContactInformationTest {

    @Test
    public void testWmsContactInformation_ParseAndReadSampleWms() throws Exception {

        // Sample xml
        String xml = "<ContactInformation xmlns=\"http://www.opengis.net/wms\">\n" +
            "            <ContactPersonPrimary>\n" +
            "                <ContactPerson>Jeff Smith</ContactPerson>\n" +
            "                <ContactOrganization>NASA</ContactOrganization>\n" +
            "            </ContactPersonPrimary>\n" +
            "            <ContactPosition>Computer Scientist</ContactPosition>\n" +
            "            <ContactAddress>\n" +
            "                <AddressType>postal</AddressType>\n" +
            "                <Address>NASA Goddard Space Flight Center</Address>\n" +
            "                <City>Greenbelt</City>\n" +
            "                <StateOrProvince>MD</StateOrProvince>\n" +
            "                <PostCode>20771</PostCode>\n" +
            "                <Country>USA</Country>\n" +
            "            </ContactAddress>\n" +
            "            <ContactVoiceTelephone>+1 301 555-1212</ContactVoiceTelephone>\n" +
            "            <ContactElectronicMailAddress>user@host.com</ContactElectronicMailAddress>\n" +
            "        </ContactInformation>";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        context.setParserInput(is);
        WmsContactInformation elementModel = new WmsContactInformation(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);
        WmsAddress address = elementModel.getContactAddress();

        assertEquals("test primary person", "Jeff Smith", elementModel.getPersonPrimary());
        assertEquals("test primary organization", "NASA", elementModel.getOrganization());
        assertEquals("test position", "Computer Scientist", elementModel.getPosition());
        assertEquals("test voice telephone", "+1 301 555-1212", elementModel.getVoiceTelephone());
        assertEquals("test email", "user@host.com", elementModel.getElectronicMailAddress());
        assertEquals("test address type", "postal", address.getAddressType());
        assertEquals("test address", "NASA Goddard Space Flight Center", address.getAddress());
        assertEquals("test address city", "Greenbelt", address.getCity());
        assertEquals("test address state", "MD", address.getStateOrProvince());
        assertEquals("test address post code", "20771", address.getPostCode());
        assertEquals("test address country", "USA", address.getCountry());
    }
}
