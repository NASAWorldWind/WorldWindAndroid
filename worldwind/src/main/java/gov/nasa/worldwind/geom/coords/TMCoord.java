/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

/**
 * This class holds a set of Transverse Mercator coordinates along with the
 * corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see TMCoordConverter
 */
public class TMCoord {

    private final double latitude;
    private final double longitude;
    private final double easting;
    private final double northing;

    /**
     * Create a set of Transverse Mercator coordinates from a pair of latitude and longitude,
     * for the given <code>Globe</code> and projection parameters.
     *
     * @param latitude the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     * @param a semi-major ellipsoid radius. If this and argument f are non-null and globe is null, will use the specfied a and f.
     * @param f ellipsoid flattening. If this and argument a are non-null and globe is null, will use the specfied a and f.
     * @param originLatitude the origin latitude <code>double</code>.
     * @param centralMeridian the central meridian longitude <code>double</code>.
     * @param falseEasting easting value at the center of the projection in meters.
     * @param falseNorthing northing value at the center of the projection in meters.
     * @param scale scaling factor.
     * @return the corresponding <code>TMCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to TM coordinates fails. If the globe is null conversion will default
     * to using WGS84.
     */
    public static TMCoord fromLatLon(double latitude, double longitude, Double a, Double f,
                                     double originLatitude, double centralMeridian,
                                     double falseEasting, double falseNorthing,
                                     double scale) {

        final TMCoordConverter converter = new TMCoordConverter();
        if (a == null || f == null) {
            a = converter.getA();
            f = converter.getF();
        }
        long err = converter.setTransverseMercatorParameters(a, f, Math.toRadians(originLatitude), Math.toRadians(centralMeridian),
                falseEasting, falseNorthing, scale);
        if (err == TMCoordConverter.TRANMERC_NO_ERROR)
            err = converter.convertGeodeticToTransverseMercator(Math.toRadians(latitude), Math.toRadians(longitude));

        if (err != TMCoordConverter.TRANMERC_NO_ERROR && err != TMCoordConverter.TRANMERC_LON_WARNING) {
            throw new IllegalArgumentException("TM Conversion Error");
        }

        return new TMCoord(latitude, longitude, converter.getEasting(), converter.getNorthing(),
                originLatitude, centralMeridian);
    }

    /**
     * Create a set of Transverse Mercator coordinates for the given <code>Globe</code>,
     * easting, northing and projection parameters.
     *
     * @param easting the easting distance value in meters.
     * @param northing the northing distance value in meters.
     * @param originLatitude the origin latitude <code>double</code>.
     * @param centralMeridian the central meridian longitude <code>double</code>.
     * @param falseEasting easting value at the center of the projection in meters.
     * @param falseNorthing northing value at the center of the projection in meters.
     * @param scale scaling factor.
     * @return the corresponding <code>TMCoord</code>.
     * @throws IllegalArgumentException if <code>originLatitude</code> or <code>centralMeridian</code>
     * is null, or the conversion to geodetic coordinates fails. If the globe is null conversion will default
     * to using WGS84.
     */
    public static TMCoord fromTM(double easting, double northing,
                                 double originLatitude, double centralMeridian,
                                 double falseEasting, double falseNorthing,
                                 double scale) {

        final TMCoordConverter converter = new TMCoordConverter();

        double a = converter.getA();
        double f = converter.getF();
        long err = converter.setTransverseMercatorParameters(a, f, Math.toRadians(originLatitude), Math.toRadians(centralMeridian),
                falseEasting, falseNorthing, scale);
        if (err == TMCoordConverter.TRANMERC_NO_ERROR)
            err = converter.convertTransverseMercatorToGeodetic(easting, northing);

        if (err != TMCoordConverter.TRANMERC_NO_ERROR && err != TMCoordConverter.TRANMERC_LON_WARNING) {
            throw new IllegalArgumentException("TM Conversion Error");
        }

        return new TMCoord(Math.toDegrees(converter.getLatitude()), Math.toDegrees(converter.getLongitude()),
                easting, northing, originLatitude, centralMeridian);
    }

    /**
     * Create an arbitrary set of Transverse Mercator coordinates with the given values.
     *
     * @param latitude the latitude <code>double</code>.
     * @param longitude the longitude <code>double</code>.
     * @param easting the easting distance value in meters.
     * @param northing the northing distance value in meters.
     * @param originLatitude the origin latitude <code>double</code>.
     * @param centralMeridian the central meridian longitude <code>double</code>.
     * @throws IllegalArgumentException if <code>latitude</code>, <code>longitude</code>, <code>originLatitude</code>
     * or <code>centralMeridian</code> is null.
     */
    public TMCoord(double latitude, double longitude, double easting, double northing,
                   double originLatitude, double centralMeridian) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.easting = easting;
        this.northing = northing;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getEasting() {
        return this.easting;
    }

    public double getNorthing() {
        return this.northing;
    }

}
