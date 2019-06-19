/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import android.graphics.Typeface;

import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.util.WWMath;

/**
 * Displays a graticule.
 *
 * @author Patrick Murris
 * @version $Id: AbstractGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public abstract class AbstractGraticuleLayer extends AbstractLayer {

//    /**
//     * Solid line rendering style. This style specifies that a line will be drawn without any breaks. <br>
//     * <pre><code>_________</code></pre>
//     * <br> is an example of a solid line.
//     */
//    public static final String LINE_STYLE_SOLID = GraticuleRenderingParams.VALUE_LINE_STYLE_SOLID;
//    /**
//     * Dashed line rendering style. This style specifies that a line will be drawn as a series of long strokes, with
//     * space in between. <br>
//     * <pre><code>- - - - -</code></pre>
//     * <br> is an example of a dashed line.
//     */
//    public static final String LINE_STYLE_DASHED = GraticuleRenderingParams.VALUE_LINE_STYLE_DASHED;
//    /**
//     * Dotted line rendering style. This style specifies that a line will be drawn as a series of evenly spaced "square"
//     * dots. <br>
//     * <pre><code>. . . . .</code></pre>
//     * is an example of a dotted line.
//     */
//    public static final String LINE_STYLE_DOTTED = GraticuleRenderingParams.VALUE_LINE_STYLE_DOTTED;

    private static final String LOOK_AT_LATITUDE_PROPERTY = "look_at_latitude";
    private static final String LOOK_AT_LONGITUDE_PROPERTY = "look_at_longitude";
    private static final String GRATICULE_PIXEL_SIZE_PROPERTY = "graticule_pixel_size";
    private static final String GRATICULE_LABEL_OFFSET_PROPERTY = "graticule_label_offset";

    // Helper variables to avoid memory leaks
    private final Vec3 surfacePoint = new Vec3();
    private final Line forwardRay = new Line();
    private final Vec3 lookAtPoint = new Vec3();
    private final Position lookAtPos = new Position();
    private final float[] scratchHeights = new float[1];
    private final Sector scratchSector = new Sector();

    private final GraticuleSupport graticuleSupport = new GraticuleSupport();

    // Update reference states
    private final Vec3 lastCameraPoint = new Vec3();
    private double lastCameraHeading;
    private double lastCameraTilt;
    private double lastFOV;
    private double lastVerticalExaggeration;
//    private Globe lastGlobe;
//    private GeographicProjection lastProjection;
//    private long frameTimeStamp; // used only for 2D continuous globes to determine whether render is in same frame
//    private double terrainConformance = 50;

    AbstractGraticuleLayer(String name) {
        this.setDisplayName(name);
        this.setPickEnabled(false);
        this.initRenderingParams();
    }

    protected abstract void initRenderingParams();

    /**
     * Returns whether or not graticule lines will be rendered.
     *
     * @param key the rendering parameters key.
     *
     * @return true if graticule lines will be rendered; false otherwise.
     */
    public boolean isDrawGraticule(String key) {
        return this.getRenderingParams(key).isDrawLines();
    }

    /**
     * Sets whether or not graticule lines will be rendered.
     *
     * @param drawGraticule true to render graticule lines; false to disable rendering.
     * @param key           the rendering parameters key.
     */
    public void setDrawGraticule(boolean drawGraticule, String key) {
        this.getRenderingParams(key).setDrawLines(drawGraticule);
    }

    /**
     * Returns the graticule line Color.
     *
     * @param key the rendering parameters key.
     *
     * @return Color used to render graticule lines.
     */
    public Color getGraticuleLineColor(String key) {
        return this.getRenderingParams(key).getLineColor();
    }

    /**
     * Sets the graticule line Color.
     *
     * @param color Color that will be used to render graticule lines.
     * @param key   the rendering parameters key.
     */
    public void setGraticuleLineColor(Color color, String key) {
        this.getRenderingParams(key).setLineColor(color);
    }

    /**
     * Returns the graticule line width.
     *
     * @param key the rendering parameters key.
     *
     * @return width of the graticule lines.
     */
    public double getGraticuleLineWidth(String key) {
        return this.getRenderingParams(key).getLineWidth();
    }

    /**
     * Sets the graticule line width.
     *
     * @param lineWidth width of the graticule lines.
     * @param key       the rendering parameters key.
     */
    public void setGraticuleLineWidth(double lineWidth, String key) {
        this.getRenderingParams(key).setLineWidth(lineWidth);
    }

