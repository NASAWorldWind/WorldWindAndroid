/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

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

    public Rect viewport = new Rect();

    public Matrix4 modelview = new Matrix4();

    public Matrix4 projection = new Matrix4();

    public Matrix4 modelviewProjection = new Matrix4();

    public Vec3 eyePoint = new Vec3();

    public Frustum frustum = new Frustum();

    public GpuObjectCache gpuObjectCache;

    public SurfaceTileRenderer surfaceTileRenderer;

    public Resources resources;

    protected boolean renderRequested;

    protected double pixelSizeFactor;

    protected Map<Object, Object> userProperties = new HashMap<>();

    protected int glProgramId;

    protected int glTexUnit = GLES20.GL_TEXTURE0;

    protected int[] glTexId = new int[32];

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
        this.viewport.setEmpty();
        this.modelview.setToIdentity();
        this.projection.setToIdentity();
        this.modelviewProjection.setToIdentity();
        this.eyePoint.set(0, 0, 0);
        this.frustum.setToUnitFrustum();
        this.gpuObjectCache = null;
        this.resources = null;
        this.renderRequested = false;
        this.pixelSizeFactor = 0;
        this.userProperties.clear();
    }

    public void contextLost() {
        // Reset properties tracking the current OpenGL state, which are now invalid.
        this.glProgramId = 0;
        this.glTexUnit = GLES20.GL_TEXTURE0;

        for (int i = 0; i < this.glTexId.length; i++) {
            this.glTexId[i] = 0;
        }
    }

    // TODO refactor to accept a programId argument
    public void useProgram(GpuProgram program) {
        int objectId = (program != null) ? program.getObjectId() : 0;

        if (this.glProgramId != objectId) {
            this.glProgramId = objectId;
            GLES20.glUseProgram(objectId);
        }
    }

    public void bindTexture(int texUnit, int textureId) {
        if (this.glTexUnit != texUnit) {
            this.glTexUnit = texUnit;
            GLES20.glActiveTexture(texUnit);
        }

        int texUnitIndex = texUnit - GLES20.GL_TEXTURE0;
        if (this.glTexId[texUnitIndex] != textureId) {
            this.glTexId[texUnitIndex] = textureId;
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
    }
}
