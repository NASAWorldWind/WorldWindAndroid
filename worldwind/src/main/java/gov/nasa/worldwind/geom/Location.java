/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Geographic location with a latitude and longitude in degrees.
 */
public class Location {

    /**
     * Conversion factor for degrees to radians.
     */
    public static final double DEGREES_TO_RADIANS = Math.PI / 180.0;

    /**
     * Conversion factor for radians to degrees.
     */
    public static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;

    /**
     * The location's latitude in degrees.
     */
    public double latitude;

    /**
     * The location's longitude in degrees.
     */
    public double longitude;

    /**
     * Constructs a location from a specified latitude and longitude in degrees.
     *
     * @param latitude  the latitude in degrees.
     * @param longitude the longitude in degrees.
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructs a location from a specified latitude and longitude in degrees.
     *
     * @param latitudeDegrees  the latitude in degrees.
     * @param longitudeDegrees the longitude in degrees.
     *
     * @return the new location.
     */
    public static Location fromDegrees(double latitudeDegrees, double longitudeDegrees) {
        return new Location(latitudeDegrees, longitudeDegrees);
    }

    /**
     * Constructs a location from a specified latitude and longitude in radians.
     *
     * @param latitudeRadians  the latitude in radians.
     * @param longitudeRadians the longitude in radians.
     *
     * @return the new location.
     */
    public static Location fromRadians(double latitudeRadians, double longitudeRadians) {
        return new Location(latitudeRadians * RADIANS_TO_DEGREES, longitudeRadians * RADIANS_TO_DEGREES);
    }

    /**
     * Normalizes a specified value to be within the range of [-180, 180] degrees.
     *
     * @param degrees the value to normalize, in degrees.
     *
     * @return the specified value normalized to [-180, 180] degrees.
     */
    public static double normalizeDegrees(double degrees) {
        double angle = degrees % 360;
        return angle > 180 ? angle - 360 : angle < -180 ? 360 + angle : angle;
    }

    /**
     * Normalizes a specified value to be within the range of [-90, 90] degrees.
     *
     * @param degreesLatitude the value to normalize, in degrees.
     *
     * @return the specified value normalized to the normal range of latitude.
     */
    public static double normalizeLatitude(double degreesLatitude) {
        double lat = degreesLatitude % 180;
        return lat > 90 ? 180 - lat : lat < -90 ? -180 - lat : lat;
    }

    /**
     * Normalizes a specified value to be within the range of [-180, 180] degrees.
     *
     * @param degreesLongitude the value to normalize, in degrees.
     *
     * @return the specified value normalized to the normal range of longitude.
     */
    public static double normalizeLongitude(double degreesLongitude) {
        double lon = degreesLongitude % 360;
        return lon > 180 ? lon - 360 : lon < -180 ? 360 + lon : lon;
    }

    /**
     * Returns a new location with latitude and longitude both 0.
     *
     * @return a location at 0.
     */
    public static Location zero() {
        return new Location(0, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Location that = (Location) o;
        return this.latitude == that.latitude
            && this.longitude == that.longitude;
    }

    @Override
    public String toString() {
        return this.latitude + "\u00b0, " + this.longitude + "\u00b0";
    }

    /**
     * Sets this location to a specified latitude and longitude in degrees.
     *
     * @param latitude  the new latitude in degrees.
     * @param longitude the new longitude in degrees.
     *
     * @return this location.
     */
    public Location set(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    /**
     * Sets this location to the latitude and longitude of a specified location.
     *
     * @param that the location specifying the new coordinates.
     *
     * @return this location.
     */
    public Location set(Location that) {
        this.latitude = that.latitude;
        this.longitude = that.longitude;
        return this;
    }

    /**
     * Compute a location along a path at a specified distance between two specified locations.
     *
     * @param pathType    the type of path to assume.
     * @param amount      The fraction of the path between the two locations at which to compute the
     *                    new location. This number should be between 0 and 1. If not, it is clamped
     *                    to the nearest of those values.
     * @param endLocation The ending location.
     * @param result      A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null.
     */
    public Location interpolateAlongPath(PathType pathType, double amount, Location endLocation, Location result) {
        if (endLocation == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "interpolateAlongPath", "missingLocation"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "interpolateAlongPath", "missingResult"));
        }

        if (this.equals(endLocation)) {
            return result.set(this);
        }

        if (pathType == PathType.GREAT_CIRCLE) {
            double azimuthDegrees = this.greatCircleAzimuth(endLocation);
            double distanceRadians = this.greatCircleDistance(endLocation) * amount;
            return this.greatCircleLocation(azimuthDegrees, distanceRadians, result);
        } else if (pathType == PathType.RHUMB_LINE) {
            double azimuthDegrees = this.rhumbAzimuth(endLocation);
            double distanceRadians = this.rhumbDistance(endLocation) * amount;
            return this.rhumbLocation(azimuthDegrees, distanceRadians, result);
        } else {
            double azimuthDegrees = this.linearAzimuth(endLocation);
            double distanceRadians = this.linearDistance(endLocation) * amount;
            return this.linearLocation(azimuthDegrees, distanceRadians, result);
        }
    }

