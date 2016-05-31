/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

public interface Terrain {

    Sector getSector();

    Vec3 geographicToCartesian(double latitude, double longitude, double altitude,
                               @WorldWind.AltitudeMode int altitudeMode, Vec3 result);

    /**
     * Computes the first intersection of this terrain with a specified line. The line is interpreted as a ray;
     * intersection points behind the line's origin are ignored.
     *
     * @param line   the line to intersect with this terrain
     * @param result a pre-allocated {@link Vec3} in which to return the intersection point
     *
     * @return true if the ray intersects this terrain, otherwise false
     *
     * @throws IllegalArgumentException If either argument is null
     */
    boolean intersect(Line line, Vec3 result);
}
