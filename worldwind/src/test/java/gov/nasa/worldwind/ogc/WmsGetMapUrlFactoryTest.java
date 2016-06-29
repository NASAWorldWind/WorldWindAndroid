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
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class WmsGetMapUrlFactoryTest {

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testUrlForTile_QueryDelimiterPositioning() {

        // Values used for the Blue Marble
        String serviceAddress = "http://worldwind25.arc.nasa.gov/wms";
        String wmsVersion = "1.3.0";
        String layerNames = "BlueMarble-200405";
        String imageFormat = "image/png";

        // Mocking of method object parameters - notional values
        int tileHeight = 5;
        int tileWidth = 4;
        double minLat = 20.0;
        double maxLat = 30.0;
        double minLon = -90.0;
        double maxLon = -80.0;
        LevelSet levelSet = new LevelSet();
        Level tileLevel = new Level(levelSet, 0, 0.1);
        Sector sector = PowerMockito.mock(Sector.class);
        PowerMockito.when(sector.minLatitude()).thenReturn(minLat);
        PowerMockito.when(sector.maxLatitude()).thenReturn(maxLat);
        PowerMockito.when(sector.minLongitude()).thenReturn(minLon);
        PowerMockito.when(sector.maxLongitude()).thenReturn(maxLon);
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
