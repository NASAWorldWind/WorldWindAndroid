/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Movable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.SurfaceImage;

public class PlacemarksDraggerActivity extends BasicGlobeActivity {

    public interface Dragger {

        boolean drag(WorldWindow wwd, Movable movable, MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY);
    }

    /**
     * A simple dragger for a WorldWindow that is capable of dragging implementations of the Movable interface.
     */
    public static class SimpleDragger implements Dragger {

        /**
         * Drags a Movable object to a new position determined by the events' x and y screen offsets.
         *
         * @param wwd       The WorldWindow screen object
         * @param movable   The object to be moved
         * @param downEvent Initial ACTION_DOWN event; not used
         * @param moveEvent Current ACTION_MOVE event; not used
         * @param distanceX delta x screen offset to move
         * @param distanceY delta y screen offset to move
         *
         * @return true if the object was moved, otherwise false.
         */
        public boolean drag(WorldWindow wwd, Movable movable, MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
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
            // Shift the ground position's lat and lon to correspond to the screen offsets
            if (!screenPointToGroundPosition(wwd, screenPt.x - distanceX, screenPt.y - distanceY, position)) {
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

        /**
         * Converts a screen point to the geographic coordinates on the globe. TODO: Move this method to the
         * WorldWindow.
         *
         * @param screenX X coordinate in Android screen coordinates
         * @param screenY Y coordinate in Android screen coordinates
         * @param result  Pre-allocated Position receives the geographic coordinates
         *
         * @return true if the screen point could be converted; false if the screen point is not on the globe
         */
        public static boolean screenPointToGroundPosition(WorldWindow wwd, float screenX, float screenY, Position result) {
            Line ray = new Line();
            Vec3 intersection = new Vec3();

            if (wwd.rayThroughScreenPoint(screenX, screenY, ray)) {
                Globe globe = wwd.getGlobe();
                if (globe.intersect(ray, intersection)) {
                    globe.cartesianToGeographic(intersection.x, intersection.y, intersection.z, result);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A simple picker for a WorldWindow that records the last picked top object.
     */
    public static class SimplePicker {

        /**
         * The last picked object. May be null.
         */
        public Object pickedObject;

        /**
         * Performs a pick at the tap location.
         *
         * @param wwd   The WorldWindow that will perform the pick operation
         * @param event The event containing the picked screen location
         */
        public void pick(WorldWindow wwd, MotionEvent event) {

            // Forget our last picked object
            this.pickedObject = null;

            // Perform a new pick at the screen x, y
            PickedObjectList pickList = wwd.pick(event.getX(), event.getY());

            // Get the top-most object for our new picked object
            PickedObject topPickedObject = pickList.topPickedObject();
            if (topPickedObject != null) {
                this.pickedObject = topPickedObject.getUserObject();
            }
        }
    }

    /**
     * A simple gesture listener for a WorldWindow that dispatches select motion events to a picker and optional
     * dragger.
     */
    public static class PickDragListener extends GestureDetector.SimpleOnGestureListener {

        protected WorldWindow wwd;

        protected SimplePicker picker = new SimplePicker();

        /**
         * Constructor.
         *
         * @param wwd The WorldWindow associated with this Gesture Listener.
         */
        public PickDragListener(WorldWindow wwd) {
            this.wwd = wwd;
        }

        /**
         * onDown implements the picking behavior.
         *
         * @param event
         *
         * @return
         */
        @Override
        public boolean onDown(MotionEvent event) {
            // Pick the object(s) at the tap location
            this.picker.pick(this.wwd, event);
            return false;   // By not consuming this event, we allow it to pass on to WorldWindow navigation gesture handlers
        }

        /**
         * onScroll implements the dragging behavior. This implementation only moves Movable Renderables that have a
         * "Dragger" in their user properties.
         *
         * @param downEvent
         * @param moveEvent
         * @param distanceX
         * @param distanceY
         *
         * @return
         */
        @Override
        public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            if (!(this.picker.pickedObject instanceof Renderable)) {
                return false;
            }
            if (!(this.picker.pickedObject instanceof Movable)) {
                return false;
            }
            Renderable shape = (Renderable) this.picker.pickedObject;
            Movable movable = (Movable) this.picker.pickedObject;

            // Interrogate the shape for a Dragger property
            Dragger dragger = (Dragger) shape.getUserProperty(Dragger.class);
            if (dragger != null) {
                // Invoke the shapes's dragger
                return dragger.drag(this.wwd, movable, downEvent, moveEvent, distanceX, distanceY);
            }

            return false;
        }
    }

    /**
     * This inner class is a custom WorldWindController that handles both picking and navigation via a combination of
     * the native World Wind navigation gestures and Android gestures. This class' onTouchEvent method arbitrates
     * between pick events and globe navigation events.
     */
    public static class CustomWorldWindowController extends BasicWorldWindowController {

        private GestureDetector preNavGestureDetector;

        private GestureDetector postNavGestureDetector;

        public CustomWorldWindowController(Context context, WorldWindow wwd) {
            this.preNavGestureDetector = new GestureDetector(context, new PickDragListener(wwd));
        }

        /**
         * Delegates events to the pick handler or the native World Wind navigation handlers.
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean consumed = false;
            if (this.preNavGestureDetector != null) {
                consumed = this.preNavGestureDetector.onTouchEvent(event);
            }
            if (!consumed) {
                consumed = super.onTouchEvent(event);
            }
            if (!consumed && this.postNavGestureDetector != null) {
                consumed = this.postNavGestureDetector.onTouchEvent(event);
            }
            return consumed;
        }

    }

    public static class MovableSurfaceImage extends SurfaceImage implements Movable {

        public MovableSurfaceImage(Sector sector, ImageSource imageSource) {
            super(sector, imageSource);
        }

        /**
         * A position associated with the object that indicates its aggregate geographic position. The chosen position
         * varies among implementers of this interface. For objects defined by a list of positions, the reference
         * position is typically the first position in the list. For symmetric objects the reference position is often
         * the center of the object. In many cases the object's reference position may be explicitly specified by the
         * application.
         *
         * @return the object's reference position, or null if no reference position is available.
         */
        @Override
        public Position getReferencePosition() {
            Sector sector = super.getSector();
            Position refPosition = new Position(sector.minLatitude(), sector.minLongitude(), 0);
            return refPosition;
        }

        /**
         * Move the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
         * North.
         *
         * @param globe    the globe on which to move the shape.
         * @param position the new position of the shape's reference position.
         */
        @Override
        public void moveTo(Globe globe, Position position) {
            Position oldRef = this.getReferencePosition();
            if (oldRef == null)
                return;

            Sector sector = super.getSector();
            Location swCorner = new Location(sector.minLatitude(), sector.minLongitude());
            Location nwCorner = new Location(sector.maxLatitude(), sector.minLongitude());
            Location seCorner = new Location(sector.minLatitude(), sector.maxLongitude());

            final double EAST = 90;
            final double WEST = 270;
            final double NORTH = 0;
            double distanceRadians = oldRef.greatCircleDistance(swCorner);
            double azimuthDegrees = oldRef.greatCircleAzimuth(swCorner);
            double widthRadians = swCorner.rhumbDistance(seCorner);
            double heightRadians = swCorner.rhumbDistance(nwCorner);

            // Compute a new positions for the SW corner
            position.greatCircleLocation(azimuthDegrees, distanceRadians, swCorner);

            // Compute the SE corner, using the original width
            swCorner.rhumbLocation(EAST, widthRadians, seCorner);
            if (Location.locationsCrossAntimeridian(Arrays.asList(new Location[]{swCorner, seCorner}))) {
                // TODO: create issue regarding Sector Antimeridian limitation
                // There's presently no support for placing SurfaceImages crossing the Anti-meridian
                // Snap the image to the other side of the date line
                double dragAzimuth = oldRef.greatCircleAzimuth(position);
                if (dragAzimuth < 0) {
                    // Set the East edge of the sector to the dateline
                    seCorner.set(seCorner.latitude, 180);
                    seCorner.rhumbLocation(WEST, widthRadians, swCorner);
                } else {
                    // Set the West edge of the sector to the dateline
                    swCorner.set(swCorner.latitude, -180);
                    swCorner.rhumbLocation(EAST, widthRadians, seCorner);
                }
            }
            // Compute the NW corner with the original height
            swCorner.rhumbLocation(NORTH, heightRadians, nwCorner);

            // Compute the delta lat and delta lon values from the new SW position
            double dLat = nwCorner.latitude - swCorner.latitude;
            double dLon = seCorner.longitude - swCorner.longitude;

            // Update the image's sector
            super.sector.set(swCorner.latitude, swCorner.longitude, dLat, dLon);
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

        // Override the World Window's built-in navigation behavior by adding picking support.
        wwd.setWorldWindowController(new CustomWorldWindowController(getApplicationContext(), wwd));

        // Add a layer for placemarks to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Placemarks");
        wwd.getLayers().addLayer(layer);

        // Configure a Surface Image to display an Android resource showing the NASA logo.
        SurfaceImage smallImage = new MovableSurfaceImage(new Sector(34.2, -119.2, 0.1, 0.12), ImageSource.fromResource(R.drawable.nasa_logo));
        SurfaceImage bigImage = new MovableSurfaceImage(new Sector(36, -120.0, 5.0, 6.0), ImageSource.fromResource(R.drawable.nasa_logo));
        smallImage.putUserProperty(Dragger.class, new SimpleDragger());
        bigImage.putUserProperty(Dragger.class, new SimpleDragger());
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
        placemark.putUserProperty(Dragger.class, new SimpleDragger());
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
