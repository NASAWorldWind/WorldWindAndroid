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
