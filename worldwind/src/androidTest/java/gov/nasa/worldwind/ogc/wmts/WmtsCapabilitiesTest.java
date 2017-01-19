/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import android.content.res.Resources;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import gov.nasa.worldwind.test.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmtsCapabilitiesTest {

    protected WmtsCapabilities wmtsCapabilities;

    @Before
    public void setup() throws Exception {
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream inputStream = resources.openRawResource(R.raw.test_gov_nasa_worldwind_wmts_capabilities_spec);
        this.wmtsCapabilities = WmtsCapabilities.getCapabilities(new BufferedInputStream(inputStream));
        inputStream.close();
    }

    @Test
    public void testGetServiceIdentification_Title() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        String expected = "World example Web Map Tile Service";

        String actual = serviceIdentification.title;

        assertEquals("Service Identification Title", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_Abstract() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        String expected = "Example service that contrains some world layers in the" +
            "            urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Pixel Well-known scale set";

        String actual = serviceIdentification.serviceAbstract;

        assertEquals("Service Identification Abstract", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_Keywords() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        Set<String> expected = new HashSet<>(Arrays.asList("World", "Global", "Digital Elevation Model", "Administrative Boundaries"));

        Set<String> actual = serviceIdentification.keywords.keywords;

        assertEquals("Service Identification Keywords", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_ServiceType() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        String expected = "OGC WMTS";

        String actual = serviceIdentification.serviceType;

        assertEquals("Service Identification Type", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_ServiceTypeVersion() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        String expected = "1.0.0";

        String actual = serviceIdentification.serviceTypeVersion;

        assertEquals("Service Identification Type Version", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_Fees() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        String expected = "none";

        String actual = serviceIdentification.fees;

        assertEquals("Service Identification Fees", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_AccessConstraints() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.serviceIdentification;
        String expected = "none";

        String actual = serviceIdentification.accessConstraints;

        assertEquals("Service Identification Access Constraints", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Name() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.serviceProvider;
        String expected = "UAB-CREAF-MiraMon";

        String actual = serviceProvider.providerName;

        assertEquals("Service Provider Name", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Site() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.serviceProvider;
        String expected = "http://www.creaf.uab.es/miramon";

        String actual = serviceProvider.siteHref;

        assertEquals("Service Provider Site Link", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Contact_Name() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.serviceProvider;
        String expected = "Joan Maso Pau";

        String actual = serviceProvider.serviceContact.individualName;

        assertEquals("Service Provider Contact Individual Name", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Contact_Position() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.serviceProvider;
        String expected = "Senior Software Engineer";

        String actual = serviceProvider.serviceContact.positionName;

        assertEquals("Service Provider Contact Position Name", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Contact_InfoPhone() throws Exception {
        OwsContactInfo contactInfo = this.wmtsCapabilities.serviceProvider.serviceContact.contactInfo;
        String expectedVoice = "+34 93 581 1312";
        String expectedFax = "+34 93 581 4151";

        String actualVoice = contactInfo.phone.voice;
        String actualFax = contactInfo.phone.fax;

        assertEquals("Service Provider Contact Phone Voice", expectedVoice, actualVoice);
        assertEquals("Service Provider Contact Phone Fax", expectedFax, actualFax);
    }

    @Test
    public void testGetServiceProvider_Contact_InfoAddress() throws Exception {
        OwsContactInfo contactInfo = this.wmtsCapabilities.serviceProvider.serviceContact.contactInfo;
        String expectedDeliveryPoint = "Fac Ciencies UAB";
        String expectedCity = "Bellaterra";
        String expectedAdministrativeArea = "Barcelona";
        String expectedPostalCode = "08193";
        String expectedCountry = "Spain";
        String expectedEmail = "joan.maso@uab.es";

        String actualDeliveryPoint = contactInfo.address.deliveryPoint;
        String actualCity = contactInfo.address.city;
        String actualAdministrativeArea = contactInfo.address.administrativeArea;
        String actualPostalCode = contactInfo.address.postalCode;
        String actualCountry = contactInfo.address.country;
        String actualEmail = contactInfo.address.email;

        assertEquals("Service Provider Contact Address Delivery Point", expectedDeliveryPoint, actualDeliveryPoint);
        assertEquals("Service Provider Contact Address City", expectedCity, actualCity);
        assertEquals("Service Provider Contact Address Admin Area", expectedAdministrativeArea, actualAdministrativeArea);
        assertEquals("Service Provider Contact Address Postal Code", expectedPostalCode, actualPostalCode);
        assertEquals("Service Provider Contact Address Country", expectedCountry, actualCountry);
        assertEquals("Service Provider Contact Address Email", expectedEmail, actualEmail);
    }
}
