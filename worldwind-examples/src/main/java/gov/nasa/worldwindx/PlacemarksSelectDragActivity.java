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
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.HashMap;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Location;
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
    private static final String[] aircraftTypes = new String[]{
        "Small Plane",
        "Twin Engine",
        "Passenger Jet",
        "Fighter Jet",
        "Helicopter"
    };

    // Resource IDs mapped to the aircraft types
    private static final int[] aircraftIcons = new int[]{
        R.drawable.aircraft_small,
        R.drawable.aircraft_twin,
        R.drawable.aircraft_jet,
        R.drawable.aircraft_fighter,
        R.drawable.helicopter,
    };

    // Aircraft types mapped to icons
    private static final HashMap<String, Integer> aircraftIconMap = new HashMap<>();

    /**
     * A custom WorldWindowController object that handles the select, drag and navigation gestures.
     */
    private SelectDragNavigateController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_select_drag));
        setAboutBoxText("Demonstrates how to select and drag Placemarks.\n\n" +
            "Single-tap an icon to toggle the highlight attributes.\n" +
            "Double-tap an aircraft icon to open an editor.\n" +
            "Dragging a highlighted aircraft icon moves it.\n" +
            "Long-press displays some context information.");

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
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.200, -119.207, 0), "Oxnard Airport"));
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.2138, -119.0944, 0), "Camarillo Airport"));
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.200, -119.207, 1000), "Commercial Aircraft", aircraftTypes[1])); // twin
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.210, -119.150, 2000), "Military Aircraft", aircraftTypes[3])); // fighter
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.250, -119.207, 30), "Private Aircraft", aircraftTypes[0])); // small plane

        // Override the World Window's built-in navigation behavior with conditional dragging support.
        // The layer object provides the collection of Placemarks used for selection and dragging.
        // TODO: Move this method to immediately follow the wwd var declaration
        this.controller = new SelectDragNavigateController(layer);
        wwd.setWorldWindowController(this.controller);

        // And finally, for this demo, position the viewer to look near the aircraft
        LookAt lookAt = new LookAt().set(34.210, -119.150, 0, WorldWind.ABSOLUTE, 2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    /**
     * Helper method to create airport placemarks.
     */
    protected static Placemark createAirportPlacemark(Position position, String airportName) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.airport_terminal));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0); // set normal attributes to 2x original size
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(3.0)); // set highlight attributes to 4x original size
        placemark.setDisplayName(airportName);
        // The select/drag controller will examine a placemark's "capabilities" to determine what operations are applicable:
        placemark.putUserProperty(SELECTABLE, null);
        return placemark;
    }

    /**
     * Helper method to create aircraft placemarks.
     */
    protected static Placemark createAircraftPlacemark(Position position, String aircraftName, String aircraftType) {
        if (!aircraftIconMap.containsKey(aircraftType)) {
            throw new IllegalArgumentException(aircraftType + " is not valid.");
        }
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(aircraftIconMap.get(aircraftType)));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0).setDrawLeader(true); // set normal attributes to 2x original size
        placemark.getAttributes().getLeaderAttributes().setOutlineWidth(4);
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(4.0).setImageColor(new gov.nasa.worldwind.render.Color(Color.YELLOW))); // set highlight attributes to 4x original size
        placemark.setDisplayName(aircraftName);
        // The AIRCRAFT_TYPE property is used to exchange the aircraft type with the AirportTypeDialog
        placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType);
        // The select/drag controller will examine a placemark's "capabilities" to determine what operations are applicable:
        placemark.putUserProperty(SELECTABLE, null);
        placemark.putUserProperty(EDITABLE, null);
        placemark.putUserProperty(MOVABLE, null);
        return placemark;
    }


    /**
     * This inner class is a custom WorldWindController that handles picking, dragging and globe navigation via a
     * combination of the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method
     * arbitrates between select and drag gestures and globe navigation gestures.
     */
    public class SelectDragNavigateController extends BasicWorldWindowController {

        protected float PIXEL_TOLERANCE = 50;

        protected Renderable pickedObject;      // last picked object from onDown events

        protected Renderable selectedObject;    // last "selected" object from single tap or double tap

        protected boolean isDragging = false;

        protected boolean isDraggingArmed = false;

        private Position dragRefPos = new Position();

        private Position dragStartPos = new Position();

        private Position dragEndPos = new Position();

        private PointF dragRefPt = new PointF();

        private Line ray = new Line();          // pre-allocated to avoid memory allocations

        private Vec3 pickPoint = new Vec3();    // pre-allocated to avoid memory allocations

        private RenderableLayer layer;          // collection of Renderables for simulated picking

        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the selection and drag gestures.
         */
        protected GestureDetector selectDragDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                pick(event);        // Pick the object(s) at the tap location
                return false;   // By not consuming this event, we allow it to pass on to the navigation gesture handlers
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                toggleSelection();  // Highlight the picked object
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                //simpleDrag(moveEvent);    // Move the selected object
                //betterDrag(downEvent, moveEvent);    // Move the selected object
                bestDrag(downEvent, moveEvent, distanceX, distanceY);    // Move the selected object
                return isDragging;  // Consume this event if dragging is active
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                // Note that double-tapping should not toggle a "selected" object's selected state
                if (pickedObject != selectedObject) {
                    toggleSelection();
                }
                edit(); // Open the placemark editor
                return true;
            }

            @Override
            public void onLongPress(MotionEvent event) {
                pick(event);
                contextMenu();
            }
        });

        public SelectDragNavigateController(RenderableLayer layer) {
            // The layer contains a collection of Renderables to pick from with our simualated picking mechanism.
            this.layer = layer;
        }

        /**
         * Delegates events to the select/drag handlers or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            // Allow our select and drag handlers to process the event first. They'll set the state flags which will
            // either preempt or allow the event to be subsequently processed by the globe's navigation event handlers.
            boolean consumed = this.selectDragDetector.onTouchEvent(event);

            // Is a dragging operation started or in progress?
            // Any ACTION_UP event cancels a drag operation.
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
         * Performs a pick at the tap location and conditionally arms the dragging flag, so that dragging can occur if
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
         * Moves the selected object's position to the event's screen position.
         */
        public void simpleDrag(MotionEvent event) {

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
         * Moves the selected object to the event's screen position.
         */
        public void betterDrag(MotionEvent downEvent, MotionEvent moveEvent) {
            if (this.isDraggingArmed && this.selectedObject != null) {
                if (this.selectedObject instanceof Placemark) {

                    // First, fire a ray through the initial touch point. If the touch point is above the
                    // horizon there will be no intersection with the globe, in which case we'll
                    // simply ignore the drag operation.
                    if (!screenPointToGroundPosition(downEvent.getX(), downEvent.getY(), dragStartPos)) {
                        return;
                    }

                    // Get a reference to the Placemark's current position, which we'll update later
                    Position position = ((Placemark) this.selectedObject).getPosition();

                    // Start the drag
                    if (!this.isDragging) {
                        // Capture a copy of the initial placemark position. Offsets will be applied to this position.
                        this.dragRefPos.set(position);
                        // Signal that dragging has been initiated
                        this.isDragging = true;
                    }

                    // Now, fire a ray through the current touch point.  If the touch point is above the
                    // horizon there will be no intersection with the globe, in which case this
                    // point will be ignored in the drag operation.
                    if (!screenPointToGroundPosition(moveEvent.getX(), moveEvent.getY(), dragEndPos)) {
                        return;
                    }

                    // Compute the geographic offsets between the two touch points...
                    double dLat = dragEndPos.latitude - dragStartPos.latitude;
                    double dLon = dragEndPos.longitude - dragStartPos.longitude;

                    // ... and apply the offsets to the placemark's initial position, keeping it at the same altitude
                    position.latitude = Location.normalizeLatitude(this.dragRefPos.latitude + dLat);
                    position.longitude = Location.normalizeLongitude(this.dragRefPos.longitude + dLon);

                    getWorldWindow().requestRedraw();
                }
            }
        }

        /**
         * Moves the selected object to the event's screen position.
         */
        public void bestDrag(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            if (this.isDraggingArmed && this.selectedObject != null) {
                if (this.selectedObject instanceof Placemark) {

                    // Signal to other event handlers that dragging is in progress
                    this.isDragging = true;

                    // First we compute the screen coordinates of the position's ground point.  We'll apply the
                    // screen X and Y drag distances to this point, from which we'll compute a new position,
                    // wherein we restore the original position's altitude.
                    Position position = ((Placemark) this.selectedObject).getPosition();
                    double altitude = position.altitude;
                    if (!getWorldWindow().geographicToScreenPoint(position.latitude, position.longitude, 0 /*altitude*/, this.dragRefPt)) {
                        // Probably clipped by near/far clipping plane.
                        this.isDragging = false;
                        return;
                    }
                    // Update the placemark's ground position...
                    if (!screenPointToGroundPosition(this.dragRefPt.x - distanceX, this.dragRefPt.y - distanceY, position)) {
                        // Probably off the globe, so cancel the drag.
                        this.isDragging = false;
                        return;
                    }
                    // ... and restore the altitude
                    position.altitude = altitude;

                    getWorldWindow().requestRedraw();
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
                args.putString("title", "Select the " + placemark.getDisplayName() + "'s type");
                args.putString("type", (String) placemark.getUserProperty(AIRCRAFT_TYPE));

                // The AircraftTypeDialog calls onFinished
                AircraftTypeDialog dialog = new AircraftTypeDialog();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "aircraft_type");
            } else {
                Toast.makeText(getApplicationContext(), this.selectedObject.getDisplayName() + " is not editable.", Toast.LENGTH_LONG).show();
            }
        }


        /**
         * Shows the context information for the WorldWindow.
         */
        public void contextMenu() {
            Toast.makeText(getApplicationContext(),
                (this.pickedObject == null ? "Nothing" : this.pickedObject.getDisplayName()) + " picked and "
                    + (this.selectedObject == null ? "nothing" : this.selectedObject.getDisplayName()) + " selected.",
                Toast.LENGTH_LONG).show();
        }

        /**
         * Simple simulation of picking based on the pick point being close to the placemark's geographic position.
         */
        public Renderable simulatedPicking(float pickX, float pickY) {
            for (Renderable renderable : this.layer) {
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
     * This inner class creates a simple Placemark editor for selecting the aircraft's type.
     */
    public static class AircraftTypeDialog extends DialogFragment {

        private int selectedItem = -1;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String title = args.getString("title", "");
            String type = args.getString("type", "");

            // Determine the initial selection
            for (int i = 0; i < aircraftTypes.length; i++) {
                if (type.equals(aircraftTypes[i])) {
                    this.selectedItem = i;
                    break;
                }
            }

            // Create "single selection" list of aircraft types
            return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setSingleChoiceItems(aircraftTypes, this.selectedItem,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedItem = which;
                        }
                    })
                    // The OK button will update the selected placemark's aircraft type
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onFinished(aircraftTypes[selectedItem]);
                    }
                })
                    // A null handler will close the dialog
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        }

        public void onFinished(String aircraftType) {
            PlacemarksSelectDragActivity activity = (PlacemarksSelectDragActivity) getActivity();
            if (activity.controller.selectedObject instanceof Placemark) {

                Placemark placemark = (Placemark) activity.controller.selectedObject;
                String currentType = (String) placemark.getUserProperty(AIRCRAFT_TYPE);
                if (currentType.equals(aircraftType)) {
                    return;
                }
                // Update the placemark's icon and aircraft type property
                ImageSource imageSource = ImageSource.fromResource(aircraftIconMap.get(aircraftType));
                placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType);
                placemark.getAttributes().setImageSource(imageSource);
                placemark.getHighlightAttributes().setImageSource(imageSource);
                // Show the change
                activity.getWorldWindow().requestRedraw();
            }
        }
    }
}