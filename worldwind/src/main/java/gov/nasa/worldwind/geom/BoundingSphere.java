/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a bounding sphere in Cartesian coordinates. Typically used as a bounding volume.
 */
public class BoundingSphere {

    /**
     * The sphere's center point.
     */
    public final Vec3 center = new Vec3();

    /**
     * The sphere's radius.
     */
    public double radius = 1;

    public BoundingSphere() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        BoundingSphere that = (BoundingSphere) o;
        return this.radius == that.radius
            && this.center.equals(that.center);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = center.hashCode();
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "center=" + center + ", radius=" + radius;
    }

    /**
     * Sets this bounding sphere to the specified center point and radius.
     *
     * @param center the new center point
     * @param radius the new radius
     *
     * @return This bounding sphere with its center point and radius set to the specified values
     *
     * @throws IllegalArgumentException If the center is null, or if the radius is negative
     */
    public BoundingSphere set(Vec3 center, double radius) {
        if (center == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingSphere", "set", "missingPoint"));
        }

        if (radius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingSphere", "set", "invalidRadius"));
        }

        this.center.set(center);
        this.radius = radius;
        return this;
    }

    /**
     * Indicates whether this bounding sphere intersects a specified frustum.
     *
     * @param frustum the frustum of interest
     *
     * @return true if the specified frustum intersects this bounding sphere, otherwise false.
     *
     * @throws IllegalArgumentException If the frustum is null
     */
    public boolean intersectsFrustum(Frustum frustum) {
        if (frustum == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingSphere", "intersectsFrustum", "missingFrustum"));
        }

        // See if the extent's bounding sphere is within or intersects the frustum. The dot product of the extent's
        // center point with each plane's vector provides a distance to each plane. If this distance is less than
        // -radius, the extent is completely clipped by that plane and therefore does not intersect the space enclosed
        // by this Frustum.

        double nr = -this.radius;

        if (frustum.near.distanceToPoint(this.center) <= nr) {
            return false;
        }

        if (frustum.far.distanceToPoint(this.center) <= nr) {
            return false;
        }

        if (frustum.left.distanceToPoint(this.center) <= nr) {
            return false;
        }

        if (frustum.right.distanceToPoint(this.center) <= nr) {
            return false;
        }

        if (frustum.top.distanceToPoint(this.center) <= nr) {
            return false;
        }

        if (frustum.bottom.distanceToPoint(this.center) <= nr) {
            return false;
        }

        return true;
    }
}
