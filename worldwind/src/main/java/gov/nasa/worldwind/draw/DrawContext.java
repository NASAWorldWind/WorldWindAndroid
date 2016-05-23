/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;

public class DrawContext {

    public Vec3 eyePoint = new Vec3();

    public Matrix4 projection = new Matrix4();

    public Matrix4 modelview = new Matrix4();

    public Matrix4 modelviewProjection = new Matrix4();

    public Matrix4 screenProjection = new Matrix4();

    public DrawableQueue drawableQueue;

    public DrawableList drawableTerrain;

    //public PickedObjectList pickedObjects;

    protected int programId;

    protected int textureUnit = GLES20.GL_TEXTURE0;

    protected int[] textureId = new int[32];

    protected int arrayBufferId;

    protected int elementArrayBufferId;

    protected int unitSquareBufferId;

    protected ArrayList<Object> scratchList = new ArrayList<>();

    public DrawContext() {
    }

    public void reset() {
        this.eyePoint.set(0, 0, 0);
        this.projection.setToIdentity();
        this.modelview.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.screenProjection.setToIdentity();
        this.scratchList.clear();
        this.drawableQueue = null;
        this.drawableTerrain = null;
        //this.pickedObjects = null;
    }

    public void contextLost() {
        // Clear objects and values associated with the current OpenGL context.
        this.programId = 0;
        this.textureUnit = GLES20.GL_TEXTURE0;
        this.arrayBufferId = 0;
        this.elementArrayBufferId = 0;
        this.unitSquareBufferId = 0;
        Arrays.fill(this.textureId, 0);
    }

    public Drawable peekDrawable() {
        return (this.drawableQueue != null) ? this.drawableQueue.peekDrawable() : null;
    }

    public Drawable pollDrawable() {
        return (this.drawableQueue != null) ? this.drawableQueue.pollDrawable() : null;
    }

    public void rewindDrawables() {
        if (this.drawableQueue != null) {
            this.drawableQueue.rewindDrawables();
        }
    }

    public int getDrawableTerrainCount() {
        return (this.drawableTerrain != null) ? this.drawableTerrain.count() : 0;
    }

    public DrawableTerrain getDrawableTerrain(int index) {
        return (this.drawableTerrain != null) ? (DrawableTerrain) this.drawableTerrain.getDrawable(index) : null;
    }

    /**
     * Returns the name of the OpenGL program object that is currently active.
     *
     * @return the currently active program object, or 0 if no program object is active
     */
    public int currentProgram() {
        return this.programId;
    }

    /**
     * Makes an OpenGL program object active as part of current rendering state. This has no effect if the specified
     * program object is already active. The default is program 0, indicating that no program is active.
     *
     * @param programId the name of the OpenGL program object to make active, or 0 to make no program active
     */
    public void useProgram(int programId) {
        if (this.programId != programId) {
            this.programId = programId;
            GLES20.glUseProgram(programId);
        }
    }

    /**
     * Returns the OpenGL multitexture unit that is currently active. Returns a value from the GL_TEXTUREi enumeration,
     * where i ranges from 0 to 32.
     *
     * @return the currently active multitexture unit.
     */
    public int currentTextureUnit() {
        return this.textureUnit;
    }

    /**
     * Specifies the OpenGL multitexture unit to make active. This has no effect if the specified multitexture unit is
     * already active. The default is GL_TEXTURE0.
     *
     * @param textureUnit the multitexture unit, one of GL_TEXTUREi, where i ranges from 0 to 32.
     */
    public void activeTextureUnit(int textureUnit) {
        if (this.textureUnit != textureUnit) {
            this.textureUnit = textureUnit;
            GLES20.glActiveTexture(textureUnit);
        }
    }

    /**
     * Returns the name of the OpenGL texture 2D object currently bound to the active multitexture unit. The active
     * multitexture unit may be determined by calling currentTextureUnit.
     *
     * @return the currently bound texture 2D object, or 0 if no texture object is bound
     */
    public int currentTexture() {
        int textureUnitIndex = this.textureUnit - GLES20.GL_TEXTURE0;
        return this.textureId[textureUnitIndex];
    }

