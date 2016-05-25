/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.Iterator;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Highlightable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class PlacemarksPickingActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_picking));
        setAboutBoxText("Demonstrates how to pick and highlight Placemarks.\n\n" +
            "Tapping a placemark will toggle its highlighted state.");

        // Get a reference to the WorldWindow view
        WorldWindow wwd = this.getWorldWindow();

        // Add a layer for placemarks to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(layer);

        // Override the World Window's built-in navigation behavior with picking support.
        wwd.setWorldWindowController(new PickNavigateController(layer));    // TODO: remove the layer when "real" picking is enabled
        // TODO: Also move above the layer declaration

        // Create a few placemarks with highlight attributes and add them to the layer
        Placemark koxr = createAirportPlacemark(Position.fromDegrees(34.200, -119.207, 0), "Oxnard Airport");
        Placemark kcma = createAirportPlacemark(Position.fromDegrees(34.2138, -119.0944, 0), "Camarillo Airport");
        Placemark kntd = createAirportPlacemark(Position.fromDegrees(34.1193, -119.1196, 0), "Pt Mugu Naval Air Station");
        Placemark plane = createAircraftPlacemark(Position.fromDegrees(34.15, -119.15, 2000));
        layer.addRenderable(koxr);
        layer.addRenderable(kcma);
        layer.addRenderable(kntd);
        layer.addRenderable(plane);

        // And finally, for this demo, position the viewer to look at the aircraft
        Position pos = plane.getPosition();
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, 0, WorldWind.ABSOLUTE,
            2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    /**
     * Helper method to create aircraft placemarks.
     */
    private static Placemark createAircraftPlacemark(Position position) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.aircraft_fighter));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0).setDrawLeader(true); // set normal attributes to 2x original size
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(3.0)); // set highlight attributes to 3x original size
        return placemark;
    }

    /**
     * Helper method to create airport placemarks.
     */
    private static Placemark createAirportPlacemark(Position position, String airportName) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.airport_terminal));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0); // set normal attributes to 2x original size
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(3.0)); // set highlight attributes to 3x original size
        placemark.setDisplayName(airportName);
        return placemark;
    }

    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    public class PickNavigateController extends BasicWorldWindowController {

        public float PIXEL_TOLERANCE = 50;

        protected Renderable pickedObject;      // last picked object from onDown events

        protected Renderable selectedObject;    // last "selected" object from single tap

        private Line ray = new Line();          // pre-allocated to avoid memory allocations

        private Vec3 pickPoint = new Vec3();    // pre-allocated to avoid memory allocations

        private RenderableLayer layer;          // collection of Renderables for simulated picking

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
                return false;       // By not consuming this event, we allow the ACTION_UP event to pass on the navigation gestures
            }
        });

        /**
         * Constructor.
         *
         * @param layer Contains a collection of Renderables to pick from with our simulated picking mechanism.
         */
        public PickNavigateController(RenderableLayer layer) {
            this.layer = layer;
        }

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
            // Pick the object at the tap point
            this.pickedObject = this.simulatedPicking(event.getX(), event.getY());
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

                // Only one object can be selected at time, deselect any previously selected object
                if (isNewSelection && this.selectedObject instanceof Highlightable) {
                    ((Highlightable) this.selectedObject).setHighlighted(false);
                }

                // Show the selection by showing its highlight attributes
                ((Highlightable) pickedObject).setHighlighted(isNewSelection);
                this.getWorldWindow().requestRedraw();

                // Track the selected object
                this.selectedObject = isNewSelection ? pickedObject : null;
            }
        }

        /**
         * Simple simulation of picking based on the pick point being close to the placemark's geographic position.
         * <p/>
         * TODO: Remove simulatedPicking.
         */
        public Renderable simulatedPicking(float pickX, float pickY) {
            Iterator<Renderable> iterator = this.layer.iterator();
            while (iterator.hasNext()) {
                Renderable renderable = iterator.next();
                if (renderable instanceof Placemark) {
                    // Get the screen point for this placemark
                    Placemark placemark = (Placemark) renderable;
                    Position position = placemark.getPosition();
                    PointF point = new PointF();
                    if (this.wwd.geographicToScreenPoint(position.latitude, position.longitude, position.altitude, point)) {

                        // Test if the placemark's screen point is within the tolerance for picking
                        if (point.x <= pickX + PIXEL_TOLERANCE && point.x >= pickX - PIXEL_TOLERANCE &&
                            point.y <= pickY + PIXEL_TOLERANCE && point.y >= pickY - PIXEL_TOLERANCE) {

                            return placemark;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Converts a screen point to the geographic coordinates on the globe.
         *
         * @param screenX X coordinate
         * @param screenY Y coordinate
         * @param result  Pre-allocated Position receives the geographic coordinates
         *
         * @return true if the screen point could be converted; false if the screen point is not on the globe
         */
        public boolean screenPointToGroundPosition(float screenX, float screenY, Position result) {
            if (this.wwd.rayThroughScreenPoint(screenX, screenY, ray)) {
                Globe globe = wwd.getGlobe();
                if (globe.intersect(ray, this.pickPoint)) {
                    globe.cartesianToGeographic(pickPoint.x, this.pickPoint.y, this.pickPoint.z, result);
                    return true;
                }
            }
            return false;
        }
    }
}
