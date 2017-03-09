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
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.wms.WmsCapabilities;
import gov.nasa.worldwind.ogc.wms.WmsCapability;
import gov.nasa.worldwind.ogc.wms.WmsLayer;
import gov.nasa.worldwind.ogc.wms.WmsRequest;
import gov.nasa.worldwind.ogc.wms.WmsRequestOperation;
import gov.nasa.worldwind.ogc.wms.WmsScaleHint;
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

    public static final List<String> DEFAULT_REFERENCE_SYSTEMS = Arrays.asList("CRS:84", "EPSG:4326");

    public static final List<String> DEFAULT_IMAGE_FORMATS = Arrays.asList("image/png", "image/jpeg");

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
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
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
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
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
        PowerMockito.when(wmsCapabilities.getCapability().getRequest().getGetMap().getGetUrl()).thenReturn(null);
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
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
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<String> modifiedReferenceSystems = Arrays.asList("CRS:84");
        PowerMockito.when(wmsLayer.getReferenceSystems()).thenReturn(modifiedReferenceSystems);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
        LayerFactory layerFactory = new LayerFactory();

        WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Alternate Coordinate System", "CRS:84", wmsLayerConfig.coordinateSystem);
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_InvalidCoordinateSystem() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<String> modifiedReferenceSystems = Arrays.asList("EPSG:1234");
        PowerMockito.when(wmsLayer.getReferenceSystems()).thenReturn(modifiedReferenceSystems);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
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
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<String> modifiedImageFormats = Arrays.asList("image/dds", "image/jpg");
        PowerMockito.when(wmsCapabilities.getCapability().getRequest().getGetMap().getFormats()).thenReturn(modifiedImageFormats);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
        LayerFactory layerFactory = new LayerFactory();

        WmsLayerConfig wmsLayerConfig = layerFactory.getLayerConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Alternate Image Format", "image/jpg", wmsLayerConfig.imageFormat);
    }

    @Test
    public void testGetLayerConfigFromWmsCapabilities_InvalidImageFormat() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<String> modifiedImageFormats = Arrays.asList("image/dds", "image/never");
        PowerMockito.when(wmsCapabilities.getCapability().getRequest().getGetMap().getFormats()).thenReturn(modifiedImageFormats);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
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
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Bounding Box", new Sector().setFullSphere(), levelSetConfig.sector);
        assertEquals("Number of Levels", 47, levelSetConfig.numLevels);
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_CoarseScaleDenominator() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        PowerMockito.when(wmsLayer.getMinScaleDenominator()).thenReturn(1e13);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Number of Levels", 1, levelSetConfig.numLevels);
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_NullScaleDenominator() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        PowerMockito.when(wmsLayer.getMinScaleDenominator()).thenReturn(null);
        PowerMockito.when(wmsLayer.getScaleHint().getMin()).thenReturn(null);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
        LayerFactory layerFactory = new LayerFactory();

        LevelSetConfig levelSetConfig = layerFactory.getLevelSetConfigFromWmsCapabilities(layerCapabilities);

        assertEquals("Number of Levels", 20, levelSetConfig.numLevels);
    }

    @Test
    public void testGetLevelSetConfigFromWmsCapabilities_CoarseScaleHint() throws Exception {
        WmsCapabilities wmsCapabilities = getBoilerPlateWmsCapabilities();
        WmsLayer wmsLayer = wmsCapabilities.getNamedLayer(DEFAULT_LAYER_NAME);
        PowerMockito.when(wmsLayer.getMinScaleDenominator()).thenReturn(null);
        PowerMockito.when(wmsLayer.getScaleHint().getMin()).thenReturn(1336.34670162102);
        List<WmsLayer> layerCapabilities = Arrays.asList(wmsLayer);
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
        WmsRequestOperation mockedRequestOperation = PowerMockito.mock(WmsRequestOperation.class);
        PowerMockito.when(mockedRequestOperation.getGetUrl()).thenReturn(DEFAULT_REQUEST_URL);
        PowerMockito.when(mockedRequestOperation.getFormats()).thenReturn(DEFAULT_IMAGE_FORMATS);
        WmsRequest mockedRequest = PowerMockito.mock(WmsRequest.class);
        PowerMockito.when(mockedRequest.getGetMap()).thenReturn(mockedRequestOperation);
        WmsCapability mockedCapability = PowerMockito.mock(WmsCapability.class);
        PowerMockito.when(mockedCapability.getRequest()).thenReturn(mockedRequest);
        WmsScaleHint mockedScaleHint = PowerMockito.mock(WmsScaleHint.class);
        PowerMockito.when(mockedScaleHint.getMin()).thenReturn(null);
        WmsLayer mockedLayer = PowerMockito.mock(WmsLayer.class);
        PowerMockito.when(mockedLayer.getName()).thenReturn(DEFAULT_LAYER_NAME);
        PowerMockito.when(mockedLayer.getReferenceSystems()).thenReturn(DEFAULT_REFERENCE_SYSTEMS);
        PowerMockito.when(mockedLayer.getMinScaleDenominator()).thenReturn(DEFAULT_MIN_SCALE_DENOMINATOR);
        PowerMockito.when(mockedLayer.getScaleHint()).thenReturn(mockedScaleHint);
        PowerMockito.when(mockedLayer.getGeographicBoundingBox()).thenReturn(new Sector().setFullSphere());
        PowerMockito.when(mockedLayer.getTitle()).thenReturn(DEFAULT_TITLE);
        WmsCapabilities mockedCapabilities = PowerMockito.mock(WmsCapabilities.class);
        PowerMockito.when(mockedCapabilities.getVersion()).thenReturn(DEFAULT_VERSION);
        PowerMockito.when(mockedCapabilities.getCapability()).thenReturn(mockedCapability);
        PowerMockito.when(mockedCapabilities.getNamedLayer(anyString())).thenReturn(mockedLayer);
        PowerMockito.when(mockedLayer.getCapability()).thenReturn(mockedCapability);
        PowerMockito.when(mockedCapability.getCapabilities()).thenReturn(mockedCapabilities);
        return mockedCapabilities;
    }
}
