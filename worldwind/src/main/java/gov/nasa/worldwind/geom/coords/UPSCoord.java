/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import android.support.annotation.NonNull;

/**
 * This immutable class holds a set of UPS coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */

public class UPSCoord {
    private final double latitude;
    private final double longitude;
    private final Hemisphere hemisphere;
    private final double easting;
    private final double northing;

    /**
     * Create a set of UPS coordinates from a pair of latitude and longitude for the given <code>Globe</code>.
     *
     * @param latitude  the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     *
     * @return the corresponding <code>UPSCoord</code>.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null, or the conversion to
     *                                  UPS coordinates fails.
     */
    public static UPSCoord fromLatLon(double latitude, double longitude) {
        final UPSCoordConverter converter = new UPSCoordConverter();
        long err = converter.convertGeodeticToUPS(Math.toRadians(latitude), Math.toRadians(longitude));

        if (err != UPSCoordConverter.UPS_NO_ERROR) {
            throw new IllegalArgumentException("UPS Conversion Error");
        }

        return new UPSCoord(latitude, longitude, converter.getHemisphere(),
            converter.getEasting(), converter.getNorthing());
    }

    /**
     * Create a set of UPS coordinates for the given <code>Globe</code>.
     *
     * @param hemisphere the hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @return the corresponding <code>UPSCoord</code>.
     *
     * @throws IllegalArgumentException if the conversion to UPS coordinates fails.
     */
    public static UPSCoord fromUPS(Hemisphere hemisphere, double easting, double northing) {
        final UPSCoordConverter converter = new UPSCoordConverter();
        long err = converter.convertUPSToGeodetic(hemisphere, easting, northing);

        if (err != UTMCoordConverter.UTM_NO_ERROR) {
            throw new IllegalArgumentException("UTM Conversion Error");
        }

        return new UPSCoord(Math.toDegrees(converter.getLatitude()),
            Math.toDegrees(converter.getLongitude()),
            hemisphere, easting, northing);
    }

    /**
     * Create an arbitrary set of UPS coordinates with the given values.
     *
     * @param latitude   the latitude <code>double</code>.
     * @param longitude  the longitude <code>double</code>.
     * @param hemisphere the hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @throws IllegalArgumentException if <code>latitude</code>, <code>longitude</code>, or <code>hemisphere</code> is
     *                                  null.
     */
    public UPSCoord(double latitude, double longitude, Hemisphere hemisphere, double easting, double northing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hemisphere = hemisphere;
        this.easting = easting;
        this.northing = northing;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
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
        return hemisphere + " " + easting + "E" + " " + northing + "N";
    }
}
