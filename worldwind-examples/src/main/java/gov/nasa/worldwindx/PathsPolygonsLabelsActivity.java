/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Highlightable;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.shape.Polygon;
import gov.nasa.worldwind.shape.ShapeAttributes;
import gov.nasa.worldwind.shape.TextAttributes;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

/**
 * This activity demonstrates rendering Labels, Paths and Polygons on the globe. All of the Renderable objects are
 * loaded from .csv files via an AsyncTask. The CreateRenderablesTask creates the individual Renderable objects on a
 * background thread and adds each renderable to the WorldWindow on the UI Thread (essential!) via publishProgress and
 * onProgressUpdate.
 * <p/>
 * This activity also implements a PickController that displays the name of the picked renderable.
 */
public class PathsPolygonsLabelsActivity extends GeneralGlobeActivity {

    // A component for displaying the status of this activity
    protected TextView statusText = null;

    protected RenderableLayer shapesLayer = new RenderableLayer("Shapes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_paths_and_polygons));
        setAboutBoxText("Demonstrates place names rendered as labels, world highways rendered as paths " +
            "and countries rendered as polygons with random interior colors. \n\n" +
            "The name of the object(s) are displayed when picked.");
        // Add a TextView on top of the globe to convey the status of this activity
        this.statusText = new TextView(this);
        this.statusText.setTextColor(android.graphics.Color.YELLOW);
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.statusText);

        // Override the World Window's built-in navigation behavior by adding picking support.
        this.getWorldWindow().setWorldWindowController(new PickController());
        this.getWorldWindow().getLayers().addLayer(this.shapesLayer);

