/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import gov.nasa.worldwind.geom.Ellipsoid;
import gov.nasa.worldwind.util.MessageService;
import gov.nasa.worldwind.util.TaskService;

public class WorldWind {

    /**
     * {@link AltitudeMode} constant indicating an altitude relative to the globe's ellipsoid. Ignores the elevation of
     * the terrain directly beneath the position's latitude and longitude.
     */
    public static final int ABSOLUTE = 0;

    /**
     * {@link AltitudeMode} constant indicating an altitude on the terrain. Ignores a position's specified altitude, and
     * always places the position on the terrain.
     */
    public static final int CLAMP_TO_GROUND = 1;

    /**
     * {@link AltitudeMode} constant indicating an altitude relative to the terrain. The altitude indicates height above
     * the terrain directly beneath the position's latitude and longitude.
     */
    public static final int RELATIVE_TO_GROUND = 2;

    /**
     * Altitude mode indicates how World Wind interprets a position's altitude component. Accepted values are {@link
     * #ABSOLUTE}, {@link #CLAMP_TO_GROUND} and {@link #RELATIVE_TO_GROUND}.
     */
    @IntDef({ABSOLUTE, CLAMP_TO_GROUND, RELATIVE_TO_GROUND})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AltitudeMode {

    }

    /**
     * {@link DrawableGroup} constant indicating drawables displayed before everything else. This group is typically
     * used to display atmosphere and stars before all other drawables.
     */
    public static final int BACKGROUND_DRAWABLE = 0;

    /**
     * {@link DrawableGroup} constant indicating drawables displayed on the globe's surface. Surface drawables are
     * displayed beneath shapes and screen drawables.
     */
    public static final int SURFACE_DRAWABLE = 1;

    /**
     * {@link DrawableGroup} constant indicating shape drawables, such as placemarks, polygons and polylines. Shape
     * drawables are displayed on top of surface drawables, but beneath screen drawables.
     */
    public static final int SHAPE_DRAWABLE = 2;

    /**
     * {@link DrawableGroup} constant indicating drawables displayed in the plane of the screen. Screen drawables are
     * displayed on top of everything else.
     */
    public static final int SCREEN_DRAWABLE = 3;

