/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.util.Log;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerFactory;
import gov.nasa.worldwind.util.Logger;

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
        Layer layer = layerFactory.createFromWms(
            //"http://neowms.sci.gsfc.nasa.gov/wms/wms",
            //"MOD_LSTD_CLIM_M",
            "https://worldwind25.arc.nasa.gov/wms",
            "earthatnight",
            new LayerFactory.Callback() {
                @Override
                public void creationSucceeded(LayerFactory factory, Layer layer) {
                    Log.i("gov.nasa.worldwind", "WMS layer creation succeeded");
                }

                @Override
                public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                    Log.e("gov.nasa.worldwind", "WMS layer creation failed", ex);
                }
            }
        );

        // Add the WMS layer to the World Window.
        wwd.getLayers().addLayer(layer);

        return wwd;
    }
}
