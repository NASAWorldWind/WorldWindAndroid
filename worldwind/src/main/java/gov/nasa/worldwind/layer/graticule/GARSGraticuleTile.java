package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

class GARSGraticuleTile extends AbstractGraticuleTile {

    /**
     * Indicates the eye altitudes in meters below which each level should be displayed.
     */
    private static final double[] THRESHOLDS = new double[] {1200e3, 600e3, 180e3}; // 30 min, 15 min, 5 min

    /**
     * Specifies the eye altitude below which the 30 minute grid is displayed.
     *
     * @param altitude the eye altitude in meters below which the 30 minute grid is displayed.
     */
    public static void set30MinuteThreshold(double altitude) {
        THRESHOLDS[0] = altitude;
    }

    /**
     * Indicates the eye altitude below which the 30 minute grid is displayed.
     *
     * @return the eye altitude in meters below which the 30 minute grid is displayed.
     */
    public static double get30MinuteThreshold() {
        return THRESHOLDS[0];
    }

    /**
     * Specifies the eye altitude below which the 15 minute grid is displayed.
     *
     * @param altitude the eye altitude in meters below which the 15 minute grid is displayed.
     */
    public static void set15MinuteThreshold(double altitude) {
        THRESHOLDS[1] = altitude;
    }

    /**
     * Indicates the eye altitude below which the 15 minute grid is displayed.
     *
     * @return the eye altitude in meters below which the 15 minute grid is displayed.
     */
    public static double get15MinuteThreshold() {
        return THRESHOLDS[1];
    }

    /**
     * Specifies the eye altitude below which the 5 minute grid is displayed.
     *
     * @param altitude the eye altitude in meters below which the 5 minute grid is displayed.
     */
    public static void set5MinuteThreshold(double altitude) {
        THRESHOLDS[2] = altitude;
    }

    /**
     * Indicates the eye altitude below which the 5 minute grid is displayed.
     *
     * @return the eye altitude in meters below which the 5 minute grid is displayed.
     */
    public static double get5MinuteThreshold() {
        return THRESHOLDS[2];
    }

    private static final List<String> LAT_LABELS = new ArrayList<>(360);
    private static final List<String> LON_LABELS = new ArrayList<>(720);
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String[][] LEVEL_2_LABELS = new String[][] {{"3", "4"}, {"1", "2"}};

    static {
        for (int i = 1; i <= 720; i++) {
            LON_LABELS.add(String.format("%03d", i));
        }

        for (int i = 0; i < 360; i++) {
            int length = CHARS.length();
            int i1 = i / length;
            int i2 = i % length;
            LAT_LABELS.add(String.format("%c%c", CHARS.charAt(i1), CHARS.charAt(i2)));
        }
    }

    private static String makeLabelLevel1(Sector sector) {
        int iLat = (int) ((90 + sector.centroidLatitude()) * 60 / 30);
        int iLon = (int) ((180 + sector.centroidLongitude()) * 60 / 30);

        return LON_LABELS.get(iLon) + LAT_LABELS.get(iLat);
    }

    private static String makeLabelLevel2(Sector sector) {
        int minutesLat = (int) ((90 + sector.minLatitude()) * 60);
        int j = (minutesLat % 30) / 15;
        int minutesLon = (int) ((180 + sector.minLongitude()) * 60);
        int i = (minutesLon % 30) / 15;

        return LEVEL_2_LABELS[j][i];
    }

    private final int divisions;
    private final int level;

    private List<GARSGraticuleTile> subTiles;

    GARSGraticuleTile(GARSGraticuleLayer layer, Sector sector, int divisions, int level) {
        super(layer, sector);
        this.divisions = divisions;
        this.level = level;
    }

    @Override
    GARSGraticuleLayer getLayer() {
        return (GARSGraticuleLayer) super.getLayer();
    }

    @Override
    boolean isInView(RenderContext rc) {
        return super.isInView(rc) && (this.level == 0 || rc.camera.position.altitude <= THRESHOLDS[this.level - 1]);
    }