    /**
     * Drawable group provides a standard set of group IDs for organizing World Window drawing into four phases:
     * background, surface, shape, and screen. Accepted values are {@link #BACKGROUND_DRAWABLE}, {@link
     * #SURFACE_DRAWABLE}, {@link #SHAPE_DRAWABLE} and {@link #SCREEN_DRAWABLE}.
     */
    @IntDef({BACKGROUND_DRAWABLE, SURFACE_DRAWABLE, SHAPE_DRAWABLE, SCREEN_DRAWABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawableGroup {

    }

    /**
     * {@link GestureState} constant for the POSSIBLE gesture recognizer state. Gesture recognizers in this state are
     * idle when there is no input event to evaluate, or are evaluating input events to determine whether or not to
     * transition into another state.
     */
    public static final int POSSIBLE = 0;

    /**
     * {@link GestureState} constant for the FAILED gesture recognizer state. Gesture recognizers transition to this
     * state from the POSSIBLE state when the gesture cannot be recognized given the current input.
     */
    public static final int FAILED = 1;

    /**
     * {@link GestureState} constant for the  RECOGNIZED gesture recognizer state. Discrete gesture recognizers
     * transition to this state from the POSSIBLE state when the gesture is recognized.
     */
    public static final int RECOGNIZED = 2;

    /**
     * {@link GestureState} constant for the BEGAN gesture recognizer state. Continuous gesture recognizers transition
     * to this state from the POSSIBLE state when the gesture is first recognized.
     */
    public static final int BEGAN = 3;

    /**
     * {@link GestureState} constant indicating the CHANGED gesture recognizer state. Continuous gesture recognizers
     * transition to this state from the BEGAN state or the CHANGED state, whenever an input event indicates a change in
     * the gesture.
     */
    public static final int CHANGED = 4;

    /**
     * {@link GestureState} constant for the CANCELLED gesture recognizer state. Continuous gesture recognizers may
     * transition to this state from the BEGAN state or the CHANGED state when the touch events are cancelled.
     */
    public static final int CANCELLED = 5;

    /**
     * {@link GestureState} constant indicating the ENDED gesture recognizer state. Continuous gesture recognizers
     * transition to this state from either the BEGAN state or the CHANGED state when the current input no longer
     * represents the gesture.
     */
    public static final int ENDED = 6;

    /**
     * Gesture state indicates a GestureRecognizer's current state. Accepted values are {@link #POSSIBLE}, {@link
     * #FAILED}, {@link #RECOGNIZED}, {@link #BEGAN}, {@link #CHANGED}, {@link #CANCELLED}, and {@link #ENDED}.
     */
    @IntDef({POSSIBLE, FAILED, RECOGNIZED, BEGAN, CHANGED, CANCELLED, ENDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureState {

    }

    /**
     * {@link ImageConfig} constant indicating 32-bit RGBA_8888 image configuration.
     */
    public static final int RGBA_8888 = 0;

    /**
     * {@link ImageConfig} constant indicating 16-bit RGBA_565 image configuration.
     */
    public static final int RGB_565 = 1;

    /**
     * Image config indicates the in-memory representation for images displayed by World Wind components. Images are
     * typically represented in the 32-bit RGBA_8888 configuration, the highest quality available. Components that do
     * not require an alpha channel and want to conserve memory may use the 16-bit RGBA_565 configuration. Accepted
     * values are {@link #RGBA_8888} and {@link #RGB_565}.
     */
    @IntDef({RGBA_8888, RGB_565})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ImageConfig {

    }

    /**
     * {@link NavigatorAction} constant indicating that the navigator has moved.
     */
    public static final int NAVIGATOR_MOVED = 0;

    /**
     * {@link NavigatorAction} constant indicating that the navigator has stopped moving.
     */
    public static final int NAVIGATOR_STOPPED = 1;

    /**
     * Navigator event action indicates the type of NavigatorEvent that has been generated. Accepted values are {@link
     * #NAVIGATOR_MOVED} and {@link #NAVIGATOR_STOPPED}.
     */
    @IntDef({NAVIGATOR_MOVED, NAVIGATOR_STOPPED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NavigatorAction {

    }

    /**
     * {@link OffsetMode} constant indicating that the associated parameters are fractional values of the virtual
     * rectangle's width or height in the range [0, 1], where 0 indicates the rectangle's origin and 1 indicates the
     * corner opposite its origin.
     */
    public static final int OFFSET_FRACTION = 0;

    /**
     * {@link OffsetMode} constant indicating that the associated parameters are in units of pixels relative to the
     * virtual rectangle's corner opposite its origin corner.
     */
    public static final int OFFSET_INSET_PIXELS = 1;

    /**
     * {@link OffsetMode} constant indicating that the associated parameters are in units of pixels relative to the
     * virtual rectangle's origin.
     */
    public static final int OFFSET_PIXELS = 2;

    /**
     * Offset mode indicates how World Wind interprets an offset's x and y values. Accepted values are {@link
     * #OFFSET_FRACTION}, {@link #OFFSET_INSET_PIXELS} and {@link #OFFSET_PIXELS}.
     */
    @IntDef({OFFSET_FRACTION, OFFSET_INSET_PIXELS, OFFSET_PIXELS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OffsetMode {

    }

    /**
     * {@link OrientationMode} constant indicating that the related value is specified relative to the globe.
     */
    public static final int RELATIVE_TO_GLOBE = 0;

    /**
     * {@link OrientationMode} constant indicating that the related value is specified relative to the plane of the
     * screen.
     */
    public static final int RELATIVE_TO_SCREEN = 1;

    /**
     * Orientation mode indicates how World Wind interprets a renderable's orientation value, e.g., tilt and rotate
     * values. Accepted values are {@link #RELATIVE_TO_GLOBE}, and {@link #RELATIVE_TO_SCREEN}.
     */
    @IntDef({RELATIVE_TO_GLOBE, RELATIVE_TO_SCREEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {

    }

    /**
     * {@link PathType} constant indicating a great circle arc between two locations.
     */
    public static final int GREAT_CIRCLE = 0;

    /**
     * {@link PathType} constant indicating simple linear interpolation between two locations.
     */
    public static final int LINEAR = 1;

    /**
     * {@link PathType} constant indicating a line of constant bearing between two locations.
     */
    public static final int RHUMB_LINE = 2;

    /**
     * Path type indicates how World Wind create a geographic path between two locations. Accepted values are {@link
     * #GREAT_CIRCLE}, {@link #LINEAR} and {@link #RHUMB_LINE}.
     */
    @IntDef({GREAT_CIRCLE, LINEAR, RHUMB_LINE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PathType {

    }

    /**
     * {@link ResamplingMode} constant indicating bilinear image sampling.
     */
    public static final int BILINEAR = 0;

    /**
     * {@link ResamplingMode} constant indicating nearest neighbor image sampling.
     */
    public static final int NEAREST_NEIGHBOR = 1;

    /**
     * Resampling mode indicates the image sampling algorithm used by World Wind to display images that appear larger or
     * smaller on screen than their native resolution. Accepted values are {@link WorldWind#BILINEAR} and {@link
     * WorldWind#NEAREST_NEIGHBOR}.
     */
    @IntDef({BILINEAR, NEAREST_NEIGHBOR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ResamplingMode {

    }

    /**
     * {@link WrapMode} constant indicating that the image's edge pixels should be displayed outside of the image
     * bounds.
     */
    public static final int CLAMP = 0;

    /**
     * {@link WrapMode} constant indicating that the image should display as a repeating pattern outside of the image
     * bounds.
     */
    public static final int REPEAT = 1;

    /**
     * Wrap mode indicates how World Wind displays the contents of an image when attempting to draw a region outside of
     * the image bounds. Accepted values are {@link WorldWind#CLAMP} and {@link WorldWind#REPEAT}.
     */
    @IntDef({CLAMP, REPEAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WrapMode {

    }

    /**
     * Notification constant requesting that World Window instances update their display.
     */
    public static final String REQUEST_REDRAW = "gov.nasa.worldwind.RequestRedraw";

    /**
     * WGS 84 reference value for Earth's semi-major axis: {@code 6378137.0}. WGS 84 reference values taken from <a
     * href="http://earth-info.nga.mil/GandG/publications/NGA_STND_0036_1_0_0_WGS84/NGA.STND.0036_1.0.0_WGS84.pdf"/>.
     */
    public static final double WGS84_SEMI_MAJOR_AXIS = 6378137.0;

    /**
     * WGS 84 reference value for Earth's inverse flattening: {@code 298.257223563}. WGS 84 reference values taken from
     * <a href="http://earth-info.nga.mil/GandG/publications/NGA_STND_0036_1_0_0_WGS84/NGA.STND.0036_1.0.0_WGS84.pdf"/>.
     */
    public static final double WGS84_INVERSE_FLATTENING = 298.257223563;

    /**
     * WGS 84 reference ellipsoid for Earth. The ellipsoid's semi-major axis and inverse flattening factor are
     * configured according to the WGS 84 reference system (aka WGS 1984, EPSG:4326). WGS 84 reference values taken from
     * <a href="http://earth-info.nga.mil/GandG/publications/NGA_STND_0036_1_0_0_WGS84/NGA.STND.0036_1.0.0_WGS84.pdf"/>.
     */
    public static final Ellipsoid WGS84_ELLIPSOID = new Ellipsoid(WGS84_SEMI_MAJOR_AXIS, WGS84_INVERSE_FLATTENING);

    /**
     * Provides a global mechanism for broadcasting notifications within the World Wind library and World Wind
     * applications.
     */
    protected static MessageService messageService = new MessageService();

    /**
     * Provides a global service for running asynchronous tasks within the World Wind library and World Wind
     * applications.
     */
    protected static TaskService taskService = new TaskService();

    /**
     * Returns a singleton MessageService instance that provides a mechanism for broadcasting notifications within the
     * World Wind library and World Wind applications.
     *
     * @return the singleton message center
     */
    public static MessageService messageService() {
        return messageService;
    }

    /**
     * Returns a singleton TaskService instance that provides a mechanism for running asynchronous tasks within the
     * World Wind library and World Wind applications.
     *
     * @return the singleton task service
     */
    public static TaskService taskService() {
        return taskService;
    }

    /**
     * Requests that all World Window instances update their display. Internally, this dispatches a REQUEST_REDRAW
     * message to the World Wind message center.
     */
    public static void requestRedraw() {
        messageService.postMessage(REQUEST_REDRAW, null, null); // specify null for no sender, no user properties
    }
}
