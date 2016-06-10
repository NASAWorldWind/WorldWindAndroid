/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Movable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.SurfaceImage;

public class PlacemarksDraggerActivity extends BasicGlobeActivity {


    /**
     * A naive gesture listener for a WorldWindow that dispatches motion events to a picker and dragger.
     */
    public static class NaivePickDragListener extends GestureDetector.SimpleOnGestureListener {

        protected WorldWindow wwd;

        /**
         * The last picked object. May be null.
         */
        protected Object pickedObject;

        /**
         * Constructor.
         *
         * @param wwd The WorldWindow to be associated with this Gesture Listener.
         */
        public NaivePickDragListener(WorldWindow wwd) {
            this.wwd = wwd;
        }

        /**
         * Implements the picking behavior; picks the object(s) at the tap location.
         *
         * @param event ACTION_DOWN event
         *
         * @return false; by not consuming this event, we allow it to pass on to WorldWindow navigation gesture handlers
         */
        @Override
        public boolean onDown(MotionEvent event) {
            this.pick(event);
            return false;
        }

        /**
         * Implements the naive dragging behavior: all "Movable" objects can be dragged.
         *
         * @param downEvent not used.
         * @param moveEvent not used.
         * @param distanceX X offset
         * @param distanceY Y offset
         *
         * @return The result of a Dragger's drag operation; otherwise false
         */
        @Override
        public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            if (!(this.pickedObject instanceof Movable)) {
                return false;
            }
            Movable movable = (Movable) this.pickedObject;
            return this.drag(movable, distanceX, distanceY);
        }

        /**
         * Performs a pick operation at the event's screen x,y position.
         *
         * @param event Typically an ACTION_DOWN event
         */
        protected void pick(MotionEvent event) {
            // Forget our last picked object
            this.pickedObject = null;

            // Perform a new pick at the screen x, y
            PickedObjectList pickList = this.wwd.pick(event.getX(), event.getY());

            // Get the top-most object for our new picked object
            PickedObject topPickedObject = pickList.topPickedObject();
            if (topPickedObject != null) {
                this.pickedObject = topPickedObject.getUserObject();
            }
        }

