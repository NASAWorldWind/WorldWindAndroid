/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.test.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsCapabilitiesTest {

    public static final double DELTA = 1e-9;

    protected WmsCapabilities wmsCapabilities130;

    protected InputStream inputStream130;

    protected WmsCapabilities wmsCapabilities111;

    protected InputStream inputStream111;

    @Before
    public void setup() throws Exception {

        Resources resources = getInstrumentation().getTargetContext().getResources();
        this.inputStream130 = resources.openRawResource(R.raw.test_gov_nasa_worldwind_wms_capabilities_v1_3_0_spec);
        this.inputStream111 = resources.openRawResource(R.raw.test_gov_nasa_worldwind_wms_capabilities_v1_1_1_spec);

        this.wmsCapabilities130 = WmsCapabilities.getCapabilities(new BufferedInputStream(this.inputStream130));
        this.wmsCapabilities111 = WmsCapabilities.getCapabilities(new BufferedInputStream(this.inputStream111));

        this.inputStream130.close();
        this.inputStream111.close();
    }

    @Test
    public void testGetVersion_Version130() {
        assertTrue("Version", this.wmsCapabilities130.getVersion().equals("1.3.0"));
    }

    @Test
    public void testGetVersion_Version111() {
        assertTrue("Version", this.wmsCapabilities111.getVersion().equals("1.1.1"));
    }

    @Test
    public void testGetImageFormats_Version130() {
        Set<String> expectedValues = new HashSet<>();
        expectedValues.addAll(Arrays.asList("image/gif", "image/png", "image/jpeg"));

        Set<String> actualValues = this.wmsCapabilities130.getImageFormats();

        assertEquals("Image Version", expectedValues, actualValues);
    }

    @Test
    public void testGetImageFormats_Version111() {
        Set<String> expectedValues = new HashSet<>();
        expectedValues.addAll(Arrays.asList("image/gif", "image/png", "image/jpeg"));

        Set<String> actualValues = this.wmsCapabilities111.getImageFormats();

        assertEquals("Image Version", expectedValues, actualValues);
    }

    @Test
    public void testGetServiceInformation_GetAbstract_Version130() {
        String expectedValue = "Map Server maintained by Acme Corporation. Contact: webmaster@wmt.acme.com. High-quality maps showing" +
            "            roadrunner nests and possible ambush locations.";
        WmsServiceInformation serviceInformation = this.wmsCapabilities130.getServiceInformation();

        String serviceAbstract = serviceInformation.getServiceAbstract();

        assertEquals("Service Abstract", expectedValue, serviceAbstract);
    }

    @Test
    public void testGetServiceInformation_GetAbstract_Version111() {
        String expectedValue = "WMT Map Server maintained by Acme Corporation. Contact: webmaster@wmt.acme.com. High-quality maps" +
            "            showing roadrunner nests and possible ambush locations.";
        WmsServiceInformation serviceInformation = this.wmsCapabilities111.getServiceInformation();

        String serviceAbstract = serviceInformation.getServiceAbstract();

        assertEquals("Service Abstract", expectedValue, serviceAbstract);
    }

    @Test
    public void testGetServiceInformation_GetName_Version130() {
        String expectedValue = "WMS";

        String serviceName = this.wmsCapabilities130.getServiceInformation().getServiceName();

        assertEquals("Service Name", expectedValue, serviceName);
    }

    @Test
    public void testGetServiceInformation_GetName_Version111() {
        String expectedValue = "OGC:WMS";

        String serviceName = this.wmsCapabilities111.getServiceInformation().getServiceName();

        assertEquals("Service Name", expectedValue, serviceName);
    }

    @Test
    public void testGetServiceInformation_GetTitle_Version130() {
        String expectedValue = "Acme Corp. Map Server";

        String serviceTitle = this.wmsCapabilities130.getServiceInformation().getServiceTitle();

        assertEquals("Service Title", expectedValue, serviceTitle);
    }

    @Test
    public void testGetServiceInformation_GetTitle_Version111() {
        String expectedValue = "Acme Corp. Map Server";

        String serviceTitle = this.wmsCapabilities111.getServiceInformation().getServiceTitle();

        assertEquals("Service Title", expectedValue, serviceTitle);
    }

    @Test
    public void testGetServiceInformation_GetKeywords_Version130() {
        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.addAll(Arrays.asList("bird", "roadrunner", "ambush"));

        Set<String> keywords = this.wmsCapabilities130.getServiceInformation().getKeywords();

        assertEquals("Service Keywords", expectedKeywords, keywords);
    }

    @Test
    public void testGetServiceInformation_GetKeywords_Version111() {
        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.addAll(Arrays.asList("bird", "roadrunner", "ambush"));

        Set<String> keywords = this.wmsCapabilities111.getServiceInformation().getKeywords();

        assertEquals("Service Keywords", expectedKeywords, keywords);
    }

    @Test
    public void testGetServiceInformation_GetOnlineResource_Version130() {
        String expectedLinkType = "simple";
        String expectedLink = "http://hostname/";
        WmsServiceInformation serviceInformation = this.wmsCapabilities130.getServiceInformation();

        String linkType = serviceInformation.getOnlineResource().getType();
        String link = serviceInformation.getOnlineResource().getHref();

        assertEquals("Service Online Resource Link Type", expectedLinkType, linkType);
        assertEquals("Service Online Resource Link", expectedLink, link);
    }

    @Test
    public void testGetServiceInformation_GetOnlineResource_Version111() {
        String expectedLinkType = "simple";
        String expectedLink = "http://hostname/";
        WmsServiceInformation serviceInformation = this.wmsCapabilities111.getServiceInformation();

        String linkType = serviceInformation.getOnlineResource().getType();
        String link = serviceInformation.getOnlineResource().getHref();

        assertEquals("Service Online Resource Link Type", expectedLinkType, linkType);
        assertEquals("Service Online Resource Link", expectedLink, link);
    }

    @Test
    public void testGetServiceInformation_GetContactPersonPrimary_Version130() {
        String expectedPerson = "Jeff Smith";
        String expectedOrganization = "NASA";
        WmsContactInformation contactInformation = this.wmsCapabilities130.getServiceInformation().getContactInformation();

        String person = contactInformation.getPersonPrimary();
        String organization = contactInformation.getOrganization();

        assertEquals("Service Contact Information Person Primary", expectedPerson, person);
        assertEquals("Service Contact Information Organization", expectedOrganization, organization);
    }

    @Test
    public void testGetServiceInformation_GetContactPersonPrimary_Version111() {
        String expectedPerson = "Jeff deLaBeaujardiere";
        String expectedOrganization = "NASA";
        WmsContactInformation contactInformation = this.wmsCapabilities111.getServiceInformation().getContactInformation();

        String person = contactInformation.getPersonPrimary();
        String organization = contactInformation.getOrganization();

        assertEquals("Service Contact Information Person Primary", expectedPerson, person);
        assertEquals("Service Contact Information Organization", expectedOrganization, organization);
    }

    @Test
    public void testGetServiceInformation_GetContactAddress_Version130() {
        String expectedAddressType = "postal";
        String expectedAddress = "NASA Goddard Space Flight Center";
        String expectedCity = "Greenbelt";
        String expectedState = "MD";
        String expectedPostCode = "20771";
        String expectedCountry = "USA";
        WmsAddress contactAddress = this.wmsCapabilities130.getServiceInformation().getContactInformation().getContactAddress();

        String addressType = contactAddress.getAddressType();
        String address = contactAddress.getAddress();
        String city = contactAddress.getCity();
        String state = contactAddress.getStateOrProvince();
        String postCode = contactAddress.getPostCode();
        String country = contactAddress.getCountry();

        assertEquals("Service Contact Address Type", expectedAddressType, addressType);
        assertEquals("Service Contact Address", expectedAddress, address);
        assertEquals("Service Contact Address City", expectedCity, city);
        assertEquals("Service Contact Address State", expectedState, state);
        assertEquals("Service Contact Address Post Code", expectedPostCode, postCode);
        assertEquals("Service Contact Address Country", expectedCountry, country);
    }

    @Test
    public void testGetServiceInformation_GetContactAddress_Version111() {
        String expectedAddressType = "postal";
        String expectedAddress = "NASA Goddard Space Flight Center, Code 933";
        String expectedCity = "Greenbelt";
        String expectedState = "MD";
        String expectedPostCode = "20771";
        String expectedCountry = "USA";
        WmsAddress contactAddress = this.wmsCapabilities111.getServiceInformation().getContactInformation().getContactAddress();

        String addressType = contactAddress.getAddressType();
        String address = contactAddress.getAddress();
        String city = contactAddress.getCity();
        String state = contactAddress.getStateOrProvince();
        String postCode = contactAddress.getPostCode();
        String country = contactAddress.getCountry();

        assertEquals("Service Contact Address Type", expectedAddressType, addressType);
        assertEquals("Service Contact Address", expectedAddress, address);
        assertEquals("Service Contact Address City", expectedCity, city);
        assertEquals("Service Contact Address State", expectedState, state);
        assertEquals("Service Contact Address Post Code", expectedPostCode, postCode);
        assertEquals("Service Contact Address Country", expectedCountry, country);
    }

    @Test
    public void testGetServiceInformation_GetPhone_Version130() {
        String expectedValue = "+1 301 555-1212";

        String voiceTelephone = this.wmsCapabilities130.getServiceInformation().getContactInformation().getVoiceTelephone();

        assertEquals("Service Phone", expectedValue, voiceTelephone);
    }

    @Test
    public void testGetServiceInformation_GetPhone_Version111() {
        String expectedValue = "+1 301 286-1569";

        String voiceTelephone = this.wmsCapabilities111.getServiceInformation().getContactInformation().getVoiceTelephone();

        assertEquals("Service Fees", expectedValue, voiceTelephone);
    }

    @Test
    public void testGetServiceInformation_GetEmail_Version130() {
        String expectedValue = "user@host.com";

        String fees = this.wmsCapabilities130.getServiceInformation().getContactInformation().getElectronicMailAddress();

        assertEquals("Service Email", expectedValue, fees);
    }

    @Test
    public void testGetServiceInformation_GetEmail_Version111() {
        String expectedValue = "delabeau@iniki.gsfc.nasa.gov";

        String fees = this.wmsCapabilities111.getServiceInformation().getContactInformation().getElectronicMailAddress();

        assertEquals("Service Email", expectedValue, fees);
    }

    @Test
    public void testGetServiceInformation_GetFees_Version130() {
        String expectedValue = "none";

        String fees = this.wmsCapabilities130.getServiceInformation().getFees();

        assertEquals("Service Fees", expectedValue, fees);
    }

    @Test
    public void testGetServiceInformation_GetFees_Version111() {
        String expectedValue = "none";

        String fees = this.wmsCapabilities111.getServiceInformation().getFees();

        assertEquals("Service Fees", expectedValue, fees);
    }

    @Test
    public void testGetServiceInformation_GetAccessConstraints_Version130() {
        String expectedValue = "none";

        String accessConstraints = this.wmsCapabilities130.getServiceInformation().getAccessConstraints();

        assertEquals("Service Fees", expectedValue, accessConstraints);
    }

    @Test
    public void testGetServiceInformation_GetAccessConstraints_Version111() {
        String expectedValue = "none";

        String accessConstraints = this.wmsCapabilities111.getServiceInformation().getAccessConstraints();

        assertEquals("Service Fees", expectedValue, accessConstraints);
    }

    @Test
    public void testGetServiceInformation_GetLayerLimit_Version130() {
        int expectedValue = 16;

        int layerLimit = this.wmsCapabilities130.getServiceInformation().getLayerLimit();

        assertEquals("Service Layer Limit", expectedValue, layerLimit);
    }

    @Test
    public void testGetServiceInformation_GetMaxHeightWidth_Version130() {
        int expectedHeight = 2048;
        int expectedWidth = 2048;

        int maxHeight = this.wmsCapabilities130.getServiceInformation().getMaxHeight();
        int maxWidth = this.wmsCapabilities130.getServiceInformation().getMaxWidth();

        assertEquals("Service Max Height", expectedHeight, maxHeight);
        assertEquals("Service Max Width", expectedWidth, maxWidth);
    }

    @Test
    public void testGetLayerByName_Version130() {
        List<String> layersToTest = Arrays.asList("ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
            "Pressure", "ozone_image", "population");

        for (String layer : layersToTest) {
            WmsLayerCapabilities wmsLayer = this.wmsCapabilities130.getLayerByName(layer);

            assertNotNull("Get Layer By Name " + layer, wmsLayer);
        }
    }

    @Test
    public void testGetLayerByName_Version111() {
        List<String> layersToTest = Arrays.asList("ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
            "Pressure", "ozone_image", "population");

        for (String layer : layersToTest) {
            WmsLayerCapabilities wmsLayer = this.wmsCapabilities111.getLayerByName(layer);

            assertNotNull("Get Layer By Name " + layer, wmsLayer);
        }
    }

    @Test
    public void testGetNamedLayers_Version130() {
        List<String> expectedLayers = Arrays.asList("ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
            "Pressure", "ozone_image", "population");
        int initialSize = expectedLayers.size();

        List<WmsLayerCapabilities> layers = this.wmsCapabilities130.getNamedLayers();
        int foundCount = 0;
        for (WmsLayerCapabilities layer : layers) {
            if (expectedLayers.contains(layer.getName())) {
                foundCount++;
            }
        }

        assertEquals("Get Named Layers Count", initialSize, layers.size());
        assertEquals("Get Named Layers Content", initialSize, foundCount);
    }

    @Test
    public void testGetNamedLayers_Version111() {
        List<String> expectedLayers = Arrays.asList("ROADS_RIVERS", "ROADS_1M", "RIVERS_1M", "Clouds", "Temperature",
            "Pressure", "ozone_image", "population");
        int initialSize = expectedLayers.size();

        List<WmsLayerCapabilities> layers = this.wmsCapabilities111.getNamedLayers();
        int foundCount = 0;
        for (WmsLayerCapabilities layer : layers) {
            if (expectedLayers.contains(layer.getName())) {
                foundCount++;
            }
        }

        assertEquals("Get Named Layers Count", initialSize, layers.size());
        assertEquals("Get Named Layers Content", initialSize, foundCount);
    }

    @Test
    public void testNamedLayerProperties_GetAttribution_Version130() {
        String expectedAttributionTitle = "State College University";
        String expectedAttributionUrl = "http://www.university.edu/";
        String expectedAttributionLogoFormat = "image/gif";
        String expectedAttributionLogoUrl = "http://www.university.edu/icons/logo.gif";
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        WmsLayerAttribution attribution = wmsLayerCapabilities.getAttribution();

        assertEquals("Layer Attributions Title", expectedAttributionTitle, attribution.getTitle());
        assertEquals("Layer Attributions Url", expectedAttributionUrl, attribution.getOnlineResource().getHref());
        assertEquals("Layer Attributions Logo Format", expectedAttributionLogoFormat, attribution.getLogoURL().getFormats().iterator().next());
        assertEquals("Layer Attributions Logo Url", expectedAttributionLogoUrl, attribution.getLogoURL().getOnlineResource().getHref());
    }

    @Test
    public void testNamedLayerProperties_GetAttribution_Version111() {
        String expectedAttributionTitle = "State College University";
        String expectedAttributionUrl = "http://www.university.edu/";
        String expectedAttributionLogoFormat = "image/gif";
        String expectedAttributionLogoUrl = "http://www.university.edu/icons/logo.gif";
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        WmsLayerAttribution attribution = wmsLayerCapabilities.getAttribution();

        assertEquals("Layer Attributions Title", expectedAttributionTitle, attribution.getTitle());
        assertEquals("Layer Attributions Url", expectedAttributionUrl, attribution.getOnlineResource().getHref());
        assertEquals("Layer Attributions Logo Format", expectedAttributionLogoFormat, attribution.getLogoURL().getFormats().iterator().next());
        assertEquals("Layer Attributions Logo Url", expectedAttributionLogoUrl, attribution.getLogoURL().getOnlineResource().getHref());
    }

    @Test
    public void testNamedLayerProperties_GetTitleAbstract_Version130() {
        String expectedTitle = "Roads at 1:1M scale";
        String expectedAbstract = "Roads at a scale of 1 to 1 million.";
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        String title = wmsLayerCapabilities.getTitle();
        String layerAbstract = wmsLayerCapabilities.getLayerAbstract();

        assertEquals("Layer Title", expectedTitle, title);
        assertEquals("Layer Abstract", expectedAbstract, layerAbstract);
    }

    @Test
    public void testNamedLayerProperties_GetTitleAbstract_Version111() {
        String expectedTitle = "Roads at 1:1M scale";
        String expectedAbstract = "Roads at a scale of 1 to 1 million.";
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        String title = wmsLayerCapabilities.getTitle();
        String layerAbstract = wmsLayerCapabilities.getLayerAbstract();

        assertEquals("Layer Title", expectedTitle, title);
        assertEquals("Layer Abstract", expectedAbstract, layerAbstract);
    }

    @Test
    public void testNamedLayerProperties_GetKeywords_Version130() {
        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.addAll(Arrays.asList("road", "transportation", "atlas"));
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Set<String> keywords = wmsLayerCapabilities.getKeywords();

        assertEquals("Layer Keywords", expectedKeywords, keywords);
    }

    @Test
    public void testNamedLayerProperties_GetKeywords_Version111() {
        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.addAll(Arrays.asList("road", "transportation", "atlas"));
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Set<String> keywords = wmsLayerCapabilities.getKeywords();

        assertEquals("Layer Keywords", expectedKeywords, keywords);
    }

    @Test
    public void testNamedLayerProperties_GetIdentities_Version130() {
        int expectedIdentities = 1;
        String expectedAuthority = "DIF_ID";
        String expectedIdentifier = "123456";
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Set<WmsLayerIdentifier> identities = wmsLayerCapabilities.getIdentifiers();
        String authority = identities.iterator().next().getAuthority();
        String identifier = identities.iterator().next().getIdentifier();

        assertEquals("Layer Identifier Count", expectedIdentities, identities.size());
        assertEquals("Layer Authority", expectedAuthority, authority);
        assertEquals("Layer Identifier", expectedIdentifier, identifier);
    }

    @Test
    public void testNamedLayerProperties_GetIdentities_Version111() {
        int expectedIdentities = 1;
        String expectedAuthority = "DIF_ID";
        String expectedIdentifier = "123456";
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Set<WmsLayerIdentifier> identities = wmsLayerCapabilities.getIdentifiers();
        String authority = identities.iterator().next().getAuthority();
        String identifier = identities.iterator().next().getIdentifier();

        assertEquals("Layer Identifier Count", expectedIdentities, identities.size());
        assertEquals("Layer Authority", expectedAuthority, authority);
        assertEquals("Layer Identifier", expectedIdentifier, identifier);
    }

    @Test
    public void testNamedLayerProperties_GetMetadataUrls_Version130() {
        int expectedMetadataUrls = 2;
        List<String> expectedMetadataUrlFormats = Arrays.asList("text/plain", "text/xml");
        List<String> expectedMetadataUrlTypes = Arrays.asList("FGDC:1998", "ISO19115:2003");
        List<String> expectedMetadataUrlUrls = Arrays.asList("http://www.university.edu/metadata/roads.txt",
            "http://www.university.edu/metadata/roads.xml");
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Set<WmsLayerInfoUrl> metadataUrls = wmsLayerCapabilities.getMetadataUrls();

        for (WmsLayerInfoUrl metadataUrl : metadataUrls) {
            assertTrue("Layer MetadataUrl Format", expectedMetadataUrlFormats.contains(metadataUrl.getFormats().iterator().next()));
            assertTrue("Layer MetadataUrl Type", expectedMetadataUrlTypes.contains(metadataUrl.getType()));
            assertTrue("Layer MetadataUrl Url", expectedMetadataUrlUrls.contains(metadataUrl.getOnlineResource().getHref()));
        }
        assertEquals("Layer MetadataUrl Count", expectedMetadataUrls, metadataUrls.size());
    }

    @Test
    public void testNamedLayerProperties_GetMetadataUrls_Version111() {
        int expectedMetadataUrls = 2;
        List<String> expectedMetadataUrlFormats = Arrays.asList("text/plain", "text/xml");
        List<String> expectedMetadataUrlTypes = Arrays.asList("FGDC", "FGDC");
        List<String> expectedMetadataUrlUrls = Arrays.asList("http://www.university.edu/metadata/roads.txt",
            "http://www.university.edu/metadata/roads.xml");
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Set<WmsLayerInfoUrl> metadataUrls = wmsLayerCapabilities.getMetadataUrls();

        for (WmsLayerInfoUrl metadataUrl : metadataUrls) {
            assertTrue("Layer MetadataUrl Format", expectedMetadataUrlFormats.contains(metadataUrl.getFormats().iterator().next()));
            assertTrue("Layer MetadataUrl Names", expectedMetadataUrlTypes.contains(metadataUrl.getType()));
            assertTrue("Layer MetadataUrl Url", expectedMetadataUrlUrls.contains(metadataUrl.getOnlineResource().getHref()));
        }
        assertEquals("Layer MetadataUrl Count", expectedMetadataUrls, metadataUrls.size());
    }

    @Test
    public void testNamedLayerProperties_GetStyles_Version130() {
        int expectedStyles = 2;
        List<String> expectedStyleNames = Arrays.asList("ATLAS", "USGS");
        List<String> expectedStyleTitles = Arrays.asList("Road atlas style", "USGS Topo Map Style");
        List<String> expectedStyleLegendUrl = Arrays.asList("http://www.university.edu/legends/atlas.gif",
            "http://www.university.edu/legends/usgs.gif");
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Set<WmsLayerStyle> styles = wmsLayerCapabilities.getStyles();

        for (WmsLayerStyle style : styles) {
            assertTrue("Layer Style Names", expectedStyleNames.contains(style.getName()));
            assertTrue("Layer Style Titles", expectedStyleTitles.contains(style.getTitle()));
            String legendUrl = style.getLegendUrls().iterator().next().getOnlineResource().getHref();
            assertTrue("Layer Style Legend Url", expectedStyleLegendUrl.contains(legendUrl));
        }
        assertEquals("Layer Style Count", expectedStyles, styles.size());
    }

    @Test
    public void testNamedLayerProperties_GetStyles_Version111() {
        int expectedStyles = 2;
        List<String> expectedStyleNames = Arrays.asList("ATLAS", "USGS");
        List<String> expectedStyleTitles = Arrays.asList("Road atlas style", "USGS Topo Map Style");
        List<String> expectedStyleLegendUrl = Arrays.asList("http://www.university.edu/legends/atlas.gif",
            "http://www.university.edu/legends/usgs.gif");
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Set<WmsLayerStyle> styles = wmsLayerCapabilities.getStyles();

        for (WmsLayerStyle style : styles) {
            assertTrue("Layer Style Names", expectedStyleNames.contains(style.getName()));
            assertTrue("Layer Style Titles", expectedStyleTitles.contains(style.getTitle()));
            String legendUrl = style.getLegendUrls().iterator().next().getOnlineResource().getHref();
            assertTrue("Layer Style Legend Url", expectedStyleLegendUrl.contains(legendUrl));
        }
        assertEquals("Layer Style Count", expectedStyles, styles.size());
    }

    @Test
    public void testNamedLayerProperties_GetReferenceSystems_Version130() {
        Set<String> expectedCrsValues = new HashSet<>();
        expectedCrsValues.addAll(Arrays.asList("EPSG:26986", "CRS:84"));
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Set<String> referenceSystems = wmsLayerCapabilities.getReferenceSystem();

        assertEquals("Layer Reference System", expectedCrsValues, referenceSystems);
    }

    @Test
    public void testNamedLayerProperties_GetReferenceSystems_Version111() {
        Set<String> expectedSrsValues = new HashSet<>();
        expectedSrsValues.addAll(Arrays.asList("EPSG:26986", "EPSG:4326"));
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Set<String> referenceSystems = wmsLayerCapabilities.getReferenceSystem();

        assertEquals("Layer Reference System", expectedSrsValues, referenceSystems);
    }

    @Test
    public void testNamedLayerProperties_GetGeographicBoundingBox_Version130() {
        double expectedGeographicBoundingBoxWestLong = -71.63;
        double expectedGeographicBoundingBoxEastLong = -70.78;
        double expectedGeographicBoundingBoxSouthLat = 41.75;
        double expectedGeographicBoundingBoxNorthLat = 42.90;
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Sector sector = wmsLayerCapabilities.getGeographicBoundingBox();

        assertEquals("Layer Geographic Bounding Box West", expectedGeographicBoundingBoxWestLong, sector.minLongitude());
        assertEquals("Layer Geographic Bounding Box East", expectedGeographicBoundingBoxEastLong, sector.maxLongitude());
        assertEquals("Layer Geographic Bounding Box North", expectedGeographicBoundingBoxNorthLat, sector.maxLatitude());
        assertEquals("Layer Geographic Bounding Box South", expectedGeographicBoundingBoxSouthLat, sector.minLatitude());
    }

    @Test
    public void testNamedLayerProperties_GetGeographicBoundingBox_Version111() {
        double expectedGeographicBoundingBoxWestLong = -71.63;
        double expectedGeographicBoundingBoxEastLong = -70.78;
        double expectedGeographicBoundingBoxSouthLat = 41.75;
        double expectedGeographicBoundingBoxNorthLat = 42.90;
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Sector sector = wmsLayerCapabilities.getGeographicBoundingBox();

        assertEquals("Layer Geographic Bounding Box West", expectedGeographicBoundingBoxWestLong, sector.minLongitude());
        assertEquals("Layer Geographic Bounding Box East", expectedGeographicBoundingBoxEastLong, sector.maxLongitude());
        assertEquals("Layer Geographic Bounding Box North", expectedGeographicBoundingBoxNorthLat, sector.maxLatitude());
        assertEquals("Layer Geographic Bounding Box South", expectedGeographicBoundingBoxSouthLat, sector.minLatitude());
    }

    @Test
    public void testNamedLayerProperties_GetBoundingBox_Version130() {
        double expectedCrs84BoundingBoxMinx = -71.63;
        double expectedCrs84BoundingBoxMiny = 41.75;
        double expectedCrs84BoundingBoxMaxx = -70.78;
        double expectedCrs84BoundingBoxMaxy = 42.90;
        double expectedEpsgBoundingBoxMinx = 189000;
        double expectedEpsgBoundingBoxMiny = 834000;
        double expectedEpsgBoundingBoxMaxx = 285000;
        double expectedEpsgBoundingBoxMaxy = 962000;
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        Set<WmsBoundingBox> boxes = wmsLayerCapabilities.getBoundingBoxes();
        Iterator<WmsBoundingBox> boxIterator = boxes.iterator();

        while (boxIterator.hasNext()) {
            WmsBoundingBox box = boxIterator.next();
            double minx = box.getMinx();
            double miny = box.getMiny();
            double maxx = box.getMaxx();
            double maxy = box.getMaxy();
            if (box.getCRS().equals("CRS:84")) {
                assertEquals("Layer Bounding Box CRS:84 Minx", expectedCrs84BoundingBoxMinx, minx);
                assertEquals("Layer Bounding Box CRS:84 Miny", expectedCrs84BoundingBoxMiny, miny);
                assertEquals("Layer Bounding Box CRS:84 Maxx", expectedCrs84BoundingBoxMaxx, maxx);
                assertEquals("Layer Bounding Box CRS:84 Maxy", expectedCrs84BoundingBoxMaxy, maxy);
            } else if (box.getCRS().equals("EPSG:26986")) {
                assertEquals("Layer Bounding Box EPSG:26986 Minx", expectedEpsgBoundingBoxMinx, minx);
                assertEquals("Layer Bounding Box EPSG:26986 Miny", expectedEpsgBoundingBoxMiny, miny);
                assertEquals("Layer Bounding Box EPSG:26986 Maxx", expectedEpsgBoundingBoxMaxx, maxx);
                assertEquals("Layer Bounding Box EPSG:26986 Maxy", expectedEpsgBoundingBoxMaxy, maxy);
            } else {
                fail("Unexpected Layer Coordinate System");
            }
        }
        assertEquals("Layer Bounding Box Count", 2, boxes.size());
    }

    @Test
    public void testNamedLayerProperties_GetBoundingBox_Version111() {
        double expectedEpsg4326BoundingBoxMinx = -71.63;
        double expectedEpsg4326BoundingBoxMiny = 41.75;
        double expectedEpsg4326BoundingBoxMaxx = -70.78;
        double expectedEpsg4326BoundingBoxMaxy = 42.90;
        double expectedEpsgBoundingBoxMinx = 189000;
        double expectedEpsgBoundingBoxMiny = 834000;
        double expectedEpsgBoundingBoxMaxx = 285000;
        double expectedEpsgBoundingBoxMaxy = 962000;
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");

        Set<WmsBoundingBox> boxes = wmsLayerCapabilities.getBoundingBoxes();
        Iterator<WmsBoundingBox> boxIterator = boxes.iterator();

        while (boxIterator.hasNext()) {
            WmsBoundingBox box = boxIterator.next();
            double minx = box.getMinx();
            double miny = box.getMiny();
            double maxx = box.getMaxx();
            double maxy = box.getMaxy();
            if (box.getCRS().equals("EPSG:4326")) {
                assertEquals("Layer Bounding Box CRS:84 Minx", expectedEpsg4326BoundingBoxMinx, minx);
                assertEquals("Layer Bounding Box CRS:84 Miny", expectedEpsg4326BoundingBoxMiny, miny);
                assertEquals("Layer Bounding Box CRS:84 Maxx", expectedEpsg4326BoundingBoxMaxx, maxx);
                assertEquals("Layer Bounding Box CRS:84 Maxy", expectedEpsg4326BoundingBoxMaxy, maxy);
            } else if (box.getCRS().equals("EPSG:26986")) {
                assertEquals("Layer Bounding Box EPSG:26986 Minx", expectedEpsgBoundingBoxMinx, minx);
                assertEquals("Layer Bounding Box EPSG:26986 Miny", expectedEpsgBoundingBoxMiny, miny);
                assertEquals("Layer Bounding Box EPSG:26986 Maxx", expectedEpsgBoundingBoxMaxx, maxx);
                assertEquals("Layer Bounding Box EPSG:26986 Maxy", expectedEpsgBoundingBoxMaxy, maxy);
            } else {
                fail("Unexpected Layer Coordinate System");
            }
        }
        assertEquals("Layer Bounding Box Count", 2, boxes.size());
    }

    @Test
    public void testServiceCapabilities() {
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities130.getLayerByName("ROADS_1M");

        WmsCapabilities wmsCapabilities = wmsLayerCapabilities.getServiceCapabilities();

        assertEquals("Layer Service Capabilities", this.wmsCapabilities130, wmsCapabilities);
    }

    @Test
    public void testNullMaxWidthHeightAndLimit_Version111() {
        // NOTE: Version 1.1.1 doesn't support the MaxHeight, MaxWidth, and LayerLimit elements, but this test is
        // to verify that null values are returned properly. This doesn't work with our specification resource for
        // 1.3.0 as the values are included and tested above
        WmsServiceInformation wmsServiceInformation = this.wmsCapabilities111.getServiceInformation();

        Integer maxHeight = wmsServiceInformation.getMaxHeight();
        Integer maxWidth = wmsServiceInformation.getMaxWidth();
        Integer layerLimit = wmsServiceInformation.getLayerLimit();

        assertNull("MaxHeight v1.1.1", maxHeight);
        assertNull("MaxWidth v1.1.1", maxWidth);
        assertNull("LayerLimit v1.1.1", layerLimit);
    }

    @Test
    public void testScaleHint_Version111() {
        WmsLayerCapabilities wmsLayerCapabilities = this.wmsCapabilities111.getLayerByName("ROADS_1M");
        double expectedMinScaleHint = 4000;
        double expectedMaxScaleHint = 35000;

        Double minScaleHint = wmsLayerCapabilities.getMinScaleHint();
        Double maxScaleHint = wmsLayerCapabilities.getMaxScaleHint();

        assertEquals("Min Scale Hint", expectedMinScaleHint, minScaleHint.doubleValue(), DELTA);
        assertEquals("Max Scale Hint", expectedMaxScaleHint, maxScaleHint.doubleValue(), DELTA);
    }

    @Test
    public void testGetCapabilitiesURL_Version130() {
        WmsRequestOperation getCapabilities = this.wmsCapabilities130.getCapabilityInformation().getCapabilitiesInfo();
        String expectedUrl = "http://hostname/path?";

        String url = getCapabilities.getOnlineResource("Get").getHref();

        assertEquals("GetCapabilities URL", expectedUrl, url);
    }

    @Test
    public void testGetCapabilitiesURL_Version111() {
        WmsRequestOperation getCapabilities = this.wmsCapabilities111.getCapabilityInformation().getCapabilitiesInfo();
        String expectedUrl = "http://hostname:port/path";

        String url = getCapabilities.getOnlineResource("Get").getHref();

        assertEquals("GetCapabilities URL", expectedUrl, url);
    }

    @Test
    public void testGetMapURL_Version130() {
        WmsRequestOperation getMap = this.wmsCapabilities130.getCapabilityInformation().getMapInfo();
        String expectedUrl = "http://hostname/path?";

        String url = getMap.getOnlineResource("Get").getHref();

        assertEquals("GetMap URL", expectedUrl, url);
    }

    @Test
    public void testGetMapURL_Version111() {
        WmsRequestOperation getMap = this.wmsCapabilities111.getCapabilityInformation().getMapInfo();
        String expectedUrl = "http://hostname:port/path";

        String url = getMap.getOnlineResource("Get").getHref();

        assertEquals("GetMap URL", expectedUrl, url);
    }

    @Test
    public void testGetFeatureInfoURL_Version130() {
        WmsRequestOperation getFeatureInfo = this.wmsCapabilities130.getCapabilityInformation().getFeatureInfo();
        String expectedUrl = "http://hostname/path?";

        String url = getFeatureInfo.getOnlineResource("Get").getHref();

        assertEquals("GetFeatureInfo URL", expectedUrl, url);
    }

    @Test
    public void testGetFeatureInfoURL_Version111() {
        WmsRequestOperation getFeatureInfo = this.wmsCapabilities111.getCapabilityInformation().getFeatureInfo();
        String expectedUrl = "http://hostname:port/path";

        String url = getFeatureInfo.getOnlineResource("Get").getHref();

        assertEquals("GetFeatureInfo URL", expectedUrl, url);
    }
}
