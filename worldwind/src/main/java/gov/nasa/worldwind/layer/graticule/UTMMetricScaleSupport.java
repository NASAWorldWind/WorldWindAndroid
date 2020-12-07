package gov.nasa.worldwind.layer.graticule;

import android.support.annotation.NonNull;

import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.geom.coords.UPSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

class UTMMetricScaleSupport {

    private class UTMExtremes {
        double minX, maxX, minY, maxY;
        Hemisphere minYHemisphere, maxYHemisphere;

        UTMExtremes() {
            this.clear();
        }

        void clear() {
            minX = 1e6;
            maxX = 0;
            minY = 10e6;
            maxY = 0;
            minYHemisphere = Hemisphere.N;
            maxYHemisphere = Hemisphere.S;
        }
    }

    private static final double OFFSET_FACTOR_X = -.5;
    private static final double OFFSET_FACTOR_Y = -.5;
    private static final double VISIBLE_DISTANCE_FACTOR = 10;

    private final AbstractUTMGraticuleLayer layer;

    private int scaleModulo = (int) 10e6;
    private double maxResolution = 1e5;
    private int zone;

    // 5 levels 100km to 10m
    private UTMExtremes[] extremes;

    UTMMetricScaleSupport(AbstractUTMGraticuleLayer layer) {
        this.layer = layer;
    }

    void setScaleModulo(int modulo) {
        this.scaleModulo = modulo;
    }

    void setMaxResolution(double maxResolution) {
        this.maxResolution = maxResolution;
        this.clear();
    }

    int getZone() {
        return this.zone;
    }

    void computeZone(RenderContext rc) {
        try {
            if(layer.hasLookAtPos(rc)) {
                double latitude = layer.getLookAtLatitude(rc);
                double longitude = layer.getLookAtLongitude(rc);
                if (latitude <= AbstractUTMGraticuleLayer.UTM_MAX_LATITUDE
                        && latitude >= AbstractUTMGraticuleLayer.UTM_MIN_LATITUDE) {
                    UTMCoord UTM = UTMCoord.fromLatLon(latitude, longitude);
                    this.zone = UTM.getZone();
                } else
                    this.zone = 0;
            }
        } catch (Exception ex) {
            this.zone = 0;
        }
    }

    void clear() {
        int numLevels = (int) Math.log10(this.maxResolution);
        this.extremes = new UTMExtremes[numLevels];
        for (int i = 0; i < numLevels; i++) {
            this.extremes[i] = new UTMExtremes();
            this.extremes[i].clear();
        }
    }

    void computeMetricScaleExtremes(int UTMZone, Hemisphere hemisphere, GridElement ge, double size) {
        if (UTMZone != this.zone)
            return;
        if (size < 1 || size > this.maxResolution)
            return;

        UTMExtremes levelExtremes = this.extremes[(int) Math.log10(size) - 1];

        if (ge.type.equals(GridElement.TYPE_LINE_EASTING)
            || ge.type.equals(GridElement.TYPE_LINE_EAST)
            || ge.type.equals(GridElement.TYPE_LINE_WEST)) {
            levelExtremes.minX = ge.value < levelExtremes.minX ? ge.value : levelExtremes.minX;
            levelExtremes.maxX = ge.value > levelExtremes.maxX ? ge.value : levelExtremes.maxX;
        } else if (ge.type.equals(GridElement.TYPE_LINE_NORTHING)
            || ge.type.equals(GridElement.TYPE_LINE_SOUTH)
            || ge.type.equals(GridElement.TYPE_LINE_NORTH)) {
            if (hemisphere.equals(levelExtremes.minYHemisphere))
                levelExtremes.minY = ge.value < levelExtremes.minY ? ge.value : levelExtremes.minY;
            else if (hemisphere.equals(Hemisphere.S)) {
                levelExtremes.minY = ge.value;
                levelExtremes.minYHemisphere = hemisphere;
            }
            if (hemisphere.equals(levelExtremes.maxYHemisphere))
                levelExtremes.maxY = ge.value > levelExtremes.maxY ? ge.value : levelExtremes.maxY;
            else if (hemisphere.equals(Hemisphere.N)) {
                levelExtremes.maxY = ge.value;
                levelExtremes.maxYHemisphere = hemisphere;
            }
        }
    }

