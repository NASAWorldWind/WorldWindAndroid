/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

public class GlobeWgs84 extends BasicGlobe {

    public static final double WGS84_SEMI_MAJOR_AXIS = 6378137.0;
    public static final double WGS84_INVERSE_FLATTENING = 298.257223563;

    public GlobeWgs84() {
        super(WGS84_SEMI_MAJOR_AXIS, WGS84_INVERSE_FLATTENING, new ProjectionWgs84());
    }
}
