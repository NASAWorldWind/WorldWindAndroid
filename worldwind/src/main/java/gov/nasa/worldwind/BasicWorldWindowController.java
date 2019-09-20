/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;

import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.gesture.FlingRecognizer;
import gov.nasa.worldwind.gesture.GestureListener;
import gov.nasa.worldwind.gesture.GestureRecognizer;
import gov.nasa.worldwind.gesture.OnGestureHandler;
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

    protected PanRecognizer panRecognizer = new PanRecognizer();

    protected PinchRecognizer pinchRecognizer = new PinchRecognizer();

    protected RotationRecognizer rotationRecognizer = new RotationRecognizer();

    protected FlingRecognizer flingRecognizer = new FlingRecognizer();

    protected PanRecognizer tiltRecognizer = new PanRecognizer();

    protected OnGestureHandler onGestureHandler = new OnGestureHandler();

    protected GestureDetectorCompat mDetector;

    protected GestureRecognizer allRecognizers[] = {panRecognizer,
        pinchRecognizer,
        rotationRecognizer,
        tiltRecognizer,
        flingRecognizer};

    public BasicWorldWindowController() {
        this(null);
    }

    public BasicWorldWindowController(Context context) {
        for (GestureRecognizer recognizer : allRecognizers) {
            recognizer.addListener(this);
        }

        if (context != null) {
            onGestureHandler.setFlingRecognizer(flingRecognizer);
            mDetector = new GestureDetectorCompat(context, onGestureHandler);
        }

        panRecognizer.setMaxNumberOfPointers(2);
        tiltRecognizer.setMinNumberOfPointers(3); // TODO support for two-finger tilt gestures Issue #15
    }

    public WorldWindow getWorldWindow() {
        return wwd;
    }

    @Override
    public void setWorldWindow(WorldWindow wwd) {
        this.wwd = wwd;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        if (mDetector != null) {
            handled = mDetector.onTouchEvent(event);
        }

        for (GestureRecognizer gestureRecognizer : allRecognizers) {
            // use or-assignment to indicate if any recognizer handled the event
            handled |= gestureRecognizer.onTouchEvent(event);
        }

        return handled;
    }

    @Override
    public void gestureStateChanged(MotionEvent event, GestureRecognizer recognizer) {
        if (recognizer == panRecognizer) {
            handlePan(recognizer);
        } else if (recognizer == pinchRecognizer) {
            handlePinch(recognizer);
        } else if (recognizer == rotationRecognizer) {
            handleRotate(recognizer);
        } else if (recognizer == tiltRecognizer) {
            handleTilt(recognizer);
        } else if (recognizer == flingRecognizer) {
            handleFling(recognizer);
        }
    }

    private void handleFling(GestureRecognizer recognizer) {
        int state = recognizer.getState();

        if (state == WorldWind.BEGAN) {
            gestureDidBegin();
            flingRecognizer.fling(lookAt, wwd);
        } else if (state == WorldWind.ENDED) {
            gestureDidEnd();
        }
    }

    protected void handlePan(GestureRecognizer recognizer) {
        int state = recognizer.getState();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
            this.lastX = 0;
            this.lastY = 0;
        } else if (state == WorldWind.CHANGED) {
            float dx = recognizer.getTranslationX();
            float dy = recognizer.getTranslationY();

            // Get the navigator's current position.
            double lat = this.lookAt.latitude;
            double lon = this.lookAt.longitude;
            double rng = this.lookAt.range;

            // Convert the translation from screen coordinates to degrees. Use the navigator's range as a metric for
            // converting screen pixels to meters, and use the globe's radius for converting from meters to arc degrees.
            double metersPerPixel = this.wwd.pixelSizeAtDistance(rng);
            double forwardMeters = (dy - this.lastY) * metersPerPixel;
            double sideMeters = -(dx - this.lastX) * metersPerPixel;
            this.lastX = dx;
            this.lastY = dy;

            double globeRadius = this.wwd.getGlobe().getRadiusAt(lat, lon);
            double forwardDegrees = Math.toDegrees(forwardMeters / globeRadius);
            double sideDegrees = Math.toDegrees(sideMeters / globeRadius);

            // Adjust the change in latitude and longitude based on the navigator's heading.
            double heading = this.lookAt.heading;
            double headingRadians = Math.toRadians(heading);
            double sinHeading = Math.sin(headingRadians);
            double cosHeading = Math.cos(headingRadians);
            lat += forwardDegrees * cosHeading - sideDegrees * sinHeading;
            lon += forwardDegrees * sinHeading + sideDegrees * cosHeading;

            // If the navigator has panned over either pole, compensate by adjusting the longitude and heading to move
            // the navigator to the appropriate spot on the other side of the pole.
            if (lat < -90 || lat > 90) {
                this.lookAt.latitude = Location.normalizeLatitude(lat);
                this.lookAt.longitude = Location.normalizeLongitude(lon + 180);
                this.lookAt.heading = WWMath.normalizeAngle360(heading + 180);
            } else if (lon < -180 || lon > 180) {
                this.lookAt.latitude = lat;
                this.lookAt.longitude = Location.normalizeLongitude(lon);
            } else {
                this.lookAt.latitude = lat;
                this.lookAt.longitude = lon;
            }

            this.wwd.getNavigator().setAsLookAt(this.wwd.getGlobe(), this.lookAt);
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
                // Apply the change in scale to the navigator, relative to when the gesture began.
                this.lookAt.range = this.beginLookAt.range / scale;
                this.applyLimits(this.lookAt);

                this.wwd.getNavigator().setAsLookAt(this.wwd.getGlobe(), this.lookAt);
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
            // Apply the change in rotation to the navigator, relative to the navigator's current values.
            double headingDegrees = this.lastRotation - rotation;
            this.lookAt.heading = WWMath.normalizeAngle360(this.lookAt.heading + headingDegrees);
            this.lastRotation = rotation;

            this.wwd.getNavigator().setAsLookAt(this.wwd.getGlobe(), this.lookAt);
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
            // Apply the change in tilt to the navigator, relative to when the gesture began.
            double headingDegrees = 180 * dx / this.wwd.getWidth();
            double tiltDegrees = -180 * dy / this.wwd.getHeight();
            this.lookAt.heading = WWMath.normalizeAngle360(this.beginLookAt.heading + headingDegrees);
            this.lookAt.tilt = this.beginLookAt.tilt + tiltDegrees;
            this.applyLimits(this.lookAt);

            this.wwd.getNavigator().setAsLookAt(this.wwd.getGlobe(), this.lookAt);
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

        double maxTilt = 80;
        lookAt.tilt = WWMath.clamp(lookAt.tilt, 0, maxTilt);
    }

    protected void gestureDidBegin() {
        if (this.activeGestures++ == 0) {
            this.wwd.getNavigator().getAsLookAt(this.wwd.getGlobe(), this.beginLookAt);
            this.lookAt.set(this.beginLookAt);
        }
    }

    protected void gestureDidEnd() {
        if (this.activeGestures > 0) { // this should always be the case, but we check anyway
            this.activeGestures--;
        }
    }
}
