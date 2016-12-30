/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.RejectedExecutionException;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.Logger;

public class LayerFactory {

    public interface Callback {

        void layerCreated(LayerFactory factory, Layer layer);

        void layerFailed(LayerFactory factory, Layer layer, Throwable ex);
    }

    protected Handler mainLoopHandler = new Handler(Looper.getMainLooper());

    public LayerFactory() {
    }

    public Layer createGeoPackageLayer(String pathName, Callback callback) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createGeoPackageLayer", "missingPathName"));
        }

        RenderableLayer layer = new RenderableLayer();
        layer.setPickEnabled(false);

        GeoPackageAsyncTask task = new GeoPackageAsyncTask(this, pathName, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.layerFailed(this, layer, logged);
        }

        return layer;
    }

    public Layer createWmsLayer(String serviceAddress, String layerNames, Callback callback) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createWmsLayer", "missingServiceAddress"));
        }

        if (layerNames == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createWmsLayer", "missingLayerNames"));
        }

        RenderableLayer layer = new RenderableLayer();
        layer.setPickEnabled(false);

        WmsAsyncTask task = new WmsAsyncTask(this, serviceAddress, layerNames, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.layerFailed(this, layer, logged);
        }

        return layer;
    }

    protected void createGeoPackageLayerAsync(String pathName, Layer layer, Callback callback) {
    }

    protected void createWmsLayerAsync(String serviceAddress, String layerNames, Layer layer, Callback callback) {
        // Retrieve and parse the WMS capabilities at the specified service address, looking for the named layers
        // specified by the comma-delimited layerNames.

        // Configure a tiled surface image appropriately for the named layers layers. The image's sector is the union of
        // all named layers, and the number of levels is the minimum necessary to capture the full resolution of all
        // named layers. Use a large default number of levels if the capabilities provides no scale hint for any of the
        // named layers.

        // Add the tiled surface image to the layer on the main thread and notify the optional callback. Request a
        // redraw to ensure that the image displays on all WorldWindows the layer is attached to.
    }

    protected static class GeoPackageAsyncTask implements Runnable {

        protected LayerFactory factory;

        protected String pathName;

        protected Layer layer;

        protected Callback callback;

        public GeoPackageAsyncTask(LayerFactory factory, String pathName, Layer layer, Callback callback) {
            this.factory = factory;
            this.pathName = pathName;
            this.layer = layer;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                this.factory.createGeoPackageLayerAsync(this.pathName, this.layer, this.callback);
            } catch (Throwable ex) {
                this.callback.layerFailed(this.factory, this.layer, ex);
            }
        }
    }

    protected static class WmsAsyncTask implements Runnable {

        protected LayerFactory factory;

        protected String serviceAddress;

        protected String layerNames;

        protected Layer layer;

        protected Callback callback;

        public WmsAsyncTask(LayerFactory factory, String serviceAddress, String layerNames, Layer layer, Callback callback) {
            this.factory = factory;
            this.serviceAddress = serviceAddress;
            this.layerNames = layerNames;
            this.layer = layer;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                this.factory.createWmsLayerAsync(this.serviceAddress, this.layerNames, this.layer, this.callback);
            } catch (Throwable ex) {
                this.callback.layerFailed(this.factory, this.layer, ex);
            }
        }
    }
}
