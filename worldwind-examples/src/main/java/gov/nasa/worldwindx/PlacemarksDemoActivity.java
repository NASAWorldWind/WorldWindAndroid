/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Highlightable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

/**
 * This Activity demonstrates a LOT of Placemarks with varying levels of detail.
 */
public class PlacemarksDemoActivity extends GeneralGlobeActivity {

    // A component for displaying the status of this activity
    protected TextView statusText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_placemarks_demo));
        setAboutBoxText("Demonstrates LOTS (38K) of Placemarks with various levels of detail.\n\n" +
            "Placemarks are conditionally displayed based on the camera distance: \n" +
                " - symbols are based on population and capitol status,\n" +
                " - zoom in to reveal more placemarks,\n" +
                " - picking is supported, touch a placemark to see the place name.");
        // Add a TextView on top of the globe to convey the status of this activity
        this.statusText = new TextView(this);
        this.statusText.setTextColor(android.graphics.Color.YELLOW);
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.statusText);

        // Override the World Window's built-in navigation behavior by adding picking support.
        this.getWorldWindow().setWorldWindowController(new PickController());

        new CreatePlacesTask().execute();
    }

    /**
     * The PlaceLevelOfDetailSelector dynamically sets the PlacemarkAttributes for a Placemark instance. An instance of
     * this class, configured with a Place object, is added to the placemark representing the Place object. The
     * placemark's attribute bundle is selected based on the place's population and its status as a capital.  The
     * placemark's visibility is based on its distance to the camera.
     */
    protected static class PlaceLevelOfDetailSelector implements Placemark.LevelOfDetailSelector {

        protected final static int LEVEL_0_DISTANCE = 2000000;

        protected final static int LEVEL_0_POPULATION = 500000;

        protected final static int LEVEL_1_DISTANCE = 1500000;

        protected final static int LEVEL_1_POPULATION = 250000;

        protected final static int LEVEL_2_DISTANCE = 500000;

        protected final static int LEVEL_2_POPULATION = 100000;

        protected final static int LEVEL_3_DISTANCE = 250000;

        protected final static int LEVEL_3_POPULATION = 50000;

        protected final static int LEVEL_4_DISTANCE = 100000;

        protected final static int LEVEL_4_POPULATION = 10000;

        protected final static int LEVEL_0 = 0;

        protected final static int LEVEL_1 = 1;

        protected final static int LEVEL_2 = 2;

        protected final static int LEVEL_3 = 3;

        protected final static int LEVEL_4 = 4;

        protected final static int LEVEL_5 = 5;

        protected static PlacemarkAttributes defaultAttributes = new PlacemarkAttributes().setImageScale(15).setMinimumImageScale(5);

        protected static HashMap<String, WeakReference<PlacemarkAttributes>> iconCache = new HashMap<>();

        protected final Resources resources;

        protected final Place place;

        protected int lastLevelOfDetail = -1;

        protected boolean lastHighlightState = false;

        protected PlacemarkAttributes attributes;

        /**
         * Constructs a level of detail selector for a Place.
         * @param resources The application resources
         * @param place     The place for which
         */
        public PlaceLevelOfDetailSelector(Resources resources, Place place) {
            this.resources = resources;
            this.place = place;
        }

        /**
         * Gets the active attributes for the current distance to the camera and highlighted state.
         *
         * @param rc
         * @param placemark      The placemark needing a level of detail selection
         * @param cameraDistance The distance from the placemark to the camera (meters)
         */
        @Override
        public void selectLevelOfDetail(RenderContext rc, Placemark placemark, double cameraDistance) {

            boolean highlighted = placemark.isHighlighted();
            boolean highlightChanged = this.lastHighlightState != highlighted;

            // Determine the attributes based on the distance from the camera to the placemark
            if (cameraDistance > LEVEL_0_DISTANCE) {
                if (this.lastLevelOfDetail != LEVEL_0 || highlightChanged) {
                    if (place.population > LEVEL_0_POPULATION || place.isCapital()) {
                        this.attributes = getPlacemarkAttributes(this.resources, this.place);
                    } else {
                        this.attributes = null;
                    }
                    this.lastLevelOfDetail = LEVEL_0;
                }
            } else if (cameraDistance > LEVEL_1_DISTANCE) {
                if (this.lastLevelOfDetail != LEVEL_1 || highlightChanged) {
                    if (place.population > LEVEL_1_POPULATION || place.isCapital()) {
                        this.attributes = getPlacemarkAttributes(this.resources, this.place);
                    } else {
                        this.attributes = null;
                    }
                    this.lastLevelOfDetail = LEVEL_1;
                }
            } else if (cameraDistance > LEVEL_2_DISTANCE) {
                if (this.lastLevelOfDetail != LEVEL_2 || highlightChanged) {
                    if (place.population > LEVEL_2_POPULATION || place.isCapital()) {
                        this.attributes = getPlacemarkAttributes(this.resources, this.place);
                    } else {
                        this.attributes = null;
                    }
                    this.lastLevelOfDetail = LEVEL_2;
                }
            } else if (cameraDistance > LEVEL_3_DISTANCE) {
                if (this.lastLevelOfDetail != LEVEL_3 || highlightChanged) {
                    if (place.population > LEVEL_3_POPULATION || place.isCapital()) {
                        this.attributes = getPlacemarkAttributes(this.resources, this.place);
                    } else {
                        this.attributes = null;
                    }
                    this.lastLevelOfDetail = LEVEL_3;
                }
            } else if (cameraDistance > LEVEL_4_DISTANCE) {
                if (this.lastLevelOfDetail != LEVEL_4 || highlightChanged) {
                    if (place.population > LEVEL_4_POPULATION || place.isCapital()) {
                        this.attributes = getPlacemarkAttributes(this.resources, this.place);
                    } else {
                        this.attributes = null;
                    }
                    this.lastLevelOfDetail = LEVEL_4;
                }
            } else {
                if (this.lastLevelOfDetail != LEVEL_5 || highlightChanged) {
                    this.attributes = getPlacemarkAttributes(this.resources, this.place);
                    this.lastLevelOfDetail = LEVEL_5;
                }
            }

            if (highlightChanged) {
                // Use a distinct set attributes when highlighted, otherwise used the shared attributes
                if (highlighted) {
                    // Create a copy of the shared attributes bundle and increase the scale
                    double scale = this.attributes.getImageScale();
                    this.attributes = new PlacemarkAttributes(this.attributes).setImageScale(scale * 2.0);
                }
            }
            this.lastHighlightState = highlighted;

            // Update the placemark's attributes bundle
            placemark.setAttributes(this.attributes);
        }

        protected static PlacemarkAttributes getPlacemarkAttributes(Resources resources, Place place) {
            int resourceId;
            double scale;

            if (place.population > LEVEL_0_POPULATION) {
                resourceId = R.drawable.btn_rating_star_on_selected;
                scale = 1.3;
            } else if (place.population > LEVEL_1_POPULATION) {
                resourceId = R.drawable.btn_rating_star_on_pressed;
                scale = 1.2;
            } else if (place.population > LEVEL_2_POPULATION) {
                resourceId = R.drawable.btn_rating_star_on_normal;
                scale = 1.1;
            } else if (place.population > LEVEL_3_POPULATION) {
                resourceId = R.drawable.btn_rating_star_off_selected;
                scale = 0.7;
            } else if (place.population > LEVEL_4_POPULATION) {
                resourceId = R.drawable.btn_rating_star_off_pressed;
                scale = 0.6;
            } else {
                resourceId = R.drawable.btn_rating_star_off_normal;
                scale = 0.6;
            }
            if (place.type == Place.NATIONAL_CAPITAL) {
                resourceId = R.drawable.star_big_on;
                scale *= 2.5;
            } else if (place.type == Place.STATE_CAPITAL) {
                resourceId = R.drawable.star_big_on;
                scale *= 1.79;
            }

            // Generate a cache key for this symbol
            String iconKey = resources.toString() + "-" + resourceId + "-" + scale;

            // Look for an attribute bundle in our cache and determine if the cached reference is valid
            WeakReference<PlacemarkAttributes> reference = iconCache.get(iconKey);
            PlacemarkAttributes placemarkAttributes = (reference == null ? null : reference.get());

            // Create the attributes if they haven't been created yet or if they've been released
            if (placemarkAttributes == null) {

                // Create the attributes bundle and add it to the cache.
                // The actual bitmap will be lazily (re)created using a factory.
                placemarkAttributes = createPlacemarkAttributes(resources, resourceId, scale);
                if (placemarkAttributes == null) {
                    throw new IllegalArgumentException("Cannot generate a icon for: " + iconKey);
                }
                // Add a weak reference to the attribute bundle to our cache
                iconCache.put(iconKey, new WeakReference<>(placemarkAttributes));
            }
            return placemarkAttributes;
        }

        protected static PlacemarkAttributes createPlacemarkAttributes(Resources resources, @DrawableRes int resourceId, double scale) {
            PlacemarkAttributes placemarkAttributes = new PlacemarkAttributes();
            // Create a BitmapFactory instance with the values needed to create and recreate the symbol's bitmap
            //IconBitmapFactory factory = new IconBitmapFactory(resources, resourceId);
            //placemarkAttributes.setImageSource(ImageSource.fromBitmapFactory(factory)).setImageScale(scale);
            placemarkAttributes.setImageSource(ImageSource.fromResource(resourceId)).setImageScale(scale).setMinimumImageScale(0.5);
            return placemarkAttributes;
        }
    }

    protected static class IconBitmapFactory implements ImageSource.BitmapFactory {

        /**
         * The default icon to use when the renderer cannot render an image.
         */
        protected static Bitmap defaultImage = android.graphics.BitmapFactory.decodeResource(Resources.getSystem(), android.R.drawable.ic_dialog_alert); // Warning sign

        protected static android.graphics.BitmapFactory.Options options = defaultAndroidBitmapFactoryOptions();

        protected final Resources resources;

        protected final int resourceId;

        public IconBitmapFactory(Resources resources, @DrawableRes int resourceId) {
            this.resources = resources;
            this.resourceId = resourceId;
        }

        @Override
        public Bitmap createBitmap() {
            System.out.println("createBitmap called");
            // Use an Android BitmapFactory to convert the resource to a Bitmap
            Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(this.resources, this.resourceId, options);
            if (bitmap == null) {
                Logger.logMessage(Logger.ERROR, "IconBitmapFactory", "createBitmap", "Failed to decode resource for " + this.resourceId);
                // TODO: File JIRA issue - must return a valid bitmap, else the ImageRetriever repeatedly attempts to create the bitmap.
                return defaultImage;
            }
            // Return the bitmap
            return bitmap;
        }

        protected static android.graphics.BitmapFactory.Options defaultAndroidBitmapFactoryOptions() {
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inScaled = false; // suppress default image scaling; load the image in its native dimensions
            return options;
        }
    }

    /**
     * The Place class ia a simple POD (plain old data) structure representing an airport from NTAD place data.
     */
    protected static class Place {

        static final String PLACE = "Populated Place";

        static final String COUNTY_SEAT = "County Seat";

        static final String STATE_CAPITAL = "State Capital";

        static final String NATIONAL_CAPITAL = "National Capital";

        final Position position;

        final String name;

        final String type;

        final int population;

        Place(Position position, String name, String feature2, int population) {
            this.position = position;
            this.name = name;
            this.population = population;
            // FEATURE2 may contain  multiple types; "-999" is used for a regular populated place
            // Here we extract the most important type
            if (feature2.contains(NATIONAL_CAPITAL)) {
                this.type = NATIONAL_CAPITAL;
            } else if (feature2.contains(STATE_CAPITAL)) {
                this.type = STATE_CAPITAL;
            } else if (feature2.contains(COUNTY_SEAT)) {
                this.type = COUNTY_SEAT;
            } else {
                this.type = PLACE;
            }
        }

        protected boolean isCapital() {
            return (this.type == Place.NATIONAL_CAPITAL) || (this.type == Place.STATE_CAPITAL);
        }

        @Override
        public String toString() {
            return "Place{" +
                "name='" + name + '\'' +
                ", position=" + position +
                ", type='" + type + '\'' +
                ", population=" + population +
                '}';
        }
    }

    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    public class PickController extends BasicWorldWindowController {

        protected Object pickedObject;          // last picked object from onDown events

        protected Object selectedObject;        // last "selected" object from single tap

        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the "pick" events.
         */
        protected GestureDetector pickGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                pick(event);    // Pick the object(s) at the tap location
                return false;   // By not consuming this event, we allow it to pass on to the navigation gesture handlers
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                toggleSelection();  // Highlight the picked object

                // By not consuming this event, we allow the "up" event to pass on to the navigation gestures,
                // which is required for proper zoom gestures.  Consuming this event will cause the first zoom
                // gesture to be ignored.  As an alternative, you can implement onSingleTapConfirmed and consume
                // event as you would expect, with the trade-off being a slight delay tap response.
                return false;
            }
        });

        /**
         * Delegates events to the pick handler or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Allow pick listener to process the event first.
            boolean consumed = this.pickGestureDetector.onTouchEvent(event);

            // If event was not consumed by the pick operation, pass it on the globe navigation handlers
            if (!consumed) {

                // The super class performs the pan, tilt, rotate and zoom
                return super.onTouchEvent(event);
            }
            return consumed;
        }

        /**
         * Performs a pick at the tap location.
         */
        public void pick(MotionEvent event) {
            // Forget our last picked object
            this.pickedObject = null;

            // Perform a new pick at the screen x, y
            PickedObjectList pickList = getWorldWindow().pick(event.getX(), event.getY());

            // Get the top-most object for our new picked object
            PickedObject topPickedObject = pickList.topPickedObject();
            if (topPickedObject != null) {
                this.pickedObject = topPickedObject.getUserObject();
            }
        }

        /**
         * Toggles the selected state of a picked object.
         */
        public void toggleSelection() {

            // Display the highlight or normal attributes to indicate the
            // selected or unselected state respectively.
            if (pickedObject instanceof Highlightable) {

                // Determine if we've picked a "new" object so we know to deselect the previous selection
                boolean isNewSelection = pickedObject != this.selectedObject;

                // Only one object can be selected at time; deselect any previously selected object
                if (isNewSelection && this.selectedObject instanceof Highlightable) {
                    ((Highlightable) this.selectedObject).setHighlighted(false);
                }

                // Show the selection by showing its highlight attributes and enunciating the name
                if (isNewSelection && pickedObject instanceof Renderable) {
                    Toast.makeText(getApplicationContext(), ((Renderable) pickedObject).getDisplayName(), Toast.LENGTH_SHORT).show();
                }
                ((Highlightable) pickedObject).setHighlighted(isNewSelection);
                this.getWorldWindow().requestRedraw();

                // Track the selected object
                this.selectedObject = isNewSelection ? pickedObject : null;
            }
        }
    }

    /**
     * CreatePlacesTask is an AsyncTask that initializes the place icons on a background thread. It must be created and
     * executed on the UI Thread.
     */
    protected class CreatePlacesTask extends AsyncTask<Void, String, Void> {

        private ArrayList<Place> places = new ArrayList<>();

        private RenderableLayer placeLayer = new RenderableLayer();

        private int numPlacesCreated;

        /**
         * Loads the ntad_place database and creates the placemarks on a background thread. The {@link RenderableLayer}
         * objects for the place icons have not been attached to the WorldWind at this stage, so its safe to perform
         * this operation on a background thread.  The layers will be added to the WorldWindow in onPostExecute.
         */
        @Override
        protected Void doInBackground(Void... notUsed) {
            loadPlacesDatabase();
            createPlaceIcons();
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
         * Updates the WorldWindow layer list on the UI Thread.
         */
        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);
            getWorldWindow().getLayers().addLayer(this.placeLayer);
            statusText.setText(String.format(Locale.US, "%,d US places created", this.numPlacesCreated));
            getWorldWindow().requestRedraw();
        }

        /**
         * Called by doInBackground(); loads the National Transportation Atlas Database (NTAD) place data.
         */
        private void loadPlacesDatabase() {
            publishProgress("Loading NTAD place database...");
            BufferedReader reader = null;
            try {
                InputStream in = getResources().openRawResource(R.raw.ntad_place);
                reader = new BufferedReader(new InputStreamReader(in));

                // Process the header in the first line of the CSV file ...
                String line = reader.readLine();
                List<String> headers = Arrays.asList(line.split(","));
                final int LAT = headers.indexOf("LATITUDE");
                final int LON = headers.indexOf("LONGITUDE");
                final int NAM = headers.indexOf("NAME");
                final int POP = headers.indexOf("POP_2010");
                final int TYP = headers.indexOf("FEATURE2");

                // ... and process the remaining lines in the CSV
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    Place place = new Place(
                        Position.fromDegrees(Double.parseDouble(fields[LAT]), Double.parseDouble(fields[LON]), 0),
                        fields[NAM],
                        fields[TYP],
                        Integer.parseInt(fields[POP]));
                    this.places.add(place);
                }
            } catch (IOException e) {
                Logger.log(Logger.ERROR, "Exception attempting to read NTAD Place database");
            } finally {
                WWUtil.closeSilently(reader);
            }
        }

        /**
         * Called by doInBackground(); creates place icons from places collection and adds them to the places layer.
         */
        private void createPlaceIcons() {
            publishProgress("Creating place icons...");
            for (Place place : this.places) {
                // Create and configure a Placemark for this place, using a PlaceLevelOfDetailSelector to
                // dynamically set the PlacemarkAttributes.
                Placemark placemark = new Placemark(place.position, null, place.name);
                placemark.setLevelOfDetailSelector(new PlaceLevelOfDetailSelector(getResources(), place));
                placemark.setEyeDistanceScaling(true);
                placemark.setEyeDistanceScalingThreshold(PlaceLevelOfDetailSelector.LEVEL_1_DISTANCE);

                // On a background thread, we can add Placemarks to a RenderableLayer that is
                // NOT attached to the WorldWindow. If the layer was attached to the WorldWindow
                // then we'd have to do this on the UI thread.  Later, we'll add the layer to
                // WorldWindow on the UI thread in the onPostExecute() method.
                this.placeLayer.addRenderable(placemark);
                this.numPlacesCreated++;
            }
        }
    }
}
