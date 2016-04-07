/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.graphics.Rect;
import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;

public class DrawContext {

    // TODO refactor these as public properties
    protected Globe globe;

    protected Terrain terrain;

    protected LayerList layers = new LayerList();

    protected Layer currentLayer;

    protected double verticalExaggeration = 1;

    protected Position eyePosition = new Position();

    protected double heading;

    protected double tilt;

    protected double roll;

    protected double fieldOfView;

    protected Rect viewport = new Rect();

    protected Matrix4 modelview = new Matrix4();

    protected Matrix4 modelviewTranspose = new Matrix4();

    protected Matrix4 projection = new Matrix4();

    protected Matrix4 projectionInv = new Matrix4();

    protected Matrix4 modelviewProjection = new Matrix4();

    protected Matrix4 modelviewProjectionInv = new Matrix4();

    protected Matrix4 screenProjection = new Matrix4();

    protected Vec3 eyePoint = new Vec3();

    protected Frustum frustum = new Frustum();

    protected double pixelSizeScale;

    protected double pixelSizeOffset;

    protected boolean pickingMode;

    protected boolean renderRequested;

    protected Resources resources;

    protected GpuObjectCache gpuObjectCache;

    protected SurfaceTileRenderer surfaceTileRenderer;

    protected int currentProgramId;

    protected int currentTexUnit = GLES20.GL_TEXTURE0;

    protected int[] currentTexId = new int[32];

    protected Map<Object, Object> userProperties = new HashMap<>();

    protected static class OrderedRenderableEntry {

        protected OrderedRenderable orderedRenderable;

        protected double eyeDistance;

        protected long insertionOrder;

        public OrderedRenderableEntry(OrderedRenderable orderedRenderable, double eyeDistance, int insertionOrder) {
            this.orderedRenderable = orderedRenderable;
            this.eyeDistance = eyeDistance;
            this.insertionOrder = insertionOrder;
        }
    }

    /**
     * Use a simple ArrayList for storing large number of OrderRenderables before reallocation
     */
    protected ArrayList<OrderedRenderableEntry> orderedRenderables = new ArrayList<>(1000);

    /**
     * Index used for iterating over the ordered renderables
     */
    private int orderedRenderablesIndex = 0;

    public DrawContext() {
    }

    public Globe getGlobe() {
        return globe;
    }

