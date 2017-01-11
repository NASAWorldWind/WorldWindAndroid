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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.Logger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class WmsLayerTest {

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    /**
     * Test default constructor of {@link WmsLayer}.
     */
    @Test
    public void testConstructor() {

        assertNotNull("assure WmsLayer creation", new WmsLayer());
    }

    /**
     * Test the three parameter constructor including throwing of {@link IllegalArgumentException} when null is provided
     * for required parameters.
     */
    @Test
    public void testConstructor_ThreeParameter() {

        // Create mock objects for testing
        Sector sector = PowerMockito.mock(Sector.class);
        double metersPerPixel = 0.5; // notional value
        WmsLayerConfig wmsLayerConfig = new WmsLayerConfig("testServiceAddress", "testLayerList");

        // Test null/invalid submissions throw exceptions
        try {

            WmsLayer wmsLayer = new WmsLayer(null, metersPerPixel, null);

            fail("submitted null parameters");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        try {

            WmsLayer wmsLayer = new WmsLayer(sector, -metersPerPixel, null);

            fail("submitted illegal parameters");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        try {

            WmsLayer wmsLayer = new WmsLayer(sector, metersPerPixel, null);

            fail("submitted null parameter");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        WmsLayer wmsLayer = new WmsLayer(sector, metersPerPixel, wmsLayerConfig);

        assertNotNull("object creation", wmsLayer);
    }

    /**
     * Test the four parameter constructor including throwing of {@link IllegalArgumentException} when null is provided
     * for required parameters.
     */
    @Test
    public void testConstructor_FourParameter() {

        // Test Values
        double mockRadius = 700000.0; // notional for testing only
        double metersPerPixel = 0.5; // notional for testing only
        // The anticipated levels reflect the altered radius of the mocked Globe object provided. The WGS84 globe would
        // provide 18 levels versus the 15 with the mocked radius.
        int anticipatedLevels = 15;

        // Create mock objects for testing
        Sector sector = PowerMockito.mock(Sector.class);
        Globe globe = PowerMockito.mock(Globe.class);
        PowerMockito.when(globe.getEquatorialRadius()).thenReturn(mockRadius);
        WmsLayerConfig wmsLayerConfig = new WmsLayerConfig("testServiceAddress", "testLayerList");

        // Test null/invalid submissions throw exceptions
        try {

            WmsLayer wmsLayer = new WmsLayer(null, null, metersPerPixel, null);

            fail("provided null parameters");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        try {

            WmsLayer wmsLayer = new WmsLayer(sector, null, metersPerPixel, null);

            fail("provided null parameters");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        try {

            WmsLayer wmsLayer = new WmsLayer(sector, globe, -metersPerPixel, null);

            fail("provided illegal parameters");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        try {

            WmsLayer wmsLayer = new WmsLayer(sector, globe, metersPerPixel, null);

            fail("provided null parameters");

        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);

        }

        WmsLayer wmsLayer = new WmsLayer(sector, globe, metersPerPixel, wmsLayerConfig);

        // check that the layer was created by the constructor
        assertNotNull("layer created", wmsLayer);

        // check the mock radius is providing the resolution level
        assertEquals("detail levels", anticipatedLevels, ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels());
    }

    /**
     * Test the three parameter setConfiguration method for throwing of {@link IllegalArgumentException} when null is
     * provided for required parameters.
     */
    @Test
    public void testSetConfiguration_ThreeParameter_NullParameters() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;

        // initial object for testing method
        WmsLayer wmsLayer = new WmsLayer(initialSector, metersPerPixel, initialWmsLayerConfig);

        // test null/invalid submissions throw exceptions
        try {

            wmsLayer.setConfiguration(null, metersPerPixel, null);

            fail("provided null arguments");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }

        try {

            wmsLayer.setConfiguration(initialSector, -metersPerPixel, null);

            fail("provided invalid argument");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }

        try {

            wmsLayer.setConfiguration(initialSector, metersPerPixel, null);

            fail("provided null arguments");
        } catch (IllegalArgumentException ex) {

            assertNotNull("null exception thrown", ex);
        }
    }

    /**
     * Test the three parameter {@code setConfiguration} method updates when the {@link Sector} is changed.
     */
    @Test
    public void testSetConfiguration_ThreeParameter_SectorUpdate() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;
        WmsLayer wmsLayer = new WmsLayer(initialSector, metersPerPixel, initialWmsLayerConfig);
        double alternativeLatMin = -45.0;
        double alternativeLonMin = 50.0;
        double alternativeDeltaLat = 5.0;
        double alternativeDeltaLon = 2.0;
        Sector alternativeSector
            = new Sector(alternativeLatMin, alternativeLonMin, alternativeDeltaLat, alternativeDeltaLon);

        wmsLayer.setConfiguration(alternativeSector, metersPerPixel, initialWmsLayerConfig);
        Sector sector = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().sector;

        assertEquals("sector updated", alternativeSector, sector);
    }

    /**
     * Test the three parameter {@code setConfiguration} method when the meters per pixel parameter has been changed.
     */
    @Test
    public void testSetConfiguration_ThreeParameter_MetersPerPixelUpdate() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;
        WmsLayer wmsLayer = new WmsLayer(initialSector, metersPerPixel, initialWmsLayerConfig);
        double alternativeMetersPerPixel = 10.0;
        int originalNumberOfLevels = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels();

        wmsLayer.setConfiguration(initialSector, alternativeMetersPerPixel, initialWmsLayerConfig);
        int numberOfLevels = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels();

        // assertEquals is not used as the determination of the number of levels is a function of LevelSetConfig
        assertFalse("levels updated", originalNumberOfLevels == numberOfLevels);
    }

    /**
     * Test the four parameter setConfiguration method for throwing of {@link IllegalArgumentException} when null is
     * provided for required parameters.
     */
    @Test
    public void testSetConfiguration_FourParameter_NullParameters() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        double notionalGlobeRadius = 3000000.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        Globe initialGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(initialGlobe.getEquatorialRadius()).thenReturn(notionalGlobeRadius);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;
        WmsLayer wmsLayer = new WmsLayer(initialSector, initialGlobe, metersPerPixel, initialWmsLayerConfig);

        // test null/invalid submissions throw exceptions
        try {

            wmsLayer.setConfiguration(null, null, metersPerPixel, null);

            fail("provided null arguments");
        } catch (IllegalArgumentException ex) {

            assertNotNull(ex);
        }

        try {

            wmsLayer.setConfiguration(initialSector, null, metersPerPixel, null);

            fail("provided null arguments");
        } catch (IllegalArgumentException ex) {

            assertNotNull(ex);
        }

        try {

            wmsLayer.setConfiguration(initialSector, initialGlobe, -metersPerPixel, null);

            fail("provided invalid argument");
        } catch (IllegalArgumentException ex) {

            assertNotNull(ex);
        }

        try {

            wmsLayer.setConfiguration(initialSector, initialGlobe, metersPerPixel, null);

            fail("provided null arguments");
        } catch (IllegalArgumentException ex) {

            assertNotNull(ex);
        }
    }

    /**
     * Test the four parameter {@code setConfiguration} method updates when the {@link Sector} is changed.
     */
    @Test
    public void testSetConfiguration_FourParameter_SectorUpdate() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        double notionalGlobeRadius = 3000000.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        Globe initialGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(initialGlobe.getEquatorialRadius()).thenReturn(notionalGlobeRadius);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;
        WmsLayer wmsLayer = new WmsLayer(initialSector, initialGlobe, metersPerPixel, initialWmsLayerConfig);
        double alternativeLatMin = -45.0;
        double alternativeLonMin = 50.0;
        double alternativeDeltaLat = 5.0;
        double alternativeDeltaLon = 2.0;
        Sector alternativeSector
            = new Sector(alternativeLatMin, alternativeLonMin, alternativeDeltaLat, alternativeDeltaLon);

        wmsLayer.setConfiguration(alternativeSector, initialGlobe, metersPerPixel, initialWmsLayerConfig);
        Sector sector = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().sector;

        assertEquals("sector updated", alternativeSector, sector);
    }

    /**
     * Test the four parameter {@code setConfiguration} method updates when the {@link Globe} is changed.
     */
    @Test
    public void testSetConfiguration_FourParameter_GlobeUpdate() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        double notionalGlobeRadius = 3000000.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        Globe initialGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(initialGlobe.getEquatorialRadius()).thenReturn(notionalGlobeRadius);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;
        WmsLayer wmsLayer = new WmsLayer(initialSector, initialGlobe, metersPerPixel, initialWmsLayerConfig);
        int initialLayers = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels();
        Globe alternativeGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(alternativeGlobe.getEquatorialRadius()).thenReturn(2 * notionalGlobeRadius);

        wmsLayer.setConfiguration(initialSector, alternativeGlobe, metersPerPixel, initialWmsLayerConfig);
        int numberOfLevels = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels();

        assertFalse("layer levels updated by globe object change", initialLayers == numberOfLevels);
    }

    /**
     * Test the four parameter {@code setConfiguration} method updates when the meters per pixel is changed.
     */
    @Test
    public void testSetConfiguration_FourParameter_MetersPerPixelUpdate() {

        // Mocked objects facilitating testing
        double minLat = 10.0;
        double deltaLat = 1.0;
        double minLon = -95.0;
        double deltaLon = 2.0;
        double notionalGlobeRadius = 3000000.0;
        Sector initialSector = new Sector(minLat, minLon, deltaLat, deltaLon);
        Globe initialGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(initialGlobe.getEquatorialRadius()).thenReturn(notionalGlobeRadius);
        String initialNotionalServiceAddress = "notionalServiceAddress";
        String initialNotionalLayerList = "notionalLayerList";
        WmsLayerConfig initialWmsLayerConfig
            = new WmsLayerConfig(initialNotionalServiceAddress, initialNotionalLayerList);
        double metersPerPixel = 0.5;
        WmsLayer wmsLayer = new WmsLayer(initialSector, initialGlobe, metersPerPixel, initialWmsLayerConfig);
        double alternativeMetersPerPixel = 10.0;
        int originalNumberOfLevels = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels();

        wmsLayer.setConfiguration(initialSector, initialGlobe, alternativeMetersPerPixel, initialWmsLayerConfig);
        int numberOfLevels = ((TiledSurfaceImage) wmsLayer.getRenderable(0)).getLevelSet().numLevels();

        assertFalse("levels updated", originalNumberOfLevels == numberOfLevels);
    }
}
