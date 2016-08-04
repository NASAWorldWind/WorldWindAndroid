/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

/**
 * Surface of a planet or celestial object. Models the geometric surface defined by an ellipsoidal globe and its
 * associated elevations. A terrain uses the Cartesian coordinate system specified by the globe's {@link
 * GeographicProjection}, and therefore may model either an ellipsoid or an arbitrary 2D projection.
 * <p/>
 * Implementations of this interface may model a subset of the globe's surface, and at an arbitrary (or varying)
 * resolution. Unless the terrain implementation is known to represent a pre-determined region of the Globe's surface,
 * method such as intersect and surfacePoint are not guaranteed to compute the desired Cartesian coordinate.
 * Additionally, the results of these methods must be interpreted as representing an arbitrary resolution, unless the
 * terrain implementation is known to represent a pre-determined resolution.
 */
public interface Terrain {

    /**
     * Indicates the globe modeled by this terrain.
     *
     * @return the terrain's globe
     */
    Globe getGlobe();

    /**
     * Indicates the geometric surface's vertical exaggeration.
     *
     * @return the terrain's vertical exaggeration
     */
    double getVerticalExaggeration();

    /**
     * Indicates the geographic rectangular region that contains this terrain. The returned sector may contain
     * geographic areas where the terrain is nonexistent.
     *
     * @return the terrain's bounding sector
     */
    Sector getSector();

    /**
     * Computes the first intersection of this terrain with a specified line in Cartesian coordinates. The line is
     * interpreted as a ray; intersection points behind the line's origin are ignored. If the line does not intersect
     * the geometric surface modeled by this terrain, this returns false and does not modify the result argument.
     *
     * @param line   the line to intersect with this terrain
     * @param result a pre-allocated {@link Vec3} in which to return the intersection point
     *
     * @return true if the ray intersects this terrain, otherwise false
     *
     * @throws IllegalArgumentException if either argument is null
     */
    boolean intersect(Line line, Vec3 result);

    /**
     * Computes the Cartesian coordinates of a geographic location on the terrain surface. If the latitude and longitude
     * are outside the geometric surface modeled by this terrain, this returns false and does not modify the result
     * argument.
     *
     * @param latitude  the location's latitude in degrees
     * @param longitude the location's longitude in degrees
     * @param offset    a vertical offset in meters applied to the terrain height
     * @param result    a pre-allocated {@link Vec3} in which to store the computed X, Y and Z Cartesian coordinates
     *
     * @return true if the geographic location is on the terrain surface, otherwise false
     *
     * @throws IllegalArgumentException if the result is null
     */
    boolean surfacePoint(double latitude, double longitude, double offset, Vec3 result);
}
