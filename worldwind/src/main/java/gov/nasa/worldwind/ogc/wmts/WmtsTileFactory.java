/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class WmtsTileFactory implements TileFactory {

    protected List<String> compatibleImageFormats = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/gif", "image/bmp");

    protected int[] rowOffsetForQuads;

    protected List<String> matrixSetLevelIdentifiers = new ArrayList<>();

    protected String urlTemplate;

    protected String baseUrl;

    protected String tileMatrixSetIdentifier;

    protected String tileLayer;

    protected int tileHeight;

    protected int tileWidth;

    protected String imageFormat;

    protected WmtsTileFactory() {
    }

    public static TileFactory generateFactory(WmtsLayer layer) {
        WmtsTileFactory tileFactory = new WmtsTileFactory();
        tileFactory.tileLayer = layer.getIdentifier();
        if (tileFactory.determineTileMatrixSet(layer)) {
            tileFactory.determineTileUrl(layer);
            return tileFactory;
        } else {
            return null;
        }
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {

        String tileUrl = null;

        int originalRow = row;
        double modRow = Math.pow(2, level.levelNumber + 1);
        row = (((int) modRow) - 1) - row;

        if (this.urlTemplate != null) {
            String url = this.urlTemplate;
            url = url.replace("{TileMatrixSet}", this.tileMatrixSetIdentifier);
            url = url.replace("{TileMatrix}", this.matrixSetLevelIdentifiers.get(level.levelNumber));
            int tileRow;
            if (rowOffsetForQuads != null) {
                tileRow = row + this.rowOffsetForQuads[level.levelNumber];
            } else {
                tileRow = row;
            }
            url = url.replace("{TileRow}", String.format("%d", tileRow));
            tileUrl = url.replace("{TileCol}", String.format("%d", column));
        } else if (this.baseUrl != null) {
            StringBuilder sb = new StringBuilder(baseUrl);
            //sb.append("?");
            sb.append("REQUEST=GetTile&");
            sb.append("SERVICE=WMTS&");
            sb.append("VERSION=1.0.0&");
            sb.append("LAYER=").append(this.tileLayer).append("&");
            sb.append("STYLE=default&");
            sb.append("FORMAT=").append(this.imageFormat).append("&");
            sb.append("TileMatrixSet=").append(this.tileMatrixSetIdentifier).append("&");
            sb.append("TileMatrix=").append(this.matrixSetLevelIdentifiers.get(level.levelNumber)).append("&");
            sb.append("TileRow=").append(String.format("%d", row)).append("&");
            sb.append("TileCol=").append(String.format("%d", column));

            tileUrl = sb.toString();
        }

        if (tileUrl == null) {
            return null;
        }

        ImageTile tile = new ImageTile(sector, level, originalRow, column);

        tile.setImageSource(ImageSource.fromUrl(tileUrl));

        return tile;
    }

    public int getNumberOfSupportedLevels() {
        return this.matrixSetLevelIdentifiers.size();
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    protected boolean determineTileMatrixSet(WmtsLayer layer) {
        // For this initial implementation, the tile set must be a quad based pyramid
        // Check if it is the GoogleCRS84Quad as we have a set solution for that case
//        if (layer.getTileMatrixSetIds().equals("GoogleCRS84Quad")) {
//            this.tileMatrixSetIdentifier = "GoogleCRS84Quad";
//            WmtsTileMatrixSet tileMatrixSet = layer.getCapabilities().getTileMatrixSet(this.tileMatrixSetIdentifier);
//            this.rowOffsetForQuads = new int[tileMatrixSet.getTileMatrices().size() - 2];
//            for (int i = 0; i < rowOffsetForQuads.length; i++) {
//                WmtsTileMatrix tileMatrix = tileMatrixSet.getTileMatrices().get(i + 1);
//                // Get the appropriate identifier
//                this.matrixSetLevelIdentifiers.add(tileMatrix.getIdentifier());
//                // Set the row offset due to the quad nature of the tile set
//                double offset = Math.pow(2, i);
//                this.rowOffsetForQuads[i] = (int) offset;
//                this.tileHeight = tileMatrix.getTileHeight();
//                this.tileWidth = tileMatrix.getTileWidth();
//            }
//
//            return true;
//        }

        // Now need to check if any of the other TileMatrixSets have a similar pyramid to WWA
        for (String tileMatrixSetIdentifier : layer.getTileMatrixSetIds()) {
            WmtsTileMatrixSet tileMatrixSet = layer.getCapabilities().getTileMatrixSet(tileMatrixSetIdentifier);
            if (this.determineTileMatrixSet(tileMatrixSet)) {
                return true;
            }
        }

        return false;
    }

    protected boolean determineTileMatrixSet(WmtsTileMatrixSet tileMatrixSet) {
        // Check coordinate systems
        if (tileMatrixSet.getSupportedCrs().equals("urn:ogc:def:crs:OGC:1.3:CRS84")
            || tileMatrixSet.getSupportedCrs().equals("urn:ogc:def:crs:EPSG::4326")) {

            List<WmtsTileMatrix> tileMatrices = tileMatrixSet.getTileMatrices();
            for (WmtsTileMatrix tileMatrix : tileMatrices) {
                if ((2 * tileMatrix.getMatrixHeight()) == tileMatrix.getMatrixWidth()
                    && (tileMatrix.getMatrixWidth() % 2 == 0)
                    && tileMatrix.getTileWidth() == tileMatrix.getTileHeight()) {

                    if (this.matrixSetLevelIdentifiers.size() > 0 || tileMatrix.getMatrixHeight() == 2) {
                        this.matrixSetLevelIdentifiers.add(tileMatrix.getIdentifier());
                        this.tileHeight = tileMatrix.getTileHeight();
                        this.tileWidth = tileMatrix.getTileWidth();
                    }
                }
            }
            if (this.matrixSetLevelIdentifiers.size() > 0) {
                this.tileMatrixSetIdentifier = tileMatrixSet.getIdentifier();
                return true;
            }
        } else {
            return false;
        }

        return false;
    }

    protected void determineTileUrl(WmtsLayer layer) {

        List<WmtsResourceUrl> urls = layer.getResourceUrls();

        for (String compatibleImageFormat : this.compatibleImageFormats) {
            for (WmtsResourceUrl url : urls) {
                if (url.getFormat().equals(compatibleImageFormat)) {
                    this.urlTemplate = url.getTemplate();
                    this.imageFormat = url.getFormat();
                    return;
                }
            }
        }

        // Add KVP support if necessary
        if (this.urlTemplate == null) {
            Boolean kvpSupported = layer.getCapabilities().getOperationsMetadata().getGetTile().getDcp().isGetMethodSupportKV();
            if (kvpSupported != null && kvpSupported == true) {
                this.baseUrl = layer.getCapabilities().getOperationsMetadata().getGetTile().getDcp().getGetHref();
                for (String compatibleFormat : this.compatibleImageFormats) {
                    if (layer.getFormats().contains(compatibleFormat)) {
                        this.imageFormat = compatibleFormat;
                        return;
                    }
                }
            }
        }
    }
}
