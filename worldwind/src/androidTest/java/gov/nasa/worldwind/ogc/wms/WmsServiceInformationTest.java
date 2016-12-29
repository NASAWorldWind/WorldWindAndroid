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
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsServiceInformationTest {

    @Test
    public void testService_ParseAndReadSampleWms() throws Exception {

        // Sample XML
        String xml = "<Service xmlns=\"http://www.opengis.net/wms\">\n" +
            "        <!-- The WMT-defined name for this type of service -->\n" +
            "        <Name>WMS</Name>\n" +
            "        <!-- Human-readable title for pick lists -->\n" +
            "        <Title>Acme Corp. Map Server</Title>\n" +
            "        <!-- Narrative description providing additional information -->\n" +
            "        <Abstract>Map Server maintained by Acme Corporation. Contact: webmaster@wmt.acme.com. High-quality maps showing\n" +
            "            roadrunner nests and possible ambush locations.\n" +
            "        </Abstract>\n" +
            "        <KeywordList>\n" +
            "            <Keyword>bird</Keyword>\n" +
            "            <Keyword>roadrunner</Keyword>\n" +
            "            <Keyword>ambush</Keyword>\n" +
            "        </KeywordList>\n" +
            "        <!-- Top-level web address of service or service provider.  See also OnlineResource\n" +
            "        elements under <DCPType>. -->\n" +
            "        <OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "                        xlink:type=\"simple\"\n" +
            "                        xlink:href=\"http://hostname/\"/>\n" +
            "        <!-- Contact information -->\n" +
            "        <ContactInformation>\n" +
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
            "        </ContactInformation>\n" +
            "        <!-- Fees or access constraints imposed. -->\n" +
            "        <Fees>none</Fees>\n" +
            "        <AccessConstraints>none</AccessConstraints>\n" +
            "        <LayerLimit>16</LayerLimit>\n" +
            "        <MaxWidth>2048</MaxWidth>\n" +
            "        <MaxHeight>2048</MaxHeight>\n" +
            "    </Service>";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        context.setParserInput(is);
        WmsServiceInformation elementModel = new WmsServiceInformation(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);
        Set<String> keywords = elementModel.getKeywords();
        WmsContactInformation contact = elementModel.getContactInformation();

        assertEquals("test name", "WMS", elementModel.getServiceName());
        assertEquals("test title", "Acme Corp. Map Server", elementModel.getServiceTitle());
        assertEquals("test name", true, elementModel.getServiceAbstract().startsWith("Map Server maintained by Acme Corporation. Contact"));
        assertEquals("test title", "Acme Corp. Map Server", elementModel.getServiceTitle());
        assertEquals("test keyword count", 3, keywords.size());
        assertEquals("test keyword list contains", true, keywords.contains("bird"));
        assertEquals("test keyword list contains", true, keywords.contains("roadrunner"));
        assertEquals("test keyword list contains", true, keywords.contains("ambush"));
        assertEquals("test online resource", "http://hostname/", elementModel.getOnlineResource().getHref());
        assertEquals("test fees", "none", elementModel.getFees());
        assertEquals("test access constraints", "none", elementModel.getAccessConstraints());
        assertEquals("test layer limit", 16, elementModel.getLayerLimit());
        assertEquals("test max width", 2048, elementModel.getMaxWidth());
        assertEquals("test max height", 2048, elementModel.getMaxHeight());
        assertEquals("test contact person", "Jeff Smith", contact.getPersonPrimary());
        assertEquals("test contact person", "Computer Scientist", contact.getPosition());
    }
}
