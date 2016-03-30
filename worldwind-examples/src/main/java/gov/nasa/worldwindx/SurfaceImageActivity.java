/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.SurfaceImage;

public class SurfaceImageActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle= "About the " + getResources().getText(R.string.title_surface_image);
        this.aboutBoxText="Demonstrates how to add SurfaceImages to a RenderableLayer.\n" +
            "This example adds two surface images to the basic globe:\n" +
            "1. A remote image showing Mount Etna erupting on July 13th, 2001.\n" +
            "2. The NASA 'Meatball' logo.";

        // Configure a Surface Image to display an Android resource showing the NASA logo.
        Sector sector = new Sector(37.46, 15.5, 0.5, 0.6);
        int resourceId = R.drawable.nasa_logo;
        SurfaceImage surfaceImageResource = new SurfaceImage(sector, resourceId);

        // Configure a Surface Image to display a remote image showing Mount Etna erupting on July 13th, 2001.
        // TODO sector constructor from North, South, East, West coordinates
        sector = new Sector(37.46543388598137, 14.60128369746704, 0.45360804083528, 0.75704283995502);
        String urlString = "http://kml-samples.googlecode.com/svn/trunk/resources/etna.jpg";
        SurfaceImage surfaceImageUrl = new SurfaceImage(sector, urlString);

        // Add a World Window layer that displays the Surface Image, just before the Atmosphere layer.
        RenderableLayer layer = new RenderableLayer("Surface Image");
        layer.addRenderable(surfaceImageResource);
        layer.addRenderable(surfaceImageUrl);
        int index = this.getWorldWindow().getLayers().indexOfLayerNamed("Atmosphere");
        this.getWorldWindow().getLayers().addLayer(index, layer);

        // Position the viewer so that the Surface Images are visible when the activity is created.
        this.getWorldWindow().getNavigator().setLatitude(37.46543388598137);
        this.getWorldWindow().getNavigator().setLongitude(14.97980511744455);
        this.getWorldWindow().getNavigator().setAltitude(4.0e5);
    }
}
