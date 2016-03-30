/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;
import gov.nasa.worldwind.util.TileUrlFactory;

public class TiledImageLayer extends AbstractLayer implements TileFactory {

    protected LevelSet levelSet = new LevelSet(); // empty level set

    protected TileUrlFactory tileUrlFactory;

    protected String imageFormat;

    protected double detailControl = 4;

    protected List<Tile> topLevelTiles = new ArrayList<>();

    protected List<ImageTile> currentTiles = new ArrayList<>();

    protected ImageTile currentFallbackTile = null;

    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(600); // capacity for 600 tiles

    public TiledImageLayer() {
        this.setDisplayName("Tiled Image Layer");
        this.init();
    }

    public TiledImageLayer(String displayName) {
        super(displayName);
        this.init();
    }

    protected void init() {
        this.setPickEnabled(false);
    }

    public LevelSet getLevelSet() {
        return this.levelSet;
    }

    public void setLevelSet(LevelSet levelSet) {
        if (levelSet == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledImageLayer", "setLevelSet", "missingLevelSet"));
        }

        this.levelSet = levelSet;
        this.invalidateTiles();
    }

    protected TileUrlFactory getTileUrlFactory() {
        return tileUrlFactory;
    }

    protected void setTileUrlFactory(TileUrlFactory tileUrlFactory) {
        this.tileUrlFactory = tileUrlFactory;
        this.invalidateTiles();
    }

    protected String getImageFormat() {
        return imageFormat;
    }

    protected void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
        this.invalidateTiles();
    }

    public double getDetailControl() {
        return detailControl;
    }

    public void setDetailControl(double detailControl) {
        this.detailControl = detailControl;
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (dc.getTerrain().getTileCount() == 0) {
            return; // no terrain surface to render on
        }

        this.assembleTiles(dc);

        if (this.currentTiles.size() > 0) { // draw the tiles on the terrain
            dc.getSurfaceTileRenderer().renderTiles(dc, this.currentTiles);
        }

        this.clearTiles(); // clear references to cached tiles; there may be no more calls to render
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        ImageTile tile = new ImageTile(sector, level, row, column);

        if (this.tileUrlFactory != null && this.imageFormat != null) {
            tile.setImageUrl(this.tileUrlFactory.urlForTile(tile, this.imageFormat));
        }

        return tile;
    }

    protected void assembleTiles(DrawContext dc) {
        this.currentTiles.clear();

        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        for (Tile tile : this.topLevelTiles) {
            this.addTileOrDescendants(dc, (ImageTile) tile);
        }
    }

    protected void createTopLevelTiles() {
        Level firstLevel = this.levelSet.firstLevel();
        if (firstLevel != null) {
            Tile.assembleTilesForLevel(firstLevel, this, this.topLevelTiles);
        }
    }

    protected void addTileOrDescendants(DrawContext dc, ImageTile tile) {
        if (!tile.intersectsFrustum(dc, dc.getFrustum())) {
            return; // ignore the tile and its descendants if it's not visible
        }

        if (tile.level.isLastLevel() || !tile.mustSubdivide(dc, this.detailControl)) {
            this.addTile(dc, tile);
            return; // use the tile if it does not need to be subdivided
        }

        ImageTile fallbackTile = this.currentFallbackTile;
        if (tile.hasTexture(dc)) { // use it as a fallback tile for descendants
            this.currentFallbackTile = tile;
        }

        for (Tile child : tile.subdivideToCache(this, this.tileCache, 4)) { // each tile has a cached size of 1
            this.addTileOrDescendants(dc, (ImageTile) child); // recursively process the tile's children
        }

        this.currentFallbackTile = fallbackTile; // restore the last fallback tile, even if it was null
    }

    protected void addTile(DrawContext dc, ImageTile tile) {
        tile.setFallbackTile(tile.hasTexture(dc) ? null : this.currentFallbackTile);
        this.currentTiles.add(tile);
    }

    protected void clearTiles() {
        for (ImageTile tile : this.currentTiles) {
            tile.setFallbackTile(null); // avoid memory leaks due to fallback tile references
        }

        this.currentTiles.clear(); // clear the tile list
        this.currentFallbackTile = null; // clear the fallback tile
    }

    protected void invalidateTiles() {
        this.topLevelTiles.clear();
        this.currentTiles.clear();
        this.tileCache.clear();
    }
}
