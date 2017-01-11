/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableSurfaceTexture;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class TiledSurfaceImage extends AbstractRenderable {

    protected TileFactory tileFactory;

    protected LevelSet levelSet = new LevelSet(); // empty level set

    protected ImageOptions imageOptions;

    protected double detailControl = 4;

    protected List<Tile> topLevelTiles = new ArrayList<>();

    /**
     * Memory cache for this layer's subdivision tiles. Each entry contains an array of four image tiles corresponding
     * to the subdivision of the group's common parent tile. The cache is configured to hold 500 groups, a number
     * empirically determined to be sufficient for storing the tiles needed to navigate a small region.
     */
    protected LruMemoryCache<String, Tile[]> tileCache = new LruMemoryCache<>(500);

    protected SurfaceTextureProgram activeProgram;

    protected ImageTile ancestorTile;

    protected Texture ancestorTexture;

    protected Matrix3 ancestorTexCoordMatrix = new Matrix3();

    public TiledSurfaceImage() {
        super("Tiled Surface Image");
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
        this.invalidateTiles();
    }

    public TileFactory getTileFactory() {
        return this.tileFactory;
    }

    public void setTileFactory(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
        this.invalidateTiles();
    }

    public ImageOptions getImageOptions() {
        return this.imageOptions;
    }

    public void setImageOptions(ImageOptions imageOptions) {
        this.imageOptions = imageOptions;
        this.invalidateTiles();
    }

    public double getDetailControl() {
        return this.detailControl;
    }

    public void setDetailControl(double detailControl) {
        this.detailControl = detailControl;
    }

    @Override
    protected void doRender(RenderContext rc) {
        if (rc.terrain.getSector().isEmpty()) {
            return; // no terrain surface to render on
        }

        this.determineActiveProgram(rc);
        this.assembleTiles(rc);

        this.activeProgram = null; // clear the active program to avoid leaking render resources
        this.ancestorTile = null; // clear the ancestor tile and texture
        this.ancestorTexture = null;
    }

    protected void determineActiveProgram(RenderContext rc) {
        this.activeProgram = (SurfaceTextureProgram) rc.getShaderProgram(SurfaceTextureProgram.KEY);

        if (this.activeProgram == null) {
            this.activeProgram = (SurfaceTextureProgram) rc.putShaderProgram(SurfaceTextureProgram.KEY, new SurfaceTextureProgram(rc.resources));
        }
    }

    protected void assembleTiles(RenderContext rc) {
        // TODO
        // The need to create Tiles with a defined image source couples the need to determine a tile's visibility with
        // he need to know its image source. Decoupling the two would mean we only need to know the image source when
        // the texture is actually requested Could the tile-based operations done here be implicit on level/row/column,
        // or use transient pooled tile objects not tied to an image source?

        if (this.topLevelTiles.isEmpty()) {
            this.createTopLevelTiles();
        }

        for (int idx = 0, len = this.topLevelTiles.size(); idx < len; idx++) {
            this.addTileOrDescendants(rc, (ImageTile) this.topLevelTiles.get(idx));
        }
    }

    protected void createTopLevelTiles() {
        Level firstLevel = this.levelSet.firstLevel();
        if (firstLevel != null) {
            Tile.assembleTilesForLevel(firstLevel, this.tileFactory, this.topLevelTiles);
        }
    }

    protected void addTileOrDescendants(RenderContext rc, ImageTile tile) {
        if (!tile.intersectsSector(this.levelSet.sector) || !tile.intersectsFrustum(rc, rc.frustum)) {
            return; // ignore the tile and its descendants if it's not needed or not visible
        }

        if (tile.level.isLastLevel() || !tile.mustSubdivide(rc, this.detailControl)) {
            this.addTile(rc, tile);
            return; // use the tile if it does not need to be subdivided
        }

        ImageTile currentAncestorTile = this.ancestorTile;
        Texture currentAncestorTexture = this.ancestorTexture;

        ImageSource tileImageSource = tile.getImageSource();
        if (tileImageSource != null) { // tile has an image source; its level is not empty
            Texture tileTexture = rc.getTexture(tileImageSource);
            if (tileTexture != null) { // tile has a texture; use it as a fallback tile for descendants
                this.ancestorTile = tile;
                this.ancestorTexture = tileTexture;
            }
        }

        for (Tile child : tile.subdivideToCache(this.tileFactory, this.tileCache, 4)) { // each tile has a cached size of 1
            this.addTileOrDescendants(rc, (ImageTile) child); // recursively process the tile's children
        }

        this.ancestorTile = currentAncestorTile; // restore the last fallback tile, even if it was null
        this.ancestorTexture = currentAncestorTexture;
    }

    protected void addTile(RenderContext rc, ImageTile tile) {
        ImageSource imageSource = tile.getImageSource();
        if (imageSource == null) {
            return; // no image source indicates an empty level or an image missing from the tiled data store
        }

        Texture texture = rc.getTexture(imageSource); // try to get the texture from the cache
        if (texture == null) {
            texture = rc.retrieveTexture(imageSource, this.imageOptions); // puts retrieved textures in the cache
        }

        if (texture != null) { // use the tile's own texture
            Pool<DrawableSurfaceTexture> pool = rc.getDrawablePool(DrawableSurfaceTexture.class);
            Drawable drawable = DrawableSurfaceTexture.obtain(pool).set(this.activeProgram, tile.sector, texture, texture.getTexCoordTransform());
            rc.offerSurfaceDrawable(drawable, 0 /*z-order*/);
        } else if (this.ancestorTile != null) { // use the ancestor tile's texture, transformed to fill the tile sector
            this.ancestorTexCoordMatrix.set(this.ancestorTexture.getTexCoordTransform());
            this.ancestorTexCoordMatrix.multiplyByTileTransform(tile.sector, this.ancestorTile.sector);
            Pool<DrawableSurfaceTexture> pool = rc.getDrawablePool(DrawableSurfaceTexture.class);
            Drawable drawable = DrawableSurfaceTexture.obtain(pool).set(this.activeProgram, tile.sector, this.ancestorTexture, this.ancestorTexCoordMatrix);
            rc.offerSurfaceDrawable(drawable, 0 /*z-order*/);
        }
    }

    protected void invalidateTiles() {
        this.topLevelTiles.clear();
        this.tileCache.clear();
    }
}
