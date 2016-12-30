/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.ogc.wms.WmsCapabilities;
import gov.nasa.worldwind.ogc.wms.WmsLayerCapabilities;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;

public class LayerFactory {

    public interface Callback {

        void creationSucceeded(LayerFactory factory, Layer layer);

        void creationFailed(LayerFactory factory, Layer layer, Throwable ex);
    }

    protected Handler mainLoopHandler = new Handler(Looper.getMainLooper());

    public LayerFactory() {
    }

    public Layer createFromGeoPackage(String pathName, Callback callback) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createGeoPackageLayer", "missingPathName"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createGeoPackageLayer", "missingCallback"));
        }

        RenderableLayer layer = new RenderableLayer();
        layer.setPickEnabled(false);

        GeoPackageAsyncTask task = new GeoPackageAsyncTask(this, pathName, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged);
        }

        return layer;
    }

    public Layer createFromWms(String serviceAddress, String layerNames, Callback callback) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createWmsLayer", "missingServiceAddress"));
        }

        if (layerNames == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createWmsLayer", "missingLayerNames"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createWmsLayer", "missingCallback"));
        }

        RenderableLayer layer = new RenderableLayer();
        layer.setPickEnabled(false);

        WmsAsyncTask task = new WmsAsyncTask(this, serviceAddress, layerNames, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged);
        }

        return layer;
    }

    protected void createGeoPackageLayerAsync(String pathName, Layer layer, Callback callback) {
    }

    protected void createWmsLayerAsync(String serviceAddress, String layerNames, final Layer layer,
                                       final Callback callback) throws Exception {

        // Retrieve and parse the WMS capabilities at the specified service address, looking for the named layers
        // specified by the comma-delimited layerNames.
        Uri serviceUri = Uri.parse(serviceAddress).buildUpon()
            .appendQueryParameter("REQUEST", "GetCapabilities")
            .appendQueryParameter("SERVICE", "WMS")
            .appendQueryParameter("VERSION", "1.3.0")
            .build();

        URLConnection conn = new URL(serviceUri.toString()).openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(30000);

        InputStream inputStream = new BufferedInputStream(conn.getInputStream());

        // Parse and read capabilities document
        WmsCapabilities wmsCapabilities = WmsCapabilities.getCapabilities(inputStream);

        // Establish Version
        String version = wmsCapabilities.getVersion();

        // TODO work with multiple layer names
        WmsLayerCapabilities wmsLayerCapabilities = wmsCapabilities.getLayerByName(layerNames);
        if (wmsLayerCapabilities == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("LayerFactory", "createWmsLayerAsync", "Provided layer did not match available layers"));
        }

        String getCapabilitiesRequestUrl = wmsCapabilities.getRequestURL("GetMap", "Get");
        if (getCapabilitiesRequestUrl == null) {
            throw new IllegalStateException(
                Logger.makeMessage("LayerFactory", "createWmsLayerAsync", "Unable to resolve GetCapabilities URL"));
        }

        WmsTileFactory wmsTileFactory = new WmsTileFactory(
            getCapabilitiesRequestUrl,
            version,
            layerNames,
            ""
        );

        Set<String> coordinateSystems = wmsLayerCapabilities.getReferenceSystem();
        if (coordinateSystems.contains("CRS:84")) {
            wmsTileFactory.setCoordinateSystem("CRS:84");
        } else if (coordinateSystems.contains("EPSG:4326")) {
            wmsTileFactory.setCoordinateSystem("EPSG:4326");
        } else {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "createWmsLayerAsync", "Coordinate systems not compatible"));
        }

        Set<String> imageFormats = wmsCapabilities.getImageFormats();
        if (imageFormats.contains("image/png")) {
            wmsTileFactory.setImageFormat("image/png");
        } else {
            wmsTileFactory.setImageFormat(imageFormats.iterator().next());
        }

        Sector sector = wmsLayerCapabilities.getGeographicBoundingBox();
        if (sector == null) {
            sector = new Sector().setFullSphere();
        }

        int levels = Math.max(1, wmsLayerCapabilities.getNumberOfLevels(512));

        final TiledSurfaceImage tiledSurfaceImage = new TiledSurfaceImage();
        tiledSurfaceImage.setTileFactory(wmsTileFactory);
        LevelSet levelSet = new LevelSet(sector, 90.0, levels, 512, 512);
        tiledSurfaceImage.setLevelSet(levelSet);

        this.mainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                RenderableLayer renderableLayer = (RenderableLayer) layer;
                renderableLayer.addRenderable(tiledSurfaceImage);
                callback.creationSucceeded(LayerFactory.this, layer);
            }
        });
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
                this.callback.creationFailed(this.factory, this.layer, ex);
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
                this.callback.creationFailed(this.factory, this.layer, ex);
            }
        }
    }
}
