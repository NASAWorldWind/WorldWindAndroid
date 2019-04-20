/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import android.content.res.Resources;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.Hemisphere;
import gov.nasa.worldwind.geom.coords.UPSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;

/**
 * Displays the UTM graticule metric scale.
 *
 * @author Patrick Murris
 * @version $Id: UTMBaseGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public abstract class AbstractUTMGraticuleLayer extends AbstractGraticuleLayer {

    static final int UTM_MIN_LATITUDE = -80;
    static final int UTM_MAX_LATITUDE = 84;

    /** Graticule for the 100,000 meter grid. */
    private static final String GRATICULE_UTM_100000M = "Graticule.UTM.100000m";
    /** Graticule for the 10,000 meter grid. */
    private static final String GRATICULE_UTM_10000M = "Graticule.UTM.10000m";
    /** Graticule for the 1,000 meter grid. */
    private static final String GRATICULE_UTM_1000M = "Graticule.UTM.1000m";
    /** Graticule for the 100 meter grid. */
    private static final String GRATICULE_UTM_100M = "Graticule.UTM.100m";
    /** Graticule for the 10 meter grid. */
    private static final String GRATICULE_UTM_10M = "Graticule.UTM.10m";
    /** Graticule for the 1 meter grid. */
    private static final String GRATICULE_UTM_1M = "Graticule.UTM.1m";

    private static final double ONEHT = 100e3;

    private final UTMMetricScaleSupport metricScaleSupport;

    AbstractUTMGraticuleLayer(String name, int scaleModulo, double maxResolution) {
        super(name);
        this.metricScaleSupport = new UTMMetricScaleSupport(this);
        this.metricScaleSupport.setScaleModulo(scaleModulo);
        this.metricScaleSupport.setMaxResolution(maxResolution);
    }

    @Override
    protected void initRenderingParams() {
        GraticuleRenderingParams params;
        // 100,000 meter graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.GREEN));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.GREEN));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 14f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        setRenderingParams(GRATICULE_UTM_100000M, params);
        // 10,000 meter graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(0, 102, 255)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(0, 102, 255)));
        setRenderingParams(GRATICULE_UTM_10000M, params);
        // 1,000 meter graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.CYAN));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.CYAN));
        setRenderingParams(GRATICULE_UTM_1000M, params);
        // 100 meter graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(0, 153, 153)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(0, 153, 153)));
        setRenderingParams(GRATICULE_UTM_100M, params);
        // 10 meter graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(102, 255, 204)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(102, 255, 204)));
        setRenderingParams(GRATICULE_UTM_10M, params);
        // 1 meter graticule
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(153, 153, 255)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(153, 153, 255)));
        setRenderingParams(GRATICULE_UTM_1M, params);
    }

    @Override
    protected List<String> getOrderedTypes() {
        return Arrays.asList(
                GRATICULE_UTM_100000M,
                GRATICULE_UTM_10000M,
                GRATICULE_UTM_1000M,
                GRATICULE_UTM_100M,
                GRATICULE_UTM_10M,
                GRATICULE_UTM_1M);
    }

    @Override
    protected String getTypeFor(double resolution) {
        if (resolution >= 100000)
            return GRATICULE_UTM_100000M;
        else if (resolution >= 10000)
            return GRATICULE_UTM_10000M;
        else if (resolution >= 1000)
            return GRATICULE_UTM_1000M;
        else if (resolution >= 100)
            return GRATICULE_UTM_100M;
        else if (resolution >= 10)
            return GRATICULE_UTM_10M;
        else if (resolution >= 1)
            return GRATICULE_UTM_1M;
        else
            return null;
    }

    @Override
    protected void clear(RenderContext rc) {
        super.clear(rc);
        this.metricScaleSupport.clear();
        this.metricScaleSupport.computeZone(rc);
    }

    @Override
    protected void selectRenderables(RenderContext rc) {
        this.metricScaleSupport.selectRenderables(rc);
    }

    void computeMetricScaleExtremes(int UTMZone, Hemisphere hemisphere, GridElement ge, double size) {
        this.metricScaleSupport.computeMetricScaleExtremes(UTMZone, hemisphere, ge, size);
    }

    Position computePosition(int zone, Hemisphere hemisphere, double easting, double northing) {
        return zone > 0 ?
                computePositionFromUTM(zone, hemisphere, easting, northing) :
                computePositionFromUPS(hemisphere, easting, northing);
    }

    private Position computePositionFromUTM(int zone, Hemisphere hemisphere, double easting, double northing) {
        UTMCoord UTM = UTMCoord.fromUTM(zone, hemisphere, easting, northing);
        return Position.fromDegrees(Position.clampLatitude(UTM.getLatitude()),
                Position.clampLongitude(UTM.getLongitude()), 10e3);
    }

    private Position computePositionFromUPS(Hemisphere hemisphere, double easting, double northing) {
        UPSCoord UPS = UPSCoord.fromUPS(hemisphere, easting, northing);
        return Position.fromDegrees(Position.clampLatitude(UPS.getLatitude()),
                Position.clampLongitude(UPS.getLongitude()), 10e3);
    }

    List<UTMSquareZone> createSquaresGrid(int UTMZone, Hemisphere hemisphere, Sector UTMZoneSector,
                                          double minEasting, double maxEasting, double minNorthing, double maxNorthing) {
        List<UTMSquareZone> squares = new ArrayList<>();
        double startEasting = Math.floor(minEasting / ONEHT) * ONEHT;
        double startNorthing = Math.floor(minNorthing / ONEHT) * ONEHT;
        int cols = (int) Math.ceil((maxEasting - startEasting) / ONEHT);
        int rows = (int) Math.ceil((maxNorthing - startNorthing) / ONEHT);
        UTMSquareZone[][] squaresArray = new UTMSquareZone[rows][cols];
        int col = 0;
        for (double easting = startEasting; easting < maxEasting; easting += ONEHT) {
            int row = 0;
            for (double northing = startNorthing; northing < maxNorthing; northing += ONEHT) {
                UTMSquareZone sz = new UTMSquareZone(this, UTMZone, hemisphere, UTMZoneSector, easting, northing, ONEHT);
                if (sz.boundingSector != null && !sz.isOutsideGridZone()) {
                    squares.add(sz);
                    squaresArray[row][col] = sz;
                }
                row++;
            }
            col++;
        }

        // Keep track of neighbors
        for (col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                UTMSquareZone sz = squaresArray[row][col];
                if (sz != null) {
                    sz.setNorthNeighbor(row + 1 < rows ? squaresArray[row + 1][col] : null);
                    sz.setEastNeighbor(col + 1 < cols ? squaresArray[row][col + 1] : null);
                }
            }
        }

        return squares;
    }

}