    @Override
    void selectRenderables(RenderContext rc) {
        super.selectRenderables(rc);

        String graticuleType = getLayer().getTypeFor(this.getSector().deltaLatitude());
        if (this.level == 0 && rc.camera.position.altitude > THRESHOLDS[0]) {
            Location labelOffset = getLayer().computeLabelOffset(rc);

            for (GridElement ge : this.getGridElements()) {
                if (ge.isInView(rc)) {
                    // Add level zero bounding lines and labels
                    if (ge.type.equals(GridElement.TYPE_LINE_SOUTH) || ge.type.equals(GridElement.TYPE_LINE_NORTH)
                        || ge.type.equals(GridElement.TYPE_LINE_WEST)) {
                        getLayer().addRenderable(ge.renderable, graticuleType);
                        String labelType = ge.type.equals(GridElement.TYPE_LINE_SOUTH)
                            || ge.type.equals(GridElement.TYPE_LINE_NORTH) ?
                            GridElement.TYPE_LATITUDE_LABEL : GridElement.TYPE_LONGITUDE_LABEL;
                        getLayer().addLabel(ge.value, labelType, graticuleType,
                            this.getSector().deltaLatitude(), labelOffset);
                    }
                }
            }

            if (rc.camera.position.altitude > THRESHOLDS[0])
                return;
        }

        // Select tile grid elements
        double eyeDistance = rc.camera.position.altitude;

        if (this.level == 0 && eyeDistance <= THRESHOLDS[0]
            || this.level == 1 && eyeDistance <= THRESHOLDS[1]
            || this.level == 2) {
            double resolution = this.getSector().deltaLatitude() / this.divisions;
            graticuleType = getLayer().getTypeFor(resolution);
            for (GridElement ge : this.getGridElements()) {
                if (ge.isInView(rc)) {
                    getLayer().addRenderable(ge.renderable, graticuleType);
                }
            }
        }

        if (this.level == 0 && eyeDistance > THRESHOLDS[1])
            return;
        else if (this.level == 1 && eyeDistance > THRESHOLDS[2])
            return;
        else if (this.level == 2)
            return;

        // Select child elements
        if (this.subTiles == null)
            createSubTiles();
        for (GARSGraticuleTile gt : this.subTiles) {
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
            for (GARSGraticuleTile gt : this.subTiles) {
                gt.clearRenderables();
            }
            this.subTiles.clear();
            this.subTiles = null;
        }
    }

    private void createSubTiles() {
        this.subTiles = new ArrayList<>();
        Sector[] sectors = this.subdivide(this.divisions);
        int nextLevel = this.level + 1;
        int subDivisions = 10;
        if (nextLevel == 1)
            subDivisions = 2;
        else if (nextLevel == 2)
            subDivisions = 3;
        for (Sector s : sectors) {
            this.subTiles.add(new GARSGraticuleTile(getLayer(), s, subDivisions, nextLevel));
        }
    }

    @Override
    void createRenderables() {
        super.createRenderables();

        double step = getSector().deltaLatitude() / this.divisions;

        // Generate meridians with labels
        double lon = getSector().minLongitude() + (this.level == 0 ? 0 : step);
        while (lon < getSector().maxLongitude() - step / 2) {
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

        double resolution = this.getSector().deltaLatitude() / this.divisions;
        if (this.level == 0) {
            Sector[] sectors = this.subdivide(20);
            for (int j = 0; j < 20; j++) {
                for (int i = 0; i < 20; i++) {
                    Sector sector = sectors[j * 20 + i];
                    String label = makeLabelLevel1(sector);
                    addLabel(label, sectors[j * 20 + i], resolution);
                }
            }
        } else if (this.level == 1) {
            String label = makeLabelLevel1(this.getSector());

            Sector[] sectors = this.subdivide(2);
            addLabel(label + "3", sectors[0], resolution);
            addLabel(label + "4", sectors[1], resolution);
            addLabel(label + "1", sectors[2], resolution);
            addLabel(label + "2", sectors[3], resolution);
        } else if (this.level == 2) {
            String label = makeLabelLevel1(this.getSector());
            label += makeLabelLevel2(this.getSector());

            resolution = 0.26; // make label priority a little higher than level 2's
            Sector[] sectors = this.subdivide(3);
            addLabel(label + "7", sectors[0], resolution);
            addLabel(label + "8", sectors[1], resolution);
            addLabel(label + "9", sectors[2], resolution);
            addLabel(label + "4", sectors[3], resolution);
            addLabel(label + "5", sectors[4], resolution);
            addLabel(label + "6", sectors[5], resolution);
            addLabel(label + "1", sectors[6], resolution);
            addLabel(label + "2", sectors[7], resolution);
            addLabel(label + "3", sectors[8], resolution);
        }
    }

    private void addLabel(String label, Sector sector, double resolution) {
        Renderable text = this.getLayer().createTextRenderable(new Position(sector.centroidLatitude(), sector.centroidLongitude(), 0), label, resolution);
        this.getGridElements().add(new GridElement(sector, text, GridElement.TYPE_GRIDZONE_LABEL));
    }

}
