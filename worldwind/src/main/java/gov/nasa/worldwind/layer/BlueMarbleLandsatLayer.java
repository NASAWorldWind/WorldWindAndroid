/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

/**
 * Displays a composite of NASA's Blue Marble next generation imagery at 500m resolution and Landsat imagery at 15m
 * resolution from an OGC Web Map Service (WMS). By default, BlueMarbleLandsatLayer is configured to retrieve imagery
 * from the WMS at <a href="https://worldwind25.arc.nasa.gov/wms?SERVICE=WMS&REQUEST=GetCapabilities">https://worldwind25.arc.nasa.gov/wms</a>.
 */
public class BlueMarbleLandsatLayer extends RenderableLayer implements TileFactory {

    protected TileFactory blueMarbleTileFactory;

    protected TileFactory landsatTileFactory;

    /**
     * Constructs a composite image layer with the WMS at https://worldwind25.arc.nasa.gov/wms.
     */
    public BlueMarbleLandsatLayer() {
        this("https://worldwind25.arc.nasa.gov/wms");
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
        this.blueMarbleTileFactory = new WmsTileFactory(blueMarbleConfig);

        // Configure a WMS Get Map URL factory to retrieve Blue Marble + Landsat tiles.
        WmsLayerConfig landsatConfig = new WmsLayerConfig();
        landsatConfig.serviceAddress = serviceAddress;
        landsatConfig.wmsVersion = "1.3.0";
        landsatConfig.layerNames = "BlueMarble-200405,esat"; // composite the esat layer over the BlueMarble layer
        landsatConfig.coordinateSystem = "EPSG:4326";
        landsatConfig.transparent = false; // combining BlueMarble and esat layers results in opaque images
        this.landsatTileFactory = new WmsTileFactory(landsatConfig);

        // Configure this layer's level set to capture the entire globe at 15m resolution.
        double metersPerPixel = 15;
        double radiansPerPixel = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;
        LevelSetConfig levelsConfig = new LevelSetConfig();
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel);

        this.setDisplayName("Blue Marble & Landsat");
        this.setPickEnabled(false);

        TiledSurfaceImage surfaceImage = new TiledSurfaceImage();
        surfaceImage.setLevelSet(new LevelSet(levelsConfig));
        surfaceImage.setTileFactory(this);
        surfaceImage.setImageOptions(new ImageOptions(WorldWind.RGB_565)); // reduce memory usage by using a 16-bit configuration with no alpha
        this.addRenderable(surfaceImage);
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        double radiansPerPixel = Math.toRadians(level.tileDelta) / level.tileHeight;
        double metersPerPixel = radiansPerPixel * WorldWind.WGS84_SEMI_MAJOR_AXIS;

        if (metersPerPixel < 2.0e3) { // switch to Landsat at 2km resolution
            return this.landsatTileFactory.createTile(sector, level, row, column);
        } else {
            return this.blueMarbleTileFactory.createTile(sector, level, row, column);
        }
    }
}
