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

    protected static final int NUM_AIRCRAFT = 1000;

    protected static final double AIRCRAFT_ALT = 10000; // meters

    protected static final Executor executor = Executors.newSingleThreadExecutor();

    protected Handler animationHandler = new Handler();

    protected boolean animationStared = false;

    protected boolean pauseAnimation = false;

    protected int frameCount = 0;

    protected RenderableLayer airportLayer = null;

    protected RenderableLayer aircraftLayer = null;

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

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // TODO: Display "Loading MILSTD2525 Renderer" message

                // Initialize the military symbol renderer singleton
                MilStd2525.initializeRenderer(getApplicationContext());

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
            this.animationHandler.postDelayed(this, DELAY_TIME);
        }
    }

    @Override
    public void run() {

        // Animate the aircraft positions on the GL Thread
        this.getWorldWindow().queueEvent(new Runnable() {
            @Override
            public void run() {
                if (++frameCount <= ANIMATION_FRAMES) {
                    Iterator<Renderable> i = aircraftLayer.iterator();
                    while (i.hasNext()) {
                        Renderable r = i.next();
                        if (r instanceof Placemark) {
                            Placemark placemark = (Placemark) r;
                            Position origin = (Position) placemark.getUserProperty("origin");
                            Position destination = (Position) placemark.getUserProperty("destination");

                            // Compute the new coordinates along the great circle route and update the position in place.
                            origin.interpolateAlongPath(
                                destination, WorldWind.GREAT_CIRCLE, (double) frameCount / ANIMATION_FRAMES,
                                placemark.getPosition());
                        }
                    }
                    // Redraw the World Window to display the above changes.
                    getWorldWindow().requestRender();
                } else {
                    // Finished with the animation.
                    pauseAnimation = true;
                }

                if (!pauseAnimation) { // stop running when this activity is paused; the Handler is resumed in onResume
                    // Re-execute the animation event after the prescribed delay time
                    animationHandler.postDelayed(this, DELAY_TIME);
                }
            }
        });

    }

    protected void startAnimation() {
        this.animationStared = true;
        this.pauseAnimation = false;
        this.run();
    }

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

    protected class CreateAirportPlacemarks implements Runnable {

        private final ArrayList<Airport> airports;

        public CreateAirportPlacemarks(ArrayList<Airport> airports) {
            this.airports = airports;
        }

        @Override
        public void run() {
            SparseArray<String> modifiers = new SparseArray<String>();
            SparseArray<String> civilianColorAttributes = new SparseArray<String>();
            civilianColorAttributes.put(MilStdAttributes.FillColor, SymbolUtilities.colorToHexString(Color.magenta, false));

            PlacemarkAttributes friendAttributes = MilStd2525.attributesFromSymbolCode("SFGPIBA---H****", modifiers);
            PlacemarkAttributes hostileAttributes = MilStd2525.attributesFromSymbolCode("SHGPIBA---H****", modifiers);
            PlacemarkAttributes neutralAttributes = MilStd2525.attributesFromSymbolCode("SNGPIBA---H****", modifiers);
            PlacemarkAttributes unknownAttributes = MilStd2525.attributesFromSymbolCode("SUGPIBA---H****", modifiers);
            PlacemarkAttributes civilianAttributes = MilStd2525.attributesFromSymbolCode("SFGPIBA---H****", modifiers, civilianColorAttributes);

            for (Airport airport : airports) {
                PlacemarkAttributes attributes;
                if (friends.contains(airport.country)) {
                    switch (airport.use) {
                        case Airport.MILITARY:
                        case Airport.JOINT:
                            attributes = friendAttributes;
                            break;
                        case Airport.CIVILIAN:
                        case Airport.OTHER:
                            attributes = civilianAttributes;
                            break;
                        default:
                            attributes = unknownAttributes;
                    }
                } else if (neutrals.contains(airport.country)) {
                    attributes = neutralAttributes;
                } else if (hostiles.contains(airport.country)) {
                    attributes = hostileAttributes;
                } else {
                    attributes = unknownAttributes;
                }

                Placemark placemark = new Placemark(airport.position, attributes);

                // Eye scaling is essential for a reasonable display with a high density of airports
                placemark.setEyeDistanceScaling(true);

                airportLayer.addRenderable(placemark);
            }
        }
    }

    protected class CreateAircraftPlacemarks implements Runnable {

        private final ArrayList<Airport> airports;

        private final Random random = new Random(123);

        public CreateAircraftPlacemarks(ArrayList<Airport> airports) {
            this.airports = airports;
        }

        @Override
        public void run() {
            SparseArray<String> modifiers = new SparseArray<String>();
            SparseArray<String> civilianColorAttributes = new SparseArray<String>();
            civilianColorAttributes.put(MilStdAttributes.FillColor, SymbolUtilities.colorToHexString(Color.magenta, false));

            PlacemarkAttributes milAirTrackAttributes = MilStd2525.attributesFromSymbolCode("SFAPM-----*****", modifiers);
            PlacemarkAttributes civFixedWingAttributes = MilStd2525.attributesFromSymbolCode("SFAPCF----*****", modifiers);

            for (int i = 0; i < NUM_AIRCRAFT; i++) {
                // Randomly assign start and finish airports for the aircraft animation
                Airport start = airports.get(random.nextInt(NUM_AIRPORTS - 1));
                Airport finish = airports.get(random.nextInt(NUM_AIRPORTS - 1));

                // Allocate the end points of the flight path
                Position origin = Position.fromDegrees(start.position.latitude, start.position.longitude, AIRCRAFT_ALT);
                Position destination = Position.fromDegrees(finish.position.latitude, finish.position.longitude, AIRCRAFT_ALT);

                // The Placemark constructor will allocate a mutable copy of the origin for the initial position.
                // Store copies of the origin and destination in the user properties for computation of the flight path.
                Placemark placemark = new Placemark(origin, milAirTrackAttributes);
                placemark.putUserProperty("origin", origin);
                placemark.putUserProperty("destination", destination);
                placemark.setEyeDistanceScaling(true);

                aircraftLayer.addRenderable(placemark);
            }

            // Start the animation
            PlacemarksMilStd2525StressActivity.this.startAnimation();
        }
    }

}
