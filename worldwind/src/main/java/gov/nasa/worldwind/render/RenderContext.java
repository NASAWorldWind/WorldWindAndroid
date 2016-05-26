/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableList;
import gov.nasa.worldwind.draw.DrawableQueue;
import gov.nasa.worldwind.draw.DrawableTerrain;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.Viewport;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;
import gov.nasa.worldwind.util.WWMath;

public class RenderContext {

    private static final int MAX_PICKED_OBJECT_ID = 0xFFFFFF;

    public Globe globe;

    public Terrain terrain;

    public LayerList layers;

    public Layer currentLayer;

    public double verticalExaggeration = 1;

    public double fieldOfView;

    public double horizonDistance;

    public Camera camera = new Camera();

    public Vec3 cameraPoint = new Vec3();

    public Viewport viewport = new Viewport();

    public Matrix4 projection = new Matrix4();

    public Matrix4 modelview = new Matrix4();

    public Matrix4 modelviewProjection = new Matrix4();

    public Frustum frustum = new Frustum();

    public RenderResourceCache renderResourceCache;

    public Resources resources;

    public DrawableQueue drawableQueue;

    public DrawableList drawableTerrain;

    public PickedObjectList pickedObjects;

    public boolean pickMode;

    private int pickedObjectId;

    private boolean redrawRequested;

    private double pixelSizeFactor;

    private Map<Object, Pool<?>> drawablePools = new HashMap<>();

    private Map<Object, Object> userProperties = new HashMap<>();

    public RenderContext() {
    }

    public void reset() {
        this.globe = null;
        this.terrain = null;
        this.layers = null;
        this.currentLayer = null;
        this.verticalExaggeration = 1;
        this.fieldOfView = 0;
        this.horizonDistance = 0;
        this.camera.set(0, 0, 0, WorldWind.ABSOLUTE /*lat, lon, alt*/, 0, 0, 0 /*heading, tilt, roll*/);
        this.cameraPoint.set(0, 0, 0);
        this.viewport.setEmpty();
        this.projection.setToIdentity();
        this.modelview.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.frustum.setToUnitFrustum();
        this.renderResourceCache = null;
        this.resources = null;
        this.drawableQueue = null;
        this.drawableTerrain = null;
        this.pickedObjects = null;
        this.pickedObjectId = 0;
        this.pickMode = false;
        this.redrawRequested = false;
        this.pixelSizeFactor = 0;
        this.userProperties.clear();
    }

    public boolean isRedrawRequested() {
        return this.redrawRequested;
    }

    public void requestRedraw() {
        this.redrawRequested = true;
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
            this.pixelSizeFactor = 2 * tanfovy_2 / this.viewport.height;
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
                Logger.logMessage(Logger.ERROR, "RenderContext", "project", "missingPoint"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderContext", "project", "missingResult"));
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
        x = x * this.viewport.width + this.viewport.x;
        y = y * this.viewport.height + this.viewport.y;

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
                Logger.logMessage(Logger.ERROR, "RenderContext", "projectWithDepth", "missingPoint"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderContext", "projectWithDepth", "missingResult"));
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
        x = x * this.viewport.width + this.viewport.x;
        y = y * this.viewport.height + this.viewport.y;

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
        if (this.drawableQueue != null) {
            this.drawableQueue.offerDrawable(drawable, groupId, order);
        }
    }

    public void offerSurfaceDrawable(Drawable drawable, double zOrder) {
        if (this.drawableQueue != null) {
            this.drawableQueue.offerDrawable(drawable, WorldWind.SURFACE_DRAWABLE, zOrder);
        }
    }

    public void offerShapeDrawable(Drawable drawable, double cameraDistance) {
        if (this.drawableQueue != null) {
            this.drawableQueue.offerDrawable(drawable, WorldWind.SHAPE_DRAWABLE, -cameraDistance); // order by descending distance to the viewer
        }
    }

    public void offerDrawableTerrain(DrawableTerrain drawable) {
        if (this.drawableTerrain != null) {
            this.drawableTerrain.offerDrawable(drawable);
        }
    }

    public void sortDrawables() {
        if (this.drawableQueue != null) {
            this.drawableQueue.sortDrawables();
        }
    }

    public int drawableCount() {
        return (this.drawableQueue != null) ? this.drawableQueue.count() : 0;
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

    public void offerPickedObject(PickedObject pickedObject) {
        if (this.pickedObjects != null) {
            this.pickedObjects.offerPickedObject(pickedObject);
        }
    }

    public int nextPickedObjectId() {
        this.pickedObjectId++;

        if (this.pickedObjectId > MAX_PICKED_OBJECT_ID) {
            this.pickedObjectId = 1;
        }

        return this.pickedObjectId;
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
}
