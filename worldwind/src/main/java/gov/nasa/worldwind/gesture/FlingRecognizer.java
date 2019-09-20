/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.MotionEvent;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.util.WWMath;

public class FlingRecognizer extends GestureRecognizer {

    private PointF slope = new PointF();

    private final int TOTAL_TIME = 2000;

    private final int MAX_ANIMATION_DISTANCE = 300;

    private ValueAnimator valueAnimator;

    boolean isValidFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        if (event1 == null || event2 == null ||
            (!isValidVelocity(velocityX) && !isValidVelocity(velocityY))) {
            return false;
        }

        slope.set((event2.getX() - event1.getX()) / -TOTAL_TIME,
            (event2.getY() - event1.getY()) / -TOTAL_TIME);

        transitionToState(event1, WorldWind.BEGAN);
        return true;
    }

    //only handle flings of a certain speed
    private boolean isValidVelocity(float speed) {
        speed = Math.abs(speed);
        return speed > 2000 && speed < 15000;
    }

    void stopAnimation() {
        if (valueAnimator != null) {
            valueAnimator.end();
        }
    }

    public void fling(final LookAt lookAt, final WorldWindow wwd) {
        valueAnimator = ValueAnimator.ofInt(0);
        valueAnimator.setDuration(TOTAL_TIME);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            PointF startingPoint = new PointF();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float speed = 10 / animation.getAnimatedFraction();

                startingPoint.set(slope.x * speed, slope.y * speed);
                conformMaxDistance();

                if (Math.abs(startingPoint.x) < .5 || Math.abs(startingPoint.y) < .5) {
                    return;
                }

                pan(lookAt, startingPoint, wwd);
            }

            /*
            TODO: involve lookAt.range in the max anim calc to determine what the actual max should be
            this will stop the globe from overspinning in zoomed out mode
             */

            private void conformMaxDistance() {
                if (startingPoint.x > MAX_ANIMATION_DISTANCE) {
                    startingPoint.x = MAX_ANIMATION_DISTANCE;
                } else if (startingPoint.x < -MAX_ANIMATION_DISTANCE) {
                    startingPoint.x = -MAX_ANIMATION_DISTANCE;
                }
                if (startingPoint.y > MAX_ANIMATION_DISTANCE) {
                    startingPoint.y = MAX_ANIMATION_DISTANCE;
                } else if (startingPoint.y < -MAX_ANIMATION_DISTANCE) {
                    startingPoint.y = -MAX_ANIMATION_DISTANCE;
                }
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                transitionToState(null, WorldWind.ENDED);
                valueAnimator = null;
            }
        });

        valueAnimator.start();
    }

    //TODO: add this to a util class or find a better spot for it, also, overload with Camera class?
    private void pan(LookAt lookAt, PointF destination, WorldWindow wwd) {

        float dx = getTranslationX();
        float dy = getTranslationY();
        // Get the navigator's current position.
        double lat = lookAt.latitude;
        double lon = lookAt.longitude;
        double rng = lookAt.range;

        // Convert the translation from screen coordinates to degrees. Use the navigator's range as a metric for
        // converting screen pixels to meters, and use the globe's radius for converting from meters to arc degrees.
        double metersPerPixel = wwd.pixelSizeAtDistance(rng);
        double forwardMeters = (dy - destination.y) * metersPerPixel;
        double sideMeters = -(dx - destination.x) * metersPerPixel;

        double globeRadius = wwd.getGlobe().getRadiusAt(lat, lon);
        double forwardDegrees = Math.toDegrees(forwardMeters / globeRadius);
        double sideDegrees = Math.toDegrees(sideMeters / globeRadius);

        // Adjust the change in latitude and longitude based on the navigator's heading.
        double heading = lookAt.heading;
        double headingRadians = Math.toRadians(heading);
        double sinHeading = Math.sin(headingRadians);
        double cosHeading = Math.cos(headingRadians);
        lat += forwardDegrees * cosHeading - sideDegrees * sinHeading;
        lon += forwardDegrees * sinHeading + sideDegrees * cosHeading;

        // If the navigator has panned over either pole, compensate by adjusting the longitude and heading to move
        // the navigator to the appropriate spot on the other side of the pole.
        if (lat < -90 || lat > 90) {
            lookAt.latitude = Location.normalizeLatitude(lat);
            lookAt.longitude = Location.normalizeLongitude(lon + 180);
            lookAt.heading = WWMath.normalizeAngle360(heading + 180);
        } else if (lon < -180 || lon > 180) {
            lookAt.latitude = lat;
            lookAt.longitude = Location.normalizeLongitude(lon);
        } else {
            lookAt.latitude = lat;
            lookAt.longitude = lon;
        }

        wwd.getNavigator().setAsLookAt(wwd.getGlobe(), lookAt);
        wwd.requestRedraw();
    }
}