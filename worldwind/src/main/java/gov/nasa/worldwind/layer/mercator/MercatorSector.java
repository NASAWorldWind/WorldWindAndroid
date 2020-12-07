package gov.nasa.worldwind.layer.mercator;

import gov.nasa.worldwind.geom.Sector;

public class MercatorSector extends Sector {

    private final double minLatPercent, maxLatPercent;

    private MercatorSector(double minLatPercent, double maxLatPercent,
                           double minLongitude, double maxLongitude) {
        this.minLatPercent = minLatPercent;
        this.maxLatPercent = maxLatPercent;
        this.minLatitude = gudermannian(minLatPercent);
        this.maxLatitude = gudermannian(maxLatPercent);
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    public static MercatorSector fromDegrees(double minLatPercent, double maxLatPercent,
                                             double minLongitude, double maxLongitude) {
        return new MercatorSector(minLatPercent, maxLatPercent, minLongitude, maxLongitude);
    }

    static MercatorSector fromSector(Sector sector) {
        return new MercatorSector(gudermannianInverse(sector.minLatitude()),
                gudermannianInverse(sector.maxLatitude()),
                sector.minLongitude(), sector.maxLongitude());
    }

    static double gudermannianInverse(double latitude) {
        return Math.log(Math.tan(Math.PI / 4.0 + Math.toRadians(latitude) / 2.0)) / Math.PI;
    }

    private static double gudermannian(double percent) {
        return Math.toDegrees(Math.atan(Math.sinh(percent * Math.PI)));
    }

    double minLatPercent() {
        return minLatPercent;
    }

    double maxLatPercent()
    {
        return maxLatPercent;
    }

}