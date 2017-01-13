/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.ogc.WmsTileFactory;
import gov.nasa.worldwind.ogc.gpkg.GeoPackage;
import gov.nasa.worldwind.ogc.gpkg.GpkgContent;
import gov.nasa.worldwind.ogc.gpkg.GpkgSpatialReferenceSystem;
import gov.nasa.worldwind.ogc.gpkg.GpkgTileFactory;
import gov.nasa.worldwind.ogc.gpkg.GpkgTileMatrixSet;
import gov.nasa.worldwind.ogc.gpkg.GpkgTileUserMetrics;
import gov.nasa.worldwind.ogc.wms.WmsCapabilities;
import gov.nasa.worldwind.ogc.wms.WmsLayerCapabilities;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class LayerFactory {

    public interface Callback {

        void creationSucceeded(LayerFactory factory, Layer layer);

        void creationFailed(LayerFactory factory, Layer layer, Throwable ex);
    }

    public LayerFactory() {
    }

    protected Handler mainLoopHandler = new Handler(Looper.getMainLooper());

    protected static final double DEFAULT_WMS_RADIANS_PER_PIXEL = 10.0 / WorldWind.WGS84_SEMI_MAJOR_AXIS;

    protected List<String> compatibleImageFormats = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/gif", "image/bmp");

    public Layer createFromGeoPackage(String pathName, Callback callback) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromGeoPackage", "missingPathName"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromGeoPackage", "missingCallback"));
        }

        // Create a layer in which to asynchronously populate with renderables for the GeoPackage contents.
        RenderableLayer layer = new RenderableLayer();

        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.setPickEnabled(false);

        GeoPackageAsyncTask task = new GeoPackageAsyncTask(this, pathName, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged);
        }

        return layer;
    }

    public Layer createFromWms(String serviceAddress, String layerName, Callback callback) {
        if (layerName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWms", "missingLayerNames"));
        }

        return createFromWms(serviceAddress, Collections.singletonList(layerName), callback);
    }

    public Layer createFromWms(String serviceAddress, List<String> layerNames, Callback callback) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWms", "missingServiceAddress"));
        }

        if (layerNames == null || layerNames.isEmpty()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWms", "missingLayerNames"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWms", "missingCallback"));
        }

        // Create a layer in which to asynchronously populate with renderables for the GeoPackage contents.
        RenderableLayer layer = new RenderableLayer();

        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.setPickEnabled(false);

        WmsAsyncTask task = new WmsAsyncTask(this, serviceAddress, layerNames, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged);
        }

        return layer;
    }

    public Layer createFromWmsLayerCapabilities(WmsLayerCapabilities layerCapabilities, Callback callback) {
        if (layerCapabilities == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWmsLayerCapabilities", "missing layers"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWmsLayerCapabilities", "missingCallback"));
        }

        return this.createFromWmsLayerCapabilities(Collections.singletonList(layerCapabilities), callback);
    }

    public Layer createFromWmsLayerCapabilities(List<WmsLayerCapabilities> layerCapabilities, Callback callback) {
        if (layerCapabilities == null || layerCapabilities.size() == 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWmsLayerCapabilities", "missing layers"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWmsLayerCapabilities", "missingCallback"));
        }

        // Create a layer in which to asynchronously populate with renderables for the GeoPackage contents.
        RenderableLayer layer = new RenderableLayer();

        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.setPickEnabled(false);

        this.createWmsLayer(layerCapabilities, layer, callback);

        return layer;
    }

    protected void createFromGeoPackageAsync(String pathName, Layer layer, Callback callback) {
        GeoPackage geoPackage = new GeoPackage(pathName);
        final RenderableLayer gpkgRenderables = new RenderableLayer();

        for (GpkgContent content : geoPackage.getContent()) {
            if (content.getDataType() == null || !content.getDataType().equalsIgnoreCase("tiles")) {
                Logger.logMessage(Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage content data_type: " + content.getDataType());
                continue;
            }

            GpkgSpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystem(content.getSrsId());
            if (srs == null || !srs.getOrganization().equalsIgnoreCase("EPSG") || srs.getOrganizationCoordSysId() != 4326) {
                Logger.logMessage(Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage spatial reference system: " + (srs == null ? "undefined" : srs.getSrsName()));
                continue;
            }

            GpkgTileMatrixSet tileMatrixSet = geoPackage.getTileMatrixSet(content.getTableName());
            if (tileMatrixSet == null || tileMatrixSet.getSrsId() != content.getSrsId()) {
                Logger.logMessage(Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage tile matrix set");
                continue;
            }

            GpkgTileUserMetrics tileMetrics = geoPackage.getTileUserMetrics(content.getTableName());
            if (tileMetrics == null) {
                Logger.logMessage(Logger.WARN, "LayerFactory", "createFromGeoPackageAsync",
                    "Unsupported GeoPackage tiles content");
                continue;
            }

            LevelSetConfig config = new LevelSetConfig();
            config.sector.set(content.getMinY(), content.getMinX(),
                content.getMaxY() - content.getMinY(), content.getMaxX() - content.getMinX());
            config.firstLevelDelta = 180;
            config.numLevels = tileMetrics.getMaxZoomLevel() + 1; // zero when there are no zoom levels, (0 = -1 + 1)
            config.tileWidth = 256;
            config.tileHeight = 256;

            TiledSurfaceImage surfaceImage = new TiledSurfaceImage();
            surfaceImage.setLevelSet(new LevelSet(config));
            surfaceImage.setTileFactory(new GpkgTileFactory(content));
            gpkgRenderables.addRenderable(surfaceImage);
        }

        if (gpkgRenderables.count() == 0) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "createFromGeoPackageAsync", "Unsupported GeoPackage contents"));
        }

        final RenderableLayer finalLayer = (RenderableLayer) layer;
        final Callback finalCallback = callback;

        // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
        // that the image displays on all WorldWindows the layer may be attached to.
        this.mainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                finalLayer.addAllRenderables(gpkgRenderables);
                finalCallback.creationSucceeded(LayerFactory.this, finalLayer);
                WorldWind.requestRedraw();
            }
        });
    }

    protected void createFromWmsAsync(String serviceAddress, List<String> layerNames, Layer layer, Callback callback) throws Exception {
        // Parse and read the WMS Capabilities document at the provided service address
        WmsCapabilities wmsCapabilities = retrieveWmsCapabilities(serviceAddress);
        List<WmsLayerCapabilities> layerCapabilities = new ArrayList<>();
        for (String layerName : layerNames) {
            WmsLayerCapabilities layerCaps = wmsCapabilities.getLayerByName(layerName);
            if (layerCaps != null) {
                layerCapabilities.add(layerCaps);
            }
        }

        if (layerCapabilities.size() == 0) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "createFromWmsAsync", "Provided layers did not match available layers"));
        }

        this.createWmsLayer(layerCapabilities, layer, callback);
    }

    protected void createWmsLayer(List<WmsLayerCapabilities> layerCapabilities, Layer layer, Callback callback) {
        final Callback finalCallback = callback;
        final RenderableLayer finalLayer = (RenderableLayer) layer;

        try {
            WmsCapabilities wmsCapabilities = layerCapabilities.get(0).getServiceCapabilities();

            // Check if the server supports multiple layer request
            Integer layerLimit = wmsCapabilities.getServiceInformation().getLayerLimit();
            if (layerLimit != null && layerLimit < layerCapabilities.size()) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "createFromWmsAsync", "The number of layers specified exceeds the services limit"));
            }

            WmsLayerConfig wmsLayerConfig = getLayerConfigFromWmsCapabilities(layerCapabilities);
            LevelSetConfig levelSetConfig = getLevelSetConfigFromWmsCapabilities(layerCapabilities);

            // Collect WMS Layer Titles to set the Layer Display Name
            StringBuilder sb = null;
            for (WmsLayerCapabilities layerCapability : layerCapabilities) {
                if (sb == null) {
                    sb = new StringBuilder(layerCapability.getTitle());
                } else {
                    sb.append(",").append(layerCapability.getTitle());
                }
            }
            layer.setDisplayName(sb.toString());

            final TiledSurfaceImage surfaceImage = new TiledSurfaceImage();

            surfaceImage.setTileFactory(new WmsTileFactory(wmsLayerConfig));
            surfaceImage.setLevelSet(new LevelSet(levelSetConfig));

            // Add the tiled surface image to the layer on the main thread and notify the caller. Request a redraw to ensure
            // that the image displays on all WorldWindows the layer may be attached to.
            this.mainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    finalLayer.addRenderable(surfaceImage);
                    finalCallback.creationSucceeded(LayerFactory.this, finalLayer);
                    WorldWind.requestRedraw();
                }
            });
        } catch (final Throwable ex) {
            this.mainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    finalCallback.creationFailed(LayerFactory.this, finalLayer, ex);
                }
            });
        }
    }

    protected WmsCapabilities retrieveWmsCapabilities(String serviceAddress) throws Exception {
        InputStream inputStream = null;
        WmsCapabilities wmsCapabilities = null;
        try {
            // Build the appropriate request Uri given the provided service address
            Uri serviceUri = Uri.parse(serviceAddress).buildUpon()
                .appendQueryParameter("VERSION", "1.3.0")
                .appendQueryParameter("SERVICE", "WMS")
                .appendQueryParameter("REQUEST", "GetCapabilities")
                .build();

            // Open the connection as an input stream
            URLConnection conn = new URL(serviceUri.toString()).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(30000);
            inputStream = new BufferedInputStream(conn.getInputStream());

            // Parse and read the input stream
            wmsCapabilities = WmsCapabilities.getCapabilities(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "retrieveWmsCapabilities", "Unable to open connection and read from service address"));
        } finally {
            WWUtil.closeSilently(inputStream);
        }

        return wmsCapabilities;
    }

    protected WmsLayerConfig getLayerConfigFromWmsCapabilities(List<WmsLayerCapabilities> layerCapabilities) {
        // Construct the WmsTiledImage renderable from the WMS Capabilities properties
        WmsLayerConfig wmsLayerConfig = new WmsLayerConfig();
        WmsCapabilities wmsCapabilities = layerCapabilities.get(0).getServiceCapabilities();
        String version = wmsCapabilities.getVersion();
        if (version.equals("1.3.0")) {
            wmsLayerConfig.wmsVersion = version;
        } else if (version.equals("1.1.1")) {
            wmsLayerConfig.wmsVersion = version;
        } else {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLayerConfigFromWmsCapabilities", "Version not compatible"));
        }

        String requestUrl = wmsCapabilities.getRequestURL("GetMap", "Get");
        if (requestUrl == null) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLayerConfigFromWmsCapabilities", "Unable to resolve GetMap URL"));
        } else {
            wmsLayerConfig.serviceAddress = requestUrl;
        }


        StringBuilder sb = null;
        Set<String> matchingCoordinateSystems = null;
        for (WmsLayerCapabilities layerCapability : layerCapabilities) {
            String layerName = layerCapability.getName();
            if (sb == null) {
                sb = new StringBuilder(layerName);
            } else {
                sb.append(",").append(layerName);
            }
            Set<String> layerCoordinateSystems = layerCapability.getReferenceSystem();
            if (matchingCoordinateSystems == null) {
                matchingCoordinateSystems = new HashSet<>();
                matchingCoordinateSystems.addAll(layerCoordinateSystems);
            } else {
                matchingCoordinateSystems.retainAll(layerCoordinateSystems);
            }
        }

        wmsLayerConfig.layerNames = sb.toString();

        if (matchingCoordinateSystems.contains("EPSG:4326")) {
            wmsLayerConfig.coordinateSystem = "EPSG:4326";
        } else if (matchingCoordinateSystems.contains("CRS:84")) {
            wmsLayerConfig.coordinateSystem = "CRS:84";
        } else {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLayerConfigFromWmsCapabilities", "Coordinate systems not compatible"));
        }

        // Negotiate Image Formats
        Set<String> imageFormats = wmsCapabilities.getImageFormats();
        for (String compatibleImageFormat : this.compatibleImageFormats) {
            if (imageFormats.contains(compatibleImageFormat)) {
                wmsLayerConfig.imageFormat = compatibleImageFormat;
                break;
            }
        }

        if (wmsLayerConfig.imageFormat == null) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLayerConfigFromWmsCapabilities", "Image Formats Not Compatible"));
        }

        return wmsLayerConfig;
    }

    protected LevelSetConfig getLevelSetConfigFromWmsCapabilities(List<WmsLayerCapabilities> layerCapabilities) {
        LevelSetConfig levelSetConfig = new LevelSetConfig();

        double minScaleDenominator = Double.MAX_VALUE;
        double minScaleHint = Double.MAX_VALUE;
        Sector sector = new Sector();
        for (WmsLayerCapabilities layerCapability : layerCapabilities) {
            Double layerMinScaleDenominator = layerCapability.getMinScaleDenominator();
            if (layerMinScaleDenominator != null) {
                minScaleDenominator = Math.min(minScaleDenominator, layerMinScaleDenominator);
            }
            Double layerMinScaleHint = layerCapability.getMinScaleHint();
            if (layerMinScaleHint != null) {
                minScaleHint = Math.min(minScaleHint, layerMinScaleHint);
            }
            Sector layerSector = layerCapability.getGeographicBoundingBox();
            if (layerSector != null) {
                sector.union(layerSector);
            }
        }

        if (!sector.isEmpty()) {
            levelSetConfig.sector.set(sector);
        } else {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLevelSetConfigFromWmsCapabilities", "Geographic Bounding Box Not Defined"));
        }

        if (minScaleDenominator != Double.MAX_VALUE) {
            // WMS 1.3.0 scale configuration. Based on the WMS 1.3.0 spec page 28. The hard coded value 0.00028 is
            // detailed in the spec as the common pixel size of 0.28mm x 0.28mm. Configures the maximum level not to
            // exceed the specified min scale denominator.
            double minMetersPerPixel = minScaleDenominator * 0.00028;
            double minRadiansPerPixel = minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;
            levelSetConfig.numLevels = levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel);
        } else if (minScaleHint != Double.MAX_VALUE) {
            // WMS 1.1.1 scale configuration, where ScaleHint indicates approximate resolution in ground distance
            // meters. Configures the maximum level not to exceed the specified min scale denominator.
            double minMetersPerPixel = minScaleHint;
            double minRadiansPerPixel = minMetersPerPixel / WorldWind.WGS84_SEMI_MAJOR_AXIS;
            levelSetConfig.numLevels = levelSetConfig.numLevelsForMinResolution(minRadiansPerPixel);
        } else {
            // Default scale configuration when no minimum scale denominator or scale hint is provided.
            double defaultRadiansPerPixel = DEFAULT_WMS_RADIANS_PER_PIXEL;
            levelSetConfig.numLevels = levelSetConfig.numLevelsForResolution(defaultRadiansPerPixel);
        }

        return levelSetConfig;
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
                this.factory.createFromGeoPackageAsync(this.pathName, this.layer, this.callback);
            } catch (final Throwable ex) {
                this.factory.mainLoopHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.creationFailed(factory, layer, ex);
                    }
                });
            }
        }
    }

    protected static class WmsAsyncTask implements Runnable {

        protected LayerFactory factory;

        protected String serviceAddress;

        protected List<String> layerNames;

        protected Layer layer;

        protected Callback callback;

        public WmsAsyncTask(LayerFactory factory, String serviceAddress, List<String> layerNames, Layer layer, Callback callback) {
            this.factory = factory;
            this.serviceAddress = serviceAddress;
            this.layerNames = layerNames;
            this.layer = layer;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                this.factory.createFromWmsAsync(this.serviceAddress, this.layerNames, this.layer, this.callback);
            } catch (final Throwable ex) {
                this.factory.mainLoopHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.creationFailed(factory, layer, ex);
                    }
                });
            }
        }
    }
}
