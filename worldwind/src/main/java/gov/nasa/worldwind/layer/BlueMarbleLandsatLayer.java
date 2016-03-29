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
 * Displays a composite of NASA's Blue Marble next generation imagery and Landsat imagery at 15m resolution from an OGC
 * Web Map Service (WMS). By default, BlueMarbleLandsatLayer is configured to retrieve imagery from the WMS at <a
 * href="http://worldwind25.arc.nasa.gov/wms?SERVICE=WMS&REQUEST=GetCapabilities">http://worldwind25.arc.nasa.gov/wms</a>.
 * <p/>
 * Information on NASA's Blue Marble next generation imagery can be found at http://earthobservatory.nasa.gov/Features/BlueMarble/
 */
public class BlueMarbleLandsatLayer extends WmsLayer {

    // TODO can this be modified so that we can use it without BlueMarbleLayer and still get Landsat after zooming in?
    // TODO it seems that such a change could improve current performance.

    /**
     * Constructs a Blue Marble & Landsat image layer with the WMS at http://worldwind25.arc.nasa.gov/wms.
     */
    public BlueMarbleLandsatLayer() {
        this("http://worldwind25.arc.nasa.gov/wms");
    }

    /**
     * Constructs a Blue Marble & Landsat image layer with the WMS at a specified address.
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

        WmsLayerConfig config = new WmsLayerConfig();
        config.serviceAddress = serviceAddress;
        config.wmsVersion = "1.3.0";
        config.layerNames = "BlueMarble-200405,esat"; // composite the esat layer over the BlueMarble layer
        config.coordinateSystem = "EPSG:4326";
        config.transparent = false; // combining BlueMarble and esat layers results in opaque images

        this.setDisplayName("Blue Marble & Landsat");
        this.setConfiguration(new Sector().setFullSphere(), 15, config); // 15m resolution on Earth
    }
}
