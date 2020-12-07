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

/**
 * Displays the geographic latitude/longitude graticule.
 *
 * @author Patrick Murris
 * @version $Id: LatLonGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public class LatLonGraticuleLayer extends AbstractLatLonGraticuleLayer  {

    private static final String GRATICULE_LATLON_LEVEL_0 = "Graticule.LatLonLevel0";
    private static final String GRATICULE_LATLON_LEVEL_1 = "Graticule.LatLonLevel1";
    private static final String GRATICULE_LATLON_LEVEL_2 = "Graticule.LatLonLevel2";
    private static final String GRATICULE_LATLON_LEVEL_3 = "Graticule.LatLonLevel3";
    private static final String GRATICULE_LATLON_LEVEL_4 = "Graticule.LatLonLevel4";
    private static final String GRATICULE_LATLON_LEVEL_5 = "Graticule.LatLonLevel5";

    public LatLonGraticuleLayer() {
        super("LatLon Graticule");
    }

    @Override
    protected void initRenderingParams() {
        GraticuleRenderingParams params;
        // Ten degrees grid
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.WHITE));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR,new Color(android.graphics.Color.WHITE));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 16f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        setRenderingParams(GRATICULE_LATLON_LEVEL_0, params);
        // One degree
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.GREEN));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.GREEN));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 14f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        setRenderingParams(GRATICULE_LATLON_LEVEL_1, params);
        // 1/10th degree - 1/6th (10 minutes)
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(0, 102, 255)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(0, 102, 255)));
        setRenderingParams(GRATICULE_LATLON_LEVEL_2, params);
        // 1/100th degree - 1/60th (one minutes)
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.CYAN));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.CYAN));
        setRenderingParams(GRATICULE_LATLON_LEVEL_3, params);
        // 1/1000 degree - 1/360th (10 seconds)
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(0, 153, 153)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(0, 153, 153)));
        setRenderingParams(GRATICULE_LATLON_LEVEL_4, params);
        // 1/10000 degree - 1/3600th (one second)
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.rgb(102, 255, 204)));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.rgb(102, 255, 204)));
        setRenderingParams(GRATICULE_LATLON_LEVEL_5, params);
    }

    @Override
    protected List<String> getOrderedTypes() {
        return Arrays.asList(
            GRATICULE_LATLON_LEVEL_0,
            GRATICULE_LATLON_LEVEL_1,
            GRATICULE_LATLON_LEVEL_2,
            GRATICULE_LATLON_LEVEL_3,
            GRATICULE_LATLON_LEVEL_4,
            GRATICULE_LATLON_LEVEL_5);
    }

    @Override
    protected String getTypeFor(double resolution) {
        if (resolution >= 10)
            return GRATICULE_LATLON_LEVEL_0;
        else if (resolution >= 1)
            return GRATICULE_LATLON_LEVEL_1;
        else if (resolution >= .1)
            return GRATICULE_LATLON_LEVEL_2;
        else if (resolution >= .01)
            return GRATICULE_LATLON_LEVEL_3;
        else if (resolution >= .001)
            return GRATICULE_LATLON_LEVEL_4;
        else if (resolution >= .0001)
            return GRATICULE_LATLON_LEVEL_5;

        return null;
    }

    @Override
    public AbstractGraticuleTile[][] initGridTiles(int rows, int cols) {
        return new LatLonGraticuleTile[rows][cols];
    }

    @Override
    public AbstractGraticuleTile createGridTile(Sector sector) {
        return new LatLonGraticuleTile(this, sector, 10, 0);
    }

}
