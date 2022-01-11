/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import androidx.annotation.NonNull;

/**
 * This immutable class holds a set of UTM coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */

public class UTMCoord {
    
    private final double latitude;
    private final double longitude;
    private final Hemisphere hemisphere;
    private final int zone;
    private final double easting;
    private final double northing;

    /**
     * Create a set of UTM coordinates from a pair of latitude and longitude for the given <code>Globe</code>.
     *
     * @param latitude  the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     *
     * @return the corresponding <code>UTMCoord</code>.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null, or the conversion to
     *                                  UTM coordinates fails.
     */
    public static UTMCoord fromLatLon(double latitude, double longitude) {
        final UTMCoordConverter converter = new UTMCoordConverter();
        long err = converter.convertGeodeticToUTM(Math.toRadians(latitude), Math.toRadians(longitude));

        if (err != UTMCoordConverter.UTM_NO_ERROR) {
            throw new IllegalArgumentException("UTM Conversion Error");
        }

        return new UTMCoord(latitude, longitude, converter.getZone(), converter.getHemisphere(),
            converter.getEasting(), converter.getNorthing());
    }

    /**
     * Create a set of UTM coordinates for the given <code>Globe</code>.
     *
     * @param zone       the UTM zone - 1 to 60.
     * @param hemisphere the hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @return the corresponding <code>UTMCoord</code>.
     *
     * @throws IllegalArgumentException if the conversion to UTM coordinates fails.
     */
    public static UTMCoord fromUTM(int zone, Hemisphere hemisphere, double easting, double northing) {
        final UTMCoordConverter converter = new UTMCoordConverter();
        long err = converter.convertUTMToGeodetic(zone, hemisphere, easting, northing);

        if (err != UTMCoordConverter.UTM_NO_ERROR) {
            throw new IllegalArgumentException("UTM Conversion Error");
        }

        return new UTMCoord(Math.toDegrees(converter.getLatitude()),
            Math.toDegrees(converter.getLongitude()),
            zone, hemisphere, easting, northing);
    }

    /**
     * Create an arbitrary set of UTM coordinates with the given values.
     *
     * @param latitude        the latitude <code>double</code>.
     * @param longitude       the longitude <code>double</code>.
     * @param zone            the UTM zone - 1 to 60.
     * @param hemisphere      the hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     * @param easting         the easting distance in meters
     * @param northing        the northing distance in meters.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null.
     */
    public UTMCoord(double latitude, double longitude, int zone, Hemisphere hemisphere, double easting, double northing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hemisphere = hemisphere;
        this.zone = zone;
        this.easting = easting;
        this.northing = northing;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public int getZone() {
        return this.zone;
    }

    public Hemisphere getHemisphere() {
        return this.hemisphere;
    }

    public double getEasting() {
        return this.easting;
    }

    public double getNorthing() {
        return this.northing;
    }

    @Override
    @NonNull
    public String toString() {
        return zone + " " + hemisphere + " " + easting + "E" + " " + northing + "N";
    }
}
