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
import java.util.Iterator;
import java.util.Set;

import gov.nasa.worldwind.test.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmtsCapabilitiesTest {

    protected static final double DELTA = 1e-9;

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

    @Test
    public void testGetOperationsMetadata_GetCapabilities() throws Exception {
        OwsOperation getCapabilities = this.wmtsCapabilities.operationsMetadata.getCapabilities;
        String expectedName = "GetCapabilities";
        String expectedLink = "http://www.opengis.uab.es/cgi-bin/world/MiraMon5_0.cgi?";

        String actualName = getCapabilities.name;
        String actualLink = getCapabilities.dcp.getHref;

        assertEquals("Operations Metadata GetCapabilities Name", expectedName, actualName);
        assertEquals("Operations Metadata GetCapabilities Link", expectedLink, actualLink);
    }

    @Test
    public void testGetOperationsMetadata_GetTile() throws Exception {
        OwsOperation getTile = this.wmtsCapabilities.operationsMetadata.getTile;
        String expectedName = "GetTile";
        String expectedLink = "http://www.opengis.uab.es/cgi-bin/world/MiraMon5_0.cgi?";

        String actualName = getTile.name;
        String actualLink = getTile.dcp.getHref;

        assertEquals("Operations Metadata GetTile Name", expectedName, actualName);
        assertEquals("Operations Metadata GetTile Link", expectedLink, actualLink);
    }

    @Test
    public void testGetLayer_Title() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedTitleOne = "etopo2";
        String expectedTitleTwo = "Administrative Boundaries";
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        String actualTitleOne = layerIterator.next().title;
        String actualTitleTwo = layerIterator.next().title;

        assertEquals("Layer Title One", expectedTitleOne, actualTitleOne);
        assertEquals("Layer Title Two", expectedTitleTwo, actualTitleTwo);
    }

    @Test
    public void testGetLayer_Abstract() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedInAbstractOne = "1. The seafloor data between latitudes 64— North and 72— South";
        String expectedInAbstractTwo = " at scales to about 1:10,000,000. The data were ge";
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        String actualAbstractOne = layerIterator.next().layerAbstract;
        String actualAbstractTwo = layerIterator.next().layerAbstract;

        assertTrue("Layer Title One", actualAbstractOne.contains(expectedInAbstractOne));
        assertTrue("Layer Title Two", actualAbstractTwo.contains(expectedInAbstractTwo));
    }

    @Test
    public void testGetLayer_WGS84BoundingBox() throws Exception {
        Set<WmtsLayer> layers = this.wmtsCapabilities.layers;
        double expectedMinXOne = -180;
        double expectedMaxXOne = 180;
        double expectedMinYOne = -90;
        double expectedMaxYOne = 90;
        double expectedMinXTwo = -180;
        double expectedMaxXTwo = 180;
        double expectedMinYTwo = -90;
        double expectedMaxYTwo = 84;
        Iterator<WmtsLayer> layerIterator = layers.iterator();

        WmtsLayer layer = layerIterator.next();
        double actualMinXOne = layer.boundingBox.minx;
        double actualMaxXOne = layer.boundingBox.maxx;
        double actualMinYOne = layer.boundingBox.miny;
        double actualMaxYOne = layer.boundingBox.maxy;
        layer = layerIterator.next();
        double actualMinXTwo = layer.boundingBox.minx;
        double actualMaxXTwo = layer.boundingBox.maxx;
        double actualMinYTwo = layer.boundingBox.miny;
        double actualMaxYTwo = layer.boundingBox.maxy;

        assertEquals("Layer Bounding Box MinX Layer One", expectedMinXOne, actualMinXOne, DELTA);
        assertEquals("Layer Bounding Box MaxX Layer One", expectedMaxXOne, actualMaxXOne, DELTA);
        assertEquals("Layer Bounding Box MinY Layer One", expectedMinYOne, actualMinYOne, DELTA);
        assertEquals("Layer Bounding Box MaxY Layer One", expectedMaxYOne, actualMaxYOne, DELTA);
        assertEquals("Layer Bounding Box MinX Layer Two", expectedMinXTwo, actualMinXTwo, DELTA);
        assertEquals("Layer Bounding Box MaxX Layer Two", expectedMaxXTwo, actualMaxXTwo, DELTA);
        assertEquals("Layer Bounding Box MinY Layer Two", expectedMinYTwo, actualMinYTwo, DELTA);
        assertEquals("Layer Bounding Box MaxY Layer Two", expectedMaxYTwo, actualMaxYTwo, DELTA);
    }

    @Test
    public void testGetLayer_Identifier() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedIdentifierOne = "etopo2";
        String expectedIdentifierTwo = "AdminBoundaries";
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        String actualIdentifierOne = layerIterator.next().identifier;
        String actualIdentifierTwo = layerIterator.next().identifier;

        assertEquals("Layer Identifier One", expectedIdentifierOne, actualIdentifierOne);
        assertEquals("Layer Identifier Two", expectedIdentifierTwo, actualIdentifierTwo);
    }

    @Test
    public void testGetLayer_Metadata() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedHrefOne = "http://www.opengis.uab.es/SITiled/world/etopo2/metadata.htm";
        String expectedHrefTwo = "http://www.opengis.uab.es/SITiled/world/AdminBoundaries/metadata.htm";
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        String actualHrefOne = layerIterator.next().metadata.iterator().next().href;
        String actualHrefTwo = layerIterator.next().metadata.iterator().next().href;

        assertEquals("Layer Metadata Href One", expectedHrefOne, actualHrefOne);
        assertEquals("Layer Metadata Href Two", expectedHrefTwo, actualHrefTwo);
    }

    @Test
    public void testGetLayer_Styles() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedTitleOne = "default";
        String expectedTitleTwo = "default";
        String expectedIdentifierOne = "default";
        String expectedIdentifierTwo = "default";
        boolean expectedIsDefaultOne = true;
        boolean expectedIsDefaultTwo = true;
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        String actualTitleOne = layerIterator.next().styles.iterator().next().title;
        String actualTitleTwo = layerIterator.next().styles.iterator().next().title;
        layerIterator = layer.iterator();
        String actualIdentifierOne = layerIterator.next().styles.iterator().next().identifier;
        String actualIdentifierTwo = layerIterator.next().styles.iterator().next().identifier;
        layerIterator = layer.iterator();
        boolean actualIsDefaultOne = layerIterator.next().styles.iterator().next().isDefault;
        boolean actualIsDefaultTwo = layerIterator.next().styles.iterator().next().isDefault;

        assertEquals("Layer Style Title One", expectedTitleOne, actualTitleOne);
        assertEquals("Layer Style Title Two", expectedTitleTwo, actualTitleTwo);
        assertEquals("Layer Style Identifier One", expectedIdentifierOne, actualIdentifierOne);
        assertEquals("Layer Style Identifier Two", expectedIdentifierTwo, actualIdentifierTwo);
        assertEquals("Layer Style IsDefault One", expectedIsDefaultOne, actualIsDefaultOne);
        assertEquals("Layer Style IsDefault Two", expectedIsDefaultTwo, actualIsDefaultTwo);
    }

    @Test
    public void testGetLayer_Formats() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedFormatOne = "image/png";
        String expectedFormatTwo = "image/png";
        int expectedFormatSizeOne = 1;
        int expectedFormatSizeTwo = 1;
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        Set<String> actualFormatsOne = layerIterator.next().formats;
        String actualFormatOne = actualFormatsOne.iterator().next();
        int actualFormatSizeOne = actualFormatsOne.size();
        Set<String> actualFormatsTwo = layerIterator.next().formats;
        String actualFormatTwo = actualFormatsTwo.iterator().next();
        int actualFormatSizeTwo = actualFormatsTwo.size();

        assertEquals("Layer Format One", expectedFormatOne, actualFormatOne);
        assertEquals("Layer Formats Size One", expectedFormatSizeOne, actualFormatSizeOne);
        assertEquals("Layer Format Two", expectedFormatTwo, actualFormatTwo);
        assertEquals("Layer Formats Size Two", expectedFormatSizeTwo, actualFormatSizeTwo);
    }

    @Test
    public void testGetLayer_TileMatrixSets() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedTileMatrixSetOne = "WholeWorld_CRS_84";
        String expectedTileMatrixSetTwo = "World84-90_CRS_84";
        int expectedTileMatrixSetSizeOne = 1;
        int expectedTileMatrixSetSizeTwo = 1;
        Iterator<WmtsLayer> layerIterator = layer.iterator();

        Set<String> actualTileMatrixSetsOne = layerIterator.next().tileMatrixSetIds;
        String actualTileMatrixSetOne = actualTileMatrixSetsOne.iterator().next();
        int actualTileMatrixSetSizeOne = actualTileMatrixSetsOne.size();
        Set<String> actualTileMatrixSetsTwo = layerIterator.next().tileMatrixSetIds;
        String actualTileMatrixSetTwo = actualTileMatrixSetsTwo.iterator().next();
        int actualTileMatrixSetSizeTwo = actualTileMatrixSetsTwo.size();

        assertEquals("Layer TileMatrixSet One", expectedTileMatrixSetOne, actualTileMatrixSetOne);
        assertEquals("Layer TileMatrixSets Size One", expectedTileMatrixSetSizeOne, actualTileMatrixSetSizeOne);
        assertEquals("Layer TileMatrixSet Two", expectedTileMatrixSetTwo, actualTileMatrixSetTwo);
        assertEquals("Layer TileMatrixSets Size Two", expectedTileMatrixSetSizeTwo, actualTileMatrixSetSizeTwo);
    }

    @Test
    public void testGetLayer_ResourceURLs_One() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedResourceUrlFormatOne = "image/png";
        String expectedResourceUrlFormatTwo = "application/gml+xml; version=3.1";
        String expectedResourceUrlResourceTypeOne = "tile";
        String expectedResourceUrlResourceTypeTwo = "FeatureInfo";
        String expectedResourceUrlTemplateOne = "http://www.opengis.uab.es/SITiled/world/etopo2/default/WholeWorld_CRS_84/{TileMatrix}/{TileRow}/{TileCol}.png";
        String expectedResourceUrlTemplateTwo = "http://www.opengis.uab.es/SITiled/world/etopo2/default/WholeWorld_CRS_84/{TileMatrix}/{TileRow}/{TileCol}/{J}/{I}.xml";
        Iterator<WmtsResourceUrl> resourceUrls = layer.iterator().next().resourceUrls.iterator();

        WmtsResourceUrl resourceOne = resourceUrls.next();
        String actualResourceUrlFormatOne = resourceOne.format;
        String actualResourceUrlResourceTypeOne = resourceOne.resourceType;
        String actualResourceUrlTemplateOne = resourceOne.template;
        WmtsResourceUrl resourceTwo = resourceUrls.next();
        String actualResourceUrlFormatTwo = resourceTwo.format;
        String actualResourceUrlResourceTypeTwo = resourceTwo.resourceType;
        String actualResourceUrlTemplateTwo = resourceTwo.template;

        assertEquals("Layer One ResourceURL One Format", expectedResourceUrlFormatOne, actualResourceUrlFormatOne);
        assertEquals("Layer One ResourceURL One ResourceType", expectedResourceUrlResourceTypeOne, actualResourceUrlResourceTypeOne);
        assertEquals("Layer One ResourceURL One Template", expectedResourceUrlTemplateOne, actualResourceUrlTemplateOne);
        assertEquals("Layer One ResourceURL Two Format", expectedResourceUrlFormatTwo, actualResourceUrlFormatTwo);
        assertEquals("Layer One ResourceURL Two ResourceType", expectedResourceUrlResourceTypeTwo, actualResourceUrlResourceTypeTwo);
        assertEquals("Layer One ResourceURL Two Template", expectedResourceUrlTemplateTwo, actualResourceUrlTemplateTwo);
    }

    @Test
    public void testGetLayer_ResourceURLs_Two() throws Exception {
        Set<WmtsLayer> layer = this.wmtsCapabilities.layers;
        String expectedResourceUrlFormatOne = "image/png";
        String expectedResourceUrlFormatTwo = "application/gml+xml; version=3.1";
        String expectedResourceUrlResourceTypeOne = "tile";
        String expectedResourceUrlResourceTypeTwo = "FeatureInfo";
        String expectedResourceUrlTemplateOne = "http://www.opengis.uab.es/SITiled/world/AdminBoundaries/default/World84-90_CRS_84/{TileMatrix}/{TileRow}/{TileCol}.png";
        String expectedResourceUrlTemplateTwo = "http://www.opengis.uab.es/SITiled/world/AdminBoundaries/default/World84-90_CRS_84/{TileMatrix}/{TileRow}/{TileCol}/{J}/{I}.xml";
        Iterator<WmtsLayer> layerIterator = layer.iterator();
        layerIterator.next();
        Iterator<WmtsResourceUrl> resourceUrls = layerIterator.next().resourceUrls.iterator();

        WmtsResourceUrl resourceOne = resourceUrls.next();
        String actualResourceUrlFormatOne = resourceOne.format;
        String actualResourceUrlResourceTypeOne = resourceOne.resourceType;
        String actualResourceUrlTemplateOne = resourceOne.template;
        WmtsResourceUrl resourceTwo = resourceUrls.next();
        String actualResourceUrlFormatTwo = resourceTwo.format;
        String actualResourceUrlResourceTypeTwo = resourceTwo.resourceType;
        String actualResourceUrlTemplateTwo = resourceTwo.template;

        assertEquals("Layer Two ResourceURL One Format", expectedResourceUrlFormatOne, actualResourceUrlFormatOne);
        assertEquals("Layer Two ResourceURL One ResourceType", expectedResourceUrlResourceTypeOne, actualResourceUrlResourceTypeOne);
        assertEquals("Layer Two ResourceURL One Template", expectedResourceUrlTemplateOne, actualResourceUrlTemplateOne);
        assertEquals("Layer Two ResourceURL Two Format", expectedResourceUrlFormatTwo, actualResourceUrlFormatTwo);
        assertEquals("Layer Two ResourceURL Two ResourceType", expectedResourceUrlResourceTypeTwo, actualResourceUrlResourceTypeTwo);
        assertEquals("Layer Two ResourceURL Two Template", expectedResourceUrlTemplateTwo, actualResourceUrlTemplateTwo);
    }
}
