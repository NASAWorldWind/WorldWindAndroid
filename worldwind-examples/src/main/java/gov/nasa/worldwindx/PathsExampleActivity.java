/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.shape.ShapeAttributes;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class PathsExampleActivity extends GeneralGlobeActivity implements Handler.Callback {

    protected ArrayList<Airport> airportTable = new ArrayList<>();

    protected HashMap<String, Airport> airportIkoIndex = new HashMap<>();

    protected RenderableLayer flightPathLayer = new RenderableLayer();

    protected Handler handler = new Handler(this);

    protected double animationAmount = 0;

    protected double animationIncrement = 0.01; // increment 1% each iteration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_paths_example));
        setAboutBoxText("Demonstrates Paths used to animate aircraft great-circle routes from Seattle to other US airports.");
        this.readAirportTable();
        this.populateFlightPaths();
        this.getWorldWindow().getLayers().addLayer(this.flightPathLayer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the animation when this activity is paused.
        this.handler.removeMessages(0 /*what*/);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start or resume the animation.
        this.handler.sendEmptyMessage(0 /*what*/);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (this.animationAmount < 1) {
            // Increment the animation amount.
            this.animationAmount += this.animationIncrement;

            for (int idx = 0, len = this.flightPathLayer.count(); idx < len; idx++) {
                // Identify the departure airport and destination airport associated with each flight path.
                Path path = (Path) this.flightPathLayer.getRenderable(idx);
                Airport dept = (Airport) path.getUserProperty("dept");
                Airport dest = (Airport) path.getUserProperty("dest");

                // Compute the location on the great circle path between the departure and the destination that
                // corresponds to the animation amount.
                Position nextPos = dept.pos.interpolateAlongPath(dest.pos, WorldWind.GREAT_CIRCLE, this.animationAmount, new Position());

                // Compute the altitude on the flight path that corresponds to the animation amount. We mock altitude
                // using an inverse parabolic function scaled to reach a max altitude of 10% of the flight distance.
                double dist = dept.pos.greatCircleDistance(dest.pos) * this.getWorldWindow().getGlobe().getEquatorialRadius();
                double altCurve = (1 - this.animationAmount) * this.animationAmount * 4;
                nextPos.altitude = altCurve * dist * 0.1;

                // Append the location and altitude to the flight path's list of positions.
                List<Position> positions = path.getPositions();
                positions.add(nextPos);
                path.setPositions(positions);
            }

            // Redraw the World Window to display the changes.
            this.getWorldWindow().requestRedraw();

            // Continue the animation after a delay.
            this.handler.sendEmptyMessageDelayed(0 /*what*/, 1000);
        }

        return false;
    }

    protected void readAirportTable() {
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
            final int ALT = headers.indexOf("ALT");
            final int NAM = headers.indexOf("NAM");
            final int IKO = headers.indexOf("IKO");
            final int NA3 = headers.indexOf("NA3");
            final int USE = headers.indexOf("USE");

            // Read the remaining lines
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                Airport apt = new Airport();
                apt.pos.latitude = Double.parseDouble(fields[LAT]);
                apt.pos.longitude = Double.parseDouble(fields[LON]);
                apt.pos.altitude = Double.parseDouble(fields[ALT]);
                apt.nam = fields[NAM];
                apt.iko = fields[IKO];
                apt.na3 = fields[NA3];
                apt.use = fields[USE];

                this.airportTable.add(apt);
                this.airportIkoIndex.put(apt.iko, apt);
            }
        } catch (IOException e) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database");
        } finally {
            WWUtil.closeSilently(reader);
        }
    }

    protected void populateFlightPaths() {
        ShapeAttributes attrs = new ShapeAttributes();
        attrs.getInteriorColor().set(0.8f, 0.8f, 1.0f, 0.8f);
        attrs.getOutlineColor().set(1.0f, 1.0f, 0.0f, 1.0f);

        Airport dept = this.airportIkoIndex.get("KSEA");

        for (Airport dest : this.airportTable) {
            if (dest.equals(dept)) {
                continue; // the destination and departure must be different
            }

            if (dest.iko.length() != 4) {
                continue; // the destination must be a major airfield
            }

            if (!dest.na3.startsWith("US")) {
                continue; // the destination must be in the United States
            }

            if (!dest.use.equals("49")) {
                continue; // the destination must a Civilian/Public airport
            }

            List<Position> positions = new ArrayList<>();
            positions.add(dept.pos);

            Path path = new Path(positions, attrs);
            path.putUserProperty("dept", dept);
            path.putUserProperty("dest", dest);
            this.flightPathLayer.addRenderable(path);
        }
    }

    protected static class Airport {

        public Position pos = new Position();

        public String nam;

        public String iko;

        public String na3;

        public String use;
    }
}
