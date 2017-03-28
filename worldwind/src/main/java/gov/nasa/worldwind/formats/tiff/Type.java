/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.util.Logger;

public enum Type {

    UBYTE,
    ASCII,
    USHORT,
    ULONG,
    RATIONAL,
    SBYTE,
    UNDEFINED,
    SSHORT,
    SLONG,
    SRATIONAL,
    FLOAT,
    DOUBLE;

    public static Type decode(int type) {
        switch (type) {
            case 1:
                return UBYTE;
            case 2:
                return ASCII;
            case 3:
                return USHORT;
            case 4:
                return ULONG;
            case 5:
                return RATIONAL;
            case 6:
                return SBYTE;
            case 7:
                return UNDEFINED;
            case 8:
                return SSHORT;
            case 9:
                return SLONG;
            case 10:
                return SRATIONAL;
            case 11:
                return FLOAT;
            case 12:
                return DOUBLE;
            default:
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Type", "decode", "invalid type"));
        }
    }

    public int getSizeInBytes() {
        switch (this) {
            case UBYTE:
                return 1;
            case ASCII:
                return 1;
            case USHORT:
                return 2;
            case ULONG:
                return 4;
            case RATIONAL:
                return 8;
            case SBYTE:
                return 1;
            case UNDEFINED:
                return 1;
            case SSHORT:
                return 2;
            case SLONG:
                return 4;
            case SRATIONAL:
                return 8;
            case FLOAT:
                return 4;
            case DOUBLE:
                return 8;
            default:
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Type", "getSizeInBytes", "invalid type"));
        }
    }

    public int getSpecificationTag() {
        switch (this) {
            case UBYTE:
                return 1;
            case ASCII:
                return 2;
            case USHORT:
                return 3;
            case ULONG:
                return 4;
            case RATIONAL:
                return 5;
            case SBYTE:
                return 6;
            case UNDEFINED:
                return 7;
            case SSHORT:
                return 8;
            case SLONG:
                return 9;
            case SRATIONAL:
                return 10;
            case FLOAT:
                return 11;
            case DOUBLE:
                return 12;
            default:
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Type", "getSizeInBytes", "invalid type"));
        }
    }

}
