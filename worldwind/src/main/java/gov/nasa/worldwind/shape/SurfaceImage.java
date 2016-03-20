/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.support.annotation.DrawableRes;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.util.Logger;

public class SurfaceImage extends AbstractRenderable implements SurfaceTile {

    // TODO consider making SurfaceTile implementation an inner class

    protected Sector sector;

    protected int imageId;

    public SurfaceImage(Sector sector, @DrawableRes int imageId) {
        super("Surface Image");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        this.sector = sector;
        this.imageId = imageId;
    }

    @Override
    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        this.sector = sector;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(@DrawableRes int imageId) {
        this.imageId = imageId;
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (!dc.getTerrain().getSector().intersects(this.sector)) {
            return;
        }

        dc.getSurfaceTileRenderer().renderTile(dc, this);
    }

    @Override
    public boolean bind(DrawContext dc, int texUnit) {
        GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.imageId);
        if (texture != null) {
            dc.bindTexture(texUnit, texture);
        }

        return texture != null;
    }

    @Override
    public void applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        GpuTexture texture = (GpuTexture) dc.getGpuObjectCache().get(this.imageId);
        if (texture != null) {
            texture.applyTexCoordTransform(result);
        }
    }
}
