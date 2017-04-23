/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.util.Log;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerFactory;

public class WmsLayerFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a WMS Layer
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer factory, World Wind's general component for creating layers
        // from complex data sources.
        LayerFactory layerFactory = new LayerFactory();

        // Create an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        layerFactory.createFromWms(
            "https://neo.sci.gsfc.nasa.gov/wms/wms", // WMS server URL
            "MOD_LSTD_CLIM_M",                         // WMS layer name
            new LayerFactory.Callback() {
                @Override
                public void creationSucceeded(LayerFactory factory, Layer layer) {
                    // Add the finished WMS layer to the World Window.
                    getWorldWindow().getLayers().addLayer(layer);
                    Log.i("gov.nasa.worldwind", "WMS layer creation succeeded");
                }

                @Override
                public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                    // Something went wrong connecting to the WMS server.
                    Log.e("gov.nasa.worldwind", "WMS layer creation failed", ex);
                }
            }
        );

        return wwd;
    }
}
