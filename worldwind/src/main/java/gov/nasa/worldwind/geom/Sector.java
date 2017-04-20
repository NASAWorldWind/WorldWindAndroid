/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Geographic rectangular region in degrees.
 */
public class Sector {

    /**
     * The sector's minimum latitude in degrees.
     */
    protected double minLatitude = Double.NaN;

    /**
     * The sector's maximum latitude in degrees.
     */
    protected double maxLatitude = Double.NaN;

    /**
     * The sector's minimum longitude in degrees.
     */
    protected double minLongitude = Double.NaN;

    /**
     * The sector's maximum longitude in degrees.
     */
    protected double maxLongitude = Double.NaN;

    /**
     * Constructs an empty sector with minimum and maximum latitudes and longitudes all NaN.
     */
    public Sector() {
    }

    /**
     * Constructs a sector with the specified latitude and longitude values in degrees.
     *
     * @param minLatitude    the minimum latitude, i.e., the latitude at the southwest corner of the sector.
     * @param minLongitude   the minimum longitude, i.e., the longitude at the southwest corner of the sector.
     * @param deltaLatitude  the width of the sector, specified in degrees; must equal to or greater than zero.
     * @param deltaLongitude the height of the sector, specified in degrees; must equal to or greater than zero.
     */
    public Sector(double minLatitude, double minLongitude, double deltaLatitude, double deltaLongitude) {
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = Location.clampLatitude(minLatitude + (deltaLatitude > 0 ? deltaLatitude : Double.NaN));
        this.maxLongitude = Location.clampLongitude(minLongitude + (deltaLongitude > 0 ? deltaLongitude : Double.NaN));
    }

    /**
     * Constructs a sector with the minimum and maximum latitudes and longitudes of a specified sector.
     *
     * @param sector the sector specifying the coordinates
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public Sector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "constructor", "missingSector"));
        }
        this.minLatitude = sector.minLatitude;
        this.maxLatitude = sector.maxLatitude;
        this.minLongitude = sector.minLongitude;
        this.maxLongitude = sector.maxLongitude;
    }

    public static Sector fromDegrees(double latitudeDegrees, double longitudeDegrees,
                                     double deltaLatitudeDegrees, double deltaLongitudeDegrees) {
        return new Sector(
            latitudeDegrees, longitudeDegrees,
            deltaLatitudeDegrees, deltaLongitudeDegrees);
    }

    public static Sector fromRadians(double latitudeRadians, double longitudeRadians,
                                     double deltaLatitudeRadians, double deltaLongitudeRadians) {
        return new Sector(
            Math.toDegrees(latitudeRadians), Math.toDegrees(longitudeRadians),
            Math.toDegrees(deltaLatitudeRadians), Math.toDegrees(deltaLongitudeRadians));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Sector that = (Sector) o;
        return this.minLatitude == that.minLatitude
            && this.maxLatitude == that.maxLatitude
            && this.minLongitude == that.minLongitude
            && this.maxLongitude == that.maxLongitude;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(minLatitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxLatitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minLongitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxLongitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "minLatitude=" + minLatitude + ", maxLatitude=" + maxLatitude + ", minLongitude=" + minLongitude + ", maxLongitude=" + maxLongitude;
    }

    /**
     * Indicates whether this sector has non-zero width and height.
     *
     * @return true if either of this sector's delta latitude and/or delta longitude are zero, false otherwise.
     */
    public boolean isEmpty() {
        // Note: Comparison with a NaN is always false.
        return !(this.minLatitude < this.maxLatitude && this.minLongitude < this.maxLongitude);
    }

    /**
     * Indicates whether this sector contains the full range of latitude [90 to +90] and longitude [-180 to +180].
     *
     * @return true if this sector's coordinates are set to the full range of latitude and longitude
     */
    public boolean isFullSphere() {
        return this.minLatitude == -90
            && this.maxLatitude == 90
            && this.minLongitude == -180
            && this.maxLongitude == 180;
    }

