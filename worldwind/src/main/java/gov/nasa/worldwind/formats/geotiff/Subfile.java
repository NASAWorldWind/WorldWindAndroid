/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.geotiff;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.util.Logger;

public class Subfile {

    public static final int UNSIGNED_INT = 1;

    public static final int TWOS_COMP_SIGNED_INT = 2;

    public static final int FLOATING_POINT = 3;

    public static final int UNDEFINED = 4;

    @IntDef({UNSIGNED_INT, TWOS_COMP_SIGNED_INT, FLOATING_POINT, UNDEFINED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SAMPLE_FORMAT {

    }

    /**
     * A {@link ByteBuffer} of the entire Tiff file of which this Subfile is a part of.
     */
    protected ByteBuffer buffer;

    /**
     * The GeoTiff absolute file offset position of this Subfile.
     */
    protected int offset;

    /**
     * The {@link Field} associated with this Subfile and provided in the Image File Directory (IFD).
     */
    protected Map<Integer, Field> fields = new HashMap<>();

    // Minimum Required Tags to support Bi-level and Gray-scale Tiffs

    // 254 - note this is a bit flag not a signed integer type
    protected int newSubfileType = 0;

    // 256
    protected int imageWidth;

    // 257
    protected int imageLength;

    // 258
    protected int[] bitsPerSample = {1};

    // 259
    protected int compression = 1;

    // 262
    protected int photometricInterpretation;

    // 277
    protected int samplesPerPixel = 1;

    // 282
    protected double xResolution = 0;

    // 283
    protected double yResolution = 0;

    // 284
    protected int planarConfiguration = 1;

    // 296
    protected int resolutionUnit = 2;

    // Strip & Tile Image Data

    // 273 & 324
    protected int[] offsets;

    // 279 & 325
    protected int[] byteCounts;

    // 278
    protected int rowsPerStrip = 0xFFFFFFFF;

    // 322
    protected int tileWidth = 0;

    // 323
    protected int tileLength = 0;

    // 339
    @SAMPLE_FORMAT
    protected int[] sampleFormat = {UNSIGNED_INT};

    public Subfile(ByteBuffer buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }

    public Map<Integer, Field> getFields() {
        return this.fields;
    }

    protected void populateDefinedFields() {
        Field field = this.fields.get(254);
        if (field != null) {
            this.newSubfileType = GeoTiff.readLimitedDWord(field.getDataBuffer());
        }

        field = this.fields.get(256);
        if (field != null) {
            if (field.type == ValueType.USHORT) {
                this.imageWidth = GeoTiff.readWord(field.getDataBuffer());
            } else if (field.type == ValueType.ULONG) {
                this.imageWidth = GeoTiff.readLimitedDWord(field.getDataBuffer());
            } else {
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Subfile", "populateDefinedFields", "invalid image width type"));
            }
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateDefinedFields", "invalid tiff format - image width missing"));
        }

        field = this.fields.get(257);
        if (field != null) {
            if (field.type == ValueType.USHORT) {
                this.imageLength = GeoTiff.readWord(field.getDataBuffer());
            } else if (field.type == ValueType.ULONG) {
                this.imageLength = GeoTiff.readLimitedDWord(field.getDataBuffer());
            } else {
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Subfile", "populateDefinedFields", "invalid image length type"));
            }
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateDefinedFields", "invalid tiff format - image length missing"));
        }

        field = this.fields.get(258);
        if (field != null) {
            this.bitsPerSample = new int[field.count];
            for (int i = 0; i < field.count; i++) {
                this.bitsPerSample[i] = GeoTiff.readWord(field.getDataBuffer());
            }
        }

        field = this.fields.get(259);
        if (field != null) {
            this.compression = GeoTiff.readWord(field.getDataBuffer());
            if (this.compression != 1) {
                throw new UnsupportedOperationException(
                    Logger.logMessage(Logger.ERROR, "Subfile", "populateDefineFields", "compressed images are not supported"));
            }
        }

        field = this.fields.get(262);
        if (field != null) {
            this.photometricInterpretation = GeoTiff.readWord(field.getDataBuffer());
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populatedDefinedFields", "photometricinterpretation missing"));
        }

        field = this.fields.get(277);
        if (field != null) {
            this.samplesPerPixel = GeoTiff.readWord(field.getDataBuffer());
        }

        field = this.fields.get(282);
        if (field != null) {
            this.xResolution = this.calculateRational(field.getDataBuffer());
        }

        field = this.fields.get(283);
        if (field != null) {
            this.yResolution = this.calculateRational(field.getDataBuffer());
        }

        field = this.fields.get(284);
        if (field != null) {
            this.planarConfiguration = GeoTiff.readWord(field.getDataBuffer());
            if (this.planarConfiguration != 1) {
                throw new UnsupportedOperationException(
                    Logger.logMessage(Logger.ERROR, "Subfile", "populateDefinedFields", "planar configurations other than 1 are not supported"));
            }
        }

        field = this.fields.get(296);
        if (field != null) {
            this.resolutionUnit = GeoTiff.readWord(field.getDataBuffer());
        }

        if (this.fields.containsKey(273)) {
            this.populateStripFields();
        } else if (this.fields.containsKey(324)) {
            this.populateTileFields();
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateDefinedFields", "no image offsets provided"));
        }

        field = this.fields.get(339);
        if (field != null) {
            this.sampleFormat = new int[field.count];
            for (int i = 0; i < field.count; i++) {
                this.sampleFormat[i] = GeoTiff.readWord(field.getDataBuffer());
            }
        }
    }

    public int getNewSubfileType() {
        return this.newSubfileType;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageLength() {
        return this.imageLength;
    }

    public int[] getBitsPerSample() {
        return this.bitsPerSample;
    }

    public int getCompression() {
        return this.compression;
    }

    public int getPhotometricInterpretation() {
        return this.photometricInterpretation;
    }

    public int getSamplesPerPixel() {
        return this.samplesPerPixel;
    }

    public double getXResolution() {
        return this.xResolution;
    }

    public double getYResolution() {
        return this.yResolution;
    }

    public int getPlanarConfiguration() {
        return this.planarConfiguration;
    }

    public int getResolutionUnit() {
        return this.resolutionUnit;
    }

    public int[] getOffsets() {
        return this.offsets;
    }

    public int[] getByteCounts() {
        return this.byteCounts;
    }

    public int getRowsPerStrip() {
        return this.rowsPerStrip;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileLength() {
        return this.tileLength;
    }

    public int[] getSampleFormat() {
        return this.sampleFormat;
    }

    protected void populateStripFields() {
        Field field = this.fields.get(273);

        if (field != null) {
            this.offsets = new int[field.count];
            ByteBuffer data = field.getDataBuffer();
            for (int i = 0; i < this.offsets.length; i++) {
                if (field.type == ValueType.USHORT) {
                    this.offsets[i] = GeoTiff.readWord(data);
                } else if (field.type == ValueType.ULONG) {
                    this.offsets[i] = GeoTiff.readLimitedDWord(data);
                } else {
                    throw new RuntimeException(
                        Logger.logMessage(Logger.ERROR, "Strip", "populateStripFields", "invalid offset type"));
                }
            }
        } else {
            throw new RuntimeException("invalid tiff format - offsets missing");
        }

        field = this.fields.get(278);
        if (field != null) {
            if (field.type == ValueType.USHORT) {
                this.rowsPerStrip = GeoTiff.readWord(field.getDataBuffer());
            } else if (field.type == ValueType.ULONG) {
                this.rowsPerStrip = GeoTiff.readLimitedDWord(field.getDataBuffer());
            } else {
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Strip", "populateStripFields", "invalid rowsperstrip type"));
            }
        }

        field = this.fields.get(279);
        if (field != null) {
            this.byteCounts = new int[field.count];
            ByteBuffer data = field.getDataBuffer();
            for (int i = 0; i < this.byteCounts.length; i++) {
                if (field.type == ValueType.USHORT) {
                    this.byteCounts[i] = GeoTiff.readWord(data);
                } else if (field.type == ValueType.ULONG) {
                    this.byteCounts[i] = GeoTiff.readLimitedDWord(data);
                } else {
                    throw new RuntimeException(
                        Logger.logMessage(Logger.ERROR, "Strip", "populateStripFields", "invalid byteCounts type"));
                }
            }
        } else {
            throw new RuntimeException("invalid tiff format - byteCounts missing");
        }
    }

    protected void populateTileFields() {
        Field field = this.fields.get(324);

        if (field != null) {
            this.offsets = new int[field.count];
            ByteBuffer data = field.getDataBuffer();
            for (int i = 0; i < this.offsets.length; i++) {
                if (field.type == ValueType.USHORT) {
                    this.offsets[i] = GeoTiff.readWord(data);
                } else if (field.type == ValueType.ULONG) {
                    this.offsets[i] = GeoTiff.readLimitedDWord(data);
                } else {
                    throw new RuntimeException(
                        Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "invalid offset type"));
                }
            }
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "missing offset"));
        }

        field = this.fields.get(325);
        if (field != null) {
            this.byteCounts = new int[field.count];
            ByteBuffer data = field.getDataBuffer();
            for (int i = 0; i < this.byteCounts.length; i++) {
                if (field.type == ValueType.USHORT) {
                    this.byteCounts[i] = GeoTiff.readWord(data);
                } else if (field.type == ValueType.ULONG) {
                    this.byteCounts[i] = GeoTiff.readLimitedDWord(data);
                } else {
                    throw new RuntimeException(
                        Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "invalid byteCounts type"));
                }
            }
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "invalid tiff format - byteCounts missing"));
        }

        field = this.fields.get(322);
        if (field != null) {
            if (field.type == ValueType.USHORT) {
                this.tileWidth = GeoTiff.readWord(field.getDataBuffer());
            } else if (field.type == ValueType.ULONG) {
                this.tileWidth = GeoTiff.readLimitedDWord(field.getDataBuffer());
            } else {
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "invalid tileWidth type"));
            }
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "missing tilewidth field"));
        }

        field = this.fields.get(323);
        if (field != null) {
            if (field.type == ValueType.USHORT) {
                this.tileLength = GeoTiff.readWord(field.getDataBuffer());
            } else if (field.type == ValueType.ULONG) {
                this.tileLength = GeoTiff.readLimitedDWord(field.getDataBuffer());
            } else {
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "invalid tileLength type"));
            }
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "populateTileFields", "missing tileLength field"));
        }
    }

    /**
     * Calculates the uncompressed data size. Should be used when preparing the ByteBuffer for write Tiff data in the
     * {@link Subfile#getData(ByteBuffer)} method.
     *
     * @return the size in bytes of the uncompressed data
     */
    public int getDataSize() {
        int bytes = 0;
        for (int i = 0; i < this.getSamplesPerPixel(); i++) {
            bytes += this.getImageLength() * this.getImageWidth() * this.getBitsPerSample()[i] / 8;
        }

        return bytes;
    }

    /**
     * Writes the uncompressed data from the Tiff data associated with the Subfile to the provided
     * ByteBuffer. The data copied to the provided buffer will use the original datas byte order and may override the
     * byte order specified by the provided buffer.
     *
     * @param result a ByteBuffer ready for the uncompressed Tiff data, should have a capacity of at least the return
     *               value of {@link Subfile#getDataSize()}
     *
     * @return the populated provided ByteBuffer
     */
    public ByteBuffer getData(ByteBuffer result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Subfile", "getUncompressedImage", "null buffer"));
        }

        int size = result.capacity() - result.position();
        if (size < this.getDataSize()) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "Subfile", "getUncompressedImage", "inadequate buffer size"));
        }

        // set the result ByteBuffer to our datas byte order
        result.order(this.buffer.order());

        // use the provided buffer to extract the image strips or tiles from the tiff buffer
        this.getOffsets(); // ensure the arrays have been initiated
        for (int i = 0; i < this.offsets.length; i++) {
            result.put(this.buffer.array(), this.offsets[i], this.byteCounts[i]);
        }

        // TODO handle compression

        return result;
    }

    protected double calculateRational(ByteBuffer buffer) {
        long numerator = GeoTiff.readDWord(buffer);
        long denominator = GeoTiff.readDWord(buffer);
        return numerator / denominator;
    }
}
