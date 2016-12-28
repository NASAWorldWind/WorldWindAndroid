/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

/**
 * Configuration values for a WMS layer.
 */
public class WmsLayerConfig {

    /**
     * The WMS service address used to build Get Map URLs.
     */
    public String serviceAddress;

    /**
     * The WMS protocol version. Defaults to 1.3.0.
     */
    public String wmsVersion = "1.3.0";

    /**
     * The comma-separated list of WMS layer names.
     */
    public String layerNames;

    /**
     * The comma-separated list of WMS style names.
     */
    public String styleNames;

    /**
     * The coordinate reference system to use when requesting layers. Defaults to EPSG:4326.
     */
    public String coordinateSystem = "EPSG:4326";

    /**
     * Indicates whether Get Map requests should include transparency.
     */
    public boolean transparent = true;

    /**
     * The image content type to use in Get Map requests.
     */
    public String imageFormat;

    /**
     * The time parameter to include in Get Map requests.
     */
    public String timeString;

    /**
     * Constructs a WMS layer configuration with values all null (or false).
     */
    public WmsLayerConfig() {
    }

    /**
     * Constructs a WMS layer configuration with specified values.
     *
     * @param serviceAddress   the WMS service address
     * @param wmsVersion       the WMS protocol version
     * @param layerNames       comma-separated list of WMS layer names
     * @param styleNames       comma-separated list of WMS style names
     * @param coordinateSystem the coordinate reference system to use when requesting layers
     * @param imageFormat      the image content type to use in Get Map requests
     * @param transparent      indicates whether Get Map requests should include transparency
     * @param timeString       the time parameter to include in Get Map requests
     */
    public WmsLayerConfig(String serviceAddress, String wmsVersion, String layerNames, String styleNames, String coordinateSystem, String imageFormat, boolean transparent, String timeString) {
        this.serviceAddress = serviceAddress;
        this.wmsVersion = wmsVersion;
        this.layerNames = layerNames;
        this.styleNames = styleNames;
        this.coordinateSystem = coordinateSystem;
        this.imageFormat = imageFormat;
        this.transparent = transparent;
        this.timeString = timeString;
    }

    /**
     * Constructs a WMS layer configuration with the minimal values.
     *
     * @param serviceAddress the WMS service address
     * @param layerNames     comma-separated list of WMS layer names
     */
    public WmsLayerConfig(String serviceAddress, String layerNames) {
        this.serviceAddress = serviceAddress;
        this.layerNames = layerNames;
    }
}
