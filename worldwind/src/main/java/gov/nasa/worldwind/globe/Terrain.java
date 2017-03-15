/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

/**
 * Surface of a planet or celestial object.
 * <p>
 * Models the geometric surface defined by an ellipsoidal globe and its
 * associated elevations. Terrain uses the Cartesian coordinate system specified by a {@code GeographicProjection} and
 * is capable of representing both a 3D ellipsoid and a 2D map projection, though not simultaneously.
 * <p/>
 * <h3>Caching Terrain Queries</h3>
 * <p>
 * Terrain implementations typically model a subset of the globe's surface at varying
 * resolution. In this case results from the methods {@code intersect} and {@code surfacePoint} cannot be cached. Either
 * method may fail to compute a result when the terrain surface has no geometry in the region queried, and even if
 * computation is successful the result is based on an unknown resolution. However, if the terrain implementation is
 * known to model a pre-determined resolution and region of interest results from the methods {@code intersect} and
 * {@code surfacePoint} may be cached.
 * </p>
 *
 * @see GeographicProjection
 */
public interface Terrain {

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
     * @param result    a pre-allocated {@link Vec3} in which to store the computed X, Y and Z Cartesian coordinates
     *
     * @return true if the geographic location is on the terrain surface, otherwise false
     *
     * @throws IllegalArgumentException if the result is null
     */
    boolean surfacePoint(double latitude, double longitude, Vec3 result);
}
