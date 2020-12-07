package gov.nasa.worldwind.layer.graticule;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;

class GridElement {
    final static String TYPE_LINE = "GridElement_Line";
    final static String TYPE_LINE_NORTH = "GridElement_LineNorth";
    final static String TYPE_LINE_SOUTH = "GridElement_LineSouth";
    final static String TYPE_LINE_WEST = "GridElement_LineWest";
    final static String TYPE_LINE_EAST = "GridElement_LineEast";
    final static String TYPE_LINE_NORTHING = "GridElement_LineNorthing";
    final static String TYPE_LINE_EASTING = "GridElement_LineEasting";
    final static String TYPE_GRIDZONE_LABEL = "GridElement_GridZoneLabel";
    final static String TYPE_LONGITUDE_LABEL = "GridElement_LongitudeLabel";
    final static String TYPE_LATITUDE_LABEL = "GridElement_LatitudeLabel";

    public final Sector sector;
    public final Renderable renderable;
    public final String type;
    public final double value;

    GridElement(Sector sector, Renderable renderable, String type, double value) {
        this.sector = sector;
        this.renderable = renderable;
        this.type = type;
        this.value = value;
    }

    GridElement(Sector sector, Renderable renderable, String type) {
        this(sector, renderable, type, 0);
    }

    boolean isInView(RenderContext rc) {
        return this.sector.intersectsOrNextTo(rc.terrain.getSector());
    }

}
