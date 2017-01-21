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
import java.util.Iterator;
import java.util.List;

import gov.nasa.worldwind.test.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
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
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        String expected = "World example Web Map Tile Service";

        String actual = serviceIdentification.getTitle();

        assertEquals("Service Identification Title", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_Abstract() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        String expected = "Example service that contrains some world layers in the" +
            "            urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Pixel Well-known scale set";

        String actual = serviceIdentification.getServiceAbstract();

        assertEquals("Service Identification Abstract", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_Keywords() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        List<String> expected = Arrays.asList("World", "Global", "Digital Elevation Model", "Administrative Boundaries");

        List<String> actual = serviceIdentification.getKeywords();

        assertEquals("Service Identification Keywords", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_ServiceType() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        String expected = "OGC WMTS";

        String actual = serviceIdentification.getServiceType();

        assertEquals("Service Identification Type", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_ServiceTypeVersion() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        String expected = "1.0.0";

        String actual = serviceIdentification.getServiceTypeVersion();

        assertEquals("Service Identification Type Version", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_Fees() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        String expected = "none";

        String actual = serviceIdentification.getFees();

        assertEquals("Service Identification Fees", expected, actual);
    }

    @Test
    public void testGetServiceIdentification_AccessConstraints() throws Exception {
        OwsServiceIdentification serviceIdentification = this.wmtsCapabilities.getServiceIdentification();
        String expected = "none";

        String actual = serviceIdentification.getAccessConstraints();

        assertEquals("Service Identification Access Constraints", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Name() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.getServiceProvider();
        String expected = "UAB-CREAF-MiraMon";

        String actual = serviceProvider.getProviderName();

        assertEquals("Service Provider Name", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Site() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.getServiceProvider();
        String expected = "http://www.creaf.uab.es/miramon";

        String actual = serviceProvider.getSiteHref();

        assertEquals("Service Provider Site Link", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Contact_Name() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.getServiceProvider();
        String expected = "Joan Maso Pau";

        String actual = serviceProvider.getServiceContact().getIndividualName();

        assertEquals("Service Provider Contact Individual Name", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Contact_Position() throws Exception {
        OwsServiceProvider serviceProvider = this.wmtsCapabilities.getServiceProvider();
        String expected = "Senior Software Engineer";

        String actual = serviceProvider.getServiceContact().getPositionName();

        assertEquals("Service Provider Contact Position Name", expected, actual);
    }

    @Test
    public void testGetServiceProvider_Contact_InfoPhone() throws Exception {
        OwsContactInfo contactInfo = this.wmtsCapabilities.getServiceProvider().getServiceContact().getContactInfo();
        String expectedVoice = "+34 93 581 1312";
        String expectedFax = "+34 93 581 4151";

        String actualVoice = contactInfo.getPhone().getVoice();
        String actualFax = contactInfo.getPhone().getFax();

        assertEquals("Service Provider Contact Phone Voice", expectedVoice, actualVoice);
        assertEquals("Service Provider Contact Phone Fax", expectedFax, actualFax);
    }

    @Test
    public void testGetServiceProvider_Contact_InfoAddress() throws Exception {
        OwsContactInfo contactInfo = this.wmtsCapabilities.getServiceProvider().getServiceContact().getContactInfo();
        String expectedDeliveryPoint = "Fac Ciencies UAB";
        String expectedCity = "Bellaterra";
        String expectedAdministrativeArea = "Barcelona";
        String expectedPostalCode = "08193";
        String expectedCountry = "Spain";
        String expectedEmail = "joan.maso@uab.es";

        String actualDeliveryPoint = contactInfo.getAddress().getDeliveryPoint();
        String actualCity = contactInfo.getAddress().getCity();
        String actualAdministrativeArea = contactInfo.getAddress().getAdministrativeArea();
        String actualPostalCode = contactInfo.getAddress().getPostalCode();
        String actualCountry = contactInfo.getAddress().getCountry();
        String actualEmail = contactInfo.getAddress().getEmail();

        assertEquals("Service Provider Contact Address Delivery Point", expectedDeliveryPoint, actualDeliveryPoint);
        assertEquals("Service Provider Contact Address City", expectedCity, actualCity);
        assertEquals("Service Provider Contact Address Admin Area", expectedAdministrativeArea, actualAdministrativeArea);
        assertEquals("Service Provider Contact Address Postal Code", expectedPostalCode, actualPostalCode);
        assertEquals("Service Provider Contact Address Country", expectedCountry, actualCountry);
        assertEquals("Service Provider Contact Address Email", expectedEmail, actualEmail);
    }

    @Test
    public void testGetOperationsMetadata_GetCapabilities() throws Exception {
        OwsOperation getCapabilities = this.wmtsCapabilities.getOperationsMetadata().getGetCapabilities();
        String expectedName = "GetCapabilities";
        String expectedLink = "http://www.opengis.uab.es/cgi-bin/world/MiraMon5_0.cgi?";

        String actualName = getCapabilities.getName();
        String actualLink = getCapabilities.getDcp().getGetHref();

        assertEquals("Operations Metadata GetCapabilities Name", expectedName, actualName);
        assertEquals("Operations Metadata GetCapabilities Link", expectedLink, actualLink);
    }

    @Test
    public void testGetOperationsMetadata_GetTile() throws Exception {
        OwsOperation getTile = this.wmtsCapabilities.getOperationsMetadata().getGetTile();
        String expectedName = "GetTile";
        String expectedLink = "http://www.opengis.uab.es/cgi-bin/world/MiraMon5_0.cgi?";

        String actualName = getTile.getName();
        String actualLink = getTile.getDcp().getGetHref();

        assertEquals("Operations Metadata GetTile Name", expectedName, actualName);
        assertEquals("Operations Metadata GetTile Link", expectedLink, actualLink);
    }

    @Test
    public void testGetLayer_Title() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedTitleOne = "etopo2";
        String expectedTitleTwo = "Administrative Boundaries";

        String actualTitleOne = layer.get(0).getTitle();
        String actualTitleTwo = layer.get(1).getTitle();

        assertEquals("Layer Title One", expectedTitleOne, actualTitleOne);
        assertEquals("Layer Title Two", expectedTitleTwo, actualTitleTwo);
    }

    @Test
    public void testGetLayer_Abstract() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedInAbstractOne = "1. The seafloor data between latitudes 64— North and 72— South";
        String expectedInAbstractTwo = " at scales to about 1:10,000,000. The data were ge";

        String actualAbstractOne = layer.get(0).getLayerAbstract();
        String actualAbstractTwo = layer.get(1).getLayerAbstract();

        assertTrue("Layer Title One", actualAbstractOne.contains(expectedInAbstractOne));
        assertTrue("Layer Title Two", actualAbstractTwo.contains(expectedInAbstractTwo));
    }

    @Test
    public void testGetLayer_WGS84BoundingBox() throws Exception {
        List<WmtsLayer> layers = this.wmtsCapabilities.getLayers();
        double expectedMinXOne = -180;
        double expectedMaxXOne = 180;
        double expectedMinYOne = -90;
        double expectedMaxYOne = 90;
        double expectedMinXTwo = -180;
        double expectedMaxXTwo = 180;
        double expectedMinYTwo = -90;
        double expectedMaxYTwo = 84;

        WmtsLayer layer = layers.get(0);
        double actualMinXOne = layer.getBoundingBox().getMinX();
        double actualMaxXOne = layer.getBoundingBox().getMaxX();
        double actualMinYOne = layer.getBoundingBox().getMinY();
        double actualMaxYOne = layer.getBoundingBox().getMaxY();
        layer = layers.get(1);
        double actualMinXTwo = layer.getBoundingBox().getMinX();
        double actualMaxXTwo = layer.getBoundingBox().getMaxX();
        double actualMinYTwo = layer.getBoundingBox().getMinY();
        double actualMaxYTwo = layer.getBoundingBox().getMaxY();

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
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedIdentifierOne = "etopo2";
        String expectedIdentifierTwo = "AdminBoundaries";

        String actualIdentifierOne = layer.get(0).getIdentifier();
        String actualIdentifierTwo = layer.get(1).getIdentifier();

        assertEquals("Layer Identifier One", expectedIdentifierOne, actualIdentifierOne);
        assertEquals("Layer Identifier Two", expectedIdentifierTwo, actualIdentifierTwo);
    }

    @Test
    public void testGetLayer_Metadata() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedHrefOne = "http://www.opengis.uab.es/SITiled/world/etopo2/metadata.htm";
        String expectedHrefTwo = "http://www.opengis.uab.es/SITiled/world/AdminBoundaries/metadata.htm";

        String actualHrefOne = layer.get(0).getMetadata().get(0).getHref();
        String actualHrefTwo = layer.get(1).getMetadata().get(0).getHref();

        assertEquals("Layer Metadata Href One", expectedHrefOne, actualHrefOne);
        assertEquals("Layer Metadata Href Two", expectedHrefTwo, actualHrefTwo);
    }

    @Test
    public void testGetLayer_Styles() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedTitleOne = "default";
        String expectedTitleTwo = "default";
        String expectedIdentifierOne = "default";
        String expectedIdentifierTwo = "default";
        boolean expectedIsDefaultOne = true;
        boolean expectedIsDefaultTwo = true;

        String actualTitleOne = layer.get(0).getStyles().get(0).getTitle();
        String actualTitleTwo = layer.get(1).getStyles().get(0).getTitle();
        String actualIdentifierOne = layer.get(0).getStyles().get(0).getIdentifier();
        String actualIdentifierTwo = layer.get(1).getStyles().get(0).getIdentifier();
        boolean actualIsDefaultOne = layer.get(0).getStyles().get(0).isDefault();
        boolean actualIsDefaultTwo = layer.get(0).getStyles().get(0).isDefault();

        assertEquals("Layer Style Title One", expectedTitleOne, actualTitleOne);
        assertEquals("Layer Style Title Two", expectedTitleTwo, actualTitleTwo);
        assertEquals("Layer Style Identifier One", expectedIdentifierOne, actualIdentifierOne);
        assertEquals("Layer Style Identifier Two", expectedIdentifierTwo, actualIdentifierTwo);
        assertEquals("Layer Style IsDefault One", expectedIsDefaultOne, actualIsDefaultOne);
        assertEquals("Layer Style IsDefault Two", expectedIsDefaultTwo, actualIsDefaultTwo);
    }

    @Test
    public void testGetLayer_Formats() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedFormatOne = "image/png";
        String expectedFormatTwo = "image/png";
        int expectedFormatSizeOne = 1;
        int expectedFormatSizeTwo = 1;

        List<String> actualFormatsOne = layer.get(0).getFormats();
        String actualFormatOne = actualFormatsOne.get(0);
        int actualFormatSizeOne = actualFormatsOne.size();
        List<String> actualFormatsTwo = layer.get(1).getFormats();
        String actualFormatTwo = actualFormatsTwo.iterator().next();
        int actualFormatSizeTwo = actualFormatsTwo.size();

        assertEquals("Layer Format One", expectedFormatOne, actualFormatOne);
        assertEquals("Layer Formats Size One", expectedFormatSizeOne, actualFormatSizeOne);
        assertEquals("Layer Format Two", expectedFormatTwo, actualFormatTwo);
        assertEquals("Layer Formats Size Two", expectedFormatSizeTwo, actualFormatSizeTwo);
    }

    @Test
    public void testGetLayer_TileMatrixSets() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedTileMatrixSetOne = "WholeWorld_CRS_84";
        String expectedTileMatrixSetTwo = "World84-90_CRS_84";
        int expectedTileMatrixSetSizeOne = 1;
        int expectedTileMatrixSetSizeTwo = 1;

        List<String> actualTileMatrixSetsOne = layer.get(0).getTileMatrixSetIds();
        String actualTileMatrixSetOne = actualTileMatrixSetsOne.get(0);
        int actualTileMatrixSetSizeOne = actualTileMatrixSetsOne.size();
        List<String> actualTileMatrixSetsTwo = layer.get(1).getTileMatrixSetIds();
        String actualTileMatrixSetTwo = actualTileMatrixSetsTwo.iterator().next();
        int actualTileMatrixSetSizeTwo = actualTileMatrixSetsTwo.size();

        assertEquals("Layer TileMatrixSet One", expectedTileMatrixSetOne, actualTileMatrixSetOne);
        assertEquals("Layer TileMatrixSets Size One", expectedTileMatrixSetSizeOne, actualTileMatrixSetSizeOne);
        assertEquals("Layer TileMatrixSet Two", expectedTileMatrixSetTwo, actualTileMatrixSetTwo);
        assertEquals("Layer TileMatrixSets Size Two", expectedTileMatrixSetSizeTwo, actualTileMatrixSetSizeTwo);
    }

    @Test
    public void testGetLayer_ResourceURLs_One() throws Exception {
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedResourceUrlFormatOne = "image/png";
        String expectedResourceUrlFormatTwo = "application/gml+xml; version=3.1";
        String expectedResourceUrlResourceTypeOne = "tile";
        String expectedResourceUrlResourceTypeTwo = "FeatureInfo";
        String expectedResourceUrlTemplateOne = "http://www.opengis.uab.es/SITiled/world/etopo2/default/WholeWorld_CRS_84/{TileMatrix}/{TileRow}/{TileCol}.png";
        String expectedResourceUrlTemplateTwo = "http://www.opengis.uab.es/SITiled/world/etopo2/default/WholeWorld_CRS_84/{TileMatrix}/{TileRow}/{TileCol}/{J}/{I}.xml";

        WmtsResourceUrl resourceOne = layer.get(0).getResourceUrls().get(0);
        String actualResourceUrlFormatOne = resourceOne.format;
        String actualResourceUrlResourceTypeOne = resourceOne.resourceType;
        String actualResourceUrlTemplateOne = resourceOne.template;
        WmtsResourceUrl resourceTwo = layer.get(0).getResourceUrls().get(1);
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
        List<WmtsLayer> layer = this.wmtsCapabilities.getLayers();
        String expectedResourceUrlFormatOne = "image/png";
        String expectedResourceUrlFormatTwo = "application/gml+xml; version=3.1";
        String expectedResourceUrlResourceTypeOne = "tile";
        String expectedResourceUrlResourceTypeTwo = "FeatureInfo";
        String expectedResourceUrlTemplateOne = "http://www.opengis.uab.es/SITiled/world/AdminBoundaries/default/World84-90_CRS_84/{TileMatrix}/{TileRow}/{TileCol}.png";
        String expectedResourceUrlTemplateTwo = "http://www.opengis.uab.es/SITiled/world/AdminBoundaries/default/World84-90_CRS_84/{TileMatrix}/{TileRow}/{TileCol}/{J}/{I}.xml";

        WmtsResourceUrl resourceOne = layer.get(1).getResourceUrls().get(0);
        String actualResourceUrlFormatOne = resourceOne.format;
        String actualResourceUrlResourceTypeOne = resourceOne.resourceType;
        String actualResourceUrlTemplateOne = resourceOne.template;
        WmtsResourceUrl resourceTwo = layer.get(1).getResourceUrls().get(1);
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

    @Test
    public void testGetTileMatrixSets_OverallSets() throws Exception {
        String setOneName = "WholeWorld_CRS_84";
        String setTwoName = "World84-90_CRS_84";
        int expectedCount = 2;

        WmtsTileMatrixSet matrixSetOne = this.wmtsCapabilities.getTileMatrixSet(setOneName);
        WmtsTileMatrixSet matrixSetTwo = this.wmtsCapabilities.getTileMatrixSet(setTwoName);
        int actualCount = this.wmtsCapabilities.getTileMatrixSets().size();

        assertNotNull("TileMatrixSet One", matrixSetOne);
        assertNotNull("TileMatrixSet Two", matrixSetTwo);
        assertEquals("TileMatrixSet Count", expectedCount, actualCount);
    }

    @Test
    public void testGetTileMatrixSets_MatrixSetOne() throws Exception {
        WmtsTileMatrixSet wmtsTileMatrixSet = this.wmtsCapabilities.getTileMatrixSet("WholeWorld_CRS_84");
        String expectedIdentifier = "WholeWorld_CRS_84";
        String expectedSupportedCRS = "urn:ogc:def:crs:OGC:1.3:CRS84";
        String expectedWellKnownScaleSet = "urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Pixel";
        int expectedTileMatrixCount = 7;

        String actualIdentifier = wmtsTileMatrixSet.getIdentifier();
        String actualSupportedCRS = wmtsTileMatrixSet.getSupportedCrs();
        String actualWellKnownScaleSet = wmtsTileMatrixSet.getWellKnownScaleSet();
        int actualTileMatrixCount = wmtsTileMatrixSet.getTileMatrices().size();

        assertEquals("TileMatrixSet One Identifier", expectedIdentifier, actualIdentifier);
        assertEquals("TileMatrixSet One SupportedCRS", expectedSupportedCRS, actualSupportedCRS);
        assertEquals("TileMatrixSet One WellKnownScaleSet", expectedWellKnownScaleSet, actualWellKnownScaleSet);
        assertEquals("TileMatrixSet One Count", expectedTileMatrixCount, actualTileMatrixCount);
    }

    @Test
    public void testGetTileMatrixSets_MatrixSetTwo() throws Exception {
        WmtsTileMatrixSet wmtsTileMatrixSet = this.wmtsCapabilities.getTileMatrixSet("World84-90_CRS_84");
        String expectedIdentifier = "World84-90_CRS_84";
        String expectedSupportedCRS = "urn:ogc:def:crs:OGC:1.3:CRS84";
        String expectedWellKnownScaleSet = "urn:ogc:def:wkss:OGC:1.0:GlobalCRS84Pixel";
        int expectedTileMatrixCount = 7;

        String actualIdentifier = wmtsTileMatrixSet.getIdentifier();
        String actualSupportedCRS = wmtsTileMatrixSet.getSupportedCrs();
        String actualWellKnownScaleSet = wmtsTileMatrixSet.getWellKnownScaleSet();
        int actualTileMatrixCount = wmtsTileMatrixSet.getTileMatrices().size();

        assertEquals("TileMatrixSet Two Identifier", expectedIdentifier, actualIdentifier);
        assertEquals("TileMatrixSet Two SupportedCRS", expectedSupportedCRS, actualSupportedCRS);
        assertEquals("TileMatrixSet Two WellKnownScaleSet", expectedWellKnownScaleSet, actualWellKnownScaleSet);
        assertEquals("TileMatrixSet Two Count", expectedTileMatrixCount, actualTileMatrixCount);
    }

    @Test
    public void testGetTileMatrixSets_TileMatrixSetOne_TileMatrixOne() throws Exception {
        WmtsTileMatrix wmtsTileMatrix = this.wmtsCapabilities.getTileMatrixSet("WholeWorld_CRS_84").getTileMatrices().get(0);
        String expectedIdentifier = "2g";
        double expectedScaleDenominator = 795139219.951954;
        double expectedMinX = -180;
        double expectedMaxY = 90;
        int expectedTileWidth = 320;
        int expectedTileHeight = 200;
        int expectedMatrixWidth = 1;
        int expectedMatrixHeight = 1;

        String actualIdentifier = wmtsTileMatrix.getIdentifier();
        double actualScaleDenominator = wmtsTileMatrix.getScaleDenominator();
        double actualMinX = wmtsTileMatrix.getMinx();
        double actualMaxY = wmtsTileMatrix.getMaxy();
        int actualTileWidth = wmtsTileMatrix.getTileWidth();
        int actualTileHeight = wmtsTileMatrix.getTileHeight();
        int actualMatrixWidth = wmtsTileMatrix.getMatrixWidth();
        int actualMatrixHeight = wmtsTileMatrix.getMatrixHeight();

        assertEquals("TileMatrixSet One TileMatrix One Identifier", expectedIdentifier, actualIdentifier);
        assertEquals("TileMatrixSet One TileMatrix One ScaleDenominator", expectedScaleDenominator, actualScaleDenominator, DELTA);
        assertEquals("TileMatrixSet One TileMatrix One MinX", expectedMinX, actualMinX, DELTA);
        assertEquals("TileMatrixSet One TileMatrix One MaxY", expectedMaxY, actualMaxY, DELTA);
        assertEquals("TileMatrixSet One TileMatrix One TileWidth", expectedTileWidth, actualTileWidth);
        assertEquals("TileMatrixSet One TileMatrix One TileHeight", expectedTileHeight, actualTileHeight);
        assertEquals("TileMatrixSet One TileMatrix One MatrixWidth", expectedMatrixWidth, actualMatrixWidth);
        assertEquals("TileMatrixSet One TileMatrix One MatrixHeight", expectedMatrixHeight, actualMatrixHeight);
    }

    @Test
    public void testGetTileMatrixSets_TileMatrixSetOne_TileMatrixTwo() throws Exception {
        Iterator<WmtsTileMatrix> tileMatrixIterator = this.wmtsCapabilities.getTileMatrixSet("WholeWorld_CRS_84").tileMatrices.iterator();
        tileMatrixIterator.next();
        WmtsTileMatrix wmtsTileMatrix = this.wmtsCapabilities.getTileMatrixSet("WholeWorld_CRS_84").getTileMatrices().get(1);
        String expectedIdentifier = "1g";
        double expectedScaleDenominator = 397569609.975977;
        double expectedMinX = -180;
        double expectedMaxY = 90;
        int expectedTileWidth = 320;
        int expectedTileHeight = 200;
        int expectedMatrixWidth = 2;
        int expectedMatrixHeight = 1;

        String actualIdentifier = wmtsTileMatrix.getIdentifier();
        double actualScaleDenominator = wmtsTileMatrix.getScaleDenominator();
        double actualMinX = wmtsTileMatrix.getMinx();
        double actualMaxY = wmtsTileMatrix.getMaxy();
        int actualTileWidth = wmtsTileMatrix.getTileWidth();
        int actualTileHeight = wmtsTileMatrix.getTileHeight();
        int actualMatrixWidth = wmtsTileMatrix.getMatrixWidth();
        int actualMatrixHeight = wmtsTileMatrix.getMatrixHeight();

        assertEquals("TileMatrixSet One TileMatrix Two Identifier", expectedIdentifier, actualIdentifier);
        assertEquals("TileMatrixSet One TileMatrix Two ScaleDenominator", expectedScaleDenominator, actualScaleDenominator, DELTA);
        assertEquals("TileMatrixSet One TileMatrix Two MinX", expectedMinX, actualMinX, DELTA);
        assertEquals("TileMatrixSet One TileMatrix Two MaxY", expectedMaxY, actualMaxY, DELTA);
        assertEquals("TileMatrixSet One TileMatrix Two TileWidth", expectedTileWidth, actualTileWidth);
        assertEquals("TileMatrixSet One TileMatrix Two TileHeight", expectedTileHeight, actualTileHeight);
        assertEquals("TileMatrixSet One TileMatrix Two MatrixWidth", expectedMatrixWidth, actualMatrixWidth);
        assertEquals("TileMatrixSet One TileMatrix Two MatrixHeight", expectedMatrixHeight, actualMatrixHeight);
    }

    @Test
    public void testGetTileMatrixSets_TileMatrixSetTwo_TileMatrixOne() throws Exception {
        WmtsTileMatrix wmtsTileMatrix = this.wmtsCapabilities.getTileMatrixSet("World84-90_CRS_84").getTileMatrices().get(0);
        String expectedIdentifier = "2g";
        double expectedScaleDenominator = 795139219.951954;
        double expectedMinX = -180;
        double expectedMaxY = 84;
        int expectedTileWidth = 320;
        int expectedTileHeight = 200;
        int expectedMatrixWidth = 1;
        int expectedMatrixHeight = 1;

        String actualIdentifier = wmtsTileMatrix.getIdentifier();
        double actualScaleDenominator = wmtsTileMatrix.getScaleDenominator();
        double actualMinX = wmtsTileMatrix.getMinx();
        double actualMaxY = wmtsTileMatrix.getMaxy();
        int actualTileWidth = wmtsTileMatrix.getTileWidth();
        int actualTileHeight = wmtsTileMatrix.getTileHeight();
        int actualMatrixWidth = wmtsTileMatrix.getMatrixWidth();
        int actualMatrixHeight = wmtsTileMatrix.getMatrixHeight();

        assertEquals("TileMatrixSet Two TileMatrix One Identifier", expectedIdentifier, actualIdentifier);
        assertEquals("TileMatrixSet Two TileMatrix One ScaleDenominator", expectedScaleDenominator, actualScaleDenominator, DELTA);
        assertEquals("TileMatrixSet Two TileMatrix One MinX", expectedMinX, actualMinX, DELTA);
        assertEquals("TileMatrixSet Two TileMatrix One MaxY", expectedMaxY, actualMaxY, DELTA);
        assertEquals("TileMatrixSet Two TileMatrix One TileWidth", expectedTileWidth, actualTileWidth);
        assertEquals("TileMatrixSet Two TileMatrix One TileHeight", expectedTileHeight, actualTileHeight);
        assertEquals("TileMatrixSet Two TileMatrix One MatrixWidth", expectedMatrixWidth, actualMatrixWidth);
        assertEquals("TileMatrixSet Two TileMatrix One MatrixHeight", expectedMatrixHeight, actualMatrixHeight);
    }

    @Test
    public void testGetTileMatrixSets_TileMatrixSetTwo_TileMatrixTwo() throws Exception {
        WmtsTileMatrix wmtsTileMatrix = this.wmtsCapabilities.getTileMatrixSet("World84-90_CRS_84").getTileMatrices().get(1);
        String expectedIdentifier = "1g";
        double expectedScaleDenominator = 397569609.975977;
        double expectedMinX = -180;
        double expectedMaxY = 84;
        int expectedTileWidth = 320;
        int expectedTileHeight = 200;
        int expectedMatrixWidth = 2;
        int expectedMatrixHeight = 1;

        String actualIdentifier = wmtsTileMatrix.getIdentifier();
        double actualScaleDenominator = wmtsTileMatrix.getScaleDenominator();
        double actualMinX = wmtsTileMatrix.getMinx();
        double actualMaxY = wmtsTileMatrix.getMaxy();
        int actualTileWidth = wmtsTileMatrix.getTileWidth();
        int actualTileHeight = wmtsTileMatrix.getTileHeight();
        int actualMatrixWidth = wmtsTileMatrix.getMatrixWidth();
        int actualMatrixHeight = wmtsTileMatrix.getMatrixHeight();

        assertEquals("TileMatrixSet Two TileMatrix Two Identifier", expectedIdentifier, actualIdentifier);
        assertEquals("TileMatrixSet Two TileMatrix Two ScaleDenominator", expectedScaleDenominator, actualScaleDenominator, DELTA);
        assertEquals("TileMatrixSet Two TileMatrix Two MinX", expectedMinX, actualMinX, DELTA);
        assertEquals("TileMatrixSet Two TileMatrix Two MaxY", expectedMaxY, actualMaxY, DELTA);
        assertEquals("TileMatrixSet Two TileMatrix Two TileWidth", expectedTileWidth, actualTileWidth);
        assertEquals("TileMatrixSet Two TileMatrix Two TileHeight", expectedTileHeight, actualTileHeight);
        assertEquals("TileMatrixSet Two TileMatrix Two MatrixWidth", expectedMatrixWidth, actualMatrixWidth);
        assertEquals("TileMatrixSet Two TileMatrix One MatrixHeight", expectedMatrixHeight, actualMatrixHeight);
    }

    @Test
    public void testGetThemes_ParentTheme() throws Exception {
        WmtsTheme parentTheme = this.wmtsCapabilities.getThemes().get(0);
        String expectedTitle = "Foundation";
        String expectedAbstract = "World reference data";
        String expectedIdentifier = "Foundation";

        String actualTitle = parentTheme.getTitle();
        String actualAbstract = parentTheme.getThemeAbstract();
        String actualIdentifier = parentTheme.getIdentifier();

        assertEquals("Parent Theme Title", expectedTitle, actualTitle);
        assertEquals("Parent Theme Abstract", expectedAbstract, actualAbstract);
        assertEquals("Parent Theme Identifier", expectedIdentifier, actualIdentifier);
    }

    @Test
    public void testGetThemes_ChildThemeOne() throws Exception {
        WmtsTheme theme = this.wmtsCapabilities.getThemes().get(0).getThemes().get(0);
        String expectedTitle = "Digital Elevation Model";
        String expectedLayerRef = "etopo2";
        String expectedIdentifier = "DEM";

        String actualTitle = theme.title;
        String actualLayerRef = theme.layerRefs.iterator().next();
        String actualIdentifier = theme.identifier;

        assertEquals("Child One Theme Title", expectedTitle, actualTitle);
        assertEquals("Child One Theme LayerRef", expectedLayerRef, actualLayerRef);
        assertEquals("Child One Theme Identifier", expectedIdentifier, actualIdentifier);
    }

    @Test
    public void testGetThemes_ChildThemeTwo() throws Exception {
        WmtsTheme theme = this.wmtsCapabilities.getThemes().get(0).getThemes().get(1);
        String expectedTitle = "Administrative Boundaries";
        String expectedLayerRef = "AdminBoundaries";
        String expectedIdentifier = "AdmBoundaries";

        String actualTitle = theme.title;
        String actualLayerRef = theme.layerRefs.iterator().next();
        String actualIdentifier = theme.identifier;

        assertEquals("Child Two Theme Title", expectedTitle, actualTitle);
        assertEquals("Child Two Theme LayerRef", expectedLayerRef, actualLayerRef);
        assertEquals("Child Two Theme Identifier", expectedIdentifier, actualIdentifier);
    }

    @Test
    public void testGetServiceMetadataUrl() throws Exception {
        WmtsElementLink serviceMetadataUrl = this.wmtsCapabilities.serviceMetadataUrls.iterator().next();
        String expectedHref = "http://www.opengis.uab.es/SITiled/world/1.0.0/WMTSCapabilities.xml";

        String actualHref = serviceMetadataUrl.href;

        assertEquals("ServiceMetadataURL Href", expectedHref, actualHref);
    }
}
