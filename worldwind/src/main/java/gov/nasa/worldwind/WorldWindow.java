/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.Viewport;
import gov.nasa.worldwind.globe.BasicTessellator;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.ProjectionWgs84;
import gov.nasa.worldwind.globe.Tessellator;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.RenderResourceCache;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.MessageListener;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;

/**
 * Provides a World Wind window that implements a virtual globe inside of the Android view hierarchy. By default, World
 * Window is configured to display an ellipsoidal globe using the WGS 84 reference values.
 */
public class WorldWindow extends GLSurfaceView implements Choreographer.FrameCallback, GLSurfaceView.Renderer, MessageListener {

    protected static final int MAX_FRAME_QUEUE_SIZE = 2;

    protected static final int MSG_ID_CLEAR_CACHE = 1;

    protected static final int MSG_ID_REQUEST_REDRAW = 2;

    protected static final int MSG_ID_SET_VIEWPORT = 3;

    protected static final int MSG_ID_SET_DEPTH_BITS = 4;

    /**
     * Planet or celestial object displayed by this World Window.
     */
    protected Globe globe = new Globe(WorldWind.WGS84_ELLIPSOID, new ProjectionWgs84());

    protected LayerList layers = new LayerList();

    protected Tessellator tessellator = new BasicTessellator();

    protected double verticalExaggeration = 1;

    protected double fieldOfView = 45;

    protected Navigator navigator = new Navigator();

    protected NavigatorEventSupport navigatorEvents = new NavigatorEventSupport(this);

    protected FrameController frameController = new BasicFrameController();

    protected FrameMetrics frameMetrics = new FrameMetrics();

    protected WorldWindowController worldWindowController = new BasicWorldWindowController();

    protected RenderResourceCache renderResourceCache;

    protected RenderContext rc = new RenderContext();

    protected DrawContext dc = new DrawContext();

    protected Viewport viewport = new Viewport();

    protected int depthBits;

    protected Pool<Frame> framePool = new SynchronizedPool<>();

    protected Queue<Frame> frameQueue = new ConcurrentLinkedQueue<>();

    protected Queue<Frame> pickQueue = new ConcurrentLinkedQueue<>();

    protected Frame currentFrame;

    protected boolean isPaused;

    protected boolean isWaitingForRedraw;

