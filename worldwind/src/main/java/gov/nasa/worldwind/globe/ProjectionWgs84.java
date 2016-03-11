/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.nio.FloatBuffer;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

public class ProjectionWgs84 implements GeographicProjection {

    public ProjectionWgs84() {
    }

    @Override
    public String getDisplayName() {
        return "WGS84";
    }

    @Override
    public Vec3 geographicToCartesian(Globe globe, double latitude, double longitude, double altitude, Vec3 offset, Vec3 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesian", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesian", "missingResult"));
        }

        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);
        double cosLat = Math.cos(radLat);
        double sinLat = Math.sin(radLat);
        double cosLon = Math.cos(radLon);
        double sinLon = Math.sin(radLon);

        double ec2 = globe.getEccentricitySquared();
        double rpm = globe.getEquatorialRadius() / Math.sqrt(1.0 - ec2 * sinLat * sinLat);

        result.x = (altitude + rpm) * cosLat * sinLon;
        result.y = (altitude + rpm * (1.0 - ec2)) * sinLat;
        result.z = (altitude + rpm) * cosLat * cosLon;

        return result;
    }

    @Override
    public Vec3 geographicToCartesianNormal(Globe globe, double latitude, double longitude, Vec3 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingResult"));
        }

        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);
        double cosLat = Math.cos(radLat);
        double sinLat = Math.sin(radLat);
        double cosLon = Math.cos(radLon);
        double sinLon = Math.sin(radLon);

        double eqr2 = globe.getEquatorialRadius() * globe.getEquatorialRadius();
        double pol2 = globe.getPolarRadius() * globe.getPolarRadius();

        result.x = cosLat * sinLon / eqr2;
        result.y = (1 - globe.getEccentricitySquared()) * sinLat / pol2;
        result.z = cosLat * cosLon / eqr2;

        return result.normalize();
    }

    protected Vec3 geographicToCartesianNorth(Globe globe, double latitude, double longitude, Vec3 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianNormal", "missingResult"));
        }

        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);
        double cosLat = Math.cos(radLat);
        double sinLat = Math.sin(radLat);
        double cosLon = Math.cos(radLon);
        double sinLon = Math.sin(radLon);

        // The north-pointing tangent is derived by rotating the vector (0, 1, 0) about the Y-axis by longitude degrees,
        // then rotating it about the X-axis by -latitude degrees. The latitude angle must be inverted because latitude
        // is a clockwise rotation about the X-axis, and standard rotation matrices assume counter-clockwise rotation.
        // The combined rotation can be represented by a combining two rotation matrices Rlat, and Rlon, then
        // transforming the vector (0, 1, 0) by the combined transform:
        //
        // NorthTangent = (Rlon * Rlat) * (0, 1, 0)
        //
        // This computation can be simplified and encoded inline by making two observations:
        // - The vector's X and Z coordinates are always 0, and its Y coordinate is always 1.
        // - Inverting the latitude rotation angle is equivalent to inverting sinLat. We know this by the
        //  trigonometric identities cos(-x) = cos(x), and sin(-x) = -sin(x).
        result.x = -sinLat * sinLon;
        result.y = cosLat;
        result.z = -sinLat * cosLon;

        return result.normalize();
    }

    @Override
    public Matrix4 geographicToCartesianTransform(Globe globe, double latitude, double longitude, double altitude, Vec3 offset, Matrix4 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianTransform", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianTransform", "missingResult"));
        }

