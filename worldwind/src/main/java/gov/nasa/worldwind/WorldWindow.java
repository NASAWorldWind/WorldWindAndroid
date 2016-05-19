/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
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
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.GeographicProjection;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.GlobeWgs84;
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

    protected static final int DEFAULT_MEMORY_CLASS = 16;

    protected static final int MAX_FRAME_QUEUE_SIZE = 2;

    /**
     * Indicates the planet or celestial object displayed by this World Window.
     */
    protected Globe globe = new GlobeWgs84();

    protected LayerList layers = new LayerList();

    protected double verticalExaggeration = 1;

    protected Navigator navigator = new BasicNavigator();

    protected NavigatorEventSupport navigatorEvents = new NavigatorEventSupport(this);

    protected FrameController frameController = new BasicFrameController();

    protected FrameMetrics frameMetrics = new FrameMetrics();

    protected WorldWindowController worldWindowController = new BasicWorldWindowController();

    protected RenderResourceCache renderResourceCache;

    protected RenderContext rc = new RenderContext();

    protected DrawContext dc = new DrawContext();

    protected Rect viewport = new Rect();

    protected Pool<Frame> framePool = new SynchronizedPool<>();

    protected ConcurrentLinkedQueue<Frame> frameQueue = new ConcurrentLinkedQueue<>();

    protected Frame currentFrame;

    protected boolean waitingForRedraw;

    protected Handler redrawHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            requestRedraw();
            return false;
        }
    });

    private Camera scratchCamera = new Camera();

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
        double initAltitude = this.distanceToViewGlobeExtents() * 1.1; // add to the minimum distance 10%
        this.navigator.setLatitude(initLocation.latitude);
        this.navigator.setLongitude(initLocation.longitude);
        this.navigator.setAltitude(initAltitude);

        // Initialize the World Window's controller.
        this.worldWindowController.setWorldWindow(this);

        // Initialize the World Window's global caches. Use 50% of the approximate per-application memory class.
        ActivityManager am = (ActivityManager) this.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = (am != null) ? am.getMemoryClass() : DEFAULT_MEMORY_CLASS; // default to 16 MB class
        int rrCacheSize = (memoryClass / 2) * 1024 * 1024;
        this.renderResourceCache = new RenderResourceCache(rrCacheSize);

        // Set up to render on demand to an OpenGL ES 2.x context
        // TODO Investigate and use the EGL chooser submitted by jgiovino
        this.setEGLConfigChooser(configChooser);
        this.setEGLContextClientVersion(2); // must be called before setRenderer
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // must be called after setRenderer

        // Log a message with some basic information about the world window's configuration.
        int rrCacheSizeMB = Math.round(rrCacheSize / 1024 / 1024);
        Logger.log(Logger.INFO, "World Window initialized {RenderResourceCache=" + rrCacheSizeMB + " MB}");
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
        this.redrawHandler.removeMessages(0 /*what*/);
        this.waitingForRedraw = false;
    }

    /**
     * Indicates the planet or celestial object displayed by this World Window. The Cartesian coordinate system is
     * specified by the globe's {@link GeographicProjection}. Defaults to {@link GlobeWgs84}.
     *
     * @return the globe displayed by this World Window
     */
    public Globe getGlobe() {
        return this.globe;
    }

    /**
     * Sets the planet or celestial object displayed by this World Window. The Cartesian coordinate system is specified
     * by the globe's {@link GeographicProjection}.
     *
     * @param globe the globe to display
     *
     * @throws IllegalArgumentException if the globe is null
     */
    public void setGlobe(Globe globe) {
        if (globe == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setGlobe", "missingGlobe"));
        }

        this.globe = globe;
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

        this.navigatorEvents.addNavigatorListener(listener);
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
     * @param result a pre-allocated {@link Point} in which to return the screen point
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

        // Transform the model point from model coordinates to eye coordinates then to clip coordinates. This inverts
        // the Z axis and stores the negative of the eye coordinate Z value in the W coordinate.
        double[] m = this.scratchProjection.m;
        double sx = m[0] * x + m[1] * y + m[2] * z + m[3];
        double sy = m[4] * x + m[5] * y + m[6] * z + m[7];
        double sz = m[8] * x + m[9] * y + m[10] * z + m[11];
        double sw = m[12] * x + m[13] * y + m[14] * z + m[15];

        if (sw == 0) {
            return false;
        }

        // Complete the conversion from model coordinates to clip coordinates by dividing by W. The resultant X, Y and
        // Z coordinates are in the range [-1, +1].
        sx /= sw;
        sy /= sw;
        sz /= sw;

        // Clip the point against the near and far clip planes. The result for points outside this range is undefined.
        if (sz < -1 || sz > 1) {
            return false;
        }

        // Convert the point from clip coordinate to the range [0, 1]. This enables the X and Y coordinates to be
        // converted to screen coordinates, and the Z coordinate to represent a depth value in the range [0, 1].
        sx = sx * 0.5 + 0.5;
        sy = sy * 0.5 + 0.5;

        // Convert from OpenGL screen coordinates to Android screen coordinates, both in the range [0, 1].
        sy = 1 - sy;

        // Convert the X and Y coordinates from the range [0, 1] to Android screen coordinates.
        sx = sx * this.getWidth();
        sy = sy * this.getHeight();

        // Store the Android screen coordinates in the result argument.
        result.x = (float) sx;
        result.y = (float) sy;

        return true;
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
     * @param result    a pre-allocated {@link Point} in which to return the screen point
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
     * Computes a Cartesian coordinate ray that passes through through a screen point. The X and Y components are
     * interpreted as coordinates in Android screen pixels relative to this View.
     *
     * @param x      the screen point's X coordinate in Android screen pixels
     * @param y      the screen point's Y coordinate in Android screen pixels
     * @param result a pre-allocated Line in which to return the computed ray
     *
     * @return the result set to the computed ray in Cartesian coordinates
     *
     * @throws IllegalArgumentException If the result is null
     */
    public boolean rayThroughScreenPoint(float x, float y, Line result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "rayThroughScreenPoint", "missingResult"));
        }

        // Compute the World Window's inverse modelview-projection matrix.
        this.computeViewingTransform(this.scratchProjection, this.scratchModelview);
        this.scratchProjection.multiplyByMatrix(this.scratchModelview).invert();

        // Convert from Android screen coordinates to coordinates in the range [0, 1]. This enables subsequent
        // conversion to clip coordinates.
        double sx = x / this.getWidth();
        double sy = y / this.getHeight();

        // Convert from Android screen coordinates to OpenGL screen coordinates, both in the range [0, 1].
        sy = 1 - sy;

        // Convert from coordinates in the range [0, 1] to clip coordinates in the range [-1, 1].
        sx = sx * 2 - 1;
        sy = sy * 2 - 1;

        // Transform the screen point from clip coordinates to model coordinates. This is a partial transformation
        // that factors out the contribution from the screen point's X and Y components. The contribution from the Z
        // component, which is both -1 and +1, is included next.
        double[] m = this.scratchProjection.m;
        double mx = (m[0] * sx) + (m[1] * sy) + m[3];
        double my = (m[4] * sx) + (m[5] * sy) + m[7];
        double mz = (m[8] * sx) + (m[9] * sy) + m[11];
        double mw = (m[12] * sx) + (m[13] * sy) + m[15];

        // Transform the screen point at the near clip plane (z = -1) to model coordinates.
        double nx = mx - m[2];
        double ny = my - m[6];
        double nz = mz - m[10];
        double nw = mw - m[14];

        // Transform the screen point at the far clip plane (z = +1) to model coordinates.
        double fx = mx + m[2];
        double fy = my + m[6];
        double fz = mz + m[10];
        double fw = mw + m[14];

        if (nw == 0 || fw == 0) {
            return false;
        }

        // Complete the conversion from near clip coordinates to model coordinates by dividing by the W component.
        nx = nx / nw;
        ny = ny / nw;
        nz = nz / nw;

        // Complete the conversion from far clip coordinates to model coordinates by dividing by the W component.
        fx = fx / fw;
        fy = fy / fw;
        fz = fz / fw;

        // Store the ray coordinates in the result argument.
        result.origin.set(nx, ny, nz);
        result.direction.set(fx - nx, fy - ny, fz - nz).normalize();

        return true;
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
        double fovyDegrees = this.navigator.getFieldOfView();
        double tanfovy_2 = Math.tan(Math.toRadians(fovyDegrees * 0.5));
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
        double fovyDegrees = this.navigator.getFieldOfView();
        double sinfovy_2 = Math.sin(Math.toRadians(fovyDegrees * 0.5));
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
            this.redrawHandler.sendEmptyMessage(0 /*what*/);
            return;
        }

        // Suppress duplicate redraw requests and requests that occur before we have an Android surface to draw to.
        if (!this.waitingForRedraw && !this.viewport.isEmpty()) {
            Choreographer.getInstance().postFrameCallback(this);
            this.waitingForRedraw = true;
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
        this.waitingForRedraw = false;

        // Obtain a frame from the pool and render the frame, accumulating Drawables to process in the OpenGL thread.
        Frame frame = Frame.obtain(this.framePool);
        this.beforeRenderFrame();
        this.renderFrame(frame);

        // Enqueue the frame for processing on the OpenGL thread as soon as possible, then wake the OpenGL thread.
        this.frameQueue.offer(frame);
        super.requestRender();

        // Perform any necessary actions after rendering the frame.
        this.afterRenderFrame();
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
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Clear any cached OpenGL resources and state, which are now invalid.
        this.dc.contextLost();
    }

    /**
     * Implements the GLSurfaceView.Renderer.onSurfaceChanged interface which is called on the GLThread when the window
     * size changes.
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // Redraw this World Window with the new viewport; may be called on any thread.
        this.requestRedraw();
    }

    /**
     * Implements the GLSurfaceView.Renderer.onDrawFrame interface which is called on the GLThread when rendering is
     * requested.
     */
    @Override
    public void onDrawFrame(GL10 unused) {
        // Remove the oldest frame from the front of the queue and recycle the previous frame back into the pool.
        Frame nextFrame = this.frameQueue.poll();
        if (nextFrame != null) {
            if (this.currentFrame != null) {
                this.currentFrame.recycle();
            }
            this.currentFrame = nextFrame;

            // Continue processing the frame queue on the OpenGL thread until the queue is empty. This has the result of
            // drawing the last frame twice, but improves overall concurrency between render and draw.
            super.requestRender();
        }

        // Process and display the Drawables accumulated during the render phase.
        if (this.currentFrame != null) {
            this.beforeDrawFrame();
            this.drawFrame(this.currentFrame);
            this.afterDrawFrame();
        }
    }

    /**
     * This is called immediately after the surface is first created, in which case the WorldWindow instance adds itself
     * as a listener to the {@link WorldWind#messageService()}. The WorldWind.messageService is a facility for
     * broadcasting global redraw requests to active WorldWindows.
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
     * This is called immediately before a surface is being destroyed, in which case the WorldWindow instance removes
     * itself from {@link WorldWind#messageService()}. Failure to do so may result in a memory leak this WorldWindow
     * instance when its owner is release/collected.
     *
     * @param holder the SurfaceHolder whose surface is being destroyed
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        // Release this WorldWindow reference from World Wind's global message service.
        WorldWind.messageService().removeListener(this);

        // Reset this WorldWindow's internal state.
        this.reset();
    }

    /**
     * This is called immediately after any structural changes (format or size) have been made to the surface, in which
     * case the WorldWindow redraws itself.
     *
     * @param holder the SurfaceHolder whose surface is has changed
     * @param format the new PixelFormat of the surface
     * @param width  the new width of the surface
     * @param height the new height of the surface
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);

        // Set the World Window's new viewport dimensions.
        this.viewport.set(0, 0, width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!super.onTouchEvent(event)) { // give the superclass first opportunity to handle the event
            if (this.worldWindowController.onTouchEvent(event)) { // let the controller try to handle the event
                this.navigatorEvents.onTouchEvent(event); // the controller handled the event; notify navigator events
            }
        }

        return true; // always return that the event was handled, otherwise Android suppresses subsequent events
    }

    @Override
    public void onMessage(String name, Object sender, Map<Object, Object> userProperties) {
        if (name.equals(WorldWind.REQUEST_REDRAW)) {
            this.requestRedraw(); // may be called on any thread
        }
    }

    protected void renderFrame(Frame frame) {
        // Setup the render context according to the World Window's current state.
        this.rc.globe = this.globe;
        this.rc.layers = this.layers;
        this.rc.verticalExaggeration = this.verticalExaggeration;
        this.rc.eyePosition.set(this.navigator.getLatitude(), this.navigator.getLongitude(), this.navigator.getAltitude());
        this.rc.heading = this.navigator.getHeading();
        this.rc.tilt = this.navigator.getTilt();
        this.rc.roll = this.navigator.getRoll();
        this.rc.fieldOfView = this.navigator.getFieldOfView();
        this.rc.horizonDistance = this.globe.horizonDistance(this.navigator.getAltitude());
        this.rc.viewport.set(this.viewport);
        this.computeViewingTransform(this.rc.projection, this.rc.modelview);
        this.rc.modelview.extractEyePoint(this.rc.eyePoint); // TODO compute eyePoint using Globe and Navigator
        this.rc.modelviewProjection.setToMultiply(this.rc.projection, this.rc.modelview);
        this.rc.frustum.setToProjectionMatrix(this.rc.projection);
        this.rc.frustum.transformByMatrix(this.scratchModelview.transposeMatrix(this.rc.modelview));
        this.rc.frustum.normalize();
        this.rc.renderResourceCache = this.renderResourceCache;
        this.rc.renderResourceCache.setResources(this.getContext().getResources());
        this.rc.resources = this.getContext().getResources();

        // Accumulate the Drawables in the frame's drawable queue and drawable terrain data structures.
        this.rc.drawableQueue = frame.drawableQueue;
        this.rc.drawableTerrain = frame.drawableTerrain;

        // Let the frame controller render the World Window's current state.
        this.frameController.renderFrame(this.rc);

        // Assign the frame's Cartesian modelview matrix and eye coordinate projection matrix.
        frame.viewport.set(this.viewport);
        frame.modelview.set(this.rc.modelview);
        frame.projection.set(this.rc.projection);
    }

    protected void beforeRenderFrame() {
        // Mark the beginning of a frame render.
        this.frameMetrics.beginRendering();
    }

    protected void afterRenderFrame() {
        // Propagate redraw requests submitted during rendering. The render context provides a layer of indirection that
        // insulates rendering code from establishing a dependency on a specific WorldWindow.
        if (this.rc.isRedrawRequested()) {
            this.requestRedraw();
        }

        // Notify navigator change listeners when the modelview matrix associated with the frame has changed.
        this.navigatorEvents.onFrameRendered(this.rc);

        // Reset the render context's state in preparation for the next frame.
        this.rc.reset();

        // Mark the end of a frame render.
        this.frameMetrics.endRendering();
    }

    protected void drawFrame(Frame frame) {
        // Setup the draw context according to the frame's current state.
        this.dc.modelview.set(frame.modelview);
        this.dc.modelview.extractEyePoint(this.dc.eyePoint);
        this.dc.projection.set(frame.projection);
        this.dc.modelviewProjection.setToMultiply(frame.projection, frame.modelview);
        this.dc.screenProjection.setToScreenProjection(frame.viewport.width(), frame.viewport.height());

        // Process the drawables in the frame's drawable queue and drawable terrain data structures.
        this.dc.drawableQueue = frame.drawableQueue;
        this.dc.drawableTerrain = frame.drawableTerrain;

        // Let the frame controller draw the frame's.
        this.frameController.drawFrame(this.dc);
    }

    protected void beforeDrawFrame() {
        // Mark the beginning of a frame draw.
        this.frameMetrics.beginDrawing();
    }

    protected void afterDrawFrame() {
        // Release resources evicted during the previous frame.
        this.renderResourceCache.releaseEvictedResources(this.dc);

        // Reset the draw context's state in preparation for the next frame.
        this.dc.reset();

        // Mark the end of a frame draw.
        this.frameMetrics.endDrawing();
    }

    protected void clearFrameQueue() {
        // Clear the frame queue and recycle pending frames back into the frame pool.
        Frame frame;
        while ((frame = this.frameQueue.poll()) != null) {
            frame.recycle();
        }
        this.frameQueue.clear();

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
        double near = this.navigator.getAltitude() * 0.75;
        double far = this.globe.horizonDistance(this.navigator.getAltitude(), 160000);

        // Compute a perspective projection matrix given the World Window's viewport, field of view, and clip distances.
        projection.setToPerspectiveProjection(this.viewport.width(), this.viewport.height(), this.navigator.getFieldOfView(), near, far);

        // Get the Navigator's properties as a Camera.
        this.navigator.getAsCamera(this.globe, this.scratchCamera);

        // Convert the Camera to a Cartesian viewing matrix, which is inverted.
        this.globe.cameraToCartesianTransform(this.scratchCamera, modelview).invertOrthonormal();
    }
}
