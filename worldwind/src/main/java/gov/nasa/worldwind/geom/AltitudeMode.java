/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

/**
 * Altitude mode indicates how World Wind interprets a position's altitude component.
 */
public enum AltitudeMode {
    /**
     * Indicates an altitude relative to the globe's ellipsoid, regardless of the elevation of the
     * terrain.
     */
    ABSOLUTE,
    /**
     * Ignores a position's specified altitude, and always places the position on the terrain.
     */
    CLAMP_TO_GROUND,
    /**
     * Indicates an altitude relative to the terrain. The altitude indicates height above the
     * terrain directly beneath the position's latitude and longitude.
     */
    RELATIVE_TO_GROUND
}
