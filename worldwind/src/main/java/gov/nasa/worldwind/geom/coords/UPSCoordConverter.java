/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/* RSC IDENTIFIER: UPS
 *
 *
 * ABSTRACT
 *
 *    This component provides conversions between geodetic (latitude
 *    and longitude) coordinates and Universal Polar Stereographic (UPS)
 *    projection (hemisphere, easting, and northing) coordinates.
 *
 *
 * ERROR HANDLING
 *
 *    This component checks parameters for valid values.  If an
 *    invalid value is found the error code is combined with the
 *    current error code using the bitwise or.  This combining allows
 *    multiple error codes to be returned. The possible error codes
 *    are:
 *
 *         UPS_NO_ERROR           : No errors occurred in function
 *         UPS_LAT_ERROR          : latitude outside of valid range
 *                                   (North Pole: 83.5 to 90,
 *                                    South Pole: -79.5 to -90)
 *         UPS_LON_ERROR          : longitude outside of valid range
 *                                   (-180 to 360 degrees)
 *         UPS_HEMISPHERE_ERROR   : Invalid hemisphere ('N' or 'S')
 *         UPS_EASTING_ERROR      : easting outside of valid range,
 *                                   (0 to 4,000,000m)
 *         UPS_NORTHING_ERROR     : northing outside of valid range,
 *                                   (0 to 4,000,000m)
 *         UPS_A_ERROR            : Semi-major axis less than or equal to zero
 *         UPS_INV_F_ERROR        : Inverse flattening outside of valid range
 *								  	               (250 to 350)
 *
 *
 * REUSE NOTES
 *
 *    UPS is intended for reuse by any application that performs a Universal
 *    Polar Stereographic (UPS) projection.
 *
 *
 * REFERENCES
 *
 *    Further information on UPS can be found in the Reuse Manual.
 *
 *    UPS originated from :  U.S. Army Topographic Engineering Center
 *                           Geospatial Information Division
 *                           7701 Telegraph Road
 *                           Alexandria, VA  22310-3864
 *
 *
 * LICENSES
 *
 *    None apply to this component.
 *
 *
 * RESTRICTIONS
 *
 *    UPS has no restrictions.
 *
 *
 * ENVIRONMENT
 *
 *    UPS was tested and certified in the following environments:
 *
 *    1. Solaris 2.5 with GCC version 2.8.1
 *    2. Windows 95 with MS Visual C++ version 6
 *
 *
 * MODIFICATIONS
 *
 *    Date              Description
 *    ----              -----------
 *    06-11-95          Original Code
 *    03-01-97          Original Code
 *
 *
 */

package gov.nasa.worldwind.geom.coords;

/**
 * Ported to Java from the NGA GeoTrans ups.c and ups.h code - Feb 12, 2007 4:52:59 PM
 *
 * @author Garrett Headley, Patrick Murris
 * @version $Id$
 */
public class UPSCoordConverter {

    public static final int UPS_NO_ERROR = 0x0000;
    private static final int UPS_LAT_ERROR = 0x0001;
    private static final int UPS_LON_ERROR = 0x0002;
    public static final int UPS_HEMISPHERE_ERROR = 0x0004;
    public static final int UPS_EASTING_ERROR = 0x0008;
    public static final int UPS_NORTHING_ERROR = 0x0010;

    private static final double PI = 3.14159265358979323;
    private static final double MAX_LAT = (PI * 90) / 180.0;             // 90 degrees in radians
    // Min and max latitude values accepted
    private static final double MIN_NORTH_LAT = 72 * PI / 180.0;       // 83.5
    private static final double MIN_SOUTH_LAT = -72 * PI / 180.0;      // -79.5

    private static final double MAX_ORIGIN_LAT = (81.114528 * PI) / 180.0;
    private static final double MIN_EAST_NORTH = 0;
    private static final double MAX_EAST_NORTH = 4000000;

    private double UPS_Origin_Latitude = MAX_ORIGIN_LAT;  /*set default = North hemisphere */
    private double UPS_Origin_Longitude = 0.0;

    /* Ellipsoid Parameters, default to WGS 84  */
    private double UPS_a = 6378137.0;          /* Semi-major axis of ellipsoid in meters   */
    private double UPS_f = 1 / 298.257223563;  /* Flattening of ellipsoid  */
    private double UPS_False_Easting = 2000000.0;
    private double UPS_False_Northing = 2000000.0;
    private double false_easting = 0.0;
    private double false_northing = 0.0;
    private double UPS_Easting = 0.0;
    private double UPS_Northing = 0.0;

    private double easting = 0.0;
    private double northing = 0.0;
    private Hemisphere hemisphere = Hemisphere.N;
    private double latitude = 0.0;
    private double longitude = 0.0;

