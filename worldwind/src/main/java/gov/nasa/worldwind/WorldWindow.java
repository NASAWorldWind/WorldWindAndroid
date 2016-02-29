/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.render.FrameController;
import gov.nasa.worldwind.render.FrameStatistics;

public class WorldWindow extends GLSurfaceView implements GLSurfaceView.Renderer {

    protected Navigator navigator;
    protected NavigatorController navigatorController;
    protected FrameController frameController;
    protected Globe globe;
    protected List<Layer> layers;
    protected double verticalExaggeration;

    /**
     * Constructs a WorldWindow associated with the specified application context. This is the
     * constructor to use when creating a WorldWindow from code.
     */
    public WorldWindow(Context context) {
        super(context);
        this.init();
    }

    /**
     * Constructs a WorldWindow associated with the specified application context and attributes
     * from an XML tag. This constructor is included to provide support for creating WorldWindow
     * from an Android XML layout file, and is not intended to be used directly.
     * <p/>
     * This is called when a view is being constructed from an XML file, supplying attributes that
     * were specified in the XML file. This version uses a default style of 0, so the only attribute
     * values applied are those in the Context's Theme and the given AttributeSet.
     */
    public WorldWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    /**
     * Prepares this WorldWindow for drawing and event handling.
     */
    protected void init() {
        // Set up to render on demand with OpenGL ES 2.x.
        this.setEGLContextClientVersion(2); // must be called before setRenderer
        this.setRenderer(this);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // must be called after setRenderer
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    public NavigatorController getNavigatorController() {
        return navigatorController;
    }

    public void setNavigatorController(NavigatorController navigatorController) {
        this.navigatorController = navigatorController;
    }

    public FrameController getFrameController() {
        return frameController;
    }

    public void setFrameController(FrameController frameController) {
        this.frameController = frameController;
    }

    public FrameStatistics getFrameStatistics() {
        return this.frameController.getFrameStatistics();
    }

    public Globe getGlobe() {
        return globe;
    }

    public void setGlobe(Globe globe) {
        this.globe = globe;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public double getVerticalExaggeration() {
        return verticalExaggeration;
    }

    public void setVerticalExaggeration(double verticalExaggeration) {
        this.verticalExaggeration = verticalExaggeration;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
    }
}
