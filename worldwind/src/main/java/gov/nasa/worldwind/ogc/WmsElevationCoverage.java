/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.TiledElevationCoverage;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;

/**
 * Generates elevations from OGC Web Map Service (WMS) layers supporting the custom 'Bil16' image format.
 * <p>
 * WmsElevationCoverage requires the WMS service address, layer names, and layer bounding sector. Get Map requests
 * generated for retrieving data use the WMS version 1.3.0 protocol and are limited to the "application/bil16" format
 * and the EPSG:4326 coordinate system. WmsElevationCoverage does not perform version negotation and assumes the service
 * supports the format and coordinate system parameters detailed here.
 */
public class WmsElevationCoverage extends TiledElevationCoverage {

    /**
     * Constructs a Web Map Service (WMS) elevation coverage with specified WMS configuration values.
     *
     * @param sector         the layer's geographic bounding sector
     * @param numLevels      the number of levels of elevations to generate, beginning with 2-by-4 geographic grid of
     *                       90-degree tiles containing 256x256 elevation pixels
     * @param serviceAddress the WMS service address
     * @param layerNames     comma-separated list of WMS layer names
     *
     * @throws IllegalArgumentException If any argument is null or if the number of levels is less than 0
     */
    public WmsElevationCoverage(Sector sector, int numLevels, String serviceAddress, String layerNames) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("WmsElevationCoverage", "constructor", "missingSector"));
        }

        if (numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.makeMessage("WmsElevationCoverage", "constructor", "invalidNumLevels"));
        }

        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("WmsElevationCoverage", "constructor", "missingServiceAddress"));
        }

        if (layerNames == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("WmsElevationCoverage", "constructor", "missingLayerNames"));
        }

        LevelSetConfig levelSetConfig = new LevelSetConfig();
        levelSetConfig.sector.set(sector);
        levelSetConfig.numLevels = numLevels;
        this.setLevelSet(new LevelSet(levelSetConfig));

        WmsLayerConfig layerConfig = new WmsLayerConfig();
        layerConfig.serviceAddress = serviceAddress;
        layerConfig.layerNames = layerNames;
        layerConfig.imageFormat = "application/bil16";
        this.setTileFactory(new WmsTileFactory(layerConfig));
    }
}
