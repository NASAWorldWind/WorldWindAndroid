/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

/*
 * GeographicProjection implementing coordinate transformations based on the WGS 84 reference system (aka WGS 1984,
 * EPSG:4326).
 *
 * The WGS 84 projection defines a Cartesian coordinate system whose origin is at the globe's center. It's Y axis points
 * to the North pole, the Z axis points to the intersection of the prime meridian and the equator, and the X axis
 * completes a right-handed coordinate system, is in the equatorial plane and 90 degrees East of the Z axis.
 */
public class ProjectionWgs84 implements GeographicProjection {

    private Position scratchPos = new Position();

    /**
     * Constructs a WGS 84 geographic projection.
     */
    public ProjectionWgs84() {
    }

    @Override
    public String getDisplayName() {
        return "WGS84";
    }

    @Override
    public Vec3 geographicToCartesian(Globe globe, double latitude, double longitude, double altitude, Vec3 result) {
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

    @Override
    public Matrix4 geographicToCartesianTransform(Globe globe, double latitude, double longitude, double altitude, Matrix4 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianTransform", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianTransform", "missingResult"));
        }

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

        // Convert the geographic position to Cartesian coordinates. This is equivalent to calling geographicToCartesian
        // but is much more efficient as an inline computation, as the results of cosLat/sinLat/etc. can be computed
        // once and reused.
        double px = (rpm + altitude) * cosLat * sinLon;
        double py = (rpm * (1.0 - ec2) + altitude) * sinLat;
        double pz = (rpm + altitude) * cosLat * cosLon;

        // Compute the surface normal at the geographic position. This is equivalent to calling
        // geographicToCartesianNormal but is much more efficient as an inline computation.
        double ux = cosLat * sinLon / eqr2;
        double uy = (1 - globe.getEccentricitySquared()) * sinLat / pol2;
        double uz = cosLat * cosLon / eqr2;
        double len = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= len;
        uy /= len;
        uz /= len;

        // Compute the north pointing tangent at the geographic position. This computation could be encoded in its own
        // method, but is much more efficient as an inline computation. The north-pointing tangent is derived by
        // rotating the vector (0, 1, 0) about the Y-axis by longitude degrees, then rotating it about the X-axis by
        // -latitude degrees. The latitude angle must be inverted because latitude is a clockwise rotation about the
        // X-axis, and standard rotation matrices assume counter-clockwise rotation. The combined rotation can be
        // represented by a combining two rotation matrices Rlat, and Rlon, then transforming the vector (0, 1, 0) by
        // the combined transform: NorthTangent = (Rlon * Rlat) * (0, 1, 0)
        //
        // Additionally, this computation can be simplified by making two observations:
        // - The vector's X and Z coordinates are always 0, and its Y coordinate is always 1.
        // - Inverting the latitude rotation angle is equivalent to inverting sinLat. We know this by the
        //   trigonometric identities cos(-x) = cos(x), and sin(-x) = -sin(x).
        double nx = -sinLat * sinLon;
        double ny = cosLat;
        double nz = -sinLat * cosLon;
        len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len;
        ny /= len;
        nz /= len;

        // Compute the east pointing tangent as the cross product of the north and up axes. This is much more efficient
        // as an inline computation.
        double ex = ny * uz - nz * uy;
        double ey = nz * ux - nx * uz;
        double ez = nx * uy - ny * ux;

        // Ensure the normal, north and east vectors represent an orthonormal basis by ensuring that the north vector is
        // perpendicular to normal and east vectors. This should already be the case, but rounding errors can be
        // introduced when working with Earth sized coordinates.
        nx = uy * ez - uz * ey;
        ny = uz * ex - ux * ez;
        nz = ux * ey - uy * ex;

        // Set the result to an orthonormal basis with the East, North, and Up vectors forming the X, Y and Z axes,
        // respectively, and the Cartesian point indicating the coordinate system's origin.
        result.set(
            ex, nx, ux, px,
            ey, ny, uy, py,
            ez, nz, uz, pz,
            0, 0, 0, 1);

        return result;
    }

    @Override
    public float[] geographicToCartesianGrid(Globe globe, Sector sector, int numLat, int numLon, float[] height, float verticalExaggeration,
                                             Vec3 origin, float[] result, int offset, int rowStride) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingGlobe"));
        }

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid",
                    "Number of latitude or longitude locations is less than one"));
        }

        int numPoints = numLat * numLon;
        if (height != null && height.length < numPoints) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingArray"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianGrid", "missingResult"));
        }

        double minLat = Math.toRadians(sector.minLatitude());
        double maxLat = Math.toRadians(sector.maxLatitude());
        double minLon = Math.toRadians(sector.minLongitude());
        double maxLon = Math.toRadians(sector.maxLongitude());
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);

        double eqr = globe.getEquatorialRadius();
        double ec2 = globe.getEccentricitySquared();
        double cosLat, sinLat, rpm;
        double[] cosLon = new double[numLon];
        double[] sinLon = new double[numLon];

        int latIndex, lonIndex, elevIndex = 0;
        double lat, lon, hgt;
        double xOffset = (origin != null) ? -origin.x : 0;
        double yOffset = (origin != null) ? -origin.y : 0;
        double zOffset = (origin != null) ? -origin.z : 0;

        // Compute and save values that are a function of each unique longitude value in the specified sector. This
        // eliminates the need to re-compute these values for each column of constant longitude.
        for (lonIndex = 0, lon = minLon; lonIndex < numLon; lonIndex++, lon += deltaLon) {
            if (lonIndex == numLon - 1) {
                lon = maxLon; // explicitly set the last lon to the max longitude to ensure alignment
            }

            cosLon[lonIndex] = Math.cos(lon);
            sinLon[lonIndex] = Math.sin(lon);
        }

        int rowIndex = offset;
        if (rowStride == 0) {
            rowStride = numLon * 3;
        }

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian
        // point corresponding to each latitude and longitude.
        for (latIndex = 0, lat = minLat; latIndex < numLat; latIndex++, lat += deltaLat) {
            if (latIndex == numLat - 1) {
                lat = maxLat; // explicitly set the last lat to the max latitude to ensure alignment
            }

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            cosLat = Math.cos(lat);
            sinLat = Math.sin(lat);
            rpm = eqr / Math.sqrt(1.0 - ec2 * sinLat * sinLat);

            int colIndex = rowIndex;
            for (lonIndex = 0; lonIndex < numLon; lonIndex++) {
                hgt = (height != null) ? height[elevIndex++] * verticalExaggeration : 0;
                result[colIndex++] = (float) ((hgt + rpm) * cosLat * sinLon[lonIndex] + xOffset);
                result[colIndex++] = (float) ((hgt + rpm * (1.0 - ec2)) * sinLat + yOffset);
                result[colIndex++] = (float) ((hgt + rpm) * cosLat * cosLon[lonIndex] + zOffset);
            }

            rowIndex += rowStride;
        }

        return result;
    }

    public float[] geographicToCartesianBorder(Globe globe, Sector sector, int numLat, int numLon, float height,
                                               Vec3 origin, float[] result) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianBorder", "missingSector"));
        }

        if (numLat < 1 || numLon < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianBorder",
                    "Number of latitude or longitude locations is less than one"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "geographicToCartesianBorder", "missingResult"));
        }

        double minLat = Math.toRadians(sector.minLatitude());
        double maxLat = Math.toRadians(sector.maxLatitude());
        double minLon = Math.toRadians(sector.minLongitude());
        double maxLon = Math.toRadians(sector.maxLongitude());
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 3 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 3 : 1);
        double lat = minLat;
        double lon = minLon;

        double eqr = globe.getEquatorialRadius();
        double ec2 = globe.getEccentricitySquared();

        double xOffset = (origin != null) ? -origin.x : 0;
        double yOffset = (origin != null) ? -origin.y : 0;
        double zOffset = (origin != null) ? -origin.z : 0;
        int resultIndex = 0;

        // Iterate over the edges of the specified sector, computing the Cartesian point at designated latitude and
        // longitude around the border.
        for (int latIndex = 0; latIndex < numLat; latIndex++) {
            if (latIndex < 2) {
                lat = minLat; // explicitly set the first lat to the min latitude to ensure alignment
            } else if (latIndex < numLat - 2) {
                lat += deltaLat;
            } else {
                lat = maxLat; // explicitly set the last lat to the max latitude to ensure alignment
            }

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double cosLat = Math.cos(lat);
            double sinLat = Math.sin(lat);
            double rpm = eqr / Math.sqrt(1.0 - ec2 * sinLat * sinLat);

            for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
                if (lonIndex < 2) {
                    lon = minLon; // explicitly set the first lon to the min longitude to ensure alignment
                } else if (lonIndex < numLon - 2) {
                    lon += deltaLon;
                } else {
                    lon = maxLon; // explicitly set the last lon to the max longitude to ensure alignment
                }

                double cosLon = Math.cos(lon);
                double sinLon = Math.sin(lon);

                result[resultIndex++] = (float) ((height + rpm) * cosLat * sinLon + xOffset);
                result[resultIndex++] = (float) ((height + rpm * (1.0 - ec2)) * sinLat + yOffset);
                result[resultIndex++] = (float) ((height + rpm) * cosLat * cosLon + zOffset);

                if (lonIndex == 0 && latIndex != 0 && latIndex != numLat - 1) {
                    int skip = numLon - 2;
                    lonIndex += skip;
                    resultIndex += skip * 3;
                }
            }
        }

        return result;
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "SuspiciousNameCombination"})
    @Override
    public Position cartesianToGeographic(Globe globe, double x, double y, double z, Position result) {
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

    @Override
    public Matrix4 cartesianToLocalTransform(Globe globe, double x, double y, double z, Matrix4 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToLocalTransform", "missingGlobe"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToLocalTransform", "missingResult"));
        }

        Position pos = this.cartesianToGeographic(globe, x, y, z, this.scratchPos);
        double radLat = Math.toRadians(pos.latitude);
        double radLon = Math.toRadians(pos.longitude);
        double cosLat = Math.cos(radLat);
        double sinLat = Math.sin(radLat);
        double cosLon = Math.cos(radLon);
        double sinLon = Math.sin(radLon);

        double eqr2 = globe.getEquatorialRadius() * globe.getEquatorialRadius();
        double pol2 = globe.getPolarRadius() * globe.getPolarRadius();

        // Compute the surface normal at the geographic position. This is equivalent to calling
        // geographicToCartesianNormal but is much more efficient as an inline computation.
        double ux = cosLat * sinLon / eqr2;
        double uy = (1 - globe.getEccentricitySquared()) * sinLat / pol2;
        double uz = cosLat * cosLon / eqr2;
        double len = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= len;
        uy /= len;
        uz /= len;

        // Compute the north pointing tangent at the geographic position. This computation could be encoded in its own
        // method, but is much more efficient as an inline computation. The north-pointing tangent is derived by
        // rotating the vector (0, 1, 0) about the Y-axis by longitude degrees, then rotating it about the X-axis by
        // -latitude degrees. The latitude angle must be inverted because latitude is a clockwise rotation about the
        // X-axis, and standard rotation matrices assume counter-clockwise rotation. The combined rotation can be
        // represented by a combining two rotation matrices Rlat, and Rlon, then transforming the vector (0, 1, 0) by
        // the combined transform: NorthTangent = (Rlon * Rlat) * (0, 1, 0)
        //
        // Additionally, this computation can be simplified by making two observations:
        // - The vector's X and Z coordinates are always 0, and its Y coordinate is always 1.
        // - Inverting the latitude rotation angle is equivalent to inverting sinLat. We know this by the
        //   trigonometric identities cos(-x) = cos(x), and sin(-x) = -sin(x).
        double nx = -sinLat * sinLon;
        double ny = cosLat;
        double nz = -sinLat * cosLon;
        len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        nx /= len;
        ny /= len;
        nz /= len;

        // Compute the east pointing tangent as the cross product of the north and up axes. This is much more efficient
        // as an inline computation.
        double ex = ny * uz - nz * uy;
        double ey = nz * ux - nx * uz;
        double ez = nx * uy - ny * ux;

        // Ensure the normal, north and east vectors represent an orthonormal basis by ensuring that the north vector is
        // perpendicular to normal and east vectors. This should already be the case, but rounding errors can be
        // introduced when working with Earth sized coordinates.
        nx = uy * ez - uz * ey;
        ny = uz * ex - ux * ez;
        nz = ux * ey - uy * ex;

        // Set the result to an orthonormal basis with the East, North, and Up vectors forming the X, Y and Z axes,
        // respectively, and the Cartesian point indicating the coordinate system's origin.
        result.set(
            ex, nx, ux, x,
            ey, ny, uy, y,
            ez, nz, uz, z,
            0, 0, 0, 1);

        return result;
    }

    @Override
    public boolean intersect(Globe globe, Line line, Vec3 result) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "cartesianToGeographic", "missingGlobe"));
        }

        if (line == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "intersect", "missingLine"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ProjectionWgs84", "intersect", "missingResult"));
        }

        // Taken from "Mathematics for 3D Game Programming and Computer Graphics, Third Edition", Section 6.2.3.
        //
        // Note that the parameter n from in equations 6.70 and 6.71 is omitted here. For an ellipsoidal globe this
        // parameter is always 1, so its square and its product with any other value simplifies to the identity.

        double vx = line.direction.x;
        double vy = line.direction.y;
        double vz = line.direction.z;
        double sx = line.origin.x;
        double sy = line.origin.y;
        double sz = line.origin.z;

        double eqr = globe.getEquatorialRadius();
        double eqr2 = eqr * eqr; // nominal radius squared
        double m = eqr / globe.getPolarRadius(); // ratio of the x semi-axis length to the y semi-axis length
        double m2 = m * m;
        double a = vx * vx + m2 * vy * vy + vz * vz;
        double b = 2 * (sx * vx + m2 * sy * vy + sz * vz);
        double c = sx * sx + m2 * sy * sy + sz * sz - eqr2;
        double d = b * b - 4 * a * c; // discriminant

        if (d < 0) {
            return false;
        }

        double t = (-b - Math.sqrt(d)) / (2 * a);
        // check if the nearest intersection point is in front of the origin of the ray
        if (t > 0) {
            result.x = sx + vx * t;
            result.y = sy + vy * t;
            result.z = sz + vz * t;
            return true;
        }

        t = (-b + Math.sqrt(d)) / (2 * a);
        // check if the second intersection point is in front of the origin of the ray
        if (t > 0) {
            result.x = sx + vx * t;
            result.y = sy + vy * t;
            result.z = sz + vz * t;
            return true;
        }

        // the intersection points were behind the origin of the provided line
        return false;
    }
}