    void selectRenderables(RenderContext rc) {
        if(!layer.hasLookAtPos(rc)) {
            return;
        }

        // Compute easting and northing label offsets
        double pixelSize = layer.getPixelSize(rc);
        double eastingOffset = rc.viewport.width * pixelSize * OFFSET_FACTOR_X / 2;
        double northingOffset = rc.viewport.height * pixelSize * OFFSET_FACTOR_Y / 2;
        // Derive labels center pos from the view center
        double labelEasting;
        double labelNorthing;
        Hemisphere labelHemisphere;
        if (this.zone > 0) {
            UTMCoord UTM = UTMCoord.fromLatLon(layer.getLookAtLatitude(rc), layer.getLookAtLongitude(rc));
            labelEasting = UTM.getEasting() + eastingOffset;
            labelNorthing = UTM.getNorthing() + northingOffset;
            labelHemisphere = UTM.getHemisphere();
            if (labelNorthing < 0) {
                labelNorthing = 10e6 + labelNorthing;
                labelHemisphere = Hemisphere.S;
            }
        } else {
            UPSCoord UPS = UPSCoord.fromLatLon(layer.getLookAtLatitude(rc), layer.getLookAtLongitude(rc));
            labelEasting = UPS.getEasting() + eastingOffset;
            labelNorthing = UPS.getNorthing() + northingOffset;
            labelHemisphere = UPS.getHemisphere();
        }

        Frustum viewFrustum = rc.frustum;

        Position labelPos;
        for (int i = 0; i < this.extremes.length; i++) {
            UTMExtremes levelExtremes = this.extremes[i];
            double gridStep = Math.pow(10, i);
            double gridStepTimesTen = gridStep * 10;
            String graticuleType = layer.getTypeFor(gridStep);
            if (levelExtremes.minX <= levelExtremes.maxX) {
                // Process easting scale labels for this level
                for (double easting = levelExtremes.minX; easting <= levelExtremes.maxX; easting += gridStep) {
                    // Skip multiples of ten grid steps except for last (higher) level
                    if (i == this.extremes.length - 1 || easting % gridStepTimesTen != 0) {
                        labelPos = layer.computePosition(this.zone, labelHemisphere, easting, labelNorthing);
                        if (labelPos == null)
                            continue;
                        double lat = labelPos.latitude;
                        double lon = labelPos.longitude;
                        Vec3 surfacePoint = layer.getSurfacePoint(rc, lat, lon);
                        if (viewFrustum.containsPoint(surfacePoint) && isPointInRange(rc, surfacePoint)) {
                            String text = String.valueOf((int) (easting % this.scaleModulo));
                            Renderable gt = this.layer.createTextRenderable(Position.fromDegrees(lat, lon, 0), text, gridStepTimesTen);
                            layer.addRenderable(gt, graticuleType);
                        }
                    }
                }
            }
            if (!(levelExtremes.maxYHemisphere.equals(Hemisphere.S) && levelExtremes.maxY == 0)) {
                // Process northing scale labels for this level
                Hemisphere currentHemisphere = levelExtremes.minYHemisphere;
                for (double northing = levelExtremes.minY; (northing <= levelExtremes.maxY)
                        || !currentHemisphere.equals(levelExtremes.maxYHemisphere); northing += gridStep) {
                    // Skip multiples of ten grid steps except for last (higher) level
                    if (i == this.extremes.length - 1 || northing % gridStepTimesTen != 0) {
                        labelPos = layer.computePosition(this.zone, currentHemisphere, labelEasting, northing);
                        if (labelPos == null)
                            continue;
                        double lat = labelPos.latitude;
                        double lon = labelPos.longitude;
                        Vec3 surfacePoint = layer.getSurfacePoint(rc, lat, lon);
                        if (viewFrustum.containsPoint(surfacePoint) && isPointInRange(rc, surfacePoint)) {
                            String text = String.valueOf((int) (northing % this.scaleModulo));
                            Renderable gt = this.layer.createTextRenderable(Position.fromDegrees(lat, lon, 0), text, gridStepTimesTen);
                            layer.addRenderable(gt, graticuleType);
                        }

                        if (!currentHemisphere.equals(levelExtremes.maxYHemisphere)
                                && northing >= 10e6 - gridStep) {
                            // Switch hemisphere
                            currentHemisphere = levelExtremes.maxYHemisphere;
                            northing = -gridStep;
                        }
                    }
                }
            } // end northing
        } // for levels
    }

    private boolean isPointInRange(RenderContext rc, Vec3 point) {
        double altitudeAboveGround = layer.computeAltitudeAboveGround(rc);
        return rc.cameraPoint.distanceTo(point) < altitudeAboveGround * VISIBLE_DISTANCE_FACTOR;
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("level ");
            sb.append(String.valueOf(i));
            sb.append(" : ");
            UTMExtremes levelExtremes = this.extremes[i];
            if (levelExtremes.minX < levelExtremes.maxX ||
                !(levelExtremes.maxYHemisphere.equals(Hemisphere.S) && levelExtremes.maxY == 0)) {
                sb.append(levelExtremes.minX);
                sb.append(", ");
                sb.append(levelExtremes.maxX);
                sb.append(" - ");
                sb.append(levelExtremes.minY);
                sb.append(levelExtremes.minYHemisphere);
                sb.append(", ");
                sb.append(levelExtremes.maxY);
                sb.append(levelExtremes.maxYHemisphere);
            } else {
                sb.append("empty");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
