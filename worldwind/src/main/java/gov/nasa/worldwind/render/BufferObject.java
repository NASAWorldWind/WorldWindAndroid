/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;

import java.nio.Buffer;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.util.Logger;

public class BufferObject implements RenderResource {

    protected int[] bufferId = new int[1];

    protected int bufferTarget;

    protected int bufferLength;

    protected int bufferByteCount;

    protected Buffer buffer;

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
