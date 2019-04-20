/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;

/**
 * @author Patrick Murris
 * @version $Id: MGRSGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */

public class MGRSGraticuleLayer extends AbstractUTMGraticuleLayer {

    static final int MGRS_OVERVIEW_RESOLUTION = 1000000;
    static final int MGRS_GRID_ZONE_RESOLUTION = 500000;

    /** Graticule for the MGRS overview. */
    private static final String GRATICULE_MGRS_OVERVIEW = "Graticule.MGRS.Overview";
    /** Graticule for the MGRS grid zone. */
    private static final String GRATICULE_MGRS_GRID_ZONE = "Graticule.MGRS.GridZone";

    private static final double GRID_ZONE_MAX_ALTITUDE = 5000e3;

    private final MGRSGridZone[][] gridZones = new MGRSGridZone[20][60]; // row/col
    private final MGRSGridZone[] poleZones = new MGRSGridZone[4]; // North x2 + South x2
    private final MGRSOverview overview = new MGRSOverview(this);

    /** Creates a new <code>MGRSGraticuleLayer</code>, with default graticule attributes. */
    public MGRSGraticuleLayer() {
        super("MGRS graticule", (int) 100e3, 1e5);
    }

    /**
     * Returns the maxiumum resolution graticule that will be rendered, or null if no graticules will be rendered. By
     * default, all graticules are rendered, and this will return GRATICULE_1M.
     *
     * @return maximum resolution rendered.
     */
    public String getMaximumGraticuleResolution() {
        String maxTypeDrawn = null;
        for (String type : getOrderedTypes()) {
            GraticuleRenderingParams params = getRenderingParams(type);
            if (params.isDrawLines()) {
                maxTypeDrawn = type;
            }
        }
        return maxTypeDrawn;
    }

    /**
     * Sets the maxiumum resolution graticule that will be rendered.
     *
     * @param graticuleType one of GRATICULE_MGRS_OVERVIEW, GRATICULE_MGRS_GRID_ZONE, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     */
    public void setMaximumGraticuleResolution(String graticuleType) {
        boolean pastTarget = false;
        for (String type : getOrderedTypes()) {
            // Enable all graticulte BEFORE and INCLUDING the target.
            // Disable all graticules AFTER the target.
            GraticuleRenderingParams params = getRenderingParams(type);
            params.setDrawLines(!pastTarget);
            params.setDrawLabels(!pastTarget);
            if (!pastTarget && type.equals(graticuleType)) {
                pastTarget = true;
            }
        }
    }

    @Override
    protected void initRenderingParams() {
        super.initRenderingParams();

        GraticuleRenderingParams params;
        // MGRS Overview graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(.8f, .8f, .8f, .5f));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(1f, 1f, 1f, .8f));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 14f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        params.put(GraticuleRenderingParams.KEY_DRAW_LABELS, Boolean.TRUE);
        setRenderingParams(GRATICULE_MGRS_OVERVIEW, params);
        // MGRS GridZone graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.YELLOW));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.YELLOW));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 16f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        setRenderingParams(GRATICULE_MGRS_GRID_ZONE, params);
    }

    @Override
    protected List<String> getOrderedTypes() {
        List<String> orderedTypes = Arrays.asList(GRATICULE_MGRS_OVERVIEW, GRATICULE_MGRS_GRID_ZONE);
        orderedTypes.addAll(super.getOrderedTypes());
        return orderedTypes;
    }

    @Override
    protected String getTypeFor(double resolution) {
        switch ((int) resolution) {
            case MGRS_OVERVIEW_RESOLUTION: return GRATICULE_MGRS_OVERVIEW;
            case MGRS_GRID_ZONE_RESOLUTION: return GRATICULE_MGRS_GRID_ZONE;
            default: return super.getTypeFor(resolution);
        }
    }

    @Override
    protected void selectRenderables(RenderContext rc) {
        if (rc.camera.altitude <= GRID_ZONE_MAX_ALTITUDE) {
            this.selectMGRSRenderables(rc);
            super.selectRenderables(rc);
        } else {
            this.overview.selectRenderables(rc);
        }
    }

    private void selectMGRSRenderables(RenderContext rc) {
        List<MGRSGridZone> zoneList = getVisibleZones(rc);
        if (zoneList.size() > 0) {
            for (MGRSGridZone gz : zoneList) {
                // Select visible grid zones elements
                gz.selectRenderables(rc);
            }
        }
    }

