/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.util.Logger;

public class SurfaceImage extends AbstractRenderable implements SurfaceTile {

    protected final Sector sector = new Sector();

    protected ImageSource imageSource;

    public SurfaceImage() {
        super("Surface Image");
    }

    public SurfaceImage(Sector sector, ImageSource imageSource) {
        super("Surface Image");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        this.sector.set(sector);
        this.imageSource = imageSource;
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    public void setSector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "setSector", "missingSector"));
        }

        this.sector.set(sector);
    }

    public ImageSource getImageSource() {
        return imageSource;
    }

    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (this.sector.isEmpty() || this.imageSource == null) {
            return; // nothing to render
        }

        if (!dc.terrain.getSector().intersects(this.sector)) {
            return; // nothing to render on
        }

        dc.surfaceTileRenderer.renderTile(dc, this);
    }

    @Override
    public boolean bindTexture(DrawContext dc, int texUnit) {
        if (this.imageSource != null) {
            GpuTexture texture = (GpuTexture) dc.gpuObjectCache.get(this.imageSource);
            if (texture == null) {
                texture = new GpuTexture(dc, this.imageSource); // adds itself to the GPU object cache
            }

            return texture.bindTexture(dc, texUnit);
        }

        return false;
    }

    @Override
    public boolean applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        if (this.imageSource != null) {
            GpuTexture texture = (GpuTexture) dc.gpuObjectCache.get(this.imageSource);
            return (texture != null) && texture.applyTexCoordTransform(result);
        }

        return false;
    }
}
