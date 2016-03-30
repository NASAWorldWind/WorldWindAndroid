/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.gesture.GestureGroup;
import gov.nasa.worldwind.gesture.GestureRecognizer;
import gov.nasa.worldwind.globe.GeographicProjection;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.GlobeWgs84;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.BasicFrameController;
import gov.nasa.worldwind.render.BasicSurfaceTileRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameController;
import gov.nasa.worldwind.render.FrameStatistics;
import gov.nasa.worldwind.render.GpuObjectCache;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.MessageListener;

/**
 * Provides a World Wind window that implements a virtual globe inside of the Android view hierarchy. By default, World
 * Window is configured to display an ellipsoidal globe using the WGS 84 reference values.
 */
public class WorldWindow extends GLSurfaceView implements GLSurfaceView.Renderer, MessageListener {

    protected static final int DEFAULT_MEMORY_CLASS = 16;

    /**
     * Indicates the planet or celestial object displayed by this World Window.
     */
    protected Globe globe = new GlobeWgs84();

    protected LayerList layers = new LayerList();

    protected double verticalExaggeration = 1;

    protected Navigator navigator = new BasicNavigator();

    protected FrameController frameController = new BasicFrameController();

    protected WorldWindowController worldWindowController = new BasicWorldWindowController();

    protected GestureGroup gestureGroup = new GestureGroup();

    protected Rect viewport = new Rect();

    protected GpuObjectCache gpuObjectCache;

    protected SurfaceTileRenderer surfaceTileRenderer = new BasicSurfaceTileRenderer();

    protected DrawContext dc;

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
        // Initialize the world window's navigator and controller.
        Location initLocation = Location.fromTimeZone(TimeZone.getDefault());
        double initAltitude = this.distanceToViewGlobeExtents() * 1.1; // add to the minimum distance 10%
        this.navigator.setLatitude(initLocation.latitude);
        this.navigator.setLongitude(initLocation.longitude);
        this.navigator.setAltitude(initAltitude);
        this.worldWindowController.setWorldWindow(this);

        // Initialize the World Window's global caches. Use 50% of the approximate per-application memory class.
        ActivityManager am = (ActivityManager) this.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = (am != null) ? am.getMemoryClass() : DEFAULT_MEMORY_CLASS; // default to 16 MB class
        int gpuCacheSize = (memoryClass / 2) * 1024 * 1024;
        this.gpuObjectCache = new GpuObjectCache(gpuCacheSize);

        // Set up to render on demand to an OpenGL ES 2.x context
        // TODO Investigate and use the EGL chooser submitted by jgiovino
        this.dc = new DrawContext();
        this.setEGLConfigChooser(configChooser);
        this.setEGLContextClientVersion(2); // must be called before setRenderer
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // must be called after setRenderer

        // Set up to receive broadcast messages from World Wind's message center.
        WorldWind.messageService().addListener(this);
    }

    /**
     * Indicates the planet or celestial object displayed by this World Window. The Cartesian coordinate system is
     * specified by the globe's {@link GeographicProjection}. Defaults to {@link GlobeWgs84}.
     *
     * @return the globe displayed by this World Window
     */
    public Globe getGlobe() {
        return globe;
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
        return layers;
    }

    public void setLayers(LayerList layers) {
        if (layers == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setLayers", "missingList"));
        }

        this.layers = layers;
    }

    public double getVerticalExaggeration() {
        return verticalExaggeration;
    }

    public void setVerticalExaggeration(double verticalExaggeration) {
        if (verticalExaggeration <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setVerticalExaggeration", "invalidVerticalExaggeration"));
        }

        this.verticalExaggeration = verticalExaggeration;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public void setNavigator(Navigator navigator) {
        if (navigator == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setNavigator", "missingNavigator"));
        }

        this.navigator = navigator;
    }

    public FrameController getFrameController() {
        return frameController;
    }

    public void setFrameController(FrameController frameController) {
        if (frameController == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setFrameController", "missingController"));
        }

        this.frameController = frameController;
    }

    public FrameStatistics getFrameStatistics() {
        return this.frameController.getFrameStatistics();
    }

    public WorldWindowController getWorldWindowController() {
        return worldWindowController;
    }

    public void setWorldWindowController(WorldWindowController worldWindowController) {
        if (worldWindowController == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setWorldWindowController", "missingController"));
        }

        this.worldWindowController.setWorldWindow(null); // detach the old controller
        this.worldWindowController = worldWindowController; // switch to the new controller
        this.worldWindowController.setWorldWindow(this); // attach the new controller
    }

    public void addGestureRecognizer(GestureRecognizer recognizer) {
        if (recognizer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "addGestureRecognizer", "missingRecognizer"));
        }

        this.gestureGroup.addRecognizer(recognizer);
    }

    public void removeGestureRecognizer(GestureRecognizer recognizer) {
        if (recognizer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "removeGestureRecognizer", "missingRecognizer"));
        }

        this.gestureGroup.removeRecognizer(recognizer);
    }

    public List<GestureRecognizer> getGestureRecognizers() {
        return this.gestureGroup.getRecognizers();
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
     * @return the pixel height in meters
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
        this.gpuObjectCache.clear();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.viewport.set(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Setup the draw context according to the World Window's current state and draw the WorldWindow.
        this.prepareToDrawFrame();
        this.navigator.applyState(this.dc);
        this.frameController.drawFrame(this.dc);

        // Dispose OpenGL resources evicted during the previous frame. Performing this step here, rather than at
        // eviction time, avoids unexpected side effects like GPU programs being evicted while in use, which can occur
        // when this cache is too small and thrashes during a frame.
        this.gpuObjectCache.disposeEvictedObjects(this.dc);

        // Propagate render requests submitted during rendering to the WorldWindow. The draw context provides a layer of
        // indirection that insulates rendering code from establishing a dependency on a specific WorldWindow.
        if (this.dc.isRenderRequested()) {
            this.requestRender(); // inherited from GLSurfaceView
        }

        this.dc.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event) || this.gestureGroup.onTouchEvent(event);
    }

    protected void prepareToDrawFrame() {
        this.dc.setGlobe(this.globe);
        this.dc.setLayers(this.layers);
        this.dc.setVerticalExaggeration(this.verticalExaggeration);
        this.dc.setViewport(this.viewport);
        this.dc.setResources(this.getContext().getResources());
        this.dc.setGpuObjectCache(this.gpuObjectCache);
        this.dc.setSurfaceTileRenderer(this.surfaceTileRenderer);
    }

    @Override
    public void onMessage(String name, Object sender, Map<Object, Object> userProperties) {
        if (name.equals(WorldWind.REQUEST_RENDER)) {
            this.requestRender(); // inherited from GLSurfaceView; may be called on any thread
        }
    }
}
