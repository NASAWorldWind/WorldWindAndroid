package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

/** Represent a square 10x10 grid and recursive tree in easting/northing coordinates */
class UTMSquareGrid extends UTMSquareSector {

    private List<UTMSquareGrid> subGrids;

    UTMSquareGrid(AbstractUTMGraticuleLayer layer, int UTMZone, Hemisphere hemisphere, Sector UTMZoneSector,
                  double SWEasting, double SWNorthing, double size) {
        super(layer, UTMZone, hemisphere, UTMZoneSector, SWEasting, SWNorthing, size);
    }

    @Override
    boolean isInView(RenderContext rc) {
        return super.isInView(rc) && getSizeInPixels(rc) > MIN_CELL_SIZE_PIXELS * 4;
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        boolean drawMetricLabels = getSizeInPixels(rc) > MIN_CELL_SIZE_PIXELS * 4 * 1.7;
        String graticuleType = getLayer().getTypeFor(this.size / 10);

        for (GridElement ge : this.getGridElements()) {
            if (ge.isInView(rc)) {
                if (drawMetricLabels)
                    getLayer().computeMetricScaleExtremes(this.UTMZone, this.hemisphere, ge, this.size);

                getLayer().addRenderable(ge.renderable, graticuleType);
            }
        }

        if (getSizeInPixels(rc) <= MIN_CELL_SIZE_PIXELS * 4 * 2)
            return;

        // Select sub grids renderables
        if (this.subGrids == null)
            createSubGrids();

        for (UTMSquareGrid sg : this.subGrids) {
            if (sg.isInView(rc))
                sg.selectRenderables(rc);
            else
                sg.clearRenderables();
        }
    }

    @Override
    void clearRenderables() {
        super.clearRenderables();
        if (this.subGrids != null) {
            for (UTMSquareGrid sg : this.subGrids)
                sg.clearRenderables();
            this.subGrids.clear();
            this.subGrids = null;
        }
    }

    private void createSubGrids() {
        this.subGrids = new ArrayList<>();
        double gridStep = this.size / 10;
        for (int i = 0; i < 10; i++) {
            double easting = this.SWEasting + gridStep * i;
            for (int j = 0; j < 10; j++) {
                double northing = this.SWNorthing + gridStep * j;
                UTMSquareGrid sg = new UTMSquareGrid(this.getLayer(), this.UTMZone, this.hemisphere, this.UTMZoneSector,
                    easting, northing, gridStep);
                if (!sg.isOutsideGridZone())
                    this.subGrids.add(sg);
            }
        }
    }

    @Override
    void createRenderables() {
        super.createRenderables();
        double gridStep = this.size / 10;
        Position p1, p2;
        List<Position> positions = new ArrayList<>();

        // South-North lines
        for (int i = 1; i <= 9; i++) {
            double easting = this.SWEasting + gridStep * i;
            positions.clear();
            p1 = getLayer().computePosition(this.UTMZone, this.hemisphere, easting, SWNorthing);
            p2 = getLayer().computePosition(this.UTMZone, this.hemisphere, easting, SWNorthing + this.size);
            if (this.isTruncated) {
                getLayer().computeTruncatedSegment(p1, p2, this.UTMZoneSector, positions);
            } else {
                positions.add(p1);
                positions.add(p2);
            }
            if (positions.size() > 0) {
                p1 = positions.get(0);
                p2 = positions.get(1);
                Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
                Sector lineSector = boundingSector(p1, p2);
                this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EASTING, easting));
            }
        }
        // West-East lines
        for (int i = 1; i <= 9; i++) {
            double northing = this.SWNorthing + gridStep * i;
            positions.clear();
            p1 = getLayer().computePosition(this.UTMZone, this.hemisphere, SWEasting, northing);
            p2 = getLayer().computePosition(this.UTMZone, this.hemisphere, SWEasting + this.size, northing);
            if (this.isTruncated) {
                getLayer().computeTruncatedSegment(p1, p2, this.UTMZoneSector, positions);
            } else {
                positions.add(p1);
                positions.add(p2);
            }
            if (positions.size() > 0) {
                p1 = positions.get(0);
                p2 = positions.get(1);
                Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
                Sector lineSector = boundingSector(p1, p2);
                this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTHING, northing));
            }
        }
    }

}
