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

    protected Object imageSource; // TODO imageSource

    protected ImageTile fallbackTile;

    public ImageTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    public Object getImageSource() {
        return imageSource;
    }

    public void setImageSource(Object imageSource) {
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
    public boolean bind(DrawContext dc, int texUnit) {
        if (this.imageSource == null) {
            return false;
        }

        GpuTexture texture = null; //dc.getGpuObjectCache().retrieveTexture(dc, this.imageSource); // TODO
        if (texture != null) {
            dc.bindTexture(texUnit, texture);
            return true;
        } else if (this.fallbackTile != null) {
            return this.fallbackTile.bind(dc, texUnit);
        }

        return false;
    }

    @Override
    public void applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        if (this.imageSource == null) {
            return;
        }

        GpuTexture texture = (GpuTexture) dc.getGpuObjectCache().get(this.imageSource);
        if (texture != null) {
            // Apply this surface tile's tex coord transform.
            texture.applyTexCoordTransform(result);
        } else if (this.fallbackTile != null) {
            // Apply the fallback tile's tex coord transform.
            this.fallbackTile.applyTexCoordTransform(dc, result);
            // Transform from this tile's coordinates to the fallback tile's coordinates.
            result.multiplyByTileTransform(this.sector, this.fallbackTile.sector);
        }
    }
}
