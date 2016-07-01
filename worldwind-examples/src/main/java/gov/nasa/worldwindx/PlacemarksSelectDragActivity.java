/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.Dialog;
import android.content.DialogInterface;
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
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Highlightable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

/**
 * This Activity demonstrates how to implement gesture detectors for picking, selecting, dragging, editing and context.
 * In this example, a custom WorldWindowController is created to handle the tap, scroll and long press gestures.  Also,
 * this example shows how to use a Renderable's "userProperty" to convey capabilities to the controller and to exchange
 * information with an editor.
 * <p/>
 * This example displays a scene with three airports, three aircraft and two automobiles.  You can select, move and edit
 * the vehicles with the single tap, drag, and double-tap gestures accordingly.  The airport icons are pickable, but
 * selectable--performing a long-press on an airport will display its name.
 */
public class PlacemarksSelectDragActivity extends GeneralGlobeActivity {

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
     * Placemark user property vehicleKey for the type of aircraft
     */
    public static final String AIRCRAFT_TYPE = "aircraft_type";

    /**
     * Placemark user property vehicleKey for the type of vehicle
     */
    public static final String AUTOMOTIVE_TYPE = "auotomotive_type";

    // Aircraft vehicleTypes used in the Placemark editing dialog
    private static final String[] aircraftTypes = new String[]{
        "Small Plane",
        "Twin Engine",
        "Passenger Jet",
        "Fighter Jet",
        "Bomber",
        "Helicopter"
    };

    // Vehicle vehicleTypes used in the Placemark editing dialog
    private static final String[] automotiveTypes = new String[]{
        "Car",
        "SUV",
        "4x4",
        "Truck",
        "Jeep",
        "Tank"
    };

    // Resource IDs for aircraft icons
    private static final int[] aircraftIcons = new int[]{
        R.drawable.aircraft_small,
        R.drawable.aircraft_twin,
        R.drawable.aircraft_jet,
        R.drawable.aircraft_fighter,
        R.drawable.aircraft_bomber,
        R.drawable.aircraft_rotor,
    };

    // Resource IDs for vehicle icons
    private static final int[] automotiveIcons = new int[]{
        R.drawable.vehicle_car,
        R.drawable.vehicle_suv,
        R.drawable.vehicle_4x4,
        R.drawable.vehicle_truck,
        R.drawable.vehicle_jeep,
        R.drawable.vehicle_tank,
    };

    private static final double NORMAL_IMAGE_SCALE = 3.0;

    private static final double HIGHLIGHTED_IMAGE_SCALE = 4.0;

    // Aircraft vehicleTypes mapped to icons
    private static final HashMap<String, Integer> aircraftIconMap = new HashMap<>();

    private static final HashMap<String, Integer> automotiveIconMap = new HashMap<>();

