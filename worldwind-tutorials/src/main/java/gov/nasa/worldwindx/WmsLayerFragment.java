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

        // Configure an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        LayerFactory layerFactory = new LayerFactory();
        Layer layer = layerFactory.createWmsLayer(
            "http://neowms.sci.gsfc.nasa.gov/wms/wms",
            "MOD_LSTD_CLIM_M",
            new LayerFactory.Callback() {
                @Override
                public void layerCreated(LayerFactory factory, Layer layer) {
                    Log.d("gov.nasa.worldwind", "MOD_LSTD_CLIM_M created successfully");
                }

                @Override
                public void layerFailed(LayerFactory factory, Layer layer, Throwable ex) {
                    Log.e("gov.nasa.worldwind", "MOD_LSTD_CLIM_M failed: " + (ex != null ? ex.toString() : ""));
                }
            }
        );

        // Add the WMS layer to the World Window.
        wwd.getLayers().addLayer(layer);

        return wwd;
    }
}