    /**
     * Returns the this sector's minimum latitude.
     *
     * @return the minimum latitude of this sector or NaN if this sector's location is nas not been defined.
     */
    public double minLatitude() {
        return minLatitude;
    }

    /**
     * Returns this sector's maximum latitude.
     *
     * @return the maximum latitude of this sector or NaN if this sector's location is nas not been defined.
     */
    public double maxLatitude() {
        return maxLatitude;
    }

    /**
     * Returns this sector's minimum longitude.
     *
     * @return the minimum longitude of this sector or NaN if this sector's location is nas not been defined.
     */
    public double minLongitude() {
        return minLongitude;
    }

    /**
     * Returns this sector's maximum longitude.
     *
     * @return the maximum longitude of this sector or NaN if this sector's location is nas not been defined.
     */
    public double maxLongitude() {
        return maxLongitude;
    }

    /**
     * Returns the angle between this sector's minimum and maximum latitudes.
     *
     * @return the difference between this sector's minimum and maximum latitudes in degrees
     */
    public double deltaLatitude() {
        return this.maxLatitude - this.minLatitude;
    }

    /**
     * Returns the angle between this sector's minimum and maximum longitudes.
     *
     * @return the difference between this sector's minimum and maximum longitudes in degrees
     */
    public double deltaLongitude() {
        return this.maxLongitude - this.minLongitude;
    }

    /**
     * Returns the angle midway between this sector's minimum and maximum latitudes.
     *
     * @return the mid-angle of this sector's minimum and maximum latitudes in degrees
     */
    public double centroidLatitude() {
        return 0.5 * (this.minLatitude + this.maxLatitude);
    }

    /**
     * Returns the angle midway between this sector's minimum and maximum longitudes.
     *
     * @return the mid-angle of this sector's minimum and maximum longitudes in degrees
     */
    public double centroidLongitude() {
        return 0.5 * (this.minLongitude + this.maxLongitude);
    }

