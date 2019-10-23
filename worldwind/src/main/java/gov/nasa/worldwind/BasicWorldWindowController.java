/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.view.MotionEvent;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.gesture.GestureListener;
import gov.nasa.worldwind.gesture.GestureRecognizer;
import gov.nasa.worldwind.gesture.MousePanRecognizer;
import gov.nasa.worldwind.gesture.PanRecognizer;
import gov.nasa.worldwind.gesture.PinchRecognizer;
import gov.nasa.worldwind.gesture.RotationRecognizer;
import gov.nasa.worldwind.util.WWMath;

public class BasicWorldWindowController implements WorldWindowController, GestureListener {

    protected WorldWindow wwd;

    protected float lastX;

    protected float lastY;

    protected float lastRotation;

    protected LookAt lookAt = new LookAt();

    protected LookAt beginLookAt = new LookAt();

    protected int activeGestures;

    protected GestureRecognizer panRecognizer = new PanRecognizer();

    protected GestureRecognizer pinchRecognizer = new PinchRecognizer();

    protected GestureRecognizer rotationRecognizer = new RotationRecognizer();

    protected GestureRecognizer tiltRecognizer = new PanRecognizer();

    protected GestureRecognizer mouseTiltRecognizer = new MousePanRecognizer();

    protected List<GestureRecognizer> allRecognizers = Arrays.asList(
        this.panRecognizer, this.pinchRecognizer, this.rotationRecognizer, this.tiltRecognizer, this.mouseTiltRecognizer);

    public BasicWorldWindowController(WorldWindow wwd) {
        this.wwd = wwd;

        this.panRecognizer.addListener(this);
        this.pinchRecognizer.addListener(this);
        this.rotationRecognizer.addListener(this);
        this.tiltRecognizer.addListener(this);
        this.mouseTiltRecognizer.addListener(this);

        ((PanRecognizer) this.panRecognizer).setMaxNumberOfPointers(1); // Do not pan during tilt
        ((PanRecognizer) this.tiltRecognizer).setMinNumberOfPointers(2); // Use two fingers for tilt gesture
        ((MousePanRecognizer) this.mouseTiltRecognizer).setButtonState(MotionEvent.BUTTON_SECONDARY);

        // Set interpret distance based on screen density
        ((PanRecognizer) this.panRecognizer).setInterpretDistance(wwd.getContext().getResources().getDimension(R.dimen.pan_interpret_distance));
        ((PinchRecognizer) this.pinchRecognizer).setInterpretDistance(wwd.getContext().getResources().getDimension(R.dimen.pinch_interpret_distance));
        ((RotationRecognizer) this.rotationRecognizer).setInterpretAngle(20f);
        ((PanRecognizer) this.tiltRecognizer).setInterpretDistance(wwd.getContext().getResources().getDimension(R.dimen.tilt_interpret_distance));
        ((MousePanRecognizer) this.mouseTiltRecognizer).setInterpretDistance(wwd.getContext().getResources().getDimension(R.dimen.tilt_interpret_distance));
    }

    @Override
    public WorldWindow getWorldWindow() {
        return wwd;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;

        for (int idx = 0, len = this.allRecognizers.size(); idx < len; idx++) {
            handled |= this.allRecognizers.get(idx).onTouchEvent(event); // use or-assignment to indicate if any recognizer handled the event
        }

        // Handle dependent gestures lock
        if(handled) {
            tiltRecognizer.setEnabled(!isInProcess(rotationRecognizer) || !rotationRecognizer.isEnabled());
            rotationRecognizer.setEnabled(!isInProcess(tiltRecognizer) || !tiltRecognizer.isEnabled());
        }

        return handled;
    }

    @Override
    public void gestureStateChanged(MotionEvent event, GestureRecognizer recognizer) {
        if (recognizer == this.panRecognizer) {
            this.handlePan(recognizer);
        } else if (recognizer == this.pinchRecognizer) {
            this.handlePinch(recognizer);
        } else if (recognizer == this.rotationRecognizer) {
            this.handleRotate(recognizer);
        } else if (recognizer == this.tiltRecognizer || recognizer == this.mouseTiltRecognizer) {
            this.handleTilt(recognizer);
        }
    }

