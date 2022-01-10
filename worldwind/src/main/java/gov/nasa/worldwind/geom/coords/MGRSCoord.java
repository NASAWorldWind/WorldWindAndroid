/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import androidx.annotation.NonNull;

/**
 * This class holds an immutable MGRS coordinate string along with
 * the corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 */

public class MGRSCoord {
    private final String MGRSString;
    private final double latitude;
    private final double longitude;

    /**
     * Create a WGS84 MGRS coordinate from a pair of latitude and longitude <code>double</code>
     * with the maximum precision of five digits (one meter).
     *
     * @param latitude the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(double latitude, double longitude) {
        return fromLatLon(latitude, longitude, 5);
    }

    /**
     * Create a MGRS coordinate from a pair of latitude and longitude <code>double</code>
     * with the given precision or number of digits (1 to 5).
     *
     * @param latitude the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     * @param precision the number of digits used for easting and northing (1 to 5).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(double latitude, double longitude, int precision) {
        final MGRSCoordConverter converter = new MGRSCoordConverter();
        long err = converter.convertGeodeticToMGRS(Math.toRadians(latitude), Math.toRadians(longitude), precision);

        if (err != MGRSCoordConverter.MGRS_NO_ERROR) {
            throw new IllegalArgumentException("MGRS Conversion Error");
        }

        return new MGRSCoord(latitude, longitude, converter.getMGRSString());
    }

    /**
     * Create a MGRS coordinate from a standard MGRS coordinate text string.
     * <p>
     * The string will be converted to uppercase and stripped of all spaces before being evaluated.
     * </p>
     * <p>Valid examples:<br>
     * 32TLP5626635418<br>
     * 32 T LP 56266 35418<br>
     * 11S KU 528 111<br>
     * </p>
     * @param MGRSString the MGRS coordinate text string.
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if the <code>MGRSString</code> is null or empty,
     * the <code>globe</code> is null, or the conversion to geodetic coordinates fails (invalid coordinate string).
     */
    public static MGRSCoord fromString(String MGRSString) {
        MGRSString = MGRSString.toUpperCase().replaceAll(" ", "");

        final MGRSCoordConverter converter = new MGRSCoordConverter();
        long err = converter.convertMGRSToGeodetic(MGRSString);

        if (err != MGRSCoordConverter.MGRS_NO_ERROR) {
            throw new IllegalArgumentException("MGRS Conversion Error");
        }

        return new MGRSCoord(Math.toDegrees(converter.getLatitude()), Math.toDegrees(converter.getLongitude()), MGRSString);
    }

    /**
     * Create an arbitrary MGRS coordinate from a pair of latitude-longitude <code>double</code>
     * and the corresponding MGRS coordinate string.
     *
     * @param latitude the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     * @param MGRSString the corresponding MGRS coordinate string.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the MGRSString is null or empty.
     */
    public MGRSCoord(double latitude, double longitude, String MGRSString) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.MGRSString = MGRSString;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @NonNull
    @Override
    public String toString() {
        return this.MGRSString;
    }

}
