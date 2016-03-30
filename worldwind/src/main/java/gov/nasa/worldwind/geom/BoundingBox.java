/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

/**
 * Represents a bounding box in Cartesian coordinates. Typically used as a bounding volume.
 */
public class BoundingBox {

    /**
     * The box's center point.
     */
    Vec3 center = new Vec3(0, 0, 0);

    /**
     * The center point of the box's bottom. (The origin of the R axis.)
     */
    Vec3 bottomCenter = new Vec3(-0.5, 0, 0);

    /**
     * The center point of the box's top. (The end of the R axis.)
     */
    Vec3 topCenter = new Vec3(0.5, 0, 0);

    /**
     * The box's R axis, its longest axis.
     */
    Vec3 r = new Vec3(1, 0, 0);

    /**
     * The box's S axis, its mid-length axis.
     */
    Vec3 s = new Vec3(0, 1, 0);

    /**
     * The box's T axis, its shortest axis.
     */
    Vec3 t = new Vec3(0, 0, 1);

    /**
     * The box's radius. (The half-length of its diagonal.)
     */
    double radius = Math.sqrt(3);

    public BoundingBox() {
    }

    @Override
    public String toString() {
        return "center=[" + center +
            "], bottomCenter=[" + bottomCenter +
            "], topCenter=[" + topCenter +
            "], r=[" + r +
            "], s=[" + s +
            "], t=[" + t +
            "], radius=" + radius;
    }

