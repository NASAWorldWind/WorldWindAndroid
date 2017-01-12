/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.wms.WmsCapabilities;
import gov.nasa.worldwind.ogc.wms.WmsLayerCapabilities;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class LayerFactoryTest {

    public static final String DEFAULT_LAYER_NAME = "LayerName";

    public static final String DEFAULT_REQUEST_URL = "http://example.com";

    public static final Set<String> DEFAULT_REFERENCE_SYSTEMS = new HashSet<>(Arrays.asList("CRS:84", "EPSG:4326"));

    public static final Set<String> DEFAULT_IMAGE_FORMATS = new HashSet<>(Arrays.asList("image/png", "image/jpeg"));

    public static final double DEFAULT_MIN_SCALE_DENOMINATOR = 1e-6;

    public static final String DEFAULT_VERSION = "1.3.0";

    public static final String DEFAULT_TITLE = "Layer Title";

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_Nominal() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Version", DEFAULT_VERSION, wmsLayerConfig.wmsVersion);
        assertEquals("Layer Name", DEFAULT_LAYER_NAME, wmsLayerConfig.layerNames);
        assertEquals("Request URL", DEFAULT_REQUEST_URL, wmsLayerConfig.serviceAddress);
        assertEquals("Reference Systems", "EPSG:4326", wmsLayerConfig.coordinateSystem);
        assertEquals("Image Format", "image/png", wmsLayerConfig.imageFormat);
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_InvalidVersion() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        // Invalid WMS version
        PowerMockito.when(wmsCapabilities.getVersion()).thenReturn("1.2.1");
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        try {
            WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);
            fail("Invalid Version");
        } catch (RuntimeException e) {
            assertNotNull("Invalid Version", e);
        }
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_InvalidRequestUrl() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        // Invalid WMS version
        PowerMockito.when(wmsCapabilities.getRequestURL(anyString(), anyString())).thenReturn(null);
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        try {
            WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);
            fail("Invalid Request URL");
        } catch (RuntimeException e) {
            assertNotNull("Invalid Request URL", e);
        }
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_OtherCoordinateSystem() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        Set<String> modifiedReferenceSystems = new HashSet<>(Arrays.asList("CRS:84"));
        PowerMockito.when(wmsLayerCapabilities.getReferenceSystem()).thenReturn(modifiedReferenceSystems);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Alternate Coordinate System", "CRS:84", wmsLayerConfig.coordinateSystem);
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_InvalidCoordinateSystem() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        Set<String> modifiedReferenceSystems = new HashSet<>(Arrays.asList("EPSG:1234"));
        PowerMockito.when(wmsLayerCapabilities.getReferenceSystem()).thenReturn(modifiedReferenceSystems);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        try {
            WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);
            fail("Invalid Coordinate System");
        } catch (RuntimeException e) {
            assertNotNull("Invalid Coordinate System", e);
        }
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_OtherImageFormat() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        Set<String> modifiedImageFormats = new HashSet<>(Arrays.asList("image/dds", "image/jpg"));
        PowerMockito.when(wmsCapabilities.getImageFormats()).thenReturn(modifiedImageFormats);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Alternate Image Format", "image/jpg", wmsLayerConfig.imageFormat);
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_InvalidImageFormat() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        Set<String> modifiedImageFormats = new HashSet<>(Arrays.asList("image/dds", "image/never"));
        PowerMockito.when(wmsCapabilities.getImageFormats()).thenReturn(modifiedImageFormats);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        try {
            WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);
            fail("Invalid Image Format");
        } catch (RuntimeException e) {
            assertNotNull("Invalid Image Format");
        }
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_Nominal() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Bounding Box", new Sector().setFullSphere(), levelSetConfig.sector);
        assertEquals("Number of Levels", 47, levelSetConfig.numLevels);
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_CoarseScaleDenominator() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        PowerMockito.when(wmsLayerCapabilities.getMinScaleDenominator()).thenReturn(1e13);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Number of Levels", 1, levelSetConfig.numLevels);
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_NullScaleDenominator() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        PowerMockito.when(wmsLayerCapabilities.getMinScaleDenominator()).thenReturn(null);
        PowerMockito.when(wmsLayerCapabilities.getMinScaleHint()).thenReturn(null);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Number of Levels", 13, levelSetConfig.numLevels);
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_CoarseScaleHint() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(DEFAULT_LAYER_NAME);
        PowerMockito.when(wmsLayerCapabilities.getMinScaleDenominator()).thenReturn(null);
        PowerMockito.when(wmsLayerCapabilities.getMinScaleHint()).thenReturn(1336.34670162102);
        List<WmsLayerCapabilities> layerCapabilities = Arrays.asList(wmsLayerCapabilities);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Number of Levels", 5, levelSetConfig.numLevels);
    }

    protected static class TestCallback implements LayerFactory.Callback {

        @Override
        public void creationSucceeded(LayerFactory factory, Layer layer) {
            // ignore
        }

        @Override
        public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
            // ignore
        }
    }

    public static WmsCapabilities getBoilerPlateWmsCapabilities() {
        WmsLayerCapabilities mockedLayerCapabilities = PowerMockito.mock(WmsLayerCapabilities.class);
        PowerMockito.when(mockedLayerCapabilities.getName()).thenReturn(DEFAULT_LAYER_NAME);
        PowerMockito.when(mockedLayerCapabilities.getReferenceSystem()).thenReturn(DEFAULT_REFERENCE_SYSTEMS);
        PowerMockito.when(mockedLayerCapabilities.getMinScaleDenominator()).thenReturn(DEFAULT_MIN_SCALE_DENOMINATOR);
        PowerMockito.when(mockedLayerCapabilities.getGeographicBoundingBox()).thenReturn(new Sector().setFullSphere());
        PowerMockito.when(mockedLayerCapabilities.getTitle()).thenReturn(DEFAULT_TITLE);
        WmsCapabilities mockedCapabilities = PowerMockito.mock(WmsCapabilities.class);
        PowerMockito.when(mockedCapabilities.getVersion()).thenReturn(DEFAULT_VERSION);
        PowerMockito.when(mockedCapabilities.getRequestURL(anyString(), anyString())).thenReturn(DEFAULT_REQUEST_URL);
        PowerMockito.when(mockedCapabilities.getLayerByName(anyString())).thenReturn(mockedLayerCapabilities);
        PowerMockito.when(mockedCapabilities.getImageFormats()).thenReturn(DEFAULT_IMAGE_FORMATS);
        PowerMockito.when(mockedLayerCapabilities.getServiceCapabilities()).thenReturn(mockedCapabilities);
        return mockedCapabilities;
    }
}