    /**
     * A custom WorldWindowController object that handles the select, drag and navigation gestures.
     */
    private SelectDragNavigateController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_placemarks_select_drag));
        setAboutBoxText("Demonstrates how to select and drag Placemarks.\n\n" +
            "Single-tap an icon to toggle its selection.\n" +
            "Double-tap a vehicle icon to open an editor.\n" +
            "Dragging a selected vehicle icon moves it.\n" +
            "Long-press displays some context information.\n\n" +
            "Vehicle icons are selectable, movable, and editable.\n" +
            "Airport icons are display only.");

        // Initialize the mapping of vehicle types to their icons.
        for (int i = 0; i < aircraftTypes.length; i++) {
            aircraftIconMap.put(aircraftTypes[i], aircraftIcons[i]);
        }
        for (int i = 0; i < automotiveTypes.length; i++) {
            automotiveIconMap.put(automotiveTypes[i], automotiveIcons[i]);
        }

        // Get a reference to the WorldWindow view
        WorldWindow wwd = this.getWorldWindow();

        // Override the World Window's built-in navigation behavior with conditional dragging support.
        this.controller = new SelectDragNavigateController();
        wwd.setWorldWindowController(this.controller);

        // Add a layer for placemarks to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(layer);

        // Create some placemarks and add them to the layer
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.2000, -119.2070, 0), "Oxnard Airport"));
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.2138, -119.0944, 0), "Camarillo Airport"));
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.1193, -119.1196, 0), "Pt Mugu Naval Air Station"));
        layer.addRenderable(createAutomobilePlacemark(Position.fromDegrees(34.210, -119.120, 0), "Civilian Vehicle", automotiveTypes[1])); // suv
        layer.addRenderable(createAutomobilePlacemark(Position.fromDegrees(34.210, -119.160, 0), "Military Vehicle", automotiveTypes[4])); // jeep
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.200, -119.207, 1000), "Commercial Aircraft", aircraftTypes[1])); // twin
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.210, -119.150, 2000), "Military Aircraft", aircraftTypes[3])); // fighter
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.150, -119.150, 500), "Private Aircraft", aircraftTypes[0])); // small plane


        // And finally, for this demo, position the viewer to look at the placemarks
        LookAt lookAt = new LookAt().set(34.150, -119.150, 0, WorldWind.ABSOLUTE, 2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    /**
     * Helper method to create airport placemarks.
     */
    protected static Placemark createAirportPlacemark(Position position, String airportName) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.airport_terminal));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(NORMAL_IMAGE_SCALE);
        placemark.setDisplayName(airportName);
        return placemark;
    }

    /**
     * Helper method to create aircraft placemarks. The aircraft are selectable, movable, and editable.
     */
    protected static Placemark createAircraftPlacemark(Position position, String aircraftName, String aircraftType) {
        if (!aircraftIconMap.containsKey(aircraftType)) {
            throw new IllegalArgumentException(aircraftType + " is not valid.");
        }
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(aircraftIconMap.get(aircraftType)));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(NORMAL_IMAGE_SCALE).setDrawLeader(true);
        placemark.getAttributes().getLeaderAttributes().setOutlineWidth(4);
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(HIGHLIGHTED_IMAGE_SCALE).setImageColor(new Color(android.graphics.Color.YELLOW)));
        placemark.setDisplayName(aircraftName);
        // The AIRCRAFT_TYPE property is used to exchange the vehicle type with the VehicleTypeDialog
        placemark.putUserProperty(AIRCRAFT_TYPE, aircraftType);
        // The select/drag controller will examine a placemark's "capabilities" to determine what operations are applicable:
        placemark.putUserProperty(SELECTABLE, null);
        placemark.putUserProperty(EDITABLE, null);
        placemark.putUserProperty(MOVABLE, null);
        return placemark;
    }

    /**
     * Helper method to create vehicle placemarks.
     */
    protected static Placemark createAutomobilePlacemark(Position position, String name, String automotiveType) {
        if (!automotiveIconMap.containsKey(automotiveType)) {
            throw new IllegalArgumentException(automotiveType + " is not valid.");
        }
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(automotiveIconMap.get(automotiveType)));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(NORMAL_IMAGE_SCALE);
        placemark.setHighlightAttributes(new PlacemarkAttributes(placemark.getAttributes()).setImageScale(HIGHLIGHTED_IMAGE_SCALE).setImageColor(new Color(android.graphics.Color.YELLOW)));
        placemark.setDisplayName(name);
        // The AUTOMOTIVE_TYPE property is used to exchange the vehicle type with the VehicleTypeDialog
        placemark.putUserProperty(AUTOMOTIVE_TYPE, automotiveType);
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

        protected Renderable pickedObject;    // last picked object from onDown events

        protected Renderable selectedObject;  // last "selected" object from single tap or double tap

        protected boolean isDragging = false;

        protected boolean isDraggingArmed = false;

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
                pick(event);    // Pick the object(s) at the tap location
                return false;   // By not consuming this event, we allow it to pass on to the navigation gesture handlers
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                // This single-tap handler has a faster response time than onSingleTapConfirmed.
                toggleSelection();

                // We do not consume this event; we allow the "up" event to pass on to the navigation gestures,
                // which is required for proper zoom gestures.  Consuming this event will cause the first zoom
                // gesture to be ignored.
                //
                // A drawback to using this callback is that a the first tap of a double-tapping will temporarily
                // deselect an item, only to reselected on the second tap.
                //
                // As an alternative, you can implement onSingleTapConfirmed and consume event as you would expect,
                // with the trade-off being a slight delay in the tap response time.
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                if (isDraggingArmed) {
                    return drag(downEvent, moveEvent, distanceX, distanceY);    // Move the selected object
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                // Note that double-tapping should not toggle a "selected" object's selected state
                if (pickedObject != selectedObject) {
                    toggleSelection();  // deselects a previously selected item
                }
                if (pickedObject == selectedObject) {
                    edit(); // Open the placemark editor
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent event) {
                pick(event);
                contextMenu();
            }
        });

        /**
         * Delegates events to the select/drag handlers or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Allow our select and drag handlers to process the event first. They'll set the state flags which will
            // either preempt or allow the event to be subsequently processed by the globe's navigation event handlers.
            boolean consumed = this.selectDragDetector.onTouchEvent(event);

            // Is a dragging operation started or in progress? Any ACTION_UP event cancels a drag operation.
            if (this.isDragging && event.getAction() == MotionEvent.ACTION_UP) {
                this.isDragging = false;
                this.isDraggingArmed = false;
            }
            // Preempt the globe's pan navigation recognizer if we're dragging
            super.panRecognizer.setEnabled(!isDragging);

            // Pass on the event on to the default globe navigation handlers
            if (!consumed) {
                consumed = super.onTouchEvent(event);
            }
            return consumed;
        }

        /**
         * Performs a pick at the tap location and conditionally arms the dragging flag, so that dragging can occur if
         * the next event is an onScroll event.
         */
        public void pick(MotionEvent event) {
            // Reset our last picked object
            this.pickedObject = null;

            // Perform the pick at the screen x, y
            PickedObjectList pickList = getWorldWindow().pick(event.getX(), event.getY());

            // Examine the picked objects for Renderables
            PickedObject topPickedObject = pickList.topPickedObject();
            if (topPickedObject != null) {
                if (topPickedObject.getUserObject() instanceof Renderable) {
                    this.pickedObject = (Renderable) topPickedObject.getUserObject();
                }
            }

            // Determine whether the dragging flag should be "armed". The prerequisite for dragging that an object must
            // have been previously selected (via a single tap) and the selected object must manifest a "movable"
            // capability.
            this.isDraggingArmed = (this.pickedObject != null)
                && (this.selectedObject == this.pickedObject)
                && (this.selectedObject.hasUserProperty(MOVABLE));
        }

        /**
         * Toggles the selected state of the picked object.
         */
        public void toggleSelection() {
            // Test if last picked object is "selectable".  If not, retain the
            // currently selected object. To discard the current selection,
            // the user must pick another selectable object or the current object.
            if (pickedObject != null) {
                if (pickedObject.hasUserProperty(SELECTABLE)) {

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
                } else {
                    Toast.makeText(getApplicationContext(), "The picked object is not selectable.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * Moves the selected object to the event's screen position.
         *
         * @return true if the event was consumed
         */
        public boolean drag(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            if (this.isDraggingArmed && this.selectedObject != null) {
                if (this.selectedObject instanceof Placemark) {
                    // Signal that dragging is in progress
                    this.isDragging = true;

                    // First we compute the screen coordinates of the position's "ground" point.  We'll apply the
                    // screen X and Y drag distances to this point, from which we'll compute a new position,
                    // wherein we restore the original position's altitude.
                    Position position = ((Placemark) this.selectedObject).getPosition();
                    double altitude = position.altitude;
                    if (getWorldWindow().geographicToScreenPoint(position.latitude, position.longitude, 0 /*altitude*/, this.dragRefPt)) {
                        // Update the placemark's ground position
                        if (screenPointToGroundPosition(this.dragRefPt.x - distanceX, this.dragRefPt.y - distanceY, position)) {
                            // Restore the placemark's original altitude
                            position.altitude = altitude;
                            // Reflect the change in position on the globe.
                            getWorldWindow().requestRedraw();
                            return true;
                        }
                    }
                    // Probably clipped by near/far clipping plane or off the globe. The position was not updated. Stop the drag.
                    this.isDraggingArmed = false;
                    return true; // We consumed this event, even if dragging has been stopped.
                }
            }
            return false;
        }

        public void cancelDragging() {
            this.isDragging = false;
            this.isDraggingArmed = false;
        }

        /**
         * Edits the currently selected object.
         */
        public void edit() {

            if (this.selectedObject instanceof Placemark &&
                this.selectedObject.hasUserProperty(EDITABLE)) {
                Placemark placemark = (Placemark) this.selectedObject;

                // Pass the current aircraft type in a Bundle
                Bundle args = new Bundle();
                args.putString("title", "Select the " + placemark.getDisplayName() + "'s type");
                if (placemark.hasUserProperty(AIRCRAFT_TYPE)) {
                    args.putString("vehicleKey", AIRCRAFT_TYPE);
                    args.putString("vehicleValue", (String) placemark.getUserProperty(AIRCRAFT_TYPE));
                } else if (placemark.hasUserProperty(AUTOMOTIVE_TYPE)) {
                    args.putString("vehicleKey", AUTOMOTIVE_TYPE);
                    args.putString("vehicleValue", (String) placemark.getUserProperty(AUTOMOTIVE_TYPE));
                }

                // The VehicleTypeDialog calls onFinished
                VehicleTypeDialog dialog = new VehicleTypeDialog();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "aircraft_type");
            } else {
                Toast.makeText(getApplicationContext(),
                    (this.selectedObject == null ? "Object " : this.selectedObject.getDisplayName()) + " is not editable.",
                    Toast.LENGTH_LONG).show();
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
     * This inner class creates a simple Placemark editor for selecting the vehicle type.
     */
    public static class VehicleTypeDialog extends DialogFragment {

        private int selectedItem = -1;

        private String vehicleKey;

        private String[] vehicleTypes;

        private HashMap<String, Integer> vehicleIcons;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String title = args.getString("title", "");

            // Determine type of vehicles displayed in this dialog
            this.vehicleKey = args.getString("vehicleKey", "");
            if (this.vehicleKey.equals(AIRCRAFT_TYPE)) {
                this.vehicleTypes = aircraftTypes;
                this.vehicleIcons = aircraftIconMap;
            } else if (this.vehicleKey.equals(AUTOMOTIVE_TYPE)) {
                this.vehicleTypes = automotiveTypes;
                this.vehicleIcons = automotiveIconMap;
            }
            // Determine the initial selection
            String type = args.getString("vehicleValue", "");
            for (int i = 0; i < this.vehicleTypes.length; i++) {
                if (type.equals(this.vehicleTypes[i])) {
                    this.selectedItem = i;
                    break;
                }
            }

            // Create "single selection" list of aircraft vehicleTypes
            return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setSingleChoiceItems(this.vehicleTypes, this.selectedItem,
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
                        onFinished(vehicleTypes[selectedItem]);
                    }
                })
                    // A null handler will close the dialog
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        }

        public void onFinished(String vehicleType) {
            PlacemarksSelectDragActivity activity = (PlacemarksSelectDragActivity) getActivity();
            if (activity.controller.selectedObject instanceof Placemark) {

                Placemark placemark = (Placemark) activity.controller.selectedObject;
                String currentType = (String) placemark.getUserProperty(this.vehicleKey);
                if (currentType.equals(vehicleType)) {
                    return;
                }
                // Update the placemark's icon attributes and vehicle type property.
                ImageSource imageSource = ImageSource.fromResource(this.vehicleIcons.get(vehicleType));
                placemark.putUserProperty(this.vehicleKey, vehicleType);
                placemark.getAttributes().setImageSource(imageSource);
                placemark.getHighlightAttributes().setImageSource(imageSource);
                // Show the change
                activity.getWorldWindow().requestRedraw();
            }
        }

    }

}