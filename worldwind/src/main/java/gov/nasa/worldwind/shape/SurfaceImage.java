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

    protected final Sector sector = new Sector();

    protected Object imageSource;

    public SurfaceImage() {
        super("Surface Image");
    }

    public SurfaceImage(Sector sector, Object imageSource) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        this.sector.set(sector);
        this.imageSource = imageSource;
    }

    public SurfaceImage(Sector sector, @DrawableRes int resourceId) {
        super("Surface Image");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        this.sector.set(sector);
        this.imageSource = resourceId;
    }

    public SurfaceImage(Sector sector, String urlString) {
        super("Surface Image");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        if (urlString == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingUrl"));
        }

        this.sector.set(sector);
        this.imageSource = urlString;
    }

    @Override
    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "setSector", "missingSector"));
        }

        this.sector.set(sector);
    }

    public Object getImageSource() {
        return imageSource;
    }

    public void setImageSource(Object imageSource) {
        this.imageSource = imageSource;
    }

    public void setImageResource(@DrawableRes int resourceId) {
        this.imageSource = resourceId;
    }

    public void setImageUrl(String urlString) {
        if (urlString == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "setImageUrl", "missingUrl"));
        }

        this.imageSource = urlString;
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (this.sector.isEmpty() || this.imageSource == null) {
            return; // nothing to render
        }

        if (!dc.getTerrain().getSector().intersects(this.sector)) {
            return; // nothing to render on
        }

        dc.getSurfaceTileRenderer().renderTile(dc, this);
    }

    @Override
    public boolean bindTexture(DrawContext dc, int texUnit) {
        GpuTexture texture = (GpuTexture) dc.getGpuObjectCache().get(this.imageSource);
        if (texture == null) {
            texture = new GpuTexture(dc, this.imageSource); // adds itself to the GPU object cache
        }

        return texture.bindTexture(dc, texUnit);
    }

    @Override
    public boolean applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        GpuTexture texture = (GpuTexture) dc.getGpuObjectCache().get(this.imageSource);
        return (texture != null) && texture.applyTexCoordTransform(result);
    }
}