    public void setGlobe(Globe globe) {
        this.globe = globe;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public LayerList getLayers() {
        return layers;
    }

    public void setLayers(LayerList layers) {
        this.layers.clearLayers();
        this.layers.addAllLayers(layers);
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(Layer layer) {
        this.currentLayer = layer;
    }

    public double getVerticalExaggeration() {
        return verticalExaggeration;
    }

    public void setVerticalExaggeration(double verticalExaggeration) {
        this.verticalExaggeration = verticalExaggeration;
    }

    public Position getEyePosition() {
        return eyePosition;
    }

    public void setEyePosition(Position position) {
        this.eyePosition.set(position);
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double headingDegrees) {
        this.heading = headingDegrees;
    }

    public double getTilt() {
        return tilt;
    }

    public void setTilt(double tiltDegrees) {
        this.tilt = tiltDegrees;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double rollDegrees) {
        this.roll = rollDegrees;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(double fovyDegrees) {
        this.fieldOfView = fovyDegrees;
    }

    public Rect getViewport() {
        return viewport;
    }

    public void setViewport(Rect rect) {
        this.viewport.set(rect);
        this.screenProjection.setToScreenProjection(this.viewport.width(), this.viewport.height());
    }

    public Matrix4 getModelview() {
        return modelview;
    }

    public Matrix4 getProjection() {
        return projection;
    }

    public Matrix4 getModelviewProjection() {
        return modelviewProjection;
    }

    public Matrix4 getModelviewProjectionInverse() {
        return modelviewProjectionInv;
    }

    public Matrix4 getScreenProjection() {
        return screenProjection;
    }

    public Vec3 getEyePoint() {
        return eyePoint;
    }

    public Frustum getFrustum() {
        return this.frustum;
    }

    public void setModelviewProjection(Matrix4 modelview, Matrix4 projection) {
        if (modelview == null || projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "DrawContext", "setModelview", "missingMatrix"));
        }

        this.modelview.set(modelview);
        this.modelviewTranspose.transposeMatrix(modelview);
        this.projection.set(projection);
        this.projectionInv.invertMatrix(projection);
        this.modelviewProjection.setToMultiply(projection, modelview);
        this.modelviewProjectionInv.invertMatrix(this.modelviewProjection);
        this.modelview.extractEyePoint(this.eyePoint);

        this.frustum.setToProjectionMatrix(this.projection);
        this.frustum.transformByMatrix(this.modelviewTranspose);
        this.frustum.normalize();

        this.computePixelSizeParams();
    }

    public boolean isPickingMode() {
        return pickingMode;
    }

    public void setPickingMode(boolean pickingMode) {
        this.pickingMode = pickingMode;
    }

    public boolean isRenderRequested() {
        return renderRequested;
    }

    public void requestRender() {
        this.renderRequested = true;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public GpuObjectCache getGpuObjectCache() {
        return gpuObjectCache;
    }

    public void setGpuObjectCache(GpuObjectCache gpuObjectCache) {
        this.gpuObjectCache = gpuObjectCache;
    }

    public SurfaceTileRenderer getSurfaceTileRenderer() {
        return surfaceTileRenderer;
    }

    public void setSurfaceTileRenderer(SurfaceTileRenderer surfaceTileRenderer) {
        this.surfaceTileRenderer = surfaceTileRenderer;
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

    public void reset() {
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
        this.viewport.setEmpty();
        this.modelview.setToIdentity();
        this.modelviewTranspose.setToIdentity();
        this.projection.setToIdentity();
        this.projectionInv.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.modelviewProjectionInv.setToIdentity();
        this.screenProjection.setToIdentity();
        this.eyePoint.set(0, 0, 0);
        this.frustum.setToUnitFrustum();
        this.pixelSizeOffset = 0;
        this.pixelSizeScale = 0;
        this.pickingMode = false;
        this.renderRequested = false;
        this.resources = null;
        this.gpuObjectCache = null;
        this.userProperties.clear();
        this.orderedRenderables.clear();
        this.orderedRenderablesIndex = 0;
    }

    public void contextLost() {
        // Reset properties tracking the current OpenGL state, which are now invalid.
        this.currentProgramId = 0;
        this.currentTexUnit = GLES20.GL_TEXTURE0;

        for (int i = 0; i < this.currentTexId.length; i++) {
            this.currentTexId[i] = 0;
        }
    }

    // TODO refactor to accept a programId argument
    public void useProgram(GpuProgram program) {
        int objectId = (program != null) ? program.getObjectId() : 0;

        if (this.currentProgramId != objectId) {
            this.currentProgramId = objectId;
            GLES20.glUseProgram(objectId);
        }
    }

    public void bindTexture(int texUnit, int textureId) {
        if (this.currentTexUnit != texUnit) {
            this.currentTexUnit = texUnit;
            GLES20.glActiveTexture(texUnit);
        }

        int texUnitIndex = texUnit - GLES20.GL_TEXTURE0;
        if (this.currentTexId[texUnitIndex] != textureId) {
            this.currentTexId[texUnitIndex] = textureId;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
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
     * @return the pixel height in meters
     */
    public double pixelSizeAtDistance(double distance) {
        // Compute the pixel size from the width of a rectangle carved out of the frustum in model coordinates at
        // the specified distance along the -Z axis and the viewport width in screen coordinates. The pixel size is
        // expressed in model coordinates per screen coordinate (e.g. meters per pixel).
        //
        // The frustum width is determined by noticing that the frustum size is a linear function of distance from
        // the eye point. The linear equation constants are determined during initialization, then solved for
        // distance here.
        //
        // This considers only the frustum width by assuming that the frustum and viewport share the same aspect
        // ratio, so that using either the frustum width or height results in the same pixel size.

        return this.pixelSizeScale * distance + this.pixelSizeOffset;
    }

    protected void computePixelSizeParams() {
        // Compute the eye coordinate rectangles carved out of the frustum by the near and far clipping planes, and
        // the distance between those planes and the eye point along the -Z axis. The rectangles are determined by
        // transforming the bottom-left and top-right points of the frustum from clip coordinates to eye
        // coordinates.
        Vec3 nbl = new Vec3(-1, -1, -1);
        Vec3 ntr = new Vec3(+1, +1, -1);
        Vec3 fbl = new Vec3(-1, -1, +1);
        Vec3 ftr = new Vec3(+1, +1, +1);
        // Convert each frustum corner from clip coordinates to eye coordinates by multiplying by the inverse
        // projection matrix.
        nbl.multiplyByMatrix(this.projectionInv);
        ntr.multiplyByMatrix(this.projectionInv);
        fbl.multiplyByMatrix(this.projectionInv);
        ftr.multiplyByMatrix(this.projectionInv);

        double nrRectWidth = Math.abs(ntr.x - nbl.x);
        double frRectWidth = Math.abs(ftr.x - fbl.x);
        double nrDistance = -nbl.z;
        double frDistance = -fbl.z;

        // Compute the scale and offset used to determine the width of a pixel on a rectangle carved out of the
        // frustum at a distance along the -Z axis in eye coordinates. These values are found by computing the scale
        // and offset of a frustum rectangle at a given distance, then dividing each by the viewport width.
        double frustumWidthScale = (frRectWidth - nrRectWidth) / (frDistance - nrDistance);
        double frustumWidthOffset = nrRectWidth - frustumWidthScale * nrDistance;
        this.pixelSizeScale = frustumWidthScale / this.viewport.width();
        this.pixelSizeOffset = frustumWidthOffset / this.viewport.height();
    }

    /**
     * Adds an ordered renderable to this draw context's ordered renderable list.
     *
     * @param orderedRenderable The ordered renderable to add.
     * @param eyeDistance       The ordered renderable's eye distance.
     */
    public void addOrderedRenderable(OrderedRenderable orderedRenderable, double eyeDistance) {
        this.orderedRenderables.add(new OrderedRenderableEntry(
            orderedRenderable,
            eyeDistance,
            this.orderedRenderables.size() // use the current size for the insertionOrder
        ));
    }

    /**
     * Sorts the ordered renderable list from the farthest to the eye point to the nearest from the eye point; the
     * nearest renderable is the last item in the list.
     */
    public void sortOrderedRenderables() {
        // Sort the ordered renderables by eye distance from front to back and then by insertion time.
        Collections.sort(this.orderedRenderables, new Comparator<OrderedRenderableEntry>() {
            @Override
            public int compare(OrderedRenderableEntry lhs, OrderedRenderableEntry rhs) {
                if (lhs.eyeDistance > rhs.eyeDistance) { // lhs is farther away; it is less than rhs
                    return -1;
                } else if (lhs.eyeDistance > rhs.eyeDistance) { // lhs is nearer; it is greater than rhs
                    return 1;
                } else { // lhs and rhs are the same distance from the eye; sort them based on insertion order
                    if (lhs.insertionOrder > rhs.insertionOrder) { // lhs is later; it is greater than rhs
                        return 1;
                    } else if (lhs.insertionOrder < rhs.insertionOrder) { // lhs is earlier; it is less than rhs
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
    }


    /**
     * Traverses the ordered renderable list from the furthest to the nearest eye distance; the nearest item is the last
     * renderable popped from the list.
     *
     * @return The furthest ordered renderable, or null if at the end of the list.
     */
    public OrderedRenderable popOrderedRenderable() {
        if (this.orderedRenderablesIndex >= this.orderedRenderables.size()) {
            return null;
        }
        // Return the next furthest item from the sorted list
        return this.orderedRenderables.get(this.orderedRenderablesIndex++).orderedRenderable;
    }

    /**
     * Transforms the specified model point from model coordinates to screen coordinates, applying an offset to the
     * modelPoint's projected depth value.
     * <p/>
     * The resultant screen point is in WebGL screen coordinates, with the origin in the bottom-left corner and axes
     * that extend up and to the right from the origin.
     * <p/>
     * This function stores the transformed point in the result argument, and returns true or false to indicate whether
     * or not the transformation is successful. It returns false if this navigator state's modelview or projection
     * matrices are malformed, or if the modelPoint is clipped by the near clipping plane or the far clipping plane,
     * ignoring the depth offset.
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
     * @param modelPoint  The model coordinate point to project.
     * @param depthOffset The amount of offset to apply.
     * @param result      A pre-allocated vector in which to return the projected point.
     *
     * @return true if the transformation is successful, otherwise false.
     *
     * @throws IllegalArgumentException If either the specified point or result argument is null or undefined.
     */
    public boolean projectWithDepth(Vec3 modelPoint, double depthOffset, Vec3 result) {
        if (modelPoint == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR,
                "DrawContext", "projectWithDepth", "missingPoint"));
        }

        if (result == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR,
                "DrawContext", "projectWithDepth", "missingResult"));
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
        // Matrix.offsetProjectionDepth for more information on the effect of this offset.
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
        x = x * this.viewport.width() + this.viewport.left;
        y = y * this.viewport.height() + this.viewport.top;

        result.x = x;
        result.y = y;
        result.z = z;

        return true;
    }

    ;
}
