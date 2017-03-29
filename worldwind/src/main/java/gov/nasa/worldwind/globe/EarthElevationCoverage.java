/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;

public class EarthElevationCoverage extends TiledElevationCoverage {

    public EarthElevationCoverage() {
        LevelSetConfig levelSetConfig = new LevelSetConfig();
        levelSetConfig.firstLevelDelta = 180;
        levelSetConfig.numLevels = 15;
        this.setLevelSet(new LevelSet(levelSetConfig));

        WmsLayerConfig layerConfig = new WmsLayerConfig();
        layerConfig.serviceAddress = "https://worldwind26.arc.nasa.gov/elev";
        layerConfig.layerNames = "GEBCO,aster_v2,USGS-NED";
        layerConfig.imageFormat = "application/bil16";
        this.setTileFactory(new WmsTileFactory(layerConfig));
    }
}
