/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.opengl.GLES20;

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
        this.eyePoint.set(0, 0, 0);
        this.frustum.setToUnitFrustum();
        this.pixelSizeOffset = 0;
        this.pixelSizeScale = 0;
        this.pickingMode = false;
        this.renderRequested = false;
        this.resources = null;
        this.gpuObjectCache = null;
        this.userProperties.clear();
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
}
