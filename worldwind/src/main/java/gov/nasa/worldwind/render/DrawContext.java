/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.graphics.Rect;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableList;
import gov.nasa.worldwind.draw.DrawableQueue;
import gov.nasa.worldwind.draw.DrawableTerrain;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;
import gov.nasa.worldwind.util.WWMath;

public class DrawContext {

    public boolean pickingMode;

    public Globe globe;

    public Terrain terrain;

    public LayerList layers = new LayerList();

    public Layer currentLayer;

    public double verticalExaggeration = 1;

    public Position eyePosition = new Position();

    public double heading;

    public double tilt;

    public double roll;

    public double fieldOfView;

    public double horizonDistance;

    public Rect viewport = new Rect();

    public Matrix4 modelview = new Matrix4();

    public Matrix4 projection = new Matrix4();

    public Matrix4 modelviewProjection = new Matrix4();

    public Matrix4 screenProjection = new Matrix4();

    public Vec3 eyePoint = new Vec3();

    public Frustum frustum = new Frustum();

    public RenderResourceCache renderResourceCache;

    public Resources resources;

    protected boolean renderRequested;

    protected double pixelSizeFactor;

    protected DrawableQueue drawableQueue = new DrawableQueue();

    protected DrawableList drawableTerrain = new DrawableList();

    protected Map<Object, Pool<?>> drawablePools = new HashMap<>();

    protected Map<Object, Object> userProperties = new HashMap<>();

    protected int programId;

    protected int textureUnit = GLES20.GL_TEXTURE0;

    protected int[] textureId = new int[32];

    protected int arrayBufferId;

    protected int elementArrayBufferId;

    protected int unitQuadBufferId;

    public DrawContext() {
    }

    public boolean isRenderRequested() {
        return renderRequested;
    }

    public void requestRender() {
        this.renderRequested = true;
    }

