/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.milstd2525.MilStd2525;
import gov.nasa.worldwindx.milstd2525.MilStd2525Placemark;

public class PlacemarksMilStd2525DemoActivity extends GeneralGlobeActivity implements Runnable {

    // The delay in milliseconds between aircraft animation frames
    protected static final int DELAY_TIME = 100;

    // The number of frames for the aircraft animation
    protected static final int ANIMATION_FRAMES = 1000;

    // There are 9,809 airports in the world airports database.
    protected static final int NUM_AIRPORTS = 9809;

    // The number of aircraft in the animation
    protected static final int NUM_AIRCRAFT = 5000;

    protected static final double AIRCRAFT_ALT = 10000; // meters

    // NATO countries
    protected static final List<String> friends = Arrays.asList(
        "US", "CA", "UK", "DA", "FR", "IT", "IC", "NL", "NO", "PO",
        "GR", "TU",
        "GM",
        "SP",
        "EZ", "PL", "HU",
        "SI", "RO", "BU", "LO", "LH", "LG", "EN",
        "AL", "HR");

    // A few neutral countries
    protected static final List<String> neutrals = Arrays.asList("AU", "MX", "SW", "SZ");

    // A few hostile countries
    protected static final List<String> hostiles = Arrays.asList("RS", "IR");

    // The handler for the aircraft animation
    protected Handler handler = new Handler();

    protected boolean animationStared = false;

    protected boolean pauseAnimation = false;

    protected int frameCount = 0;

    // A component for displaying the status of this activity
    protected TextView statusText = null;

