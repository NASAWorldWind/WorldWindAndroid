/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

/**
 * Globe implementing the WGS 84 reference system (aka WGS 1984, EPSG:4326).
 */
public class GlobeWgs84 extends BasicGlobe {

    /**
     * WGS 84 reference value for the Earth ellipsoid's semi-major axis: 6378137.0.
     */
    public static final double WGS84_SEMI_MAJOR_AXIS = 6378137.0;

    /**
     * WGS 84 reference value for the Earth ellipsoid's inverse flattening (1/f): 298.257223563.
     */
    public static final double WGS84_INVERSE_FLATTENING = 298.257223563;

    /**
     * Constructs a globe with WGS 84 reference values and a WGS 84 geographic projection.
     */
    public GlobeWgs84() {
        super(WGS84_SEMI_MAJOR_AXIS, WGS84_INVERSE_FLATTENING, new ProjectionWgs84());
    }
}
