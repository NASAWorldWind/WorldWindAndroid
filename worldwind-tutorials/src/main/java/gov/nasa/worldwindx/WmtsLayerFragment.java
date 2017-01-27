/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.util.Log;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerFactory;

public class WmtsLayerFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a WMTS Layer
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

        // Create an OGC Web Map Tile Service (WMTS) layer to display Global Hillshade based on GMTED2010
        layerFactory.createFromWmts(
            "https://tiles.geoservice.dlr.de/service/wmts", // WMTS server URL
            "hillshade",                                    // WMTS layer identifier
            new LayerFactory.Callback() {
                @Override
                public void creationSucceeded(LayerFactory factory, Layer layer) {
                    // Add the finished WMTS layer to the World Window.
                    getWorldWindow().getLayers().addLayer(layer);
                    Log.i("gov.nasa.worldwind", "WMTS layer creation succeeded");
                }

                @Override
                public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                    // Something went wrong connecting to the WMTS server.
                    Log.e("gov.nasa.worldwind", "WMTS layer creation failed", ex);
                }
            }
        );

        return wwd;
    }
}
