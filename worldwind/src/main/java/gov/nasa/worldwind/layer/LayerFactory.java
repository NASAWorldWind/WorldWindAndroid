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
import gov.nasa.worldwind.ogc.wms.WmsLayer;
import gov.nasa.worldwind.ogc.wmts.OwsConstraint;
import gov.nasa.worldwind.ogc.wmts.OwsDcp;
import gov.nasa.worldwind.ogc.wmts.OwsHttpMethod;
import gov.nasa.worldwind.ogc.wmts.OwsOperation;
import gov.nasa.worldwind.ogc.wmts.OwsOperationsMetadata;
import gov.nasa.worldwind.ogc.wmts.OwsWgs84BoundingBox;
import gov.nasa.worldwind.ogc.wmts.WmtsCapabilities;
import gov.nasa.worldwind.ogc.wmts.WmtsLayer;
import gov.nasa.worldwind.ogc.wmts.WmtsResourceUrl;
import gov.nasa.worldwind.ogc.wmts.WmtsTileFactory;
import gov.nasa.worldwind.ogc.wmts.WmtsTileMatrix;
import gov.nasa.worldwind.ogc.wmts.WmtsTileMatrixSet;
import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.TileFactory;
import gov.nasa.worldwind.util.WWUtil;

public class LayerFactory {

    public interface Callback {

        void creationSucceeded(LayerFactory factory, Layer layer);

        void creationFailed(LayerFactory factory, Layer layer, Throwable ex);
    }

    public LayerFactory() {
    }

    protected Handler mainLoopHandler = new Handler(Looper.getMainLooper());

    protected List<String> compatibleImageFormats = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/gif", "image/bmp");

    protected List<String> compatibleCoordinateSystems = Arrays.asList("urn:ogc:def:crs:OGC:1.3:CRS84", "urn:ogc:def:crs:EPSG::4326", "http://www.opengis.net/def/crs/OGC/1.3/CRS84");

    protected static final int DEFAULT_WMS_NUM_LEVELS = 20;

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

    public Layer createFromWmsLayerCapabilities(WmsLayer layerCapabilities, Callback callback) {
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

    public Layer createFromWmsLayerCapabilities(List<WmsLayer> layerCapabilities, Callback callback) {
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

    public Layer createFromWmts(String serviceAddress, String layerIdentifier, Callback callback) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWms", "missingServiceAddress"));
        }

        if (layerIdentifier == null || layerIdentifier.isEmpty()) {
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

        WmtsAsyncTask task = new WmtsAsyncTask(this, serviceAddress, layerIdentifier, layer, callback);

        try {
            WorldWind.taskService().execute(task);
        } catch (RejectedExecutionException logged) { // singleton task service is full; this should never happen but we check anyway
            callback.creationFailed(this, layer, logged);
        }

        return layer;
    }

    public Layer createFromWmtsLayer(WmtsLayer wmtsLayer, Callback callback) {
        if (wmtsLayer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWmtsLayer", "missing layer"));
        }

        if (callback == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerFactory", "createFromWmtsLayer", "missingCallback"));
        }

        // Create a layer in which to asynchronously populate with renderables for the WMTS contents.
        RenderableLayer layer = new RenderableLayer();

        // Disable picking for the layer; terrain surface picking is performed automatically by WorldWindow.
        layer.setPickEnabled(false);

        this.createWmtsLayer(wmtsLayer, layer, callback);

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
        WmsCapabilities wmsCapabilities = this.retrieveWmsCapabilities(serviceAddress);
        List<WmsLayer> layerCapabilities = new ArrayList<>();
        for (String layerName : layerNames) {
            WmsLayer layerCaps = wmsCapabilities.getNamedLayer(layerName);
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

    protected void createFromWmtsAsync(String serviceAddress, String layerIdentifier, Layer layer, Callback callback) throws Exception {
        // Parse and read the WMTS Capabilities document at the provided service address
        WmtsCapabilities wmtsCapabilities = this.retrieveWmtsCapabilities(serviceAddress);

        WmtsLayer wmtsLayer = wmtsCapabilities.getLayer(layerIdentifier);
        if (wmtsLayer == null) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "createFromWmtsAsync", "The layer identifier specified was not found"));
        }

        this.createWmtsLayer(wmtsLayer, layer, callback);
    }

