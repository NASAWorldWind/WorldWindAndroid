/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.GlobeWgs84;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class BoundingBoxTest {

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);
    }

    @Test
    public void testConstructor() throws Exception {

        BoundingBox bb = new BoundingBox();

        assertNotNull(bb);

    }

    @Test
    public void testSetToSector() throws Exception {
        Sector sector = Sector.fromDegrees(-0.5, -0.5, 1d, 1d);
        Globe globe = new GlobeWgs84();
        double minElevation = 0;
        double maxElevation = 1000;
        BoundingBox bb = new BoundingBox();

        bb.setToSector(sector, globe, minElevation, maxElevation);

        fail("The test case is a stub.");

    }
}