        // Load the shapes into the renderable layer
        statusText.setText("Loading countries....");
        new CreateRenderablesTask().execute();
    }

    /**
     * CreateRenderablesTask is an AsyncTask that initializes the shapes on a background thread. The task must be
     * created and executed on the UI Thread.
     */
    protected class CreateRenderablesTask extends AsyncTask<Void, Renderable, Void> {

        private int numCountriesCreated;

        private int numHighwaysCreated;

        private int numPlacesCreated;

        private Random random = new Random(22);    // Seed the random number generator for random color fills.

        /**
         * Loads the world_highways and world_political_areas files a background thread. The {@link Renderable} objects
         * are added to the RenderableLayer on the UI thread via onProgressUpdate.
         */
        @Override
        protected Void doInBackground(Void... notUsed) {
            loadCountriesFile();
            loadHighways();
            loadPlaceNames();
            return null;
        }

        /**
         * Updates the RenderableLayer on the UI Thread. Invoked by calls to publishProgress.
         *
         * @param renderables An array of Renderables (length = 1) to add to the shapes layer.
         */
        @Override
        protected void onProgressUpdate(Renderable... renderables) {
            super.onProgressUpdate(renderables);
            Renderable shape = renderables[0];
            statusText.setText("Added " + shape.getDisplayName() + " feature...");
            shapesLayer.addRenderable(shape);
            getWorldWindow().requestRedraw();
        }

        /**
         * Updates the status overlay after the background processing is complete.
         */
        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);
            statusText.setText(String.format(Locale.US, "%,d places, %,d highways and %,d countries created",
                this.numPlacesCreated,
                this.numHighwaysCreated,
                this.numCountriesCreated));
            getWorldWindow().requestRedraw();
        }

        /**
         * Creates Label objects from the VMAP0 World Place Names data. Called by doInBackground().
         */
        private void loadPlaceNames() {
            // Define the text attributes used for places
            TextAttributes placeAttrs = new TextAttributes()
                .setTypeface(Typeface.DEFAULT_BOLD)     // Override the normal Typeface
                .setTextSize(28f)                       // default size is 24
                .setTextOffset(Offset.bottomRight());   // anchor the label's bottom-right corner at its position

            // Define the text attribute used for lakes
            TextAttributes lakeAttrs = new TextAttributes()
                .setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC))
                .setTextSize(32f)                           // default size is 24
                .setTextColor(new Color(0f, 1f, 1f, 0.70f))  // cyan, with 7% opacity
                .setTextOffset(Offset.center());            // center the label over its position

            // Load the place names
            BufferedReader reader = null;
            try {
                InputStream in = getResources().openRawResource(R.raw.world_placenames);
                reader = new BufferedReader(new InputStreamReader(in));

                // Process the header in the first line of the CSV file ...
                String line = reader.readLine();
                List<String> headers = Arrays.asList(line.split(","));
                final int LAT = headers.indexOf("LAT");
                final int LON = headers.indexOf("LON");
                final int NAM = headers.indexOf("PLACE_NAME");

                // ... and process the remaining lines in the CSV
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");

                    Label label = new Label(
                        Position.fromDegrees(Double.parseDouble(fields[LAT]), Double.parseDouble(fields[LON]), 0),
                        fields[NAM],
                        fields[NAM].contains("Lake") ? lakeAttrs : placeAttrs);
                    label.setDisplayName(label.getText());

                    // Add the Label object to the RenderableLayer on the UI Thread (see onProgressUpdate)
                    publishProgress(label);

                    this.numPlacesCreated++;
                }
            } catch (IOException e) {
                Logger.log(Logger.ERROR, "Exception attempting to read/parse world_placenames file.");
            } finally {
                WWUtil.closeSilently(reader);
            }
        }

        /**
         * Creates Path objects from the VMAP0 World Highways data. Called by doInBackground().
         */
        private void loadHighways() {
            // Define the normal shape attributes
            ShapeAttributes attrs = new ShapeAttributes();
            attrs.getOutlineColor().set(1.0f, 1.0f, 0.0f, 1.0f);
            attrs.setOutlineWidth(3);

            // Define the shape attributes used for highlighted "highways"
            ShapeAttributes highlightAttrs = new ShapeAttributes();
            highlightAttrs.getOutlineColor().set(1.0f, 0.0f, 0.0f, 1.0f);
            highlightAttrs.setOutlineWidth(7);

            // Load the highways
            BufferedReader reader = null;
            try {
                InputStream in = getResources().openRawResource(R.raw.world_highways);
                reader = new BufferedReader(new InputStreamReader(in));

                // Process the header in the first line of the CSV file ...
                String line = reader.readLine();
                List<String> headers = Arrays.asList(line.split(","));
                final int WKT = headers.indexOf("WKT");
                final int HWY = headers.indexOf("Highway");

                // ... and process the remaining lines in the CSV
                final String WKT_START = "\"LINESTRING (";
                final String WKT_END = ")\"";

                while ((line = reader.readLine()) != null) {
                    // Extract the "well known text"  feature and the attributes
                    // e.g.: "LINESTRING (x.xxx y.yyy,x.xxx y.yyy)",text
                    int featureBegin = line.indexOf(WKT_START) + WKT_START.length();
                    int featureEnd = line.indexOf(WKT_END, featureBegin);
                    String feature = line.substring(featureBegin, featureEnd);
                    String attributes = line.substring(featureEnd + WKT_END.length() + 1);

                    // Buildup the Path. Coordinate tuples are separated by ",".
                    List<Position> positions = new ArrayList<>();
                    String[] tuples = feature.split(",");
                    for (int i = 0; i < tuples.length; i++) {
                        // The XY tuple components a separated by a space
                        String[] xy = tuples[i].split(" ");
                        positions.add(Position.fromDegrees(Double.parseDouble(xy[1]), Double.parseDouble(xy[0]), 0));
                    }
                    Path path = new Path(positions, attrs);
                    path.setHighlightAttributes(highlightAttrs);
                    path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    path.setPathType(WorldWind.LINEAR);
                    path.setFollowTerrain(true);  // essential for preventing long segments from intercepting ellipsoid.
                    path.setDisplayName(attributes);

                    // Add the Path object to the RenderableLayer on the UI Thread (see onProgressUpdate)
                    publishProgress(path);
                    this.numHighwaysCreated++;
                }
            } catch (IOException e) {
                Logger.log(Logger.ERROR, "Exception attempting to read/parse world_highways file.");
            } finally {
                WWUtil.closeSilently(reader);
            }
        }

        /**
         * Creates Polygon objects from the e VMAP0 World Political Areas data. Called by doInBackground().
         */
        private void loadCountriesFile() {
            // Define the normal shape attributes
            ShapeAttributes commonAttrs = new ShapeAttributes();
            commonAttrs.getInteriorColor().set(1.0f, 1.0f, 0.0f, 0.5f);
            commonAttrs.getOutlineColor().set(0.0f, 0.0f, 0.0f, 1.0f);
            commonAttrs.setOutlineWidth(3);

            // Define the shape attributes used for highlighted countries
            ShapeAttributes highlightAttrs = new ShapeAttributes();
            highlightAttrs.getInteriorColor().set(1.0f, 1.0f, 1.0f, 0.5f);
            highlightAttrs.getOutlineColor().set(1.0f, 1.0f, 1.0f, 1.0f);
            highlightAttrs.setOutlineWidth(5);

            // Load the countries
            BufferedReader reader = null;
            try {
                InputStream in = getResources().openRawResource(R.raw.world_political_boundaries);
                reader = new BufferedReader(new InputStreamReader(in));

                // Process the header in the first line of the CSV file ...
                String line = reader.readLine();
                List<String> headers = Arrays.asList(line.split(","));
                final int GEOMETRY = headers.indexOf("WKT");
                final int NAME = headers.indexOf("COUNTRY_NA");

                // ... and process the remaining lines in the CSV
                final String WKT_START = "\"POLYGON (";
                final String WKT_END = ")\"";

                while ((line = reader.readLine()) != null) {
                    // Extract the "well known text" feature and the attributes
                    // e.g.: "POLYGON ((x.xxx y.yyy,x.xxx y.yyy), (x.xxx y.yyy,x.xxx y.yyy))",text,more text,...
                    int featureBegin = line.indexOf(WKT_START) + WKT_START.length();
                    int featureEnd = line.indexOf(WKT_END, featureBegin) + WKT_END.length();
                    String feature = line.substring(featureBegin, featureEnd);
                    String attributes = line.substring(featureEnd + 1);

                    String[] fields = attributes.split(",");

                    Polygon polygon = new Polygon();
                    polygon.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    polygon.setPathType(WorldWind.LINEAR);
                    polygon.setFollowTerrain(true);  // essential for preventing long segments from intercepting ellipsoid.
                    polygon.setDisplayName(fields[1]);
                    polygon.setAttributes(new ShapeAttributes(commonAttrs));
                    polygon.getAttributes().setInteriorColor(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.3f));
                    polygon.setHighlightAttributes(highlightAttrs);

                    // Process all the polygons within this feature by creating "boundaries" for each.
                    // Individual polygons are bounded by "(" and ")"
                    int polyStart = feature.indexOf("(");
                    while (polyStart >= 0) {
                        int polyEnd = feature.indexOf(")", polyStart);
                        String poly = feature.substring(polyStart + 1, polyEnd);

                        // Buildup the Polygon boundaries. Coordinate tuples are separated by ",".
                        List<Position> positions = new ArrayList<>();
                        String[] tuples = poly.split(",");
                        for (int i = 0; i < tuples.length; i++) {
                            // The XY tuple components a separated by a space
                            String[] xy = tuples[i].split(" ");
                            positions.add(Position.fromDegrees(Double.parseDouble(xy[1]), Double.parseDouble(xy[0]), 0));
                        }
                        polygon.addBoundary(positions);

                        // Locate the next polygon in the feature
                        polyStart = feature.indexOf("(", polyEnd);
                    }

                    // Add the Polygon object to the RenderableLayer on the UI Thread (see onProgressUpdate).
                    publishProgress(polygon);

                    this.numCountriesCreated++;
                }
            } catch (IOException e) {
                Logger.log(Logger.ERROR, "Exception attempting to read/parse world_highways file.");
            } finally {
                WWUtil.closeSilently(reader);
            }
        }
    }

    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    public class PickController extends BasicWorldWindowController {

        protected ArrayList<Object> pickedObjects = new ArrayList<>();    // last picked objects from onDown events

        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the "pick" events.
         */
        protected GestureDetector pickGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                pick(event);    // Pick the object(s) at the tap location
                return true;
            }
        });

        /**
         * Delegates events to the pick handler or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean consumed = super.onTouchEvent(event);
            if (!consumed) {
                consumed = this.pickGestureDetector.onTouchEvent(event);
            }
            return consumed;
        }

        /**
         * Performs a pick at the tap location.
         */
        public void pick(MotionEvent event) {
            final int PICK_REGION_SIZE = 40;    // pixels

            // Forget our last picked objects
            togglePickedObjectHighlights();
            this.pickedObjects.clear();

            // Perform a new pick at the screen x, y
            PickedObjectList pickList = getWorldWindow().pickShapesInRect(
                event.getX() - PICK_REGION_SIZE / 2,
                event.getY() - PICK_REGION_SIZE / 2,
                PICK_REGION_SIZE, PICK_REGION_SIZE);

            // pickShapesInRect can return multiple objects, i.e., they're may be more that one 'top object'
            // So we iterate through the list instead of calling pickList.topPickedObject which returns the
            // arbitrary 'first' top object.
            for (int i = 0; i < pickList.count(); i++) {
                if (pickList.pickedObjectAt(i).isOnTop()) {
                    this.pickedObjects.add(pickList.pickedObjectAt(i).getUserObject());
                }
            }
            togglePickedObjectHighlights();
        }

        /**
         * Toggles the highlighted state of a picked object.
         */
        public void togglePickedObjectHighlights() {
            String message = "";
            for (Object pickedObject : pickedObjects) {

                if (pickedObject instanceof Highlightable) {
                    Highlightable highlightable = (Highlightable) pickedObject;
                    highlightable.setHighlighted(!highlightable.isHighlighted());
                    if (highlightable.isHighlighted()) {
                        if (!message.isEmpty()) {
                            message += ", ";
                        }
                        message += ((Renderable) highlightable).getDisplayName();
                    }
                }
            }
            if (!message.isEmpty()) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
            this.getWorldWindow().requestRedraw();
        }
    }
}


