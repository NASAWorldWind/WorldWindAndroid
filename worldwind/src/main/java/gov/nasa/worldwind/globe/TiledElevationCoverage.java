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
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;
import gov.nasa.worldwind.util.WWMath;

public class TiledElevationCoverage extends AbstractElevationCoverage implements Retriever.Callback<ImageSource, Void, ShortBuffer> {

    protected TileFactory tileFactory;

    protected LevelSet levelSet = new LevelSet(); // empty level set

    protected LruMemoryCache<Long, Tile> tileCache;

    protected LruMemoryCache<ImageSource, ShortBuffer> coverageCache;

    protected ElevationRetriever coverageRetriever;

    protected Handler coverageHandler;

    public TiledElevationCoverage() {
        this.tileCache = new LruMemoryCache<>(200);
        this.coverageCache = new LruMemoryCache<>(1024 * 1024 * 4);
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

    public LevelSet getLevelSet() {
        return this.levelSet;
    }

    public void setLevelSet(LevelSet levelSet) {
        if (levelSet == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledSurfaceImage", "setLevelSet", "missingLevelSet"));
        }

        this.levelSet = levelSet;
    }

    public TileFactory getTileFactory() {
        return this.tileFactory;
    }

    public void setTileFactory(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
    }

    @Override
    public boolean hasCoverage(double latitude, double longitude) {
        return this.levelSet.sector.contains(latitude, longitude);
    }

    @Override
    public boolean hasCoverage(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledElevationCoverage", "hasCoverage", "missingSector"));
        }

        return this.levelSet.sector.intersects(sector);
    }

    @Override
    protected boolean doGetHeight(double latitude, double longitude, float[] result) {
        return false; // TODO
    }

    @Override
    protected boolean doGetHeightGrid(Sector gridSector, int gridWidth, int gridHeight, double radiansPerPixel, float[] result) {
        TileBlock tileBlock = new TileBlock();

        Level level = this.levelSet.levelForResolution(radiansPerPixel);
        while (level != null) {

            if (this.fetchTileBlock(gridSector, gridWidth, gridHeight, level, tileBlock)) {
                this.sampleTileBlock(gridSector, gridWidth, gridHeight, tileBlock, result);
                return true;
            }

            level = level.previousLevel(); // try a lower resolution ancestor level
        }

        return false;
    }

    @Override
    protected boolean doGetHeightLimits(Sector sector, double radiansPerPixel, float[] result) {
        return false; // TODO
    }

