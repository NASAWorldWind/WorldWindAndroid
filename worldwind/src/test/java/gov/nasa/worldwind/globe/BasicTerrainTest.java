/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class BasicTerrainTest {

    private static final double OFFICIAL_WGS84_SEMI_MAJOR_AXIS = 6378137.0;

    private static final double OFFICIAL_WGS84_EC2 = 6.69437999014E-3;

    private static final double TOLERANCE = 0.0015; // Cartesian XYZ components must be within 1.5 millimeters

    private Globe globe;

    private Terrain terrain;

    private static Vec3 officialWgs84Ecef(double latitude, double longitude, double altitude) {
        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);
        double cosLat = Math.cos(radLat);
        double sinLat = Math.sin(radLat);
        double cosLon = Math.cos(radLon);
        double sinLon = Math.sin(radLon);

        double normal = OFFICIAL_WGS84_SEMI_MAJOR_AXIS / Math.sqrt(1.0 - OFFICIAL_WGS84_EC2 * sinLat * sinLat);

        double x = (normal + altitude) * cosLat * cosLon;
        double y = (normal + altitude) * cosLat * sinLon;
        double z = (normal * (1.0 - OFFICIAL_WGS84_EC2) + altitude) * sinLat;

        return new Vec3(x, y, z);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private static Vec3 worldWindEcef(Vec3 officialEcef) {
        double x = officialEcef.y;
        double y = officialEcef.z;
        double z = officialEcef.x;

        return new Vec3(x, y, z);
    }

    private static Vec3 bilinearCentroid(Vec3 sw, Vec3 se, Vec3 nw, Vec3 ne) {
        double px = (sw.x * 0.25) + (se.x * 0.25) + (nw.x * 0.25) + (ne.x * 0.25);
        double py = (sw.y * 0.25) + (se.y * 0.25) + (nw.y * 0.25) + (ne.y * 0.25);
        double pz = (sw.z * 0.25) + (se.z * 0.25) + (nw.z * 0.25) + (ne.z * 0.25);

        return new Vec3(px, py, pz);
    }

    @Before
    public void setUp() {
        // Mock all the static methods in Logger
        PowerMockito.mockStatic(Logger.class);

        // Create the globe object used by the test
        this.globe = new Globe(WorldWind.WGS84_ELLIPSOID, new ProjectionWgs84());

        // Create the terrain object used by the test
        this.terrain = new BasicTerrain();

        // Add a terrain tile used to the mocked terrain
        LevelSet levelSet = new LevelSet(new Sector().setFullSphere(), 1.0, 1, 5, 5); // tiles with 5x5 vertices
        TerrainTile tile = new TerrainTile(new Sector(0, 0, 1, 1), levelSet.firstLevel(), 90, 180);
        ((BasicTerrain) this.terrain).addTile(tile);

        // Populate the terrain tile's geometry
        int tileWidth = tile.level.tileWidth;
        int tileHeight = tile.level.tileHeight;
        int rowStride = (tileWidth + 2) * 3;
        float[] points = new float[(tileWidth + 2) * (tileHeight + 2) * 3];
        Vec3 tileOrigin = this.globe.geographicToCartesian(0.5, 0.5, 0.0, new Vec3());
        this.globe.geographicToCartesianGrid(tile.sector, tileWidth, tileHeight, null, 1.0f, tileOrigin, points, rowStride + 3, rowStride);
        this.globe.geographicToCartesianBorder(tile.sector, tileWidth + 2, tileHeight + 2, 0.0f, tileOrigin, points);
        tile.setOrigin(tileOrigin);
        tile.setPoints(points);
    }

    @After
    public void tearDown() throws Exception {
        // Release the terrain object
        this.terrain = null;
    }

    @Test
    public void testGetSector() throws Exception {
        Sector expected = new Sector(0, 0, 1, 1);

        Sector actual = this.terrain.getSector();

        assertEquals("sector", expected, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_SouthwestCorner() throws Exception {
        double lat = 0.0;
        double lon = 0.0;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint Southwest corner x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Southwest corner y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Southwest corner z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Southwest corner return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_SoutheastCorner() throws Exception {
        double lat = 0.0;
        double lon = 1.0;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint Southeast corner x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Southeast corner y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Southeast corner z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Southeast corner return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_NorthwestCorner() throws Exception {
        double lat = 1.0;
        double lon = 0.0;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint Northwest corner x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Northwest corner y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Northwest corner z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Northwest corner return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_NortheastCorner() throws Exception {
        double lat = 1.0;
        double lon = 1.0;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint Northeast corner x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Northeast corner y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Northeast corner z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Northeast corner return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_SouthEdge() throws Exception {
        double lat = 0.0;
        double lon = 0.5;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint South edge x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint South edge y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint South edge z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint South edge return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_NorthEdge() throws Exception {
        double lat = 1.0;
        double lon = 0.5;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint North edge x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint North edge y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint North edge z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint North edge return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_WestEdge() throws Exception {
        double lat = 0.5;
        double lon = 0.0;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint West edge x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint West edge y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint West edge z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint West edge return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_EastEdge() throws Exception {
        double lat = 0.5;
        double lon = 1.0;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint East edge x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint East edge y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint East edge z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint East edge return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_SouthwestCell() throws Exception {
        Vec3 sw = officialWgs84Ecef(0.0, 0.0, 0.0);
        Vec3 se = officialWgs84Ecef(0.0, 0.25, 0.0);
        Vec3 nw = officialWgs84Ecef(0.25, 0.0, 0.0);
        Vec3 ne = officialWgs84Ecef(0.25, 0.25, 0.0);
        Vec3 expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(0.125, 0.125, actual);

        assertEquals("surfacePoint Southwest cell x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Southwest cell y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Southwest cell z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Southwest cell return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_SoutheastCell() throws Exception {
        Vec3 sw = officialWgs84Ecef(0.0, 0.75, 0.0);
        Vec3 se = officialWgs84Ecef(0.0, 1.0, 0.0);
        Vec3 nw = officialWgs84Ecef(0.25, 0.75, 0.0);
        Vec3 ne = officialWgs84Ecef(0.25, 1.0, 0.0);
        Vec3 expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(0.125, 0.875, actual);

        assertEquals("surfacePoint Southeast cell x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Southeast cell y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Southeast cell z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Southeast cell return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_NorthwestCell() throws Exception {
        Vec3 sw = officialWgs84Ecef(0.75, 0.0, 0.0);
        Vec3 se = officialWgs84Ecef(0.75, 0.25, 0.0);
        Vec3 nw = officialWgs84Ecef(1.0, 0.0, 0.0);
        Vec3 ne = officialWgs84Ecef(1.0, 0.25, 0.0);
        Vec3 expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(0.875, 0.125, actual);

        assertEquals("surfacePoint Northwest cell x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Northwest cell y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Northwest cell z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Northwest cell return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_NortheastCell() throws Exception {
        Vec3 sw = officialWgs84Ecef(0.75, 0.75, 0.0);
        Vec3 se = officialWgs84Ecef(0.75, 1.0, 0.0);
        Vec3 nw = officialWgs84Ecef(1.0, 0.75, 0.0);
        Vec3 ne = officialWgs84Ecef(1.0, 1.0, 0.0);
        Vec3 expected = worldWindEcef(bilinearCentroid(sw, se, nw, ne));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(0.875, 0.875, actual);

        assertEquals("surfacePoint Northeast cell x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint Northeast cell y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint Northeast cell z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint Northeast cell return", expectedReturn, actualReturn);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testSurfacePoint_Centroid() throws Exception {
        double lat = 0.5;
        double lon = 0.5;
        double alt = 0.0;
        Vec3 expected = worldWindEcef(officialWgs84Ecef(lat, lon, alt));
        boolean expectedReturn = true;

        Vec3 actual = new Vec3();
        boolean actualReturn = this.terrain.surfacePoint(lat, lon, actual);

        assertEquals("surfacePoint centroid x", expected.x, actual.x, TOLERANCE);
        assertEquals("surfacePoint centroid y", expected.y, actual.y, TOLERANCE);
        assertEquals("surfacePoint centroid z", expected.z, actual.z, TOLERANCE);
        assertEquals("surfacePoint centroid return", expectedReturn, actualReturn);
    }
}
