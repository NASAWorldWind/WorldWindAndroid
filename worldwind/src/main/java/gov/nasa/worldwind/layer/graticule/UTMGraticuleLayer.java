/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import android.content.res.Resources;
import android.graphics.Typeface;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;

/**
 * Displays the UTM graticule.
 *
 * @author Patrick Murris
 * @version $Id: UTMGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public class UTMGraticuleLayer extends AbstractUTMGraticuleLayer implements GridTilesSupport.Callback {

    static final int UTM_ZONE_RESOLUTION = 500000;

    /** Graticule for the UTM zone grid. */
    private static final String GRATICULE_UTM_ZONE = "Graticule.UTM.Zone";

    private static final int GRID_ROWS = 2;
    private static final int GRID_COLS = 60;

    private final GridTilesSupport gridTilesSupport;

    public UTMGraticuleLayer() {
        super("UTM Graticule", (int) 10e6, 1e6);
        this.gridTilesSupport = new GridTilesSupport(this, GRID_ROWS, GRID_COLS);
    }

    @Override
    protected void initRenderingParams() {
        super.initRenderingParams();

        GraticuleRenderingParams params;
        // UTM zone grid
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.WHITE));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.WHITE));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 16f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        setRenderingParams(GRATICULE_UTM_ZONE, params);
    }

    @Override
    protected List<String> getOrderedTypes() {
        List<String> orderedTypes = Arrays.asList(GRATICULE_UTM_ZONE);
        orderedTypes.addAll(super.getOrderedTypes());
        return orderedTypes;
    }

    @Override
    protected String getTypeFor(double resolution) {
        if (resolution >= UTM_ZONE_RESOLUTION)
            return GRATICULE_UTM_ZONE;
        else
            return super.getTypeFor(resolution);
    }

    @Override
    protected void selectRenderables(RenderContext rc) {
        this.gridTilesSupport.selectRenderables(rc);
        super.selectRenderables(rc);
    }

    @Override
    public AbstractGraticuleTile[][] initGridTiles(int rows, int cols) {
        return new UTMGraticuleTile[rows][cols];
    }

    @Override
    public AbstractGraticuleTile createGridTile(Sector sector) {
        return new UTMGraticuleTile(this, sector, getGridColumn(sector.centroidLongitude()) + 1);
    }

    @Override
    public Sector getGridSector(int row, int col) {
        double deltaLat = UTM_MAX_LATITUDE * 2d / GRID_ROWS;
        double deltaLon = 360d / GRID_COLS;
        double minLat = row == 0 ? UTM_MIN_LATITUDE : -UTM_MAX_LATITUDE + deltaLat * row;
        double maxLat = -UTM_MAX_LATITUDE + deltaLat * (row + 1);
        double minLon = -180 + deltaLon * col;
        double maxLon = minLon + deltaLon;
        return Sector.fromDegrees(minLat, minLon, maxLat - minLat, maxLon - minLon);
    }

    @Override
    public int getGridColumn(double longitude) {
        double deltaLon = 360d / GRID_COLS;
        int col = (int) Math.floor((longitude + 180) / deltaLon);
        return Math.min(col, GRID_COLS - 1);
    }

    @Override
    public int getGridRow(double latitude) {
        double deltaLat = UTM_MAX_LATITUDE * 2d / GRID_ROWS;
        int row = (int) Math.floor((latitude + UTM_MAX_LATITUDE) / deltaLat);
        return Math.max(0, Math.min(row, GRID_ROWS - 1));
    }

}