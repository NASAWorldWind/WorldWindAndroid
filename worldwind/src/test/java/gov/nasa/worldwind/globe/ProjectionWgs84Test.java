/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class) // Support for mocking static methods
@PrepareForTest(Logger.class)   // We mock the Logger class to avoid its calls to android.util.log
public class ProjectionWgs84Test {

    private Globe globe;

    @Before
    public void setUp() throws Exception {
        // To accommodate WorldWind exception handling, we must mock all
        // the static methods in Logger to avoid calls to android.util.log
        PowerMockito.mockStatic(Logger.class);

        // Create a globe with a WGS84 definition.
        this.globe = new Globe(WorldWind.WGS84_ELLIPSOID, new ProjectionWgs84());
    }

    @After
    public void tearDown() throws Exception {
        this.globe = null;
    }

    @Test
    public void testConstructor() {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();

        assertNotNull(wgs84);
    }

    @Test
    public void testGetDisplayName() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();

        String string = wgs84.getDisplayName();

        assertEquals("WGS84 name string", "WGS84", string);
    }

    /**
     * Tests the cartesian coordinates against values defined in the NIMA WGS specifications:
     * http://earth-info.nga.mil/GandG/publications/tr8350.2/Addendum%20NIMA%20TR8350.2.pdf
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesian() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Map<String, Object[]> stations = getStations();
        for (Map.Entry<String, Object[]> station : stations.entrySet()) {
            Position p = (Position) station.getValue()[0];
            Vec3 v = (Vec3) station.getValue()[1];
            Vec3 result = new Vec3();

            wgs84.geographicToCartesian(globe, p.latitude, p.longitude, p.altitude, result);

            // Note: we must rotate the axis to match the WW coord system to the WGS coord system
            // WW: Y is polar axis, X and Z line on the equatorial plane with X +/-90 and Z +/-180
            // WGS: Z is polar axis
            assertEquals(station.getKey(), v.x, result.x, 1e-3);
            assertEquals(station.getKey(), v.y, result.y, 1e-3);
            assertEquals(station.getKey(), v.z, result.z, 1e-3);
        }
    }

    /**
     * Simply tests that the reciprocal method will regenerate the original value.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesian_Reciprocal() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        double lat = 34.2;      // KOXR airport
        double lon = -119.2;
        double alt = 1000;
        Vec3 vec = new Vec3();
        Position pos = new Position();

        wgs84.geographicToCartesian(globe, lat, lon, alt, vec);
        wgs84.cartesianToGeographic(globe, vec.x, vec.y, vec.z, pos);

        assertEquals("lat", lat, pos.latitude, 1e-6);
        assertEquals("lon", lon, pos.longitude, 1e-6);
        assertEquals("alt", alt, pos.altitude, 1e-6);
    }

    /**
     * Simple test ensures the computed normal aligns to an expected result.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesianNormal() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        double lat = 34.2;  // KOXR airport
        double lon = -119.2;
        double latRad = Math.toRadians(lat);

        Vec3 result = wgs84.geographicToCartesianNormal(globe, lat, lon, new Vec3());

        double theta = Math.toDegrees(Math.asin(result.y));
        double lambda = Math.toDegrees(Math.atan2(result.x, result.z));
        assertEquals("latitude: ", theta, lat, 1e-6);
        assertEquals("longitude: ", lambda, lon, 1e-6);
    }

    /**
     * Ensures that transform matrix agrees with manual cartesian transform of a position.
     *
     * @throws Exception
     */
    @Test
    public void testGeographicToCartesianTransform() throws Exception {
        // The expectation of geographicToCartesianTransform is that the
        // coordinate system is perpendicular to the geodetic tangent plane
        // at the position. So the Z axes points along the normal to the
        // ellipsoid -- the vector Rn in diagrams.
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        double lat = 34.2;
        double lon = -119.2;
        double alt = 0;
        // Compute the radius of the prime vertical, Rn, and the vertical
        // delta between the equator and the intersection of Rn and Y axis.
        double a = globe.getEquatorialRadius();
        double e2 = globe.getEccentricitySquared();
        double sinLat = Math.sin(Math.toRadians(lat));
        double Rn = a / Math.sqrt(1 - e2 * sinLat * sinLat);
        double delta = e2 * Rn * sinLat;
        // First rotate longitude about the Y axis; second, translate the
        // origin to the intersection of Rn and the Y axis; third, rotate
        // latitude about the X axis (opposite rotation used for latitudes);
        // and finally, translate along Rn (the Z axis) to the surface of
        // the ellipsoid.
        Matrix4 expected = new Matrix4();
        expected.multiplyByRotation(0, 1, 0, lon);
        expected.multiplyByTranslation(0, -delta, 0);
        expected.multiplyByRotation(1, 0, 0, -lat);
        expected.multiplyByTranslation(0, 0, Rn);

        Matrix4 result = wgs84.geographicToCartesianTransform(globe, lat, lon, alt, new Matrix4());

        assertArrayEquals(expected.m, result.m, 1e-6);
    }

    @Ignore("not implemented yet.")
    @Test
    public void testGeographicToCartesianGrid() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();

        int stride = 5;
        int numLat = 17;
        int numLon = 33;
        int count = numLat * numLon * stride;
        float[] elevations = new float[count];
        float verticalExaggeration = 1.0f;
        Sector sector = new Sector();
        Vec3 referencePoint = new Vec3();
        Vec3 offset = new Vec3();
        float[] result = new float[count];

        wgs84.geographicToCartesianGrid(globe, sector, numLat, numLon, elevations, verticalExaggeration, referencePoint, result, stride, 0);

        fail("The test case is a stub.");
    }

    /**
     * Tests the geodetic coordinates against values defined in the NIMA WGS specifications:
     * http://earth-info.nga.mil/GandG/publications/tr8350.2/Addendum%20NIMA%20TR8350.2.pdf
     *
     * @throws Exception
     */
    @Test
    public void testCartesianToGeographic() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Map<String, Object[]> stations = getStations();
        for (Map.Entry<String, Object[]> station : stations.entrySet()) {
            Position p = (Position) station.getValue()[0];
            Vec3 v = (Vec3) station.getValue()[1];
            Position result = new Position();

            // Note: we must rotate the axis to match the WW coord system to the WGS ECEF coord system
            // WW: Y is polar axis, X and Z line on the equatorial plane with X coincident with +/-90 and Z +/-180
            wgs84.cartesianToGeographic(globe, v.x, v.y, v.z, result);

            assertEquals(station.getKey(), Location.normalizeLatitude(p.latitude), result.latitude, 1e-6);
            assertEquals(station.getKey(), Location.normalizeLongitude(p.longitude), result.longitude, 1e-6);
            assertEquals(station.getKey(), p.altitude, result.altitude, 1e-3);
        }
    }

    /**
     * Simply tests that the reciprocal method will regenerate the original value.
     *
     * @throws Exception
     */
    @Test
    public void testCartesianToGeographic_Reciprocal() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        double x = -4610466.9131683465; // KOXR airport
        double y = 3565379.0227454384;
        double z = -2576702.8642047923;
        Vec3 vec = new Vec3();
        Position pos = new Position();

        wgs84.cartesianToGeographic(globe, x, y, z, pos);
        wgs84.geographicToCartesian(globe, pos.latitude, pos.longitude, pos.altitude, vec);

        assertEquals("x", x, vec.x, 1e-6);
        assertEquals("y", y, vec.y, 1e-6);
        assertEquals("z", z, vec.z, 1e-6);
    }

    /**
     * This test case was provided by the COE EMP team. Visually, it is obvious the Line in this examples has a
     * direction and origin that will not intersect the ellipsoid.
     *
     * @throws Exception
     */
    @Test
    public void testEmpBackwardInstance() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Line ray = new Line(new Vec3(990474.8037403631, 3007310.9566306924, 5583923.602748461), new Vec3(-0.1741204769506282, 0.9711294099374702, -0.16306357245254538));

        boolean intersection = wgs84.intersect(this.globe, ray, new Vec3());

        assertFalse("EMP backward intersection", intersection);
    }

    /**
     * An instance which is easily visualized for understanding the backwards intersection instance.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleBackwardsIntersection() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Globe mockedGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(mockedGlobe.getEquatorialRadius()).thenReturn(1.0);
        PowerMockito.when(mockedGlobe.getPolarRadius()).thenReturn(1.0);
        Line ray = new Line(new Vec3(0.8, 0.8, 0.0), new Vec3(0.0, 1.0, 0.0));

        boolean intersection = wgs84.intersect(mockedGlobe, ray, new Vec3());

        assertFalse("simple backwards intersection", intersection);
    }

    /**
     * An instance which is easily visualized for understanding the forwards intersection instance.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleIntersection() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Globe mockedGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(mockedGlobe.getEquatorialRadius()).thenReturn(1.0);
        PowerMockito.when(mockedGlobe.getPolarRadius()).thenReturn(1.0);
        Line ray = new Line(new Vec3(0.8, 0.8, 0.0), new Vec3(0.0, -1.0, 0.0));

        boolean intersection = wgs84.intersect(mockedGlobe, ray, new Vec3());

        assertTrue("simple intersection", intersection);
    }

    /**
     * An instance which demonstrates two intersections, but the closest, or first surface intersection position is
     * desired.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleTwoIntersection() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Globe mockedGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(mockedGlobe.getEquatorialRadius()).thenReturn(1.0);
        PowerMockito.when(mockedGlobe.getPolarRadius()).thenReturn(1.0);
        Line ray = new Line(new Vec3(-1.0, 2.0, 0.0), new Vec3(1.0, -1.0, 0.0).normalize());
        Vec3 result = new Vec3();
        double errorThreshold = 1e-9;

        boolean intersection = wgs84.intersect(mockedGlobe, ray, result);

        assertTrue("simple intersection", intersection);
        assertEquals("nearest calculated intersection x", 0.0, result.x, errorThreshold);
        assertEquals("nearest calculated intersection y", 1.0, result.y, errorThreshold);
    }

    /**
     * An instance which demonstrates two intersections with a ray originating within the ellipsoid.
     *
     * @throws Exception
     */
    @Test
    public void testSimpleTwoIntersectionInternal() throws Exception {
        ProjectionWgs84 wgs84 = new ProjectionWgs84();
        Globe mockedGlobe = PowerMockito.mock(Globe.class);
        PowerMockito.when(mockedGlobe.getEquatorialRadius()).thenReturn(1.0);
        PowerMockito.when(mockedGlobe.getPolarRadius()).thenReturn(1.0);
        Line ray = new Line(new Vec3(-0.8, 0, 0.0), new Vec3(1.0, 0.0, 0.0).normalize());
        Vec3 result = new Vec3();
        double errorThreshold = 1e-9;

        boolean intersection = wgs84.intersect(mockedGlobe, ray, result);

        assertTrue("simple internal intersection", intersection);
        assertEquals("forward calculated intersection x", 1.0, result.x, errorThreshold);
        assertEquals("forward calculated intersection y", 0.0, result.y, errorThreshold);
    }

    //////////////////////////////////////////
    //           Helper Methods
    //////////////////////////////////////////

    public double radiusOfPrimeVeritcal(double geographicLat) {
        double a = globe.getEquatorialRadius();
        double e2 = globe.getEccentricitySquared();
        double sinSquared = Math.pow(Math.sin(Math.toRadians(geographicLat)), 2);

        return a / Math.sqrt(1 - e2 * sinSquared);
    }

    /**
     * Creates a Vec3 in the WorldWind coordinate system from WGS84 ECEF coordinates.
     *
     * @param xEcef
     * @param yEcef
     * @param zEcef
     *
     * @return a Vec3 compatible with the WorldWind graphics coordinate system.
     */
    public static Vec3 fromEcef(double xEcef, double yEcef, double zEcef) {
        return new Vec3(yEcef, zEcef, xEcef);
    }

    /**
     * Returns a Map of station names with Position and Vec3 pairs.
     * <pre>
     * Geodetic Coordinates 2001 epoch:
     * Air Force Station    Station  Lat             Lon             Ellipsoid Height
     * -------------------------------------------------------------------------------
     * Colorado Springs      85128   38.80305456     255.47540844    1911.755
     * Ascension             85129   -7.95132970     345.58786950    106.558
     * Diego Garcia          85130   -7.26984347     72.37092177     -64.063
     * Kwajalein             85131   8.72250074      167.73052625    39.927
     * Hawaii                85132   21.56149086     201.76066922    426.077
     * Cape Canaveral        85143   28.48373800     279.42769549    -24.005
     *
     * Cartesian Coordinates 2001 epoch (ECEF coordinates positive Z points up)
     * Air Force Station    Station  X(km)           Y(km)           Z(km)
     * -------------------------------------------------------------------------------
     * Colorado Springs      85128   -1248.597295    -4819.433239    3976.500175
     * Ascension             85129   6118.524122     -1572.350853    -876.463990
     * Diego Garcia          85130   1916.197142     6029.999007     -801.737366
     * Kwajalein             85131   -6160.884370    1339.851965     960.843071
     * Hawaii                85132   -5511.980484    -2200.247093    2329.480952
     * Cape Canaveral        85143   918.988120      -5534.552966    3023.721377
     * </pre>
     * http://earth-info.nga.mil/GandG/publications/tr8350.2/Addendum%20NIMA%20TR8350.2.pdf
     *
     * @return a Map collection containing station names with reference positions and ECEF coordinates.
     */
    public static Map<String, Object[]> getStations() {
        Map<String, Object[]> stations = new HashMap<>();
        stations.put("Colorado Springs", new Object[]{
            Position.fromDegrees(38.80305456, 255.47540844, 1911.755),
            fromEcef(-1248.597295e3, -4819.433239e3, 3976.500175e3)});
        stations.put("Ascension", new Object[]{
            Position.fromDegrees(-7.95132970, 345.58786950, 106.558),
            fromEcef(6118.524122e3, -1572.350853e3, -876.463990e3)});
        stations.put("Diego Garcia", new Object[]{
            Position.fromDegrees(-7.26984347, 72.37092177, -64.063),
            fromEcef(1916.197142e3, 6029.999007e3, -801.737366e3)});
        stations.put("Kwajalein", new Object[]{
            Position.fromDegrees(8.72250074, 167.73052625, 39.927),
            fromEcef(-6160.884370e3, 1339.851965e3, 960.843071e3)});
        stations.put("Hawaii", new Object[]{
            Position.fromDegrees(21.56149086, 201.76066922, 426.077),
            fromEcef(-5511.980484e3, -2200.247093e3, 2329.480952e3)});
        stations.put("Cape Canaveral", new Object[]{
            Position.fromDegrees(28.48373800, 279.42769549, -24.005),
            fromEcef(918.988120e3, -5534.552966e3, 3023.721377e3)});

        return stations;
    }
}