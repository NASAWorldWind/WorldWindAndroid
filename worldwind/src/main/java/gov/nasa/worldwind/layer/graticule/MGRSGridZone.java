package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

/** Represent a UTM zone / latitude band intersection */
class MGRSGridZone extends AbstractGraticuleTile {

    private static final double ONEHT = 100e3;
    private static final double TWOMIL = 2e6;
    private static final double SQUARE_MAX_ALTITUDE = 3000e3;

    private final boolean ups;
    private final String name;
    private final Hemisphere hemisphere;
    private final int zone;

    private List<UTMSquareZone> squares;

    MGRSGridZone(MGRSGraticuleLayer layer, Sector sector) {
        super(layer, sector);
        this.ups = (sector.maxLatitude() > MGRSGraticuleLayer.UTM_MAX_LATITUDE
                || sector.minLatitude() < MGRSGraticuleLayer.UTM_MIN_LATITUDE);
        MGRSCoord MGRS = MGRSCoord.fromLatLon(sector.centroidLatitude(), sector.centroidLongitude());
        if (this.ups) {
            this.name = MGRS.toString().substring(2, 3);
            this.hemisphere = sector.minLatitude() > 0 ? Hemisphere.N : Hemisphere.S;
            this.zone = 0;
        } else {
            this.name = MGRS.toString().substring(0, 3);
            UTMCoord UTM = UTMCoord.fromLatLon(sector.centroidLatitude(), sector.centroidLongitude());
            this.hemisphere = UTM.getHemisphere();
            this.zone = UTM.getZone();
        }
    }

