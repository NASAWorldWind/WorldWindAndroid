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

import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.Color;

public class DrawContext {

    public Vec3 eyePoint = new Vec3();

    public Matrix4 projection = new Matrix4();

    public Matrix4 modelview = new Matrix4();

    public Matrix4 modelviewProjection = new Matrix4();

    public Matrix4 screenProjection = new Matrix4();

    public DrawableQueue drawableQueue;

    public DrawableList drawableTerrain;

    public PickedObjectList pickedObjects;

    public Vec2 pickPoint;

    public boolean pickMode;

    private int programId;

    private int textureUnit = GLES20.GL_TEXTURE0;

    private int[] textureId = new int[32];

    private int arrayBufferId;

    private int elementArrayBufferId;

    private BufferObject unitSquareBuffer;

    private ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());

    private byte[] pixelArray = new byte[4];

    private ArrayList<Object> scratchList = new ArrayList<>();

    public DrawContext() {
    }

    public void reset() {
        this.eyePoint.set(0, 0, 0);
        this.projection.setToIdentity();
        this.modelview.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.screenProjection.setToIdentity();
        this.drawableQueue = null;
        this.drawableTerrain = null;
        this.pickedObjects = null;
        this.pickPoint = null;
        this.pickMode = false;
        this.scratchList.clear();
    }

    public void contextLost() {
        // Clear objects and values associated with the current OpenGL context.
        this.programId = 0;
        this.textureUnit = GLES20.GL_TEXTURE0;
        this.arrayBufferId = 0;
        this.elementArrayBufferId = 0;
        this.unitSquareBuffer = null;
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
     * Returns an OpenGL buffer object containing a unit square expressed as four vertices at (0, 1), (0, 0), (1, 1) and
     * (1, 0). Each vertex is stored as two 32-bit floating point coordinates. The four vertices are in the order
     * required by a triangle strip.
     * <p/>
     * The OpenGL buffer object is created on first use and cached. Subsequent calls to this method return the cached
     * buffer object.
     */
    public BufferObject unitSquareBuffer() {
        if (this.unitSquareBuffer != null) {
            return this.unitSquareBuffer;
        }

        float[] points = new float[]{
            0, 1,   // upper left corner
            0, 0,   // lower left corner
            1, 1,   // upper right corner
            1, 0};  // lower right corner

        FloatBuffer buffer = ByteBuffer.allocateDirect(points.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(points).rewind();

        this.unitSquareBuffer = new BufferObject(GLES20.GL_ARRAY_BUFFER, buffer);
        return this.unitSquareBuffer;
    }

    /**
     * Reads the fragment color at a screen point in the currently active OpenGL frame buffer. The X and Y components
     * indicate OpenGL screen coordinates, which originate in the frame buffer's lower left corner.
     *
     * @param x      the screen point's X component
     * @param y      the screen point's Y component
     * @param result an optional pre-allocated Color in which to return the fragment color, or null to return a new
     *               color
     *
     * @return the result argument set to the fragment color, or a new color if the result is null
     */
    public Color readPixelColor(int x, int y, Color result) {
        if (result == null) {
            result = new Color();
        }

        GLES20.glReadPixels(x, y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, this.pixelBuffer.rewind());
        this.pixelBuffer.get(this.pixelArray);

        result.red = (this.pixelArray[0] & 0xFF) / (float) 0xFF;
        result.green = (this.pixelArray[1] & 0xFF) / (float) 0xFF;
        result.blue = (this.pixelArray[2] & 0xFF) / (float) 0xFF;
        result.alpha = (this.pixelArray[3] & 0xFF) / (float) 0xFF;

        return result;
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
