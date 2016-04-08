/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;

import java.util.Random;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.random;
import static java.lang.Math.toDegrees;

public class PlacemarksActivity extends BasicGlobeActivity implements Runnable {

    protected Handler rotationHandler = new Handler();

    protected boolean pauseHandler;

    static final int DELAY_TIME = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.aboutBoxTitle = "About the " + getResources().getText(R.string.title_placemarks);
        this.aboutBoxText = "Demonstrates how to add Placemarks to a RenderableLayer.";

//        // Turn off all layers while debugging
//        for (Layer l : this.getWorldWindow().getLayers()) {
//            l.setEnabled(false);
//        }
//        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());
        LayerList layers = this.getWorldWindow().getLayers();
        int index = layers.indexOfLayerNamed("Atmosphere");

        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        this.getWorldWindow().getLayers().addLayer(index, placemarksLayer);


//        // Create a placemark
//        Placemark placemark = new Placemark(Position.fromDegrees(34.2, -119.2, 0));
//        placemarksLayer.addRenderable(placemark);

        PlacemarkAttributes attributes = new PlacemarkAttributes();
        attributes.setImageScale(10);

        // Create a random number generator with an arbitrary seed
        // that will generate the same numbers between runs.
        Random random = new Random(123);
        for (int i = 0; i < 10000; i++) {
            attributes.setImageColor(Color.random());

            // Use a random sin to generate the latitude to prevent clustering at the poles
            double lat = toDegrees(asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
            // Generate
            double lon = 180d - (random.nextDouble() * 360);
            Position pos = Position.fromDegrees(lat, lon, 0);

            Placemark placemark = new Placemark(pos, new PlacemarkAttributes(attributes));
            placemarksLayer.addRenderable(placemark);
        }

        // Position the viewer so that the Placemarks are visible when the activity is created.
//        this.getWorldWindow().getNavigator().setLatitude(34.2);
//        this.getWorldWindow().getNavigator().setLongitude(-119.2);
//        this.getWorldWindow().getNavigator().setAltitude(5000000);

        // Set up an Android Handler to change the view
        this.rotationHandler.postDelayed(this, DELAY_TIME);

    }


    @Override
    public void run() {
        // Move the navigator to simulate the Earth's rotation about its axis.
        Navigator navigator = getWorldWindow().getNavigator();
        navigator.setLongitude(navigator.getLongitude() - 0.03);

        // Redraw the World Window to display the above changes.
        getWorldWindow().requestRender();

        if (!this.pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            this.rotationHandler.postDelayed(this, 30);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the Handler when this activity is paused.
        this.pauseHandler = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the Handler that changes earth's rotation.
        this.pauseHandler = false;
        this.rotationHandler.postDelayed(this, DELAY_TIME);
    }
}
