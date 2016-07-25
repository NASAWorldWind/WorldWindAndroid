/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import android.util.SparseIntArray;

import java.util.List;
import java.util.TimeZone;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

// TODO test performance of making all geom methods final

/**
 * Geographic location with a latitude and longitude in degrees.
 */
public class Location {

    protected static SparseIntArray timeZoneLatitudes = new SparseIntArray();

    protected final static double NEAR_ZERO_THRESHOLD = 1e-15;

    static {
        timeZoneLatitudes.put(-12, -45);// GMT-12
        timeZoneLatitudes.put(-11, -30);// GMT-11
        timeZoneLatitudes.put(-10, 20); // GMT-10
        timeZoneLatitudes.put(-9, 45);  // GMT-9
        timeZoneLatitudes.put(-8, 40);  // GMT-8
        timeZoneLatitudes.put(-7, 35);  // GMT-7
        timeZoneLatitudes.put(-6, 30);  // GMT-6
        timeZoneLatitudes.put(-5, 25);  // GMT-5
        timeZoneLatitudes.put(-4, -15); // GMT-4
        timeZoneLatitudes.put(-3, 0);   // GMT-3
        timeZoneLatitudes.put(-2, 45);  // GMT-2
        timeZoneLatitudes.put(-1, 30);   // GMT-1
        timeZoneLatitudes.put(0, 30);   // GMT+0
        timeZoneLatitudes.put(1, 20);   // GMT+1
        timeZoneLatitudes.put(2, 20);   // GMT+2
        timeZoneLatitudes.put(3, 25);   // GMT+3
        timeZoneLatitudes.put(4, 30);   // GMT+4
        timeZoneLatitudes.put(5, 35);   // GMT+5
        timeZoneLatitudes.put(6, 30);   // GMT+6
        timeZoneLatitudes.put(7, 25);   // GMT+7
        timeZoneLatitudes.put(8, -30);  // GMT+8
        timeZoneLatitudes.put(9, -30);  // GMT+9
        timeZoneLatitudes.put(10, -30); // GMT+10
        timeZoneLatitudes.put(11, -45); // GMT+11
        timeZoneLatitudes.put(12, -45); // GMT+12
    }

    /**
     * The location's latitude in degrees.
     */
    public double latitude;

    /**
     * The location's longitude in degrees.
     */
    public double longitude;

    /**
     * Constructs a location with latitude and longitude both 0.
     */
    public Location() {
    }

