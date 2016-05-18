/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.WorldWindowController;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class PlacemarkDragActivity extends BasicGlobeActivity {

    public static class CustomWorldWindowController extends BasicWorldWindowController implements GestureDetector.OnGestureListener {

        protected GestureDetector gestures;

        protected boolean navigationSuppressed = false;

        // TODO: Implement select/deselect (highlighted)
        // TODO: Implement open (double tap)
        // TODO: Implement context menu (
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            gestures.onTouchEvent(event);
            if (!navigationSuppressed) {
                // call the native WorldWindow navigation gesture recognizers
                return super.onTouchEvent(event);
            }
            return true;
        }

        public CustomWorldWindowController(Context context) {
            this.gestures = new GestureDetector(context, this);
        }

        /**
         * Notified when a tap occurs with the down {@link MotionEvent} that triggered it. This will be triggered
         * immediately for every down event. All other events should be preceded by this.
         *
         * @param e The down motion event.
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * The user has performed a down {@link MotionEvent} and not performed a move or up yet. This event is commonly
         * used to provide visual feedback to the user to let them know that their action has been recognized i.e.
         * highlight an element.
         *
         * @param e The down motion event
         */
        @Override
        public void onShowPress(MotionEvent e) {

        }

        /**
         * Notified when a tap occurs with the up {@link MotionEvent} that triggered it.
         *
         * @param e The up motion event that completed the first tap
         *
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        /**
         * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the current move {@link
         * MotionEvent}. The distance in x and y is also supplied for convenience.
         *
         * @param e1        The first down motion event that started the scrolling.
         * @param e2        The move motion event that triggered the current onScroll.
         * @param distanceX The distance along the X axis that has been scrolled since the last call to onScroll. This
         *                  is NOT the distance between {@code e1} and {@code e2}.
         * @param distanceY The distance along the Y axis that has been scrolled since the last call to onScroll. This
         *                  is NOT the distance between {@code e1} and {@code e2}.
         *
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        /**
         * Notified when a long press occurs with the initial on down {@link MotionEvent} that triggered it.
         *
         * @param e The initial on down motion event that started the longpress.
         */
        @Override
        public void onLongPress(MotionEvent e) {
            // Test case: on long press toggle the native WorldWind navigation gestures.
            this.navigationSuppressed = !this.navigationSuppressed;
        }

        /**
         * Notified of a fling event when it occurs with the initial on down {@link MotionEvent} and the matching up
         * {@link MotionEvent}. The calculated velocity is supplied along the x and y axis in pixels per second.
         *
         * @param e1        The first down motion event that started the fling.
         * @param e2        The move motion event that triggered the current onFling.
         * @param velocityX The velocity of this fling measured in pixels per second along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per second along the y axis.
         *
         * @return true if the event is consumed, else false
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks));
        setAboutBoxText("Demonstrates how to drag Placemarks.");

        // Override the World Window's built-in navigation behavior.
        WorldWindow wwd = this.getWorldWindow();
        wwd.setWorldWindowController(new CustomWorldWindowController(this.getApplicationContext()));

        // Create a renderable
        Bitmap bitmap = BitmapFactory.decodeResource(getWorldWindow().getResources(), R.drawable.ehipcc);
        Placemark placemark = new Placemark(Position.fromDegrees(34.300, -119.25, 0),
            PlacemarkAttributes.createWithImage(ImageSource.fromBitmap(bitmap)).setImageOffset(Offset.bottomCenter()));

        // Add the renderable to a layer
        RenderableLayer layer = new RenderableLayer("Placemarks");
        layer.addRenderable(placemark);
        wwd.getLayers().addLayer(layer);

        // Get screen coordinates of renderable


        //

        // Disable


        // And finally, for this demo, position the viewer to look at the airport placemark
        // from a tilted perspective when this Android activity is created.
        Position pos = placemark.getPosition();
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 80 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }
}