    /**
     * Returns the name of the OpenGL texture 2D object currently bound to the specified multitexture unit.
     *
     * @param textureUnit the multitexture unit, one of GL_TEXTUREi, where i ranges from 0 to 32.
     *
     * @return the currently bound texture 2D object, or 0 if no texture object is bound
     */
    public int currentTexture(int textureUnit) {
        int textureUnitIndex = textureUnit - GLES20.GL_TEXTURE0;
        return this.textureId[textureUnitIndex];
    }

    /**
     * Makes an OpenGL texture 2D object bound to the current multitexture unit. This has no effect if the specified
     * texture object is already bound. The default is texture 0, indicating that no texture is bound.
     *
     * @param textureId the name of the OpenGL texture 2D object to make active, or 0 to make no texture active
     */
    public void bindTexture(int textureId) {
        int textureUnitIndex = this.textureUnit - GLES20.GL_TEXTURE0;
        if (this.textureId[textureUnitIndex] != textureId) {
            this.textureId[textureUnitIndex] = textureId;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
    }

    /**
     * Returns the name of the OpenGL buffer object bound to the specified target buffer.
     *
     * @param target the target buffer, either GL_ARRAY_BUFFER or GL_ELEMENT_ARRAY_BUFFER
     *
     * @return the currently bound buffer object, or 0 if no buffer object is bound
     */
    public int currentBuffer(int target) {
        if (target == GLES20.GL_ARRAY_BUFFER) {
            return this.arrayBufferId;
        } else if (target == GLES20.GL_ELEMENT_ARRAY_BUFFER) {
            return this.elementArrayBufferId;
        } else {
            return 0;
        }
    }

    /**
     * Makes an OpenGL buffer object bound to a specified target buffer. This has no effect if the specified buffer
     * object is already bound. The default is buffer 0, indicating that no buffer object is bound.
     *
     * @param target   the target buffer, either GL_ARRAY_BUFFER or GL_ELEMENT_ARRAY_BUFFER
     * @param bufferId the name of the OpenGL buffer object to make active
     */
    public void bindBuffer(int target, int bufferId) {
        if (target == GLES20.GL_ARRAY_BUFFER && this.arrayBufferId != bufferId) {
            this.arrayBufferId = bufferId;
            GLES20.glBindBuffer(target, bufferId);
        } else if (target == GLES20.GL_ELEMENT_ARRAY_BUFFER && this.elementArrayBufferId != bufferId) {
            this.elementArrayBufferId = bufferId;
            GLES20.glBindBuffer(target, bufferId);
        } else {
            GLES20.glBindBuffer(target, bufferId);
        }
    }

    /**
     * Returns the name of an OpenGL buffer object containing a unit square expressed as four vertices at (0, 1), (0,
     * 0), (1, 1) and (1, 0). Each vertex is stored as two 32-bit floating point coordinates. The four vertices are in
     * the order required by a triangle strip.
     * <p/>
     * The OpenGL buffer object is created on first use and cached. Subsequent calls to this method return the cached
     * buffer object.
     */
    public int unitSquareBuffer() {
        if (this.unitSquareBufferId != 0) {
            return this.unitSquareBufferId;
        }

        int[] newBuffer = new int[1];
        GLES20.glGenBuffers(1, newBuffer, 0);
        this.unitSquareBufferId = newBuffer[0];

        float[] points = new float[]{
            0, 1,   // upper left corner
            0, 0,   // lower left corner
            1, 1,   // upper right corner
            1, 0};  // lower right corner
        int size = points.length;
        FloatBuffer quadBuffer = ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        quadBuffer.put(points).rewind();

        int currentBuffer = this.currentBuffer(GLES20.GL_ARRAY_BUFFER);
        try {
            this.bindBuffer(GLES20.GL_ARRAY_BUFFER, this.unitSquareBufferId);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size * 4, quadBuffer, GLES20.GL_STATIC_DRAW);
        } finally {
            this.bindBuffer(GLES20.GL_ARRAY_BUFFER, currentBuffer);
        }

        return this.unitSquareBufferId;
    }

    /**
     * Returns a scratch list suitable for accumulating entries during drawing. This list is cleared before each frame,
     * otherwise its contents are undefined.
     *
     * @return the draw context's scratch list
     */
    public ArrayList<Object> scratchList() {
        return this.scratchList;
    }
}
