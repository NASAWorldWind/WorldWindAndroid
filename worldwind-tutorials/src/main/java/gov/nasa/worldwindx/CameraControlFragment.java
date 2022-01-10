/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.gesture.GestureRecognizer;
import gov.nasa.worldwind.gesture.PinchRecognizer;
import gov.nasa.worldwind.gesture.RotationRecognizer;
import gov.nasa.worldwind.util.WWMath;

public class CameraControlFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow object with a custom WorldWindowController.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Override the default "look at" gesture behavior with a camera centric gesture controller
        wwd.setWorldWindowController(new CameraController(wwd));

        // Apply camera position above KOXR airport, Oxnard, CA
        wwd.getCamera().set(34.2, -119.2,
                10000, WorldWind.ABSOLUTE,
                90, // Looking east
                70, // Lookup up from nadir
                0); // No roll

        return wwd;
    }

    /**
     * A custom WorldWindController that uses gestures to control the camera directly via the setAsCamera interface
     * instead of the default setAsLookAt interface.
     */
    private static class CameraController extends BasicWorldWindowController {

        protected double beginHeading;

        protected double beginTilt;

        public CameraController(WorldWindow wwd) {
            super(wwd);
        }

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
                Camera camera = this.getWorldWindow().getCamera();

                // Get the camera's current position.
                double lat = camera.position.latitude;
                double lon = camera.position.longitude;
                double alt = camera.position.altitude;

                // Convert the translation from screen coordinates to degrees. Use the camera's range as a metric for
                // converting screen pixels to meters, and use the globe's radius for converting from meters to arc degrees.
                double metersPerPixel = this.wwd.pixelSizeAtDistance(alt);
                double forwardMeters = (dy - this.lastY) * metersPerPixel;
                double sideMeters = -(dx - this.lastX) * metersPerPixel;
                this.lastX = dx;
                this.lastY = dy;

                double globeRadius = this.wwd.getGlobe().getRadiusAt(lat, lon);
                double forwardDegrees = Math.toDegrees(forwardMeters / globeRadius);
                double sideDegrees = Math.toDegrees(sideMeters / globeRadius);

                // Adjust the change in latitude and longitude based on the camera's heading.
                double heading = camera.heading;
                double headingRadians = Math.toRadians(heading);
                double sinHeading = Math.sin(headingRadians);
                double cosHeading = Math.cos(headingRadians);
                lat += forwardDegrees * cosHeading - sideDegrees * sinHeading;
                lon += forwardDegrees * sinHeading + sideDegrees * cosHeading;

                // If the camera has panned over either pole, compensate by adjusting the longitude and heading to move
                // the camera to the appropriate spot on the other side of the pole.
                if (lat < -90 || lat > 90) {
                    camera.position.latitude = Location.normalizeLatitude(lat);
                    camera.position.longitude = Location.normalizeLongitude(lon + 180);
                } else if (lon < -180 || lon > 180) {
                    camera.position.latitude = lat;
                    camera.position.longitude = Location.normalizeLongitude(lon);
                } else {
                    camera.position.latitude = lat;
                    camera.position.longitude = lon;
                }
                //camera.heading = WWMath.normalizeAngle360(heading + sideDegrees * 1000);

                this.wwd.requestRedraw();
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
                    // Apply the change in scale to the camera, relative to when the gesture began.
                    scale = ((scale - 1) * 0.1f) + 1; // dampen the scale factor
                    Camera camera = this.getWorldWindow().getCamera();
                    camera.position.altitude = camera.position.altitude * scale;
                    this.applyLimits(camera);

                    this.wwd.requestRedraw();
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

                // Apply the change in rotation to the camera, relative to the camera's current values.
                double headingDegrees = this.lastRotation - rotation;
                Camera camera = this.getWorldWindow().getCamera();
                camera.heading = WWMath.normalizeAngle360(camera.heading + headingDegrees);
                this.lastRotation = rotation;

                this.wwd.requestRedraw();
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
                // Apply the change in tilt to the camera, relative to when the gesture began.
                double headingDegrees = 180 * dx / this.wwd.getWidth();
                double tiltDegrees = -180 * dy / this.wwd.getHeight();

                Camera camera = this.getWorldWindow().getCamera();
                camera.heading = WWMath.normalizeAngle360(this.beginHeading + headingDegrees);
                camera.tilt = this.beginTilt + tiltDegrees;
                this.applyLimits(camera);

                this.wwd.requestRedraw();
            } else if (state == WorldWind.ENDED || state == WorldWind.CANCELLED) {
                this.gestureDidEnd();
            }
        }

        @Override
        protected void gestureDidBegin() {
            if (this.activeGestures++ == 0) {
                Camera camera = this.getWorldWindow().getCamera();
                this.beginHeading = camera.heading;
                this.beginTilt = camera.tilt;
            }
        }

        protected void applyLimits(Camera camera) {
            double distanceToExtents = this.wwd.distanceToViewGlobeExtents();

            double minAltitude = 100;
            camera.position.altitude = WWMath.clamp(camera.position.altitude, minAltitude, distanceToExtents);

            // Limit the tilt to between nadir and the horizon (roughly)
            double r = wwd.getGlobe().getRadiusAt(camera.position.latitude, camera.position.latitude);
            double maxTilt = Math.toDegrees(Math.asin(r / (r + camera.position.altitude)));
            double minTilt = 0;
            camera.tilt = WWMath.clamp(camera.tilt, minTilt, maxTilt);
        }
    }
}
