/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.cache.LruMemoryCache;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;
import gov.nasa.worldwind.util.TileUrlFactory;

public class TiledImageLayer extends AbstractLayer implements TileFactory {

    protected static final int DEFAULT_DETAIL_CONTROL = 80;

    protected static final int DEFAULT_TILE_CACHE_CAPACITY = 600; // capacity for 600 tiles

    protected LevelSet levels;

    protected TileUrlFactory tileUrlFactory;

    protected String imageFormat;

    protected double detailControl = DEFAULT_DETAIL_CONTROL;

    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(DEFAULT_TILE_CACHE_CAPACITY);

    protected List<Tile> topLevelTiles = new ArrayList<>();

    protected List<ImageTile> currentTiles = new ArrayList<>();

    public TiledImageLayer(Sector sector, double topLevelDelta, int numLevels, int tileWidth, int tileHeight) {
        super("Tiled Image Layer");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledImageLayer", "constructor", "missingSector"));
        }

        if (topLevelDelta <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledImageLayer", "constructor", "invalidTileDelta"));
        }

        if (numLevels < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledImageLayer", "constructor", "invalidNumLevels"));
        }

        if (tileWidth < 1 || tileHeight < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledImageLayer", "constructor", "invalidWidthOrHeight"));
        }

        this.levels = new LevelSet(sector, topLevelDelta, numLevels, tileWidth, tileHeight);
        this.setPickEnabled(false);
    }

    public TileUrlFactory getTileUrlFactory() {
        return tileUrlFactory;
    }

    public void setTileUrlFactory(TileUrlFactory tileUrlFactory) {
        this.tileUrlFactory = tileUrlFactory;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public double getDetailControl() {
        return detailControl;
    }

    public void setDetailControl(double detailControl) {
        this.detailControl = detailControl;
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (dc.getTerrain().getTileCount() > 0) {
            return;
        }

        this.assembleTiles(dc);

        if (this.currentTiles.size() == 0) {
            return;
        }

        dc.getSurfaceTileRenderer().renderTiles(dc, this.currentTiles);
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        ImageTile tile = new ImageTile(sector, level, row, column);

        if (this.tileUrlFactory != null) {
            tile.setImageSource(this.tileUrlFactory.urlForTile(tile, this.imageFormat));
        }

        return tile;
    }

    protected void assembleTiles(DrawContext dc) {
        this.topLevelTiles.clear();

        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        for (Tile tile : this.topLevelTiles) {
            this.addTileOrDescendants(dc, (ImageTile) tile);
        }
    }

    protected void createTopLevelTiles() {
        Tile.assembleTilesForLevel(this.levels.firstLevel(), this, this.topLevelTiles);
    }

    protected void addTileOrDescendants(DrawContext dc, ImageTile tile) {
        if (!tile.intersectsFrustum(dc, dc.getFrustum())) {
            return; // ignore the tile and its descendants if it's not visible
        }

        if (tile.level.isLastLevel() || !tile.mustSubdivide(dc, this.detailControl)) {
            this.addTile(dc, tile);
            return; // use the tile if it does not need to be subdivided
        }

        for (Tile child : tile.subdivideToCache(this, this.tileCache, 4)) { // each tile has a cached size of 1
            this.addTileOrDescendants(dc, (ImageTile) child); // recursively process the tile's children
        }
    }

    protected void addTile(DrawContext dc, ImageTile tile) {
        this.currentTiles.add(tile);
    }
}
