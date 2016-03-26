/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.TiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;

/**
 * Displays imagery from OGC Web Map Service (WMS) layers.
 * <p/>
 * WMSLayer's configuration may be specified at construction or by calling <code>setConfiguration</code>, and must
 * include the following values: service address, WMS protocol version, layer names, coordinate reference system, sector
 * and resolution. All other WMS configuration values may be unspecified, in which case a default value is used.
 * <p/>
 * WmsLayer defaults to retrieving imagery in the PNG format. This may be configured by calling
 * <code>setImageFormat</code>.
 */
public class WmsLayer extends TiledImageLayer {

    /**
     * WMSLayer's default image format: image/png.
     */
    protected static final String DEFAULT_IMAGE_FORMAT = "image/png";

    // TODO resolution should be something else, like radians/texel

    /**
     * Constructs an empty Web Map Service (WMS) layer that displays nothing.
     */
    public WmsLayer() {
        super("WMS Layer");
        this.setImageFormat(DEFAULT_IMAGE_FORMAT); // establish a default image format
    }

    /**
     * Constructs a Web Map Service (WMS) layer with specified WMS layer configuration values. The configuration must
     * specify the following values: service address, WMS protocol version, layer names, coordinate reference system,
     * sector and resolution. All other WMS configuration values may be unspecified, in which case a default value is
     * used.
     *
     * @param sector     the geographic region in which to display the WMS layer
     * @param resolution the desired resolution in pixels per degree
     * @param config     the WMS layer configuration values
     *
     * @throws IllegalArgumentException If any argument is null, if the resolution is not positive, or if any
     *                                  configuration value is invalid
     */
    public WmsLayer(Sector sector, double resolution, WmsLayerConfig config) {
        super("WMS Layer");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingSector"));
        }

        if (resolution <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"));
        }

        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingConfig"));
        }

        this.setConfiguration(sector, resolution, config);
        this.setImageFormat(DEFAULT_IMAGE_FORMAT); // establish a default image format
    }

    /**
     * Specifies this Web Map Service (WMS) layer's configuration. The configuration must specify the following values:
     * service address, WMS protocol version, layer names, coordinate reference system, sector and resolution. All other
     * WMS configuration values may be unspecified, in which case a default value is used.
     *
     * @param sector     the geographic region in which to display the WMS layer
     * @param resolution the desired resolution in pixels per degree
     * @param config     the WMS layer configuration values
     */
    public void setConfiguration(Sector sector, double resolution, WmsLayerConfig config) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingSector"));
        }

        if (resolution <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"));
        }

        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingConfig"));
        }

        LevelSetConfig levelsConfig = new LevelSetConfig();
        levelsConfig.sector.set(sector);
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(resolution);

        this.setLevelSet(new LevelSet(levelsConfig));
        this.setTileUrlFactory(new WmsGetMapUrlFactory(config));
    }
}
