package gov.nasa.worldwind.geom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Angle {
    public static final String ANGLE_FORMAT_DD = "gov.nasa.worldwind.Geom.AngleDD";
    public static final String ANGLE_FORMAT_DM = "gov.nasa.worldwind.Geom.AngleDM";
    public static final String ANGLE_FORMAT_DMS = "gov.nasa.worldwind.Geom.AngleDMS";
    public static final Angle ZERO = fromDegrees(0.0D);
    public static final Angle POS90 = fromDegrees(90.0D);
    public static final Angle NEG90 = fromDegrees(-90.0D);
    public static final Angle POS180 = fromDegrees(180.0D);
    public static final Angle NEG180 = fromDegrees(-180.0D);
    public static final Angle POS360 = fromDegrees(360.0D);
    public static final Angle NEG360 = fromDegrees(-360.0D);
    public static final Angle MINUTE = fromDegrees(0.016666666666666666D);
    public static final Angle SECOND = fromDegrees(2.777777777777778E-4D);
    private static final double DEGREES_TO_RADIANS = 0.017453292519943295D;
    private static final double RADIANS_TO_DEGREES = 57.29577951308232D;
    private static final double PIOver2 = 1.5707963267948966D;
    public final double degrees;
    public final double radians;

    public static Angle fromDegrees(double degrees) {
        return new Angle(degrees, 0.017453292519943295D * degrees);
    }

    public static Angle fromRadians(double radians) {
        return new Angle(57.29577951308232D * radians, radians);
    }

    public static Angle fromDegreesLatitude(double degrees) {
        degrees = degrees < -90.0D ? -90.0D : (degrees > 90.0D ? 90.0D : degrees);
        double radians = 0.017453292519943295D * degrees;
        radians = radians < -1.5707963267948966D ? -1.5707963267948966D : (radians > 1.5707963267948966D ? 1.5707963267948966D : radians);
        return new Angle(degrees, radians);
    }

    public static Angle fromRadiansLatitude(double radians) {
        radians = radians < -1.5707963267948966D ? -1.5707963267948966D : (radians > 1.5707963267948966D ? 1.5707963267948966D : radians);
        double degrees = 57.29577951308232D * radians;
        degrees = degrees < -90.0D ? -90.0D : (degrees > 90.0D ? 90.0D : degrees);
        return new Angle(degrees, radians);
    }

    public static Angle fromDegreesLongitude(double degrees) {
        degrees = degrees < -180.0D ? -180.0D : (degrees > 180.0D ? 180.0D : degrees);
        double radians = 0.017453292519943295D * degrees;
        radians = radians < -3.141592653589793D ? -3.141592653589793D : (radians > 3.141592653589793D ? 3.141592653589793D : radians);
        return new Angle(degrees, radians);
    }

    public static Angle fromRadiansLongitude(double radians) {
        radians = radians < -3.141592653589793D ? -3.141592653589793D : (radians > 3.141592653589793D ? 3.141592653589793D : radians);
        double degrees = 57.29577951308232D * radians;
        degrees = degrees < -180.0D ? -180.0D : (degrees > 180.0D ? 180.0D : degrees);
        return new Angle(degrees, radians);
    }

    public static Angle fromXY(double x, double y) {
        double radians = Math.atan2(y, x);
        return new Angle(57.29577951308232D * radians, radians);
    }

    public static Angle fromDMS(int degrees, int minutes, int seconds) {
        String message;
        if (degrees < 0) {
        } else if (minutes >= 0 && minutes < 60) {
            if (seconds >= 0 && seconds < 60) {
                return fromDegrees((double)degrees + (double)minutes / 60.0D + (double)seconds / 3600.0D);
            } else {
            }
        } else {
        }
        return null;
    }

    public static Angle fromDMdS(int degrees, double minutes) {
        return fromDegrees((double)degrees + minutes / 60.0D);
    }

    public static Angle fromDMS(String dmsString) {
        String regex;
        regex = "([-|\\+]?\\d{1,3}[d|D|°|\\s](\\s*\\d{1,2}['|’|\\s])?(\\s*\\d{1,2}[\"|”|\\s])?\\s*([N|n|S|s|E|e|W|w])?\\s?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dmsString + " ");
        dmsString = dmsString.replaceAll("[D|d|°|'|’|\"|”]", " ");
        dmsString = dmsString.replaceAll("\\s+", " ");
        dmsString = dmsString.trim();
        int sign = 1;
        char suffix = dmsString.toUpperCase().charAt(dmsString.length() - 1);
        char prefix = dmsString.charAt(0);
        if (!Character.isDigit(suffix)) {
            sign = suffix != 'S' && suffix != 'W' ? 1 : -1;
            dmsString = dmsString.substring(0, dmsString.length() - 1);
            dmsString = dmsString.trim();
            if (!Character.isDigit(prefix)) {
                dmsString = dmsString.substring(1, dmsString.length());
                dmsString = dmsString.trim();
            }
        } else if (!Character.isDigit(prefix)) {
            sign *= prefix == '-' ? -1 : 1;
            dmsString = dmsString.substring(1, dmsString.length());
        }

        String[] DMS = dmsString.split(" ");
        int d = Integer.parseInt(DMS[0]);
        int m = DMS.length > 1 ? Integer.parseInt(DMS[1]) : 0;
        int s = DMS.length > 2 ? Integer.parseInt(DMS[2]) : 0;
        return fromDMS(d, m, s).multiply((double)sign);
    }

    public Angle(Angle angle) {
        this.degrees = angle.degrees;
        this.radians = angle.radians;
    }

    private Angle(double degrees, double radians) {
        this.degrees = degrees;
        this.radians = radians;
    }

    public final double getDegrees() {
        return this.degrees;
    }

    public final double getRadians() {
        return this.radians;
    }

    public final Angle add(Angle angle) {
            return fromDegrees(this.degrees + angle.degrees);
    }

    public final Angle subtract(Angle angle) {
            return fromDegrees(this.degrees - angle.degrees);
    }

    public final Angle multiply(double multiplier) {
        return fromDegrees(this.degrees * multiplier);
    }

    public final double divide(Angle angle) {
        return this.degrees / angle.degrees;
    }

    public final Angle addDegrees(double degrees) {
        return fromDegrees(this.degrees + degrees);
    }

    public final Angle subtractDegrees(double degrees) {
        return fromDegrees(this.degrees - degrees);
    }

    public final Angle divide(double divisor) {
        return fromDegrees(this.degrees / divisor);
    }

    public final Angle addRadians(double radians) {
        return fromRadians(this.radians + radians);
    }

    public final Angle subtractRadians(double radians) {
        return fromRadians(this.radians - radians);
    }

    public Angle angularDistanceTo(Angle angle) {
            double differenceDegrees = angle.subtract(this).degrees;
            if (differenceDegrees < -180.0D) {
                differenceDegrees += 360.0D;
            } else if (differenceDegrees > 180.0D) {
                differenceDegrees -= 360.0D;
            }

            double absAngle = Math.abs(differenceDegrees);
            return fromDegrees(absAngle);
    }

    public final double sin() {
        return Math.sin(this.radians);
    }

    public final double sinHalfAngle() {
        return Math.sin(0.5D * this.radians);
    }

    public static Angle asin(double sine) {
        return fromRadians(Math.asin(sine));
    }

    public static double arctanh(double radians) {
        return 0.5D * Math.log((1.0D + radians) / (1.0D - radians));
    }

    public final double cos() {
        return Math.cos(this.radians);
    }

    public final double cosHalfAngle() {
        return Math.cos(0.5D * this.radians);
    }

    public static Angle acos(double cosine) {
        return fromRadians(Math.acos(cosine));
    }

    public final double tanHalfAngle() {
        return Math.tan(0.5D * this.radians);
    }

    public static Angle atan(double tan) {
        return fromRadians(Math.atan(tan));
    }

    public static Angle midAngle(Angle a1, Angle a2) {
            return fromDegrees(0.5D * (a1.degrees + a2.degrees));
    }

    public static Angle average(Angle a, Angle b) {
            return fromDegrees(0.5D * (a.degrees + b.degrees));
    }

    public static Angle average(Angle a, Angle b, Angle c) {
            return fromDegrees((a.degrees + b.degrees + c.degrees) / 3.0D);
    }

    public static Angle clamp(Angle value, Angle min, Angle max) {
            return value.degrees < min.degrees ? min : (value.degrees > max.degrees ? max : value);
    }

//    public static Angle mix(double amount, Angle value1, Angle value2) {
//        if (value1 != null && value2 != null) {
//            if (amount < 0.0D) {
//                return value1;
//            } else if (amount > 1.0D) {
//                return value2;
//            } else {
//                Quaternion quat = Quaternion.slerp(amount, Quaternion.fromAxisAngle(value1, Vec4.UNIT_X), Quaternion.fromAxisAngle(value2, Vec4.UNIT_X));
//                Angle angle = quat.getRotationX();
//                return Double.isNaN(angle.degrees) ? null : angle;
//            }
//        }
//    }

//    public final int compareTo(Angle angle) {
//        if (angle == null) {
//            String msg = Logging.getMessage("nullValue.AngleIsNull");
//            Logging.logger().severe(msg);
//            throw new IllegalArgumentException(msg);
//        } else if (this.degrees < angle.degrees) {
//            return -1;
//        } else {
//            return this.degrees > angle.degrees ? 1 : 0;
//        }
//    }

    public static double normalizedDegrees(double degrees) {
        double a = degrees % 360.0D;
        return a > 180.0D ? a - 360.0D : (a < -180.0D ? 360.0D + a : a);
    }

    public static double normalizedDegreesLatitude(double degrees) {
        double lat = degrees % 180.0D;
        return lat > 90.0D ? 180.0D - lat : (lat < -90.0D ? -180.0D - lat : lat);
    }

    public static double normalizedDegreesLongitude(double degrees) {
        double lon = degrees % 360.0D;
        return lon > 180.0D ? lon - 360.0D : (lon < -180.0D ? 360.0D + lon : lon);
    }

    public static Angle normalizedAngle(Angle unnormalizedAngle) {
            return fromDegrees(normalizedDegrees(unnormalizedAngle.degrees));
    }

    public static Angle normalizedLatitude(Angle unnormalizedAngle) {
            return fromDegrees(normalizedDegreesLatitude(unnormalizedAngle.degrees));
    }

    public static Angle normalizedLongitude(Angle unnormalizedAngle) {
        return fromDegrees(normalizedDegreesLongitude(unnormalizedAngle.degrees));
    }

    public Angle normalize() {
        return normalizedAngle(this);
    }

    public Angle normalizedLatitude() {
        return normalizedLatitude(this);
    }

    public Angle normalizedLongitude() {
        return normalizedLongitude(this);
    }

    public static boolean crossesLongitudeBoundary(Angle angleA, Angle angleB) {
            return Math.signum(angleA.degrees) != Math.signum(angleB.degrees) && Math.abs(angleA.degrees - angleB.degrees) > 180.0D;
    }

    public static boolean isValidLatitude(double value) {
        return value >= -90.0D && value <= 90.0D;
    }

    public static boolean isValidLongitude(double value) {
        return value >= -180.0D && value <= 180.0D;
    }

    public static Angle max(Angle a, Angle b) {
        return a.degrees >= b.degrees ? a : b;
    }

    public static Angle min(Angle a, Angle b) {
        return a.degrees <= b.degrees ? a : b;
    }

    public final String toString() {
        return Double.toString(this.degrees) + '°';
    }

    public final String toDecimalDegreesString(int digits) {
        return String.format("%." + digits + "f°", this.degrees);
    }

    public final String toDMSString() {
        double temp = this.degrees;
        int sign = (int)Math.signum(temp);
        temp *= (double)sign;
        int d = (int)Math.floor(temp);
        temp = (temp - (double)d) * 60.0D;
        int m = (int)Math.floor(temp);
        temp = (temp - (double)m) * 60.0D;
        int s = (int)Math.round(temp);
        if (s == 60) {
            ++m;
            s = 0;
        }

        if (m == 60) {
            ++d;
            m = 0;
        }

        return (sign == -1 ? "-" : "") + d + '°' + ' ' + m + '’' + ' ' + s + '”';
    }

    public final String toDMString() {
        double temp = this.degrees;
        int sign = (int)Math.signum(temp);
        temp *= (double)sign;
        int d = (int)Math.floor(temp);
        temp = (temp - (double)d) * 60.0D;
        int m = (int)Math.floor(temp);
        temp = (temp - (double)m) * 60.0D;
        int s = (int)Math.round(temp);
        if (s == 60) {
            ++m;
            s = 0;
        }

        if (m == 60) {
            ++d;
            m = 0;
        }

        double mf = s == 0 ? (double)m : (double)m + (double)s / 60.0D;
        return (sign == -1 ? "-" : "") + d + '°' + ' ' + String.format("%5.2f", mf) + '’';
    }

    public final String toFormattedDMSString() {
        double temp = this.degrees;
        int sign = (int)Math.signum(temp);
        temp *= (double)sign;
        int d = (int)Math.floor(temp);
        temp = (temp - (double)d) * 60.0D;
        int m = (int)Math.floor(temp);
        temp = (temp - (double)m) * 60.0D;
        double s = Math.rint(temp * 100.0D) / 100.0D;
        if (s == 60.0D) {
            ++m;
            s = 0.0D;
        }

        if (m == 60) {
            ++d;
            m = 0;
        }

        return String.format("%4d° %2d’ %5.2f”", sign * d, m, s);
    }

    public final double[] toDMS() {
        double temp = this.degrees;
        int sign = (int)Math.signum(temp);
        temp *= (double)sign;
        int d = (int)Math.floor(temp);
        temp = (temp - (double)d) * 60.0D;
        int m = (int)Math.floor(temp);
        temp = (temp - (double)m) * 60.0D;
        double s = Math.rint(temp * 100.0D) / 100.0D;
        if (s == 60.0D) {
            ++m;
            s = 0.0D;
        }

        if (m == 60) {
            ++d;
            m = 0;
        }

        return new double[]{(double)(sign * d), (double)m, s};
    }

    public long getSizeInBytes() {
        return 8L;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Angle angle = (Angle)o;
            return angle.degrees == this.degrees;
        } else {
            return false;
        }
    }

    public int hashCode() {
        long temp = this.degrees != 0.0D ? Double.doubleToLongBits(this.degrees) : 0L;
        return (int)(temp ^ temp >>> 32);
    }
}
