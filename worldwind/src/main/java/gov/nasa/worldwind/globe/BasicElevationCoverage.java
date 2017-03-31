/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;

/**
 * Displays NASA's global elevation coverage at 10m resolution within the continental United States, 30m resolution in
 * all other continents, and 900m resolution on the ocean floor, all from an OGC Web Map Service (WMS). By default,
 * BasicElevationCoverage is configured to retrieve elevation coverage from the WMS at <a
 * href="https://worldwind26.arc.nasa.gov/wms?SERVICE=WMS&REQUEST=GetCapabilities">https://worldwind26.arc.nasa.gov/wms</a>.
 */
public class BasicElevationCoverage extends TiledElevationCoverage {

    /**
     * Constructs a global elevation coverage with the WMS at https://worldwind26.arc.nasa.gov/wms.
     */
    public BasicElevationCoverage() {
        this("https://worldwind26.arc.nasa.gov/elev");
    }

    /**
     * Constructs a global elevation coverage with the WMS at a specified address.
     *
     * @param serviceAddress a URL string specifying the WMS address
     *
     * @throws IllegalArgumentException If the service address is null
     */
    public BasicElevationCoverage(String serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicElevationCoverage", "constructor", "missingServiceAddress"));
        }

        LevelSetConfig levelSetConfig = new LevelSetConfig();
        levelSetConfig.numLevels = 15;
        this.setLevelSet(new LevelSet(levelSetConfig));

        WmsLayerConfig layerConfig = new WmsLayerConfig();
        layerConfig.serviceAddress = serviceAddress;
        layerConfig.layerNames = "GEBCO,aster_v2,USGS-NED";
        layerConfig.imageFormat = "application/bil16";
        this.setTileFactory(new WmsTileFactory(layerConfig));
    }
}