    private List<MGRSGridZone> getVisibleZones(RenderContext rc) {
        List<MGRSGridZone> zoneList = new ArrayList<>();
        Sector vs = rc.terrain.getSector();
        if (vs != null) {
            // UTM Grid
            Rect gridRectangle = getGridRectangleForSector(vs);
            if (gridRectangle != null) {
                for (int row = gridRectangle.top; row <= gridRectangle.bottom; row++) {
                    for (int col = gridRectangle.left; col <= gridRectangle.right; col++) {
                        if (row != 19 || (col != 31 && col != 33 && col != 35)) { // ignore X32, 34 and 36
                            if (gridZones[row][col] == null)
                                gridZones[row][col] = new MGRSGridZone(this, getGridSector(row, col));
                            if (gridZones[row][col].isInView(rc))
                                zoneList.add(gridZones[row][col]);
                            else
                                gridZones[row][col].clearRenderables();
                        }
                    }
                }
            }
            // Poles
            if (vs.maxLatitude() > 84) {
                // North pole
                if (poleZones[2] == null)
                    poleZones[2] = new MGRSGridZone(this, Sector.fromDegrees(84, -180, 6,180)); // Y
                if (poleZones[3] == null)
                    poleZones[3] = new MGRSGridZone(this, Sector.fromDegrees(84, 0, 6,180));  // Z
                zoneList.add(poleZones[2]);
                zoneList.add(poleZones[3]);
            }
            if (vs.minLatitude() < -80) {
                // South pole
                if (poleZones[0] == null)
                    poleZones[0] = new MGRSGridZone(this, Sector.fromDegrees(-90, -180, 10,180)); // B
                if (poleZones[1] == null)
                    poleZones[1] = new MGRSGridZone(this, Sector.fromDegrees(-90, 0, 10,180));  // A
                zoneList.add(poleZones[0]);
                zoneList.add(poleZones[1]);
            }
        }
        return zoneList;
    }

    private Rect getGridRectangleForSector(Sector sector) {
        Rect rectangle = null;
        if (sector.minLatitude() < 84 && sector.maxLatitude() > -80) {
            double minLat = Math.max(sector.minLatitude(), -80);
            double maxLat = Math.min(sector.maxLatitude(), 84);
            Sector gridSector = Sector.fromDegrees(minLat, sector.minLongitude(),
                    maxLat - minLat, sector.deltaLongitude());
            int x1 = getGridColumn(gridSector.minLongitude());
            int x2 = getGridColumn(gridSector.maxLongitude());
            int y1 = getGridRow(gridSector.minLatitude());
            int y2 = getGridRow(gridSector.maxLatitude());
            // Adjust rectangle to include special zones
            if (y1 <= 17 && y2 >= 17 && x2 == 30) // 32V Norway
                x2 = 31;
            if (y1 <= 19 && y2 >= 19) { // X band
                if (x1 == 31) // 31X
                    x1 = 30;
                if (x2 == 31) // 33X
                    x2 = 32;
                if (x1 == 33) // 33X
                    x1 = 32;
                if (x2 == 33) // 35X
                    x2 = 34;
                if (x1 == 35) // 35X
                    x1 = 34;
                if (x2 == 35) // 37X
                    x2 = 36;
            }
            rectangle = new Rect(x1, y1, x2, y2);
        }
        return rectangle;
    }

    private int getGridColumn(double longitude) {
        return Math.min((int) Math.floor((longitude + 180) / 6d), 59);
    }

    private int getGridRow(double latitude) {
        return Math.min((int) Math.floor((latitude + 80) / 8d), 19);
    }

    private Sector getGridSector(int row, int col) {
        int minLat = -80 + row * 8;
        int maxLat = minLat + (minLat != 72 ? 8 : 12);
        int minLon = -180 + col * 6;
        int maxLon = minLon + 6;
        // Special sectors
        if (row == 17 && col == 30)         // 31V
            maxLon -= 3;
        else if (row == 17 && col == 31)    // 32V
            minLon -= 3;
        else if (row == 19 && col == 30)   // 31X
            maxLon += 3;
        else if (row == 19 && col == 31) { // 32X does not exist
            minLon += 3;
            maxLon -= 3;
        } else if (row == 19 && col == 32) { // 33X
            minLon -= 3;
            maxLon += 3;
        } else if (row == 19 && col == 33) { // 34X does not exist
            minLon += 3;
            maxLon -= 3;
        } else if (row == 19 && col == 34) { // 35X
            minLon -= 3;
            maxLon += 3;
        } else if (row == 19 && col == 35) { // 36X does not exist
            minLon += 3;
            maxLon -= 3;
        } else if (row == 19 && col == 36)   // 37X
            minLon -= 3;
        return Sector.fromDegrees(minLat, minLon, maxLat - minLat, maxLon - minLon);
    }

    boolean isNorthNeighborInView(MGRSGridZone gz, RenderContext rc) {
        if (gz.isUPS())
            return true;

        int row = getGridRow(gz.getSector().centroidLatitude());
        int col = getGridColumn(gz.getSector().centroidLongitude());
        MGRSGridZone neighbor = row + 1 <= 19 ? this.gridZones[row + 1][col] : null;
        return neighbor != null && neighbor.isInView(rc);
    }

    boolean isEastNeighborInView(MGRSGridZone gz, RenderContext rc) {
        if (gz.isUPS())
            return true;

        int row = getGridRow(gz.getSector().centroidLatitude());
        int col = getGridColumn(gz.getSector().centroidLongitude());
        MGRSGridZone neighbor = col + 1 <= 59 ? this.gridZones[row][col + 1] : null;
        return neighbor != null && neighbor.isInView(rc);
    }

}
