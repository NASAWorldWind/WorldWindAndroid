/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.util.Logger;

/**
 * Displays Landsat imagery at 15m resolution from an OGC Web Map Service (WMS). By default, LandsatLayer is configured
 * to retrieve imagery from the WMS at <a href="https://worldwind25.arc.nasa.gov/wms?SERVICE=WMS&REQUEST=GetCapabilities">https://worldwind25.arc.nasa.gov/wms</a>.
 */
public class LandsatLayer extends WmsLayer {

    /**
     * Constructs a Landsat image layer with the WMS at https://worldwind25.arc.nasa.gov/wms.
     */
    public LandsatLayer() {
        this("https://worldwind25.arc.nasa.gov/wms");
    }

    /**
     * Constructs a Landsat image layer with the WMS at a specified address.
     *
     * @param serviceAddress a URL string specifying the WMS address
     *
     * @throws IllegalArgumentException If the service address is null
     */
    public LandsatLayer(String serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LandsatLayer", "constructor", "missingServiceAddress"));
        }

        WmsLayerConfig config = new WmsLayerConfig();
        config.serviceAddress = serviceAddress;
        config.wmsVersion = "1.3.0";
        config.layerNames = "esat";
        config.coordinateSystem = "EPSG:4326";

        this.setDisplayName("Landsat");
        this.setConfiguration(new Sector().setFullSphere(), 15, config); // 15m resolution on Earth
    }
}
