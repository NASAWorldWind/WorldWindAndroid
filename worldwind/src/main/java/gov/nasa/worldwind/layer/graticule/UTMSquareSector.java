package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.render.RenderContext;

/** Represent a generic UTM/UPS square area */
abstract class UTMSquareSector extends AbstractGraticuleTile {

    static final int MIN_CELL_SIZE_PIXELS = 50;

    final int UTMZone;
    final Hemisphere hemisphere;
    final Sector UTMZoneSector;
    final double SWEasting;
    final double SWNorthing;
    final double size;

    Position sw, se, nw, ne; // Four corners position
    Sector boundingSector;
    Location centroid;
    final Location squareCenter;
    final boolean isTruncated;

    UTMSquareSector(AbstractUTMGraticuleLayer layer, int UTMZone, Hemisphere hemisphere, Sector UTMZoneSector,
                    double SWEasting, double SWNorthing, double size) {
        super(layer, new Sector());
        this.UTMZone = UTMZone;
        this.hemisphere = hemisphere;
        this.UTMZoneSector = UTMZoneSector;
        this.SWEasting = SWEasting;
        this.SWNorthing = SWNorthing;
        this.size = size;

        // Compute corners positions
        this.sw = layer.computePosition(this.UTMZone, this.hemisphere, SWEasting, SWNorthing);
        this.se = layer.computePosition(this.UTMZone, this.hemisphere, SWEasting + size, SWNorthing);
        this.nw = layer.computePosition(this.UTMZone, this.hemisphere, SWEasting, SWNorthing + size);
        this.ne = layer.computePosition(this.UTMZone, this.hemisphere, SWEasting + size, SWNorthing + size);
        this.squareCenter = layer.computePosition(this.UTMZone, this.hemisphere,
                SWEasting + size / 2, SWNorthing + size / 2);

        // Compute approximate bounding sector and center point
        if (this.sw != null && this.se != null && this.nw != null && this.ne != null) {
            adjustDateLineCrossingPoints();
            this.boundingSector = boundingSector(Arrays.asList(sw, se, nw, ne));
            if (!isInsideGridZone())
                this.boundingSector.intersect(this.UTMZoneSector);

            this.centroid = this.boundingSector != null ? this.boundingSector.centroid(new Location()) : this.squareCenter;

            if(this.boundingSector != null) {
                this.getSector().set(this.boundingSector);
            }
        }

        // Check whether this square is truncated by the grid zone boundary
        this.isTruncated = !isInsideGridZone();
    }

    @Override
    AbstractUTMGraticuleLayer getLayer() {
        return (AbstractUTMGraticuleLayer) super.getLayer();
    }

    Sector boundingSector(Location pA, Location pB) {
        double minLat = pA.latitude;
        double minLon = pA.longitude;
        double maxLat = pA.latitude;
        double maxLon = pA.longitude;

        if (pB.latitude < minLat)
            minLat = pB.latitude;
        else if (pB.latitude > maxLat)
            maxLat = pB.latitude;

        if (pB.longitude < minLon)
            minLon = pB.longitude;
        else if (pB.longitude > maxLon)
            maxLon = pB.longitude;

        return Sector.fromDegrees(minLat, minLon, maxLat - minLat + 1E-15, maxLon - minLon + 1E-15);
    }

    private Sector boundingSector(Iterable<? extends Location> locations) {
        double minLat = 90;
        double minLon = 180;
        double maxLat = -90;
        double maxLon = -180;

        for (Location p : locations) {
            double lat = p.latitude;
            if (lat < minLat)
                minLat = lat;
            if (lat > maxLat)
                maxLat = lat;

            double lon = p.longitude;
            if (lon < minLon)
                minLon = lon;
            if (lon > maxLon)
                maxLon = lon;
        }

        return Sector.fromDegrees(minLat, minLon, maxLat - minLat + 1E-15, maxLon - minLon + 1E-15);
    }

    private void adjustDateLineCrossingPoints() {
        List<Position> corners = new ArrayList<>(Arrays.asList(sw, se, nw, ne));
        if (!locationsCrossDateLine(corners))
            return;

        double lonSign = 0;
        for (Location corner : corners) {
            if (Math.abs(corner.longitude) != 180)
                lonSign = Math.signum(corner.longitude);
        }

        if (lonSign == 0)
            return;

        if (Math.abs(sw.longitude) == 180 && Math.signum(sw.longitude) != lonSign)
            sw = Position.fromDegrees(sw.latitude, sw.longitude * -1, sw.altitude);
        if (Math.abs(se.longitude) == 180 && Math.signum(se.longitude) != lonSign)
            se = Position.fromDegrees(se.latitude, se.longitude * -1, se.altitude);
        if (Math.abs(nw.longitude) == 180 && Math.signum(nw.longitude) != lonSign)
            nw = Position.fromDegrees(nw.latitude, nw.longitude * -1, nw.altitude);
        if (Math.abs(ne.longitude) == 180 && Math.signum(ne.longitude) != lonSign)
            ne = Position.fromDegrees(ne.latitude, ne.longitude * -1, ne.altitude);
    }

    private boolean locationsCrossDateLine(Iterable<? extends Location> locations) {
        Location pos = null;
        for (Location posNext : locations) {
            if (pos != null) {
                // A segment cross the line if end pos have different longitude signs
                // and are more than 180 degrees longitude apart
                if (Math.signum(pos.longitude) != Math.signum(posNext.longitude)) {
                    double delta = Math.abs(pos.longitude - posNext.longitude);
                    if (delta > 180 && delta < 360)
                        return true;
                }
            }
            pos = posNext;
        }

        return false;
    }

    /**
     * Determines whether this square is fully inside its parent grid zone.
     *
     * @return true if this square is totaly inside its parent grid zone.
     */
    private boolean isInsideGridZone() {
        return this.isPositionInside(this.nw) && this.isPositionInside(this.ne)
                && this.isPositionInside(this.sw) && this.isPositionInside(this.se);
    }

    /**
     * Determines whether this square is fully outside its parent grid zone.
     *
     * @return true if this square is totaly outside its parent grid zone.
     */
    boolean isOutsideGridZone() {
        return !this.isPositionInside(this.nw) && !this.isPositionInside(this.ne)
                && !this.isPositionInside(this.sw) && !this.isPositionInside(this.se);
    }

    boolean isPositionInside(Location position) {
        return position != null && this.UTMZoneSector.contains(position.latitude, position.longitude);
    }

    @Override
    double getSizeInPixels(RenderContext rc) {
        Vec3 centerPoint = getLayer().getSurfacePoint(rc, this.centroid.latitude, this.centroid.longitude);
        double distance = rc.cameraPoint.distanceTo(centerPoint);
        return this.size / rc.pixelSizeAtDistance(distance) / rc.resources.getDisplayMetrics().density;
    }

}
