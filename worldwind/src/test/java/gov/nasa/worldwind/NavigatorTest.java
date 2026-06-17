/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.ProjectionWgs84;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Logger.class)
public class NavigatorTest {

    private static final double ANGLE_DELTA = 1.0e-7;

    private static final double ALTITUDE_DELTA = 1.0e-6;

    private Globe globe;

    private Navigator navigator;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Logger.class);

        this.globe = new Globe(WorldWind.WGS84_ELLIPSOID, new ProjectionWgs84());
        this.navigator = new Navigator();
        this.navigator.setLatitude(34.2);
        this.navigator.setLongitude(-118.2);
        this.navigator.setAltitude(20000);
        this.navigator.setHeading(35);
        this.navigator.setTilt(55);
        this.navigator.setRoll(3);
    }

    @Test
    public void testCameraToLookAt_UsesSuppliedLookAtPosition() {
        LookAt baseline = this.navigator.getAsLookAt(this.globe, new LookAt());
        Camera camera = this.navigator.getAsCamera(this.globe, new Camera());

        Position terrainPosition = new Position(baseline.latitude, baseline.longitude, 1500);
        LookAt terrainLookAt = this.navigator.cameraToLookAt(this.globe, camera, terrainPosition, new LookAt());

        assertEquals("latitude", terrainPosition.latitude, terrainLookAt.latitude, ANGLE_DELTA);
        assertEquals("longitude", terrainPosition.longitude, terrainLookAt.longitude, ANGLE_DELTA);
        assertEquals("altitude", terrainPosition.altitude, terrainLookAt.altitude, ALTITUDE_DELTA);
        assertEquals("heading", baseline.heading, terrainLookAt.heading, ANGLE_DELTA);
        assertEquals("tilt", baseline.tilt, terrainLookAt.tilt, ANGLE_DELTA);
        assertTrue("range", terrainLookAt.range < baseline.range);
    }

    @Test
    public void testCameraToLookAt_WithNullPositionMatchesLegacyPath() {
        Camera camera = this.navigator.getAsCamera(this.globe, new Camera());

        LookAt legacyLookAt = this.navigator.cameraToLookAt(this.globe, camera, new LookAt());
        LookAt nullPositionLookAt = this.navigator.cameraToLookAt(this.globe, camera, (Position) null, new LookAt());

        assertEquals("latitude", legacyLookAt.latitude, nullPositionLookAt.latitude, ANGLE_DELTA);
        assertEquals("longitude", legacyLookAt.longitude, nullPositionLookAt.longitude, ANGLE_DELTA);
        assertEquals("altitude", legacyLookAt.altitude, nullPositionLookAt.altitude, ALTITUDE_DELTA);
        assertEquals("range", legacyLookAt.range, nullPositionLookAt.range, ALTITUDE_DELTA);
        assertEquals("heading", legacyLookAt.heading, nullPositionLookAt.heading, ANGLE_DELTA);
        assertEquals("tilt", legacyLookAt.tilt, nullPositionLookAt.tilt, ANGLE_DELTA);
        assertEquals("roll", legacyLookAt.roll, nullPositionLookAt.roll, ANGLE_DELTA);
    }
}