    protected void createWmsLayer(List<WmsLayer> layerCapabilities, Layer layer, Callback callback) {
        final Callback finalCallback = callback;
        final RenderableLayer finalLayer = (RenderableLayer) layer;

        try {
            WmsCapabilities wmsCapabilities = layerCapabilities.get(0).getCapability().getCapabilities();

            // Check if the server supports multiple layer request
            Integer layerLimit = wmsCapabilities.getService().getLayerLimit();
            if (layerLimit != null && layerLimit < layerCapabilities.size()) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "createFromWmsAsync", "The number of layers specified exceeds the services limit"));
            }

            WmsLayerConfig wmsLayerConfig = getLayerConfigFromWmsCapabilities(layerCapabilities);
            LevelSetConfig levelSetConfig = getLevelSetConfigFromWmsCapabilities(layerCapabilities);

            // Collect WMS Layer Titles to set the Layer Display Name
            StringBuilder sb = null;
            for (WmsLayer layerCapability : layerCapabilities) {
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

    protected void createWmtsLayer(WmtsLayer wmtsLayer, Layer layer, Callback callback) {

        final Callback finalCallback = callback;
        final RenderableLayer finalLayer = (RenderableLayer) layer;

        try {
            // Determine if there is a TileMatrixSet which matches our Coordinate System compatibility and tiling scheme
            List<String> compatibleTileMatrixSets = this.determineCoordSysCompatibleTileMatrixSets(wmtsLayer);
            if (compatibleTileMatrixSets.isEmpty()) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "createWmtsLayer", "Coordinate Systems Not Compatible"));
            }

            // Search the list of coordinate system compatible tile matrix sets for compatible tiling schemes
            CompatibleTileMatrixSet compatibleTileMatrixSet = this.determineTileSchemeCompatibleTileMatrixSet(wmtsLayer.getCapabilities(), compatibleTileMatrixSets);
            if (compatibleTileMatrixSet == null) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "createWmtsLayer", "Tile Schemes Not Compatible"));
            }

            TileFactory tileFactory = this.createWmtsTileFactory(wmtsLayer, compatibleTileMatrixSet);
            if (tileFactory == null) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "createWmtsLayer", "Unable to create TileFactory"));
            }

            LevelSet levelSet = this.createWmtsLevelSet(wmtsLayer, compatibleTileMatrixSet);

            final TiledSurfaceImage surfaceImage = new TiledSurfaceImage();

            surfaceImage.setTileFactory(tileFactory);
            surfaceImage.setLevelSet(levelSet);

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

    protected WmtsCapabilities retrieveWmtsCapabilities(String serviceAddress) throws Exception {
        InputStream inputStream = null;
        WmtsCapabilities wmtsCapabilities = null;
        try {
            // Build the appropriate request Uri given the provided service address
            Uri serviceUri = Uri.parse(serviceAddress).buildUpon()
                .appendQueryParameter("VERSION", "1.0.0")
                .appendQueryParameter("SERVICE", "WMTS")
                .appendQueryParameter("REQUEST", "GetCapabilities")
                .build();

            // Open the connection as an input stream
            URLConnection conn = new URL(serviceUri.toString()).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(30000);
            inputStream = new BufferedInputStream(conn.getInputStream());

            // Parse and read the input stream
            wmtsCapabilities = WmtsCapabilities.getCapabilities(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "retrieveWmsCapabilities", "Unable to open connection and read from service address " + e.toString()));
        } finally {
            WWUtil.closeSilently(inputStream);
        }

        return wmtsCapabilities;
    }

    protected WmsLayerConfig getLayerConfigFromWmsCapabilities(List<WmsLayer> wmsLayers) {
        // Construct the WmsTiledImage renderable from the WMS Capabilities properties
        WmsLayerConfig wmsLayerConfig = new WmsLayerConfig();
        WmsCapabilities wmsCapabilities = wmsLayers.get(0).getCapability().getCapabilities();
        String version = wmsCapabilities.getVersion();
        if (version.equals("1.3.0")) {
            wmsLayerConfig.wmsVersion = version;
        } else if (version.equals("1.1.1")) {
            wmsLayerConfig.wmsVersion = version;
        } else {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLayerConfigFromWmsCapabilities", "Version not compatible"));
        }

        String requestUrl = wmsCapabilities.getCapability().getRequest().getGetMap().getGetUrl();
        if (requestUrl == null) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getLayerConfigFromWmsCapabilities", "Unable to resolve GetMap URL"));
        } else {
            wmsLayerConfig.serviceAddress = requestUrl;
        }


        StringBuilder sb = null;
        Set<String> matchingCoordinateSystems = null;
        for (WmsLayer wmsLayer : wmsLayers) {
            String layerName = wmsLayer.getName();
            if (sb == null) {
                sb = new StringBuilder(layerName);
            } else {
                sb.append(",").append(layerName);
            }
            List<String> wmsLayerCoordinateSystems = wmsLayer.getReferenceSystems();
            if (matchingCoordinateSystems == null) {
                matchingCoordinateSystems = new HashSet<>();
                matchingCoordinateSystems.addAll(wmsLayerCoordinateSystems);
            } else {
                matchingCoordinateSystems.retainAll(wmsLayerCoordinateSystems);
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
        List<String> imageFormats = wmsCapabilities.getCapability().getRequest().getGetMap().getFormats();
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

    protected LevelSetConfig getLevelSetConfigFromWmsCapabilities(List<WmsLayer> layerCapabilities) {
        LevelSetConfig levelSetConfig = new LevelSetConfig();

        double minScaleDenominator = Double.MAX_VALUE;
        double minScaleHint = Double.MAX_VALUE;
        Sector sector = new Sector();
        for (WmsLayer layerCapability : layerCapabilities) {
            Double layerMinScaleDenominator = layerCapability.getMinScaleDenominator();
            if (layerMinScaleDenominator != null) {
                minScaleDenominator = Math.min(minScaleDenominator, layerMinScaleDenominator);
            }
            Double layerMinScaleHint = layerCapability.getScaleHint().getMin();
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
            levelSetConfig.numLevels = DEFAULT_WMS_NUM_LEVELS;
        }

        return levelSetConfig;
    }

    protected TileFactory createWmtsTileFactory(WmtsLayer wmtsLayer, CompatibleTileMatrixSet compatibleTileMatrixSet) {
        // First choice is a ResourceURL
        List<WmtsResourceUrl> resourceUrls = wmtsLayer.getResourceUrls();
        if (resourceUrls != null) {
            // Attempt to find a supported image format
            for (WmtsResourceUrl resourceUrl : resourceUrls) {
                if (this.compatibleImageFormats.contains(resourceUrl.getFormat())) {
                    String template = resourceUrl.getTemplate().replace("{TileMatrixSet}", compatibleTileMatrixSet.tileMatrixSetId);
                    return new WmtsTileFactory(template, compatibleTileMatrixSet.tileMatrices);
                }
            }
        }

        // Second choice is if the server supports KVP
        String baseUrl = this.determineKvpUrl(wmtsLayer);
        if (baseUrl != null) {
            String imageFormat = null;
            for (String compatibleImageFormat : this.compatibleImageFormats) {
                if (wmtsLayer.getFormats().contains(compatibleImageFormat)) {
                    imageFormat = compatibleImageFormat;
                    break;
                }
            }
            if (imageFormat == null) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "getWmtsTileFactory", "Image Formats Not Compatible"));
            }

            String styleIdentifier = wmtsLayer.getStyles().get(0).getIdentifier();
            if (styleIdentifier == null) {
                throw new RuntimeException(
                    Logger.makeMessage("LayerFactory", "getWmtsTileFactory", "No Style Identifier"));
            }
            String template = this.buildWmtsKvpTemplate(baseUrl, wmtsLayer.getIdentifier(), imageFormat, styleIdentifier, compatibleTileMatrixSet.tileMatrixSetId);
            return new WmtsTileFactory(template, compatibleTileMatrixSet.tileMatrices);
        } else {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "getWmtsTileFactory", "No KVP Get Support"));
        }
    }

    protected LevelSet createWmtsLevelSet(WmtsLayer wmtsLayer, CompatibleTileMatrixSet compatibleTileMatrixSet) {
        Sector boundingBox = null;
        OwsWgs84BoundingBox wgs84BoundingBox = wmtsLayer.getWgs84BoundingBox();
        if (wgs84BoundingBox == null) {
            Logger.logMessage(Logger.WARN, "LayerFactory", "createWmtsLevelSet", "WGS84BoundingBox not defined for layer: " + wmtsLayer.getIdentifier());
        } else {
            boundingBox = wgs84BoundingBox.getSector();
        }

        WmtsTileMatrixSet tileMatrixSet = wmtsLayer.getCapabilities().getTileMatrixSet(compatibleTileMatrixSet.tileMatrixSetId);
        if (tileMatrixSet == null) {
            throw new RuntimeException(
                Logger.makeMessage("LayerFactory", "createWmtsLevelSet", "Compatible TileMatrixSet not found for: " + compatibleTileMatrixSet));
        }
        int imageSize = tileMatrixSet.getTileMatrices().get(0).getTileHeight();

        return new LevelSet(boundingBox, 90.0, compatibleTileMatrixSet.tileMatrices.size(), imageSize, imageSize);
    }

    protected String buildWmtsKvpTemplate(String kvpServiceAddress, String layer, String format, String styleIdentifier, String tileMatrixSet) {
        StringBuilder urlTemplate = new StringBuilder(kvpServiceAddress);

        int index = urlTemplate.indexOf("?");
        if (index < 0) { // if service address contains no query delimiter
            urlTemplate.append("?"); // add one
        } else if (index != urlTemplate.length() - 1) { // else if query delimiter not at end of string
            index = urlTemplate.lastIndexOf("&");
            if (index != urlTemplate.length() - 1) {
                urlTemplate.append("&"); // add a parameter delimiter
            }
        }

        urlTemplate.append("SERVICE=WMTS&");
        urlTemplate.append("REQUEST=GetTile&");
        urlTemplate.append("VERSION=1.0.0&");
        urlTemplate.append("LAYER=").append(layer).append("&");
        urlTemplate.append("STYLE=").append(styleIdentifier).append("&");
        urlTemplate.append("FORMAT=").append(format).append("&");
        urlTemplate.append("TILEMATRIXSET=").append(tileMatrixSet).append("&");
        urlTemplate.append("TILEMATRIX=").append(WmtsTileFactory.TILEMATRIX_TEMPLATE).append("&");
        urlTemplate.append("TILEROW=").append(WmtsTileFactory.TILEROW_TEMPLATE).append("&");
        urlTemplate.append("TILECOL=").append(WmtsTileFactory.TILECOL_TEMPLATE);

        return urlTemplate.toString();
    }

    protected List<String> determineCoordSysCompatibleTileMatrixSets(WmtsLayer layer) {
        List<String> compatibleTileMatrixSets = new ArrayList<>();

        // Look for compatible coordinate system types
        List<WmtsTileMatrixSet> tileMatrixSets = layer.getLayerSupportedTileMatrixSets();
        for (WmtsTileMatrixSet tileMatrixSet : tileMatrixSets) {
            if (this.compatibleCoordinateSystems.contains(tileMatrixSet.getSupportedCrs())) {
                compatibleTileMatrixSets.add(tileMatrixSet.getIdentifier());
            }
        }

        return compatibleTileMatrixSets;
    }

    protected CompatibleTileMatrixSet determineTileSchemeCompatibleTileMatrixSet(WmtsCapabilities capabilities, List<String> tileMatrixSetIds) {
        CompatibleTileMatrixSet compatibleSet = new CompatibleTileMatrixSet();

        // Iterate through each provided tile matrix set
        for (String tileMatrixSetId : tileMatrixSetIds) {
            compatibleSet.tileMatrixSetId = tileMatrixSetId;
            compatibleSet.tileMatrices.clear();
            WmtsTileMatrixSet tileMatrixSet = capabilities.getTileMatrixSet(tileMatrixSetId);
            int previousHeight = 0;
            // Walk through the associated tile matrices and check for compatibility with WWA tiling scheme
            for (WmtsTileMatrix tileMatrix : tileMatrixSet.getTileMatrices()) {

                // Aspect and symmetry check of current matrix
                if ((2 * tileMatrix.getMatrixHeight()) != tileMatrix.getMatrixWidth()) {
                    continue;
                    // Quad division check
                } else if ((tileMatrix.getMatrixWidth() % 2 != 0) || (tileMatrix.getMatrixHeight() % 2 != 0)) {
                    continue;
                    // Square image check
                } else if (tileMatrix.getTileHeight() != tileMatrix.getTileWidth()) {
                    continue;
                    // Minimum row check
                } else if (tileMatrix.getMatrixHeight() < 2) {
                    continue;
                }

                // Parse top left corner values
                String[] topLeftCornerValue = tileMatrix.getTopLeftCorner().split("\\s+");
                if (topLeftCornerValue.length != 2) {
                    continue;
                }

                // Convert Values
                double[] topLeftCorner;
                try {
                    topLeftCorner = new double[]{
                        Double.parseDouble(topLeftCornerValue[0]),
                        Double.parseDouble(topLeftCornerValue[1])};
                } catch (Exception e) {
                    Logger.logMessage(Logger.WARN, "LayerFactory", "determineTileSchemeCompatibleTileMatrixSet",
                        "Unable to parse TopLeftCorner values");
                    continue;
                }

                // Check top left corner values
                if (tileMatrixSet.getSupportedCrs().equals("urn:ogc:def:crs:OGC:1.3:CRS84")
                    || tileMatrixSet.getSupportedCrs().equals("http://www.opengis.net/def/crs/OGC/1.3/CRS84")) {
                    if (Math.abs(topLeftCorner[0] + 180) > 1e-9) {
                        continue;
                    } else if (Math.abs(topLeftCorner[1] - 90) > 1e-9) {
                        continue;
                    }
                } else if (tileMatrixSet.getSupportedCrs().equals("urn:ogc:def:crs:EPSG::4326")) {
                    if (Math.abs(topLeftCorner[1] + 180) > 1e-9) {
                        continue;
                    } else if (Math.abs(topLeftCorner[0] - 90) > 1e-9) {
                        continue;
                    }
                } else {
                    // The provided list of tile matrix set ids should adhere to either EPGS:4326 or CRS84
                    continue;
                }

                // Ensure quad division behavior from previous tile matrix and add compatible tile matrix
                if (previousHeight == 0) {
                    previousHeight = tileMatrix.getMatrixHeight();
                    compatibleSet.tileMatrices.add(tileMatrix.getIdentifier());
                } else if ((2 * previousHeight) == tileMatrix.getMatrixHeight()) {
                    previousHeight = tileMatrix.getMatrixHeight();
                    compatibleSet.tileMatrices.add(tileMatrix.getIdentifier());
                }

            }

            // Return the first compatible tile matrix set
            if (compatibleSet.tileMatrices.size() > 2) {
                return compatibleSet;
            }
        }

        return null;
    }

    /**
     * Conducts a simple search through the {@link WmtsLayer}s distributed computing platform resources for a URL which
     * supports KVP queries to the WMTS. This method only looks at the first entry of every array of the layers 'GET'
     * retrieval methods.
     *
     * @param layer the {@link WmtsLayer} to search for KVP support
     *
     * @return the URL for the supported KVP or null if KVP or 'GET' method isn't provided by the layer
     */
    protected String determineKvpUrl(WmtsLayer layer) {
        WmtsCapabilities capabilities = layer.getCapabilities();
        OwsOperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();
        if (operationsMetadata == null) {
            return null;
        }
        OwsOperation getTileOperation = operationsMetadata.getGetTile();
        if (getTileOperation == null) {
            return null;
        }
        List<OwsDcp> dcp = getTileOperation.getDcps();
        if (dcp == null || dcp.isEmpty()) {
            return null;
        }

        List<OwsHttpMethod> getMethods = dcp.get(0).getGetMethods();
        if (getMethods == null || getMethods.isEmpty()) {
            return null;
        }

        List<OwsConstraint> constraints = getMethods.get(0).getConstraints();
        if (constraints == null || constraints.isEmpty()) {
            return null;
        }

        List<String> allowedValues = constraints.get(0).getAllowedValues();
        if (allowedValues != null && allowedValues.contains("KVP")) {
            return getMethods.get(0).getUrl();
        } else {
            return null;
        }
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

    protected static class WmtsAsyncTask implements Runnable {

        protected LayerFactory factory;

        protected String serviceAddress;

        protected String layerName;

        protected Layer layer;

        protected Callback callback;

        public WmtsAsyncTask(LayerFactory factory, String serviceAddress, String layerName, Layer layer, Callback callback) {
            this.factory = factory;
            this.serviceAddress = serviceAddress;
            this.layerName = layerName;
            this.layer = layer;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                this.factory.createFromWmtsAsync(this.serviceAddress, this.layerName, this.layer, this.callback);
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

    protected static class CompatibleTileMatrixSet {

        protected String tileMatrixSetId;

        protected List<String> tileMatrices = new ArrayList<>();

    }
}
