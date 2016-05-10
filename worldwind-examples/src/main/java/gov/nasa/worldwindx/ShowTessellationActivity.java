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
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_show_tessellation));
        setAboutBoxText("Demonstrates the globe's Tessellator.\n" +
            "This example adds a layer to the basic globe that shows the tessellation.");

        // Create a layer that displays the globe's tessellation geometry.
        ShowTessellationLayer layer = new ShowTessellationLayer();
        this.getWorldWindow().getLayers().addLayer(layer);
    }
}
