/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;

public class ShowTessellationFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow with a tessellation layer.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer that displays the globe's tessellation geometry.
        ShowTessellationLayer layer = new ShowTessellationLayer();
        wwd.getLayers().addLayer(layer);

        return wwd;
    }
}
