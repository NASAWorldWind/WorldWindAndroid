package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.BoundingBox;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.RenderContext;

abstract class AbstractGraticuleTile {

    private final AbstractGraticuleLayer layer;
    private final Sector sector;

    private List<GridElement> gridElements;

    private BoundingBox extent;
    private float[] heightLimits;
    private long heightLimitsTimestamp;
    private double extentExaggeration;

    AbstractGraticuleTile(AbstractGraticuleLayer layer, Sector sector) {
        this.layer = layer;
        this.sector = sector;
    }

    AbstractGraticuleLayer getLayer() {
        return this.layer;
    }

    Sector getSector() {
        return this.sector;
    }

    List<GridElement> getGridElements() {
        return this.gridElements;
    }

    boolean isInView(RenderContext rc) {
        return this.getExtent(rc).intersectsFrustum(rc.frustum);
    }

    double getSizeInPixels(RenderContext rc) {
        Vec3 centerPoint = layer.getSurfacePoint(rc, this.sector.centroidLatitude(), this.sector.centroidLongitude());
        double distance = rc.cameraPoint.distanceTo(centerPoint);
        double tileSizeMeter = Math.toRadians(this.sector.deltaLatitude()) * rc.globe.getEquatorialRadius();
        return tileSizeMeter / rc.pixelSizeAtDistance(distance) / rc.resources.getDisplayMetrics().density;
    }

    void selectRenderables(RenderContext rc) {
        if (this.gridElements == null)
            this.createRenderables();
    }

    void clearRenderables() {
        if (this.gridElements != null) {
            this.gridElements.clear();
            this.gridElements = null;
        }
    }

    void createRenderables() {
        this.gridElements = new ArrayList<>();
    }

    Sector[] subdivide(int div) {
        double dLat = this.getSector().deltaLatitude() / div;
        double dLon = this.getSector().deltaLongitude() / div;

        Sector[] sectors = new Sector[div * div];
        int idx = 0;
        for (int row = 0; row < div; row++) {
            for (int col = 0; col < div; col++) {
                sectors[idx++] = Sector.fromDegrees(this.getSector().minLatitude() + dLat * row,
                        this.getSector().minLongitude() + dLon * col, dLat, dLon);
            }
        }

        return sectors;
    }

    private BoundingBox getExtent(RenderContext rc) {
        if (this.heightLimits == null) {
            this.heightLimits = new float[2];
        }

        if (this.extent == null) {
            this.extent = new BoundingBox();
        }

        long elevationTimestamp = rc.globe.getElevationModel().getTimestamp();
        if (elevationTimestamp != this.heightLimitsTimestamp) {
            // initialize the heights for elevation model scan
            this.heightLimits[0] = Float.MAX_VALUE;
            this.heightLimits[1] = -Float.MAX_VALUE;
            rc.globe.getElevationModel().getHeightLimits(this.sector, this.heightLimits);
            // check for valid height limits
            if (this.heightLimits[0] > this.heightLimits[1]) {
                Arrays.fill(this.heightLimits, 0f);
            }
        }

        double verticalExaggeration = rc.verticalExaggeration;
        if (verticalExaggeration != this.extentExaggeration ||
                elevationTimestamp != this.heightLimitsTimestamp) {
            float minHeight = (float) (this.heightLimits[0] * verticalExaggeration);
            float maxHeight = (float) (this.heightLimits[1] * verticalExaggeration);
            this.extent.setToSector(this.sector, rc.globe, minHeight, maxHeight);
        }

        this.heightLimitsTimestamp = elevationTimestamp;
        this.extentExaggeration = verticalExaggeration;

        return this.extent;
    }

}