    /**
     * Returns the height of a pixel at a given distance from the eye point. This method assumes the model of a screen
     * composed of rectangular pixels, where pixel coordinates denote infinitely thin space between pixels. The units of
     * the returned size are in meters per pixel.
     * <p/>
     * The result of this method is undefined if the distance is negative.
     *
     * @param distance the distance from the eye point in meters
     *
     * @return the pixel height in meters per pixel
     */
    public double pixelSizeAtDistance(double distance) {
        if (this.pixelSizeFactor == 0) { // cache the scaling factor used to convert distances to pixel sizes
            double fovyDegrees = this.fieldOfView;
            double tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5));
            this.pixelSizeFactor = 2 * tanfovy_2 / this.viewport.height();
        }

        return distance * this.pixelSizeFactor;
    }

    /**
     * Projects a Cartesian point to screen coordinates. The resultant screen point is in OpenGL screen coordinates,
     * with the origin in the bottom-left corner and axes that extend up and to the right from the origin.
     * <p/>
     * This stores the projected point in the result argument, and returns a boolean value indicating whether or not the
     * projection is successful. This returns false if the Cartesian point is clipped by the near clipping plane or the
     * far clipping plane.
     *
     * @param modelPoint the Cartesian point to project
     * @param result     a pre-allocated {@link Vec3} in which to return the projected point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public boolean project(Vec3 modelPoint, Vec3 result) {
        if (modelPoint == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "DrawContext", "project", "missingPoint"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "DrawContext", "project", "missingResult"));
        }

        // Transform the model point from model coordinates to eye coordinates then to clip coordinates. This
        // inverts the Z axis and stores the negative of the eye coordinate Z value in the W coordinate.
        double mx = modelPoint.x;
        double my = modelPoint.y;
        double mz = modelPoint.z;
        double[] m = this.modelviewProjection.m;
        double x = m[0] * mx + m[1] * my + m[2] * mz + m[3];
        double y = m[4] * mx + m[5] * my + m[6] * mz + m[7];
        double z = m[8] * mx + m[9] * my + m[10] * mz + m[11];
        double w = m[12] * mx + m[13] * my + m[14] * mz + m[15];

        if (w == 0) {
            return false;
        }

        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1].
        x /= w;
        y /= w;
        z /= w;

        // Clip the point against the near and far clip planes.
        if (z < -1 || z > 1) {
            return false;
        }

        // Convert the point from clip coordinate to the range [0,1]. This enables the X and Y coordinates to be
        // converted to screen coordinates, and the Z coordinate to represent a depth value in the range[0,1].
        x = x * 0.5 + 0.5;
        y = y * 0.5 + 0.5;
        z = z * 0.5 + 0.5;

        // Convert the X and Y coordinates from the range [0,1] to screen coordinates.
        x = x * this.viewport.width() + viewport.left;
        y = y * this.viewport.height() + viewport.top; // viewport rectangle is inverted in OpenGL coordinates

        result.x = x;
        result.y = y;
        result.z = z;

        return true;
    }

    /**
     * Projects a Cartesian point to screen coordinates, applying an offset to the point's projected depth value. The
     * resultant screen point is in OpenGL screen coordinates, with the origin in the bottom-left corner and axes that
     * extend up and to the right from the origin.
     * <p/>
     * This stores the projected point in the result argument, and returns a boolean value indicating whether or not the
     * projection is successful. This returns false if the Cartesian point is clipped by the near clipping plane or the
     * far clipping plane.
     * <p/>
     * The depth offset may be any real number and is typically used to move the screenPoint slightly closer to the
     * user's eye in order to give it visual priority over nearby objects or terrain. An offset of zero has no effect.
     * An offset less than zero brings the screenPoint closer to the eye, while an offset greater than zero pushes the
     * projected screen point away from the eye.
     * <p/>
     * Applying a non-zero depth offset has no effect on whether the model point is clipped by this method or by WebGL.
     * Clipping is performed on the original model point, ignoring the depth offset. The final depth value after
     * applying the offset is clamped to the range [0,1].
     *
     * @param modelPoint  the Cartesian point to project
     * @param depthOffset the amount of depth offset to apply
     * @param result      a pre-allocated {@link Vec3} in which to return the projected point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public boolean projectWithDepth(Vec3 modelPoint, double depthOffset, Vec3 result) {
        if (modelPoint == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "DrawContext", "projectWithDepth", "missingPoint"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "DrawContext", "projectWithDepth", "missingResult"));
        }

        // Transform the model point from model coordinates to eye coordinates. The eye coordinate and the clip
        // coordinate are transformed separately in order to reuse the eye coordinate below.
        double mx = modelPoint.x;
        double my = modelPoint.y;
        double mz = modelPoint.z;
        double[] m = this.modelview.m;
        double ex = m[0] * mx + m[1] * my + m[2] * mz + m[3];
        double ey = m[4] * mx + m[5] * my + m[6] * mz + m[7];
        double ez = m[8] * mx + m[9] * my + m[10] * mz + m[11];
        double ew = m[12] * mx + m[13] * my + m[14] * mz + m[15];

        // Transform the point from eye coordinates to clip coordinates.
        double[] p = this.projection.m;
        double x = p[0] * ex + p[1] * ey + p[2] * ez + p[3] * ew;
        double y = p[4] * ex + p[5] * ey + p[6] * ez + p[7] * ew;
        double z = p[8] * ex + p[9] * ey + p[10] * ez + p[11] * ew;
        double w = p[12] * ex + p[13] * ey + p[14] * ez + p[15] * ew;

        if (w == 0) {
            return false;
        }

        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y
        // and Z coordinates are in the range [-1,1].
        x /= w;
        y /= w;
        z /= w;

        // Clip the point against the near and far clip planes.
        if (z < -1 || z > 1) {
            return false;
        }

        // Transform the Z eye coordinate to clip coordinates again, this time applying a depth offset. The depth
        // offset is applied only to the matrix element affecting the projected Z coordinate, so we inline the
        // computation here instead of re-computing X, Y, Z and W in order to improve performance. See
        // Matrix4.offsetProjectionDepth for more information on the effect of this offset.
        z = p[8] * ex + p[9] * ey + p[10] * ez * (1 + depthOffset) + p[11] * ew;
        z /= w;

        // Clamp the point to the near and far clip planes. We know the point's original Z value is contained within
        // the clip planes, so we limit its offset z value to the range [-1, 1] in order to ensure it is not clipped
        // by WebGL. In clip coordinates the near and far clip planes are perpendicular to the Z axis and are
        // located at -1 and 1, respectively.
        z = WWMath.clamp(z, -1, 1);

        // Convert the point from clip coordinates to the range [0, 1]. This enables the XY coordinates to be
        // converted to screen coordinates, and the Z coordinate to represent a depth value in the range [0, 1].
        x = x * 0.5 + 0.5;
        y = y * 0.5 + 0.5;
        z = z * 0.5 + 0.5;

        // Convert the X and Y coordinates from the range [0,1] to screen coordinates.
        x = x * viewport.width() + viewport.left;
        y = y * viewport.height() + viewport.top; // viewport rectangle is inverted in OpenGL coordinates

        result.x = x;
        result.y = y;
        result.z = z;

        return true;
    }

    public ShaderProgram getShaderProgram(Object key) {
        return (ShaderProgram) this.renderResourceCache.get(key);
    }

    public ShaderProgram putShaderProgram(Object key, ShaderProgram program) {
        this.renderResourceCache.put(key, program, (program != null) ? program.getProgramLength() : 0);
        return program;
    }

    public Texture getTexture(ImageSource imageSource) {
        return (Texture) this.renderResourceCache.get(imageSource);
    }

    public Texture putTexture(ImageSource imageSource, Texture texture) {
        this.renderResourceCache.put(imageSource, texture, (texture != null) ? texture.getImageByteCount() : 0);
        return texture;
    }

    public Texture retrieveTexture(ImageSource imageSource) {
        return this.renderResourceCache.retrieveTexture(imageSource);
    }

    public void offerDrawable(Drawable drawable, int groupId, double order) {
        this.drawableQueue.offerDrawable(drawable, groupId, order);
    }

    public void offerSurfaceDrawable(Drawable drawable, double zOrder) {
        this.drawableQueue.offerDrawable(drawable, WorldWind.SURFACE_DRAWABLE, zOrder);
    }

    public void offerShapeDrawable(Drawable drawable, double eyeDistance) {
        this.drawableQueue.offerDrawable(drawable, WorldWind.SHAPE_DRAWABLE, -eyeDistance); // order by descending eye distance
    }

    public void offerDrawableTerrain(DrawableTerrain drawable) {
        this.drawableTerrain.offerDrawable(drawable);
    }

    public Drawable peekDrawable() {
        return this.drawableQueue.peekDrawable();
    }

    public Drawable pollDrawable() {
        return this.drawableQueue.pollDrawable();
    }

    public void sortDrawables() {
        this.drawableQueue.sortDrawables();
    }

    public int getDrawableTerrainCount() {
        return this.drawableTerrain.count();
    }

    public DrawableTerrain getDrawableTerrain(int index) {
        return (DrawableTerrain) this.drawableTerrain.getDrawable(index);
    }

    @SuppressWarnings("unchecked")
    public <T extends Drawable> Pool<T> getDrawablePool(Class<T> key) {
        Pool<T> pool = (Pool<T>) this.drawablePools.get(key);

        if (pool == null) {
            pool = new SynchronizedPool<>(); // use SynchronizedPool; acquire and are release may be called in separate threads
            this.drawablePools.put(key, pool);
        }

        return pool;
    }

    public Object getUserProperty(Object key) {
        return this.userProperties.get(key);
    }

    public Object putUserProperty(Object key, Object value) {
        return this.userProperties.put(key, value);
    }

    public Object removeUserProperty(Object key) {
        return this.userProperties.remove(key);
    }

    public boolean hasUserProperty(Object key) {
        return this.userProperties.containsKey(key);
    }

    public void resetFrameProperties() {
        this.pickingMode = false;
        this.globe = null;
        this.terrain = null;
        this.layers.clearLayers();
        this.currentLayer = null;
        this.verticalExaggeration = 1;
        this.eyePosition.set(0, 0, 0);
        this.heading = 0;
        this.tilt = 0;
        this.roll = 0;
        this.fieldOfView = 0;
        this.horizonDistance = 0;
        this.viewport.setEmpty();
        this.modelview.setToIdentity();
        this.projection.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.screenProjection.setToIdentity();
        this.eyePoint.set(0, 0, 0);
        this.frustum.setToUnitFrustum();
        this.renderResourceCache = null;
        this.resources = null;
        this.renderRequested = false;
        this.pixelSizeFactor = 0;
        this.drawableQueue.clearDrawables();
        this.drawableTerrain.clearDrawables();
        this.userProperties.clear();
    }

    public void contextLost() {
        // Clear objects and values associated with the current OpenGL context.
        this.programId = 0;
        this.textureUnit = GLES20.GL_TEXTURE0;
        this.arrayBufferId = 0;
        this.elementArrayBufferId = 0;
        this.unitQuadBufferId = 0;
        Arrays.fill(this.textureId, 0);
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
     * Returns the name of an OpenGL buffer object containing a unit quadrilateral expressed as four vertices at (0, 1),
     * (0, 0), (1, 1) and (1, 0). Each vertex is stored as two 32-bit floating point coordinates. The four vertices are
     * in the order required by a triangle strip.
     * <p/>
     * The OpenGL buffer object is created on first use and cached. Subsequent calls to this method return the cached
     * buffer object.
     */
    public int unitQuadBuffer() {
        if (this.unitQuadBufferId != 0) {
            return this.unitQuadBufferId;
        }

        int[] newBuffer = new int[1];
        GLES20.glGenBuffers(1, newBuffer, 0);
        this.unitQuadBufferId = newBuffer[0];

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
            this.bindBuffer(GLES20.GL_ARRAY_BUFFER, this.unitQuadBufferId);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size * 4, quadBuffer, GLES20.GL_STATIC_DRAW);
        } finally {
            this.bindBuffer(GLES20.GL_ARRAY_BUFFER, currentBuffer);
        }

        return this.unitQuadBufferId;
    }
}
