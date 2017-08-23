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
import java.util.HashSet;
import java.util.Set;

import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.Viewport;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.Framebuffer;
import gov.nasa.worldwind.render.Texture;

public class DrawContext {

    public Vec3 eyePoint = new Vec3();

    public Viewport viewport = new Viewport();

    public Matrix4 projection = new Matrix4();

    public Matrix4 modelview = new Matrix4();

    public Matrix4 modelviewProjection = new Matrix4();

    public Matrix4 infiniteProjection = new Matrix4();

    public Matrix4 screenProjection = new Matrix4();

    public DrawableQueue drawableQueue;

    public DrawableQueue drawableTerrain;

    public PickedObjectList pickedObjects;

    public Viewport pickViewport;

    public Vec2 pickPoint;

    public boolean pickMode;

    private int framebufferId;

    private int programId;

    private int textureUnit = GLES20.GL_TEXTURE0;

    private int[] textureId = new int[32];

    private int arrayBufferId;

    private int elementArrayBufferId;

    private Framebuffer scratchFramebuffer;

    private BufferObject unitSquareBuffer;

    private ByteBuffer scratchBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());

    private ArrayList<Object> scratchList = new ArrayList<>();

    private byte[] pixelArray = new byte[4];

    public DrawContext() {
    }

    public void reset() {
        this.eyePoint.set(0, 0, 0);
        this.viewport.setEmpty();
        this.projection.setToIdentity();
        this.modelview.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.screenProjection.setToIdentity();
        this.infiniteProjection.setToIdentity();
        this.drawableQueue = null;
        this.drawableTerrain = null;
        this.pickedObjects = null;
        this.pickViewport = null;
        this.pickPoint = null;
        this.pickMode = false;
        this.scratchBuffer.clear();
        this.scratchList.clear();
    }

    public void contextLost() {
        // Clear objects and values associated with the current OpenGL context.
        this.framebufferId = 0;
        this.programId = 0;
        this.textureUnit = GLES20.GL_TEXTURE0;
        this.arrayBufferId = 0;
        this.elementArrayBufferId = 0;
        this.scratchFramebuffer = null;
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
     * Returns the name of the OpenGL framebuffer object that is currently active.
     *
     * @return the currently active framebuffer object, or 0 if no framebuffer object is active
     */
    public int currentFramebuffer() {
        return this.framebufferId;
    }

    /**
     * Makes an OpenGL framebuffer object active. The active framebuffer becomes the target of all OpenGL commands that
     * render to the framebuffer or read from the framebuffer. This has no effect if the specified framebuffer object is
     * already active. The default is framebuffer 0, indicating that the default framebuffer provided by the windowing
     * system is active.
     *
     * @param framebufferId the name of the OpenGL framebuffer object to make active, or 0 to make the default
     *                      framebuffer provided by the windowing system active
     */
    public void bindFramebuffer(int framebufferId) {
        if (this.framebufferId != framebufferId) {
            this.framebufferId = framebufferId;
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        }
    }

    /**
     * Returns an OpenGL framebuffer object suitable for offscreen drawing. The framebuffer has a 32-bit color buffer
     * and a 32-bit depth buffer, both attached as OpenGL texture 2D objects.
     * <p>
     * The framebuffer may be used by any drawable and for any purpose. However, the draw context makes no guarantees
     * about the framebuffer's contents. Drawables must clear the framebuffer before use, and must assume its contents
     * may be modified by another drawable, either during the current frame or in a subsequent frame.
     * <p>
     * The OpenGL framebuffer object is created on first use and cached. Subsequent calls to this method return the
     * cached buffer object.
     *
     * @return the draw context's scratch OpenGL framebuffer object
     */
    public Framebuffer scratchFramebuffer() {
        if (this.scratchFramebuffer != null) {
            return this.scratchFramebuffer;
        }

        Framebuffer framebuffer = new Framebuffer();
        Texture colorAttachment = new Texture(1024, 1024, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE);
        Texture depthAttachment = new Texture(1024, 1024, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_SHORT);
        // TODO consider modifying Texture's tex parameter behavior in order to make this unnecessary
        depthAttachment.setTexParameter(GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        depthAttachment.setTexParameter(GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        framebuffer.attachTexture(this, colorAttachment, GLES20.GL_COLOR_ATTACHMENT0);
        framebuffer.attachTexture(this, depthAttachment, GLES20.GL_DEPTH_ATTACHMENT);

        return (this.scratchFramebuffer = framebuffer);
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
     *
     * @return the draw context's unit square OpenGL buffer object
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

        int size = points.length * 4;
        FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(points).rewind();

        BufferObject bufferObject = new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer);

        return (this.unitSquareBuffer = bufferObject);
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

        // Read the fragment pixel as an RGBA 8888 color.
        ByteBuffer pixelBuffer = (ByteBuffer) this.scratchBuffer(4).clear();
        GLES20.glReadPixels(x, y, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        pixelBuffer.get(this.pixelArray, 0, 4);

        // Convert the RGBA 8888 color to a World Wind color.
        result.red = (this.pixelArray[0] & 0xFF) / (float) 0xFF;
        result.green = (this.pixelArray[1] & 0xFF) / (float) 0xFF;
        result.blue = (this.pixelArray[2] & 0xFF) / (float) 0xFF;
        result.alpha = (this.pixelArray[3] & 0xFF) / (float) 0xFF;

        return result;
    }

    /**
     * Reads the unique fragment colors within a screen rectangle in the currently active OpenGL frame buffer. The
     * components indicate OpenGL screen coordinates, which originate in the frame buffer's lower left corner.
     *
     * @param x      the screen rectangle's X component
     * @param y      the screen rectangle's Y component
     * @param width  the screen rectangle's width
     * @param height the screen rectangle's height
     *
     * @return a set containing the unique fragment colors
     */
    public Set<Color> readPixelColors(int x, int y, int width, int height) {
        // Read the fragment pixels as a tightly packed array of RGBA 8888 colors.
        int pixelCount = width * height;
        ByteBuffer pixelBuffer = (ByteBuffer) this.scratchBuffer(pixelCount * 4).clear();
        GLES20.glReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);

        HashSet<Color> resultSet = new HashSet<>();
        Color result = new Color();

        for (int idx = 0; idx < pixelCount; idx++) {
            // Copy each RGBA 888 color from the NIO buffer a heap array in bulk to reduce buffer access overhead.
            pixelBuffer.get(this.pixelArray, 0, 4);

            // Convert the RGBA 8888 color to a World Wind color.
            result.red = (this.pixelArray[0] & 0xFF) / (float) 0xFF;
            result.green = (this.pixelArray[1] & 0xFF) / (float) 0xFF;
            result.blue = (this.pixelArray[2] & 0xFF) / (float) 0xFF;
            result.alpha = (this.pixelArray[3] & 0xFF) / (float) 0xFF;

            // Accumulate the unique colors in a set.
            if (resultSet.add(result)) {
                result = new Color();
            }
        }

        return resultSet;
    }

    /**
     * Returns a scratch NIO buffer suitable for use during drawing. The returned buffer has capacity at least equal to
     * the specified capacity. The buffer is cleared before each frame, otherwise its contents, position, limit and mark
     * are undefined.
     *
     * @param capacity the buffer's minimum capacity in bytes
     *
     * @return the draw context's scratch buffer
     */
    public ByteBuffer scratchBuffer(int capacity) {
        if (this.scratchBuffer.capacity() < capacity) {
            this.scratchBuffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        }

        return this.scratchBuffer;
    }

    /**
     * Returns a scratch list suitable for accumulating entries during drawing. The list is cleared before each frame,
     * otherwise its contents are undefined.
     *
     * @return the draw context's scratch list
     */
    public ArrayList<Object> scratchList() {
        return this.scratchList;
    }
}
