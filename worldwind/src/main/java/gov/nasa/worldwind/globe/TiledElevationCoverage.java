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
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;
import gov.nasa.worldwind.util.WWMath;

public class TiledElevationCoverage extends AbstractElevationCoverage implements Retriever.Callback<ImageTile, Void, ShortBuffer> {

    protected TileFactory tileFactory;

    protected LevelSet levelSet = new LevelSet(); // empty level set

    protected ElevationRetriever coverageRetriever;

    protected LruMemoryCache<Long, ShortBuffer> coverageCache;

    protected Handler coverageHandler;

    private int lastReadRow = -1;

    private int lastReadCol = -1;

    private ShortBuffer lastReadCoverage;

    public TiledElevationCoverage() {
        this.coverageRetriever = new ElevationRetriever(4);
        this.coverageCache = new LruMemoryCache<>(1024 * 1024 * 4);
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
        this.sampleHeight(tile, level, result);

        return result;
    }

    @Override
    public float[] getHeightLimits(Tile tile, float[] result) {
        return new float[2]; // TODO
    }

    protected void sampleHeight(Tile tile, Level level, float[] result) {
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
                    float height = this.sampleHeight(lat, lon, level);
                    if (!Float.isNaN(height)) {
                        result[index] = height;
                    }
                }

                index++;
            }
        }
    }

    protected float sampleHeight(double latitude, double longitude, Level level) {
        double s = (longitude + 180) / 360;
        double t = (latitude + 90) / 180;
        float[] samples = new float[4];

        while (level != null) {
            double u = level.levelWidth * WWMath.fract(s); // wrap the horizontal coordinate
            int u0 = WWMath.mod((int) Math.floor(u - 0.5), level.levelWidth);
            int u1 = WWMath.mod((u0 + 1), level.levelWidth);
            float uf = (float) WWMath.fract(u - 0.5);

            double tMin = 1 / (2 * level.levelHeight);
            double tMax = 1 - tMin;
            double vMin = 0;
            double vMax = level.levelWidth - 1;
            double v = level.levelHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the level edge
            int v0 = (int) WWMath.clamp(Math.floor(v - 0.5), vMin, vMax);
            int v1 = (int) WWMath.clamp(v0 + 1, vMin, vMax);
            float vf = (float) WWMath.fract(v - 0.5);

            if (this.readCoverage(u0, v0, u1, v1, level, samples)) {
                return (1 - uf) * (1 - vf) * samples[0] +
                    uf * (1 - vf) * samples[1] +
                    (1 - uf) * vf * samples[2] +
                    uf * vf * samples[3];
            } else {
                level = level.previousLevel();
            }
        }

        return Float.NaN;
    }

    protected boolean readCoverage(int u0, int v0, int u1, int v1, Level level, float[] result) {
        int tileWidth = level.tileWidth;
        int tileHeight = level.tileHeight;
        int rowi = (int) Math.floor(v0 / tileHeight);
        int rowj = (int) Math.floor(v1 / tileHeight);
        int coli = (int) Math.floor(u0 / tileWidth);
        int colj = (int) Math.floor(u1 / tileWidth);
        ShortBuffer ii, ij, ji, jj;

        if (rowi == rowj && coli == colj && rowi == this.lastReadRow && coli == this.lastReadCol) {
            ii = ij = ji = jj = this.lastReadCoverage; // use results from previous lookup
        } else if (rowi == rowj && coli == colj) {
            ii = ij = ji = jj = this.lookupCoverage(level, rowi, coli); // only need to lookup one image
            this.lastReadRow = rowi;
            this.lastReadCol = coli;
            this.lastReadCoverage = ii; // note the results for subsequent lookups
        } else {
            ii = this.lookupCoverage(level, rowi, coli);
            ij = this.lookupCoverage(level, rowi, colj);
            ji = this.lookupCoverage(level, rowj, coli);
            jj = this.lookupCoverage(level, rowj, colj);
        }

        if (ii != null && ij != null && ji != null && jj != null) {
            result[0] = this.coverageValue(ii, u0 % tileWidth, v0 % tileHeight);
            result[1] = this.coverageValue(ij, u1 % tileWidth, v0 % tileHeight);
            result[2] = this.coverageValue(ji, u0 % tileWidth, v1 % tileHeight);
            result[3] = this.coverageValue(jj, u1 % tileWidth, v1 % tileHeight);
            return true;
        }

        return false;
    }

    protected ShortBuffer lookupCoverage(Level level, int row, int column) {
        long key = tileKey(level, row, column);
        ShortBuffer coverage = this.coverageCache.get(key);

        if (coverage == null) {
            Sector sector = tileSector(level, row, column);
            ImageTile tile = (ImageTile) this.tileFactory.createTile(sector, level, row, column);
            this.coverageRetriever.retrieve(tile, null, this);
        }

        return coverage;
    }

    protected float coverageValue(ShortBuffer buffer, int x, int y) {
        y = this.levelSet.tileHeight - y - 1; // flip the y coordinate origin to the lower left corner
        return buffer.get(x + y * this.levelSet.tileWidth);
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

    public void retrievalSucceeded(Retriever retriever, ImageTile tile, Void unused, ShortBuffer value) {
        final long finalKey = tileKey(tile.level, tile.row, tile.column);
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
            Logger.log(Logger.INFO, "Coverage retrieval succeeded \'" + tile.getImageSource() + "\'");
        }
    }

    @Override
    public void retrievalFailed(Retriever retriever, ImageTile tile, Throwable ex) {
        if (ex instanceof SocketTimeoutException) { // log socket timeout exceptions while suppressing the stack trace
            Logger.log(Logger.ERROR, "Socket timeout retrieving coverage \'" + tile.getImageSource() + "\'");
        } else if (ex != null) { // log checked exceptions with the entire stack trace
            Logger.log(Logger.ERROR, "Coverage retrieval failed with exception \'" + tile.getImageSource() + "\'", ex);
        } else {
            Logger.log(Logger.ERROR, "Coverage retrieval failed \'" + tile.getImageSource() + "\'");
        }
    }

    @Override
    public void retrievalRejected(Retriever retriever, ImageTile tile) {
        if (Logger.isLoggable(Logger.DEBUG)) {
            Logger.log(Logger.DEBUG, "Coverage retrieval rejected \'" + tile.getImageSource() + "\'");
        }
    }
}