//    /**
//     * Returns the graticule line rendering style.
//     *
//     * @param key the rendering parameters key.
//     *
//     * @return rendering style of the graticule lines.
//     */
//    public String getGraticuleLineStyle(String key) {
//        return this.getRenderingParams(key).getLineStyle();
//    }
//
//    /**
//     * Sets the graticule line rendering style.
//     *
//     * @param lineStyle rendering style of the graticule lines. One of LINE_STYLE_SOLID, LINE_STYLE_DASHED, or
//     *                  LINE_STYLE_DOTTED.
//     * @param key       the rendering parameters key.
//     */
//    public void setGraticuleLineStyle(String lineStyle, String key) {
//        this.getRenderingParams(key).setLineStyle(lineStyle);
//    }

    /**
     * Returns whether or not graticule labels will be rendered.
     *
     * @param key the rendering parameters key.
     *
     * @return true if graticule labels will be rendered; false otherwise.
     */
    public boolean isDrawLabels(String key) {
        return this.getRenderingParams(key).isDrawLabels();
    }

    /**
     * Sets whether or not graticule labels will be rendered.
     *
     * @param drawLabels true to render graticule labels; false to disable rendering.
     * @param key        the rendering parameters key.
     */
    public void setDrawLabels(boolean drawLabels, String key) {
        this.getRenderingParams(key).setDrawLabels(drawLabels);
    }

    /**
     * Returns the graticule label Color.
     *
     * @param key the rendering parameters key.
     *
     * @return Color used to render graticule labels.
     */
    public Color getLabelColor(String key) {
        return this.getRenderingParams(key).getLabelColor();
    }

    /**
     * Sets the graticule label Color.
     *
     * @param color Color that will be used to render graticule labels.
     * @param key   the rendering parameters key.
     */
    public void setLabelColor(Color color, String key) {
        this.getRenderingParams(key).setLabelColor(color);
    }

    /**
     * Returns the Typeface used for graticule labels.
     *
     * @param key the rendering parameters key.
     *
     * @return Typeface used to render graticule labels.
     */
    public Typeface getLabelTypeface(String key) {
        return this.getRenderingParams(key).getLabelTypeface();
    }

    /**
     * Sets the Typeface used for graticule labels.
     *
     * @param typeface Typeface that will be used to render graticule labels.
     * @param key  the rendering parameters key.
     */
    public void setLabelTypeface(Typeface typeface, String key) {
        this.getRenderingParams(key).setLabelTypeface(typeface);
    }

    /**
     * Returns the Size used for graticule labels.
     *
     * @param key the rendering parameters key.
     *
     * @return Size used to render graticule labels.
     */
    public Float getLabelSize(String key) {
        return this.getRenderingParams(key).getLabelSize();
    }

    /**
     * Sets the Size used for graticule labels.
     *
     * @param size Size that will be used to render graticule labels.
     * @param key  the rendering parameters key.
     */
    public void setLabelSize(Float size, String key) {
        this.getRenderingParams(key).setLabelSize(size);
    }

    GraticuleRenderingParams getRenderingParams(String key) {
        return this.graticuleSupport.getRenderingParams(key);
    }

    void setRenderingParams(String key, GraticuleRenderingParams renderingParams) {
        this.graticuleSupport.setRenderingParams(key, renderingParams);
    }

    void addRenderable(Renderable renderable, String paramsKey) {
        this.graticuleSupport.addRenderable(renderable, paramsKey);
    }

    private void removeAllRenderables() {
        this.graticuleSupport.removeAllRenderables();
    }

    @Override
    public void doRender(RenderContext rc) {
//        if (rc.isContinuous2DGlobe()) {
//            if (this.needsToUpdate(rc)) {
//                this.clear(rc);
//                this.selectRenderables(rc);
//            }
//
//            // If the frame time stamp is the same, then this is the second or third pass of the same frame. We continue
//            // selecting renderables in these passes.
//            if (rc.getFrameTimeStamp() == this.frameTimeStamp)
//                this.selectRenderables(rc);
//
//            this.frameTimeStamp = rc.getFrameTimeStamp();
//        } else {
        if (this.needsToUpdate(rc)) {
            this.clear(rc);
            this.selectRenderables(rc);
        }
//        }

        // Render
        this.graticuleSupport.render(rc, this.getOpacity());
    }

    /**
     * Select the visible grid elements
     *
     * @param rc the current <code>RenderContext</code>.
     */
    protected abstract void selectRenderables(RenderContext rc);

    protected abstract List<String> getOrderedTypes();

    protected abstract String getTypeFor(double resolution);

    /**
     * Determines whether the grid should be updated. It returns true if: <ul> <li>the eye has moved more than 1% of its
     * altitude above ground <li>the view FOV, heading or pitch have changed more than 1 degree <li>vertical
     * exaggeration has changed </ul
     *
     * @param rc the current <code>RenderContext</code>.
     *
     * @return true if the graticule should be updated.
     */
    @SuppressWarnings({"RedundantIfStatement"})
    private boolean needsToUpdate(RenderContext rc) {
        if (this.lastVerticalExaggeration != rc.verticalExaggeration)
            return true;

        if (Math.abs(this.lastCameraHeading - rc.camera.heading) > 1)
            return true;

        if (Math.abs(this.lastCameraTilt - rc.camera.tilt) > 1)
            return true;

        if (Math.abs(this.lastFOV - rc.fieldOfView) > 1)
            return true;

        if (rc.cameraPoint.distanceTo(this.lastCameraPoint) > computeAltitudeAboveGround(rc) / 100)  // 1% of AAG
            return true;

        // We must test the globe and its projection to see if either changed. We can't simply use the globe state
        // key for this because we don't want a 2D globe offset change to cause an update. Offset changes don't
        // invalidate the current set of renderables.

//        if (rc.globe != this.lastGlobe)
//            return true;

//        if (rc.is2DGlobe())
//            if (((Globe2D) rc.getGlobe()).getProjection() != this.lastProjection)
//                return true;

        return false;
    }

    protected void clear(RenderContext rc) {
        this.removeAllRenderables();
        this.lastCameraPoint.set(rc.cameraPoint);
        this.lastFOV = rc.fieldOfView;
        this.lastCameraHeading = rc.camera.heading;
        this.lastCameraTilt = rc.camera.tilt;
        this.lastVerticalExaggeration = rc.verticalExaggeration;
//        this.lastGlobe = rc.globe;
//        if (rc.is2DGlobe())
//            this.lastProjection = ((Globe2D) rc.getGlobe()).getProjection();
//        this.terrainConformance = this.computeTerrainConformance(rc);
//        this.applyTerrainConformance();
    }

