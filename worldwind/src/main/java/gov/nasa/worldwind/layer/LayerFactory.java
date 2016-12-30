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
import java.util.HashSet;
import java.util.List;
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
        Uri serviceUri = Uri.parse(serviceAddress).buildUpon()
            .appendQueryParameter("REQUEST", "GetCapabilities")
            .appendQueryParameter("SERVICE", "WMS")
            .appendQueryParameter("VERSION", "1.3.0")
            .build();

        try {
            URL serviceUrl = new URL(serviceUri.toString());
            InputStream inputStream = new BufferedInputStream(serviceUrl.openStream());
            WmsCapabilities wmsCapabilities = WmsCapabilities.getCapabilities(inputStream);

            // Establish Version
            String version = wmsCapabilities.getVersion();

            // Check that layers exist in this service
            String[] requestedLayers = layerNames.split(",");
            int cnt = 0;
            List<WmsLayerCapabilities> namedLayers = wmsCapabilities.getNamedLayers();
            Set<String> coordinateSystems = null;
            double minLat = 90.0;
            double minLon = 180.0;
            double maxLat = -90.0;
            double maxLon = -180.0;
            for (WmsLayerCapabilities namedLayer : namedLayers) {
                for (String requestedLayer : requestedLayers) {
                    if (requestedLayer.equals(namedLayer.getName())) {
                        cnt++;
                        if (coordinateSystems == null) {
                            coordinateSystems = new HashSet<>();
                            if (version.equals("1.3.0")) {
                                coordinateSystems.addAll(namedLayer.getCRS());
                            } else if (version.equals("1.1.1")) {
                                coordinateSystems.addAll(namedLayer.getSRS());
                            }
                        } else {
                            if (version.equals("1.3.0")) {
                                coordinateSystems.retainAll(namedLayer.getCRS());
                            } else if (version.equals("1.1.1")) {
                                coordinateSystems.retainAll(namedLayer.getSRS());
                            }
                        }
                        Sector sector = namedLayer.getGeographicBoundingBox();
                        if (sector != null) {
                            minLat = Math.min(minLat, sector.minLatitude());
                            minLon = Math.min(minLon, sector.minLongitude());
                            maxLat = Math.max(maxLat, sector.maxLatitude());
                            maxLon = Math.max(maxLon, sector.maxLongitude());
                        }
                        break;
                    }
                }
            }
            if (cnt != requestedLayers.length) {
                callback.layerFailed(this, layer, null);
                return;
            }

            WmsTileFactory wmsTileFactory = new WmsTileFactory(
                wmsCapabilities.getCapabilityInformation().getCapabilitiesInfo()
                    .getOnlineResouce(serviceUrl.getProtocol(), "Get").getHref(),
                version,
                layerNames,
                ""
            );

            if (coordinateSystems.contains("CRS:84")) {
                wmsTileFactory.setCoordinateSystem("CRS:84");
            } else if (coordinateSystems.contains("EPSG:4326")) {
                wmsTileFactory.setCoordinateSystem("EPSG:4326");
            } else {
                callback.layerFailed(this, layer, null);
            }

            Set<String> imageFormats = wmsCapabilities.getImageFormats();
            if (imageFormats.contains("image/png")) {
                wmsTileFactory.setImageFormat("image/png");
            } else {
                wmsTileFactory.setImageFormat(imageFormats.iterator().next());
            }

            TiledSurfaceImage tiledSurfaceImage = new TiledSurfaceImage();
            tiledSurfaceImage.setTileFactory(wmsTileFactory);
            LevelSet levelSet = new LevelSet(new Sector().setFullSphere(), 90.0, 16, 512, 512);
            tiledSurfaceImage.setLevelSet(levelSet);

            RenderableLayer renderableLayer = (RenderableLayer) layer;
            renderableLayer.addRenderable(tiledSurfaceImage);
        } catch (Exception e) {
            callback.layerFailed(this, layer, e);
            return;
        }

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
