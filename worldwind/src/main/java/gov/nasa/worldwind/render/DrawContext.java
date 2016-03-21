/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES20;

import java.util.HashMap;
import java.util.Map;

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
    protected Context context;

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

    protected Matrix4 projection = new Matrix4();

    protected Matrix4 modelviewProjection = new Matrix4();

    protected Matrix4 modelviewProjectionInv = new Matrix4();

    protected Vec3 eyePoint = new Vec3();

    protected boolean pickingMode;

    protected boolean renderRequested;

    protected GpuObjectCache gpuObjectCache;

    protected SurfaceTileRenderer surfaceTileRenderer;

    protected int currentProgramId;

    protected int currentTexUnit = GLES20.GL_TEXTURE0;

    protected int[] currentTexId = new int[32];

    protected Map<Object, Object> userProperties = new HashMap<>();

    public DrawContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
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

    public void setModelviewProjection(Matrix4 modelview, Matrix4 projection) {
        if (modelview == null || projection == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "DrawContext", "setModelview", "missingMatrix"));
        }

        this.modelview.set(modelview);
        this.projection.set(projection);
        this.modelviewProjection.setToMultiply(projection, modelview);
        this.modelviewProjectionInv.invertMatrix(this.modelviewProjection);
        this.modelview.extractEyePoint(this.eyePoint);
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
        this.projection.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.modelviewProjectionInv.setToIdentity();
        this.eyePoint.set(0, 0, 0);
        this.pickingMode = false;
        this.renderRequested = false;
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

    // TODO overloaded version that accepts an objectId argument
    public void useProgram(GpuProgram program) {
        int objectId = (program != null) ? program.getObjectId() : 0;

        if (this.currentProgramId != objectId) {
            this.currentProgramId = objectId;
            GLES20.glUseProgram(objectId);
        }
    }

    // TODO overloaded version that accepts an objectId argument
    public void bindTexture(int texUnit, GpuTexture texture) {
        int texUnitIndex = texUnit - GLES20.GL_TEXTURE0;
        int objectId = (texture != null) ? texture.getObjectId() : 0;

        if (this.currentTexUnit != texUnit) {
            this.currentTexUnit = texUnit;
            GLES20.glActiveTexture(texUnit);
        }

        if (this.currentTexId[texUnitIndex] != objectId) {
            this.currentTexId[texUnitIndex] = objectId;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, objectId);
        }
    }
}
