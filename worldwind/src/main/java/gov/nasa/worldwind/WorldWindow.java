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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.GeographicProjection;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.globe.GlobeWgs84;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.BasicFrameController;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameController;
import gov.nasa.worldwind.render.FrameStatistics;
import gov.nasa.worldwind.render.GpuObjectCache;
import gov.nasa.worldwind.util.Logger;

/**
 * Provides a World Wind window that implements a virtual globe inside of the Android view hierarchy. By default, World
 * Window is configured to display an ellipsoidal globe using the WGS 84 reference values.
 */
public class WorldWindow extends GLSurfaceView implements GLSurfaceView.Renderer {

    /**
     * Indicates the planet or celestial object displayed by this World Window.
     */
    protected Globe globe = new GlobeWgs84();

    protected LayerList layers = new LayerList();

    protected double verticalExaggeration = 1;

    protected Navigator navigator = new BasicNavigator();

    protected NavigatorController navigatorController = new BasicNavigatorController();

    protected FrameController frameController = new BasicFrameController();

    protected Rect viewport = new Rect();

    protected GpuObjectCache gpuObjectCache;

    protected DrawContext dc;

    /**
     * Constructs a WorldWindow associated with the specified application context. This is the constructor to use when
     * creating a WorldWindow from code.
     */
    public WorldWindow(Context context) {
        super(context);
        this.init();
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
        this.init();
    }

    /**
     * Prepares this WorldWindow for drawing and event handling.
     */
    protected void init() {
        // Initialize the world window's navigator and controller.
        this.navigator.setPosition(Position.fromDegrees(0, 0, 3e7)); // TODO adaptive initial position
        this.navigatorController.setWorldWindow(this);

        // Initialize the World Window's global caches. // TODO can we use ActivityManager.getLargeMemoryClass?
        ActivityManager am = (ActivityManager) this.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        int totalBytes = (am != null) ? (am.getMemoryClass() * 1024 * 1024) : (64 * 1024 * 1024);
        int gpuBytes = totalBytes / 2;
        this.gpuObjectCache = new GpuObjectCache(gpuBytes, (int) (gpuBytes * 0.75));

        // Set up to render on demand to an OpenGL ES 2.x context
        this.dc = new DrawContext(this.getContext());
        this.setEGLContextClientVersion(2); // must be called before setRenderer
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // must be called after setRenderer
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

    public NavigatorController getNavigatorController() {
        return navigatorController;
    }

    public void setNavigatorController(NavigatorController navigatorController) {
        if (navigatorController == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WorldWindow", "setNavigatorController", "missingController"));
        }

        this.navigatorController.setWorldWindow(null); // detach the old controller
        this.navigatorController = navigatorController; // switch to the new controller
        this.navigatorController.setWorldWindow(this); // attach the new controller
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

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Specify the default World Wind OpenGL state.
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
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
        // Setup the draw context according to the World Window's current state.
        this.prepareToDrawFrame();
        this.navigator.applyState(this.dc);

        // Draw the WorldWindow's current state.
        this.navigatorController.windowWillDraw(this.dc);
        this.frameController.drawFrame(this.dc);
        this.navigatorController.windowDidDraw(this.dc);

        // Propagate render requests submitted during rendering to the WorldWindow. The draw context provides a layer of
        // indirection that insulates rendering code from establishing a dependency on a specific WorldWindow.
        if (this.dc.isRenderRequested()) {
            this.requestRender();
        }
    }

    protected void prepareToDrawFrame() {
        this.dc.reset();
        this.dc.setGlobe(this.globe);
        this.dc.setLayers(this.layers);
        this.dc.setVerticalExaggeration(this.verticalExaggeration);
        this.dc.setViewport(this.viewport);
        this.dc.setGpuObjectCache(this.gpuObjectCache);
    }
}
