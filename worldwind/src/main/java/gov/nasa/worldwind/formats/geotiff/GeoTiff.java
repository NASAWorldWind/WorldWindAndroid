/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.geotiff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.Logger;

public class GeoTiff {

    /**
     * The {@link Subfile} contained within this Tiff.
     */
    protected List<Subfile> subfiles = new ArrayList<>();

    /**
     * {@link ByteBuffer} facilitating the view of the underlying Tiff data buffer.
     */
    protected ByteBuffer buffer;

    public GeoTiff(ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GeoTiff", "constructor", "missingBuffer"));
        }

        this.buffer = buffer;

        this.checkAndSetByteOrder();
    }

    protected void checkAndSetByteOrder() {
        // Check Endianess
        this.buffer.clear();
        char posOne = (char) this.buffer.get();
        char posTwo = (char) this.buffer.get();
        if (posOne == 'I' && posTwo == 'I') {
            this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else if (posOne == 'M' && posTwo == 'M') {
            this.buffer.order(ByteOrder.BIG_ENDIAN);
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "GeoTiff", "checkAndSetByteOrder", "noncompliantByteOrder"));
        }

        // Check the version
        int version = this.readWord();
        if (version != 42) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "GeoTiff", "checkAndSetByteOrder", "noncompliantVersion"));
        }
    }

    public void parseFile() {
        this.buffer.position(4);
        this.parseIfds(this.readLimitedDWord());
    }

    public List<Subfile> getSubfiles() {
        if (this.subfiles.isEmpty()) {
            this.parseFile();
        }

        return this.subfiles;
    }

    protected void parseIfds(int offset) {
        Subfile ifd = new Subfile(this.buffer, offset);
        this.buffer.position(offset);
        this.parseIfdFields(ifd);
        this.subfiles.add(ifd);

        // check if there are more IFDs
        int nextIfdOffset = this.readLimitedDWord();
        if (nextIfdOffset != 0) {
            this.buffer.position(nextIfdOffset);
            this.parseIfds(nextIfdOffset);
        }
    }

    protected void parseIfdFields(Subfile ifd) {
        int entries = this.readWord();

        for (int i = 0; i < entries; i++) {
            Field field = new Field();
            field.subfile = ifd;
            field.offset = this.buffer.position();
            field.tag = this.readWord();
            field.type = ValueType.decode(this.readWord());
            field.count = this.readLimitedDWord();

            // Check if the data is available in the last four bytes of the field entry or if we need to read the pointer
            int size = field.count * field.type.getSizeInBytes();

            if (size > 4) {
                field.dataOffset = this.readLimitedDWord();
            } else {
                field.dataOffset = this.buffer.position();
            }
            this.buffer.position(field.dataOffset);
            field.sliceBuffer(this.buffer);
            this.buffer.position(field.offset + 12); // move the buffer position to the end of the field

            ifd.fields.put(field.tag, field);
        }
    }

    protected int readWord() {
        return readWord(this.buffer);
    }

    protected int readLimitedDWord() {
        return readLimitedDWord(this.buffer);
    }

    public static int readWord(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }

    public static long readDWord(ByteBuffer buffer) {
        return buffer.getInt() & 0xFFFFFFFFL;
    }

    public static int readLimitedDWord(ByteBuffer buffer) {
        long val = readDWord(buffer);
        if (val > Integer.MAX_VALUE) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "GeoTiff", "readLimitedDWord", "value exceeds signed integer range"));
        } else {
            return (int) val;
        }
    }
}
