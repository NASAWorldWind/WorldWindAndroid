/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.TileMatrixSet;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

/**
 * Displays NASA's global elevation coverage at 10m resolution within the continental United States, 30m resolution in
 * all other continents, and 900m resolution on the ocean floor, all from an OGC Web Map Service (WMS). By default,
 * BasicElevationCoverage is configured to retrieve elevation coverage from the WMS at <a
 * href="https://wms.worldwind.earth/elev?SERVICE=WMS&amp;REQUEST=GetCapabilities">https://wms.worldwind.earth/elev</a>.
 */
public class BasicElevationCoverage extends TiledElevationCoverage {

    /**
     * Constructs a global elevation coverage with the WMS at https://wms.worldwind.earth/elev.
     */
    public BasicElevationCoverage() {
        this("https://wms.worldwind.earth/elev");
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

        Sector sector = new Sector().setFullSphere();
        int matrixWidth = 4; // 4x2 top level matrix equivalent to 90 degree top level tiles
        int matrixHeight = 2;
        int tileWidth = 256;
        int tileHeight = 256;
        int numLevels = 13;
        this.setTileMatrixSet(TileMatrixSet.fromTilePyramid(sector, matrixWidth, matrixHeight, tileWidth, tileHeight, numLevels));

        WmsLayerConfig layerConfig = new WmsLayerConfig();
        layerConfig.serviceAddress = serviceAddress;
        layerConfig.layerNames = "SRTM-CGIAR,GEBCO";
        layerConfig.imageFormat = "application/bil16";
        final WmsTileFactory wmsTileFactory = new WmsTileFactory(layerConfig);

        this.setTileFactory((tileMatrix, row, column) -> {
            Sector tileSector = tileMatrix.tileSector(row, column);
            String urlString = wmsTileFactory.urlForTile(tileSector, tileMatrix.tileWidth, tileMatrix.tileHeight);
            return ImageSource.fromUrl(urlString);
        });
    }
}
