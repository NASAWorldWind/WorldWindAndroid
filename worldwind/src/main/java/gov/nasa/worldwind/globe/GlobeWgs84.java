/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.WorldWind;

/**
 * Globe implementing the WGS 84 reference system (aka WGS 1984, EPSG:4326).
 */
public class GlobeWgs84 extends BasicGlobe {

    /**
     * Constructs a globe with WGS 84 reference values and a WGS 84 geographic projection.
     */
    public GlobeWgs84() {
        super(WorldWind.WGS84_SEMI_MAJOR_AXIS, WorldWind.WGS84_INVERSE_FLATTENING, new ProjectionWgs84());
    }
}
