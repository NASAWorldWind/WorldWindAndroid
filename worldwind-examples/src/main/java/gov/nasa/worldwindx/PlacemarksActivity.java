/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
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
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks));
        setAboutBoxText("Demonstrates how to add Placemarks to a RenderableLayer.");

        ///////////////////////////////////////////////////////////////////////////
        // First, setup the WorldWind globe to support the rendering of placemarks
        ///////////////////////////////////////////////////////////////////////////

        // Create a RenderableLayer for the placemarks
        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        // Add the new layer to the globe's layer list; for this demo, place it just in front of the Atmosphere layer
        LayerList layers = this.getWorldWindow().getLayers();
        int index = layers.indexOfLayerNamed("Atmosphere");
        layers.addLayer(index, placemarksLayer);

        //////////////////////////////////////
        // Second, create some placemarks...
        /////////////////////////////////////

        // Create a simple placemark at downtown Ventura, CA. This placemark is a 20x20 cyan square centered on the
        // geographic position. This placemark demonstrates the creation with a convenient factory method.
        Placemark ventura = Placemark.simple(Position.fromDegrees(34.281, -119.293, 0), new Color(0, 1, 1, 1), 20);

        // Create an image-based placemark of an aircraft above the ground with a leader-line to the surface.
        // This placemark demonstrates creation via a constructor and a convenient PlacemarkAttributes factory method.
        // The image is scaled to 1.5 times its original size.
        Placemark airplane = new Placemark(
            Position.fromDegrees(34.260, -119.2, 5000),
            PlacemarkAttributes.withImageAndLeaderLine(ImageSource.fromResource(R.drawable.air_fixwing)).setImageScale(1.5));

        // Create an image-based placemark with a label at Oxnard Airport, CA. This placemark demonstrates creation
        // with a constructor and a convenient PlacemarkAttributes factory method. The image is scaled to 2x
        // its original size, with the bottom center of the image anchored at the geographic position.
        Placemark airport = new Placemark(
            Position.fromDegrees(34.200, -119.208, 0),
            PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.airport_terminal)).setImageOffset(Offset.bottomCenter()).setImageScale(2),
            "Oxnard Airport");


        // Create an image-based placemark from a bitmap. This placemark demonstrates creation with a
        // constructor and a convenient PlacemarkAttributes factory method. First, a 64x64 bitmap is loaded
        // and then it is passed into the placemark attributes. The the bottom center of the image anchored
        // at the geographic position.
        Bitmap bitmap = BitmapFactory.decodeResource(getWorldWindow().getResources(), R.drawable.ehipcc);
        Placemark wildfire = new Placemark(
            Position.fromDegrees(34.300, -119.25, 0),
            PlacemarkAttributes.withImage(ImageSource.fromBitmap(bitmap)).setImageOffset(Offset.bottomCenter()));


        /////////////////////////////////////////////////////
        // Third, add the placemarks to the renderable layer
        /////////////////////////////////////////////////////

        placemarksLayer.addRenderable(ventura);
        placemarksLayer.addRenderable(airport);
        placemarksLayer.addRenderable(airplane);
        placemarksLayer.addRenderable(wildfire);


        // And finally, for this demo, position the viewer to look at the airport placemark
        // from a tilted perspective when this Android activity is created.
        Position pos = airport.getPosition();
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 80 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }
}
