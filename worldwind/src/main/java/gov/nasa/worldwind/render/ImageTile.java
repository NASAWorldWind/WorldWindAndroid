/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;

public class ImageTile extends Tile implements SurfaceTile {

    protected ImageSource imageSource;

    protected ImageTile fallbackTile;

    public ImageTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    public ImageSource getImageSource() {
        return imageSource;
    }

    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    public ImageTile getFallbackTile() {
        return fallbackTile;
    }

    public void setFallbackTile(ImageTile fallbackTile) {
        this.fallbackTile = fallbackTile;
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public boolean bindTexture(DrawContext dc) {
        Texture texture = dc.getTexture(this.imageSource);
        if (texture != null && texture.bindTexture(dc)) {
            return true;
        }

        if (this.fallbackTile != null && this.fallbackTile.bindTexture(dc)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        Texture texture = dc.getTexture(this.imageSource);
        if (texture != null) {
            result.multiplyByMatrix(texture.getTexCoordTransform());
            return true; // use this surface tile's tex coord transform
        }

        // Use the fallback tile's tex coord transform, adjusted to the fallback image into this tile's sector.
        if (this.fallbackTile != null && this.fallbackTile.applyTexCoordTransform(dc, result)) {
            result.multiplyByTileTransform(this.sector, this.fallbackTile.sector);
            return true;
        }

        return false;
    }
}