    /**
     * Constructs a location with a specified latitude and longitude in degrees.
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructs a location with the latitude and longitude of a specified location.
     *
     * @param location the location specifying the coordinates
     *
     * @throws IllegalArgumentException If the location is null
     */
    public Location(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "constructor", "missingLocation"));
        }

        this.latitude = location.latitude;
        this.longitude = location.longitude;
    }

    /**
     * Constructs a location with a specified latitude and longitude in degrees.
     *
     * @param latitudeDegrees  the latitude in degrees
     * @param longitudeDegrees the longitude in degrees
     *
     * @return the new location
     */
    public static Location fromDegrees(double latitudeDegrees, double longitudeDegrees) {
        Location loc = new Location();
        loc.latitude = latitudeDegrees;
        loc.longitude = longitudeDegrees;
        return loc;
    }

    /**
     * Constructs a location with a specified latitude and longitude in radians.
     *
     * @param latitudeRadians  the latitude in radians
     * @param longitudeRadians the longitude in radians
     *
     * @return the new location
     */
    public static Location fromRadians(double latitudeRadians, double longitudeRadians) {
        Location loc = new Location();
        loc.latitude = Math.toDegrees(latitudeRadians);
        loc.longitude = Math.toDegrees(longitudeRadians);
        return loc;
    }

    /**
     * Constructs an approximate location for a specified time zone. Used when selecting an initial navigator position
     * based on the device's current time zone.
     *
     * @param timeZone the time zone in question
     *
     * @return the new location
     */
    public static Location fromTimeZone(TimeZone timeZone) {
        if (timeZone == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "fromTimeZone", "The time zone is null"));
        }

        double millisPerHour = 3.6e6;
        int offsetMillis = timeZone.getRawOffset();
        int offsetHours = (int) (offsetMillis / millisPerHour);

        double lat = timeZoneLatitudes.get(offsetHours, 0); // use a pre-determined latitude or 0 if none is available
        double lon = 180 * offsetHours / 12; // center on the time zone's average longitude
        return new Location(lat, lon);
    }

    /**
     * Restricts an angle to the range [-90, +90] degrees, wrapping angles outside the range. Wrapping takes place along
     * a line of constant longitude which may pass through the poles. In which case, 135 degrees normalizes to 45
     * degrees; 181 degrees normalizes to -1 degree.
     *
     * @param degrees the angle to wrap in degrees
     *
     * @return the specified angle wrapped to the range [-90, +90] degrees
     */
    public static double normalizeLatitude(double degrees) {
        double lat = degrees % 180;
        double normalizedLat = lat > 90 ? 180 - lat : (lat < -90 ? -180 - lat : lat);
        // Determine whether whether the latitude is in the north or south hemisphere
        int numEquatorCrosses = (int) (degrees / 180);
        return (numEquatorCrosses % 2 == 0) ? normalizedLat : -normalizedLat;
    }

    /**
     * Restricts an angle to the range [-180, +180] degrees, wrapping angles outside the range. Wrapping takes place as
     * though traversing a line of constant latitude which may pass through the antimeridian; angles less than -180 wrap
     * back to +180, while angles greater than +180 wrap back to -180.
     *
     * @param degrees the angle to wrap in degrees
     *
     * @return the specified angle wrapped to the range [-180, +180] degrees
     */
    public static double normalizeLongitude(double degrees) {
        double lon = degrees % 360;
        return lon > 180 ? lon - 360 : (lon < -180 ? 360 + lon : lon);
    }

    /**
     * Restricts an angle to the range [-90, +90] degrees, clamping angles outside the range. Angles less than -90 are
     * returned as -90, and angles greater than +90 are returned as +90. Angles within the range are returned
     * unmodified.
     *
     * @param degrees the angle to clamp in degrees
     *
     * @return the specified angle clamped to the range [-90, +90] degrees
     */
    public static double clampLatitude(double degrees) {
        return degrees > 90 ? 90 : (degrees < -90 ? -90 : degrees);
    }

    /**
     * Restricts an angle to the range [-180, +180] degrees, clamping angles outside the range. Angles less than -180
     * are returned as 0, and angles greater than +180 are returned as +180. Angles within the range are returned
     * unmodified.
     *
     * @param degrees the angle to clamp in degrees
     *
     * @return the specified angle clamped to the range [-180, +180] degrees
     */
    public static double clampLongitude(double degrees) {
        return degrees > 180 ? 180 : (degrees < -180 ? -180 : degrees);
    }

    /**
     * Determines whether a list of locations crosses the antimeridian.
     *
     * @param locations the locations to test
     *
     * @return true if the antimeridian is crossed, false otherwise
     *
     * @throws IllegalArgumentException If the locations list is null
     */
    public static boolean locationsCrossAntimeridian(List<? extends Location> locations) {
        if (locations == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "locationsCrossAntimeridian", "missingList"));
        }

        // Check the list's length. A list with fewer than two locations does not cross the antimeridan.
        int len = locations.size();
        if (len < 2) {
            return false;
        }

        // Compute the longitude attributes associated with the first location.
        double lon1 = normalizeLongitude(locations.get(0).longitude);
        double sig1 = Math.signum(lon1);

        // Iterate over the segments in the list. A segment crosses the antimeridian if its endpoint longitudes have
        // different signs and are more than 180 degrees apart (but not 360, which indicates the longitudes are the same).
        for (int idx = 1; idx < len; idx++) {
            double lon2 = normalizeLongitude(locations.get(idx).longitude);
            double sig2 = Math.signum(lon2);

            if (sig1 != sig2) {
                double delta = Math.abs(lon1 - lon2);
                if (delta > 180 && delta < 360) {
                    return true;
                }
            }

            lon1 = lon2;
            sig1 = sig2;
        }

        return false;
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
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return this.latitude + "\u00b0, " + this.longitude + "\u00b0";
    }

    /**
     * Sets this location to a specified latitude and longitude in degrees.
     *
     * @param latitude  the new latitude in degrees
     * @param longitude the new longitude in degrees
     *
     * @return this location with its latitude and longitude set to the specified values
     */
    public Location set(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    /**
     * Sets this location to the latitude and longitude of a specified location.
     *
     * @param location the location specifying the new coordinates
     *
     * @return this location with its latitude and longitude set to that of the specified location
     *
     * @throws IllegalArgumentException If the location is null
     */
    public Location set(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "set", "missingLocation"));
        }

        this.latitude = location.latitude;
        this.longitude = location.longitude;
        return this;
    }

    /**
     * Compute a location along a path between two locations. The amount indicates the fraction of the path at which to
     * compute a location. This value is typically between 0 and 1, where 0 indicates the begin location (this location)
     * and 1 indicates the end location.
     *
     * @param endLocation the path's end location
     * @param pathType    {@link gov.nasa.worldwind.WorldWind.PathType} indicating type of path to assume
     * @param amount      the fraction of the path at which to compute a location
     * @param result      a pre-allocated Location in which to return the computed location
     *
     * @return the result argument set to the computed location
     *
     * @throws IllegalArgumentException If either of the end location or the result argument is null
     */
    public Location interpolateAlongPath(Location endLocation, @WorldWind.PathType int pathType, double amount,
                                         Location result) {
        if (endLocation == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "interpolateAlongPath", "missingLocation"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "interpolateAlongPath", "missingResult"));
        }

        if (this.equals(endLocation)) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
            return this;
        }

        if (pathType == WorldWind.GREAT_CIRCLE) {
            double azimuthDegrees = this.greatCircleAzimuth(endLocation);
            double distanceRadians = this.greatCircleDistance(endLocation) * amount;
            return this.greatCircleLocation(azimuthDegrees, distanceRadians, result);
        } else if (pathType == WorldWind.RHUMB_LINE) {
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
     * Computes the azimuth angle (clockwise from North) for the great circle path between this location and a specified
     * location. This angle can be used as the starting azimuth for a great circle path beginning at this location, and
     * passing through the specified location. This function uses a spherical model, not elliptical.
     *
     * @param location the great circle path's ending location
     *
     * @return the computed azimuth in degrees
     *
     * @throws IllegalArgumentException If the location is null
     */
    public double greatCircleAzimuth(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleAzimuth", "missingLocation"));
        }

        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(location.latitude);
        double lon2 = Math.toRadians(location.longitude);

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

        return Double.isNaN(azimuthRadians) ? 0 : Math.toDegrees(azimuthRadians);
    }

    /**
     * Computes the angular distance of the great circle path between this location and a specified location. In
     * radians, this angle is the arc length of the segment between the two locations. To compute a distance in meters
     * from this value, multiply the return value by the radius of the globe. This function uses a spherical model, not
     * elliptical.
     *
     * @param location the great circle path's ending location
     *
     * @return the computed angular distance in radians
     *
     * @throws IllegalArgumentException If the location is null
     */
    public double greatCircleDistance(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "greatCircleDistance", "missingLocation"));
        }

        double lat1Radians = Math.toRadians(this.latitude);
        double lon1Radians = Math.toRadians(this.longitude);
        double lat2Radians = Math.toRadians(location.latitude);
        double lon2Radians = Math.toRadians(location.longitude);

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
     * Computes the location on the great circle path starting at this location and traversing with the specified
     * azimuth and angular distance. This function uses a spherical model, not elliptical.
     *
     * @param azimuthDegrees  the starting azimuth in degrees
     * @param distanceRadians the angular distance along the path in radians
     * @param result          a pre-allocated Location in which to return the computed location
     *
     * @return the result argument set to the computed location
     *
     * @throws IllegalArgumentException If either the location or the result is null
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

        double latRadians = Math.toRadians(this.latitude);
        double lonRadians = Math.toRadians(this.longitude);
        double azimuthRadians = Math.toRadians(azimuthDegrees);
        double cosLat = Math.cos(latRadians);
        double sinLat = Math.sin(latRadians);
        double cosAzimuth = Math.cos(azimuthRadians);
        double sinAzimuth = Math.sin(azimuthRadians);
        double sinDistance = Math.sin(distanceRadians);
        double cosDistance = Math.cos(distanceRadians);

        // Taken from "Map Projections - A Working Manual", page 31, equation 5-5 and 5-6.
        double endLatRadians = Math.asin(sinLat * cosDistance +
            cosLat * sinDistance * cosAzimuth);
        double endLonRadians = lonRadians + Math.atan2(
            sinDistance * sinAzimuth,
            cosLat * cosDistance - sinLat * sinDistance * cosAzimuth);

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians)) {
            result.latitude = this.latitude;
            result.longitude = this.longitude;
        } else {
            result.latitude = normalizeLatitude(Math.toDegrees(endLatRadians));
            result.longitude = normalizeLongitude(Math.toDegrees(endLonRadians));
        }

        return result;
    }

    /**
     * Computes the azimuth angle (clockwise from North) for the rhumb path (line of constant azimuth) between this
     * location and a specified location. This angle can be used as the starting azimuth for a rhumb path beginning at
     * this location, and passing through the specified location. This function uses a spherical model, not elliptical.
     *
     * @param location the rhumb path's ending location
     *
     * @return the computed azimuth in degrees
     *
     * @throws IllegalArgumentException If the location is null
     */
    public double rhumbAzimuth(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbAzimuth", "missingLocation"));
        }

        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(location.latitude);
        double lon2 = Math.toRadians(location.longitude);

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

        return Double.isNaN(azimuthRadians) ? 0 : Math.toDegrees(azimuthRadians);
    }

    /**
     * Computes the angular distance of the rhumb path (line of constant azimuth) between this location and a specified
     * location. In radians, this angle is the arc length of the segment between the two locations. To compute a
     * distance in meters from this value, multiply the return value by the radius of the globe. This function uses a
     * spherical model, not elliptical.
     *
     * @param location the great circle path's ending location
     *
     * @return the computed angular distance in radians
     *
     * @throws IllegalArgumentException If the location is null
     */
    public double rhumbDistance(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "rhumbDistance", "missingLocation"));
        }

        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(location.latitude);
        double lon2 = Math.toRadians(location.longitude);

        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double q;
        if (Math.abs(dLat) < NEAR_ZERO_THRESHOLD) {
            // Avoid indeterminates along E/W courses when lat end points are "nearly" identical
            q = Math.cos(lat1);
        } else {
            double dPhi = Math.log(Math.tan(lat2 / 2.0 + Math.PI / 4) /
                Math.tan(lat1 / 2.0 + Math.PI / 4));
            q = dLat / dPhi;
        }

        // If lonChange over 180 take shorter rhumb across 180 meridian.
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }

        double distanceRadians = Math.sqrt(dLat * dLat + q * q * dLon * dLon);

        return Double.isNaN(distanceRadians) ? 0 : distanceRadians;
    }

    /**
     * Computes the location on a rhumb path (line of constant azimuth) starting at this location and traversing with
     * the specified azimuth and angular distance. This function uses a spherical model, not elliptical.
     *
     * @param azimuthDegrees  the starting azimuth in degrees
     * @param distanceRadians the angular distance along the path in radians
     * @param result          a pre-allocated Location in which to return the computed location
     *
     * @return the result argument set to the computed location
     *
     * @throws IllegalArgumentException If either the location or the result is null
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

        double latRadians = Math.toRadians(this.latitude);
        double lonRadians = Math.toRadians(this.longitude);
        double azimuthRadians = Math.toRadians(azimuthDegrees);
        double endLatRadians = latRadians + distanceRadians * Math.cos(azimuthRadians);
        double endLonRadians;
        double dLat = endLatRadians - latRadians;
        double q;
        if (Math.abs(dLat) < NEAR_ZERO_THRESHOLD) {
            // Avoid indeterminates along E/W courses when lat end points are "nearly" identical
            q = Math.cos(latRadians);
        } else {
            double dPhi = Math.log(Math.tan(endLatRadians / 2 + Math.PI / 4) /
                Math.tan(latRadians / 2 + Math.PI / 4));
            q = dLat / dPhi;
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
            result.latitude = normalizeLatitude(Math.toDegrees(endLatRadians));
            result.longitude = normalizeLongitude(Math.toDegrees(endLonRadians));
        }

        return result;
    }

    /**
     * Computes the azimuth angle (clockwise from North) for the linear path between this location and a specified
     * location. This angle can be used as the starting azimuth for a linear path beginning at this location, and
     * passing through the specified location. This function uses a flat-earth approximation proximal to this location.
     *
     * @param location the linear path's ending location
     *
     * @return the computed azimuth in degrees
     *
     * @throws IllegalArgumentException If the location is null
     */
    public double linearAzimuth(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearAzimuth", "missingLocation"));
        }

        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(location.latitude);
        double lon2 = Math.toRadians(location.longitude);

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

        return Double.isNaN(azimuthRadians) ? 0 : Math.toDegrees(azimuthRadians);
    }

    /**
     * Computes the angular distance of the linear path between this location and a specified location. In radians, this
     * angle is the arc length of the segment between the two locations. To compute a distance in meters from this
     * value, multiply the return value by the radius of the globe. This function uses a flat-earth approximation
     * proximal to this location.
     *
     * @param location the great circle path's ending location
     *
     * @return the computed angular distance in radians
     *
     * @throws IllegalArgumentException If the location is null
     */
    public double linearDistance(Location location) {
        if (location == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Location", "linearDistance", "missingLocation"));
        }

        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(location.latitude);
        double lon2 = Math.toRadians(location.longitude);

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
     * Computes the location on the linear path starting at this location and traversing with the specified azimuth and
     * angular distance. This function uses a flat-earth approximation proximal to this location.
     *
     * @param azimuthDegrees  the starting azimuth in degrees
     * @param distanceRadians the angular distance along the path in radians
     * @param result          a pre-allocated Location in which to return the computed location
     *
     * @return the result argument set to the computed location
     *
     * @throws IllegalArgumentException If either the location or the result is null
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

        double latRadians = Math.toRadians(this.latitude);
        double lonRadians = Math.toRadians(this.longitude);
        double azimuthRadians = Math.toRadians(azimuthDegrees);
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
            result.latitude = normalizeLatitude(Math.toDegrees(endLatRadians));
            result.longitude = normalizeLongitude(Math.toDegrees(endLonRadians));
        }

        return result;
    }
}
