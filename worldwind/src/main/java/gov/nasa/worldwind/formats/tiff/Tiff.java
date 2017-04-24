/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.Logger;

public class Tiff {

    public static final int NEW_SUBFILE_TYPE_TAG = 254;

    public static final int IMAGE_WIDTH_TAG = 256;

    public static final int IMAGE_LENGTH_TAG = 257;

    public static final int BITS_PER_SAMPLE_TAG = 258;

    public static final int COMPRESSION_TAG = 259;

    public static final int PHOTOMETRIC_INTERPRETATION_TAG = 262;

    public static final int SAMPLES_PER_PIXEL_TAG = 277;

    public static final int X_RESOLUTION_TAG = 282;

    public static final int Y_RESOLUTION_TAG = 283;

    public static final int PLANAR_CONFIGURATION_TAG = 284;

    public static final int RESOLUTION_UNIT_TAG = 296;

    public static final int STRIP_OFFSETS_TAG = 273;

    public static final int STRIP_BYTE_COUNTS_TAG = 279;

    public static final int ROWS_PER_STRIP_TAG = 278;

    public static final int COMPRESSION_PREDICTOR_TAG = 317;

    public static final int TILE_OFFSETS_TAG = 324;

    public static final int TILE_BYTE_COUNTS_TAG = 325;

    public static final int TILE_WIDTH_TAG = 322;

    public static final int TILE_LENGTH_TAG = 323;

    public static final int SAMPLE_FORMAT_TAG = 339;

    /**
     * Tiff tags are the integer definitions of individual Image File Directories (IFDs) and set by the Tiff 6.0
     * specification. The tags defined here are a minimal set and not inclusive of the complete 6.0 specification.
     */
    @IntDef({NEW_SUBFILE_TYPE_TAG, IMAGE_WIDTH_TAG, IMAGE_LENGTH_TAG, BITS_PER_SAMPLE_TAG, COMPRESSION_TAG, PHOTOMETRIC_INTERPRETATION_TAG,
        SAMPLES_PER_PIXEL_TAG, X_RESOLUTION_TAG, Y_RESOLUTION_TAG, PLANAR_CONFIGURATION_TAG, RESOLUTION_UNIT_TAG, STRIP_OFFSETS_TAG,
        STRIP_BYTE_COUNTS_TAG, ROWS_PER_STRIP_TAG, COMPRESSION_PREDICTOR_TAG, TILE_OFFSETS_TAG, TILE_BYTE_COUNTS_TAG, TILE_WIDTH_TAG,
        TILE_LENGTH_TAG, SAMPLE_FORMAT_TAG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TIFF_TAGS {

    }

    public static final int UNSIGNED_INT = 1;

    public static final int TWOS_COMP_SIGNED_INT = 2;

    public static final int FLOATING_POINT = 3;

    public static final int UNDEFINED = 4;

    @IntDef({UNSIGNED_INT, TWOS_COMP_SIGNED_INT, FLOATING_POINT, UNDEFINED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SAMPLE_FORMAT {

    }

    /**
     * The {@link Subfile} contained within this Tiff.
     */
    protected List<Subfile> subfiles = new ArrayList<>();

    /**
     * {@link ByteBuffer} facilitating the view of the underlying Tiff data buffer.
     */
    protected ByteBuffer buffer;

    public Tiff(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tiff", "constructor", "missingBuffer"));
        }

        this.buffer = buffer;

        this.checkAndSetByteOrder();
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    protected void checkAndSetByteOrder() {
        // check byte order
        this.buffer.clear();
        char posOne = (char) this.buffer.get();
        char posTwo = (char) this.buffer.get();
        if (posOne == 'I' && posTwo == 'I') {
            this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else if (posOne == 'M' && posTwo == 'M') {
            this.buffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Tiff", "checkAndSetByteOrder", "Tiff byte order incompatible"));
        }

        // check the version
        int version = readWord(this.buffer);
        if (version != 42) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Tiff", "checkAndSetByteOrder", "Tiff version incompatible"));
        }
    }

    public List<Subfile> getSubfiles() {
        if (this.subfiles.isEmpty()) {
            this.buffer.position(4);
            int ifdOffset = readLimitedDWord(this.buffer);
            this.parseSubfiles(ifdOffset);
        }

        return this.subfiles;
    }

    protected void parseSubfiles(int offset) {
        this.buffer.position(offset);
        Subfile ifd = new Subfile(this, offset);
        this.subfiles.add(ifd);

        // check if there are more IFDs
        int nextIfdOffset = readLimitedDWord(this.buffer);
        if (nextIfdOffset != 0) {
            this.buffer.position(nextIfdOffset);
            this.parseSubfiles(nextIfdOffset);
        }
    }

    protected static int readWord(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }

    protected static long readDWord(ByteBuffer buffer) {
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    protected static int readLimitedDWord(ByteBuffer buffer) {
        long val = readDWord(buffer);
        if (val > Integer.MAX_VALUE) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Tiff", "readLimitedDWord", "value exceeds signed integer range"));
        } else {
            return (int) val;
        }
    }
}