    @Override
    MGRSGraticuleLayer getLayer() {
        return (MGRSGraticuleLayer) super.getLayer();
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        String graticuleType = getLayer().getTypeFor(MGRSGraticuleLayer.MGRS_GRID_ZONE_RESOLUTION);
        for (GridElement ge : this.getGridElements())
            if (ge.isInView(rc)) {
                if (ge.type.equals(GridElement.TYPE_LINE_NORTH) && this.getLayer().isNorthNeighborInView(this, rc))
                    continue;
                if (ge.type.equals(GridElement.TYPE_LINE_EAST) && this.getLayer().isEastNeighborInView(this, rc))
                    continue;

                getLayer().addRenderable(ge.renderable, graticuleType);
            }

        if (rc.camera.altitude > SQUARE_MAX_ALTITUDE)
            return;

        // Select 100km squares elements
        if (this.squares == null)
            if (this.ups)
                createSquaresUPS();
            else
                createSquaresUTM();

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

    @Override
    void createRenderables() {
        super.createRenderables();

        List<Position> positions = new ArrayList<>();

        // left meridian segment
        positions.clear();
        positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(), 10e3));
        positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().minLongitude(), 10e3));
        Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
        Sector lineSector = Sector.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(),
                this.getSector().deltaLatitude(), 1E-15);
        this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_WEST));

        if (!this.ups) {
            // right meridian segment
            positions.clear();
            positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().maxLongitude(), 10e3));
            positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().maxLongitude(), 10e3));
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
            lineSector = Sector.fromDegrees(this.getSector().minLatitude(), this.getSector().maxLongitude(),
                    this.getSector().deltaLatitude(), 1E-15);
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EAST));

            // bottom parallel segment
            positions.clear();
            positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(), 10e3));
            positions.add(Position.fromDegrees(this.getSector().minLatitude(), this.getSector().maxLongitude(), 10e3));
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
            lineSector = Sector.fromDegrees(this.getSector().minLatitude(), this.getSector().minLongitude(),
                    1E-15, this.getSector().deltaLongitude());
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_SOUTH));

            // top parallel segment
            positions.clear();
            positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().minLongitude(), 10e3));
            positions.add(Position.fromDegrees(this.getSector().maxLatitude(), this.getSector().maxLongitude(), 10e3));
            polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
            lineSector = Sector.fromDegrees(this.getSector().maxLatitude(), this.getSector().minLongitude(),
                    1E-15, this.getSector().deltaLongitude());
            this.getGridElements().add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTH));
        }

        // Label
        Renderable text = this.getLayer().createTextRenderable(Position.fromDegrees(this.getSector().centroidLatitude(), this.getSector().centroidLongitude(), 0), this.name, 10e6);
        this.getGridElements().add(new GridElement(this.getSector(), text, GridElement.TYPE_GRIDZONE_LABEL));
    }


    boolean isUPS() {
        return this.ups;
    }

    private void createSquaresUTM() {
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
        minEasting = UTM.getEasting() < minEasting ? UTM.getEasting() : minEasting;
        double maxEasting = 1e6 - minEasting;

        // Compensate for some distorted zones
        if (this.name.equals("32V")) // catch KS and LS in 32V
            maxNorthing += 20e3;
        if (this.name.equals("31X")) // catch GA and GV in 31X
            maxEasting += ONEHT;

        // Create squares
        this.squares = getLayer().createSquaresGrid(this.zone, this.hemisphere, this.getSector(), minEasting, maxEasting,
                minNorthing, maxNorthing);
        this.setSquareNames();
    }

    private void createSquaresUPS() {
        this.squares = new ArrayList<>();
        double minEasting, maxEasting, minNorthing, maxNorthing;

        if (Hemisphere.N.equals(this.hemisphere)) {
            minNorthing = TWOMIL - ONEHT * 7;
            maxNorthing = TWOMIL + ONEHT * 7;
            minEasting = this.name.equals("Y") ? TWOMIL - ONEHT * 7 : TWOMIL;
            maxEasting = this.name.equals("Y") ? TWOMIL : TWOMIL + ONEHT * 7;
        } else {
            minNorthing = TWOMIL - ONEHT * 12;
            maxNorthing = TWOMIL + ONEHT * 12;
            minEasting = this.name.equals("A") ? TWOMIL - ONEHT * 12 : TWOMIL;
            maxEasting = this.name.equals("A") ? TWOMIL : TWOMIL + ONEHT * 12;
        }

        // Create squares
        this.squares = getLayer().createSquaresGrid(this.zone, this.hemisphere, this.getSector(), minEasting, maxEasting,
            minNorthing, maxNorthing);
        this.setSquareNames();
    }

    private void setSquareNames() {
        for (UTMSquareZone sz : this.squares) {
            this.setSquareName(sz);
        }
    }

    private void setSquareName(UTMSquareZone sz) {
        // Find out MGRS 100Km square name
        double tenMeterDegree = Math.toDegrees(10d / 6378137d);
        MGRSCoord MGRS = null;
        if (sz.centroid != null && sz.isPositionInside(Position.fromDegrees(sz.centroid.latitude, sz.centroid.longitude, 0)))
            MGRS = MGRSCoord.fromLatLon(sz.centroid.latitude, sz.centroid.longitude);
        else if (sz.isPositionInside(sz.sw))
            MGRS = MGRSCoord.fromLatLon(
                    Position.clampLatitude(sz.sw.latitude + tenMeterDegree),
                    Position.clampLongitude(sz.sw.longitude + tenMeterDegree));
        else if (sz.isPositionInside(sz.se))
            MGRS = MGRSCoord.fromLatLon(
                    Position.clampLatitude(sz.se.latitude + tenMeterDegree),
                    Position.clampLongitude(sz.se.longitude - tenMeterDegree));
        else if (sz.isPositionInside(sz.nw))
            MGRS = MGRSCoord.fromLatLon(
                    Position.clampLatitude(sz.nw.latitude - tenMeterDegree),
                    Position.clampLongitude(sz.nw.longitude + tenMeterDegree));
        else if (sz.isPositionInside(sz.ne))
            MGRS = MGRSCoord.fromLatLon(
                    Position.clampLatitude(sz.ne.latitude - tenMeterDegree),
                    Position.clampLongitude(sz.ne.longitude - tenMeterDegree));
        // Set square zone name
        if (MGRS != null)
            sz.setName(MGRS.toString().substring(3, 5));
    }

}
