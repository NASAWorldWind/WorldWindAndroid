/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.HashMap;
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

public class PlacemarksSelectDragActivity extends BasicGlobeActivity {

    /**
     * The EDITABLE capability, if it exists in a Placemark's user properties, allows editing with a double-tap
     */
    public static final String EDITABLE = "editable";

    /**
     * The MOVABLE capability, if it exists in a Placemark's user properties, allows dragging (after being selected)
     */
    public static final String MOVABLE = "movable";

    /**
     * The SELECTABLE capability, if it exists in a Placemark's user properties, allows selection with single-tap
     */
    public static final String SELECTABLE = "selectable";

    /**
     * Placemark user property key for the type of aircraft
     */
    public static final String AIRCRAFT_TYPE = "aircraft_type";

    // Aircraft types used in the Placemark editing dialog
    public static final String[] aircraftTypes = new String[]{
        "Small Plane",
        "Twin Engine",
        "Passenger Jet",
        "Fighter Jet",
        "Helicopter"
    };

    // Resource ID mapped to the aircraft types
    public static final int[] aircraftIcons = new int[]{
        R.drawable.aircraft_small,
        R.drawable.aircraft_twin,
        R.drawable.aircraft_jet,
        R.drawable.aircraft_fighter,
        R.drawable.helicopter,
    };

