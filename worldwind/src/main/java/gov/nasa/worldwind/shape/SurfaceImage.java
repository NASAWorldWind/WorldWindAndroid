/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.render.Texture;
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
        if (this.sector.isEmpty()) {
            return; // nothing to render
        }

        if (!dc.terrain.getSector().intersects(this.sector)) {
            return; // nothing to render on
        }

        Texture texture = dc.getTexture(this.imageSource);
        if (texture == null) {
            texture = dc.retrieveTexture(this.imageSource); // adds the retrieved texture to the cache
        }

        if (texture != null) {
            dc.surfaceTileRenderer.renderTile(dc, this);
        }
    }

    @Override
    public boolean bindTexture(DrawContext dc) {
        Texture texture = dc.getTexture(this.imageSource);
        return (texture != null) && texture.bindTexture(dc);
    }

    @Override
    public boolean applyTexCoordTransform(DrawContext dc, Matrix3 result) {
        Texture texture = dc.getTexture(this.imageSource);
        return (texture != null) && texture.applyTexCoordTransform(result);
    }
}