        protected boolean drag(Movable movable, float distanceX, float distanceY) {
            // First we compute the screen coordinates of the position's ground point.  We'll apply the
            // screen X and Y drag distances to this point, from which we'll compute a new ground position,
            // wherein we restore the original position's altitude.

            // Get a 'copy' of the movable's reference position
            Position position = new Position(movable.getReferencePosition());
            PointF screenPt = new PointF();
            double altitude = position.altitude;

            // Get the screen x,y of the ground position (e.g., the base of the leader line for above ground Placemarks)
            if (!wwd.geographicToScreenPoint(position.latitude, position.longitude, 0 /*altitude*/, screenPt)) {
                // Probably clipped by near/far clipping plane.
                return false;
            }
            // Shift the ground position's lat and lon to correspond to the screen x and y offsets ...
            if (!wwd.screenPointToGeographic(screenPt.x - distanceX, screenPt.y - distanceY, position)) {
                return false;   // Probably off the globe
            }
            // ... and restore the altitude
            position.altitude = altitude;

            // Finally, perform the actual move on the object
            movable.moveTo(wwd.getGlobe(), position);

            // Request a redraw to visualize the change in position
            wwd.requestRedraw();

            // Consume the event
            return true;
        }
    }

    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    public static class CustomWorldWindowController extends BasicWorldWindowController {

        protected ArrayList<GestureDetector> gestureDetectors = new ArrayList<>();
        protected boolean isDragging;

        public void addGestureDetector(GestureDetector gestureDetector) {
            this.gestureDetectors.add(gestureDetector);
        }
        public void removeGestureDetector(GestureDetector gestureDetector) {
            this.gestureDetectors.remove(gestureDetector);
        }

        /**
         * Delegates events to the pre and post navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean consumed = false;

            // Allow an application defined GestureDetector to process events before
            // the globe's navigation handlers to.  Typically, picking and dragging
            // occurs here.
            for (GestureDetector gestureDetector : this.gestureDetectors) {
                consumed = gestureDetector.onTouchEvent(event);

                // Android doesn't send all ACTION_MOVE events to onScroll! If a drag
                // operation becomes too slow for Android it doesn't call onScroll.
                // Thus the message is not consumed and it is passed on to the navigation
                // gesture handler to be interpreted as a pan gesture. To prevent this
                // we have to implement an isDragging state property.
                if (consumed && event.getAction() == MotionEvent.ACTION_MOVE) {
                    // If we've consumed an ACTION_MOVE (onScroll) message
                    // then assume that we must be dragging.
                    isDragging = true;
                }
            }

            // Any ACTION_UP message means we're not dragging
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // If we're not dragging, then ACTION_MOVE messages are navigation gestures.
                isDragging = false;
            }

            // If we're dragging, we preempt the globe's pan navigation behavior.
            // When disabled, this setting prevents ACTION_MOVE messages that weren't
            // sent to onScroll while dragging from being interpreted as pan gestures.
            //super.panRecognizer.setEnabled(!isDragging);

            // Unconsumed messages are sent to the globe's navigation gesture handlers
            if (!consumed && !isDragging) {
                consumed = super.onTouchEvent(event);
            }

            return consumed;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_dragger));
        setAboutBoxText("Demonstrates how to drag Placemarks.\n\n" +
            "Tapping a placemark will toggle its highlighted state.");

        // Get a reference to the WorldWindow view
        WorldWindow wwd = this.getWorldWindow();

        // Add picking and dragging support to the World Window...

        // Create a WorldWindController that will dispatch MotionEvents to our GestureDetector(s)
        CustomWorldWindowController worldWindowController = new CustomWorldWindowController();

        // Add a gesture detector to the WorldWindowController that handles for pick and drag events
        worldWindowController.addGestureDetector(new GestureDetector(getApplicationContext(), new NaivePickDragListener(wwd)));

        // Replace the default WorldWindowController with our own
        wwd.setWorldWindowController(worldWindowController);
        // TODO: make this controller the BasicWorldWindowController

        // Add a layer for placemarks to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Renderables");
        wwd.getLayers().addLayer(layer);

        // Create SurfaceImages to display an Android resource showing the NASA logo.
        SurfaceImage smallImage = new SurfaceImage(new Sector(34.2, -119.2, 0.1, 0.12), ImageSource.fromResource(R.drawable.nasa_logo));
        SurfaceImage bigImage = new SurfaceImage(new Sector(36, -120.0, 5.0, 10.0), ImageSource.fromResource(R.drawable.nasa_logo));

        layer.addRenderable(smallImage);
        layer.addRenderable(bigImage);

        // Create a few placemarks with highlight attributes and add them to the layer
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.2000, -119.2070, 0), "Oxnard Airport"));
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.2138, -119.0944, 0), "Camarillo Airport"));
        layer.addRenderable(createAirportPlacemark(Position.fromDegrees(34.1193, -119.1196, 0), "Pt Mugu Naval Air Station"));
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.200, -119.207, 1000)));
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.210, -119.150, 2000)));
        layer.addRenderable(createAircraftPlacemark(Position.fromDegrees(34.150, -119.150, 3000)));

        // Position the viewer to look near the airports
        LookAt lookAt = new LookAt().set(34.15, -119.15, 0, WorldWind.ABSOLUTE, 2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    /**
     * Helper method to create aircraft placemarks.
     */
    private static Placemark createAircraftPlacemark(Position position) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.aircraft_fighter));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0).setDrawLeader(true); // set normal attributes to 2x original size
        return placemark;
    }

    /**
     * Helper method to create airport placemarks.
     */
    private static Placemark createAirportPlacemark(Position position, String airportName) {
        Placemark placemark = Placemark.createWithImage(position, ImageSource.fromResource(R.drawable.airport_terminal));
        placemark.getAttributes().setImageOffset(Offset.bottomCenter()).setImageScale(2.0); // set normal attributes to 2x original size
        placemark.setDisplayName(airportName);
        return placemark;
    }
}
