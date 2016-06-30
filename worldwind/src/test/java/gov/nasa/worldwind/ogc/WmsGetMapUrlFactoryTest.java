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
     * Test Object used by the test classes.
     */
    private WmsGetMapUrlFactory standardWmsMapFactory;

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

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);

        // Utilize a "standard" map factory for testing the setters
        WmsLayerConfig layerConfig = new WmsLayerConfig(COMMON_SERVICE_ADDRESS, COMMON_LAYER_NAMES);
        this.standardWmsMapFactory = new WmsGetMapUrlFactory(layerConfig);
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
        assertEquals("Service Address Match", layerConfig.serviceAddress, wmsFactory.serviceAddress);
        assertEquals("WMS Version Match", layerConfig.wmsVersion, wmsFactory.wmsVersion);
        assertEquals("Layer Name Match", layerConfig.layerNames, wmsFactory.layerNames);
        assertEquals("Coordinate System Match", layerConfig.coordinateSystem, wmsFactory.coordinateSystem);
        assertNull("Null Style Names", wmsFactory.styleNames);
    }

    /**
     * Test the {@code setServiceAddress} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetServiceAddress() {

        String alteredServiceAddress = "testAddress"; // notional address
        this.standardWmsMapFactory.setServiceAddress(alteredServiceAddress);
        assertEquals("Update Service Address", alteredServiceAddress, this.standardWmsMapFactory.getServiceAddress());

        // Check the setter prevents null submissions
        try {
            this.standardWmsMapFactory.setServiceAddress(null);
            fail("null Submission Allowed for Service Address");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    /**
     * Test the {@code setWmsVersion} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetWmsVersion() {

        String updatedWmsVersion = "1.4.0"; // notional versioning
        this.standardWmsMapFactory.setWmsVersion(updatedWmsVersion);
        assertEquals("Update WMS Version", updatedWmsVersion, this.standardWmsMapFactory.getWmsVersion());

        // Check the setter prevents null submissions
        try {
            this.standardWmsMapFactory.setWmsVersion(null);
            fail("null Submission Allowed for WMS Version");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    /**
     * Test the {@code setLayerNames} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetLayerNames() {

        String updatedLayerNames = "layer1,layer2"; // notional
        this.standardWmsMapFactory.setLayerNames(updatedLayerNames);
        assertEquals("Update Layer Names", updatedLayerNames, this.standardWmsMapFactory.getLayerNames());

        // Check the setter prevents null submissions
        try {
            this.standardWmsMapFactory.setLayerNames(null);
            fail("null Submission Allowed for Layer Names");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    /**
     * Test the {@code setStyleNames} method.
     */
    @Test
    public void testSetStyleNames() {

        String updatedStyleNames = "style1,style2"; // notional
        this.standardWmsMapFactory.setStyleNames(updatedStyleNames);
        assertEquals("Update Style Names", updatedStyleNames, this.standardWmsMapFactory.getStyleNames());
    }

    /**
     * Test the {@code setCoordinateSystem} method. Testing includes ensuring null submissions throw an {@link
     * IllegalArgumentException}.
     */
    @Test
    public void testSetCoordinateSystem() {

        String updatedCoordinateSystem = "system"; // notional
        this.standardWmsMapFactory.setCoordinateSystem(updatedCoordinateSystem);
        assertEquals(
            "Update Coordinate System", updatedCoordinateSystem,
        this.standardWmsMapFactory.getCoordinateSystem());

        // Check the setter prevents null submissions
        try {
            this.standardWmsMapFactory.setCoordinateSystem(null);
            fail("null Submission Allowed for Coordinate System");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    /**
     * Test the {@code setTransparency} method.
     */
    @Test
    public void testSetTransparency() {
        boolean previousSetting = this.standardWmsMapFactory.isTransparent();
        this.standardWmsMapFactory.setTransparent(!previousSetting);
        assertFalse("Ensure Transparency Change", previousSetting == this.standardWmsMapFactory.isTransparent());
    }

    /**
     * Test the {@code setTimeString} method.
     */
    @Test
    public void testSetTimeString() {
        String updatedTimeString = "time"; // notional
        this.standardWmsMapFactory.setTimeString(updatedTimeString);
        assertEquals("Update Time String", updatedTimeString, this.standardWmsMapFactory.getTimeString());
    }

    /**
     * Test that null submission to required parameters result in a thrown {@link IllegalArgumentException}.
     */
    @Test
    public void testUrlForTile_ParameterCheck() {

        // check null's are not permitted
        try {
            this.standardWmsMapFactory.urlForTile(null, null);
            fail("null Parameters Pushed to urlForTile");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
        Tile tile = PowerMockito.mock(Tile.class);
        try {
            this.standardWmsMapFactory.urlForTile(tile, null);
            fail("null Parameters Pushed to urlForTile");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }
    }

    /**
     * Test that the query delimiter is properly placed in the url. Tests three scenarios in which no query delimiter is
     * provided on the service address, a query delmiter is appended to the end of a service address, and the service
     * address already includes a delimiter.
     */
    @Test
    public void testUrlForTile_QueryDelimiterPositioning() {

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

        // Provide the method a service address with a query delimiter appended
        wmsUrlFactory = new WmsGetMapUrlFactory(serviceAddress + '?', wmsVersion, layerNames, null);
        url = wmsUrlFactory.urlForTile(tile, imageFormat);
        checkQueryDelimiter(url);

        // Provide the method a service address with a query delimiter and existing parameters
        wmsUrlFactory = new WmsGetMapUrlFactory(serviceAddress + "?NOTIONAL=YES", wmsVersion, layerNames, null);
        url = wmsUrlFactory.urlForTile(tile, imageFormat);
        checkQueryDelimiter(url);
    }

    /**
     * Tests the generated url parameters match the properties of the {@link WmsGetMapUrlFactory} and {@link Tile} used
     * to generate the url. Additionally, checks the order of coordinates is correct given the coordinate system and
     * WMS version.
     */
    @Test
    public void testUrlForTile_Parameters() {

        // Essentially three different formats for describing the bounding box to the WMS Server. A fourth and fifth
        // test case provide for testing the optional STYLE and TIME parameters.
        // 1. WMS Version 1.3.0 and EPGSG:4326
        // 2. WMS Version 1.3.0 and CRS:84
        // 3. Other WMS Version
        // 4. Keep last factory and add notional styles parameter
        // 5. Keep last factory and add notional time parameter

        // Create mocked supporting objects
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(NOTIONAL_MIN_LAT);
        PowerMockito.when(sector.maxLatitude()).thenReturn(NOTIONAL_MAX_LAT);
        PowerMockito.when(sector.minLongitude()).thenReturn(NOTIONAL_MIN_LON);
        PowerMockito.when(sector.maxLongitude()).thenReturn(NOTIONAL_MAX_LON);
        // The urlForTile method accesses protected fields, thus notional objects are used
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);


        // A Standard Tile to use for generating URLs
        Tile tile = new Tile(sector, tileLevel, NOTIONAL_ROW, NOTIONAL_COLUMN);

        // Set the Service Address
        this.standardWmsMapFactory.setServiceAddress(COMMON_SERVICE_ADDRESS);

        // Settings for test one
        this.standardWmsMapFactory.setWmsVersion(COMMON_WMS_VERSION);
        this.standardWmsMapFactory.setCoordinateSystem(SYSTEM_EPSG4326);

        // Test 1
        String url = this.standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);
        UrlTestCase testCaseOne = new UrlTestCase(this.standardWmsMapFactory, tile);
        testCaseOne.testUrl(url);

        // Settings for test two
        this.standardWmsMapFactory.setCoordinateSystem(SYSTEM_CRS84);

        // Test 2
        url = this.standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);
        UrlTestCase testCaseTwo = new UrlTestCase(this.standardWmsMapFactory, tile);
        testCaseTwo.testUrl(url);

        // Settings for test three
        this.standardWmsMapFactory.setWmsVersion(NOTIONAL_WMS_VERSION);

        // Test 3
        url = this.standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);
        UrlTestCase testCaseThree = new UrlTestCase(this.standardWmsMapFactory, tile);
        testCaseThree.testUrl(url);

        // Settings for test four
        this.standardWmsMapFactory.setStyleNames("notionalstyle1,notionalstyle2");

        // Test 4
        url = this.standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);
        UrlTestCase testCaseFour = new UrlTestCase(this.standardWmsMapFactory, tile);
        testCaseFour.testUrl(url);

        // Settings for test five
        this.standardWmsMapFactory.setTimeString("1800-ZULU-NOTIONAL");

        // Test 5
        url = this.standardWmsMapFactory.urlForTile(tile, COMMON_IMAGE_FORMAT);
        UrlTestCase testCaseFive = new UrlTestCase(this.standardWmsMapFactory, tile);
        testCaseFive.testUrl(url);

    }

    /**
     * A utility class for testing url parameters. The provided generated urls will use {@link Pattern}s to check the
     * fields of the {@link WmsGetMapUrlFactory} are properly represented in the {@link String} url.
     */
    private static class UrlTestCase {

        /**
         * The {@link WmsGetMapUrlFactory} instance used to generate the url.
         */
        private final WmsGetMapUrlFactory urlFactory;

        /**
         * The {@link Tile} instance used to generate the url.
         */
        private final Tile tile;

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

        /**
         * Create an instance of {@link UrlTestCase} with the required objects.
         *
         * @param urlFactory the {@link WmsGetMapUrlFactory} used to generate the urls to be tested, cannot be null
         * @param tile the {@link Tile} used to generate the urls to be tested, cannot be null
         */
        private UrlTestCase(WmsGetMapUrlFactory urlFactory, Tile tile) {

            assert urlFactory != null;
            assert tile != null;

            this.urlFactory = urlFactory;
            this.tile = tile;

        }

        /**
         * Test the provided {@link String} url against the {@link WmsGetMapUrlFactory} and {@link Tile} objects
         * properties. This method will test that the parameters of {@link WmsGetMapUrlFactory} and {@link Tile} are
         * properly represented in the url. This method uses the {@link junit.framework.Assert} methods to communicate
         * test results.
         *
         * @param url the generated {@link String} url to be evaluated
         */
        void testUrl(String url) {

            // Test Service Description - WMS only at this time
            Matcher m = SERVICE_P.matcher(url);

            if (m.find()) {
                assertEquals("Test Service Parameter", COMMON_SERVICE_WMS, m.group(1));
            } else {
                fail("Service Did Not Match");
            }

            // Test Version
            m = VERSION_P.matcher(url);

            if (m.find()) {
                assertEquals("Test WMS Version Parameter", this.urlFactory.getWmsVersion(), m.group(1));
            } else {
                fail("WMS Version Did Not Match");
            }

            // Test Layers
            m = LAYERS_P.matcher(url);

            if (m.find()) {
                assertEquals("Test Layer List Parameter", this.urlFactory.getLayerNames(), m.group(1));
            } else {
                fail("Layer List Did Not Match");
            }

            // Test Styles
            if (this.urlFactory.getStyleNames() != null) {
                m = STYLES_P.matcher(url);
                if (m.find()) {
                    assertEquals("Test Style Parameter", this.urlFactory.getStyleNames(), m.group(1));
                } else {
                    fail("Style List Did Not Match");
                }
            }

            // Test CRS/SRS System
            m = CRS_P.matcher(url);

            if (m.find()) {
                assertEquals("Test Coordinate System Parameter", this.urlFactory.getCoordinateSystem(), m.group(1));
            } else {
                fail("Coordinate System Did Not Match");
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
                    if (this.urlFactory.getWmsVersion().equals(COMMON_WMS_VERSION)) {

                        if (this.urlFactory.getCoordinateSystem().equals(SYSTEM_CRS84)) {

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
                    assertEquals("Test Min Lat", NOTIONAL_MIN_LAT, coords[LAT_MIN], DELTA);
                    assertEquals("Test Max Lat", NOTIONAL_MAX_LAT, coords[LAT_MAX], DELTA);
                    assertEquals("Test Min Lon", NOTIONAL_MIN_LON, coords[LON_MIN], DELTA);
                    assertEquals("Test Max Lon", NOTIONAL_MAX_LON, coords[LON_MAX], DELTA);

                } else {

                    fail("Unable to delimit bounding box values.");

                }


            } else {

                fail("Unable to find bounding box values.");

            }

            // Test Width and Height
            m = WIDTH_P.matcher(url);
            if (m.find()) {
                assertEquals("Width Value Test", this.tile.level.tileWidth, Integer.parseInt(m.group(1)));
            } else {
                fail("Did not find Width parameter.");
            }

            m = HEIGHT_P.matcher(url);
            if (m.find()) {
                assertEquals("Height Value Test", this.tile.level.tileHeight, Integer.parseInt(m.group(1)));
            } else {
                fail("Did not find Height parameter.");
            }

            // Test Format
            m = FORMAT_P.matcher(url);
            if (m.find()) {
                assertEquals("Format Test", COMMON_IMAGE_FORMAT, m.group(1));
            } else {
                fail("Image Formate Not Detected");
            }

            // Test Transparency
            m = TRANSPARENT_P.matcher(url);
            if (m.find()) {
                assertEquals("Test Transparency", this.urlFactory.isTransparent(), Boolean.parseBoolean(m.group(1)));
            } else {
                fail("Transparency Not Matched");
            }

            // Test Time, if there is any
            String timeString = this.urlFactory.getTimeString();
            if (timeString != null && !timeString.isEmpty()) {
                m = TIME_P.matcher(url);
                if (m.find()) {
                    assertEquals("Time Test", this.urlFactory.getTimeString(), m.group(1));
                } else {
                    fail("Time Did Not Match");
                }
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
