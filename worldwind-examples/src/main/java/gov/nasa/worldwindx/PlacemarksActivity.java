/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.SurfaceImage;

public class PlacemarksActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle = "About the " + getResources().getText(R.string.title_placemarks);
        this.aboutBoxText = "Demonstrates how to add Placemarks to a RenderableLayer.";

        // Turn off all layers while debugging
        for (Layer l : this.getWorldWindow().getLayers()) {
            l.setEnabled(false);
        }
        // Add a couple simple layers
        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());
        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        this.getWorldWindow().getLayers().addLayer(placemarksLayer);


        // Create a placemark
        Placemark placemark = new Placemark(Position.fromDegrees(34.2, -119.2, 0));
        placemarksLayer.addRenderable(placemark);

        // Position the viewer so that the Placemarks are visible when the activity is created.
        this.getWorldWindow().getNavigator().setLatitude(34.2);
        this.getWorldWindow().getNavigator().setLongitude(-119.2);
        this.getWorldWindow().getNavigator().setAltitude(500000);
    }
}
