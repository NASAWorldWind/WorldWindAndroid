/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

/*
 * Converter used to translate UTM coordinates to and from geodetic latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see UTMCoord, TMCoordConverter
 */

/**
 * Ported to Java from the NGA GeoTrans utm.c and utm.h
 *
 * @author Garrett Headley, Patrick Murris
 */
class UTMCoordConverter {

    public final static int UTM_NO_ERROR = 0x0000;
    public final static int UTM_LAT_ERROR = 0x0001;
    public final static int UTM_LON_ERROR = 0x0002;
    public final static int UTM_EASTING_ERROR = 0x0004;
    public final static int UTM_NORTHING_ERROR = 0x0008;
    public final static int UTM_ZONE_ERROR = 0x0010;
    public final static int UTM_HEMISPHERE_ERROR = 0x0020;
    public final static int UTM_ZONE_OVERRIDE_ERROR = 0x0040;
    public final static int UTM_TM_ERROR = 0x0200;

    private final static double PI = 3.14159265358979323;
    private final static double MIN_LAT = ((-82 * PI) / 180.0); /* -82 degrees in radians    */
    private final static double MAX_LAT = ((86 * PI) / 180.0);  /* 86 degrees in radians     */

    private final static int MIN_EASTING = 100000;
    private final static int MAX_EASTING = 900000;
    private final static int MIN_NORTHING = 0;
    private final static int MAX_NORTHING = 10000000;

    private double UTM_a = 6378137.0;         /* Semi-major axis of ellipsoid in meters  */
    private double UTM_f = 1 / 298.257223563; /* Flattening of ellipsoid                 */
    private long UTM_Override = 0;          /* Zone override flag                      */

    private double Easting;
    private double Northing;
    private Hemisphere hemisphere;
    private int Zone;
    private double Latitude;
    private double Longitude;
    private double Central_Meridian;

    UTMCoordConverter(){}

    /**
     * The function Convert_Geodetic_To_UTM converts geodetic (latitude and longitude) coordinates to UTM projection
     * (zone, hemisphere, easting and northing) coordinates according to the current ellipsoid and UTM zone override
     * parameters.  If any errors occur, the error code(s) are returned by the function, otherwise UTM_NO_ERROR is
     * returned.
     *
     * @param Latitude  Latitude in radians
     * @param Longitude Longitude in radians
     *
     * @return error code
     */
    public long convertGeodeticToUTM(double Latitude, double Longitude) {
        long Lat_Degrees;
        long Long_Degrees;
        long temp_zone;
        long Error_Code = UTM_NO_ERROR;
        double Origin_Latitude = 0;
        double False_Easting = 500000;
        double False_Northing = 0;
        double Scale = 0.9996;

        if ((Latitude < MIN_LAT) || (Latitude > MAX_LAT)) { /* Latitude out of range */
            Error_Code |= UTM_LAT_ERROR;
        }
        if ((Longitude < -PI) || (Longitude > (2 * PI))) { /* Longitude out of range */
            Error_Code |= UTM_LON_ERROR;
        }
        if (Error_Code == UTM_NO_ERROR) { /* no errors */
            if (Longitude < 0)
                Longitude += (2 * PI) + 1.0e-10;
            Lat_Degrees = (long) (Latitude * 180.0 / PI);
            Long_Degrees = (long) (Longitude * 180.0 / PI);

            if (Longitude < PI)
                temp_zone = (long) (31 + ((Longitude * 180.0 / PI) / 6.0));
            else
                temp_zone = (long) (((Longitude * 180.0 / PI) / 6.0) - 29);
            if (temp_zone > 60)
                temp_zone = 1;
            /* UTM special cases */
            if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > -1) && (Long_Degrees < 3))
                temp_zone = 31;
            if ((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > 2) && (Long_Degrees < 12))
                temp_zone = 32;
            if ((Lat_Degrees > 71) && (Long_Degrees > -1) && (Long_Degrees < 9))
                temp_zone = 31;
            if ((Lat_Degrees > 71) && (Long_Degrees > 8) && (Long_Degrees < 21))
                temp_zone = 33;
            if ((Lat_Degrees > 71) && (Long_Degrees > 20) && (Long_Degrees < 33))
                temp_zone = 35;
            if ((Lat_Degrees > 71) && (Long_Degrees > 32) && (Long_Degrees < 42))
                temp_zone = 37;