//    private double computeTerrainConformance(RenderContext rc) {
//        int value = 100;
//        double alt = rc.camera.altitude;
//        if (alt < 10e3)
//            value = 20;
//        else if (alt < 50e3)
//            value = 30;
//        else if (alt < 100e3)
//            value = 40;
//        else if (alt < 1000e3)
//            value = 60;
//
//        return value;
//    }
//
//    private void applyTerrainConformance() {
//        String[] graticuleType = getOrderedTypes();
//        for (String type : graticuleType) {
//            getRenderingParams(type).put(
//                GraticuleRenderingParams.KEY_LINE_CONFORMANCE, this.terrainConformance);
//        }
//    }

    Location computeLabelOffset(RenderContext rc) {
        if(this.hasLookAtPos(rc)) {
            double labelOffsetDegrees = this.getLabelOffset(rc);
            Location labelPos = Location.fromDegrees(this.getLookAtLatitude(rc) - labelOffsetDegrees,
                    this.getLookAtLongitude(rc) - labelOffsetDegrees);
            labelPos.set(WWMath.clamp(Location.normalizeLatitude(labelPos.latitude), -70, 70),
                    Location.normalizeLongitude(labelPos.longitude));
            return labelPos;
        } else {
            return Location.fromDegrees(rc.camera.latitude, rc.camera.longitude);
        }
    }

    Renderable createLineRenderable(List<Position> positions, int pathType) {
        Path path = new Path(positions);
        path.setPathType(pathType);
        path.setFollowTerrain(true);
        // path.setTerrainConformance(1); // WTF Why not this.terrainConformance?
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        return path;
    }

    Renderable createTextRenderable(Position position, String label, double resolution) {
        Label text = new Label(position, label).setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        //text.setPriority(resolution * 1e6);
        return text;
    }

    boolean hasLookAtPos(RenderContext rc) {
        calculateLookAtProperties(rc);
        return rc.getUserProperty(LOOK_AT_LATITUDE_PROPERTY) != null && rc.getUserProperty(LOOK_AT_LONGITUDE_PROPERTY) != null;
    }

    double getLookAtLatitude(RenderContext rc) {
        calculateLookAtProperties(rc);
        return (double) rc.getUserProperty(LOOK_AT_LATITUDE_PROPERTY);
    }

    double getLookAtLongitude(RenderContext rc) {
        calculateLookAtProperties(rc);
        return (double) rc.getUserProperty(LOOK_AT_LONGITUDE_PROPERTY);
    }

    double getPixelSize(RenderContext rc) {
        calculateLookAtProperties(rc);
        return (double) rc.getUserProperty(GRATICULE_PIXEL_SIZE_PROPERTY);
    }

    double getLabelOffset(RenderContext rc) {
        calculateLookAtProperties(rc);
        return (double) rc.getUserProperty(GRATICULE_LABEL_OFFSET_PROPERTY);
    }

    Vec3 getSurfacePoint(RenderContext rc, double latitude, double longitude) {
        if (!rc.terrain.surfacePoint(latitude, longitude, surfacePoint))
            rc.globe.geographicToCartesian(latitude, longitude,
                getElevation(rc, latitude, longitude), surfacePoint);

        return surfacePoint;
    }

    double computeAltitudeAboveGround(RenderContext rc) {
        Vec3 surfacePoint = getSurfacePoint(rc, rc.camera.latitude, rc.camera.longitude);
        return rc.cameraPoint.distanceTo(surfacePoint);
    }

    void computeTruncatedSegment(Position p1, Position p2, Sector sector, List<Position> positions) {
        if (p1 == null || p2 == null)
            return;

        boolean p1In = sector.contains(p1.latitude, p1.longitude);
        boolean p2In = sector.contains(p2.latitude, p2.longitude);
        if (!p1In && !p2In) {
            // whole segment is (likely) outside
            return;
        }
        if (p1In && p2In) {
            // whole segment is (likely) inside
            positions.add(p1);
            positions.add(p2);
        } else {
            // segment does cross the boundary
            Position outPoint = !p1In ? p1 : p2;
            Position inPoint = p1In ? p1 : p2;
            for (int i = 1; i <= 2; i++) { // there may be two intersections
                Location intersection = null;
                if (outPoint.longitude > sector.maxLongitude()
                    || (sector.maxLongitude() == 180 && outPoint.longitude < 0))
                {
                    // intersect with east meridian
                    intersection = this.greatCircleIntersectionAtLongitude(
                        inPoint, outPoint, sector.maxLongitude());
                } else if (outPoint.longitude < sector.minLongitude()
                    || (sector.minLongitude() == -180 && outPoint.longitude > 0)) {
                    // intersect with west meridian
                    intersection = this.greatCircleIntersectionAtLongitude(
                        inPoint, outPoint, sector.minLongitude());
                } else if (outPoint.latitude > sector.maxLatitude()) {
                    // intersect with top parallel
                    intersection = this.greatCircleIntersectionAtLatitude(
                        inPoint, outPoint, sector.maxLatitude());
                } else if (outPoint.latitude < sector.minLatitude()) {
                    // intersect with bottom parallel
                    intersection = this.greatCircleIntersectionAtLatitude(
                        inPoint, outPoint, sector.minLatitude());
                }
                if (intersection != null)
                    outPoint = new Position(intersection.latitude, intersection.longitude, outPoint.altitude);
                else
                    break;
            }
            positions.add(inPoint);
            positions.add(outPoint);
        }
    }

    /**
     * Computes the intersection point position between a great circle segment and a meridian.
     *
     * @param p1        the great circle segment start position.
     * @param p2        the great circle segment end position.
     * @param longitude the meridian longitude <code>Angle</code>
     *
     * @return the intersection <code>Position</code> or null if there was no intersection found.
     */
    private Location greatCircleIntersectionAtLongitude(Location p1, Location p2, double longitude) {
        if (p1.longitude == longitude)
            return p1;
        if (p2.longitude == longitude)
            return p2;
        Location pos = null;
        double deltaLon = this.getDeltaLongitude(p1, p2.longitude);
        if (this.getDeltaLongitude(p1, longitude) < deltaLon && this.getDeltaLongitude(p2, longitude) < deltaLon) {
            int count = 0;
            double precision = 1d / 6378137d; // 1m angle in radians
            Location a = p1;
            Location b = p2;
            Location midPoint = this.greatCircleMidPoint(a, b);
            while (Math.toRadians(this.getDeltaLongitude(midPoint, longitude)) > precision && count <= 20) {
                count++;
                if (this.getDeltaLongitude(a, longitude) < this.getDeltaLongitude(b, longitude))
                    b = midPoint;
                else
                    a = midPoint;
                midPoint = this.greatCircleMidPoint(a, b);
            }
            pos = midPoint;
        }
        // Adjust final longitude for an exact match
        if (pos != null)
            pos = new Location(pos.latitude, longitude);
        return pos;
    }

    /**
     * Computes the intersection point position between a great circle segment and a parallel.
     *
     * @param p1       the great circle segment start position.
     * @param p2       the great circle segment end position.
     * @param latitude the parallel latitude <code>Angle</code>
     *
     * @return the intersection <code>Position</code> or null if there was no intersection found.
     */
    private Location greatCircleIntersectionAtLatitude(Location p1, Location p2, double latitude) {
        Location pos = null;
        if (Math.signum(p1.latitude - latitude) != Math.signum(p2.latitude - latitude)) {
            int count = 0;
            double precision = 1d / 6378137d; // 1m angle in radians
            Location a = p1;
            Location b = p2;
            Location midPoint = this.greatCircleMidPoint(a, b);
            while (Math.abs(Math.toRadians(midPoint.latitude) - Math.toRadians(latitude)) > precision && count <= 20) {
                count++;
                if (Math.signum(a.latitude - latitude)
                    != Math.signum(midPoint.latitude - latitude))
                    b = midPoint;
                else
                    a = midPoint;
                midPoint = this.greatCircleMidPoint(a, b);
            }
            pos = midPoint;
        }
        // Adjust final latitude for an exact match
        if (pos != null)
            pos = new Location(latitude, pos.longitude);
        return pos;
    }

    private Location greatCircleMidPoint(Location p1, Location p2) {
        double azimuth = p1.greatCircleAzimuth(p2);
        double distance = p1.greatCircleDistance(p2);
        return p1.greatCircleLocation(azimuth, distance / 2, new Location());
    }

    private double getDeltaLongitude(Location p1, double longitude) {
        double deltaLon = Math.abs(p1.longitude - longitude);
        return deltaLon < 180 ? deltaLon : 360 - deltaLon;
    }

    // TODO Use rc.globe.getElevationAtLocation(latitude, longitude) when it will be merged to develop
    private double getElevation(RenderContext rc, double latitude, double longitude) {
        this.scratchSector.set(latitude, longitude, 1E-15, 1E-15);
        rc.globe.getElevationModel().getHeightGrid(this.scratchSector, 1, 1, this.scratchHeights);
        return this.scratchHeights[0];
    }

    private void calculateLookAtProperties(RenderContext rc) {
        if (!rc.hasUserProperty(LOOK_AT_LATITUDE_PROPERTY) || !rc.hasUserProperty(LOOK_AT_LONGITUDE_PROPERTY)) {
            //rc.modelview.extractEyePoint(forwardRay.origin);
            forwardRay.origin.set(rc.cameraPoint);
            rc.modelview.extractForwardVector(forwardRay.direction);

            double range;
            if (rc.globe.intersect(forwardRay, lookAtPoint)) {
                rc.globe.cartesianToGeographic(lookAtPoint.x, lookAtPoint.y, lookAtPoint.z, lookAtPos);
                rc.putUserProperty(LOOK_AT_LATITUDE_PROPERTY, lookAtPos.latitude);
                rc.putUserProperty(LOOK_AT_LONGITUDE_PROPERTY, lookAtPos.longitude);
                range = lookAtPoint.distanceTo(rc.cameraPoint);
            } else {
                rc.putUserProperty(LOOK_AT_LATITUDE_PROPERTY, null);
                rc.putUserProperty(LOOK_AT_LONGITUDE_PROPERTY, null);
                range = rc.horizonDistance;
            }

            double pixelSizeMeters = rc.pixelSizeAtDistance(range);
            rc.putUserProperty(GRATICULE_PIXEL_SIZE_PROPERTY, pixelSizeMeters);

            double pixelSizeDegrees = Math.toDegrees(pixelSizeMeters / rc.globe.getEquatorialRadius());
            rc.putUserProperty(GRATICULE_LABEL_OFFSET_PROPERTY, pixelSizeDegrees * rc.viewport.width / 4);
        }
    }

}
