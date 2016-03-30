/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.layer.ShowTessellationLayer;

public class ShowTessellationActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a layer that displays the globe's tessellation geometry.
        ShowTessellationLayer layer = new ShowTessellationLayer();

        // Add the WMS layer to the World Window before the Atmosphere layer.
        int index = this.getWorldWindow().getLayers().indexOfLayerNamed("Atmosphere");
        this.getWorldWindow().getLayers().addLayer(index, layer);
    }
}
