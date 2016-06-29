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
import gov.nasa.worldwind.util.Logger;

import static junit.framework.Assert.assertEquals;
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

        // Test null submissions throw exceptions
        try {
            WmsLayer wmsLayer = new WmsLayer(null, null, metersPerPixel, null);
            fail("provided null parameters");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        try {
            WmsLayer wmsLayer = new WmsLayer(sector, null, metersPerPixel, null);
            fail("provided null parameters");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        try {
            WmsLayer wmsLayer = new WmsLayer(sector, globe, metersPerPixel, null);
            fail("provided null parameters");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex);
        }

        WmsLayer wmsLayer = new WmsLayer(sector, globe, metersPerPixel, wmsLayerConfig);

        // check that the layer was created by the constructor
        assertNotNull("layer created", wmsLayer);

        // check the mock radius is providing the resolution level
        assertEquals("detail levels", anticipatedLevels, wmsLayer.getLevelSet().numLevels());
    }
}
