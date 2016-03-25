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

    protected String imagePath;

    protected ImageTile fallbackTile;

    public ImageTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
        if (this.imagePath != null) {
            //GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.imagePath);
            //if (texture != null) {
            //    dc.bindTexture(texUnit, texture);
            //    return true;
            //}
        }

        return (this.fallbackTile != null) && this.fallbackTile.bind(dc, texUnit);
    }

    @Override
    public void applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        if (this.imagePath != null) {
            //GpuTexture texture = (GpuTexture) dc.getGpuObjectCache().get(this.imagePath);
            //if (texture != null) {
            //    texture.applyTexCoordTransform(result); // apply this surface tile's tex coord transform
            //    return;
            //}
        }

        if (this.fallbackTile != null) {
            this.fallbackTile.applyTexCoordTransform(dc, result); // apply the fallback tile's tex coord transform
            result.multiplyByTileTransform(this.sector, this.fallbackTile.sector); // transform to fallback tile coords
        }
    }
}