    /**
     * Sets this bounding box such that it contains a specified sector on a specified globe with min and max elevation.
     * <p/>
     * To create a bounding box that contains the sector at mean sea level, specify zero for the minimum and maximum
     * elevations. To create a bounding box that contains the terrain surface in this sector, specify the actual minimum
     * and maximum elevation values associated with the sector, multiplied by the model's vertical exaggeration.
     * <p/>
     *
     * @param sector       The sector for which to create the bounding box.
     * @param globe        The globe associated with the sector.
     * @param minElevation The minimum elevation within the sector.
     * @param maxElevation The maximum elevation within the sector.
     *
     * @return This bounding box set to contain the specified sector.
     *
     * @throws IllegalArgumentException If either the specified sector or globe is null or undefined.
     */
    public BoundingBox setToSector(Sector sector, Globe globe, double minElevation, double maxElevation) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingBox", "setToSector", "missingSector"));
        }

        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingBox", "setToSector", "missingGlobe"));
        }

        // Compute the cartesian points for a 3x3 geographic grid. This grid captures enough detail to bound the
        // sector. Use minimum elevation at the corners and max elevation everywhere else.
        int numLat = 3;
        int numLon = 3;
        int count = numLat * numLon;
        int stride = 3;

        double[] elevations = new double[count];
        Arrays.fill(elevations, maxElevation);
        elevations[0] = elevations[2] = elevations[6] = elevations[8] = minElevation;

        FloatBuffer points = ByteBuffer.allocateDirect(count * stride * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        globe.geographicToCartesianGrid(sector, numLat, numLon, elevations, null, points, stride);
        points.rewind();

        // Compute the local coordinate axes. Since we know this box is bounding a geographic sector, we use the
        // local coordinate axes at its centroid as the box axes. Using these axes results in a box that has +-10%
        // the volume of a box with axes derived from a principal component analysis, but is faster to compute.
        double centroidLat = sector.centroidLatitude();
        double centroidLon = sector.centroidLongitude();
        Matrix4 matrix = globe.geographicToCartesianTransform(centroidLat, centroidLon, 0, new Matrix4());
        double m[] = matrix.m;

        this.r.set(m[0], m[4], m[8]);
        this.s.set(m[1], m[5], m[9]);
        this.t.set(m[2], m[6], m[10]);

        // Find the extremes along each axis.
        double rExtremes[] = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double sExtremes[] = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double tExtremes[] = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        Vec3 u = new Vec3();
        float[] coords = new float[stride];
        for (int i = 0; i < count; i++) {
            points.get(coords, 0, stride);
            u.set(coords[0], coords[1], coords[2]);
            adjustExtremes(this.r, rExtremes, this.s, sExtremes, this.t, tExtremes, u);
        }

        // Sort the axes from most prominent to least prominent. The frustum intersection methods in WWBoundingBox assume
        // that the axes are defined in this way.
        if (rExtremes[1] - rExtremes[0] < sExtremes[1] - sExtremes[0]) {
            swapAxes(this.r, rExtremes, this.s, sExtremes);
        }
        if (sExtremes[1] - sExtremes[0] < tExtremes[1] - tExtremes[0]) {
            swapAxes(this.s, sExtremes, this.t, tExtremes);
        }
        if (rExtremes[1] - rExtremes[0] < sExtremes[1] - sExtremes[0]) {
            swapAxes(this.r, rExtremes, this.s, sExtremes);
        }

        // Compute the box properties from its unit axes and the extremes along each axis.
        double rLen = rExtremes[1] - rExtremes[0];
        double sLen = sExtremes[1] - sExtremes[0];
        double tLen = tExtremes[1] - tExtremes[0];
        double rSum = rExtremes[1] + rExtremes[0];
        double sSum = sExtremes[1] + sExtremes[0];
        double tSum = tExtremes[1] + tExtremes[0];

        double cx = 0.5 * (this.r.x * rSum + this.s.x * sSum + this.t.x * tSum);
        double cy = 0.5 * (this.r.y * rSum + this.s.y * sSum + this.t.y * tSum);
        double cz = 0.5 * (this.r.z * rSum + this.s.z * sSum + this.t.z * tSum);
        double rx_2 = 0.5 * this.r.x * rLen;
        double ry_2 = 0.5 * this.r.y * rLen;
        double rz_2 = 0.5 * this.r.z * rLen;

        this.center.set(cx, cy, cz);
        this.topCenter.set(cx + rx_2, cy + ry_2, cz + rz_2);
        this.bottomCenter.set(cx - rx_2, cy - ry_2, cz - rz_2);

        this.r.multiply(rLen);
        this.s.multiply(sLen);
        this.t.multiply(tLen);

        this.radius = 0.5 * Math.sqrt(rLen * rLen + sLen * sLen + tLen * tLen);

        return this;
    }

    public double distanceTo(Vec3 point) {
        return this.center.distanceTo(point); // TODO shortest distance to center and corner points
    }

    /**
     * Indicates whether this bounding box intersects a specified frustum.
     *
     * @param frustum The frustum of interest.
     *
     * @throws IllegalArgumentException If the specified frustum is null or undefined.
     * @returns true if the specified frustum intersects this bounding box, otherwise false.
     */
    public boolean intersectsFrustum(final Frustum frustum) {
        if (frustum == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BoundingBox", "intersectsFrustum", "missingFrustum"));
        }

        Vec3 tmp1 = new Vec3(this.bottomCenter);
        Vec3 tmp2 = new Vec3(this.topCenter);

        if (this.intersectionPoint(frustum.near, tmp1, tmp2) < 0) {
            return false;
        }
        if (this.intersectionPoint(frustum.far, tmp1, tmp2) < 0) {
            return false;
        }
        if (this.intersectionPoint(frustum.left, tmp1, tmp2) < 0) {
            return false;
        }
        if (this.intersectionPoint(frustum.right, tmp1, tmp2) < 0) {
            return false;
        }
        if (this.intersectionPoint(frustum.top, tmp1, tmp2) < 0) {
            return false;
        }
        if (this.intersectionPoint(frustum.bottom, tmp1, tmp2) < 0) {
            return false;
        }

        return true;
    }

    // Internal. Intentionally not documented.
    private double intersectionPoint(final Plane plane, Vec3 endPoint1, Vec3 endPoint2) {
        Vec3 n = plane.normal;
        double effectiveRadius = 0.5 * (Math.abs(this.s.dot(n)) + Math.abs(this.t.dot(n)));

        return intersectsAt(plane, effectiveRadius, endPoint1, endPoint2);
    }

    // Internal. Intentionally not documented.
    private static double intersectsAt(final Plane plane, final double effRadius, Vec3 endPoint1, Vec3 endPoint2) {
        // Test the distance from the first end-point.
        double dq1 = plane.dot(endPoint1);
        boolean bq1 = dq1 <= -effRadius;

        // Test the distance from the second end-point.
        double dq2 = plane.dot(endPoint2);
        boolean bq2 = dq2 <= -effRadius;

        if (bq1 && bq2) { // endpoints more distant from plane than effective radius; box is on neg. side of plane
            return -1;
        }

        if (bq1 == bq2) { // endpoints less distant from plane than effective radius; can't draw any conclusions
            return 0;
        }

        // Compute and return the endpoints of the box on the positive side of the plane
        Vec3 tmpPoint = new Vec3(endPoint1);
        tmpPoint.subtract(endPoint2);
        double t = (effRadius + dq1) / plane.normal.dot(tmpPoint);

        tmpPoint.set(endPoint2);
        tmpPoint.subtract(endPoint1);
        tmpPoint.multiply(t);
        tmpPoint.add(endPoint1);

        // Truncate the line to only that in the positive halfspace, e.g., inside the frustum.
        if (bq1) {
            endPoint1.set(tmpPoint);
        } else {
            endPoint2.set(tmpPoint);
        }

        return t;
    }

    // Internal. Intentionally not documented.
    private static void adjustExtremes(Vec3 r, double[] rExtremes, Vec3 s, double[] sExtremes, Vec3 t, double[] tExtremes, Vec3 p) {
        double pdr = p.dot(r);
        if (rExtremes[0] > pdr) {
            rExtremes[0] = pdr;
        }
        if (rExtremes[1] < pdr) {
            rExtremes[1] = pdr;
        }

        double pds = p.dot(s);
        if (sExtremes[0] > pds) {
            sExtremes[0] = pds;
        }
        if (sExtremes[1] < pds) {
            sExtremes[1] = pds;
        }

        double pdt = p.dot(t);
        if (tExtremes[0] > pdt) {
            tExtremes[0] = pdt;
        }
        if (tExtremes[1] < pdt) {
            tExtremes[1] = pdt;
        }
    }

    // Internal. Intentionally not documented.
    private static void swapAxes(Vec3 a, double[] aExtremes, Vec3 b, double[] bExtremes) {
        a.swap(b);

        double tmp = aExtremes[0];
        aExtremes[0] = bExtremes[0];
        bExtremes[0] = tmp;

        tmp = aExtremes[1];
        aExtremes[1] = bExtremes[1];
        bExtremes[1] = tmp;
    }
}
