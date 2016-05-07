/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableSurfaceTexture;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;

public class SurfaceImage extends AbstractRenderable {

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
            return; // no terrain surface to render on
        }

        Texture texture = dc.getTexture(this.imageSource); // try to get the texture from the cache
        if (texture == null) {
            texture = dc.retrieveTexture(this.imageSource); // puts retrieved textures in the cache
        }

        if (texture == null) {
            return; // no texture to draw
        }

        SurfaceTextureProgram program = this.getShaderProgram(dc);
        Pool<DrawableSurfaceTexture> pool = dc.getDrawablePool(DrawableSurfaceTexture.class);
        Drawable drawable = DrawableSurfaceTexture.obtain(pool).set(program, this.sector, texture, texture.getTexCoordTransform());
        dc.offerSurfaceDrawable(drawable, 0 /*z-order*/);
    }

    protected SurfaceTextureProgram getShaderProgram(DrawContext dc) {
        SurfaceTextureProgram program = (SurfaceTextureProgram) dc.getShaderProgram(SurfaceTextureProgram.KEY);

        if (program == null) {
            program = (SurfaceTextureProgram) dc.putShaderProgram(SurfaceTextureProgram.KEY, new SurfaceTextureProgram(dc.resources));
        }

        return program;
    }
}
