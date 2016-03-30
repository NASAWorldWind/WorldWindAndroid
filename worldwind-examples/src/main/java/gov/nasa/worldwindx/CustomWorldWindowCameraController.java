/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.gesture.GestureRecognizer;
import gov.nasa.worldwind.gesture.PinchRecognizer;
import gov.nasa.worldwind.gesture.RotationRecognizer;
import gov.nasa.worldwind.util.WWMath;


class CustomWorldWindowCameraController extends BasicWorldWindowController {

    protected Camera camera = new Camera();

    protected Camera beginCamera = new Camera();

    @Override
    protected void handlePan(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float dx = recognizer.getTranslationX();
        float dy = recognizer.getTranslationY();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
            this.lastX = 0;
            this.lastY = 0;
        } else if (state == WorldWind.CHANGED) {
            // Get the navigator's current position.
            double lat = this.camera.latitude;
            double lon = this.camera.longitude;
            double alt = this.camera.altitude;

            // Convert the translation from screen coordinates to degrees. Use the navigator's range as a metric for
            // converting screen pixels to meters, and use the globe's radius for converting from meters to arc degrees.
            double metersPerPixel = this.wwd.pixelSizeAtDistance(alt);
            double forwardMeters = (dy - this.lastY) * metersPerPixel;
            double sideMeters = -(dx - this.lastX) * metersPerPixel;
            this.lastX = dx;
            this.lastY = dy;

            double globeRadius = this.wwd.getGlobe().getRadiusAt(lat, lon);
            double forwardDegrees = Math.toDegrees(forwardMeters / globeRadius);
            double sideDegrees = Math.toDegrees(sideMeters / globeRadius);

            // Adjust the change in latitude and longitude based on the navigator's heading.
            double heading = this.camera.heading;
            double headingRadians = Math.toRadians(heading);
            double sinHeading = Math.sin(headingRadians);
            double cosHeading = Math.cos(headingRadians);
            lat += forwardDegrees * cosHeading - sideDegrees * sinHeading;
            lon += forwardDegrees * sinHeading + sideDegrees * cosHeading;

            // If the navigator has panned over either pole, compensate by adjusting the longitude and heading to move
            // the navigator to the appropriate spot on the other side of the pole.
            if (lat < -90 || lat > 90) {
                this.camera.latitude = Location.normalizeLatitude(lat);
                this.camera.longitude = Location.normalizeLongitude(lon + 180);
            } else if (lon < -180 || lon > 180) {
                this.camera.latitude = lat;
                this.camera.longitude = Location.normalizeLongitude(lon);
            } else {
                this.camera.latitude = lat;
                this.camera.longitude = lon;
            }
            //this.camera.heading = WWMath.normalizeAngle360(heading + sideDegrees * 1000);

            this.wwd.getNavigator().setAsCamera(this.wwd.getGlobe(), this.camera);
            this.wwd.requestRender();
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    @Override
    protected void handlePinch(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float scale = ((PinchRecognizer) recognizer).getScale();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
        } else if (state == WorldWind.CHANGED) {
            if (scale != 0) {
                // Apply the change in scale to the navigator, relative to when the gesture began.
                this.camera.altitude = this.camera.altitude + (scale > 1 ? scale * 1000 : -1 / scale * 1000);
                this.applyLimits(this.camera);

                this.wwd.getNavigator().setAsCamera(this.wwd.getGlobe(), this.camera);
                this.wwd.requestRender();
            }
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    @Override
    protected void handleRotate(GestureRecognizer recognizer) {
        int state = recognizer.getState();
        float rotation = ((RotationRecognizer) recognizer).getRotation();

        if (state == WorldWind.BEGAN) {
            this.gestureDidBegin();
            this.lastRotation = 0;
        } else if (state == WorldWind.CHANGED) {
            // Apply the change in rotation to the navigator, relative to the navigator's current values.
            double headingDegrees = this.lastRotation - rotation;
            this.camera.heading = WWMath.normalizeAngle360(this.camera.heading + headingDegrees);
            this.lastRotation = rotation;

            this.wwd.getNavigator().setAsCamera(this.wwd.getGlobe(), this.camera);
            this.wwd.requestRender();
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    @Override
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
            this.camera.heading = WWMath.normalizeAngle360(this.beginCamera.heading + headingDegrees);
            this.camera.tilt = this.beginCamera.tilt + tiltDegrees;

            this.wwd.getNavigator().setAsCamera(this.wwd.getGlobe(), this.camera);
            this.wwd.requestRender();
        } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
            this.gestureDidEnd();
        }
    }

    protected void gestureDidBegin() {
        if (this.activeGestures++ == 0) {
            this.wwd.getNavigator().getAsCamera(this.wwd.getGlobe(), this.beginCamera);
            this.camera.set(this.beginCamera);
        }
    }

    protected void applyLimits(Camera camera) {
        double distanceToExtents = this.wwd.distanceToViewGlobeExtents();

        double minRange = 100;
        double maxRange = distanceToExtents * 2;
        camera.altitude = WWMath.clamp(camera.altitude, minRange, maxRange);

    }

}
