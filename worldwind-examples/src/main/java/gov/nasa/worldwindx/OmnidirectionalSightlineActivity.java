/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.OmnidirectionalSightline;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.ShapeAttributes;

/**
 * This Activity demonstrates the OmnidirectionalSightline object which provides a visual representation of line of
 * sight from a specified origin. Terrain visible from the origin is colored differently than areas not visible from
 * the OmnidirectionalSightline origin. Line of sight is calculated as a straight line from the origin to the available
 * terrain.
 */
public class OmnidirectionalSightlineActivity extends BasicGlobeActivity {

    /**
     * The OmnidirectionalSightline object which will display areas visible using a line of sight from the origin
     */
    protected OmnidirectionalSightline sightline;

    /**
     * A Placemark representing the origin of the sightline
     */
    protected Placemark sightlinePlacemark;

    /**
     * A custom WorldWindowController object that handles the select, drag and navigation gestures.
     */
    protected SimpleSelectDragNavigateController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_movable_omni_sightline));
        setAboutBoxText("Demonstrates a draggable WorldWind Omnidirectional sightline. Drag the placemark icon around the " +
            "screen to move the sightline position.");

        // Initialize attributes for the OmnidirectionalSightline
        ShapeAttributes viewableRegions = new ShapeAttributes();
        viewableRegions.setInteriorColor(new Color(0f, 1f, 0f, 0.25f));

        ShapeAttributes blockedRegions = new ShapeAttributes();
        blockedRegions.setInteriorColor(new Color(0.1f, 0.1f, 0.1f, 0.5f));

        // Initialize the OmnidirectionalSightline and Corresponding Placemark
        // The position is the line of sight origin for determining visible terrain
        Position pos = new Position(46.202, -122.190, 500.0);
        this.sightline = new OmnidirectionalSightline(pos, 10000.0);
        this.sightline.setAttributes(viewableRegions);
        this.sightline.setOccludeAttributes(blockedRegions);
        this.sightline.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        this.sightlinePlacemark = new Placemark(pos);
        this.sightlinePlacemark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        this.sightlinePlacemark.getAttributes().setImageSource(ImageSource.fromResource(R.drawable.aircraft_fixwing));
        this.sightlinePlacemark.getAttributes().setImageScale(2);
        this.sightlinePlacemark.getAttributes().setDrawLeader(true);

        // Establish a layer to hold the sightline and placemark
        RenderableLayer sightlineLayer = new RenderableLayer();
        sightlineLayer.addRenderable(this.sightline);
        sightlineLayer.addRenderable(this.sightlinePlacemark);
        this.wwd.getLayers().addLayer(sightlineLayer);

        // Override the WorldWindow's built-in navigation behavior with conditional dragging support.
        this.controller = new SimpleSelectDragNavigateController();
        this.wwd.setWorldWindowController(this.controller);

        // And finally, for this demo, position the viewer to look at the sightline position
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE, 2e4 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    /**
     * This inner class is based on the controller in the {@link PlacemarksSelectDragActivity} but has been simplified
     * for the single Placemark.
     */
    public class SimpleSelectDragNavigateController extends BasicWorldWindowController {

        protected boolean isDragging = false;

        protected boolean isDraggingArmed = false;

        private PointF dragRefPt = new PointF();

        /**
         * Pre-allocated to avoid memory allocations
         */
        private Line ray = new Line();

        /**
         * Pre-allocated to avoid memory allocations
         */
        private Vec3 pickPoint = new Vec3();

        /**
         * Assign a subclassed SimpleOnGestureListener to a GestureDetector to handle the drag gestures.
         */
        protected GestureDetector selectDragDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent event) {
                pick(event);    // Pick the object(s) at the tap location
                return false;   // By not consuming this event, we allow it to pass on to the navigation gesture handlers
            }

            @Override
            public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
                if (isDraggingArmed) {
                    return drag(downEvent, moveEvent, distanceX, distanceY);
                }
                return false;
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
         * Moves the selected object to the event's screen position.
         *
         * @return true if the event was consumed
         */
        public boolean drag(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            if (this.isDraggingArmed) {
                // Signal that dragging is in progress
                this.isDragging = true;

                // First we compute the screen coordinates of the position's "ground" point.  We'll apply the
                // screen X and Y drag distances to this point, from which we'll compute a new position,
                // wherein we restore the original position's altitude.
                Position position = sightlinePlacemark.getReferencePosition();
                double altitude = position.altitude;
                if (getWorldWindow().geographicToScreenPoint(position.latitude, position.longitude, 0, this.dragRefPt)) {
                    // Update the placemark's ground position
                    if (screenPointToGroundPosition(this.dragRefPt.x - distanceX, this.dragRefPt.y - distanceY, position)) {
                        // Restore the placemark's original altitude
                        position.altitude = altitude;
                        // Move the sightline
                        sightline.setPosition(position);
                        // Reflect the change in position on the globe.
                        getWorldWindow().requestRedraw();
                        return true;
                    }
                }
                // Probably clipped by near/far clipping plane or off the globe. The position was not updated. Stop the drag.
                this.isDraggingArmed = false;
                return true; // We consumed this event, even if dragging has been stopped.
            }
            return false;
        }

        /**
         * Performs a pick at the tap location and conditionally arms the dragging flag, so that dragging can occur if
         * the next event is an onScroll event.
         */
        public void pick(MotionEvent event) {

            // Perform the pick at the screen x, y
            PickedObjectList pickList = getWorldWindow().pick(event.getX(), event.getY());

            // Examine the picked objects for Renderables
            PickedObject topPickedObject = pickList.topPickedObject();
            // There is only one placemark on the globe and
            this.isDraggingArmed = topPickedObject != null && topPickedObject.getUserObject() instanceof Placemark;
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