    /**
     * Computes the azimuth angle (clockwise from North) that points from the first location to the
     * second location. This angle can be used as the starting azimuth for a great circle arc that
     * begins at the first location, and passes through the second location. This function uses a
     * spherical model, not elliptical.
     *
     * @param that The ending location.
     *
     * @return The computed azimuth, in degrees.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    public double greatCircleAzimuth(Location that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleAzimuth", "missingLocation"));
        }

        double lat1 = this.latitude * DEGREES_TO_RADIANS;
        double lat2 = that.latitude * DEGREES_TO_RADIANS;
        double lon1 = this.longitude * DEGREES_TO_RADIANS;
        double lon2 = that.longitude * DEGREES_TO_RADIANS;

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        if (lon1 == lon2) {
            return lat1 > lat2 ? 180 : 0;
        }

        // Taken from "Map Projections - A Working Manual", page 30, equation 5-4b.
        // The atan2() function is used in place of the traditional atan(y/x) to simplify the case when x == 0.
        double y = Math.cos(lat2) * Math.sin(lon2 - lon1);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
        double azimuthRadians = Math.atan2(y, x);

        return Double.isNaN(azimuthRadians) ? 0 : azimuthRadians * RADIANS_TO_DEGREES;
    }

    /**
     * Computes the great circle angular distance between two locations. The return value gives the
     * distance as the angle between the two positions. In radians, this angle is the arc length of
     * the segment between the two positions. To compute a distance in meters from this value,
     * multiply the return value by the radius of the globe. This function uses a spherical model,
     * not elliptical.
     *
     * @param that The ending location.
     *
     * @return The computed distance, in radians.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    public double greatCircleDistance(Location that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleDistance", "missingLocation"));
        }

        double lat1Radians = this.latitude * DEGREES_TO_RADIANS;
        double lat2Radians = that.latitude * DEGREES_TO_RADIANS;
        double lon1Radians = this.longitude * DEGREES_TO_RADIANS;
        double lon2Radians = that.longitude * DEGREES_TO_RADIANS;

        if (lat1Radians == lat2Radians && lon1Radians == lon2Radians) {
            return 0;
        }

        // "Haversine formula," taken from http://en.wikipedia.org/wiki/Great-circle_distance#Formul.C3.A6
        double a = Math.sin((lat2Radians - lat1Radians) / 2.0);
        double b = Math.sin((lon2Radians - lon1Radians) / 2.0);
        double c = a * a + Math.cos(lat1Radians) * Math.cos(lat2Radians) * b * b;
        double distanceRadians = 2.0 * Math.asin(Math.sqrt(c));

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }

    /**
     * Computes the location on a great circle path corresponding to a given starting location,
     * azimuth, and arc distance. This function uses a spherical model, not elliptical.
     *
     * @param azimuthDegrees  The azimuth in degrees.
     * @param distanceRadians The radian distance along the path at which to compute the end
     *                        location.
     * @param result          A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null or
     *                                  undefined.
     */
    public Location greatCircleLocation(double azimuthDegrees, double distanceRadians, Location result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleLocation", "missingResult"));
        }

