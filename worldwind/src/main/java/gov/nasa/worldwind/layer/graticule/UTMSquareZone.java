package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

/** Represent a 100km square zone inside an UTM zone. */
class UTMSquareZone extends UTMSquareSector {

    private String name;
    private UTMSquareGrid squareGrid;
    private UTMSquareZone northNeighbor, eastNeighbor;

    UTMSquareZone(AbstractUTMGraticuleLayer layer, int UTMZone, Hemisphere hemisphere, Sector UTMZoneSector,
                  double SWEasting, double SWNorthing, double size) {
        super(layer, UTMZone, hemisphere, UTMZoneSector, SWEasting, SWNorthing, size);
    }

    @Override
    boolean isInView(RenderContext rc) {
        return super.isInView(rc) && getSizeInPixels(rc) > MIN_CELL_SIZE_PIXELS;
    }

    void setName(String name) {
        this.name = name;
    }

    void setNorthNeighbor(UTMSquareZone sz) {
        this.northNeighbor = sz;
    }

    void setEastNeighbor(UTMSquareZone sz) {
        this.eastNeighbor = sz;
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        boolean drawMetricLabels = getSizeInPixels(rc) > MIN_CELL_SIZE_PIXELS * 2;
        String graticuleType = getLayer().getTypeFor(this.size);
        for (GridElement ge : this.getGridElements()) {
            if (ge.isInView(rc)) {
                if (ge.type.equals(GridElement.TYPE_LINE_NORTH) && this.isNorthNeighborInView(rc))
                    continue;
                if (ge.type.equals(GridElement.TYPE_LINE_EAST) && this.isEastNeighborInView(rc))
                    continue;

                if (drawMetricLabels)
                    getLayer().computeMetricScaleExtremes(this.UTMZone, this.hemisphere, ge,
                        this.size * 10);
                getLayer().addRenderable(ge.renderable, graticuleType);
            }
        }

        if (getSizeInPixels(rc) <= MIN_CELL_SIZE_PIXELS * 2)
            return;

        // Select grid renderables
        if (this.squareGrid == null)
            this.squareGrid = new UTMSquareGrid(getLayer(), this.UTMZone, this.hemisphere, this.UTMZoneSector, this.SWEasting,
                this.SWNorthing, this.size);

        if (this.squareGrid.isInView(rc))
            this.squareGrid.selectRenderables(rc);
        else
            this.squareGrid.clearRenderables();
    }

    private boolean isNorthNeighborInView(RenderContext rc) {
        return this.northNeighbor != null && this.northNeighbor.isInView(rc);
    }

    private boolean isEastNeighborInView(RenderContext rc) {
        return this.eastNeighbor != null && this.eastNeighbor.isInView(rc);
    }

    @Override
    void clearRenderables() {
        super.clearRenderables();
        if (this.squareGrid != null) {
            this.squareGrid.clearRenderables();
            this.squareGrid = null;
        }
    }

    @Override
    void createRenderables() {
        super.createRenderables();

        List<Position> positions = new ArrayList<>();
        Position p1, p2;
        Renderable polyline;
        Sector lineSector;

        // left segment
        positions.clear();
        if (this.isTruncated) {
            getLayer().computeTruncatedSegment(sw, nw, this.UTMZoneSector, positions);
        } else {
            positions.add(sw);
            positions.add(nw);
        }
        if (positions.size() > 0) {
            p1 = positions.get(0);
            p2 = positions.get(1);
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
            lineSector = boundingSector(p1, p2);
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_WEST, this.SWEasting));
        }

        // right segment
        positions.clear();
        if (this.isTruncated) {
            getLayer().computeTruncatedSegment(se, ne, this.UTMZoneSector, positions);
        } else {
            positions.add(se);
            positions.add(ne);
        }
        if (positions.size() > 0) {
            p1 = positions.get(0);
            p2 = positions.get(1);
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
            lineSector = boundingSector(p1, p2);
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EAST, this.SWEasting + this.size));
        }

        // bottom segment
        positions.clear();
        if (this.isTruncated) {
            getLayer().computeTruncatedSegment(sw, se, this.UTMZoneSector, positions);
        } else {
            positions.add(sw);
            positions.add(se);
        }
        if (positions.size() > 0) {
            p1 = positions.get(0);
            p2 = positions.get(1);
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
            lineSector = boundingSector(p1, p2);
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_SOUTH, this.SWNorthing));
        }

        // top segment
        positions.clear();
        if (this.isTruncated) {
            getLayer().computeTruncatedSegment(nw, ne, this.UTMZoneSector, positions);
        } else {
            positions.add(nw);
            positions.add(ne);
        }
        if (positions.size() > 0) {
            p1 = positions.get(0);
            p2 = positions.get(1);
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
            lineSector = boundingSector(p1, p2);
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTH, this.SWNorthing + this.size));
        }

        // Label
        if (this.name != null) {
            // Only add a label to squares above some dimension
            if (this.boundingSector.deltaLongitude() * Math.cos(Math.toRadians(this.centroid.latitude)) > .2
                && this.boundingSector.deltaLatitude() > .2) {
                Location labelPos = null;
                if (this.UTMZone != 0) { // Not at poles
                    labelPos = this.centroid;
                } else if (this.isPositionInside(Position.fromDegrees(this.squareCenter.latitude, this.squareCenter.longitude, 0))) {
                    labelPos = this.squareCenter;
                } else if (this.squareCenter.latitude <= this.UTMZoneSector.maxLatitude()
                    && this.squareCenter.latitude >= this.UTMZoneSector.minLatitude()) {
                    labelPos = this.centroid;
                }
                if (labelPos != null) {
                    Renderable text = this.getLayer().createTextRenderable(Position.fromDegrees(labelPos.latitude, labelPos.longitude, 0), this.name, this.size * 10);
                    this.getGridElements().add(new GridElement(this.boundingSector, text, GridElement.TYPE_GRIDZONE_LABEL));
                }
            }
        }
    }

}
