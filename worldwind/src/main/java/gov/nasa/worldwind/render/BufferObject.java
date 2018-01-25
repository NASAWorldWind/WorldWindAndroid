/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;
import android.util.SparseArray;

import java.nio.Buffer;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Range;
import gov.nasa.worldwind.util.Logger;

public class BufferObject implements RenderResource {

    protected int[] bufferId = new int[1];

    protected int bufferTarget;

    protected int bufferLength;

    protected int bufferByteCount;

    protected Buffer buffer;

    protected SparseArray<Range> bufferRanges;

    public BufferObject(int target, int size, Buffer buffer) {
        this.bufferTarget = target;
        this.bufferLength = (buffer != null) ? buffer.remaining() : 0;
        this.bufferByteCount = size;
        this.buffer = buffer;
    }

    public int getBufferTarget() {
        return this.bufferTarget;
    }

    public int getBufferLength() {
        return this.bufferLength;
    }

    public int getBufferByteCount() {
        return this.bufferByteCount;
    }

    public Range getBufferRange(int key) {
        return (this.bufferRanges != null) ? this.bufferRanges.get(key) : null;
    }

    public void putBufferRange(int key, Range range) {
        if (this.bufferRanges == null) {
            this.bufferRanges = new SparseArray<>();
        }

        this.bufferRanges.put(key, range);
    }

    public Range removeBufferRange(int key) {
        if (this.bufferRanges == null) {
            return null;
        }

        Range oldRange = this.bufferRanges.get(key);
        this.bufferRanges.delete(key);

        return oldRange;
    }

    public boolean hasBufferRange(int key) {
        return (this.bufferRanges != null) && this.bufferRanges.indexOfKey(key) >= 0;
    }

    @Override
    public void release(DrawContext dc) {
        this.deleteBufferObject(dc);
        this.buffer = null; // buffer can be non-null if the object has not been bound
    }

    public boolean bindBuffer(DrawContext dc) {
        if (this.buffer != null) {
            this.loadBuffer(dc);
            this.buffer = null;
        }

        if (this.bufferId[0] != 0) {
            dc.bindBuffer(this.bufferTarget, this.bufferId[0]);
        }

        return this.bufferId[0] != 0;
    }

    protected void loadBuffer(DrawContext dc) {
        int currentBuffer = dc.currentBuffer(this.bufferTarget);

        try {

            // Create the OpenGL buffer object.
            if (this.bufferId[0] == 0) {
                this.createBufferObject(dc);
            }

            // Make the OpenGL buffer object bound to the specified target.
            dc.bindBuffer(this.bufferTarget, this.bufferId[0]);
            // Load the current NIO buffer as the OpenGL buffer object's data.
            this.loadBufferObjectData(dc);

        } catch (Exception e) {

            // The NIO buffer could not be used as buffer data for an OpenGL buffer object. Delete the buffer object
            // to ensure that calls to bindBuffer fail.
            this.deleteBufferObject(dc);
            Logger.logMessage(Logger.ERROR, "BufferObject", "loadBuffer", "Exception attempting to load buffer data", e);

        } finally {

            // Restore the current OpenGL buffer object binding.
            dc.bindBuffer(this.bufferTarget, currentBuffer);
        }
    }

    protected void createBufferObject(DrawContext dc) {
        GLES20.glGenBuffers(1, this.bufferId, 0);
    }

    protected void deleteBufferObject(DrawContext dc) {
        if (this.bufferId[0] != 0) {
            GLES20.glDeleteBuffers(1, this.bufferId, 0);
            this.bufferId[0] = 0;
        }
    }

    protected void loadBufferObjectData(DrawContext dc) {
        GLES20.glBufferData(this.bufferTarget, this.bufferByteCount, this.buffer, GLES20.GL_STATIC_DRAW);
    }
}
