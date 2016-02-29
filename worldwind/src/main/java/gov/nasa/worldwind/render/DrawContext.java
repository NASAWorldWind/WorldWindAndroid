/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.Context;

import java.util.List;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.Layer;

public class DrawContext {

    protected Context context;
    protected Globe globe;
    protected Terrain terrain;
    protected List<Layer> layers;
    protected Layer currentLayer;
    protected Matrix modelview;
    protected Matrix projection;
    protected Matrix modelviewProjection;
    protected Matrix modelviewProjectionInv;
    protected Vec3 eyePoint;
    protected Position eyePosition;
    protected double heading;
    protected double tilt;
    protected double roll;
    protected double fieldOfView;
    protected boolean pickingMode;
    protected boolean renderRequested;

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

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(Layer currentLayer) {
        this.currentLayer = currentLayer;
    }

    public Matrix getModelview() {
        return modelview;
    }

    public void setModelview(Matrix modelview) {
        this.modelview = modelview;
    }

    public Matrix getProjection() {
        return projection;
    }

    public void setProjection(Matrix projection) {
        this.projection = projection;
    }

    public Matrix getModelviewProjection() {
        return modelviewProjection;
    }

    public Matrix getModelviewProjectionInverse() {
        return modelviewProjectionInv;
    }

    public Vec3 getEyePoint() {
        return eyePoint;
    }

    public Position getEyePosition() {
        return eyePosition;
    }

    public void setEyePosition(Position eyePosition) {
        this.eyePosition = eyePosition;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getTilt() {
        return tilt;
    }

    public void setTilt(double tilt) {
        this.tilt = tilt;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(double fieldOfView) {
        this.fieldOfView = fieldOfView;
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
}