    // Aircraft icon maps
    public static final HashMap<String, Integer> aircraftIconMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks));
        setAboutBoxText("Demonstrates how to select and drag Placemarks.");

        // Initialize the mapping of aircraft types to their icons.
        for (int i = 0; i < aircraftTypes.length; i++) {
            aircraftIconMap.put(aircraftTypes[i], aircraftIcons[i]);
        }

        // Get a reference to the WorldWindow view
        WorldWindow wwd = this.getWorldWindow();

        // Add a layer for placemarks to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(layer);

        // Create some placemarks and add them to the layer
        Placemark koxr = createAirportPlacemark(Position.fromDegrees(34.200, -119.207, 0), "Oxnard Airport");
        Placemark kcma = createAirportPlacemark(Position.fromDegrees(34.2138, -119.0944, 0), "Camarillo Airport");
        layer.addRenderable(koxr);
        layer.addRenderable(kcma);

        Placemark aircraft1 = createAircraftPlacemark(Position.fromDegrees(34.200, -119.207, 1000), "Commercial", "Twin Engine");
        Placemark aircraft2 = createAircraftPlacemark(Position.fromDegrees(34.210, -119.150, 2000), "Military", "Fighter Jet");
        Placemark aircraft3 = createAircraftPlacemark(Position.fromDegrees(34.250, -119.207, 1000), "Private", "Small Plane");
        layer.addRenderable(aircraft1);
        layer.addRenderable(aircraft2);
        layer.addRenderable(aircraft3);

        // Override the World Window's built-in navigation behavior with conditional dragging support.
        // The layer object provides the collection of Placemarks used for selection and dragging.
        wwd.setWorldWindowController(new SelectDragNavigateController(layer));

        // And finally, for this demo, position the viewer to look at Oxnard airport
        Position pos = aircraft2.getPosition();
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, 0, WorldWind.ABSOLUTE,
            2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    protected static Placemark createAirportPlacemark(Position position, String airportName) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.airport_terminal));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0); // set normal attributes to 2x original size
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(3.0)); // set highlight attributes to 4x original size
        placemark.setDisplayName(airportName);
        placemark.putUserProperty(SELECTABLE, null);
        return placemark;
    }

    protected static Placemark createAircraftPlacemark(Position position, String aircraftName, String aircraftType) {
        if (!aircraftIconMap.containsKey(aircraftType)) {
            throw new IllegalArgumentException(aircraftType + " is not valid.");
        }
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(aircraftIconMap.get(aircraftType)));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0).setDrawLeader(true); // set normal attributes to 2x original size
        placemark.getAttributes().getLeaderAttributes().setOutlineWidth(4);
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(4.0).setImageColor(new gov.nasa.worldwind.render.Color(Color.YELLOW))); // set highlight attributes to 4x original size
        placemark.setDisplayName(aircraftName);
        placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType);
        placemark.putUserProperty(EDITABLE, null);
        placemark.putUserProperty(MOVABLE, null);
        placemark.putUserProperty(SELECTABLE, null);
        return placemark;
    }

    /**
     * This inner class is a custom WorldWindController that handles picking, dragging and globe navigation via a
     * combination of the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method
     * arbitrates between placemark selection events and globe navigation events.
     */
    public class SelectDragNavigateController extends BasicWorldWindowController
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, AircraftTypeDialog.PlacemarkAircraftTypeListener {

        public float PIXEL_TOLERANCE = 50;

        protected GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), this);

        protected Renderable pickedObject;      // last picked object from onDown events

        protected Renderable selectedObject;    // last "selected" object from single tap or double tap

        protected boolean isDragging = false;

        protected boolean isDraggingArmed = false;

        private Line ray = new Line();          // pre-allocated to avoid memory allocations

        private Vec3 pickPoint = new Vec3();    // pre-allocated to avoid memory allocations

        private RenderableLayer layer;          // collection of Renderables for simulated picking

        /**
         * Constructor
         *
         * @param layer Contains a collection of Renderables to pick from with our simualated picking mechanism.
         */
        public SelectDragNavigateController(RenderableLayer layer) {
            this.layer = layer;
        }

        /**
         * Delegates events to the select/drag handlers or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            // Allow our select and drag handlers to process the event first. They'll set the state flags which will
            // either preempt or allow the event to be subsequently processed by the globe's navigation event handlers.
            boolean consumed = this.gestureDetector.onTouchEvent(event);

            // Is a dragging operation started or in progress? Any ACTION_UP event cancels a drag operation.
            if (this.isDraggingArmed && event.getAction() == MotionEvent.ACTION_UP) {
                this.isDraggingArmed = false;
                this.isDragging = false;
            }

            // If we're not dragging pass on the event on to the default globe navigation handlers
            if (!consumed && !this.isDragging) {
                return super.onTouchEvent(event);
            }
            return consumed;
        }

        /**
         * Notified when a tap occurs with the down {@link MotionEvent} that triggered it.
         */
        @Override
        public boolean onDown(MotionEvent event) {
            pick(event);
            return false;   // By not consuming we allow this event to pass on to the navigation gesture handlers
        }

        /**
         * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the current move {@link
         * MotionEvent}. The distance in x and y is also supplied for convenience.
         */
        @Override
        public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            drag(moveEvent);
            return isDragging;  // Consume this event if dragging is active
        }


        /**
         * Notified when a tap occurs with the up {@link MotionEvent} that triggered it.
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false; // we're using onSingleTapConfirmed
        }

        /**
         * Notified when a single-tap occurs after the detector is confident it's not part of a double tap event.
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // A single tap on a selected object toggles its selected state
            toggleSelection();
            return true;
        }

        /**
         * Notified when a double-tap occurs.
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Double tapping a selected object does not toggle its selected state
            if (this.pickedObject != this.selectedObject) {
                toggleSelection();
            }
            edit();
            return true;
        }

        /**
         * Notified when an event within a double-tap gesture occurs, including the down, move, and up events.
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        /**
         * Notified when a long press occurs with the initial on down {@link MotionEvent} that triggered it.
         */
        @Override
        public void onLongPress(MotionEvent e) {
            pick(e);
            contextMenu();
        }

        /**
         * Not used.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        /**
         * Not implemented
         */
        @Override
        public void onShowPress(MotionEvent e) {
        }

        /**
         * Performs a pick at the tap location and conditionally arms the dragging flag, such that dragging can occur if
         * the next event is an onScroll event.
         */
        public void pick(MotionEvent event) {
            // Pick the object at the tap point
            this.pickedObject = this.simulatedPicking(event.getX(), event.getY());

            // Determine whether the dragging flag should be armed. The prerequisite for dragging that an object must
            // have been previously selected (via a single tap) and the selected object must manifest a "movable"
            // capability.
            this.isDraggingArmed = (pickedObject != null)
                && (this.selectedObject == pickedObject)
                && (this.selectedObject.hasUserProperty(MOVABLE));
        }

        /**
         * Toggles the selected state of the picked object.
         */
        public void toggleSelection() {
            // Test if last picked object is "selectable".  If not, retain the
            // currently selected object. To discard the current selection,
            // the user must pick another selectable object or the current object.
            if (pickedObject != null && pickedObject.hasUserProperty(SELECTABLE)) {

                boolean isNewSelection = pickedObject != this.selectedObject;

                // Display the highlight or normal attributes to indicate the
                // selected or unselected state respectively.
                if (pickedObject instanceof Highlightable) {

                    // Only one object can be selected at time, deselect any previously selected object
                    if (isNewSelection && this.selectedObject instanceof Highlightable) {
                        ((Highlightable) this.selectedObject).setHighlighted(false);
                    }
                    ((Highlightable) pickedObject).setHighlighted(isNewSelection);
                    this.getWorldWindow().requestRedraw();
                }
                // Track the selected object
                this.selectedObject = isNewSelection ? pickedObject : null;
            }
        }

        /**
         * Moves the selected object to the event's screen position.
         */
        public void drag(MotionEvent event) {

            if (this.isDraggingArmed && this.selectedObject != null) {
                if (this.selectedObject instanceof Placemark) {
                    // Signal that dragging has been initiated
                    this.isDragging = true;

                    // Move the placemark to new screen position. The screenPointToGeographic returns the ground
                    // point at the tap point.  For above ground placemarks, we need to set save and restore
                    // the altitude to ensure an above ground placement after the drab event.
                    Placemark placemark = (Placemark) this.selectedObject;
                    double oldAltitude = placemark.getPosition().altitude;
                    screenPointToGroundPosition(event.getX(), event.getY(), placemark.getPosition());
                    placemark.getPosition().altitude = oldAltitude;
                    this.getWorldWindow().requestRedraw();
                }
            }
        }

        /**
         * Edits the currently selected object.
         */
        public void edit() {

            if (this.selectedObject instanceof Placemark && this.selectedObject.hasUserProperty(EDITABLE)) {
                Placemark placemark = (Placemark) this.selectedObject;

                // Pass the current aircraft type in a Bundle
                Bundle args = new Bundle();
                args.putString("title", "Select the " + placemark.getDisplayName() + " Aircraft Type");
                args.putString("type", (String) placemark.getUserProperty(AIRCRAFT_TYPE));

                AircraftTypeDialog dialog = new AircraftTypeDialog();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "aircraft_type");
            }
        }

        @Override
        public void onFinishedAircraftEditing(String aircraftType) {
            Toast.makeText(getApplicationContext(), aircraftType, Toast.LENGTH_SHORT).show();
            if (this.selectedObject instanceof Placemark) {
                Placemark placemark = (Placemark) this.selectedObject;

                String currentType = (String) placemark.getUserProperty(AIRCRAFT_TYPE);
                if (currentType == aircraftType) {
                    return;
                }

                ImageSource imageSource = ImageSource.fromResource(aircraftIconMap.get(aircraftType));
                placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType);
                placemark.getAttributes().setImageSource(imageSource);
                placemark.getHighlightAttributes().setImageSource(imageSource);
                this.getWorldWindow().requestRedraw();
            }
        }

        /**
         * Shows the context menu for the WorldWindow.
         */
        public void contextMenu() {
            Toast.makeText(getApplicationContext(),
                (this.pickedObject == null ? "Nothing" : this.pickedObject.getDisplayName()) + " picked"
                    + (this.pickedObject == this.selectedObject ? " and " : " but not ") + "selected.",
                Toast.LENGTH_LONG).show();
        }

        /**
         * Simple simulation of picking based on the pick point being close to the placemark's geographic position.
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

    /**
     * This inner class manifests a simple Placemark editor for selecting the aircraft type.
     */
    public static class AircraftTypeDialog extends DialogFragment {

        public interface PlacemarkAircraftTypeListener {

            void onFinishedAircraftEditing(String aircraftType);
        }

        private int selection = -1;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String title = args.getString("title", "");
            String type = args.getString("type", "");

            // Determine the initial selection
            for (int i = 0; i < aircraftTypes.length; i++) {
                if (type == aircraftTypes[i]) {
                    this.selection = i;
                    break;
                }
            }

            // Create single selection list of aircraft types
            return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setSingleChoiceItems(aircraftTypes, this.selection,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selection = which;
                        }
                    })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PlacemarksSelectDragActivity activity = (PlacemarksSelectDragActivity) getActivity();
                        PlacemarkAircraftTypeListener listener = (PlacemarkAircraftTypeListener) activity.getWorldWindow().getWorldWindowController();
                        listener.onFinishedAircraftEditing(aircraftTypes[selection]);
                    }
                })
                    // A null handler will close the dialog
                .setNegativeButton(android.R.string.no, null)
                .create();
        }
    }
}