    /**
     * Computes the location of the angular center of this sector, which is the mid-angle of each of this sector's
     * latitude and longitude dimensions.
     *
     * @param result a pre-allocated {@link Location} in which to return the computed centroid
     *
     * @return the specified result argument containing the computed centroid
     *
     * @throws IllegalArgumentException If the result is null
     */
    public Location centroid(Location result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "centroid", "missingResult"));
        }

        result.latitude = this.centroidLatitude();
        result.longitude = this.centroidLongitude();

        return result;
    }

    /**
     * Sets this sector to the specified latitude, longitude and dimension in degrees.
     *
     * @param minLatitude    the minimum latitude, i.e., the latitude at the southwest corner of the sector.
     * @param minLongitude   the minimum longitude, i.e., the longitude at the southwest corner of the sector.
     * @param deltaLatitude  the width of the sector, specified in degrees; must equal to or greater than zero.
     * @param deltaLongitude the height of the sector, specified in degrees; must equal to or greater than zero.
     *
     * @return this sector with its coordinates set to the specified values
     */
    public Sector set(double minLatitude, double minLongitude, double deltaLatitude, double deltaLongitude) {
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = Location.clampLatitude(minLatitude + (deltaLatitude > 0 ? deltaLatitude : Double.NaN));
        this.maxLongitude = Location.clampLongitude(minLongitude + (deltaLongitude > 0 ? deltaLongitude : Double.NaN));
        return this;
    }

    /**
     * Sets this sector to the minimum and maximum latitudes and longitudes of a specified sector.
     *
     * @param sector the sector specifying the new coordinates
     *
     * @return this sector with its coordinates set to that of the specified sector
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public Sector set(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "set", "missingSector"));
        }

        this.minLatitude = sector.minLatitude;
        this.maxLatitude = sector.maxLatitude;
        this.minLongitude = sector.minLongitude;
        this.maxLongitude = sector.maxLongitude;
        return this;
    }

    /**
     * Sets this sector to an empty sector.
     *
     * @return this sector with its coordinates set to an empty sector
     */
    public Sector setEmpty() {
        this.minLatitude = this.maxLatitude = Double.NaN;
        this.minLongitude = this.maxLongitude = Double.NaN;
        return this;
    }

    /**
     * Sets this sector to the full range of latitude [90 to +90] and longitude [-180 to +180].
     *
     * @return this sector with its coordinates set to the full range of latitude and longitude
     */
    public Sector setFullSphere() {
        this.minLatitude = -90;
        this.maxLatitude = 90;
        this.minLongitude = -180;
        this.maxLongitude = 180;
        return this;
    }

    /**
     * Indicates whether this sector intersects a specified sector. Two sectors intersect when both the latitude
     * boundaries and the longitude boundaries overlap by a non-zero amount. An empty sector never intersects another
     * sector.
     * <p/>
     * The sectors are assumed to have normalized angles (angles within the range [-90, +90] latitude and [-180, +180]
     * longitude).
     *
     * @param minLatitude    the minimum latitude of the sector to test intersection with, in degrees
     * @param minLongitude   the minimum longitude of the sector to test intersection with, in degrees
     * @param deltaLatitude  the width of the sector to test intersection with, in degrees
     * @param deltaLongitude the height of the sector to test intersection with, in degrees
     *
     * @return true if the specified sector intersections this sector, false otherwise
     */
    public boolean intersects(double minLatitude, double minLongitude, double deltaLatitude, double deltaLongitude) {
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        double maxLatitude = Location.clampLatitude(minLatitude + (deltaLatitude > 0 ? deltaLatitude : Double.NaN));
        double maxLongitude = Location.clampLongitude(minLongitude + (deltaLongitude > 0 ? deltaLongitude : Double.NaN));
        // Note: comparisons with NaN are always false.
        return this.minLatitude < maxLatitude
            && this.maxLatitude > minLatitude
            && this.minLongitude < maxLongitude
            && this.maxLongitude > minLongitude;
    }

    /**
     * Indicates whether this sector intersects a specified sector. Two sectors intersect when both the latitude
     * boundaries and the longitude boundaries overlap by a non-zero amount. An empty sector never intersects another
     * sector.
     * <p/>
     * The sectors are assumed to have normalized angles (angles within the range [-90, +90] latitude and [-180, +180]
     * longitude).
     *
     * @param sector the sector to test intersection with
     *
     * @return true if the specified sector intersections this sector, false otherwise
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public boolean intersects(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "intersects", "missingSector"));
        }

        // Assumes normalized angles: [-90, +90], [-180, +180]
        // Note: comparisons with NaN are always false
        return this.minLatitude < sector.maxLatitude
            && this.maxLatitude > sector.minLatitude
            && this.minLongitude < sector.maxLongitude
            && this.maxLongitude > sector.minLongitude;
    }

    /**
     * Computes the intersection of this sector and a specified sector, storing the result in this sector and returning
     * whether or not the sectors intersect. Two sectors intersect when both the latitude boundaries and the longitude
     * boundaries overlap by a non-zero amount. An empty sector never intersects another sector. When there is no
     * intersection, this returns false and leaves this sector unchanged.
     * <p/>
     * The sectors are assumed to have normalized angles (angles within the range [-90, +90] latitude and [-180, +180]
     * longitude).
     *
     * @param minLatitude    the minimum latitude of the sector to intersect with, in degrees
     * @param minLongitude   the minimum longitude of the sector to intersect with, in degrees
     * @param deltaLatitude  the width of the sector to intersect with, in degrees
     * @param deltaLongitude the height of the sector to intersect with, in degrees
     *
     * @return true if the specified sector intersections this sector, false otherwise
     */
    public boolean intersect(double minLatitude, double minLongitude, double deltaLatitude, double deltaLongitude) {
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        double maxLatitude = Location.clampLatitude(minLatitude + (deltaLatitude > 0 ? deltaLatitude : Double.NaN));
        double maxLongitude = Location.clampLongitude(minLongitude + (deltaLongitude > 0 ? deltaLongitude : Double.NaN));
        // Note: comparisons with NaN are always false
        if ((this.minLatitude < maxLatitude) && (this.maxLatitude > minLatitude) // latitudes intersect
            && (this.minLongitude < maxLongitude) && (this.maxLongitude > minLongitude)) { // longitudes intersect

            if (this.minLatitude < minLatitude)
                this.minLatitude = minLatitude;
            if (this.maxLatitude > maxLatitude)
                this.maxLatitude = maxLatitude;
            if (this.minLongitude < minLongitude)
                this.minLongitude = minLongitude;
            if (this.maxLongitude > maxLongitude)
                this.maxLongitude = maxLongitude;

            return true;
        }

        return false; // the two sectors do not intersect
    }

    /**
     * Computes the intersection of this sector and a specified sector, storing the result in this sector and returning
     * whether or not the sectors intersect. Two sectors intersect when both the latitude boundaries and the longitude
     * boundaries overlap by a non-zero amount. An empty sector never intersects another sector. When there is no
     * intersection, this returns false and leaves this sector unchanged.
     * <p/>
     * The sectors are assumed to have normalized angles (angles within the range [-90, +90] latitude and [-180, +180]
     * longitude).
     *
     * @param sector the sector to intersect with
     *
     * @return this true if this sector intersects the specified sector, false otherwise
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public boolean intersect(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "intersect", "missingSector"));
        }

        // Assumes normalized angles: [-90, +90], [-180, +180] and comparisons with NaN are always false
        if ((this.minLatitude < sector.maxLatitude) && (this.maxLatitude > sector.minLatitude) // latitudes intersect
            && (this.minLongitude < sector.maxLongitude) && (this.maxLongitude > sector.minLongitude)) { // longitudes intersect

            if (this.minLatitude < sector.minLatitude)
                this.minLatitude = sector.minLatitude;
            if (this.maxLatitude > sector.maxLatitude)
                this.maxLatitude = sector.maxLatitude;
            if (this.minLongitude < sector.minLongitude)
                this.minLongitude = sector.minLongitude;
            if (this.maxLongitude > sector.maxLongitude)
                this.maxLongitude = sector.maxLongitude;

            return true;
        }

        return false; // the two sectors do not intersect
    }

    /**
     * Indicates whether this sector contains a specified geographic location. An empty sector never contains a
     * location.
     *
     * @param latitude  the location's latitude in degrees
     * @param longitude the location's longitude in degrees
     *
     * @return true if this sector contains the location, false otherwise
     */
    public boolean contains(double latitude, double longitude) {
        // Assumes normalized angles: [-90, +90], [-180, +180] and comparisons with NaN are always false
        return this.minLatitude <= latitude
            && this.maxLatitude >= latitude
            && this.minLongitude <= longitude
            && this.maxLongitude >= longitude;
    }

    /**
     * Indicates whether this sector fully contains a specified sector. This sector contains the specified sector when
     * the specified sector's boundaries are completely contained within this sector's boundaries, or are equal to this
     * sector's boundaries. An empty sector never contains another sector.
     * <p/>
     * The sectors are assumed to have normalized angles (angles within the range [-90, +90] latitude and [-180, +180]
     * longitude).
     *
     * @param minLatitude    the minimum latitude of the sector to test containment with, in degrees
     * @param minLongitude   the minimum longitude of the sector to test containment with, in degrees
     * @param deltaLatitude  the width of the sector to test containment with, in degrees
     * @param deltaLongitude the height of the sector to test containment with, in degrees
     *
     * @return true if the specified sector contains this sector, false otherwise
     */
    public boolean contains(double minLatitude, double minLongitude, double deltaLatitude, double deltaLongitude) {
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        double maxLatitude = Location.clampLatitude(minLatitude + (deltaLatitude > 0 ? deltaLatitude : Double.NaN));
        double maxLongitude = Location.clampLongitude(minLongitude + (deltaLongitude > 0 ? deltaLongitude : Double.NaN));

        // Note: comparisons with NaN are always false
        return this.minLatitude <= minLatitude
            && this.maxLatitude >= maxLatitude
            && this.minLongitude <= minLongitude
            && this.maxLongitude >= maxLongitude;
    }

    /**
     * Indicates whether this sector fully contains a specified sector. This sector contains the specified sector when
     * the specified sector's boundaries are completely contained within this sector's boundaries, or are equal to this
     * sector's boundaries. An empty sector never contains another sector.
     * <p/>
     * The sectors are assumed to have normalized angles (angles within the range [-90, +90] latitude and [-180, +180]
     * longitude).
     *
     * @param sector the sector to test containment with
     *
     * @return true if the specified sector contains this sector, false otherwise
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public boolean contains(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "contains", "missingSector"));
        }

        // Assumes normalized angles: [-90, +90], [-180, +180], and comparisons with NaN are always false
        return this.minLatitude <= sector.minLatitude
            && this.maxLatitude >= sector.maxLatitude
            && this.minLongitude <= sector.minLongitude
            && this.maxLongitude >= sector.maxLongitude;
    }

    /**
     * Sets this sector to the union of itself and a specified location.
     *
     * @param latitude  the location's latitude in degrees
     * @param longitude the location's longitude in degrees
     *
     * @return this sector, set to its union with the specified location
     */
    public Sector union(double latitude, double longitude) {
        // Assumes normalized angles: [-90, +90], [-180, +180], and comparisons with NaN are always false
        if (this.minLatitude < this.maxLatitude && this.minLongitude < this.maxLongitude) {
            this.maxLatitude = Math.max(this.maxLatitude, latitude);
            this.minLatitude = Math.min(this.minLatitude, latitude);
            this.maxLongitude = Math.max(this.maxLongitude, longitude);
            this.minLongitude = Math.min(this.minLongitude, longitude);
        } else if (!Double.isNaN(this.minLatitude) && !Double.isNaN(this.minLongitude)) {
            // Note, order is important, set the max members first
            this.maxLatitude = Math.max(this.minLatitude, latitude);
            this.maxLongitude = Math.max(this.minLongitude, longitude);
            this.minLatitude = Math.min(this.minLatitude, latitude);
            this.minLongitude = Math.min(this.minLongitude, longitude);
        } else {
            this.minLatitude = latitude;
            this.minLongitude = longitude;
            this.maxLatitude = Double.NaN;
            this.maxLongitude = Double.NaN;
        }
        return this;
    }

    /**
     * Sets this sector to the union of itself and an array of specified locations. If this sector is empty, it bounds
     * the specified locations. The array is understood to contain location of at least two coordinates organized as
     * (longitude, latitude, ...), where stride indicates the number of coordinates between longitude values.
     *
     * @param array  the array of locations to consider
     * @param count  the number of array elements to consider
     * @param stride the number of coordinates between the first coordinate of adjacent locations - must be at least 2
     *
     * @return This bounding box set to contain the specified array of locations.
     *
     * @throws IllegalArgumentException If the array is null or empty, if the count is less than 0, or if the stride is
     *                                  less than 2
     */
    public Sector union(float[] array, int count, int stride) {
        if (array == null || array.length < stride) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "union", "missingArray"));
        }

        if (count < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "union", "invalidCount"));
        }

        if (stride < 2) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "union", "invalidStride"));
        }

        double minLat = Double.isNaN(this.minLatitude) ? Double.MAX_VALUE : this.minLatitude;
        double maxLat = Double.isNaN(this.maxLatitude) ? -Double.MAX_VALUE : this.maxLatitude;
        double minLon = Double.isNaN(this.minLongitude) ? Double.MAX_VALUE : this.minLongitude;
        double maxLon = Double.isNaN(this.maxLongitude) ? -Double.MAX_VALUE : this.maxLongitude;

        for (int idx = 0; idx < count; idx += stride) {
            float lon = array[idx];
            float lat = array[idx + 1];

            if (maxLat < lat) {
                maxLat = lat;
            }
            if (minLat > lat) {
                minLat = lat;
            }
            if (maxLon < lon) {
                maxLon = lon;
            }
            if (minLon > lon) {
                minLon = lon;
            }
        }

        if (minLat < Double.MAX_VALUE) {
            this.minLatitude = minLat;
        }
        if (maxLat > -Double.MAX_VALUE) {
            this.maxLatitude = maxLat;
        }
        if (minLon < Double.MAX_VALUE) {
            this.minLongitude = minLon;
        }
        if (maxLon > -Double.MAX_VALUE) {
            this.maxLongitude = maxLon;
        }

        return this;
    }

    /**
     * Sets this sector to the union of itself and a specified sector. This has no effect if the specified sector is
     * empty. If this sector is empty, it is set to the specified sector.
     *
     * @param minLatitude    the minimum latitude of the sector to union with, in degrees
     * @param minLongitude   the minimum longitude of the sector to union with, in degrees
     * @param deltaLatitude  the width of the sector to union with, in degrees
     * @param deltaLongitude the height of the sector to union with, in degrees
     *
     * @return this sector, set to its union with the specified sector
     */
    public Sector union(double minLatitude, double minLongitude, double deltaLatitude, double deltaLongitude) {
        // Assumes normalized latitude and longitude: [-90, +90], [-180, +180]
        double maxLatitude = Location.clampLatitude(minLatitude + (deltaLatitude > 0 ? deltaLatitude : Double.NaN));
        double maxLongitude = Location.clampLongitude(minLongitude + (deltaLongitude > 0 ? deltaLongitude : Double.NaN));

        if ((minLatitude < maxLatitude) && (minLongitude < maxLongitude)) { // specified sector not empty
            if ((this.minLatitude < this.maxLatitude) && (this.minLongitude < this.maxLongitude)) { // this sector not empty
                if (this.minLatitude > minLatitude)
                    this.minLatitude = minLatitude;
                if (this.maxLatitude < maxLatitude)
                    this.maxLatitude = maxLatitude;
                if (this.minLongitude > minLongitude)
                    this.minLongitude = minLongitude;
                if (this.maxLongitude < maxLongitude)
                    this.maxLongitude = maxLongitude;
            } else {
                // this sector is empty, set to the specified sector
                this.minLatitude = minLatitude;
                this.maxLatitude = maxLatitude;
                this.minLongitude = minLongitude;
                this.maxLongitude = maxLongitude;
            }
        }

        return this;
    }

    /**
     * Sets this sector to the union of itself and a specified sector. This has no effect if the specified sector is
     * empty. If this sector is empty, it is set to the specified sector.
     *
     * @param sector the sector to union with
     *
     * @return this sector, set to its union with the specified sector
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public Sector union(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Sector", "union", "missingSector"));
        }

        // Assumes normalized angles: [-90, +90], [-180, +180]
        if ((sector.minLatitude < sector.maxLatitude) && (sector.minLongitude < sector.maxLongitude)) { // specified sector not empty
            if ((this.minLatitude < this.maxLatitude) && (this.minLongitude < this.maxLongitude)) { // this sector not empty
                if (this.minLatitude > sector.minLatitude)
                    this.minLatitude = sector.minLatitude;
                if (this.maxLatitude < sector.maxLatitude)
                    this.maxLatitude = sector.maxLatitude;
                if (this.minLongitude > sector.minLongitude)
                    this.minLongitude = sector.minLongitude;
                if (this.maxLongitude < sector.maxLongitude)
                    this.maxLongitude = sector.maxLongitude;
            } else {
                // this sector is empty, set to the specified sector
                this.minLatitude = sector.minLatitude;
                this.maxLatitude = sector.maxLatitude;
                this.minLongitude = sector.minLongitude;
                this.maxLongitude = sector.maxLongitude;
            }
        }

        return this;
    }
}
