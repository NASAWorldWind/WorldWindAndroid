/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

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
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import armyc2.c2sd.renderer.utilities.Color;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.milstd2525.MilStd2525;

public class PlacemarksMilStd2525StressActivity extends BasicGlobeActivity implements Runnable {

    protected static final int DELAY_TIME = 100;

    protected static final int ANIMATION_FRAMES = 1000;

    // There are 9,809 airports in the world airports database.
    protected static final int NUM_AIRPORTS = 9809;

    protected static final int NUM_AIRCRAFT = 5000;

    protected static final double AIRCRAFT_ALT = 10000; // meters

    protected static final Executor executor = Executors.newSingleThreadExecutor();

    protected Handler handler = new Handler();

    protected boolean animationStared = false;

    protected boolean pauseAnimation = false;

    protected int frameCount = 0;

    protected RenderableLayer airportLayer = null;

    protected RenderableLayer aircraftLayer = null;

    protected TextView statusText = null;

    protected static final List<String> friends = Arrays.asList(
        // NATO countries
        "US", "CA", "UK", "DA", "FR", "IT", "IC", "NL", "NO", "PO",
        "GR", "TU",
        "GM",
        "SP",
        "EZ", "PL", "HU",
        "SI", "RO", "BU", "LO", "LH", "LG", "EN",
        "AL", "HR");

    protected static final List<String> neutrals = Arrays.asList("AU", "MX", "SW", "SZ");

    protected static final List<String> hostiles = Arrays.asList("RS", "IR");

