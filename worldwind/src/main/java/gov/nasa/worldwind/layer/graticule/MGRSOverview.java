package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Label;

class MGRSOverview extends AbstractGraticuleTile {

    // Exceptions for some meridians. Values: longitude, min latitude, max latitude
    private static final int[][] SPECIAL_MERIDIANS = {{3, 56, 64}, {6, 64, 72}, {9, 72, 84}, {21, 72, 84}, {33, 72, 84}};
    // Latitude bands letters - from south to north
    private static final String LAT_BANDS = "CDEFGHJKLMNPQRSTUVWX";

    MGRSOverview(MGRSGraticuleLayer layer) {
        super(layer, null);
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        Location labelPos = getLayer().computeLabelOffset(rc);
        for (GridElement ge : this.getGridElements()) {
            if (ge.isInView(rc)) {
                if (ge.renderable instanceof Label) {
                    Label gt = (Label) ge.renderable;
                    if (labelPos.latitude < 72 || !"*32*34*36*".contains("*" + gt.getText() + "*")) {
                        // Adjust label position according to eye position
                        Position pos = gt.getPosition();
                        if (ge.type.equals(GridElement.TYPE_LATITUDE_LABEL))
                            pos = Position.fromDegrees(pos.latitude,
                                    labelPos.longitude, pos.altitude);
                        else if (ge.type.equals(GridElement.TYPE_LONGITUDE_LABEL))
                            pos = Position.fromDegrees(labelPos.latitude,
                                    pos.longitude, pos.altitude);

                        gt.setPosition(pos);
                    }
                }

                getLayer().addRenderable(ge.renderable, getLayer().getTypeFor(MGRSGraticuleLayer.MGRS_OVERVIEW_RESOLUTION));
            }
        }
    }

    @Override
    void createRenderables() {
        super.createRenderables();

        List<Position> positions = new ArrayList<>();

        // Generate meridians and zone labels
        int lon = -180;
        int zoneNumber = 1;
        int maxLat;
        for (int i = 0; i < 60; i++) {
            double longitude = lon;
            // Meridian
            positions.clear();
            positions.add(Position.fromDegrees(-80, longitude, 10e3));
            positions.add(Position.fromDegrees(-60, longitude, 10e3));
            positions.add(Position.fromDegrees(-30, longitude, 10e3));
            positions.add(Position.fromDegrees(0, longitude, 10e3));
            positions.add(Position.fromDegrees(30, longitude, 10e3));
            if (lon < 6 || lon > 36) {
                // 'regular' UTM meridians
                maxLat = 84;
                positions.add(Position.fromDegrees(60, longitude, 10e3));
                positions.add(Position.fromDegrees(maxLat, longitude, 10e3));
            } else {
                // Exceptions: shorter meridians around and north-east of Norway
                if (lon == 6) {
                    maxLat = 56;
                    positions.add(Position.fromDegrees(maxLat, longitude, 10e3));
                } else {
                    maxLat = 72;
                    positions.add(Position.fromDegrees(60, longitude, 10e3));
                    positions.add(Position.fromDegrees(maxLat, longitude, 10e3));
                }
            }
            Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
            Sector sector = Sector.fromDegrees(-80, lon, maxLat + 80, 1E-15);
            this.getGridElements().add(new GridElement(sector, polyline, GridElement.TYPE_LINE));

            // Zone label
            Renderable text = getLayer().createTextRenderable(Position.fromDegrees(0, lon + 3, 0), zoneNumber + "", 10e6);
            sector = Sector.fromDegrees(-90,  lon + 3, 180, 1E-15);
            this.getGridElements().add(new GridElement(sector, text, GridElement.TYPE_LONGITUDE_LABEL));

            // Increase longitude and zone number
            lon += 6;
            zoneNumber++;
        }

        // Generate special meridian segments for exceptions around and north-east of Norway
        for (int i = 0; i < 5; i++) {
            positions.clear();
            lon = SPECIAL_MERIDIANS[i][0];
            positions.add(Position.fromDegrees(SPECIAL_MERIDIANS[i][1], lon, 10e3));
            positions.add(Position.fromDegrees(SPECIAL_MERIDIANS[i][2], lon, 10e3));
            Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.GREAT_CIRCLE);
            Sector sector = Sector.fromDegrees(SPECIAL_MERIDIANS[i][1], lon, SPECIAL_MERIDIANS[i][2] - SPECIAL_MERIDIANS[i][1], 1E-15);
            this.getGridElements().add(new GridElement(sector, polyline, GridElement.TYPE_LINE));
        }

        // Generate parallels - no exceptions
        int lat = -80;
        for (int i = 0; i < 21; i++) {
            double latitude = lat;
            for (int j = 0; j < 4; j++) {
                // Each prallel is divided into four 90 degrees segments
                positions.clear();
                lon = -180 + j * 90;
                positions.add(Position.fromDegrees(latitude, lon, 10e3));
                positions.add(Position.fromDegrees(latitude, lon + 30, 10e3));
                positions.add(Position.fromDegrees(latitude, lon + 60, 10e3));
                positions.add(Position.fromDegrees(latitude, lon + 90, 10e3));
                Renderable polyline = getLayer().createLineRenderable(new ArrayList<>(positions), WorldWind.LINEAR);
                Sector sector = Sector.fromDegrees(lat, lon, 1E-15, 90);
                this.getGridElements().add(new GridElement(sector, polyline, GridElement.TYPE_LINE));
            }
            // Latitude band label
            if (i < 20) {
                Renderable text = getLayer().createTextRenderable(Position.fromDegrees(lat + 4, 0, 0), LAT_BANDS.charAt(i) + "", 10e6);
                Sector sector = Sector.fromDegrees(lat + 4, -180, 1E-15,360);
                this.getGridElements().add(new GridElement(sector, text, GridElement.TYPE_LATITUDE_LABEL));
            }

            // Increase latitude
            lat += lat < 72 ? 8 : 12;
        }
    }

}
