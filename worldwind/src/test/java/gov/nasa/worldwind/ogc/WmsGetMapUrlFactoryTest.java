/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class WmsGetMapUrlFactoryTest {

    /**
     * Common parameters used when creating a WMS request URL.
     */
    public static final String COMMON_SERVICE_ADDRESS = "http://worldwind25.arc.nasa.gov/wms";

    public static final String COMMON_SERVICE_WMS = "WMS";

    public static final String COMMON_WMS_VERSION = "1.3.0";

    public static final String COMMON_LAYER_NAMES = "BlueMarble-200405,esat";

    public static final String COMMON_IMAGE_FORMAT = "image/png";

    public static final String SYSTEM_CRS84 = "CRS:84";

    public static final String SYSTEM_EPSG4326 = "EPSG:4326";

    /**
     * Notional values used for testing.
     */
    private static final double DELTA = 1e-6;

    private static final double NOTIONAL_MIN_LAT = -90.0;

    private static final double NOTIONAL_MAX_LAT = 0.0;

    private static final double NOTIONAL_MIN_LON = -180.0;

    private static final double NOTIONAL_MAX_LON = -90.0;

    private static final int NOTIONAL_ROW = 3;

    private static final int NOTIONAL_COLUMN = 2;

    private static final String NOTIONAL_WMS_VERSION = "1.23";

    /**
     * Enumerations of a double array used internally for storing parsed values of the latitude and longitude.
     */
    public static final int LAT_MIN = 0;

    public static final int LAT_MAX = 1;

    public static final int LON_MIN = 2;

    public static final int LON_MAX = 3;

    /**
     * Patterns for checking the generated URL parameters.
     */
    private static final Pattern SERVICE_P = Pattern.compile("SERVICE=(.*?)(&|\\z)");

    private static final Pattern VERSION_P = Pattern.compile("VERSION=(.*?)(&|\\z)");

    private static final Pattern LAYERS_P = Pattern.compile("LAYERS=(.*?)(&|\\z)");

    private static final Pattern STYLES_P = Pattern.compile("STYLES=(.*?)(&|\\z)");

    private static final Pattern CRS_P = Pattern.compile("[CS]RS=(.*?)(&|\\z)");

    private static final Pattern BBOX_P = Pattern.compile("BBOX=(.*?)(&|\\z)");

    private static final Pattern WIDTH_P = Pattern.compile("WIDTH=(.*?)(&|\\z)");

    private static final Pattern HEIGHT_P = Pattern.compile("HEIGHT=(.*?)(&|\\z)");

    private static final Pattern FORMAT_P = Pattern.compile("FORMAT=(.*?)(&|\\z)");

    private static final Pattern TRANSPARENT_P = Pattern.compile("TRANSPARENT=(.*?)(&|\\z)");

    private static final Pattern TIME_P = Pattern.compile("TIME=(.*?)(&|\\z)");

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    /**
     * Tests the {@link WmsGetMapUrlFactory} constructor that takes the different service parameters. Checks that
     * parameters which are not intended to be null throw {@link IllegalArgumentException}.
     */
    @Test
    public void testConstructor_ServiceParameters() {

        // Javadocs specify that the serviceAddress, wmsVersion, and layerNames cannot be null
        try {
            WmsGetMapUrlFactory wmsFactory
                = new WmsGetMapUrlFactory(null, COMMON_WMS_VERSION, COMMON_LAYER_NAMES, null);
            fail("Missing the serviceAddress, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        try {
            WmsGetMapUrlFactory wmsFactory
                = new WmsGetMapUrlFactory(COMMON_SERVICE_ADDRESS, null, COMMON_LAYER_NAMES, null);
            fail("Missing the wmsVersion, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        try {
            WmsGetMapUrlFactory wmsFactory
                = new WmsGetMapUrlFactory(COMMON_SERVICE_ADDRESS, COMMON_WMS_VERSION, null, null);
            fail("Missing the layerNames, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        // Check parameters are passed successfully and utilized
        WmsGetMapUrlFactory wmsFactory
            = new WmsGetMapUrlFactory(COMMON_SERVICE_ADDRESS, COMMON_WMS_VERSION, COMMON_LAYER_NAMES, null);
        assertEquals("Service Address Match", COMMON_SERVICE_ADDRESS, wmsFactory.serviceAddress);
        assertEquals("WMS Version Match", COMMON_WMS_VERSION, wmsFactory.wmsVersion);
        assertEquals("Layer Name Match", COMMON_LAYER_NAMES, wmsFactory.layerNames);
        assertNull("Null Style Names", wmsFactory.styleNames);
    }

    /**
     * Tests the {@link WmsGetMapUrlFactory} constructor which takes a single non-null {@link WmsLayerConfig} object.
     * Checks that a null {@link WmsLayerConfig} will throw an {@link IllegalArgumentException}.
     */
    @Test
    public void testConstructor_Config() {

        try {
            WmsGetMapUrlFactory wmsFactory = new WmsGetMapUrlFactory(null);
            fail("Missing config, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        // The config constructor incorporates the same pattern as the service parameters constructor
        // Test all null checks conducted by the config constructor
        WmsLayerConfig layerConfig = new WmsLayerConfig(null, null, null, null, null, false, null);
        try {
            WmsGetMapUrlFactory wmsFactory = new WmsGetMapUrlFactory(layerConfig);
            fail("Missing the serviceAddress, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        // Progressively add agruments and test for exceptions
        layerConfig.serviceAddress = COMMON_SERVICE_ADDRESS;
        try {
            WmsGetMapUrlFactory wmsFactory = new WmsGetMapUrlFactory(layerConfig);
            fail("Missing the wmsVersion, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        layerConfig.wmsVersion = COMMON_WMS_VERSION;
        try {
            WmsGetMapUrlFactory wmsFactory = new WmsGetMapUrlFactory(layerConfig);
            fail("Missing the layerNames, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        layerConfig.layerNames = COMMON_LAYER_NAMES;
        try {
            WmsGetMapUrlFactory wmsFactory = new WmsGetMapUrlFactory(layerConfig);
            fail("Missing the coordinateSystem, should not return");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        // This is the final required parameter
        layerConfig.coordinateSystem = "EPSG:4326";

        WmsGetMapUrlFactory wmsFactory = new WmsGetMapUrlFactory(layerConfig);

        assertEquals("service address match", layerConfig.serviceAddress, wmsFactory.serviceAddress);
        assertEquals("wms version match", layerConfig.wmsVersion, wmsFactory.wmsVersion);
        assertEquals("layer name match", layerConfig.layerNames, wmsFactory.layerNames);
        assertEquals("coordinate system match", layerConfig.coordinateSystem, wmsFactory.coordinateSystem);
        assertNull("null style names", wmsFactory.styleNames);
    }

    /**
     * Test the {@code setServiceAddress} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetServiceAddress() {

        String alteredServiceAddress = "testAddress"; // notional address
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        standardWmsMapFactory.setServiceAddress(alteredServiceAddress);
        String serviceAddress = standardWmsMapFactory.getServiceAddress();

        assertEquals("update service address", alteredServiceAddress, serviceAddress);

        // Check the setter prevents null submissions
        standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        try {

            standardWmsMapFactory.setServiceAddress(null);

            fail("null submission allowed for service address");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }
    }

    /**
     * Test the {@code getServiceAddress} method.
     */
    @Test
    public void testGetServiceAddress() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        String serviceAddress = standardWmsMapFactory.getServiceAddress();

        assertEquals("update service address", COMMON_SERVICE_ADDRESS, serviceAddress);
    }

    /**
     * Test the {@code setWmsVersion} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetWmsVersion() {

        String updatedWmsVersion = "1.4.0"; // notional versioning
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        standardWmsMapFactory.setWmsVersion(updatedWmsVersion);

        String wmsVersion = standardWmsMapFactory.getWmsVersion();
        assertEquals("update wms version", updatedWmsVersion, wmsVersion);

        // Check the setter prevents null submissions
        standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        try {

            standardWmsMapFactory.setWmsVersion(null);

            fail("null submission allowed for wms version");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }
    }

    /**
     * Test the {@code getWmsVersion} method.
     */
    @Test
    public void testGetWmsVersion() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        String wmsVersion = standardWmsMapFactory.getWmsVersion();

        assertEquals("wms version", COMMON_WMS_VERSION, wmsVersion);
    }

    /**
     * Test the {@code setLayerNames} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetLayerNames() {

        String updatedLayerNames = "layer1,layer2"; // notional
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        standardWmsMapFactory.setLayerNames(updatedLayerNames);

        String layerNames = standardWmsMapFactory.getLayerNames();
        assertEquals("update layer names", updatedLayerNames, layerNames);

        // Check the setter prevents null submissions
        standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        try {

            standardWmsMapFactory.setLayerNames(null);

            fail("null submission allowed for layer names");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }
    }

    /**
     * Test the {@code getLayerNames} method.
     */
    @Test
    public void testGetLayerNames() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        String layerNames = standardWmsMapFactory.getLayerNames();

        assertEquals("layer names", COMMON_LAYER_NAMES, layerNames);
    }

    /**
     * Test the {@code setStyleNames} method.
     */
    @Test
    public void testSetStyleNames() {

        String updatedStyleNames = "style1,style2"; // notional
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        standardWmsMapFactory.setStyleNames(updatedStyleNames);

        String styleNames = standardWmsMapFactory.getStyleNames();
        assertEquals("update style names", updatedStyleNames, styleNames);
    }

    /**
     * Test the {@code getStyleNames} method. A default instantiation of a {@link WmsGetMapUrlFactory} will null style
     * names and this setting is tested in this test.
     */
    @Test
    public void testGetStyleNames_Null() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        String styleNames = standardWmsMapFactory.getStyleNames();

        assertNull("default null style names", styleNames);
    }

    /**
     * Test the {@code getStyleNames} method. A default instantiation of a {@link WmsGetMapUrlFactory} will null style
     * names. This test sets a style name, then proceeds with the test.
     */
    @Test
    public void testGetStyleNames_NotNull() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        String notionalStyleNames = "notionalstyle1,notionalstyle2";
        standardWmsMapFactory.setStyleNames(notionalStyleNames);

        String styleNames = standardWmsMapFactory.getStyleNames();

        assertEquals("style names", notionalStyleNames, styleNames);
    }

    /**
     * Test the {@code setCoordinateSystem} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetCoordinateSystem() {

        String updatedCoordinateSystem = "system"; // notional
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        standardWmsMapFactory.setCoordinateSystem(updatedCoordinateSystem);

        String coordinateSystem = standardWmsMapFactory.getCoordinateSystem();
        assertEquals("update coordinate system", updatedCoordinateSystem, coordinateSystem);

        // Check the setter prevents null submissions
        standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        try {

            standardWmsMapFactory.setCoordinateSystem(null);

            fail("null submission allowed for coordinate system");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }
    }

    /**
     * Test the {@code getCoordinateSystem} method.
     */
    @Test
    public void testGetCoordinateSystem() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        String coordinateSystem = standardWmsMapFactory.getCoordinateSystem();

        // at this time the default coordinate is EPSG
        assertEquals("coordinate system", SYSTEM_EPSG4326, coordinateSystem);
    }

    /**
     * Test the {@code setTransparency} method.
     */
    @Test
    public void testSetTransparency() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        boolean previousSetting = standardWmsMapFactory.isTransparent();

        standardWmsMapFactory.setTransparent(!previousSetting);

        assertFalse("ensure transparency set", previousSetting == standardWmsMapFactory.isTransparent());
    }

    /**
     * Test the {@code isTransparent} method.
     */
    @Test
    public void testIsTransparent() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        boolean transparent = standardWmsMapFactory.isTransparent();

        // default of WmsLayerConfig is transparency = true
        assertTrue("is transparent", transparent);
    }

    /**
     * Test the {@code setTimeString} method.
     */
    @Test
    public void testSetTimeString() {

        String updatedTimeString = "time"; // notional
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        standardWmsMapFactory.setTimeString(updatedTimeString);

        String time = standardWmsMapFactory.getTimeString();
        assertEquals("update time string", updatedTimeString, time);
    }

    /**
     * Test the {@code getTimeString} method. A default instantiation of a {@link WmsGetMapUrlFactory} will null the
     * time string and this setting is tested in this test.
     */
    @Test
    public void testGetTimeString_Null() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        String timeString = standardWmsMapFactory.getTimeString();

        assertNull("default null of time string", timeString);
    }

    /**
     * Test the {@code getTimeString} method. A default instantiation of a {@link WmsGetMapUrlFactory} will null the the
     * time string. This test sets a notional time string, then proceeds with the test.
     */
    @Test
    public void testGetTimeString_NotNull() {

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        String alternativeTimeString = "1600-NOTIONAL";
        standardWmsMapFactory.setTimeString(alternativeTimeString);

        String timeString = standardWmsMapFactory.getTimeString();

        assertEquals("updated time string", alternativeTimeString, timeString);
    }

    /**
     * Test that null submission to required parameters result in a thrown {@link IllegalArgumentException}.
     */
    @Test
    public void testUrlForTile_ParameterCheck() {

        // check null's are not permitted
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        try {

            standardWmsMapFactory.urlForTile(null, null);

            fail("null parameters provided to urlForTile");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }

        Tile tile = PowerMockito.mock(Tile.class);
        standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
        try {

            standardWmsMapFactory.urlForTile(tile, null);

            fail("null Parameters Pushed to urlForTile");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }
    }

    /**
     * Test that the query delimiter is properly placed in the url. This test ensures that a base url, defined as a url
     * which does not include the query delimiter '?', has the delimiter appended to the url.
     */
    @Test
    public void testUrlForTile_QueryDelimiterPositioning_BaseUrl() {

        // Values used for the Blue Marble
        String serviceAddress = COMMON_SERVICE_ADDRESS;
        String wmsVersion = COMMON_WMS_VERSION;
        String layerNames = COMMON_LAYER_NAMES;
        String imageFormat = COMMON_IMAGE_FORMAT;

        // Mocking of method object parameters - notional values
        int tileHeight = 5;
        int tileWidth = 4;
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        Tile tile = new Tile(sector, tileLevel, tileHeight, tileWidth);

        // Provide the method a service address without a query delimiter
        WmsGetMapUrlFactory wmsUrlFactory = new WmsGetMapUrlFactory(serviceAddress, wmsVersion, layerNames, null);
        String url = wmsUrlFactory.urlForTile(tile, imageFormat);

        checkQueryDelimiter(url);
    }

    /**
     * Test that the query delimiter is properly placed in the url. This test ensures a url which includes a query
     * delimiter character at the end of the address will not be changed while appending additional parameters.
     */
    @Test
    public void testUrlForTile_QueryDelimiterPositioning_DelimiterAppended() {

        // Values used for the Blue Marble
        String serviceAddress = COMMON_SERVICE_ADDRESS;
        String wmsVersion = COMMON_WMS_VERSION;
        String layerNames = COMMON_LAYER_NAMES;
        String imageFormat = COMMON_IMAGE_FORMAT;

        // Mocking of method object parameters - notional values
        int tileHeight = 5;
        int tileWidth = 4;
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        Tile tile = new Tile(sector, tileLevel, tileHeight, tileWidth);

        // Provide the method a service address with a query delimiter appended
        WmsGetMapUrlFactory wmsUrlFactory = new WmsGetMapUrlFactory(serviceAddress + '?', wmsVersion, layerNames, null);
        String url = wmsUrlFactory.urlForTile(tile, imageFormat);

        checkQueryDelimiter(url);
    }

    /**
     * Test that the query delimiter is properly placed in the url. This test ensures the provided service address which
     * includes a query delimiter followed by parameters will only have an ampersand appended by the factory.
     */
    @Test
    public void testUrlForTile_QueryDelimiterPositioning_BareUrl_AdditionalParameters() {

        // Values used for the Blue Marble
        String serviceAddress = COMMON_SERVICE_ADDRESS;
        String wmsVersion = COMMON_WMS_VERSION;
        String layerNames = COMMON_LAYER_NAMES;
        String imageFormat = COMMON_IMAGE_FORMAT;

        // Mocking of method object parameters - notional values
        int tileHeight = 5;
        int tileWidth = 4;
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        Tile tile = new Tile(sector, tileLevel, tileHeight, tileWidth);

        // Provide the method a service address with a query delimiter and existing parameters
        WmsGetMapUrlFactory wmsUrlFactory
            = new WmsGetMapUrlFactory(serviceAddress + "?NOTIONAL=YES", wmsVersion, layerNames, null);
        String url = wmsUrlFactory.urlForTile(tile, imageFormat);

        checkQueryDelimiter(url);
    }

    /**
     * Test that the query delimiter is properly placed in the url. This test ensures the provided service address which
     * includes a query delimiter followed by parameters and an ampersand be unaltered by the factory.
     */
    @Test
    public void testUrlForTile_QueryDelimiterPositioning_BareUrl_AdditionalParametersWithAmpersand() {

        // Values used for the Blue Marble
        String serviceAddress = COMMON_SERVICE_ADDRESS;
        String wmsVersion = COMMON_WMS_VERSION;
        String layerNames = COMMON_LAYER_NAMES;
        String imageFormat = COMMON_IMAGE_FORMAT;

        // Mocking of method object parameters - notional values
        int tileHeight = 5;
        int tileWidth = 4;
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        Tile tile = new Tile(sector, tileLevel, tileHeight, tileWidth);

        // Provide the method a service address with a query delimiter and existing parameters
        WmsGetMapUrlFactory wmsUrlFactory
            = new WmsGetMapUrlFactory(serviceAddress + "?NOTIONAL=YES&", wmsVersion, layerNames, null);
        String url = wmsUrlFactory.urlForTile(tile, imageFormat);

        checkQueryDelimiter(url);
    }

    /**
     * Tests the generated url parameters match the properties of the {@link WmsGetMapUrlFactory} and {@link Tile} used
     * to generate the url. This test evaluates the configuration: WMS version 1.3.0 using the EPSG:4326 coordinate
     * format.
     * <p/>
     * <p>This is part of test suite detailed below:</p>
     * <p/>
     * <p>Essentially three different formats for describing the bounding box to the WMS servier. A four and fifth test
     * case provide for testing the optional STYLE and TIME parameters. 1. WMS Version 1.3.0 and EPSG:4326 2. WMS
     * Version 1.3.0 and CRS:84 3. Other WMS Version 4. Optional Styles Parameter 5. Optional Time Parameter</p>
     */
    @Test
    public void testUrlForTile_Parameters_Wms130_EPSG4326() {

        // Create mocked supporting objects
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        // The urlForTile method accesses protected fields, thus notional objects are used
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        // A Standard Tile to use for generating URLs
        Tile tile = new Tile(sector, tileLevel, NOTIONAL_ROW, NOTIONAL_COLUMN);

        // Set the Service Address
        standardWmsMapFactory.setServiceAddress(COMMON_SERVICE_ADDRESS);

        // Settings for test one
        standardWmsMapFactory.setWmsVersion(COMMON_WMS_VERSION);
        standardWmsMapFactory.setCoordinateSystem(SYSTEM_EPSG4326);

        String url = standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);

        checkUrl(url, standardWmsMapFactory, tile);
    }

    /**
     * Tests the generated url parameters match the properties of the {@link WmsGetMapUrlFactory} and {@link Tile} used
     * to generate the url. This test evaluates the configuration: WMS version 1.3.0 using the CRS:84 coordinate
     * format.
     * <p/>
     * <p>This is part of test suite detailed below:</p>
     * <p/>
     * <p>Essentially three different formats for describing the bounding box to the WMS servier. A four and fifth test
     * case provide for testing the optional STYLE and TIME parameters. 1. WMS Version 1.3.0 and EPSG:4326 2. WMS
     * Version 1.3.0 and CRS:84 3. Other WMS Version 4. Optional Styles Parameter 5. Optional Time Parameter</p>
     */
    @Test
    public void testUrlForTile_Parameters_Wms130_CRS84() {

        // Create mocked supporting objects
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        // The urlForTile method accesses protected fields, thus notional objects are used
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        // A Standard Tile to use for generating URLs
        Tile tile = new Tile(sector, tileLevel, NOTIONAL_ROW, NOTIONAL_COLUMN);

        // Set the Service Address
        standardWmsMapFactory.setServiceAddress(COMMON_SERVICE_ADDRESS);

        // Settings for test one
        standardWmsMapFactory.setWmsVersion(COMMON_WMS_VERSION);
        standardWmsMapFactory.setCoordinateSystem(SYSTEM_CRS84);

        String url = standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);

        checkUrl(url, standardWmsMapFactory, tile);
    }

    /**
     * Tests the generated url parameters match the properties of the {@link WmsGetMapUrlFactory} and {@link Tile} used
     * to generate the url. This test evaluates the configuration: WMS Version other than 1.3.0.
     * <p/>
     * <p>This is part of test suite detailed below:</p>
     * <p/>
     * <p>Essentially three different formats for describing the bounding box to the WMS servier. A four and fifth test
     * case provide for testing the optional STYLE and TIME parameters. 1. WMS Version 1.3.0 and EPSG:4326 2. WMS
     * Version 1.3.0 and CRS:84 3. Other WMS Version 4. Optional Styles Parameter 5. Optional Time Parameter</p>
     */
    @Test
    public void testUrlForTile_Parameters_WmsNot130() {

        // Create mocked supporting objects
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        // The urlForTile method accesses protected fields, thus notional objects are used
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        // A Standard Tile to use for generating URLs
        Tile tile = new Tile(sector, tileLevel, NOTIONAL_ROW, NOTIONAL_COLUMN);

        // Set the Service Address
        standardWmsMapFactory.setServiceAddress(COMMON_SERVICE_ADDRESS);

        // Settings for test one
        standardWmsMapFactory.setWmsVersion(NOTIONAL_WMS_VERSION);

        String url = standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);

        checkUrl(url, standardWmsMapFactory, tile);
    }

    /**
     * Tests the generated url parameters match the properties of the {@link WmsGetMapUrlFactory} and {@link Tile} used
     * to generate the url. This test evaluates the configuration: Addition of optional Style parameter.
     * <p/>
     * <p>This is part of test suite detailed below:</p>
     * <p/>
     * <p>Essentially three different formats for describing the bounding box to the WMS servier. A four and fifth test
     * case provide for testing the optional STYLE and TIME parameters. 1. WMS Version 1.3.0 and EPSG:4326 2. WMS
     * Version 1.3.0 and CRS:84 3. Other WMS Version 4. Optional Styles Parameter 5. Optional Time Parameter</p>
     */
    @Test
    public void testUrlForTile_Parameters_OptionalStyles() {

        // Create mocked supporting objects
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        // The urlForTile method accesses protected fields, thus notional objects are used
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        // A Standard Tile to use for generating URLs
        Tile tile = new Tile(sector, tileLevel, NOTIONAL_ROW, NOTIONAL_COLUMN);
        standardWmsMapFactory.setStyleNames("notionalstyle1,notionalstyle2");

        String url = standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);

        checkUrl(url, standardWmsMapFactory, tile);
    }

    /**
     * Tests the generated url parameters match the properties of the {@link WmsGetMapUrlFactory} and {@link Tile} used
     * to generate the url. This test evaluates the configuration: Additional optional time parameter.
     * <p/>
     * <p>This is part of test suite detailed below:</p>
     * <p/>
     * <p>Essentially three different formats for describing the bounding box to the WMS servier. A four and fifth test
     * case provide for testing the optional STYLE and TIME parameters. 1. WMS Version 1.3.0 and EPSG:4326 2. WMS
     * Version 1.3.0 and CRS:84 3. Other WMS Version 4. Optional Styles Parameter 5. Optional Time Parameter</p>
     */
    @Test
    public void testUrlForTile_Parameters_OptionalTime() {

        // Create mocked supporting objects
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        // The urlForTile method accesses protected fields, thus notional objects are used
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);

        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        WmsGetMapUrlFactory standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);

        // A Standard Tile to use for generating URLs
        Tile tile = new Tile(sector, tileLevel, NOTIONAL_ROW, NOTIONAL_COLUMN);
        standardWmsMapFactory.setTimeString("1800-ZULU"); //notional time

        String url = standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);

        checkUrl(url, standardWmsMapFactory, tile);
    }

    /**
     * Test the provided {@link String} url against the {@link WmsGetMapUrlFactory} and {@link Tile} objects properties.
     * This method will test that the parameters of {@link WmsGetMapUrlFactory} and {@link Tile} are properly
     * represented in the url. This method uses the {@link junit.framework.Assert} methods to communicate test results.
     *
     * @param url        the generated {@link String} url to be evaluated
     * @param urlFactory the {@link WmsGetMapUrlFactory} which generated the url
     * @param tile       the {@link Tile} used to generate the url
     */
    private static void checkUrl(String url, WmsGetMapUrlFactory urlFactory, Tile tile) {

        // Test Service Description - WMS only at this time
        Matcher m = SERVICE_P.matcher(url);

        if (m.find()) {
            assertEquals("test service parameter", COMMON_SERVICE_WMS, m.group(1));
        } else {
            fail("service parameter not found");
        }

        // Test Version
        m = VERSION_P.matcher(url);

        if (m.find()) {
            assertEquals("test wms version parameter", urlFactory.getWmsVersion(), m.group(1));
        } else {
            fail("wms version parameter not found");
        }

        // Test Layers
        m = LAYERS_P.matcher(url);

        if (m.find()) {
            assertEquals("test layer list parameter", urlFactory.getLayerNames(), m.group(1));
        } else {
            fail("layer list parameter not found");
        }

        // Test Styles
        if (urlFactory.getStyleNames() != null) {
            m = STYLES_P.matcher(url);
            if (m.find()) {
                assertEquals("test style parameter", urlFactory.getStyleNames(), m.group(1));
            } else {
                fail("style list parameter not found");
            }
        }

        // Test CRS/SRS System
        m = CRS_P.matcher(url);

        if (m.find()) {
            assertEquals("test coordinate system parameter", urlFactory.getCoordinateSystem(), m.group(1));
        } else {
            fail("coordinate system parameter not found");
        }

        // Test Bounding Box
        m = BBOX_P.matcher(url);

        if (m.find()) {
            // Now need to split up the values and parse to doubles
            String[] values = m.group(1).split(",");
            if (values.length == 4) {
                double[] coords = new double[4];

                // From this point need to proceed with knowledge of the WMS version and coordinate system in order
                // to parse the values in the right order
                if (urlFactory.getWmsVersion().equals(COMMON_WMS_VERSION)) {

                    if (urlFactory.getCoordinateSystem().equals(SYSTEM_CRS84)) {

                        coords[LON_MIN] = Double.parseDouble(values[0]);
                        coords[LAT_MIN] = Double.parseDouble(values[1]);
                        coords[LON_MAX] = Double.parseDouble(values[2]);
                        coords[LAT_MAX] = Double.parseDouble(values[3]);

                    } else {

                        coords[LAT_MIN] = Double.parseDouble(values[0]);
                        coords[LON_MIN] = Double.parseDouble(values[1]);
                        coords[LAT_MAX] = Double.parseDouble(values[2]);
                        coords[LON_MAX] = Double.parseDouble(values[3]);
                    }
                } else {

                    coords[LON_MIN] = Double.parseDouble(values[0]);
                    coords[LAT_MIN] = Double.parseDouble(values[1]);
                    coords[LON_MAX] = Double.parseDouble(values[2]);
                    coords[LAT_MAX] = Double.parseDouble(values[3]);
                }

                //Now Check the values
                assertEquals("test min lat", NOTIONAL_MIN_LAT, coords[LAT_MIN], DELTA);
                assertEquals("test max lat", NOTIONAL_MAX_LAT, coords[LAT_MAX], DELTA);
                assertEquals("test min lon", NOTIONAL_MIN_LON, coords[LON_MIN], DELTA);
                assertEquals("test max lon", NOTIONAL_MAX_LON, coords[LON_MAX], DELTA);

            } else {

                fail("unable to delimit bounding box values");
            }
        } else {

            fail("unable to find bounding box values");
        }

        // Test Width and Height
        m = WIDTH_P.matcher(url);
        if (m.find()) {
            assertEquals("width value test", tile.level.tileWidth, Integer.parseInt(m.group(1)));
        } else {
            fail("did not find width parameter");
        }

        m = HEIGHT_P.matcher(url);
        if (m.find()) {
            assertEquals("height value test", tile.level.tileHeight, Integer.parseInt(m.group(1)));
        } else {
            fail("did not find height parameter");
        }

        // Test Format
        m = FORMAT_P.matcher(url);
        if (m.find()) {
            assertEquals("format test", COMMON_IMAGE_FORMAT, m.group(1));
        } else {
            fail("image format parameter not found");
        }

        // Test Transparency
        m = TRANSPARENT_P.matcher(url);
        if (m.find()) {
            assertEquals("test transparency", urlFactory.isTransparent(), Boolean.parseBoolean(m.group(1)));
        } else {
            fail("transparency parameter not found");
        }

        // Test Time, if there is any
        String timeString = urlFactory.getTimeString();
        if (timeString != null && !timeString.isEmpty()) {
            m = TIME_P.matcher(url);
            if (m.find()) {
                assertEquals("time test", urlFactory.getTimeString(), m.group(1));
            } else {
                fail("time did not match");
            }
        }
    }

    /**
     * Tests the provided url {@link String} with four tests. The first test ensures a query delimiter is present in the
     * {@link String}. The second test ensures only one query delimiter is present. The third test ensures that the url
     * is not empty behind the query delimiter and contains parameters, although the validity of the parameter is not
     * checked. The fourth test ensures parameters immedietly follow the query delimiter and not the ampersand
     * character.
     *
     * @param url a {@link String} with the url to test for query delimiter placement
     */
    private static void checkQueryDelimiter(String url) {

        char queryDelimiter = '?';

        int index = url.indexOf(queryDelimiter);

        assertTrue("added delimiter", index > 0);

        // ensure only one delimiter
        int lastIndex = url.lastIndexOf(queryDelimiter);
        assertTrue("one delimiter", index == lastIndex);

        // check parameters follow query delimiter
        assertTrue("no following parameters", (url.length() - 1) > index);

        // check trailing character isn't an ampersand
        assertFalse("ampersand trailing", url.charAt(index + 1) == '&');
    }
}
