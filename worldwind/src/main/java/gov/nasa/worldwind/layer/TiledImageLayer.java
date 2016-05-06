/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableSurfaceTexture;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
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

    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(600); // capacity for 600 tiles

    protected SurfaceTextureProgram activeProgram;

    protected ImageTile ancestorTile;

    protected Texture ancestorTexture;

    protected Matrix3 ancestorTexCoordMatrix = new Matrix3();

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
        if (dc.terrain.getTileCount() == 0) {
            return; // no terrain surface to render on
        }

        this.determineActiveProgram(dc);
        this.assembleTiles(dc);

        this.activeProgram = null; // clear the active program to avoid leaking render resources
        this.ancestorTile = null; // clear the ancestor tile and texture
        this.ancestorTexture = null;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        ImageTile tile = new ImageTile(sector, level, row, column);

        if (this.tileUrlFactory != null && this.imageFormat != null) {
            String urlString = this.tileUrlFactory.urlForTile(tile, this.imageFormat);
            tile.setImageSource(ImageSource.fromUrl(urlString));
        }

        return tile;
    }

    protected void determineActiveProgram(DrawContext dc) {
        this.activeProgram = (SurfaceTextureProgram) dc.getShaderProgram(SurfaceTextureProgram.KEY);

        if (this.activeProgram == null) {
            this.activeProgram = (SurfaceTextureProgram) dc.putShaderProgram(SurfaceTextureProgram.KEY, new SurfaceTextureProgram(dc.resources));
        }
    }

    protected void assembleTiles(DrawContext dc) {
        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        for (int idx = 0, len = this.topLevelTiles.size(); idx < len; idx++) {
            this.addTileOrDescendants(dc, (ImageTile) this.topLevelTiles.get(idx));
        }
    }

    protected void createTopLevelTiles() {
        Level firstLevel = this.levelSet.firstLevel();
        if (firstLevel != null) {
            Tile.assembleTilesForLevel(firstLevel, this, this.topLevelTiles);
        }
    }

    protected void addTileOrDescendants(DrawContext dc, ImageTile tile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(dc, dc.frustum)) {
            return; // ignore the tile and its descendants if it's not needed or not visible
        }

        if (tile.level.isLastLevel() || !tile.mustSubdivide(dc, this.detailControl)) {
            this.addTile(dc, tile);
            return; // use the tile if it does not need to be subdivided
        }

        ImageTile currentAncestorTile = this.ancestorTile;
        Texture currentAncestorTexture = this.ancestorTexture;

        Texture tileTexture = dc.getTexture(tile.getImageSource());
        if (tileTexture != null) { // use it as a fallback tile for descendants
            this.ancestorTile = tile;
            this.ancestorTexture = tileTexture;
        }

        for (Tile child : tile.subdivideToCache(this, this.tileCache, 4)) { // each tile has a cached size of 1
            this.addTileOrDescendants(dc, (ImageTile) child); // recursively process the tile's children
        }

        this.ancestorTile = currentAncestorTile; // restore the last fallback tile, even if it was null
        this.ancestorTexture = currentAncestorTexture;
    }

    protected void addTile(DrawContext dc, ImageTile tile) {
        Texture texture = dc.getTexture(tile.getImageSource());

        if (texture == null) {
            texture = dc.retrieveTexture(tile.getImageSource());
        }

        if (texture != null) { // use the tile's own texture
            Drawable drawable = DrawableSurfaceTexture.obtain(this.activeProgram, tile.sector, texture);
            dc.offerSurfaceDrawable(drawable, 0 /*z-order*/);
        } else if (this.ancestorTile != null) { // use the ancestor tile's texture, transformed to fill the tile sector
            this.ancestorTexCoordMatrix.set(this.ancestorTexture.getTexCoordTransform());
            this.ancestorTexCoordMatrix.multiplyByTileTransform(tile.sector, this.ancestorTile.sector);
            Drawable drawable = DrawableSurfaceTexture.obtain(this.activeProgram, tile.sector, this.ancestorTexture, this.ancestorTexCoordMatrix);
            dc.offerSurfaceDrawable(drawable, 0 /*z-order*/);
        }
    }

    protected void invalidateTiles() {
        this.topLevelTiles.clear();
        this.tileCache.clear();
    }
}
