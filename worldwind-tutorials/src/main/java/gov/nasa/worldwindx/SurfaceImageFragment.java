/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.SurfaceImage;

public class SurfaceImageFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow with an additional RenderableLayer containing two SurfaceImages.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Configure a Surface Image to display an Android resource showing the NASA logo.
        Sector sector = new Sector(37.46, 15.5, 0.5, 0.6);
        int resourceId = R.drawable.nasa_logo;
        SurfaceImage surfaceImageResource = new SurfaceImage(sector, ImageSource.fromResource(resourceId));

        // Configure a Surface Image to display a remote image showing Mount Etna erupting on July 13th, 2001.
        // TODO sector constructor from North, South, East, West coordinates
        sector = new Sector(37.46543388598137, 14.60128369746704, 0.45360804083528, 0.75704283995502);
        String urlString = "http://worldwindserver.net/android/images/etna.jpg";
        SurfaceImage surfaceImageUrl = new SurfaceImage(sector, ImageSource.fromUrl(urlString));

        // Add a World Window layer that displays the Surface Image, just before the Atmosphere layer.
        RenderableLayer layer = new RenderableLayer("Surface Image");
        layer.addRenderable(surfaceImageResource);
        layer.addRenderable(surfaceImageUrl);
        wwd.getLayers().addLayer(layer);

        // Position the viewer so that the Surface Images are visible when the activity is created.
        wwd.getNavigator().setLatitude(37.46543388598137);
        wwd.getNavigator().setLongitude(14.97980511744455);
        wwd.getNavigator().setAltitude(4.0e5);

        return wwd;
    }
}
