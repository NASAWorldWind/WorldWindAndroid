/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WorldWind {

    /**
     * Altitude mode constant indicating an altitude relative to the globe's ellipsoid. Ignores the elevation of the
     * terrain directly beneath the position's latitude and longitude.
     */
    public static final int ABSOLUTE = 0;

    /**
     * Altitude mode constant indicating an altitude on the terrain. Ignores a position's specified altitude, and always
     * places the position on the terrain.
     */
    public static final int CLAMP_TO_GROUND = 1;

    /**
     * Altitude mode constant indicating an altitude relative to the terrain. The altitude indicates height above the
     * terrain directly beneath the position's latitude and longitude.
     */
    public static final int RELATIVE_TO_GROUND = 2;

    /**
     * Path type constant indicating a great circle arc between two locations.
     */
    public static final int GREAT_CIRCLE = 0;

    /**
     * Path type constant indicating simple linear interpolation between two locations.
     */
    public static final int LINEAR = 1;

    /**
     * Path type constant indicating a line of constant bearing between two locations.
     */
    public static final int RHUMB_LINE = 2;

    /**
     * Altitude mode indicates how World Wind interprets a position's altitude component. Accepted values are {@link
     * #ABSOLUTE}, {@link #CLAMP_TO_GROUND} and {@link #RELATIVE_TO_GROUND}.
     */
    @IntDef({ABSOLUTE, CLAMP_TO_GROUND, RELATIVE_TO_GROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AltitudeMode {

    }

    /**
     * Path type indicates how World Wind create a geographic path between two locations. Accepted values are {@link
     * #GREAT_CIRCLE}, {@link #LINEAR} and {@link #RHUMB_LINE}.
     */
    @IntDef({GREAT_CIRCLE, LINEAR, RHUMB_LINE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PathType {

    }
}
