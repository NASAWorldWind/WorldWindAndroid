/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.ogc.WmsGetMapUrlFactory;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlFactory;

/**
 * Displays a composite of NASA's Blue Marble next generation imagery at 500m resolution and Landsat imagery at 15m
 * resolution from an OGC Web Map Service (WMS). By default, BlueMarbleLandsatLayer is configured to retrieve imagery
 * from the WMS at <a href="http://worldwind25.arc.nasa.gov/wms?SERVICE=WMS&REQUEST=GetCapabilities">http://worldwind25.arc.nasa.gov/wms</a>.
 */
public class BlueMarbleLandsatLayer extends TiledImageLayer implements TileUrlFactory {

    protected TileUrlFactory blueMarbleUrlFactory;

    protected TileUrlFactory landsatUrlFactory;

    /**
     * Constructs a composite image layer with the WMS at http://worldwind25.arc.nasa.gov/wms.
     */
    public BlueMarbleLandsatLayer() {
        this("http://worldwind25.arc.nasa.gov/wms");
    }

    /**
     * Constructs a composite image layer with the WMS at a specified address.
     *
     * @param serviceAddress a URL string specifying the WMS address
     *
     * @throws IllegalArgumentException If the service address is null
     */
    public BlueMarbleLandsatLayer(String serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BlueMarbleLandsatLayer", "constructor", "missingServiceAddress"));
        }

        // Configure a WMS Get Map URL factory to retrieve Blue Marble tiles.
        WmsLayerConfig blueMarbleConfig = new WmsLayerConfig();
        blueMarbleConfig.serviceAddress = serviceAddress;
        blueMarbleConfig.wmsVersion = "1.3.0";
        blueMarbleConfig.layerNames = "BlueMarble-200405";
        blueMarbleConfig.coordinateSystem = "EPSG:4326";
        blueMarbleConfig.transparent = false; // the BlueMarble layer is opaque
        this.blueMarbleUrlFactory = new WmsGetMapUrlFactory(blueMarbleConfig);

        // Configure a WMS Get Map URL factory to retrieve Blue Marble + Landsat tiles.
        WmsLayerConfig landsatConfig = new WmsLayerConfig();
        landsatConfig.serviceAddress = serviceAddress;
        landsatConfig.wmsVersion = "1.3.0";
        landsatConfig.layerNames = "BlueMarble-200405,esat"; // composite the esat layer over the BlueMarble layer
        landsatConfig.coordinateSystem = "EPSG:4326";
        landsatConfig.transparent = false; // combining BlueMarble and esat layers results in opaque images
        this.landsatUrlFactory = new WmsGetMapUrlFactory(landsatConfig);

        // Configure this layer's level set to capture the entire globe at 15m resolution.
        double metersPerPixel = 15;
        double radiansPerPixel = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;
        LevelSetConfig levelsConfig = new LevelSetConfig();
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel);

        this.setDisplayName("Blue Marble & Landsat");
        this.setLevelSet(new LevelSet(levelsConfig));
        this.setTileUrlFactory(this);
        this.setImageFormat("image/png");
    }

    @Override
    public String urlForTile(Tile tile, String imageFormat) {
        double radiansPerPixel = tile.level.texelHeight;
        double metersPerPixel = radiansPerPixel * WorldWind.WGS84_SEMI_MAJOR_AXIS;

        if (metersPerPixel < 2.0e3) { // switch to Landsat at 2km resolution
            return this.landsatUrlFactory.urlForTile(tile, imageFormat);
        } else {
            return this.blueMarbleUrlFactory.urlForTile(tile, imageFormat);
        }
    }
}
