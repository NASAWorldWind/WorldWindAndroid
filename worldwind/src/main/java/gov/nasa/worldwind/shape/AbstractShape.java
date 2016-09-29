/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.BoundingBox;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.WWMath;

public abstract class AbstractShape extends AbstractRenderable implements Attributable, Highlightable {

    protected ShapeAttributes attributes;

    protected ShapeAttributes highlightAttributes;

    protected ShapeAttributes activeAttributes;

    protected boolean highlighted;

    @WorldWind.AltitudeMode
    protected int altitudeMode = WorldWind.ABSOLUTE;

    @WorldWind.PathType
    protected int pathType = WorldWind.GREAT_CIRCLE;

    protected int maximumIntermediatePoints = 10;

    protected int pickedObjectId;

    protected Color pickColor = new Color();

    protected Sector boundingSector = new Sector();

    protected BoundingBox boundingBox = new BoundingBox();

    protected static final double NEAR_ZERO_THRESHOLD = 1.0e-10;

    private Vec3 scratchPoint = new Vec3();

    public AbstractShape() {
        this.attributes = new ShapeAttributes();
    }

    public AbstractShape(ShapeAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public ShapeAttributes getAttributes() {
        return this.attributes;
    }

    @Override
    public void setAttributes(ShapeAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public ShapeAttributes getHighlightAttributes() {
        return this.highlightAttributes;
    }

    @Override
    public void setHighlightAttributes(ShapeAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
    }

    @Override
    public boolean isHighlighted() {
        return this.highlighted;
    }

    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    @WorldWind.AltitudeMode
    public int getAltitudeMode() {
        return this.altitudeMode;
    }

    public void setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        this.reset();
    }

    @WorldWind.PathType
    public int getPathType() {
        return this.pathType;
    }

    public void setPathType(@WorldWind.PathType int pathType) {
        this.pathType = pathType;
        this.reset();
    }

    public int getMaximumIntermediatePoints() {
        return maximumIntermediatePoints;
    }

    public void setMaximumIntermediatePoints(int maximumIntermediatePoints) {
        this.maximumIntermediatePoints = maximumIntermediatePoints;
    }

    @Override
    protected void doRender(RenderContext rc) {
        // Don't render anything if the shape is not visible.
        if (!this.intersectsFrustum(rc)) {
            return;
        }

        // Select the currently active attributes. Don't render anything if the attributes are unspecified.
        this.determineActiveAttributes(rc);
        if (this.activeAttributes == null) {
            return;
        }

        // Keep track of the drawable count to determine whether or not this shape has enqueued drawables.
        int drawableCount = rc.drawableCount();
        if (rc.pickMode) {
            this.pickedObjectId = rc.nextPickedObjectId();
            this.pickColor = PickedObject.identifierToUniqueColor(this.pickedObjectId, this.pickColor);
        }

        // Enqueue drawables for processing on the OpenGL thread.
        this.makeDrawable(rc);

        // Enqueue a picked object that associates the shape's drawables with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(PickedObject.fromRenderable(this.pickedObjectId, this, rc.currentLayer));
        }
    }

    protected boolean intersectsFrustum(RenderContext rc) {
        return this.boundingBox.isUnitBox() || this.boundingBox.intersectsFrustum(rc.frustum);
    }

    protected void determineActiveAttributes(RenderContext rc) {
        if (this.highlighted && this.highlightAttributes != null) {
            this.activeAttributes = this.highlightAttributes;
        } else {
            this.activeAttributes = this.attributes;
        }
    }

    protected double cameraDistanceGeographic(RenderContext rc, Sector boundingSector) {
        double lat = WWMath.clamp(rc.camera.latitude, boundingSector.minLatitude(), boundingSector.maxLatitude());
        double lon = WWMath.clamp(rc.camera.longitude, boundingSector.minLongitude(), boundingSector.maxLongitude());
        Vec3 point = rc.geographicToCartesian(lat, lon, 0, WorldWind.CLAMP_TO_GROUND, this.scratchPoint);

        return point.distanceTo(rc.cameraPoint);
    }

    protected double cameraDistanceCartesian(RenderContext rc, float[] array, int count, int stride, Vec3 offset) {
        double cx = rc.cameraPoint.x - offset.x;
        double cy = rc.cameraPoint.y - offset.y;
        double cz = rc.cameraPoint.z - offset.z;
        double minDistance2 = Double.POSITIVE_INFINITY;

        for (int idx = 0; idx < count; idx += stride) {
            double px = array[idx];
            double py = array[idx + 1];
            double pz = array[idx + 2];
            double dx = px - cx;
            double dy = py - cy;
            double dz = pz - cz;
            double distance2 = (dx * dx) + (dy * dy) + (dz * dz);

            if (minDistance2 > distance2) {
                minDistance2 = distance2;
            }
        }

        return Math.sqrt(minDistance2);
    }

    protected abstract void reset();

    protected abstract void makeDrawable(RenderContext rc);
}