//        Vec3 e = new Vec3();
//        Vec3 n = new Vec3();
//        Vec3 u = new Vec3();
//        Vec3 p = new Vec3();
//
//        this.geographicToCartesian(globe, latitude, longitude, altitude, offset, p);
//        this.geographicToCartesianNormal(globe, latitude, longitude, u);
//        this.geographicToCartesianNorth(globe, latitude, longitude, n);
//
//        e.cross(n, u);
//        n.cross(u, e);
//
//        result.set(
//            e.x, n.x, u.x, p.x,
//            e.y, n.y, u.y, p.y,
//            e.z, n.z, u.z, p.z,
//            0, 0, 0, 1);
//
//        return result;

        double radLat = Math.toRadians(latitude);
        double radLon = Math.toRadians(longitude);
        double cosLat = Math.cos(radLat);
        double sinLat = Math.sin(radLat);
        double cosLon = Math.cos(radLon);
        double sinLon = Math.sin(radLon);

        double ec2 = globe.getEccentricitySquared();
        double rpm = globe.getEquatorialRadius() / Math.sqrt(1.0 - ec2 * sinLat * sinLat);
        double eqr2 = globe.getEquatorialRadius() * globe.getEquatorialRadius();
        double pol2 = globe.getPolarRadius() * globe.getPolarRadius();

        double px = (rpm + altitude) * cosLat * sinLon;
        double py = (rpm * (1.0 - ec2) + altitude) * sinLat;
        double pz = (rpm + altitude) * cosLat * cosLon;

        double ux = cosLat * sinLon / eqr2;
        double uy = (1 - globe.getEccentricitySquared()) * sinLat / pol2;
        double uz = cosLat * cosLon / eqr2;

        double len = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= len;
        uy /= len;
        uz /= len;

        // The north-pointing tangent is derived by rotating the vector (0, 1, 0) about the Y-axis by longitude degrees,
        // then rotating it about the X-axis by -latitude degrees. The latitude angle must be inverted because latitude
        // is a clockwise rotation about the X-axis, and standard rotation matrices assume counter-clockwise rotation.
        // The combined rotation can be represented by a combining two rotation matrices Rlat, and Rlon, then
        // transforming the vector (0, 1, 0) by the combined transform:
        //
        // NorthTangent = (Rlon * Rlat) * (0, 1, 0)
        //
        // This computation can be simplified and encoded inline by making two observations:
        // - The vector's X and Z coordinates are always 0, and its Y coordinate is always 1.
        // - Inverting the latitude rotation angle is equivalent to inverting sinLat. We know this by the
        //  trigonometric identities cos(-x) = cos(x), and sin(-x) = -sin(x).
        double nx = -sinLat * sinLon;
        double ny = cosLat;
        double nz = -sinLat * cosLon;

        len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len;
        ny /= len;
        nz /= len;

        // East axis is the cross product of the north and up axes.
        double ex = ny * uz - nz * uy;
        double ey = nz * ux - nx * uz;
        double ez = nx * uy - ny * ux;

        // Re-compute the north axis as the cross product of the up and east axes. This ensures that all three axes are orthogonal.
        // Though the initial y axis computed above is likely to be very nearly orthogonal, we re-compute it using cross
        // products to reduce the effect of floating point rounding errors caused by working with Earth sized coordinates.
        nx = uy * ez - uz * ey;
        ny = uz * ex - ux * ez;
        nz = ux * ey - uy * ex;

        result.set(
            ex, nx, ux, px,
            ey, ny, uy, py,
            ez, nz, uz, pz,
            0, 0, 0, 1);

        return result;
    }

    @Override
    public FloatBuffer geographicToCartesianGrid(Globe globe, Sector sector, int numLat, int numLon, double[] elevations, Vec3 referencePoint, Vec3 offset, FloatBuffer result, int stride) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingGlobe"));
        }

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "ProjectionWgs84",
                "geographicToCartesianGrid", "Number of latitude or longitude locations is less than one"));
        }

        int numPoints = numLat * numLon;
        if (elevations != null && elevations.length < numPoints) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "ProjectionWgs84",
                "geographicToCartesianGrid", "missingArray"));
        }

        if (result == null || result.remaining() < numPoints * stride) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicGlobe", "geographicToCartesianGrid", "missingResult"));
        }

        double minLat = Math.toRadians(sector.minLatitude);
        double maxLat = Math.toRadians(sector.maxLatitude);
        double minLon = Math.toRadians(sector.minLongitude);
        double maxLon = Math.toRadians(sector.maxLongitude);
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);

        double eqr = globe.getEquatorialRadius();
        double ec2 = globe.getEccentricitySquared();

        int latIndex, lonIndex, elevIndex = 0;
        double lat, lon;
        double xOffset = (referencePoint != null) ? -referencePoint.x : 0;
        double yOffset = (referencePoint != null) ? -referencePoint.y : 0;
        double zOffset = (referencePoint != null) ? -referencePoint.z : 0;

        double[] cosLon = new double[numLon];
        double[] sinLon = new double[numLon];
        float[] coords = new float[stride];

        // Compute and save values that are a function of each unique longitude value in the specified sector. This
        // eliminates the need to re-compute these values for each column of constant longitude.
        for (lonIndex = 0, lon = minLon; lonIndex < numLon; lonIndex++, lon += deltaLon) {
            if (lonIndex == numLon - 1) {
                lon = maxLon; // explicitly set the last lon to the max longitude to ensure alignment
            }

            cosLon[lonIndex] = Math.cos(lon);
            sinLon[lonIndex] = Math.sin(lon);
        }

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian
        // point corresponding to each latitude and longitude.
        for (latIndex = 0, lat = minLat; latIndex < numLat; latIndex++, lat += deltaLat) {
            if (latIndex == numLat - 1) {
                lat = maxLat; // explicitly set the last lat to the max latitude to ensure alignment
            }

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double cosLat = Math.cos(lat);
            double sinLat = Math.sin(lat);
            double rpm = eqr / Math.sqrt(1.0 - ec2 * sinLat * sinLat);

            for (lonIndex = 0; lonIndex < numLon; lonIndex++) {
                double elev = (elevations != null) ? elevations[elevIndex++] : 0;
                coords[0] = (float) ((elev + rpm) * cosLat * sinLon[lonIndex] + xOffset);
                coords[1] = (float) ((elev + rpm * (1.0 - ec2)) * sinLat + yOffset);
                coords[2] = (float) ((elev + rpm) * cosLat * cosLon[lonIndex] + zOffset);
                result.put(coords, 0, stride);
            }
        }

        return result;
    }

    @Override
    public Position cartesianToGeographic(Globe globe, double x, double y, double z, Vec3 offset, Position result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToGeographic", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToGeographic", "missingResult"));
        }

        // According to
        // H. Vermeille,
        // "An analytical method to transform geocentric into geodetic coordinates"
        // http://www.springerlink.com/content/3t6837t27t351227/fulltext.pdf
        // Journal of Geodesy, accepted 10/2010, not yet published
        double X = z;
        double Y = x;
        double Z = y;
        double XXpYY = X * X + Y * Y;
        double sqrtXXpYY = Math.sqrt(XXpYY);

        double a = globe.getEquatorialRadius();
        double ra2 = 1 / (a * a);
        double e2 = globe.getEccentricitySquared();
        double e4 = e2 * e2;

        // Step 1
        double p = XXpYY * ra2;
        double q = Z * Z * (1 - e2) * ra2;
        double r = (p + q - e4) / 6;

        double h;
        double phi;

        double evoluteBorderTest = 8 * r * r * r + e4 * p * q;
        if (evoluteBorderTest > 0 || q != 0) {
            double u;

            if (evoluteBorderTest > 0) {
                // Step 2: general case
                double rad1 = Math.sqrt(evoluteBorderTest);
                double rad2 = Math.sqrt(e4 * p * q);

                // 10*e2 is my arbitrary decision of what Vermeille means by "near... the cusps of the evolute".
                if (evoluteBorderTest > 10 * e2) {
                    double rad3 = Math.cbrt((rad1 + rad2) * (rad1 + rad2));
                    u = r + 0.5 * rad3 + 2 * r * r / rad3;
                } else {
                    u = r + 0.5 * Math.cbrt((rad1 + rad2) * (rad1 + rad2)) + 0.5 * Math.cbrt(
                        (rad1 - rad2) * (rad1 - rad2));
                }
            } else {
                // Step 3: near evolute
                double rad1 = Math.sqrt(-evoluteBorderTest);
                double rad2 = Math.sqrt(-8 * r * r * r);
                double rad3 = Math.sqrt(e4 * p * q);
                double atan = 2 * Math.atan2(rad3, rad1 + rad2) / 3;

                u = -4 * r * Math.sin(atan) * Math.cos(Math.PI / 6 + atan);
            }

            double v = Math.sqrt(u * u + e4 * q);
            double w = e2 * (u + v - q) / (2 * v);
            double k = (u + v) / (Math.sqrt(w * w + u + v) + w);
            double D = k * sqrtXXpYY / (k + e2);
            double sqrtDDpZZ = Math.sqrt(D * D + Z * Z);

            h = (k + e2 - 1) * sqrtDDpZZ / k;
            phi = 2 * Math.atan2(Z, sqrtDDpZZ + D);
        } else {
            // Step 4: singular disk
            double rad1 = Math.sqrt(1 - e2);
            double rad2 = Math.sqrt(e2 - p);
            double e = Math.sqrt(e2);

            h = -a * rad1 * rad2 / e;
            phi = rad2 / (e * rad2 + rad1 * Math.sqrt(p));
        }

        // Compute lambda
        double lambda;
        double s2 = Math.sqrt(2);
        if ((s2 - 1) * Y < sqrtXXpYY + X) {
            // case 1 - -135deg < lambda < 135deg
            lambda = 2 * Math.atan2(Y, sqrtXXpYY + X);
        } else if (sqrtXXpYY + Y < (s2 + 1) * X) {
            // case 2 - -225deg < lambda < 45deg
            lambda = -Math.PI * 0.5 + 2 * Math.atan2(X, sqrtXXpYY - Y);
        } else {
            // if (sqrtXXpYY-Y<(s2=1)*X) {  // is the test, if needed, but it's not
            // case 3: - -45deg < lambda < 225deg
            lambda = Math.PI * 0.5 - 2 * Math.atan2(X, sqrtXXpYY + Y);
        }

        result.latitude = Math.toDegrees(phi);
        result.longitude = Math.toDegrees(lambda);
        result.altitude = h;

        return result;
    }
}
