/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.milstd2525.MilStd2525;

public class PlacemarksMilStd2525StressActivity extends BasicGlobeActivity implements Runnable {

    protected Handler animationHandler = new Handler();

    protected boolean pauseHandler;

    static final int DELAY_TIME = 100;


    static final Executor executor = Executors.newSingleThreadExecutor();

    // There are 9,809 airports in the world airports database.
    protected static final int NUM_AIRPORTS = 9809;

    protected RenderableLayer airportLayer = null;

    protected RenderableLayer aircraftLayer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_milstd2525_stress_test));
        setAboutBoxText("Demonstrates a LOT of MILSTD2525 Placemarks.");

        // Add a Renderable layer for the placemarks before the Atmosphere layer
        this.airportLayer = new RenderableLayer("Airports");
        this.aircraftLayer = new RenderableLayer("Aircraft");
        int index = this.getWorldWindow().getLayers().indexOfLayerNamed("Atmosphere");
        this.getWorldWindow().getLayers().addLayer(index++, airportLayer);
        this.getWorldWindow().getLayers().addLayer(index, aircraftLayer);

        executor.execute(new CreatePlacemarksTask());

        // Create a random number generator with an arbitrary seed
        // that will generate the same values between runs.
        Random random = new Random(123);

//        // Set up an Android Handler to change the view
//        this.animationHandler.postDelayed(this, DELAY_TIME);

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
        //this.animationHandler.postDelayed(this, DELAY_TIME);
    }


    @Override
    public void run() {
        // Move the navigator to simulate the Earth's rotation about its axis.
        //Navigator navigator = getWorldWindow().getNavigator();
        //navigator.setLongitude(navigator.getLongitude() - 0.03);

        // Redraw the World Window to display the above changes.
        getWorldWindow().requestRender();

        if (!this.pauseHandler) { // stop running when this activity is paused; the Handler is resumed in onResume
            this.animationHandler.postDelayed(this, 30);
        }
    }


    protected ArrayList<Position> loadAirportDatabase() {

        ArrayList<Position> airports = new ArrayList<>(NUM_AIRPORTS);

        BufferedReader reader = null;
        try {
            InputStream in = this.getResources().openRawResource(R.raw.world_apts);
            reader = new BufferedReader(new InputStreamReader(in));

            // The first line is the CSV header
            String line = reader.readLine(); //
            String[] headerString = line.split(",");

            // Read the remaining lines which contain the following fields:
            //  LAT,LON,ALT,NAM,IKO,NA3,USE,USEdesc
            while ((line = reader.readLine()) != null) {
                String[] valueString = line.split(",");
                Position pos = Position.fromDegrees(Double.parseDouble(valueString[0]), Double.parseDouble(valueString[1]), 0);
                airports.add(pos);
            }

        } catch (IOException e) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database");
        } finally {
            WWUtil.closeSilently(reader);
        }

        return airports;
    }

    private class CreatePlacemarksTask implements Runnable {

        @Override
        public void run() {
            MilStd2525.initializeRenderer(getApplicationContext());

            // Load the airport collection from the database
            ArrayList<Position> airports = loadAirportDatabase();

            // Create the airports
            getWorldWindow().queueEvent(new CreateAirportPlacemarks(airports, airportLayer));

            //getWorldWindow().queueEvent(new CreateAircraftPlacemarks(airports));

        }
    }

    private static class CreateAirportPlacemarks implements Runnable {

        private final ArrayList<Position> airports;

        private final RenderableLayer layer;

        public CreateAirportPlacemarks(ArrayList<Position> airports, RenderableLayer layer) {
            this.airports = airports;
            this.layer = layer;
        }

        @Override
        public void run() {
            SparseArray<String> modifiers = new SparseArray<String>();
            SparseArray<String> civilAttributes = new SparseArray<String>();
            civilAttributes.put(MilStdAttributes.FillColor, SymbolUtilities.colorToHexString(Color.magenta, false));

            PlacemarkAttributes milAptAttributes = MilStd2525.attributesFromSymbolCode("SFGPIBA---H****", modifiers);
            PlacemarkAttributes civAptAttributes = MilStd2525.attributesFromSymbolCode("SFGPIBA---H***C", modifiers, civilAttributes);
            for (Position position : airports) {

                Placemark airport = new Placemark(position, civAptAttributes);

                // Eye scaling is essential for a reasonable display with a high density of airports
                airport.setEyeDistanceScaling(true);

                layer.addRenderable(airport);
            }
        }
    }

    private static class CreateAircraftPlacemarks implements Runnable {

        private final ArrayList<Position> airports;

        public CreateAircraftPlacemarks(ArrayList<Position> airports) {
            this.airports = airports;
        }

        @Override
        public void run() {

        }
    }
}
