/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
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
 * Displays the geographic Global Area Reference System (GARS) graticule. The graticule has four levels. The first level
 * displays lines of latitude and longitude. The second level displays 30 minute square grid cells. The third level
 * displays 15 minute grid cells. The fourth and final level displays 5 minute grid cells.
 *
 * This graticule is intended to be used on 2D globes because it is so dense.
 *
 * @version $Id: GARSGraticuleLayer.java 2384 2014-10-14 21:55:10Z tgaskins $
 */
public class GARSGraticuleLayer extends AbstractLatLonGraticuleLayer {

    private static final String GRATICULE_GARS_LEVEL_0 = "Graticule.GARSLevel0";
    private static final String GRATICULE_GARS_LEVEL_1 = "Graticule.GARSLevel1";
    private static final String GRATICULE_GARS_LEVEL_2 = "Graticule.GARSLevel2";
    private static final String GRATICULE_GARS_LEVEL_3 = "Graticule.GARSLevel3";

    public GARSGraticuleLayer() {
        super("GARS Graticule");
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
        setRenderingParams(GRATICULE_GARS_LEVEL_0, params);
        // One degree
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.YELLOW));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.YELLOW));
        params.put(GraticuleRenderingParams.KEY_LABEL_TYPEFACE, Typeface.create("arial", Typeface.BOLD));
        params.put(GraticuleRenderingParams.KEY_LABEL_SIZE, 14f * Resources.getSystem().getDisplayMetrics().scaledDensity);
        setRenderingParams(GRATICULE_GARS_LEVEL_1, params);
        // 1/10th degree - 1/6th (10 minutes)
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.GREEN));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.GREEN));
        setRenderingParams(GRATICULE_GARS_LEVEL_2, params);
        // 1/100th degree - 1/60th (one minutes)
        params = new GraticuleRenderingParams();
        params.put(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(android.graphics.Color.CYAN));
        params.put(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(android.graphics.Color.CYAN));
        setRenderingParams(GRATICULE_GARS_LEVEL_3, params);
    }

    @Override
    protected List<String> getOrderedTypes() {
        return Arrays.asList(
            GRATICULE_GARS_LEVEL_0,
            GRATICULE_GARS_LEVEL_1,
            GRATICULE_GARS_LEVEL_2,
            GRATICULE_GARS_LEVEL_3);
    }

    @Override
    protected String getTypeFor(double resolution) {
        if (resolution >= 10)
            return GRATICULE_GARS_LEVEL_0;
        else if (resolution >= 0.5)
            return GRATICULE_GARS_LEVEL_1;
        else if (resolution >= .25)
            return GRATICULE_GARS_LEVEL_2;
        else if (resolution >= 5.0 / 60.0)
            return GRATICULE_GARS_LEVEL_3;

        return null;
    }

    @Override
    public AbstractGraticuleTile[][] initGridTiles(int rows, int cols) {
        return new GARSGraticuleTile[rows][cols];
    }

    @Override
    public AbstractGraticuleTile createGridTile(Sector sector) {
        return new GARSGraticuleTile(this, sector, 20, 0);
    }

}
