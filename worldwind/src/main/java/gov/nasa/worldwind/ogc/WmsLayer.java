/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
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
public class WmsLayer extends RenderableLayer {

    /**
     * Constructs an empty Web Map Service (WMS) layer that displays nothing.
     */
    public WmsLayer() {
        this.init();
    }

    /**
     * Constructs a Web Map Service (WMS) layer with specified WMS layer configuration values. The configuration must
     * specify the following values: service address, WMS protocol version, layer names, coordinate reference system,
     * sector and resolution. All other WMS configuration values may be unspecified, in which case a default value is
     * used.
     *
     * @param sector         the geographic region in which to display the WMS layer
     * @param metersPerPixel the desired resolution in meters on Earth
     * @param config         the WMS layer configuration values
     *
     * @throws IllegalArgumentException If any argument is null, if the resolution is not positive, or if any
     *                                  configuration value is invalid
     */
    public WmsLayer(Sector sector, double metersPerPixel, WmsLayerConfig config) {
        super("WMS Layer");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingSector"));
        }

        if (metersPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"));
        }

        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingConfig"));
        }

        this.init();
        this.setConfiguration(sector, metersPerPixel, config);
    }

    /**
     * Constructs a Web Map Service (WMS) layer with specified WMS layer configuration values. The configuration must
     * specify the following values: service address, WMS protocol version, layer names, coordinate reference system,
     * sector and resolution. All other WMS configuration values may be unspecified, in which case a default value is
     * used.
     *
     * @param sector         the geographic region in which to display the WMS layer
     * @param metersPerPixel the desired resolution in meters on the specified globe
     * @param config         the WMS layer configuration values
     *
     * @throws IllegalArgumentException If any argument is null, if the resolution is not positive, or if any
     *                                  configuration value is invalid
     */
    public WmsLayer(Sector sector, Globe globe, double metersPerPixel, WmsLayerConfig config) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingSector"));
        }

        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingGlobe"));
        }

        if (metersPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "invalidResolution"));
        }

        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "constructor", "missingConfig"));
        }

        this.init();
        this.setConfiguration(sector, globe, metersPerPixel, config);
    }

    protected void init() {
        this.setDisplayName("WMS Layer");
        this.setPickEnabled(false);
        this.addRenderable(new TiledSurfaceImage());
    }

    /**
     * Specifies this Web Map Service (WMS) layer's configuration. The configuration must specify the following values:
     * service address, WMS protocol version, layer names, coordinate reference system, sector and resolution. All other
     * WMS configuration values may be unspecified, in which case a default value is used.
     *
     * @param sector         the geographic region in which to display the WMS layer
     * @param metersPerPixel the desired resolution in meters on Earth
     * @param config         the WMS layer configuration values
     */
    public void setConfiguration(Sector sector, double metersPerPixel, WmsLayerConfig config) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingSector"));
        }

        if (metersPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"));
        }

        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingConfig"));
        }

        double radiansPerPixel = metersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;

        LevelSetConfig levelsConfig = new LevelSetConfig();
        levelsConfig.sector.set(sector);
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel);

        TiledSurfaceImage surfaceImage = (TiledSurfaceImage) this.getRenderable(0);
        surfaceImage.setLevelSet(new LevelSet(levelsConfig));
        surfaceImage.setTileFactory(new WmsTileFactory(config));
    }

    /**
     * Specifies this Web Map Service (WMS) layer's configuration. The configuration must specify the following values:
     * service address, WMS protocol version, layer names, coordinate reference system, sector and resolution. All other
     * WMS configuration values may be unspecified, in which case a default value is used.
     *
     * @param sector         the geographic region in which to display the WMS layer
     * @param metersPerPixel the desired resolution in meters on the specified globe
     * @param config         the WMS layer configuration values
     */
    public void setConfiguration(Sector sector, Globe globe, double metersPerPixel, WmsLayerConfig config) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingSector"));
        }

        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingGlobe"));
        }

        if (metersPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "invalidResolution"));
        }

        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WmsLayer", "setConfiguration", "missingConfig"));
        }

        double radiansPerPixel = metersPerPixel / globe.getEquatorialRadius();

        LevelSetConfig levelsConfig = new LevelSetConfig();
        levelsConfig.sector.set(sector);
        levelsConfig.numLevels = levelsConfig.numLevelsForResolution(radiansPerPixel);

        TiledSurfaceImage surfaceImage = (TiledSurfaceImage) this.getRenderable(0);
        surfaceImage.setLevelSet(new LevelSet(levelsConfig));
        surfaceImage.setTileFactory(new WmsTileFactory(config));
    }
}
