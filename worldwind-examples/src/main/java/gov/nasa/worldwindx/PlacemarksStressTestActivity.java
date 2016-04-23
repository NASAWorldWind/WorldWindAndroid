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
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

import static java.lang.Math.asin;
import static java.lang.Math.toDegrees;

public class PlacemarksStressTestActivity extends BasicGlobeActivity implements Runnable {

    protected Handler animationHandler = new Handler();

    protected boolean pauseHandler;

    static final int DELAY_TIME = 100;

    static final int NUM_PLACEMARKS = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_stress_test));
        setAboutBoxText("Demonstrates a LOT of Placemarks.");

        // Turn off all layers while debugging/profiling memory allocations
        for (Layer l : this.getWorldWindow().getLayers()) {
            l.setEnabled(false);
        }
        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());

        // Add a Renderable layer for the placemarks before the Atmosphere layer
        LayerList layers = this.getWorldWindow().getLayers();
        int index = layers.indexOfLayerNamed("Atmosphere");
        RenderableLayer placemarksLayer = new RenderableLayer("Placemarks");
        this.getWorldWindow().getLayers().addLayer(index, placemarksLayer);

        // Create some placemarks at a known locations
        Placemark origin = new Placemark(Position.fromDegrees(0, 0, 1e5),
            PlacemarkAttributes.withImageAndLabelAndLeaderLine(ImageSource.fromResource(R.drawable.pushpin_plain_yellow)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "Origin");
        Placemark northPole = new Placemark(Position.fromDegrees(90, 0, 1e5),
            PlacemarkAttributes.withImageAndLabelAndLeaderLine(ImageSource.fromResource(R.drawable.pushpin_plain_white)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "North Pole");
        Placemark southPole = new Placemark(Position.fromDegrees(-90, 0, 0),
            PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.pushpin_plain_black)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "South Pole");
        Placemark antiMeridian = new Placemark(Position.fromDegrees(0, 180, 0),
            PlacemarkAttributes.withImageAndLabel(ImageSource.fromResource(R.drawable.pushpin_plain_green)).setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN),
                "Anti-meridian");

        placemarksLayer.addRenderable(origin);
        placemarksLayer.addRenderable(northPole);
        placemarksLayer.addRenderable(southPole);
        placemarksLayer.addRenderable(antiMeridian);


        ////////////////////
        // Stress Tests
        ////////////////////

        //Placemark.DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e7;

        // Create a random number generator with an arbitrary seed
        // that will generate the same numbers between runs.
        Random random = new Random(123);


//        // Create "sprinkles" -- random colored squares
//        PlacemarkAttributes attributes = new PlacemarkAttributes();
//        attributes.setImageScale(10);
//        for (int i = 0; i < NUM_PLACEMARKS; i++) {
//            // Generate a random color for this placemark
//            attributes.setImageColor(Color.random());
//            // Create an even distribution of latitude and longitudes
//            // Use a random sin value to generate latitudes without clustering at the poles
//            double lat = toDegrees(asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
//            double lon = 180d - (random.nextDouble() * 360);
//            Position pos = Position.fromDegrees(lat, lon, 0);
//
//            Placemark placemark = new Placemark(pos, new PlacemarkAttributes(attributes));
//            placemark.setEyeDistanceScaling(false);
//            placemarksLayer.addRenderable(placemark);
//        }


        // Create pushpins anchored at the "pinpoints" with eye distance scaling
        PlacemarkAttributes attributes = PlacemarkAttributes.withImage(ImageSource.fromResource(R.drawable.pushpin_plain_red))
            .setImageOffset(PlacemarkAttributes.OFFSET_PUSHPIN);
        for (int i = 0; i < NUM_PLACEMARKS; i++) {
            // Create an even distribution of latitude and longitudes across the globe.
            // Use a random sin value to generate latitudes without clustering at the poles.
            double lat = toDegrees(asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
            double lon = 180d - (random.nextDouble() * 360);
            Position pos = Position.fromDegrees(lat, lon, 0);

            Placemark placemark = new Placemark(pos,
                new PlacemarkAttributes(attributes).setMinimumImageScale(0.5)).setEyeDistanceScaling(true);
            placemark.setDisplayName(placemark.getPosition().toString());

            placemarksLayer.addRenderable(placemark);
        }


//        // Create animated aircraft
//        int aircraft = R.drawable.airplane;
//        PlacemarkAttributes attributes = new PlacemarkAttributes();
//        attributes.setImageOffset(PlacemarkAttributes.OFFSET_CENTER);
//        attributes.setImageSource(aircraft);
//        attributes.setImageScale(1);
//
//        for (int i = 0; i < NUM_PLACEMARKS; i++) {
//
//            // Use a random sin to generate the latitude to prevent clustering at the poles
//            double lat = toDegrees(asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
//            // Generate
//            double lon = 180d - (random.nextDouble() * 360);
//            Position pos = Position.fromDegrees(lat, lon, 0);
//
//            Double aircraftHeading = 90d; //new Double(WWMath.normalizeAngle180(random.nextDouble() * 360d));
//            Placemark placemark = new Placemark(pos, new PlacemarkAttributes(attributes));
//            placemark.setImageRotation(aircraftHeading);
//            placemark.setImageRotationReference(WorldWind.RELATIVE_TO_GLOBE);
//            //placemark.setImageTilt(90d);
//            //placemark.setImageTiltReference(WorldWind.RELATIVE_TO_GLOBE);
//            placemark.putUserProperty("aircraftHeading", aircraftHeading);
//            placemarksLayer.addRenderable(placemark);
//        }
//
//        // Set up an Android Handler to change the view
//        this.animationHandler.postDelayed(this, DELAY_TIME);

    }


    @Override
    public void run() {
        // Move the navigator to simulate the Earth's rotation about its axis.
        Navigator navigator = getWorldWindow().getNavigator();
        navigator.setLongitude(navigator.getLongitude() - 0.03);

        // Redraw the World Window to display the above changes.
        getWorldWindow().requestRender();

        if (!this.pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            this.animationHandler.postDelayed(this, 30);
        }

//        // Aircraft animation
//        this.getWorldWindow().queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                LayerList layers = getWorldWindow().getLayers();
//                int index = layers.indexOfLayerNamed("Placemarks");
//                RenderableLayer layer = (RenderableLayer) layers.getLayer(index);
//                Iterator<Renderable> i = layer.iterator();
//                while (i.hasNext()) {
//                    Renderable r = i.next();
//                    if (r instanceof Placemark) {
//                        Placemark placemark = (Placemark) r;
//                        Double currentHeading = (Double) placemark.getUserProperty("aircraftHeading");
//                        if (currentHeading != null) {
//                            Position pos = placemark.getPosition();
//                            Location endPoint = pos.rhumbLocation(currentHeading, 0.001, new Location());
//                            Location midPoint = pos.interpolateAlongPath(endPoint, WorldWind.RHUMB_LINE, 0.5, new Location());
//                            //double newHeading = midPoint.greatCircleAzimuth(endPoint);
//                            placemark.setPosition(pos.set(midPoint.latitude, midPoint.longitude, pos.altitude));
//                            //placemark.putUserProperty("aircraftHeading", new Double(newHeading));
//                        }
//                    }
//                }
//
//            }
//        });

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
        this.animationHandler.postDelayed(this, DELAY_TIME);
    }
}
