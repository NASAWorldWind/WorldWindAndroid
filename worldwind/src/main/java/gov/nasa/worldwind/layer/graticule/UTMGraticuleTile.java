package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

class UTMGraticuleTile extends AbstractGraticuleTile {

    private static final int MIN_CELL_SIZE_PIXELS = 40; // TODO: make settable

    private final int zone;
    private final Hemisphere hemisphere;

    private List<UTMSquareZone> squares;

    UTMGraticuleTile(UTMGraticuleLayer layer, Sector sector, int zone) {
        super(layer, sector);
        this.zone = zone;
        this.hemisphere = sector.centroidLatitude() > 0 ? Hemisphere.N : Hemisphere.S;
    }

    @Override
    UTMGraticuleLayer getLayer() {
        return (UTMGraticuleLayer) super.getLayer();
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        // Select tile grid elements
        String graticuleType = getLayer().getTypeFor(UTMGraticuleLayer.UTM_ZONE_RESOLUTION);
        for (GridElement ge : this.getGridElements())
            if (ge.isInView(rc))
                getLayer().addRenderable(ge.renderable, graticuleType);

        if (getSizeInPixels(rc) / 10 < MIN_CELL_SIZE_PIXELS * 2)
            return;

        // Select child elements
        if (this.squares == null)
            createSquares();

        for (UTMSquareZone sz : this.squares)
            if (sz.isInView(rc))
                sz.selectRenderables(rc);
            else
                sz.clearRenderables();
    }

    @Override
    void clearRenderables() {
        super.clearRenderables();
        if (this.squares != null) {
            for (UTMSquareZone sz : this.squares)
                sz.clearRenderables();
            this.squares.clear();
            this.squares = null;
        }
    }

    private void createSquares() {
        // Find grid zone easting and northing boundaries
        UTMCoord UTM;
        UTM = UTMCoord.fromLatLon(this.getSector().minLatitude(), this.getSector().centroidLongitude());
        double minNorthing = UTM.getNorthing();
        UTM = UTMCoord.fromLatLon(this.getSector().maxLatitude(), this.getSector().centroidLongitude());
        double maxNorthing = UTM.getNorthing();
        maxNorthing = maxNorthing == 0 ? 10e6 : maxNorthing;
        UTM = UTMCoord.fromLatLon(this.getSector().minLatitude(), this.getSector().minLongitude());
        double minEasting = UTM.getEasting();
        UTM = UTMCoord.fromLatLon(this.getSector().maxLatitude(), this.getSector().minLongitude());
        minEasting = Math.min(UTM.getEasting(), minEasting);
        double maxEasting = 1e6 - minEasting;

        // Create squares
        this.squares = getLayer().createSquaresGrid(this.zone, this.hemisphere, this.getSector(),
                minEasting, maxEasting, minNorthing, maxNorthing);
    }

    @Override
    void createRenderables() {
        super.createRenderables();

        List<Position> positions = new ArrayList<>();

        // Generate west meridian
        positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(), 0));
        positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().minLongitude(), 0));
        Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
        Sector lineSector = Sector.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(),
                this.getSector().deltaLatitude(), 1E-15);
        this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE, this.getSector().minLongitude()));

        // Generate south parallel at south pole and equator
        if (this.getSector().minLatitude() == UTMGraticuleLayer.UTM_MIN_LATITUDE || this.getSector().minLatitude() == 0) {
            positions.clear();
            positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(), 0));
            positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().maxLongitude(), 0));
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
            lineSector = Sector.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(),
                    1E-15, this.getSector().deltaLongitude());
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE, this.getSector().minLatitude()));
        }

        // Generate north parallel at north pole
        if (this.getSector().maxLatitude() == UTMGraticuleLayer.UTM_MAX_LATITUDE) {
            positions.clear();
            positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().minLongitude(), 0));
            positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().maxLongitude(), 0));
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
            lineSector = Sector.fromDegrees(this.getSector().maxLatitude(), this.getSector().minLongitude(),
                    1E-15, this.getSector().deltaLongitude());
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE, this.getSector().maxLatitude()));
        }

        // Add label
        if (this.hasLabel()) {
            Renderable text = this.getLayer().createTextRenderable(Position.fromDegrees(this.getSector().centroidLatitude(), this.getSector().centroidLongitude(), 0), String.valueOf(this.zone) + this.hemisphere, 10e6);
            this.getGridElements().add(new GridElement(this.getSector(), text, GridElement.TYPE_GRIDZONE_LABEL));
        }
    }

    private boolean hasLabel() {
        // Has label if it contains hemisphere mid latitude
        double southLat = UTMGraticuleLayer.UTM_MIN_LATITUDE / 2d;
        boolean southLabel = this.getSector().minLatitude() < southLat
            && southLat <= this.getSector().maxLatitude();

        double northLat = UTMGraticuleLayer.UTM_MAX_LATITUDE / 2d;
        boolean northLabel = this.getSector().minLatitude() < northLat
            && northLat <= this.getSector().maxLatitude();

        return southLabel || northLabel;
    }

}
