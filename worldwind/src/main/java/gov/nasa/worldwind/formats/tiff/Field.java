/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.formats.tiff;

import java.nio.ByteBuffer;

public class Field {

    /**
     * The {@link Subfile} which contains this entry.
     */
    protected Subfile subfile;

    /**
     * The byte offset from the beginning of the original file.
     */
    protected int offset;

    /**
     * The Tiff specification field tag.
     */
    protected int tag;

    /**
     * The Tiff specification field type.
     */
    protected Type type;

    /**
     * The Tiff specification length of the field in the units specified of {@link Field#type}.
     */
    protected int count;

    /**
     * Data offset of the field. The data starts on a word boundary, thus the dword should be even. The data for the
     * field may be anywhere in the file, even after the image data. If the data size is less or equal to 4 bytes
     * (determined by the field type and length), then this offset is not a offset but instead the data itself, to save
     * space. If the data size is less than 4 bytes, the data is stored left-justified within the 4 bytes of the offset
     * field.
     */
    protected int dataOffset;

    /**
     * The data associated with this field. This ByteBuffers 0 position should correspond to the offset position in the
     * complete buffer of the Tiff and the limit should correspond to the end of the data associated with this field.
     * Use the {@link Field#sliceBuffer(ByteBuffer)} method to populate once all other field properites have been
     * populated.
     */
    protected ByteBuffer data;

    /**
     * Slices the provided ByteBuffer, sets the byte order to the original, and sets the limit to the current position
     * plus the amount needed to view the data indicated by this field.
     *
     * @param original
     */
    protected void sliceBuffer(ByteBuffer original) {
        int originalLimit = original.limit();
        original.limit(this.dataOffset + this.type.getSizeInBytes() * this.count);
        this.data = original.slice().order(original.order());
        original.limit(originalLimit);
    }

    public ByteBuffer getDataBuffer() {
        this.data.rewind();
        return this.data;
    }
}