    protected boolean fetchTileBlock(Sector gridSector, int gridWidth, int gridHeight, Level level, TileBlock result) {
        int levelWidth = level.levelWidth;
        int levelHeight = level.levelHeight;
        int tileWidth = level.tileWidth;
        int tileHeight = level.tileHeight;
        double tMin = 1.0 / (2.0 * levelHeight);
        double tMax = 1.0 - tMin;

        result.level = level;
        result.clear();

        double lon = gridSector.minLongitude();
        double deltaLon = gridSector.deltaLongitude() / (gridWidth - 1);
        for (int uidx = 0; uidx < gridWidth; uidx++, lon += deltaLon) {
            double s = (lon + 180) / 360;
            double u = levelWidth * WWMath.fract(s); // wrap the horizontal coordinate
            int i0 = WWMath.mod((int) Math.floor(u - 0.5), levelWidth);
            int i1 = WWMath.mod((i0 + 1), levelWidth);
            int col0 = (int) Math.floor(i0 / tileWidth);
            int col1 = (int) Math.floor(i1 / tileWidth);
            result.cols.append(col0, 0);
            result.cols.append(col1, 0);
        }

        double lat = gridSector.minLatitude();
        double deltaLat = gridSector.deltaLatitude() / (gridHeight - 1);
        for (double vidx = 0; vidx < gridHeight; vidx++, lat += deltaLat) {
            double t = (lat + 90) / 180;
            double v = levelHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the level edge
            int j0 = (int) WWMath.clamp(Math.floor(v - 0.5), 0, levelHeight - 1);
            int j1 = (int) WWMath.clamp(j0 + 1, 0, levelHeight - 1);
            int row0 = (int) Math.floor(j0 / tileHeight);
            int row1 = (int) Math.floor(j1 / tileHeight);
            result.rows.append(row0, 0);
            result.rows.append(row1, 0);
        }

        for (int ridx = 0, rlen = result.rows.size(); ridx < rlen; ridx++) {
            for (int cidx = 0, clen = result.cols.size(); cidx < clen; cidx++) {
                int row = result.rows.keyAt(ridx);
                int col = result.cols.keyAt(cidx);
                ShortBuffer array = this.fetchTileArray(level, row, col);
                if (array != null) {
                    result.putTileArray(row, col, array);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    protected ShortBuffer fetchTileArray(Level level, int row, int column) {
        long key = tileKey(level, row, column);
        Tile tile = this.tileCache.get(key);
        if (tile == null) {
            Sector sector = tileSector(level, row, column);
            tile = this.tileFactory.createTile(sector, level, row, column);
            this.tileCache.put(key, tile, 1);
        }

        ImageSource source = ((ImageTile) tile).getImageSource();
        ShortBuffer coverage = this.coverageCache.get(source);
        if (coverage == null) {
            this.coverageRetriever.retrieve(source, null, this);
        }

        return coverage;
    }

    protected static long tileKey(Level level, int row, int column) {
        long llevelNum = (level.levelNumber & 0xFFL); // 8 bits
        long lrow = (row & 0xFFFFFFFL); // 28 bits
        long lcolumn = (column & 0xFFFFFFFL); // 28 bits
        long key = (llevelNum << 56) | (lrow << 28) | lcolumn;

        return key;
    }

    protected static Sector tileSector(Level level, int row, int column) {
        double minLat = -90 + row * level.tileDelta;
        double minLon = -180 + column * level.tileDelta;

        return new Sector(minLat, minLon, level.tileDelta, level.tileDelta);
    }

    protected void sampleTileBlock(Sector gridSector, int gridWidth, int gridHeight, TileBlock tileBlock, float[] result) {
        int levelWidth = tileBlock.level.levelWidth;
        int levelHeight = tileBlock.level.levelHeight;
        int tileWidth = tileBlock.level.tileWidth;
        int tileHeight = tileBlock.level.tileHeight;
        double tMin = 1.0 / (2.0 * levelHeight);
        double tMax = 1.0 - tMin;
        int index = 0;

        double lat = gridSector.minLatitude();
        double deltaLat = gridSector.deltaLatitude() / (gridHeight - 1);
        for (double vidx = 0; vidx < gridHeight; vidx++, lat += deltaLat) {
            double t = (lat + 90) / 180;
            double v = levelHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the level edge
            float b = (float) WWMath.fract(v - 0.5);
            int j0 = (int) WWMath.clamp(Math.floor(v - 0.5), 0, levelHeight - 1);
            int j1 = (int) WWMath.clamp(j0 + 1, 0, levelHeight - 1);
            int row0 = (int) Math.floor(j0 / tileHeight);
            int row1 = (int) Math.floor(j1 / tileHeight);

            double lon = gridSector.minLongitude();
            double deltaLon = gridSector.deltaLongitude() / (gridWidth - 1);
            for (int uidx = 0; uidx < gridWidth; uidx++, lon += deltaLon) {
                double s = (lon + 180) / 360;
                double u = levelWidth * WWMath.fract(s); // wrap the horizontal coordinate
                float a = (float) WWMath.fract(u - 0.5);
                int i0 = WWMath.mod((int) Math.floor(u - 0.5), levelWidth);
                int i1 = WWMath.mod((i0 + 1), levelWidth);
                int col0 = (int) Math.floor(i0 / tileWidth);
                int col1 = (int) Math.floor(i1 / tileWidth);

                float i0j0 = tileBlock.readTexel(row0, col0, i0 % tileWidth, j0 % tileHeight);
                float i1j0 = tileBlock.readTexel(row0, col1, i1 % tileWidth, j0 % tileHeight);
                float i0j1 = tileBlock.readTexel(row1, col0, i0 % tileWidth, j1 % tileHeight);
                float i1j1 = tileBlock.readTexel(row1, col1, i1 % tileWidth, j1 % tileHeight);

                result[index++] = (1 - a) * (1 - b) * i0j0 +
                    a * (1 - b) * i1j0 +
                    (1 - a) * b * i0j1 +
                    a * b * i1j1;
            }
        }
    }

    public void retrievalSucceeded(Retriever retriever, ImageSource key, Void unused, ShortBuffer value) {
        final ImageSource finalKey = key;
        final ShortBuffer finalValue = value;

        this.coverageHandler.post(new Runnable() {
            @Override
            public void run() {
                coverageCache.put(finalKey, finalValue, finalValue.capacity() * 2);
                updateTimestamp();
                WorldWind.requestRedraw();
            }
        });

        if (Logger.isLoggable(Logger.INFO)) {
            Logger.log(Logger.INFO, "Coverage retrieval succeeded \'" + key + "\'");
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

        public Level level;

        public SparseIntArray rows = new SparseIntArray();

        public SparseIntArray cols = new SparseIntArray();

        public LongSparseArray<ShortBuffer> arrays = new LongSparseArray<>();

        private int texelRow = -1;

        private int texelCol = -1;

        private ShortBuffer texelArray;

        public void clear() {
            this.rows.clear();
            this.cols.clear();
            this.arrays.clear();
            this.texelRow = -1;
            this.texelCol = -1;
            this.texelArray = null;
        }

        public void putTileArray(int row, int column, ShortBuffer array) {
            long key = tileKey(this.level, row, column);
            this.arrays.put(key, array);
        }

        public float readTexel(int row, int column, int i, int j) {
            if (this.texelRow != row || this.texelCol != column) {
                long key = tileKey(this.level, row, column);
                this.texelRow = row;
                this.texelCol = column;
                this.texelArray = this.arrays.get(key);
            }

            j = this.level.tileHeight - j - 1; // flip the vertical coordinate origin
            return this.texelArray.get(i + j * this.level.tileWidth);
        }
    }
}

