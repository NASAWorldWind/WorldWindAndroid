package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

class LatLonGraticuleTile extends AbstractGraticuleTile {

    private static final int MIN_CELL_SIZE_PIXELS = 40; // TODO: make settable

    private final int divisions;
    private final int level;

    private List<LatLonGraticuleTile> subTiles;

    LatLonGraticuleTile(LatLonGraticuleLayer layer, Sector sector, int divisions, int level) {
        super(layer, sector);
        this.divisions = divisions;
        this.level = level;
    }

    @Override
    LatLonGraticuleLayer getLayer() {
        return (LatLonGraticuleLayer) super.getLayer();
    }

    @Override
    boolean isInView(RenderContext rc) {
        return super.isInView(rc) && (this.level == 0 || getSizeInPixels(rc) / this.divisions >= MIN_CELL_SIZE_PIXELS);
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        Location labelOffset = getLayer().computeLabelOffset(rc);
        String graticuleType = getLayer().getTypeFor(this.getSector().deltaLatitude());
        if (this.level == 0) {
            for (GridElement ge : this.getGridElements()) {
                if (ge.isInView(rc)) {
                    // Add level zero bounding lines and labels
                    if (ge.type.equals(GridElement.TYPE_LINE_SOUTH) || ge.type.equals(GridElement.TYPE_LINE_NORTH)
                        || ge.type.equals(GridElement.TYPE_LINE_WEST)) {
                        getLayer().addRenderable(ge.renderable, graticuleType);
                        String labelType = ge.type.equals(GridElement.TYPE_LINE_SOUTH)
                            || ge.type.equals(GridElement.TYPE_LINE_NORTH) ?
                            GridElement.TYPE_LATITUDE_LABEL : GridElement.TYPE_LONGITUDE_LABEL;
                        getLayer().addLabel(ge.value, labelType, graticuleType, this.getSector().deltaLatitude(), labelOffset);
                    }
                }
            }
            if (getSizeInPixels(rc) / this.divisions < MIN_CELL_SIZE_PIXELS)
                return;
        }

        // Select tile grid elements
        double resolution = this.getSector().deltaLatitude() / this.divisions;
        graticuleType = getLayer().getTypeFor(resolution);
        for (GridElement ge : this.getGridElements()) {
            if (ge.isInView(rc)) {
                if (ge.type.equals(GridElement.TYPE_LINE)) {
                    getLayer().addRenderable(ge.renderable, graticuleType);
                    String labelType = ge.sector.deltaLatitude() < 1E-14 ?
                        GridElement.TYPE_LATITUDE_LABEL : GridElement.TYPE_LONGITUDE_LABEL;
                    getLayer().addLabel(ge.value, labelType, graticuleType, resolution, labelOffset);
                }
            }
        }

        if (getSizeInPixels(rc) / this.divisions < MIN_CELL_SIZE_PIXELS * 2)
            return;

        // Select child elements
        if (this.subTiles == null)
            createSubTiles();
        for (LatLonGraticuleTile gt : this.subTiles) {
            if (gt.isInView(rc)) {
                gt.selectRenderables(rc);
            } else
                gt.clearRenderables();
        }
    }

    @Override
    void clearRenderables() {
        super.clearRenderables();
        if (this.subTiles != null) {
            for (LatLonGraticuleTile gt : this.subTiles) {
                gt.clearRenderables();
            }
            this.subTiles.clear();
            this.subTiles = null;
        }
    }

    private void createSubTiles() {
        this.subTiles = new ArrayList<>();
        Sector[] sectors = this.subdivide(this.divisions);
        int subDivisions = 10;
        if ((getLayer().getAngleFormat().equals(LatLonGraticuleLayer.AngleFormat.DMS)
                || getLayer().getAngleFormat().equals(LatLonGraticuleLayer.AngleFormat.DM))
            && (this.level == 0 || this.level == 2))
            subDivisions = 6;
        for (Sector s : sectors) {
            this.subTiles.add(new LatLonGraticuleTile(getLayer(), s, subDivisions, this.level + 1));
        }
    }

    @Override
    void createRenderables() {
        super.createRenderables();

        double step = this.getSector().deltaLatitude() / this.divisions;

        // Generate meridians with labels
        double lon = this.getSector().minLongitude() + (this.level == 0 ? 0 : step);
        while (lon < this.getSector().maxLongitude() - step / 2) {
            double longitude = lon;
            // Meridian
            List<Position> positions = new ArrayList<>(2);
            positions.add(new Position(this.getSector().minLatitude(), longitude, 0));
            positions.add(new Position(this.getSector().maxLatitude(), longitude, 0));

            Renderable line = getLayer().createLineRenderable(positions, WorldWind.LINEAR);
            Sector sector = Sector.fromDegrees(
                this.getSector().minLatitude(), lon, this.getSector().deltaLatitude(), 1E-15);
            String lineType = lon == this.getSector().minLongitude() ?
                GridElement.TYPE_LINE_WEST : GridElement.TYPE_LINE;
            this.getGridElements().add(new GridElement(sector, line, lineType, lon));

            // Increase longitude
            lon += step;
        }

        // Generate parallels
        double lat = this.getSector().minLatitude() + (this.level == 0 ? 0 : step);
        while (lat < this.getSector().maxLatitude() - step / 2) {
            double latitude = lat;
            List<Position> positions = new ArrayList<>(2);
            positions.add(new Position(latitude, this.getSector().minLongitude(), 0));
            positions.add(new Position(latitude, this.getSector().maxLongitude(), 0));

            Renderable line = getLayer().createLineRenderable(positions, WorldWind.LINEAR);
            Sector sector = Sector.fromDegrees(
                lat, this.getSector().minLongitude(), 1E-15, this.getSector().deltaLongitude());
            String lineType = lat == this.getSector().minLatitude() ?
                GridElement.TYPE_LINE_SOUTH : GridElement.TYPE_LINE;
            this.getGridElements().add(new GridElement(sector, line, lineType, lat));

            // Increase latitude
            lat += step;
        }

        // Draw and label a parallel at the top of the graticule. The line is apparent only on 2D globes.
        if (this.getSector().maxLatitude() == 90) {
            List<Position> positions = new ArrayList<>(2);
            positions.add(new Position(90, this.getSector().minLongitude(), 0));
            positions.add(new Position(90, this.getSector().maxLongitude(), 0));

            Renderable line = getLayer().createLineRenderable(positions, WorldWind.LINEAR);
            Sector sector = Sector.fromDegrees(
                90, this.getSector().minLongitude(), 1E-15, this.getSector().deltaLongitude());
            this.getGridElements().add(new GridElement(sector, line, GridElement.TYPE_LINE_NORTH, 90));
        }
    }

}