    protected Handler mainThreadHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_ID_CLEAR_CACHE) {
                renderResourceCache.clear();
            } else if (msg.what == MSG_ID_REQUEST_REDRAW) {
                requestRedraw();
            } else if (msg.what == MSG_ID_SET_VIEWPORT) {
                viewport.set((Viewport) msg.obj);
            } else if (msg.what == MSG_ID_SET_DEPTH_BITS) {
                depthBits = (Integer) msg.obj;
            }
            return false;
        }
    });

    private Matrix4 scratchModelview = new Matrix4();

    private Matrix4 scratchProjection = new Matrix4();

    private Vec3 scratchPoint = new Vec3();

    /**
     * Constructs a WorldWindow associated with the specified application context. This is the constructor to use when
     * creating a WorldWindow from code.
     */
    public WorldWindow(Context context) {
        super(context);
        this.init(null);
    }

    /**
     * Constructs a WorldWindow associated with the specified application context and EGL configuration chooser. This is
     * the constructor to use when creating a WorldWindow from code.
     */
    public WorldWindow(Context context, EGLConfigChooser configChooser) {
        super(context);
        this.init(configChooser);
    }

    /**
     * Constructs a WorldWindow associated with the specified application context and attributes from an XML tag. This
     * constructor is included to provide support for creating WorldWindow from an Android XML layout file, and is not
     * intended to be used directly.
     * <p/>
     * This is called when a view is being constructed from an XML file, supplying attributes that were specified in the
     * XML file. This version uses a default style of 0, so the only attribute values applied are those in the Context's
     * Theme and the given AttributeSet.
     */
    public WorldWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(null);
    }

    /**
     * Prepares this WorldWindow for drawing and event handling.
     *
     * @param configChooser optional argument for choosing an EGL configuration; may be null
     */
    protected void init(EGLConfigChooser configChooser) {
        // Initialize the World Window's navigator.
        Location initLocation = Location.fromTimeZone(TimeZone.getDefault());
        double initAltitude = this.distanceToViewGlobeExtents() * 1.1; // add 10% to the minimum distance to allow for space around the screen edges
        this.navigator.setLatitude(initLocation.latitude);
        this.navigator.setLongitude(initLocation.longitude);
        this.navigator.setAltitude(initAltitude);

        // Initialize the World Window's controller.
        this.worldWindowController.setWorldWindow(this);

        // Initialize the World Window's render resource cache.
        int cacheCapacity = RenderResourceCache.recommendedCapacity(this.getContext());
        this.renderResourceCache = new RenderResourceCache(cacheCapacity);

        // Set up to render on demand to an OpenGL ES 2.x context
        // TODO Investigate and use the EGL chooser submitted by jgiovino
        this.setEGLConfigChooser(configChooser);
        this.setEGLContextClientVersion(2); // must be called before setRenderer
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // must be called after setRenderer

        // Log a message with some basic information about the world window's configuration.
        Logger.log(Logger.INFO, "World Window initialized");
    }

    /**
     * Resets this WorldWindow to its initial internal state.
     */
    protected void reset() {
        // Reset any state associated with navigator events.
        this.navigatorEvents.reset();

        // Clear the render resource cache; it's entries are now invalid.
        this.renderResourceCache.clear();

        // Clear the viewport dimensions.
        this.viewport.setEmpty();

        // Clear the frame queue and recycle pending frames back into the frame pool.
        this.clearFrameQueue();

        // Cancel any outstanding request redraw messages.
        Choreographer.getInstance().removeFrameCallback(this);
        this.mainThreadHandler.removeMessages(MSG_ID_REQUEST_REDRAW /*msg.what*/);
        this.isWaitingForRedraw = false;
    }

    /**
     * Indicates the planet or celestial object displayed by this World Window. Defines the reference ellipsoid and
     * elevation models. Globe expresses its ellipsoidal parameters and elevation values in meters.
     * <p>
     * World Window's globe is initially configured with the WGS 84 reference ellipsoid.
     *
     * @return the globe displayed by this World Window
     */
    public Globe getGlobe() {
        return this.globe;
    }

    /**
     * Sets the planet or celestial object displayed by this World Window. Defines the reference ellipsoid and
     * elevation models. Globe expresses its ellipsoidal parameters and elevation values in meters.
     *
     * @param globe the globe to display
     *
     * @throws IllegalArgumentException If the globe is null
     */
    public void setGlobe(Globe globe) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setGlobe", "missingGlobe"));
        }

        this.globe = globe;
    }

    public Tessellator getTessellator() {
        return this.tessellator;
    }

    public void setTessellator(Tessellator tessellator) {
        if (tessellator == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setTessellator", "missingTessellator"));
        }

        this.tessellator = tessellator;
    }

    public LayerList getLayers() {
        return this.layers;
    }

    public void setLayers(LayerList layers) {
        if (layers == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setLayers", "missingList"));
        }

        this.layers = layers;
    }

    public double getVerticalExaggeration() {
        return this.verticalExaggeration;
    }

    public void setVerticalExaggeration(double verticalExaggeration) {
        if (verticalExaggeration <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setVerticalExaggeration", "invalidVerticalExaggeration"));
        }

        this.verticalExaggeration = verticalExaggeration;
    }

    public double getFieldOfView() {
        return this.fieldOfView;
    }

    public void setFieldOfView(double fovyDegrees) {
        if (fovyDegrees <= 0 || fovyDegrees >= 180) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setFieldOfView", "invalidFieldOfView"));
        }

        this.fieldOfView = fovyDegrees;
    }

    public Navigator getNavigator() {
        return this.navigator;
    }

    public void setNavigator(Navigator navigator) {
        if (navigator == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setNavigator", "missingNavigator"));
        }

        this.navigator = navigator;
    }

    public void addNavigatorListener(NavigatorListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "addNavigatorListener", "missingListener"));
        }

        this.navigatorEvents.addNavigatorListener(listener);
    }

    public void removeNavigatorListener(NavigatorListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "removeNavigatorListener", "missingListener"));
        }

        this.navigatorEvents.removeNavigatorListener(listener);
    }

    public long getNavigatorStoppedDelay() {
        return this.navigatorEvents.getNavigatorStoppedDelay();
    }

    public void setNavigatorStoppedDelay(long delay, TimeUnit unit) {
        this.navigatorEvents.setNavigatorStoppedDelay(delay, unit);
    }

    public FrameController getFrameController() {
        return this.frameController;
    }

    public void setFrameController(FrameController frameController) {
        if (frameController == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setFrameController", "missingController"));
        }

        this.frameController = frameController;
    }

    public FrameMetrics getFrameMetrics() {
        return this.frameMetrics;
    }

    public void setFrameMetrics(FrameMetrics frameMetrics) {
        if (frameMetrics == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setFrameMetrics", "missingFrameMetrics"));
        }

        this.frameMetrics = frameMetrics;
    }

    public WorldWindowController getWorldWindowController() {
        return this.worldWindowController;
    }

    public void setWorldWindowController(WorldWindowController controller) {
        if (controller == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setWorldWindowController", "missingController"));
        }

        this.worldWindowController.setWorldWindow(null); // detach the old controller
        this.worldWindowController = controller; // switch to the new controller
        this.worldWindowController.setWorldWindow(this); // attach the new controller
    }

    public RenderResourceCache getRenderResourceCache() {
        return this.renderResourceCache;
    }

    public void setRenderResourceCache(RenderResourceCache cache) {
        if (cache == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setRenderResourceCache", "missingCache"));
        }

        // TODO provide a mechanism for the old cache to evict its entries
        this.renderResourceCache = cache;
    }

    /**
     * Determines the World Wind objects displayed at a screen point. The screen point is interpreted as coordinates in
     * Android screen pixels relative to this View.
     * <p/>
     * If the screen point intersects any number of World Wind shapes, the returned list contains a picked object
     * identifying the top shape at the screen point. This picked object includes the shape renderable (or its non-null
     * pick delegate) and the World Wind layer that displayed the shape. Shapes which are either hidden behind another
     * shape at the screen point or hidden behind terrain at the screen point are omitted from the returned list.
     * Therefore if the returned list contains a picked object identifying a shape, it is always marked as 'on top'.
     * <p/>
     * If the screen point intersects the World Wind terrain, the returned list contains a picked object identifying the
     * associated geographic position. If there are no shapes in the World Wind scene between the terrain and the screen
     * point, the terrain picked object is marked as 'on top'.
     * <p/>
     * This returns an empty list when nothing in the World Wind scene intersects the screen point, when the screen
     * point is outside this View's bounds, or if the OpenGL thread displaying the World Window's scene is paused (or
     * becomes paused while this method is executing).
     *
     * @param x the screen point's X coordinate in Android screen pixels
     * @param y the screen point's Y coordinate in Android screen pixels
     *
     * @return a list of World Wind objects at the screen point
     */
    public PickedObjectList pick(float x, float y) {
        // Allocate a list in which to collect and return the picked objects.
        PickedObjectList pickedObjects = new PickedObjectList();

        // Nothing can be picked if the World Window's OpenGL thread is paused.
        if (this.isPaused) {
            return pickedObjects;
        }

        // Compute the pick point in OpenGL screen coordinates, rounding to the nearest whole pixel. Nothing can be picked
        // if pick point is outside the World Window's viewport.
        int px = Math.round(x);
        int py = Math.round(this.getHeight() - y);
        if (!this.viewport.contains(px, py)) {
            return pickedObjects;
        }

        // Compute the line in Cartesian coordinates that passes through the pick point. Nothing can be picked if the
        // line cannot be constructed.
        Line pickRay = new Line();
        if (!this.rayThroughScreenPoint(x, y, pickRay)) { // use the original XY coordinates for the pick ray
            return pickedObjects;
        }

        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        Frame frame = Frame.obtain(this.framePool);
        frame.pickedObjects = pickedObjects;
        frame.pickViewport = new Viewport(px - 1, py - 1, 3, 3); // 3x3 viewport centered on the pick point
        frame.pickViewport.intersect(this.viewport); // limit the 3x3 viewport to the screen viewport
        frame.pickPoint = new Vec2(px, py);
        frame.pickRay = pickRay;
        frame.pickMode = true;
        this.renderFrame(frame);

        // Wait until the OpenGL thread is done processing the frame and resolving the picked objects.
        frame.awaitDone();

        return pickedObjects;
    }

    /**
     * Determines the World Wind shapes displayed in a screen rectangle. The screen rectangle is interpreted as
     * coordinates in Android screen pixels relative to this view.
     * <p/>
     * If the screen rectangle intersects any number of World Wind shapes, the returned list contains a picked object
     * identifying the all of the top shapes in the rectangle. This picked object includes the shape renderable (or its
     * non-null pick delegate) and the World Wind layer that displayed the shape. Shapes which are entirely hidden
     * behind another shape in the screen rectangle or are entirely hidden behind terrain in the screen rectangle are
     * omitted from the returned list.
     * <p/>
     * This returns an empty list when no shapes in the World Wind scene intersect the screen rectangle, when the screen
     * rectangle is outside this View's bounds, or if the OpenGL thread displaying the World Window's scene is paused
     * (or becomes paused while this method is executing).
     *
     * @param x      the screen rectangle's X coordinate in Android screen pixels
     * @param y      the screen rectangle's Y coordinate in Android screen pixels
     * @param width  the screen rectangle's width in Android screen pixels
     * @param height the screen rectangle's height in Android screen pixels
     *
     * @return a list of World Wind shapes in the screen rectangle
     */
    public PickedObjectList pickShapesInRect(float x, float y, float width, float height) {
        // Allocate a list in which to collect and return the picked objects.
        PickedObjectList pickedObjects = new PickedObjectList();

        // Nothing can be picked if the World Window's OpenGL thread is paused.
        if (this.isPaused) {
            return pickedObjects;
        }

        int px = (int) Math.floor(x);
        int py = (int) Math.floor(this.getHeight() - (y + height));
        int pw = (int) Math.ceil(width);
        int ph = (int) Math.ceil(height);
        if (!this.viewport.intersects(px, py, pw, ph)) {
            return pickedObjects;
        }

        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        Frame frame = Frame.obtain(this.framePool);
        frame.pickedObjects = pickedObjects;
        frame.pickViewport = new Viewport(px, py, pw, ph); // caller-specified pick rectangle
        frame.pickViewport.intersect(this.viewport); // limit the pick viewport to the screen viewport
        frame.pickMode = true;
        this.renderFrame(frame);

        // Wait until the OpenGL thread is done processing the frame and resolving the picked objects.
        frame.awaitDone();

        return pickedObjects;
    }

    /**
     * Transforms a Cartesian coordinate point to Android screen coordinates. The resultant screen point is in Android
     * screen pixels relative to this View.
     * <p/>
     * This stores the converted point in the result argument, and returns a boolean value indicating whether or not the
     * converted is successful. This returns false if the Cartesian point is clipped by either the World Window's near
     * clipping plane or far clipping plane.
     *
     * @param x      the Cartesian point's x component in meters
     * @param y      the Cartesian point's y component in meters
     * @param z      the Cartesian point's z component in meters
     * @param result a pre-allocated {@link PointF} in which to return the screen point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If the result is null
     */
    public boolean cartesianToScreenPoint(double x, double y, double z, PointF result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "cartesianToScreenPoint", "missingResult"));
        }

        // Compute the World Window's modelview-projection matrix.
        this.computeViewingTransform(this.scratchProjection, this.scratchModelview);
        this.scratchProjection.multiplyByMatrix(this.scratchModelview);

        // Transform the Cartesian point to OpenGL screen coordinates. Complete the transformation by converting to
        // Android screen coordinates and discarding the screen Z component.
        if (this.scratchProjection.project(x, y, z, this.viewport, this.scratchPoint)) {
            result.x = (float) this.scratchPoint.x;
            result.y = (float) (this.getHeight() - this.scratchPoint.y);
            return true;
        }

        return false;
    }

    /**
     * Transforms a geographic position to Android screen coordinates. The resultant screen point is in Android screen
     * pixels relative to this View.
     * <p/>
     * This stores the converted point in the result argument, and returns a boolean value indicating whether or not the
     * converted is successful. This returns false if the Cartesian point is clipped by either of the World Window's
     * near clipping plane or far clipping plane.
     *
     * @param latitude  the position's latitude in degrees
     * @param longitude the position's longitude in degrees
     * @param altitude  the position's altitude in meters
     * @param result    a pre-allocated {@link PointF} in which to return the screen point
     *
     * @return true if the transformation is successful, otherwise false
     *
     * @throws IllegalArgumentException If the result is null
     */
    public boolean geographicToScreenPoint(double latitude, double longitude, double altitude, PointF result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "geographicToScreenPoint", "missingResult"));
        }

        // Convert the position from geographic coordinates to Cartesian coordinates.
        this.globe.geographicToCartesian(latitude, longitude, altitude, this.scratchPoint);

        // Convert the position from Cartesian coordinates to screen coordinates.
        return this.cartesianToScreenPoint(this.scratchPoint.x, this.scratchPoint.y, this.scratchPoint.z, result);
    }

    /**
     * Computes a Cartesian coordinate ray that passes through through a screen point. The screen point is interpreted
     * as coordinates in Android screen pixels relative to this View.
     *
     * @param x      the screen point's X coordinate in Android screen pixels
     * @param y      the screen point's Y coordinate in Android screen pixels
     * @param result a pre-allocated Line in which to return the computed ray
     *
     * @return the result set to the computed ray in Cartesian coordinates
     *
     * @throws IllegalArgumentException If the result is null
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public boolean rayThroughScreenPoint(float x, float y, Line result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "rayThroughScreenPoint", "missingResult"));
        }

        // Convert from Android screen coordinates to OpenGL screen coordinates by inverting the Y axis.
        double sx = x;
        double sy = this.getHeight() - y;

        // Compute the inverse modelview-projection matrix corresponding to the World Window's current Navigator state.
        this.computeViewingTransform(this.scratchProjection, this.scratchModelview);
        this.scratchProjection.multiplyByMatrix(this.scratchModelview).invert();

        // Transform the screen point to Cartesian coordinates at the near and far clip planes, store the result in the
        // ray's origin and direction, respectively. Complete the ray direction by subtracting the near point from the
        // far point and normalizing.
        if (this.scratchProjection.unProject(sx, sy, this.viewport, result.origin /*near*/, result.direction /*far*/)) {
            result.direction.subtract(result.origin).normalize();
            return true;
        }

        return false;
    }

    /**
     * Returns the height of a pixel at a given distance from the eye point. This method assumes the model of a screen
     * composed of rectangular pixels, where pixel coordinates denote infinitely thin space between pixels. The units of
     * the returned size are in meters per pixel.
     * <p/>
     * The result of this method is undefined if the distance is negative.
     *
     * @param distance the distance from the eye point in meters
     *
     * @return the pixel height in meters per pixel
     */
    public double pixelSizeAtDistance(double distance) {
        double tanfovy_2 = Math.tan(Math.toRadians(this.fieldOfView * 0.5));
        double frustumHeight = 2 * distance * tanfovy_2;
        return frustumHeight / this.getHeight();
    }

    /**
     * Returns the minimum distance from the globe's surface necessary to make the globe's extents visible in this World
     * Window.
     *
     * @return the distance in meters needed to view the entire globe
     */
    public double distanceToViewGlobeExtents() {
        double sinfovy_2 = Math.sin(Math.toRadians(this.fieldOfView * 0.5));
        double radius = this.globe.getEquatorialRadius();
        return radius / sinfovy_2 - radius;
    }

    /**
     * Request that this World Window update its display. Prior changes to this World Window's Navigator, Globe and
     * Layers (including the contents of layers) are reflected on screen sometime after calling this method. May be
     * called from any thread.
     */
    public void requestRedraw() {
        // Forward calls to requestRedraw to the main thread.
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            this.mainThreadHandler.sendEmptyMessage(MSG_ID_REQUEST_REDRAW /*what*/);
            return;
        }

        // Suppress duplicate redraw requests, request that occur while the World Window is paused, and requests that
        // occur before we have an Android surface to draw to.
        if (!this.isWaitingForRedraw && !this.isPaused && !this.viewport.isEmpty()) {
            Choreographer.getInstance().postFrameCallback(this);
            this.isWaitingForRedraw = true;
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        // Skip frames when OpenGL thread has fallen two or more frames behind. Continue to request frame callbacks
        // until the OpenGL thread catches up.
        if (this.frameQueue.size() >= MAX_FRAME_QUEUE_SIZE) {
            Choreographer.getInstance().postFrameCallback(this);
            return;
        }

        // Allow subsequent redraw requests.
        this.isWaitingForRedraw = false;

        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        // The frame is recycled by the OpenGL thread.
        try {
            Frame frame = Frame.obtain(this.framePool);
            this.renderFrame(frame);
        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "WorldWindow", "doFrame",
                "Exception while rendering frame in Choreographer callback \'" + frameTimeNanos + "\'", e);
        }
    }

    /**
     * Requests that this World Window's OpenGL renderer display another frame on the OpenGL thread. Does not cause the
     * World Window's to display changes in its Navigator, Globe or Layers. Use {@link #requestRedraw()} instead.
     *
     * @deprecated Use {@link #requestRedraw} instead.
     */
    @Deprecated
    public void requestRender() {
        super.requestRender();
    }

    /**
     * Queues a runnable to be executed on this World Window's OpenGL thread. Must not be used to affect changes to this
     * World Window's state, including the Navigator, Globe and Layers. See the Android developers guide on <a
     * href="http://developer.android.com/training/multiple-threads/communicate-ui.html">Communicating with the UI
     * Thread</a> instead.
     *
     * @param r the runnable to execute
     *
     * @deprecated See <a href="http://developer.android.com/training/multiple-threads/communicate-ui.html">Communicating
     * with the UI Thread</a> instead.
     */
    @Deprecated
    @Override
    public void queueEvent(Runnable r) {
        super.queueEvent(r);
    }

    /**
     * Implements the GLSurfaceView.Renderer.onSurfaceChanged interface which is called on the GLThread when the surface
     * is created.
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Specify the default World Wind OpenGL state.
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Clear any cached OpenGL resources and state, which are now invalid.
        this.dc.contextLost();

        // Set the World Window's depth bits.
        int[] depthBits = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_DEPTH_BITS, depthBits, 0);
        this.mainThreadHandler.sendMessage(
            Message.obtain(this.mainThreadHandler, MSG_ID_SET_DEPTH_BITS /*msg.what*/, depthBits[0] /*msg.obj*/));

        // Clear the render resource cache on the main thread.
        this.mainThreadHandler.sendEmptyMessage(MSG_ID_CLEAR_CACHE /*msg.what*/);
    }

    /**
     * Implements the GLSurfaceView.Renderer.onSurfaceChanged interface which is called on the GLThread when the window
     * size changes.
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // Set the World Window's new viewport dimensions.
        Viewport newViewport = new Viewport(0, 0, width, height);
        this.mainThreadHandler.sendMessage(
            Message.obtain(this.mainThreadHandler, MSG_ID_SET_VIEWPORT /*msg.what*/, newViewport /*msg.obj*/));

        // Redraw this World Window with the new viewport.
        this.mainThreadHandler.sendEmptyMessage(MSG_ID_REQUEST_REDRAW /*msg.what*/);
    }

    /**
     * Implements the GLSurfaceView.Renderer.onDrawFrame interface which is called on the GLThread when rendering is
     * requested.
     */
    @Override
    public void onDrawFrame(GL10 unused) {
        // Remove and process pick the frame from the front of the pick queue, recycling it back into the pool. Continue
        // requesting frames on the OpenGL thread until the pick queue is empty. This is critical for correct operation.
        // All frames must be processed or threads waiting on a frame to finish may block indefinitely.
        Frame pickFrame = this.pickQueue.poll();
        if (pickFrame != null) {
            try {
                this.drawFrame(pickFrame);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "WorldWindow", "onDrawFrame",
                    "Exception while processing pick in OpenGL thread", e);
            } finally {
                pickFrame.signalDone();
                pickFrame.recycle();
                super.requestRender();
            }
        }

        // Remove and switch to to the frame at the front of the frame queue, recycling the previous frame back into the
        // pool. Continue requesting frames on the OpenGL thread until the frame queue is empty.
        Frame nextFrame = this.frameQueue.poll();
        if (nextFrame != null) {
            if (this.currentFrame != null) {
                this.currentFrame.recycle();
            }
            this.currentFrame = nextFrame;
            super.requestRender();
        }

        // Process and display the Drawables accumulated in the last frame taken from the front of the queue. This frame
        // may be drawn multiple times if the OpenGL thread executes more often than the World Window enqueues frames.
        try {
            if (this.currentFrame != null) {
                this.drawFrame(this.currentFrame);
            }
        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "WorldWindow", "onDrawFrame",
                "Exception while drawing frame in OpenGL thread", e);
        }
    }

    /**
     * Called immediately after the surface is first created, in which case the WorldWindow instance adds itself as a
     * listener to the {@link WorldWind#messageService()}. The WorldWind.messageService is a facility for broadcasting
     * global redraw requests to active WorldWindows.
     *
     * @param holder the SurfaceHolder whose surface is being created
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);

        // Set up to receive broadcast messages from World Wind's global message center.
        WorldWind.messageService().addListener(this);
    }

    /**
     * Called immediately before a surface is being destroyed, in which case the WorldWindow instance removes itself
     * from {@link WorldWind#messageService()}. Failure to do so may result in a memory leak this WorldWindow instance
     * when its owner is release/collected.
     *
     * @param holder the SurfaceHolder whose surface is being destroyed
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        // Release this WorldWindow reference from World Wind's global message service.
        WorldWind.messageService().removeListener(this);

        // Reset the World Window's internal state.
        this.reset();
    }

    /**
     * Called when the activity is paused. Calling this method will pause the rendering thread, cause any outstanding
     * pick operations to return an empty pick list, and prevent subsequent calls to pick and requestRedraw to return
     * without performing any action.
     */
    @Override
    public void onPause() {
        super.onPause();

        // Mark the World Window as paused.
        this.isPaused = true;

        // Reset the World Window's internal state. The OpenGL thread is paused, so frames in the queue will not be
        // processed. Clear the frame queue and recycle pending frames back into the frame pool. We also don't know
        // whether or not the render resources are valid, so we reset and let the GLSurfaceView establish the new
        // EGL context and viewport.
        this.reset();
    }

    /**
     * Called when the activity is resumed. Calling this method will resume the rendering thread and enable subsequent
     * calls to pick and requestRedraw to function normally.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Mark the World Window as not paused.
        this.isPaused = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Give the superclass first opportunity to handle the event.
        if (super.onTouchEvent(event)) {
            return true;
        }

        // Give the World Window's controller an opportunity to handle the event
        try {
            if (this.worldWindowController.onTouchEvent(event)) {
                this.navigatorEvents.onTouchEvent(event);
            }
        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "WorldWindow", "onTouchEvent",
                "Exception while handling touch event \'" + event + "\'", e);
        }

        // Always return true indicating that the event was handled, otherwise Android suppresses subsequent events.
        return true;
    }

    @Override
    public void onMessage(String name, Object sender, Map<Object, Object> userProperties) {
        if (name.equals(WorldWind.REQUEST_REDRAW)) {
            this.requestRedraw(); // may be called on any thread
        }
    }

    protected void renderFrame(Frame frame) {
        // Mark the beginning of a frame render.
        boolean pickMode = frame.pickMode;
        if (!pickMode) {
            this.frameMetrics.beginRendering(this.rc);
        }

        // Setup the render context according to the World Window's current state.
        this.rc.globe = this.globe;
        this.rc.terrainTessellator = this.tessellator;
        this.rc.layers = this.layers;
        this.rc.verticalExaggeration = this.verticalExaggeration;
        this.rc.fieldOfView = this.fieldOfView;
        this.rc.horizonDistance = this.globe.horizonDistance(this.navigator.getAltitude());
        this.rc.camera = this.navigator.getAsCamera(this.globe, this.rc.camera);
        this.rc.cameraPoint = this.globe.geographicToCartesian(this.rc.camera.latitude, this.rc.camera.longitude, this.rc.camera.altitude, this.rc.cameraPoint);
        this.rc.renderResourceCache = this.renderResourceCache;
        this.rc.renderResourceCache.setResources(this.getContext().getResources());
        this.rc.resources = this.getContext().getResources();

        // Configure the frame's Cartesian modelview matrix and eye coordinate projection matrix.
        this.computeViewingTransform(frame.projection, frame.modelview);
        frame.viewport.set(this.viewport);
        frame.infiniteProjection.setToInfiniteProjection(this.viewport.width, this.viewport.height, this.fieldOfView, 1.0);
        frame.infiniteProjection.multiplyByMatrix(frame.modelview);
        this.rc.viewport.set(frame.viewport);
        this.rc.projection.set(frame.projection);
        this.rc.modelview.set(frame.modelview);
        this.rc.modelviewProjection.setToMultiply(frame.projection, frame.modelview);
        if (pickMode) {
            this.rc.frustum.setToModelviewProjection(frame.projection, frame.modelview, frame.viewport, frame.pickViewport);
        } else {
            this.rc.frustum.setToModelviewProjection(frame.projection, frame.modelview, frame.viewport);
        }

        // Accumulate the Drawables in the frame's drawable queue and drawable terrain data structures.
        this.rc.drawableQueue = frame.drawableQueue;
        this.rc.drawableTerrain = frame.drawableTerrain;
        this.rc.pickedObjects = frame.pickedObjects;
        this.rc.pickViewport = frame.pickViewport;
        this.rc.pickPoint = frame.pickPoint;
        this.rc.pickRay = frame.pickRay;
        this.rc.pickMode = frame.pickMode;

        // Let the frame controller render the World Window's current state.
        this.frameController.renderFrame(this.rc);

        // Enqueue the frame for processing on the OpenGL thread as soon as possible and wake the OpenGL thread.
        if (pickMode) {
            this.pickQueue.offer(frame);
            super.requestRender();
        } else {
            this.frameQueue.offer(frame);
            super.requestRender();
        }

        // Propagate redraw requests submitted during rendering. The render context provides a layer of indirection that
        // insulates rendering code from establishing a dependency on a specific WorldWindow.
        if (!pickMode && this.rc.isRedrawRequested()) {
            this.requestRedraw();
        }

        // Notify navigator change listeners when the modelview matrix associated with the frame has changed.
        if (!pickMode) {
            this.navigatorEvents.onFrameRendered(this.rc);
        }

        // Mark the end of a frame render.
        if (!pickMode) {
            this.frameMetrics.endRendering(this.rc);
        }

        // Reset the render context's state in preparation for the next frame.
        this.rc.reset();
    }

    protected void drawFrame(Frame frame) {
        // Mark the beginning of a frame draw.
        boolean pickMode = frame.pickMode;
        if (!pickMode) {
            this.frameMetrics.beginDrawing(this.dc);
        }

        // Setup the draw context according to the frame's current state.
        this.dc.eyePoint = frame.modelview.extractEyePoint(this.dc.eyePoint);
        this.dc.viewport.set(frame.viewport);
        this.dc.projection.set(frame.projection);
        this.dc.modelview.set(frame.modelview);
        this.dc.modelviewProjection.setToMultiply(frame.projection, frame.modelview);
        this.dc.infiniteProjection.set(frame.infiniteProjection);
        this.dc.screenProjection.setToScreenProjection(frame.viewport.width, frame.viewport.height);

        // Process the drawables in the frame's drawable queue and drawable terrain data structures.
        this.dc.drawableQueue = frame.drawableQueue;
        this.dc.drawableTerrain = frame.drawableTerrain;
        this.dc.pickedObjects = frame.pickedObjects;
        this.dc.pickViewport = frame.pickViewport;
        this.dc.pickPoint = frame.pickPoint;
        this.dc.pickMode = frame.pickMode;

        // Let the frame controller draw the frame.
        this.frameController.drawFrame(this.dc);

        // Release resources evicted during the previous frame.
        this.renderResourceCache.releaseEvictedResources(this.dc);

        // Mark the end of a frame draw.
        if (!pickMode) {
            this.frameMetrics.endDrawing(this.dc);
        }

        // Reset the draw context's state in preparation for the next frame.
        this.dc.reset();
    }

    protected void clearFrameQueue() {
        // Clear the pick queue and recycle pending frames back into the frame pool. Mark the frame as done to ensure
        // that threads waiting for the frame to finish don't block indefinitely.
        Frame pickFrame;
        while ((pickFrame = this.pickQueue.poll()) != null) {
            pickFrame.signalDone();
            pickFrame.recycle();
        }

        // Clear the frame queue and recycle pending frames back into the frame pool.
        Frame frame;
        while ((frame = this.frameQueue.poll()) != null) {
            frame.recycle();
        }

        // Recycle the current frame back into the frame pool.
        if (this.currentFrame != null) {
            this.currentFrame.recycle();
            this.currentFrame = null;
        }
    }

    protected void computeViewingTransform(Matrix4 projection, Matrix4 modelview) {
        // Compute the clip plane distances. The near distance is set to a large value that does not clip the globe's
        // surface. The far distance is set to the smallest value that does not clip the atmosphere.
        // TODO adjust the clip plane distances based on the navigator's orientation - shorter distances when the
        // TODO horizon is not in view
        // TODO parameterize the object altitude for horizon distance
        double eyeAltitude = this.navigator.getAltitude();
        double eyeHorizon = this.globe.horizonDistance(eyeAltitude);
        double atmosphereHorizon = this.globe.horizonDistance(160000);
        double near = eyeAltitude * 0.5;
        double far = eyeHorizon + atmosphereHorizon;

        // Computes the near clip distance that provides a minimum resolution at the far clip plane, based on the OpenGL
        // context's depth buffer precision.
        if (this.depthBits != 0) {
            double maxDepthValue = (1 << this.depthBits) - 1;
            double farResolution = 10.0;
            double nearDistance = far / (maxDepthValue / (1 - farResolution / far) - maxDepthValue + 1);
            // Use the computed near distance only when it's less than our default distance.
            if (near > nearDistance) {
                near = nearDistance;
            }
        }

        // Compute a perspective projection matrix given the World Window's viewport, field of view, and clip distances.
        projection.setToPerspectiveProjection(this.viewport.width, this.viewport.height, this.fieldOfView, near, far);

        // Compute a Cartesian transform matrix from the Navigator.
        this.navigator.getAsViewingMatrix(this.globe, modelview);
    }
}
