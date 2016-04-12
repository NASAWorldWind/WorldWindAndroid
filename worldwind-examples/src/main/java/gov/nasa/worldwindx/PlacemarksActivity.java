/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class PlacemarksActivity extends BasicGlobeActivity {

    protected Handler animationHandler = new Handler();

    protected boolean pauseHandler;

    static final int DELAY_TIME = 100;

    static final int NUM_PLACEMARKS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle = "About the " + getResources().getText(R.string.title_placemarks);
        this.aboutBoxText = "Demonstrates how to add Placemarks to a RenderableLayer.";

        ///////////////////////////////////////////////////////////////////////////
        // First, setup the WorldWind globe to support the rendering of placemarks
        ///////////////////////////////////////////////////////////////////////////

        // Create a RenderableLayer for the placemarks
        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        // Add the new layer to the globe's layer list; for this demo, place it just in front of the Atmosphere layer
        LayerList layers = this.getWorldWindow().getLayers();
        int index = layers.indexOfLayerNamed("Atmosphere");
        layers.addLayer(index, placemarksLayer);

        ///////////////////////////////////
        // Second, create some placemarks
        ///////////////////////////////////

        // Create an image-based placemark at Oxnard Airport, CA. The image is scaled to 2x its original size,
        // with the bottom center of the image anchored at the geographic position.
        Placemark airport = new Placemark(
            Position.fromDegrees(34.200, -119.208, 0),
            PlacemarkAttributes.withImageAndLabel(R.drawable.airport).setImageOffset(Offset.BOTTOM_CENTER).setImageScale(2),
            "Oxnard Airport",  // the display name (optional), it would be used as the label if label were null
            "KOXR", // the label text (optional) that is displayed near the placemark
            false); // defines if the placemark's size is scaled based on the distance to the viewer

        // Create another placemark at nearby downtown Ventura, CA. The placemark is a simple 20x20 cyan square
        // centered on the geographic position.
        Placemark ventura = new Placemark(
            Position.fromDegrees(34.281, -119.293, 0),
            PlacemarkAttributes.defaults().setImageColor(Color.CYAN).setImageScale(20));

        /////////////////////////////////////////////////////
        // Third, add the placemarks to the renderable layer
        /////////////////////////////////////////////////////

        placemarksLayer.addRenderable(airport);
        placemarksLayer.addRenderable(ventura);


        // And finally, for this demo, position the viewer to look at the airport placemark
        // from a tilted perspective when this Android activity is created.
        Position pos = airport.getPosition();
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 80 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }
}
