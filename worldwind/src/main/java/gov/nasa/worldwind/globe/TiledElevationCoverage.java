/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.LongSparseArray;
import android.util.SparseIntArray;

import java.net.SocketTimeoutException;
import java.nio.ShortBuffer;
import java.util.Locale;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.TileMatrix;
import gov.nasa.worldwind.geom.TileMatrixSet;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.WWMath;

public class TiledElevationCoverage extends AbstractElevationCoverage implements Retriever.Callback<ImageSource, Void, ShortBuffer> {

    public interface TileFactory {

        ImageSource createTileSource(TileMatrix tileMatrix, int row, int column);
    }

    protected TileMatrixSet tileMatrixSet = new TileMatrixSet(); // empty tile matrix set

    protected TileFactory tileFactory;

    protected LruMemoryCache<Long, ImageSource> coverageSource;

    protected LruMemoryCache<ImageSource, short[]> coverageCache;

    protected ElevationRetriever coverageRetriever;

    protected Handler coverageHandler;

    protected boolean enableRetrieval;

    protected static final int GET_HEIGHT_LIMIT_SAMPLES = 8;

    public TiledElevationCoverage() {
        this.coverageSource = new LruMemoryCache<>(200);
        this.coverageCache = new LruMemoryCache<>(1024 * 1024 * 8);
        this.coverageRetriever = new ElevationRetriever(4);
        this.coverageHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });

        Logger.log(Logger.INFO, String.format(Locale.US, "Coverage cache initialized  %,.0f KB",
            this.coverageCache.getCapacity() / 1024.0));
    }

    public TileMatrixSet getTileMatrixSet() {
        return this.tileMatrixSet;
    }

    public void setTileMatrixSet(TileMatrixSet tileMatrixSet) {
        if (tileMatrixSet == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledSurfaceImage", "setTileMatrixSet", "missingTileMatrixSet"));
        }

        this.tileMatrixSet = tileMatrixSet;
        this.invalidateTiles();
    }

    public TileFactory getTileFactory() {
        return this.tileFactory;
    }

    public void setTileFactory(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
        this.invalidateTiles();
    }

    protected boolean isEnableRetrieval() {
        return this.enableRetrieval;
    }

    protected void setEnableRetrieval(boolean enable) {
        this.enableRetrieval = enable;
    }

    protected void invalidateTiles() {
        this.coverageSource.clear();
        this.coverageCache.clear();
    }

    @Override
    protected void doGetHeightGrid(Sector gridSector, int gridWidth, int gridHeight, float[] result) {
        if (!this.tileMatrixSet.sector.intersects(gridSector)) {
            return; // no coverage in the specified sector
        }

        double targetPixelSpan = gridSector.deltaLatitude() / gridHeight;
        int targetIdx = this.tileMatrixSet.indexOfMatrixNearest(targetPixelSpan);
        TileBlock tileBlock = new TileBlock();

        for (int idx = targetIdx; idx >= 0; idx--) {

            this.setEnableRetrieval(idx == targetIdx || idx == 0); // enable retrieval of the target matrix and the first matrix

            TileMatrix tileMatrix = this.tileMatrixSet.matrix(idx);
            if (this.fetchTileBlock(gridSector, gridWidth, gridHeight, tileMatrix, tileBlock)) {
                this.readHeightGrid(gridSector, gridWidth, gridHeight, tileBlock, result);
                return;
            }
        }
    }

    @Override
    protected void doGetHeightLimits(Sector sector, float[] result) {
        if (!this.tileMatrixSet.sector.intersects(sector)) {
            return; // no coverage in the specified sector
        }

        double targetPixelSpan = sector.deltaLatitude() / GET_HEIGHT_LIMIT_SAMPLES;
        int targetIdx = this.tileMatrixSet.indexOfMatrixNearest(targetPixelSpan);
        TileBlock tileBlock = new TileBlock();

        for (int idx = targetIdx; idx >= 0; idx--) {

            this.setEnableRetrieval(idx == targetIdx || idx == 0); // enable retrieval of the target matrix and the first matrix

            TileMatrix tileMatrix = this.tileMatrixSet.matrix(idx);
            if (this.fetchTileBlock(sector, tileMatrix, tileBlock)) {
                this.scanHeightLimits(sector, tileBlock, result);
                return;
            }
        }
    }

    protected boolean fetchTileBlock(Sector gridSector, int gridWidth, int gridHeight, TileMatrix tileMatrix, TileBlock result) {
        int tileWidth = tileMatrix.tileWidth;
        int tileHeight = tileMatrix.tileHeight;
        int rasterWidth = tileMatrix.matrixWidth * tileWidth;
        int rasterHeight = tileMatrix.matrixHeight * tileHeight;
        double matrixMinLat = tileMatrix.sector.minLatitude();
        double matrixMaxLat = tileMatrix.sector.maxLatitude();
        double matrixMinLon = tileMatrix.sector.minLongitude();
        double matrixMaxLon = tileMatrix.sector.maxLongitude();
        double matrixDeltaLat = tileMatrix.sector.deltaLatitude();
        double matrixDeltaLon = tileMatrix.sector.deltaLongitude();
        double sMin = 1.0 / (2.0 * rasterWidth);
        double sMax = 1.0 - sMin;
        double tMin = 1.0 / (2.0 * rasterHeight);
        double tMax = 1.0 - tMin;

        result.tileMatrix = tileMatrix;
        result.clear();

        double lon = gridSector.minLongitude();
        double deltaLon = gridSector.deltaLongitude() / (gridWidth - 1);
        for (int uidx = 0; uidx < gridWidth; uidx++, lon += deltaLon) {
            if (uidx == gridWidth - 1) {
                lon = gridSector.maxLongitude(); // explicitly set the last lon to the max longitude to ensure alignment
            }

            if (matrixMinLon <= lon && lon <= matrixMaxLon) {
                double s = (lon - matrixMinLon) / matrixDeltaLon;
                double u;
                int i0, i1;
                if (tileMatrix.sector.isFullSphere()) {
                    u = rasterWidth * WWMath.fract(s); // wrap the horizontal coordinate
                    i0 = WWMath.mod((int) Math.floor(u - 0.5), rasterWidth);
                    i1 = WWMath.mod((i0 + 1), rasterWidth);
                } else {
                    u = rasterWidth * WWMath.clamp(s, sMin, sMax); // clamp the horizontal coordinate
                    i0 = (int) WWMath.clamp((int) Math.floor(u - 0.5), 0, rasterWidth - 1);
                    i1 = (int) WWMath.clamp((i0 + 1), 0, rasterWidth - 1);
                }
                int col0 = i0 / tileWidth;
                int col1 = i1 / tileWidth;
                result.cols.append(col0, 0);
                result.cols.append(col1, 0);
            }
        }

        double lat = gridSector.minLatitude();
        double deltaLat = gridSector.deltaLatitude() / (gridHeight - 1);
        for (int vidx = 0; vidx < gridHeight; vidx++, lat += deltaLat) {
            if (vidx == gridHeight - 1) {
                lat = gridSector.maxLatitude(); // explicitly set the last lat to the max latitude to ensure alignment
            }

            if (matrixMinLat <= lat && lat <= matrixMaxLat) {
                double t = (matrixMaxLat - lat) / matrixDeltaLat;
                double v = rasterHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the raster edge
                int j0 = (int) WWMath.clamp(Math.floor(v - 0.5), 0, rasterHeight - 1);
                int j1 = (int) WWMath.clamp(j0 + 1, 0, rasterHeight - 1);
                int row0 = j0 / tileHeight;
                int row1 = j1 / tileHeight;
                result.rows.append(row0, 0);
                result.rows.append(row1, 0);
            }
        }

        for (int ridx = 0, rlen = result.rows.size(); ridx < rlen; ridx++) {
            for (int cidx = 0, clen = result.cols.size(); cidx < clen; cidx++) {
                int row = result.rows.keyAt(ridx);
                int col = result.cols.keyAt(cidx);
                short[] tileArray = this.fetchTileArray(tileMatrix, row, col);
                if (tileArray != null) {
                    result.putTileArray(row, col, tileArray);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    protected boolean fetchTileBlock(Sector sector, TileMatrix tileMatrix, TileBlock result) {
        int tileWidth = tileMatrix.tileWidth;
        int tileHeight = tileMatrix.tileHeight;
        int rasterWidth = tileMatrix.matrixWidth * tileWidth;
        int rasterHeight = tileMatrix.matrixHeight * tileHeight;
        double matrixMaxLat = tileMatrix.sector.maxLatitude();
        double matrixMinLon = tileMatrix.sector.minLongitude();
        double matrixDeltaLat = tileMatrix.sector.deltaLatitude();
        double matrixDeltaLon = tileMatrix.sector.deltaLongitude();

        Sector intersection = new Sector(tileMatrix.sector);
        intersection.intersect(sector);

        double sMin = (intersection.minLongitude() - matrixMinLon) / matrixDeltaLon;
        double sMax = (intersection.maxLongitude() - matrixMinLon) / matrixDeltaLon;
        double uMin = Math.floor(rasterWidth * sMin);
        double uMax = Math.ceil(rasterWidth * sMax);
        int iMin = (int) WWMath.clamp(uMin, 0, rasterWidth - 1);
        int iMax = (int) WWMath.clamp(uMax, 0, rasterWidth - 1);
        int colMin = iMin / tileWidth;
        int colMax = iMax / tileWidth;

        double tMin = (matrixMaxLat - intersection.maxLatitude()) / matrixDeltaLat;
        double tMax = (matrixMaxLat - intersection.minLatitude()) / matrixDeltaLat;
        double vMin = Math.floor(rasterHeight * tMin);
        double vMax = Math.ceil(rasterHeight * tMax);
        int jMin = (int) WWMath.clamp(vMin, 0, rasterHeight - 1);
        int jMax = (int) WWMath.clamp(vMax, 0, rasterHeight - 1);
        int rowMin = jMin / tileHeight;
        int rowMax = jMax / tileHeight;

        result.tileMatrix = tileMatrix;
        result.clear();

        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                short[] tileArray = this.fetchTileArray(tileMatrix, row, col);
                if (tileArray != null) {
                    result.rows.put(row, 0);
                    result.cols.put(col, 0);
                    result.putTileArray(row, col, tileArray);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    protected short[] fetchTileArray(TileMatrix tileMatrix, int row, int column) {
        long key = tileKey(tileMatrix, row, column);
        ImageSource tileSource = this.coverageSource.get(key);

        if (tileSource == null) {
            tileSource = this.tileFactory.createTileSource(tileMatrix, row, column);
            this.coverageSource.put(key, tileSource, 1);
        }

        short[] tileArray = this.coverageCache.get(tileSource);
        if (tileArray == null && this.isEnableRetrieval()) {
            this.coverageRetriever.retrieve(tileSource, null, this);
        }

        return tileArray;
    }

    protected static long tileKey(TileMatrix tileMatrix, int row, int column) {
        long lord = (tileMatrix.ordinal & 0xFFL); // 8 bits
        long lrow = (row & 0xFFFFFFFL); // 28 bits
        long lcol = (column & 0xFFFFFFFL); // 28 bits
        long key = (lord << 56) | (lrow << 28) | lcol;

        return key;
    }

    protected void readHeightGrid(Sector gridSector, int gridWidth, int gridHeight, TileBlock tileBlock, float[] result) {
        int tileWidth = tileBlock.tileMatrix.tileWidth;
        int tileHeight = tileBlock.tileMatrix.tileHeight;
        int rasterWidth = tileBlock.tileMatrix.matrixWidth * tileWidth;
        int rasterHeight = tileBlock.tileMatrix.matrixHeight * tileHeight;
        double matrixMinLat = tileBlock.tileMatrix.sector.minLatitude();
        double matrixMaxLat = tileBlock.tileMatrix.sector.maxLatitude();
        double matrixMinLon = tileBlock.tileMatrix.sector.minLongitude();
        double matrixMaxLon = tileBlock.tileMatrix.sector.maxLongitude();
        double matrixDeltaLat = tileBlock.tileMatrix.sector.deltaLatitude();
        double matrixDeltaLon = tileBlock.tileMatrix.sector.deltaLongitude();
        double sMin = 1.0 / (2.0 * rasterWidth);
        double sMax = 1.0 - sMin;
        double tMin = 1.0 / (2.0 * rasterHeight);
        double tMax = 1.0 - tMin;
        int ridx = 0;

        double lat = gridSector.minLatitude();
        double deltaLat = gridSector.deltaLatitude() / (gridHeight - 1);
        for (int hidx = 0; hidx < gridHeight; hidx++, lat += deltaLat) {
            if (hidx == gridHeight - 1) {
                lat = gridSector.maxLatitude(); // explicitly set the last lat to the max latitude to ensure alignment
            }

            double t = (matrixMaxLat - lat) / matrixDeltaLat;
            double v = rasterHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the raster edge
            float b = (float) WWMath.fract(v - 0.5);
            int j0 = (int) WWMath.clamp(Math.floor(v - 0.5), 0, rasterHeight - 1);
            int j1 = (int) WWMath.clamp(j0 + 1, 0, rasterHeight - 1);
            int row0 = j0 / tileHeight;
            int row1 = j1 / tileHeight;

            double lon = gridSector.minLongitude();
            double deltaLon = gridSector.deltaLongitude() / (gridWidth - 1);
            for (int widx = 0; widx < gridWidth; widx++, lon += deltaLon) {
                if (widx == gridWidth - 1) {
                    lon = gridSector.maxLongitude(); // explicitly set the last lon to the max longitude to ensure alignment
                }

                double s = (lon - matrixMinLon) / matrixDeltaLon;
                double u;
                int i0, i1;
                if (tileBlock.tileMatrix.sector.isFullSphere()) {
                    u = rasterWidth * WWMath.fract(s); // wrap the horizontal coordinate
                    i0 = WWMath.mod((int) Math.floor(u - 0.5), rasterWidth);
                    i1 = WWMath.mod((i0 + 1), rasterWidth);
                } else {
                    u = rasterWidth * WWMath.clamp(s, sMin, sMax); // clamp the horizontal coordinate
                    i0 = (int) WWMath.clamp((int) Math.floor(u - 0.5), 0, rasterWidth - 1);
                    i1 = (int) WWMath.clamp((i0 + 1), 0, rasterWidth - 1);
                }
                float a = (float) WWMath.fract(u - 0.5);
                int col0 = i0 / tileWidth;
                int col1 = i1 / tileWidth;

                if (matrixMinLat <= lat && lat <= matrixMaxLat &&
                    matrixMinLon <= lon && lon <= matrixMaxLon) {

                    short i0j0 = tileBlock.readTexel(row0, col0, i0 % tileWidth, j0 % tileHeight);
                    short i1j0 = tileBlock.readTexel(row0, col1, i1 % tileWidth, j0 % tileHeight);
                    short i0j1 = tileBlock.readTexel(row1, col0, i0 % tileWidth, j1 % tileHeight);
                    short i1j1 = tileBlock.readTexel(row1, col1, i1 % tileWidth, j1 % tileHeight);

                    result[ridx] = (1 - a) * (1 - b) * i0j0 +
                        a * (1 - b) * i1j0 +
                        (1 - a) * b * i0j1 +
                        a * b * i1j1;
                }

                ridx++;
            }
        }
    }

    protected void scanHeightLimits(Sector sector, TileBlock tileBlock, float[] result) {
        int tileWidth = tileBlock.tileMatrix.tileWidth;
        int tileHeight = tileBlock.tileMatrix.tileHeight;
        int rasterWidth = tileBlock.tileMatrix.matrixWidth * tileWidth;
        int rasterHeight = tileBlock.tileMatrix.matrixHeight * tileHeight;
        double matrixMaxLat = tileBlock.tileMatrix.sector.maxLatitude();
        double matrixMinLon = tileBlock.tileMatrix.sector.minLongitude();
        double matrixDeltaLat = tileBlock.tileMatrix.sector.deltaLatitude();
        double matrixDeltaLon = tileBlock.tileMatrix.sector.deltaLongitude();

        Sector intersection = new Sector(tileBlock.tileMatrix.sector);
        intersection.intersect(sector);

        double sMin = (intersection.minLongitude() - matrixMinLon) / matrixDeltaLon;
        double sMax = (intersection.maxLongitude() - matrixMinLon) / matrixDeltaLon;
        double uMin = Math.floor(rasterWidth * sMin);
        double uMax = Math.ceil(rasterWidth * sMax);
        int iMin = (int) WWMath.clamp(uMin, 0, rasterWidth - 1);
        int iMax = (int) WWMath.clamp(uMax, 0, rasterWidth - 1);

        double tMin = (matrixMaxLat - intersection.maxLatitude()) / matrixDeltaLat;
        double tMax = (matrixMaxLat - intersection.minLatitude()) / matrixDeltaLat;
        double vMin = Math.floor(rasterHeight * tMin);
        double vMax = Math.ceil(rasterHeight * tMax);
        int jMin = (int) WWMath.clamp(vMin, 0, rasterHeight - 1);
        int jMax = (int) WWMath.clamp(vMax, 0, rasterHeight - 1);

        for (int ridx = 0, rlen = tileBlock.rows.size(); ridx < rlen; ridx++) {
            int row = tileBlock.rows.keyAt(ridx);
            int rowjMin = row * tileHeight;
            int rowjMax = rowjMin + tileHeight - 1;
            int j0 = (int) WWMath.clamp(jMin, rowjMin, rowjMax) % tileHeight;
            int j1 = (int) WWMath.clamp(jMax, rowjMin, rowjMax) % tileHeight;

            for (int cidx = 0, clen = tileBlock.cols.size(); cidx < clen; cidx++) {
                int col = tileBlock.cols.keyAt(cidx);
                int coliMin = col * tileWidth;
                int coliMax = coliMin + tileWidth - 1;
                int i0 = (int) WWMath.clamp(iMin, coliMin, coliMax) % tileWidth;
                int i1 = (int) WWMath.clamp(iMax, coliMin, coliMax) % tileWidth;

                short[] tileArray = tileBlock.getTileArray(row, col);
                // TODO how often do we read all of tileArray?

                for (int j = j0; j <= j1; j++) {
                    for (int i = i0; i <= i1; i++) {

                        int pos = i + j * tileWidth;
                        short texel = tileArray[pos];

                        if (result[0] > texel) {
                            result[0] = texel;
                        }
                        if (result[1] < texel) {
                            result[1] = texel;
                        }
                    }
                }
            }
        }
    }

    public void retrievalSucceeded(Retriever retriever, ImageSource key, Void unused, ShortBuffer value) {
        final ImageSource finalKey = key;
        final short[] finalArray = new short[value.remaining()];
        value.get(finalArray);

        this.coverageHandler.post(new Runnable() {
            @Override
            public void run() {
                coverageCache.put(finalKey, finalArray, finalArray.length * 2);
                updateTimestamp();
                WorldWind.requestRedraw();
            }
        });

        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Coverage retrieval succeeded \'" + key + "\'");
        }
    }

    @Override
    public void retrievalFailed(Retriever retriever, ImageSource key, Throwable ex) {
        if (ex instanceof SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
            Logger.log(Logger.ERROR, "Socket timeout retrieving coverage \'" + key + "\'");
        } else if (ex != null) { // log checked exceptions with the entire stack trace
            Logger.log(Logger.ERROR, "Coverage retrieval failed with exception \'" + key + "\'", ex);
        } else {
            Logger.log(Logger.ERROR, "Coverage retrieval failed \'" + key + "\'");
        }
    }

    @Override
    public void retrievalRejected(Retriever retriever, ImageSource key) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Coverage retrieval rejected \'" + key + "\'");
        }
    }

    protected static class TileBlock {

        public TileMatrix tileMatrix;

        public SparseIntArray rows = new SparseIntArray();

        public SparseIntArray cols = new SparseIntArray();

        public LongSparseArray<short[]> arrays = new LongSparseArray<>();

        private int texelRow = -1;

        private int texelCol = -1;

        private short[] texelArray;

        public void clear() {
            this.rows.clear();
            this.cols.clear();
            this.arrays.clear();
            this.texelRow = -1;
            this.texelCol = -1;
            this.texelArray = null;
        }

        public void putTileArray(int row, int column, short[] array) {
            long key = tileKey(this.tileMatrix, row, column);
            this.arrays.put(key, array);
        }

        public short[] getTileArray(int row, int column) {
            if (this.texelRow != row || this.texelCol != column) {
                long key = tileKey(this.tileMatrix, row, column);
                this.texelRow = row;
                this.texelCol = column;
                this.texelArray = this.arrays.get(key);
            }

            return this.texelArray;
        }

        public short readTexel(int row, int column, int i, int j) {
            short[] array = this.getTileArray(row, column);
            int pos = i + j * this.tileMatrix.tileWidth;
            return array[pos];
        }
    }
}