    private final PolarCoordConverter polarConverter = new PolarCoordConverter();

    UPSCoordConverter(){}

    /**
     * The function convertGeodeticToUPS converts geodetic (latitude and longitude) coordinates to UPS (hemisphere,
     * easting, and northing) coordinates, according to the current ellipsoid parameters. If any errors occur, the error
     * code(s) are returned by the function, otherwide UPS_NO_ERROR is returned.
     *
     * @param latitude  latitude in radians
     * @param longitude longitude in radians
     *
     * @return error code
     */
    public long convertGeodeticToUPS(double latitude, double longitude) {
        if ((latitude < -MAX_LAT) || (latitude > MAX_LAT)) {   /* latitude out of range */
            return UPS_LAT_ERROR;
        }
        if ((latitude < 0) && (latitude > MIN_SOUTH_LAT))
            return UPS_LAT_ERROR;
        if ((latitude >= 0) && (latitude < MIN_NORTH_LAT))
            return UPS_LAT_ERROR;

        if ((longitude < -PI) || (longitude > (2 * PI))) {  /* slam out of range */
            return UPS_LON_ERROR;
        }

        if (latitude < 0) {
            UPS_Origin_Latitude = -MAX_ORIGIN_LAT;
            hemisphere = Hemisphere.S;
        } else {
            UPS_Origin_Latitude = MAX_ORIGIN_LAT;
            hemisphere = Hemisphere.N;
        }

        polarConverter.setPolarStereographicParameters(UPS_a, UPS_f,
            UPS_Origin_Latitude, UPS_Origin_Longitude,
            false_easting, false_northing);

        polarConverter.convertGeodeticToPolarStereographic(latitude, longitude);

        UPS_Easting = UPS_False_Easting + polarConverter.getEasting();
        UPS_Northing = UPS_False_Northing + polarConverter.getNorthing();
        if (Hemisphere.S.equals(hemisphere))
            UPS_Northing = UPS_False_Northing - polarConverter.getNorthing();

        easting = UPS_Easting;
        northing = UPS_Northing;

        return UPS_NO_ERROR;
    }

    /** @return easting/X in meters */
    public double getEasting() {
        return easting;
    }

    /** @return northing/Y in meters */
    public double getNorthing() {
        return northing;
    }

    /**
     * @return hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     */
    public Hemisphere getHemisphere() {
        return hemisphere;
    }

    /**
     * The function Convert_UPS_To_Geodetic converts UPS (hemisphere, easting, and northing) coordinates to geodetic
     * (latitude and longitude) coordinates according to the current ellipsoid parameters.  If any errors occur, the
     * error code(s) are returned by the function, otherwise UPS_NO_ERROR is returned.
     *
     * @param hemisphere hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     * @param easting    easting/X in meters
     * @param northing   northing/Y in meters
     *
     * @return error code
     */
    public long convertUPSToGeodetic(Hemisphere hemisphere, double easting, double northing) {
        long Error_Code = UPS_NO_ERROR;

        if (!Hemisphere.N.equals(hemisphere) && !Hemisphere.S.equals(hemisphere))
            Error_Code |= UPS_HEMISPHERE_ERROR;
        if ((easting < MIN_EAST_NORTH) || (easting > MAX_EAST_NORTH))
            Error_Code |= UPS_EASTING_ERROR;
        if ((northing < MIN_EAST_NORTH) || (northing > MAX_EAST_NORTH))
            Error_Code |= UPS_NORTHING_ERROR;

        if (Hemisphere.N.equals(hemisphere))
            UPS_Origin_Latitude = MAX_ORIGIN_LAT;
        if (Hemisphere.S.equals(hemisphere))
            UPS_Origin_Latitude = -MAX_ORIGIN_LAT;

        if (Error_Code == UPS_NO_ERROR) {   /*  no errors   */
            polarConverter.setPolarStereographicParameters(UPS_a,
                UPS_f,
                UPS_Origin_Latitude,
                UPS_Origin_Longitude,
                UPS_False_Easting,
                UPS_False_Northing);

            polarConverter.convertPolarStereographicToGeodetic(easting, northing);
            latitude = polarConverter.getLatitude();
            longitude = polarConverter.getLongitude();

            if ((latitude < 0) && (latitude > MIN_SOUTH_LAT))
                Error_Code |= UPS_LAT_ERROR;
            if ((latitude >= 0) && (latitude < MIN_NORTH_LAT))
                Error_Code |= UPS_LAT_ERROR;
        }
        return Error_Code;
    }

    /** @return latitude in radians. */
    public double getLatitude() {
        return latitude;
    }

    /** @return longitude in radians. */
    public double getLongitude() {
        return longitude;
    }
}