        if (distanceRadians == 0) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
            return result;
        }

        double latRadians = this.latitude * DEGREES_TO_RADIANS;
        double lonRadians = this.longitude * DEGREES_TO_RADIANS;
        double azimuthRadians = azimuthDegrees * DEGREES_TO_RADIANS;

        // Taken from "Map Projections - A Working Manual", page 31, equation 5-5 and 5-6.
        double endLatRadians = Math.asin(Math.sin(latRadians) * Math.cos(distanceRadians) +
            Math.cos(latRadians) * Math.sin(distanceRadians) * Math.cos(azimuthRadians));
        double endLonRadians = lonRadians + Math.atan2(
            Math.sin(distanceRadians) * Math.sin(azimuthRadians),
            Math.cos(latRadians) * Math.cos(distanceRadians) -
                Math.sin(latRadians) * Math.sin(distanceRadians) * Math.cos(azimuthRadians));

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians)) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
        } else {
            result.latitude = normalizeLatitude(endLatRadians * RADIANS_TO_DEGREES);
            result.longitude = normalizeLongitude(endLonRadians * RADIANS_TO_DEGREES);
        }

        return result;
    }

    /**
     * Computes the azimuth angle (clockwise from North) that points from the first location to the
     * second location. This angle can be used as the azimuth for a rhumb arc that begins at the
     * first location, and passes through the second location. This function uses a spherical model,
     * not elliptical.
     *
     * @param that The ending location.
     *
     * @return The computed azimuth, in degrees.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    public double rhumbAzimuth(Location that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbAzimuth", "missingLocation"));
        }

        double lat1 = this.latitude * DEGREES_TO_RADIANS;
        double lat2 = that.latitude * DEGREES_TO_RADIANS;
        double lon1 = this.longitude * DEGREES_TO_RADIANS;
        double lon2 = that.longitude * DEGREES_TO_RADIANS;

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        double dLon = lon2 - lon1;
        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4) /
            Math.tan(lat1 / 2.0 + Math.PI / 4));

        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double azimuthRadians = Math.atan2(dLon, dPhi);

        return Double.isNaN(azimuthRadians) ? 0 : azimuthRadians * RADIANS_TO_DEGREES;
    }

    /**
     * Computes the rhumb angular distance between two locations. The return value gives the
     * distance as the angle between the two positions in radians. This angle is the arc length of
     * the segment between the two positions. To compute a distance in meters from this value,
     * multiply the return value by the radius of the globe. This function uses a spherical model,
     * not elliptical.
     *
     * @param that The ending location.
     *
     * @return The computed distance, in radians.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    public double rhumbDistance(Location that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbDistance", "missingLocation"));
        }

        double lat1 = this.latitude * DEGREES_TO_RADIANS;
        double lat2 = that.latitude * DEGREES_TO_RADIANS;
        double lon1 = this.longitude * DEGREES_TO_RADIANS;
        double lon2 = that.longitude * DEGREES_TO_RADIANS;

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4) /
            Math.tan(lat1 / 2.0 + Math.PI / 4));
        double q = dLat / dPhi;

        if (Double.isNaN(dPhi) || Double.isNaN(q)) {
            q = Math.cos(lat1);
        }

        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double distanceRadians = Math.sqrt(dLat * dLat + q * q * dLon * dLon);

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }

    /**
     * Computes the location on a rhumb arc with the given starting location, azimuth, and arc
     * distance. This function uses a spherical model, not elliptical.
     *
     * @param azimuthDegrees  The azimuth in degrees.
     * @param distanceRadians The radian distance along the path at which to compute the location.
     * @param result          A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null or
     *                                  undefined.
     */
    public Location rhumbLocation(double azimuthDegrees, double distanceRadians, Location result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbLocation", "missingResult"));
        }

        if (distanceRadians == 0) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
            return result;
        }

        double latRadians = this.latitude * DEGREES_TO_RADIANS;
        double lonRadians = this.longitude * DEGREES_TO_RADIANS;
        double azimuthRadians = azimuthDegrees * DEGREES_TO_RADIANS;
        double endLatRadians = latRadians + distanceRadians * Math.cos(azimuthRadians);
        double endLonRadians;
        double dPhi = Math.log(Math.tan(endLatRadians / 2 + Math.PI / 4) /
            Math.tan(latRadians / 2 + Math.PI / 4));
        double q = (endLatRadians - latRadians) / dPhi;

        if (Double.isNaN(dPhi) || Double.isNaN(q) || Double.isInfinite(q)) {
            q = Math.cos(latRadians);
        }

        double dLon = distanceRadians * Math.sin(azimuthRadians) / q;

        // Handle latitude passing over either pole.
        if (Math.abs(endLatRadians) > Math.PI / 2) {
            endLatRadians = endLatRadians > 0 ? Math.PI - endLatRadians : -Math.PI - endLatRadians;
        }

        endLonRadians = (lonRadians + dLon + Math.PI) % (2 * Math.PI) - Math.PI;

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians)) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
        } else {
            result.latitude = normalizeLatitude(endLatRadians * RADIANS_TO_DEGREES);
            result.longitude = normalizeLongitude(endLonRadians * RADIANS_TO_DEGREES);
        }

        return result;
    }

    /**
     * Computes the azimuth angle (clockwise from North) that points from the first location to the
     * second location. This angle can be used as the azimuth for a linear arc that begins at the
     * first location, and passes through the second location.
     *
     * @param that The ending location.
     *
     * @return The computed azimuth, in degrees.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    public double linearAzimuth(Location that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearAzimuth", "missingLocation"));
        }

        double lat1 = this.latitude * DEGREES_TO_RADIANS;
        double lat2 = that.latitude * DEGREES_TO_RADIANS;
        double lon1 = this.longitude * DEGREES_TO_RADIANS;
        double lon2 = that.longitude * DEGREES_TO_RADIANS;

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        double dLon = lon2 - lon1;
        double dPhi = lat2 - lat1;

        // If longitude change is over 180 take shorter path across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double azimuthRadians = Math.atan2(dLon, dPhi);

        return Double.isNaN(azimuthRadians) ? 0 : azimuthRadians * RADIANS_TO_DEGREES;
    }

    /**
     * Computes the linear angular distance between two locations. The return value gives the
     * distance as the angle between the two positions in radians. This angle is the arc length of
     * the segment between the two positions. To compute a distance in meters from this value,
     * multiply the return value by the radius of the globe.
     *
     * @param that The ending location.
     *
     * @return The computed distance, in radians.
     *
     * @throws IllegalArgumentException If either specified location is null or undefined.
     */
    public double linearDistance(Location that) {
        if (that == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearDistance", "missingLocation"));
        }

        double lat1 = this.latitude * DEGREES_TO_RADIANS;
        double lat2 = that.latitude * DEGREES_TO_RADIANS;
        double lon1 = this.longitude * DEGREES_TO_RADIANS;
        double lon2 = that.longitude * DEGREES_TO_RADIANS;

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // If lonChange over 180 take shorter path across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double distanceRadians = Math.sqrt(dLat * dLat + dLon * dLon);

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }

    /**
     * Computes the location on a linear path with the given starting location, azimuth, and arc
     * distance.
     *
     * @param azimuthDegrees  The azimuth in degrees.
     * @param distanceRadians The radian distance along the path at which to compute the location.
     * @param result          A Location in which to return the result.
     *
     * @return The specified result location.
     *
     * @throws IllegalArgumentException If the specified location or the result argument is null or
     *                                  undefined.
     */
    public Location linearLocation(double azimuthDegrees, double distanceRadians, Location result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearLocation", "missingResult"));
        }

        if (distanceRadians == 0) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
            return result;
        }

        double latRadians = this.latitude * DEGREES_TO_RADIANS;
        double lonRadians = this.longitude * DEGREES_TO_RADIANS;
        double azimuthRadians = azimuthDegrees * DEGREES_TO_RADIANS;
        double endLatRadians = latRadians + distanceRadians * Math.cos(azimuthRadians);
        double endLonRadians;

        // Handle latitude passing over either pole.
        if (Math.abs(endLatRadians) > Math.PI / 2) {
            endLatRadians = endLatRadians > 0 ? Math.PI - endLatRadians : -Math.PI - endLatRadians;
        }

        endLonRadians = (lonRadians + distanceRadians * Math.sin(azimuthRadians) + Math.PI) %
            (2 * Math.PI) - Math.PI;

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians)) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
        } else {
            result.latitude = normalizeLatitude(endLatRadians * RADIANS_TO_DEGREES);
            result.longitude = normalizeLongitude(endLonRadians * RADIANS_TO_DEGREES);
        }

        return result;
    }
}
