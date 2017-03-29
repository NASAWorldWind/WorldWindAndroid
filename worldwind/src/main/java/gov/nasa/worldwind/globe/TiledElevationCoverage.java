/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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

    private int fetchRow = -1;

    private int fetchCol = -1;

    private ShortBuffer fetchCoverage;

    public TiledElevationCoverage() {
        this.tileCache = new LruMemoryCache<>(400);
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
    public boolean hasCoverage(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledElevationCoverage", "hasCoverage", "missingSector"));
        }

        return this.levelSet.sector.intersects(sector);
    }

    @Override
    public float getHeight(double latitude, double longitude) {
        return 0; // TODO
    }

    @Override
    public float[] getHeight(Tile tile, float[] result) {
        if (tile == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledElevationCoverage", "getHeight", "missingTile"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledElevationCoverage", "getHeight", "missingResult"));
        }

        Level level = this.levelSet.levelForResolution(tile.level.texelHeight);
        this.sampleHeight(level, tile, result);

        return result;
    }

    @Override
    public float[] getHeightLimits(Tile tile, float[] result) {
        return new float[2]; // TODO
    }

    protected void sampleHeight(Level level, Tile tile, float[] result) {
        double minLat = tile.sector.minLatitude();
        double maxLat = tile.sector.maxLatitude();
        double minLon = tile.sector.minLongitude();
        double maxLon = tile.sector.maxLongitude();
        double deltaLat = tile.sector.deltaLatitude() / (tile.level.tileHeight - 1);
        double deltaLon = tile.sector.deltaLongitude() / (tile.level.tileWidth - 1);
        int index = 0;

        for (double j = 0, lat = minLat; j < tile.level.tileHeight; j += 1, lat += deltaLat) {
            if (j == tile.level.tileHeight - 1) {
                lat = maxLat; // explicitly set the last lat to the max latitude ensure alignment
            }

            for (double i = 0, lon = minLon; i < tile.level.tileWidth; i += 1, lon += deltaLon) {
                if (i == tile.level.tileWidth - 1) {
                    lon = maxLon; // explicitly set the last lon to the max longitude ensure alignment
                }

                if (this.levelSet.sector.contains(lat, lon)) { // ignore locations outside of the model
                    float height = this.sampleHeight(level, lat, lon);
                    if (!Float.isNaN(height)) {
                        result[index] = height;
                    }
                }

                index++;
            }
        }
    }

    protected float sampleHeight(Level level, double latitude, double longitude) {
        double s = (longitude + 180) / 360;
        double t = (latitude + 90) / 180;
        TexelBlock tex = new TexelBlock();

        while (level != null) {
            double tMin = 1.0 / (2.0 * level.levelHeight);
            double tMax = 1.0 - tMin;
            double u = level.levelWidth * WWMath.fract(s); // wrap the horizontal coordinate
            double v = level.levelHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the level edge

            int i0 = WWMath.mod((int) Math.floor(u - 0.5), level.levelWidth);
            int j0 = (int) WWMath.clamp(Math.floor(v - 0.5), 0, level.levelHeight - 1);

            int i1 = WWMath.mod((i0 + 1), level.levelWidth);
            int j1 = (int) WWMath.clamp(j0 + 1, 0, level.levelHeight - 1);

            float a = (float) WWMath.fract(u - 0.5);
            float b = (float) WWMath.fract(v - 0.5);

            if (this.readTexelBlock(level, i0, j0, i1, j1, tex)) {
                return tex.linear(a, b);
            }

            level = level.previousLevel();
        }

        return Float.NaN;
    }

    protected boolean readTexelBlock(Level level, int i0, int j0, int i1, int j1, TexelBlock result) {
        int tileWidth = level.tileWidth;
        int tileHeight = level.tileHeight;
        int col0 = (int) Math.floor(i0 / tileWidth);
        int col1 = (int) Math.floor(i1 / tileWidth);
        int row0 = (int) Math.floor(j0 / tileHeight);
        int row1 = (int) Math.floor(j1 / tileHeight);
        ShortBuffer i0j0, i1j0, i0j1, i1j1;

        if (row0 == row1 && col0 == col1 && row0 == this.fetchRow && col0 == this.fetchCol) {
            i0j0 = i1j0 = i0j1 = i1j1 = this.fetchCoverage; // use results from previous fetch
        } else if (row0 == row1 && col0 == col1) {
            i0j0 = i1j0 = i0j1 = i1j1 = this.fetchImage(level, row0, col0); // only need to fetch one coverage
            this.fetchRow = row0;
            this.fetchCol = col0;
            this.fetchCoverage = i0j0; // note the results for subsequent lookups
        } else {
            i0j0 = this.fetchImage(level, row0, col0);
            i1j0 = this.fetchImage(level, row0, col1);
            i0j1 = this.fetchImage(level, row1, col0);
            i1j1 = this.fetchImage(level, row1, col1);
        }

        if (i0j0 != null && i1j0 != null && i0j1 != null && i1j1 != null) {
            result.i0j0 = this.texel(i0j0, i0 % tileWidth, j0 % tileHeight);
            result.i1j0 = this.texel(i1j0, i1 % tileWidth, j0 % tileHeight);
            result.i0j1 = this.texel(i0j1, i0 % tileWidth, j1 % tileHeight);
            result.i1j1 = this.texel(i1j1, i1 % tileWidth, j1 % tileHeight);
            return true;
        }

        return false;
    }

    protected ShortBuffer fetchImage(Level level, int row, int column) {
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

    protected float texel(ShortBuffer buffer, int i, int j) {
        j = this.levelSet.tileHeight - j - 1; // flip the vertical coordinate origin
        return buffer.get(i + j * this.levelSet.tileWidth);
    }

    protected static class TexelBlock {

        public float i0j0;

        public float i1j0;

        public float i0j1;

        public float i1j1;

        public float linear(float a, float b) {
            return (1 - a) * (1 - b) * i0j0 +
                a * (1 - b) * i1j0 +
                (1 - a) * b * i0j1 +
                a * b * i1j1;
        }
    }
}

