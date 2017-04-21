/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.TileMatrix;
import gov.nasa.worldwind.geom.TileMatrixSet;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.render.ImageSource;
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

        Sector sector = new Sector().setFullSphere();
        int matrixWidth = 4; // 4x2 top level matrix equivalent to 90 degree top level tiles
        int matrixHeight = 2;
        int tileWidth = 256;
        int tileHeight = 256;
        int numLevels = 13;
        this.setTileMatrixSet(TileMatrixSet.fromTilePyramid(sector, matrixWidth, matrixHeight, tileWidth, tileHeight, numLevels));

        WmsLayerConfig layerConfig = new WmsLayerConfig();
        layerConfig.serviceAddress = serviceAddress;
        layerConfig.layerNames = "GEBCO,aster_v2,USGS-NED";
        layerConfig.imageFormat = "application/bil16";
        final WmsTileFactory wmsTileFactory = new WmsTileFactory(layerConfig);

        this.setTileFactory(new TiledElevationCoverage.TileFactory() {
            @Override
            public ImageSource createTileSource(TileMatrix tileMatrix, int row, int column) {
                Sector sector = tileMatrix.tileSector(row, column);
                String urlString = wmsTileFactory.urlForTile(sector, tileMatrix.tileWidth, tileMatrix.tileHeight);
                return ImageSource.fromUrl(urlString);
            }
        });
    }
}
