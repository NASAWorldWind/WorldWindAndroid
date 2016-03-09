/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

public class GlobeWgs84 extends BasicGlobe {

    public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0;
    public static final double WGS84_POLAR_RADIUS = 6356752.3;
    public static final double WGS84_EC2 = 0.00669437999013;

    public GlobeWgs84() {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_EC2, new ProjectionWgs84());
    }
}