    protected void handlePan(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float dx = recognizer.getTranslationX();
        float dy = recognizer.getTranslationY();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
            this.lastX = 0;
            this.lastY = 0;
        } else if (state == WorldWind.CHANGED) {
            // Get observation point position.
            double lat = this.lookAt.position.latitude;
            double lon = this.lookAt.position.longitude;
            double rng = this.lookAt.range;

            // Convert the translation from screen coordinates to degrees. Use observation point range as a metric for
            // converting screen pixels to meters, and use the globe's radius for converting from meters to arc degrees.
            double metersPerPixel = this.wwd.pixelSizeAtDistance(rng);
            double forwardMeters = (dy - this.lastY) * metersPerPixel;
            double sideMeters = -(dx - this.lastX) * metersPerPixel;
            this.lastX = dx;
            this.lastY = dy;

            double globeRadius = this.wwd.getGlobe().getRadiusAt(lat, lon);
            double forwardDegrees = Math.toDegrees(forwardMeters / globeRadius);
            double sideDegrees = Math.toDegrees(sideMeters / globeRadius);

            // Adjust the change in latitude and longitude based on observation point heading.
            double heading = this.lookAt.heading;
            double headingRadians = Math.toRadians(heading);
            double sinHeading = Math.sin(headingRadians);
            double cosHeading = Math.cos(headingRadians);
            lat += forwardDegrees * cosHeading - sideDegrees * sinHeading;
            lon += forwardDegrees * sinHeading + sideDegrees * cosHeading;

            // If the camera has panned over either pole, compensate by adjusting the longitude and heading to move
            // the camera to the appropriate spot on the other side of the pole.
            if (lat < -90 || lat > 90) {
                this.lookAt.position.latitude = Location.normalizeLatitude(lat);
                this.lookAt.position.longitude = Location.normalizeLongitude(lon + 180);
                this.lookAt.heading = WWMath.normalizeAngle360(heading + 180);
            } else if (lon < -180 || lon > 180) {
                this.lookAt.position.latitude = lat;
                this.lookAt.position.longitude = Location.normalizeLongitude(lon);
            } else {
                this.lookAt.position.latitude = lat;
                this.lookAt.position.longitude = lon;
            }

            this.wwd.getCamera().setFromLookAt(this.lookAt);
            this.wwd.requestRedraw();
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    protected void handlePinch(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float scale = ((PinchRecognizer) recognizer).getScale();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
        } else if (state == WorldWind.CHANGED) {
            if (scale != 0) {
                // Apply the change in range to observation point, relative to when the gesture began.
                this.lookAt.range = this.beginLookAt.range / scale;
                this.applyLimits(this.lookAt);

                this.wwd.getCamera().setFromLookAt(this.lookAt);
                this.wwd.requestRedraw();
            }
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    protected void handleRotate(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float rotation = ((RotationRecognizer) recognizer).getRotation();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
            this.lastRotation = 0;
        } else if (state == WorldWind.CHANGED) {
            // Apply the change in rotation to the camera, relative to the camera's current values.
            double headingDegrees = this.lastRotation - rotation;
            this.lookAt.heading = WWMath.normalizeAngle360(this.lookAt.heading + headingDegrees);
            this.lastRotation = rotation;

            this.wwd.getCamera().setFromLookAt(this.lookAt);
            this.wwd.requestRedraw();
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    protected void handleTilt(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float dx = recognizer.getTranslationX();
        float dy = recognizer.getTranslationY();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
            this.lastRotation = 0;
        } else if (state == WorldWind.CHANGED) {
            // Apply the change in tilt to the camera, relative to when the gesture began.
            double headingDegrees = 180 * dx / this.wwd.getWidth();
            double tiltDegrees = -180 * dy / this.wwd.getHeight();
            this.lookAt.heading = WWMath.normalizeAngle360(this.beginLookAt.heading + headingDegrees);
            this.lookAt.tilt = this.beginLookAt.tilt + tiltDegrees;
            this.applyLimits(this.lookAt);

            this.wwd.getCamera().setFromLookAt(this.lookAt);
            this.wwd.requestRedraw();
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    protected void applyLimits(LookAt lookAt) {
        double distanceToExtents = this.wwd.distanceToViewGlobeExtents();

        double minRange = 10;
        double maxRange = distanceToExtents * 2;
        lookAt.range = WWMath.clamp(lookAt.range, minRange, maxRange);

        //double minTiltRange = distanceToExtents * 0.1;
        //double maxTiltRange = distanceToExtents * 0.9;
        //double tiltAmount = WWMath.clamp((lookAt.range - minTiltRange) / (maxTiltRange - minTiltRange), 0, 1);
        double maxTilt = 80;
        lookAt.tilt = WWMath.clamp(lookAt.tilt, 0, maxTilt);
    }

    protected void gestureDidBegin() {
        if (this.activeGestures++ == 0) {
            this.wwd.getCamera().getAsLookAt(this.beginLookAt);
            this.lookAt.set(this.beginLookAt);
        }
    }

    protected void gestureDidEnd() {
        if (this.activeGestures > 0) { // this should always be the case, but we check anyway
            this.activeGestures--;
        }
    }

    private boolean isInProcess(GestureRecognizer recognizer) {
        return recognizer.getState() == WorldWind.BEGAN || recognizer.getState() == WorldWind.CHANGED;
    }

}