    public PlacemarksMilStd2525StressActivity() {
        super();
        System.out.println(this.getClass().getSimpleName() + ": constructor called");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_milstd2525_stress_test));
        setAboutBoxText("Demonstrates a LOT of MIL-STD-2525 Placemarks.\n"
            + "There are " + NUM_AIRPORTS + " airports and " + NUM_AIRCRAFT + " aircraft symbols in this example.");

        // Add a TextView on top of the globe to convey the status of this activity
        this.statusText = new TextView(this);
        this.statusText.setTextColor(android.graphics.Color.YELLOW);
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.content_globe);
        globeLayout.addView(this.statusText);

        // Add a Renderable layer for the placemarks before the Atmosphere layer
        this.airportLayer = new RenderableLayer("Airports");
        this.aircraftLayer = new RenderableLayer("Aircraft");
        int index = this.getWorldWindow().getLayers().indexOfLayerNamed("Atmosphere");
        this.getWorldWindow().getLayers().addLayer(index++, airportLayer);
        this.getWorldWindow().getLayers().addLayer(index, aircraftLayer);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Update the status on the UI thread
                handler.post(new StatusTask("Initializing the MIL-STD-2525 Renderer..."));

                // Initialize the military symbol renderer singleton
                MilStd2525.initializeRenderer(getApplicationContext());

                handler.post(new StatusTask("Loading airports and aircraft symbols..."));

                // Load the airport collection from the database on this thread.
                ArrayList<Airport> airports = loadAirportDatabase();

                // Create the airports on the GL Thread
                getWorldWindow().queueEvent(new CreateAirportPlacemarks(airports));

                // Create the aircraft on the GL Thread and start the animation
                getWorldWindow().queueEvent(new CreateAircraftPlacemarks(airports));
            }
        });
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
        this.animationStared = true;
        this.pauseAnimation = false;
        handler.post(new StatusTask("Starting the animation...")); // update the UI on the main thread
        handler.post(this); // post this Runnable on the main thread.
        handler.postDelayed(new StatusTask(""), 3000);   // clear the status text after a few seconds
    }

    /**
     * This Runnable interface updates the aircraft positions.
     */
    @Override
    public void run() {

        // Animate the aircraft positions on the GL Thread
        this.getWorldWindow().queueEvent(new Runnable() {
            @Override
            public void run() {

                // This animation runs for a fixed number of frames. During the animation, aircraft symbols
                // transit from a departure airport to an arrival airport.
                if (++frameCount <= ANIMATION_FRAMES) {
                    Iterator<Renderable> i = aircraftLayer.iterator();
                    for (Renderable r : aircraftLayer) {
                        if (r instanceof Placemark) {

                            // Move the aircraft placemark along its great circle route flight path.
                            Placemark placemark = (Placemark) r;
                            Position origin = (Position) placemark.getUserProperty("origin");
                            Position destination = (Position) placemark.getUserProperty("destination");

                            // Compute the new coordinates on flight path and update the position property in-place.
                            origin.interpolateAlongPath(
                                destination, WorldWind.GREAT_CIRCLE, (double) frameCount / ANIMATION_FRAMES,
                                placemark.getPosition());
                        }
                    }
                    // Redraw the World Window to display the above changes.
                    getWorldWindow().requestRender();

                } else {
                    // The aircraft a arrived at their destinations; pause the animation and generate frame statistics
                    // TODO: Generate frame statistics.
                    pauseAnimation = true;
                    handler.post(new StatusTask("Animation complete."));

                }

                // Re-execute the animation event after the prescribed delay
                if (!pauseAnimation) {
                    handler.postDelayed(this, DELAY_TIME);
                }
            }
        });

    }

    /**
     * Loads and returns the the world airports.
     *
     * @return A collection of Airport plain-old-data structures.
     */
    protected ArrayList<Airport> loadAirportDatabase() {

        ArrayList<Airport> airports = new ArrayList<>(NUM_AIRPORTS);

        BufferedReader reader = null;
        try {
            InputStream in = this.getResources().openRawResource(R.raw.world_apts);
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
                airports.add(airport);
            }
        } catch (IOException e) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database");
        } finally {
            WWUtil.closeSilently(reader);
        }
        return airports;
    }

    /**
     * An simple POD (plain old data) structure representing an airport.
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
     * This Runnable creates the airport placemarks.  An instance of this class should be run on the GL Thread.
     */
    protected class CreateAirportPlacemarks implements Runnable {

        private final ArrayList<Airport> airports;

        public CreateAirportPlacemarks(ArrayList<Airport> airports) {
            this.airports = airports;
        }

        @Override
        public void run() {
            SparseArray<String> unitModifiers = new SparseArray<>();
            SparseArray<String> milStdAttributes = new SparseArray<>();
            SparseArray<String> civilianColorAttributes = new SparseArray<>();
            civilianColorAttributes.put(MilStdAttributes.FillColor, SymbolUtilities.colorToHexString(Color.magenta, false));

            PlacemarkAttributes friendAttributes = MilStd2525.getPlacemarkAttributes("SFGPIBA---H****", unitModifiers, milStdAttributes);
            PlacemarkAttributes hostileAttributes = MilStd2525.getPlacemarkAttributes("SHGPIBA---H****", unitModifiers, milStdAttributes);
            PlacemarkAttributes neutralAttributes = MilStd2525.getPlacemarkAttributes("SNGPIBA---H****", unitModifiers, milStdAttributes);
            PlacemarkAttributes unknownAttributes = MilStd2525.getPlacemarkAttributes("SUGPIBA---H****", unitModifiers, milStdAttributes);
            PlacemarkAttributes civilianAttributes = MilStd2525.getPlacemarkAttributes("SFGPIBA---H****", unitModifiers, civilianColorAttributes);

            for (Airport airport : airports) {
                PlacemarkAttributes placemarkAttributes;
                if (friends.contains(airport.country)) {
                    switch (airport.use) {
                        case Airport.MILITARY:
                        case Airport.JOINT:
                            placemarkAttributes = friendAttributes;
                            break;
                        case Airport.CIVILIAN:
                        case Airport.OTHER:
                            placemarkAttributes = civilianAttributes;
                            break;
                        default:
                            placemarkAttributes = unknownAttributes;
                    }
                } else if (neutrals.contains(airport.country)) {
                    placemarkAttributes = neutralAttributes;
                } else if (hostiles.contains(airport.country)) {
                    placemarkAttributes = hostileAttributes;
                } else {
                    placemarkAttributes = unknownAttributes;
                }

                Placemark placemark = new Placemark(airport.position, placemarkAttributes);

                // Eye scaling is essential for a reasonable display with a high density of airports
                placemark.setEyeDistanceScaling(true);

                airportLayer.addRenderable(placemark);
            }
        }
    }

    /**
     * This Runnable creates the aircraft placemarks and initiates the animation.  An instance of this class should be
     * run on the GL Thread.
     */
    protected class CreateAircraftPlacemarks implements Runnable {

        private final ArrayList<Airport> airports;

        private final Random random = new Random(123);

        public CreateAircraftPlacemarks(ArrayList<Airport> airports) {
            this.airports = airports;
        }

        @Override
        public void run() {

            for (int i = 0; i < NUM_AIRCRAFT; i++) {
                // Randomly assign departure and arrival airports to each aircraft
                Airport departure = airports.get(random.nextInt(NUM_AIRPORTS - 1));
                Airport arrival = airports.get(random.nextInt(NUM_AIRPORTS - 1));

                String symbolCode = createAircraftSymbolCode(departure.country, departure.use);
                PlacemarkAttributes attributes = MilStd2525.getPlacemarkAttributes(symbolCode, null, null);

                // Allocate the end points of the flight path.  The animation will move the aircraft
                // along the great circle route between these two points.
                Position origin = Position.fromDegrees(departure.position.latitude, departure.position.longitude, AIRCRAFT_ALT);
                Position destination = Position.fromDegrees(arrival.position.latitude, arrival.position.longitude, AIRCRAFT_ALT);

                // Store copies of the origin and destination in the user properties for computation of the flight path.
                Placemark placemark = new Placemark(origin, attributes);
                placemark.putUserProperty("origin", origin);
                placemark.putUserProperty("destination", destination);
                placemark.setEyeDistanceScaling(true);

                aircraftLayer.addRenderable(placemark);
            }

            // Start the animation
            PlacemarksMilStd2525StressActivity.this.startAnimation();
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
        protected String createAircraftSymbolCode(String country, String airportUse) {

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

            // Adding the country code the the symboel creates more and larger images, but it adds a useful bit
            // of context to the aircraft as they fly across the globe.  Replace country with "**" to reduce the
            // the memory footprint of the image textures.
            String symbolCode = "S" + identity + "AP" + type + "----**" + country + "*";

            return symbolCode;
        }
    }

    protected class StatusTask implements Runnable {

        private final String statusMessage;

        public StatusTask(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        @Override
        public void run() {
            statusText.setText(statusMessage);
        }
    }
}