            if (UTM_Override != 0) {
                if ((temp_zone == 1) && (UTM_Override == 60))
                    temp_zone = UTM_Override;
                else if ((temp_zone == 60) && (UTM_Override == 1))
                    temp_zone = UTM_Override;
                else if (((temp_zone - 1) <= UTM_Override) && (UTM_Override <= (temp_zone + 1)))
                    temp_zone = UTM_Override;
                else
                    Error_Code = UTM_ZONE_OVERRIDE_ERROR;
            }
            if (Error_Code == UTM_NO_ERROR) {
                if (temp_zone >= 31)
                    Central_Meridian = (6 * temp_zone - 183) * PI / 180.0;
                else
                    Central_Meridian = (6 * temp_zone + 177) * PI / 180.0;
                Zone = (int) temp_zone;
                if (Latitude < 0) {
                    False_Northing = 10000000;
                    hemisphere = Hemisphere.S;
                }
                else
                    hemisphere = Hemisphere.N;

                try {
                    TMCoord TM = TMCoord.fromLatLon(Math.toDegrees(Latitude), Math.toDegrees(Longitude),
                        this.UTM_a, this.UTM_f, Math.toDegrees(Origin_Latitude),
                        Math.toDegrees(Central_Meridian), False_Easting, False_Northing, Scale);
                    Easting = TM.getEasting();
                    Northing = TM.getNorthing();

                    if ((Easting < MIN_EASTING) || (Easting > MAX_EASTING))
                        Error_Code = UTM_EASTING_ERROR;
                    if ((Northing < MIN_NORTHING) || (Northing > MAX_NORTHING))
                        Error_Code |= UTM_NORTHING_ERROR;
                } catch (Exception e) {
                    Error_Code = UTM_TM_ERROR;
                }
            }
        }
        return (Error_Code);
    }

    /** @return Easting (X) in meters */
    public double getEasting() {
        return Easting;
    }

    /** @return Northing (Y) in meters */
    public double getNorthing() {
        return Northing;
    }

    /**
     * @return The coordinate hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     */
    public Hemisphere getHemisphere() {
        return hemisphere;
    }

    /** @return UTM zone */
    public int getZone() {
        return Zone;
    }

    /**
     * The function Convert_UTM_To_Geodetic converts UTM projection (zone, hemisphere, easting and northing) coordinates
     * to geodetic(latitude and  longitude) coordinates, according to the current ellipsoid parameters.  If any errors
     * occur, the error code(s) are returned by the function, otherwise UTM_NO_ERROR is returned.
     *
     * @param zone       UTM zone.
     * @param hemisphere The coordinate hemisphere, either {@link Hemisphere#N} of {@link Hemisphere#S}.
     * @param easting    easting (X) in meters.
     * @param Northing   Northing (Y) in meters.
     *
     * @return error code.
     */
    public long convertUTMToGeodetic(long zone, Hemisphere hemisphere, double easting, double Northing) {
        // TODO: arg checking
        long Error_Code = UTM_NO_ERROR;
        double Origin_Latitude = 0;
        double False_Easting = 500000;
        double False_Northing = 0;
        double Scale = 0.9996;

        if ((zone < 1) || (zone > 60))
            Error_Code |= UTM_ZONE_ERROR;
        if (!hemisphere.equals(Hemisphere.S) && !hemisphere.equals(Hemisphere.N))
            Error_Code |= UTM_HEMISPHERE_ERROR;
        if ((Northing < MIN_NORTHING) || (Northing > MAX_NORTHING))
            Error_Code |= UTM_NORTHING_ERROR;

        if (Error_Code == UTM_NO_ERROR) { /* no errors */
            if (zone >= 31)
                Central_Meridian = ((6 * zone - 183) * PI / 180.0 /*+ 0.00000005*/);
            else
                Central_Meridian = ((6 * zone + 177) * PI / 180.0 /*+ 0.00000005*/);
            if (hemisphere.equals(Hemisphere.S))
                False_Northing = 10000000;
            try {
                TMCoord TM = TMCoord.fromTM(easting, Northing,
                    Math.toDegrees(Origin_Latitude), Math.toDegrees(Central_Meridian),
                    False_Easting, False_Northing, Scale);
                Latitude = Math.toRadians(TM.getLatitude());
                Longitude = Math.toRadians(TM.getLongitude());

                if ((Latitude < MIN_LAT) || (Latitude > MAX_LAT)) { /* Latitude out of range */
                    Error_Code |= UTM_NORTHING_ERROR;
                }
            } catch (Exception e) {
                Error_Code = UTM_TM_ERROR;
            }
        }
        return (Error_Code);
    }

    /** @return Latitude in radians. */
    public double getLatitude() {
        return Latitude;
    }

    /** @return Longitude in radians. */
    public double getLongitude() {
        return Longitude;
    }

}