    // A collection of aircraft to be animated
    protected HashMap<Placemark, Position> aircraftPositions = new HashMap<>(NUM_AIRCRAFT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_placemarks_milstd2525_demo));
        setAboutBoxText("Demonstrates a LOT of MIL-STD-2525 Placemarks.\n"
            + "There are " + NUM_AIRPORTS + " airports and " + NUM_AIRCRAFT + " aircraft symbols in this example.");

        // Add a TextView on top of the globe to convey the status of this activity
        this.statusText = new TextView(this);
        this.statusText.setTextColor(android.graphics.Color.YELLOW);
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.statusText);

        // Initialize MIL-STD-2525 rendering library and symbols on background threads. AsyncTask tasks
        // are executed serially, ensuring the renderer is initialized before we create symbols.
        new InitializeRendererTask().execute();
        new CreateSymbolsTask().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the animation when this activity is paused.
        this.pauseAnimation = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the Handler that animates the aircraft
        if (animationStared) {
            pauseAnimation = false;
            this.handler.postDelayed(this, DELAY_TIME);
        }
    }

    @Override
    protected void onDestroy() {
        // Release the cached MIL-STD-2525 PlacemarkAttributes
        MilStd2525.clearSymbolCache();
        super.onDestroy();
    }

    /**
     * Initiates the aircraft animation.
     */
    protected void startAnimation() {
        this.statusText.setText("Starting the animation...");
        this.animationStared = true;
        this.pauseAnimation = false;

        // Post this Runnable on the main thread to start the animation
        handler.post(this);

        // Clear the "Starting..." status text after a few seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                statusText.setText("");
            }
        }, 3000);
    }

    /**
     * Animates the aircraft symbols. Each execution of this method is an "animation frame" that updates the aircraft
     * positions on the UI Thread.
     */
    @Override
    public void run() {
        new AnimateAircraftTask().execute();
    }

    /**
     * The Airport class ia a simple POD (plain old data) structure representing an airport from VMAP0 data.
     */
    protected static class Airport {

        static final String MILITARY = "8"; //"Military" USE code

        static final String CIVILIAN = "49";//"Civilian/Public" USE code;

        static final String JOINT = "22";   //"Joint Military/Civilian" USE code;

        static final String OTHER = "999";  //"Other" USE code;

        final Position position;

        final String name;

        final String use;

        final String country;

        Airport(Position position, String name, String use, String country) {
            this.position = position;
            this.name = name;
            this.use = use;
            this.country = country;
        }

        @Override
        public String toString() {
            return "Airport{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", use='" + use + '\'' +
                ", country='" + country + '\'' +
                '}';
        }
    }

    /**
     * InitializeRendererTask is an AsyncTask that initializes the MIL-STD-2525 Rendering Library on a background
     * thread. It must be created and executed on the UI Thread.
     */
    protected class InitializeRendererTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusText.setText("Initializing the MIL-STD-2525 Library...");
        }

        /**
         * Initialize the MIL-STD-2525 Rendering Library.
         */
        @Override
        protected Void doInBackground(Void... notUsed) {
            // Time consuming . . .
            MilStd2525.initializeRenderer(getApplicationContext());
            return null;
        }

        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);
            statusText.setText("");
        }
    }

    /**
     * CreateSymbolsTask is an AsyncTask that initializes the aircraft and airport symbols on a background thread. It
     * must be created and executed on the UI Thread.
     */
    protected class CreateSymbolsTask extends AsyncTask<Void, String, Void> {

        private ArrayList<Airport> airports = new ArrayList<>(NUM_AIRPORTS);

        private RenderableLayer airportLayer = new RenderableLayer();

        private RenderableLayer aircraftLayer = new RenderableLayer();

        /**
         * Loads the aircraft database and creates the placemarks on a background thread. The {@link RenderableLayer}
         * objects for the airport and aircraft symbols have not been attached to the WorldWind at this stage, so its
         * safe to perform this operation on a background thread.  The layers will be added to the WorldWindow in
         * onPostExecute.
         */
        @Override
        protected Void doInBackground(Void... notUsed) {
            loadAirportDatabase();
            createAirportSymbols();
            createAircraftSymbols();
            return null;
        }

        /**
         * Updates the statusText TextView on the UI Thread.
         *
         * @param strings An array of status messages.
         */
        @Override
        protected void onProgressUpdate(String... strings) {
            super.onProgressUpdate(strings);
            statusText.setText(strings[0]);
        }

        /**
         * Updates the WorldWindow layer list on the UI Thread and starts the animation.
         */
        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);

            getWorldWindow().getLayers().addLayer(this.airportLayer);
            getWorldWindow().getLayers().addLayer(this.aircraftLayer);

            statusText.setText("");
            PlacemarksMilStd2525DemoActivity.this.startAnimation();
        }

        /**
         * Loads the VMAP0 world airport data.
         */
        private void loadAirportDatabase() {

            publishProgress("Loading world airports database...");

            BufferedReader reader = null;
            try {
                InputStream in = getResources().openRawResource(R.raw.world_apts);
                reader = new BufferedReader(new InputStreamReader(in));

                // The first line is the CSV header:
                //  LAT,LON,ALT,NAM,IKO,NA3,USE,USEdesc
                String line = reader.readLine();
                List<String> headers = Arrays.asList(line.split(","));
                final int LAT = headers.indexOf("LAT");
                final int LON = headers.indexOf("LON");
                final int NAM = headers.indexOf("NAM");
                final int NA3 = headers.indexOf("NA3");
                final int USE = headers.indexOf("USE");

                // Read the remaining lines
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    Airport airport = new Airport(
                        Position.fromDegrees(Double.parseDouble(fields[LAT]), Double.parseDouble(fields[LON]), 0),
                        fields[NAM],
                        fields[USE],
                        fields[NA3].substring(0, 2));
                    this.airports.add(airport);
                }
            } catch (IOException e) {
                Logger.log(Logger.ERROR, "Exception attempting to read Airports database");
            } finally {
                WWUtil.closeSilently(reader);
            }
        }

        /**
         * Creates airport symbols from the airports collection and adds them to the airports layer.
         */
        private void createAirportSymbols() {

            publishProgress("Creating airport symbols...");

            // Shared rendering attributes
            SparseArray<String> milStdAttributes = new SparseArray<>();
            SparseArray<String> civilianColorAttributes = new SparseArray<>();
            civilianColorAttributes.put(MilStdAttributes.FillColor, SymbolUtilities.colorToHexString(Color.magenta, false));

            Placemark placemark;
            for (Airport airport : this.airports) {
                SparseArray<String> unitModifiers = new SparseArray<>();
                unitModifiers.put(ModifiersUnits.T_UNIQUE_DESIGNATION_1, airport.name);
                if (friends.contains(airport.country)) {
                    switch (airport.use) {
                        case Airport.MILITARY:
                        case Airport.JOINT:
                            placemark = new MilStd2525Placemark(airport.position, "SFGPIBA---H****", unitModifiers, milStdAttributes);
                            break;
                        case Airport.CIVILIAN:
                        case Airport.OTHER:
                            placemark = new MilStd2525Placemark(airport.position, "SFGPIBA---H****", unitModifiers, civilianColorAttributes);
                            break;
                        default:
                            placemark = new MilStd2525Placemark(airport.position, "SUGPIBA---H****", unitModifiers, milStdAttributes);
                    }
                } else if (neutrals.contains(airport.country)) {
                    placemark = new MilStd2525Placemark(airport.position, "SNGPIBA---H****", unitModifiers, milStdAttributes);
                } else if (hostiles.contains(airport.country)) {
                    placemark = new MilStd2525Placemark(airport.position, "SHGPIBA---H****", unitModifiers, milStdAttributes);
                } else {
                    placemark = new MilStd2525Placemark(airport.position, "SUGPIBA---H****", unitModifiers, milStdAttributes);
                }

                // Eye scaling is essential for a reasonable display with a high density of airports
                placemark.setEyeDistanceScalingThreshold(400000);
                placemark.setEyeDistanceScaling(true);

                this.airportLayer.addRenderable(placemark);
            }
        }

        /**
         * Creates aircraft symbols with randomly assigned origin and destination positions, and adds symbols to the
         * aircraft layer.
         */
        private void createAircraftSymbols() {

            publishProgress("Creating aircraft symbols...");

            Random random = new Random(123);
            for (int i = 0; i < NUM_AIRCRAFT; i++) {

                // Randomly assign departure and arrival airports to each aircraft
                Airport departure = this.airports.get(random.nextInt(NUM_AIRPORTS - 1));
                Airport arrival = this.airports.get(random.nextInt(NUM_AIRPORTS - 1));

                // Allocate the end points of the aircraft's flight path.
                Position origin = Position.fromDegrees(departure.position.latitude, departure.position.longitude, AIRCRAFT_ALT);
                Position destination = Position.fromDegrees(arrival.position.latitude, arrival.position.longitude, AIRCRAFT_ALT);

                // Create a MIL-STD-2525 symbol based on the departure airport
                String symbolCode = createAircraftSymbolCode(departure.country, departure.use);
                SparseArray<String> unitModifiers = new SparseArray<>();
                unitModifiers.put(ModifiersUnits.H_ADDITIONAL_INFO_1, "ORIG: " + departure.name);
                unitModifiers.put(ModifiersUnits.G_STAFF_COMMENTS, "DEST: " + arrival.name);

                Placemark placemark = new MilStd2525Placemark(origin, symbolCode, unitModifiers, null);
                placemark.setEyeDistanceScalingThreshold(400000);
                placemark.setEyeDistanceScaling(true);

                // Store these flight path end points in the user properties for the computation of the flight path
                // during the animation frame. The animation will move the aircraft along the great circle route
                // between these two points.
                placemark.putUserProperty("origin", origin);
                placemark.putUserProperty("destination", destination);

                // Add the placemark to the layer that will render it.
                this.aircraftLayer.addRenderable(placemark);

                // Add the aircraft the collection of aircraft positions to be animated. The position in this HashMap
                // is the current position of the aircraft.  It is computed and updated in-place by the
                // AnimateAircraftTask.doInBackground() method, and subsequently the placemark's position is set
                // to this value in the AnimateAircraftTask.onPostExecute() method.
                PlacemarksMilStd2525DemoActivity.this.aircraftPositions.put(placemark, new Position());
            }
        }

        /**
         * Generates a SIDC (symbol identification coding scheme) for an aircraft originating the given county and
         * departure airport use type.
         *
         * @param country    A country code as defined in the airports database.
         * @param airportUse The use code for the departure airport.
         *
         * @return A 15-character alphanumeric identifier.
         */
        private String createAircraftSymbolCode(String country, String airportUse) {

            String identity;
            if (friends.contains(country)) {
                identity = "F";
            } else if (neutrals.contains(country)) {
                identity = "N";
            } else if (hostiles.contains(country)) {
                identity = "H";
            } else {
                identity = "U";
            }
            String type;
            switch (airportUse) {
                case Airport.MILITARY:
                case Airport.JOINT:
                    type = "MF";    // Military fixed wing
                    break;
                case Airport.CIVILIAN:
                case Airport.OTHER:
                    type = "CF";    // Civilian fixed wing
                    break;
                default:
                    type = "--";
            }

            // Adding the country code the the symbol creates more and larger images, but it adds a useful bit
            // of context to the aircraft as they fly across the globe.  Replace country with "**" to reduce the
            // the memory footprint of the image textures.
            return "S" + identity + "AP" + type + "----**" + country + "*";
        }
    }

    /**
     * AnimateAircraftTask is an AsyncTask that computes and updates the aircraft positions. It must be created and
     * executed on the UI Thread.
     */
    protected class AnimateAircraftTask extends AsyncTask<Void, Void, Void> {

        /**
         * Computes the aircraft positions on a background thread.
         */
        @Override
        protected Void doInBackground(Void... params) {
            double amount = (double) frameCount++ / ANIMATION_FRAMES; // fractional amount along path

            for (Placemark aircraft : aircraftPositions.keySet()) {

                // Move the aircraft placemark along its great circle flight path.
                Position origin = (Position) aircraft.getUserProperty("origin");
                Position destination = (Position) aircraft.getUserProperty("destination");
                Position currentPosition = aircraftPositions.get(aircraft);

                // Update the currentPosition members (in-place)
                origin.interpolateAlongPath(destination, WorldWind.GREAT_CIRCLE, amount, currentPosition);
            }
            return null;
        }

        /**
         * Updates the aircraft placemark positions on the UI Thread.
         */
        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);

            // Update the aircraft placemark positions with the positions computed on the background thread.
            for (Placemark aircraft : aircraftPositions.keySet()) {
                aircraft.setPosition(aircraftPositions.get(aircraft));
            }
            getWorldWindow().requestRedraw();

            // Determine if the animation is done
            if (frameCount > ANIMATION_FRAMES) {
                // All the aircraft have arrived at their destinations; pause the animation
                pauseAnimation = true;
                statusText.setText("Animation complete");
            }

            // Re-execute the animation after the prescribed delay
            if (!pauseAnimation) {
                handler.postDelayed(PlacemarksMilStd2525DemoActivity.this, DELAY_TIME);
            }
        }
    }
}
