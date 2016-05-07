/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwindx.R;

public class AtmosphereLayer extends AbstractLayer {

    protected ImageSource nightImageSource;

    protected Location lightLocation;

    protected Vec3 activeLightDirection = new Vec3();

    public AtmosphereLayer() {
        super("Atmosphere");
        this.nightImageSource = ImageSource.fromResource(R.drawable.dnb_land_ocean_ice_2012);
    }

    public ImageSource getNightImageSource() {
        return nightImageSource;
    }

    public void setNightImageSource(ImageSource nightImageSource) {
        this.nightImageSource = nightImageSource;
    }

    public Location getLightLocation() {
        return lightLocation;
    }

    public void setLightLocation(Location location) {
        this.lightLocation = location;
    }

    @Override
    protected void doRender(DrawContext dc) {
        // Compute the currently active light direction.
        this.determineLightDirection(dc);

        // Render the sky portion of the atmosphere.
        this.renderSky(dc);

        // Render the ground portion of the atmosphere.
        this.renderGround(dc);
    }

    protected void determineLightDirection(DrawContext dc) {
        // TODO Make light/sun direction an optional property of the WorldWindow and attach it to the DrawContext each frame
        // TODO DrawContext property defaults to the eye lat/lon like we have below
        if (this.lightLocation != null) {
            dc.globe.geographicToCartesianNormal(this.lightLocation.latitude, this.lightLocation.longitude, this.activeLightDirection);
        } else {
            dc.globe.geographicToCartesianNormal(dc.eyePosition.latitude, dc.eyePosition.longitude, this.activeLightDirection);
        }
    }

    protected void renderSky(DrawContext dc) {
        SkyProgram program = (SkyProgram) dc.getShaderProgram(SkyProgram.KEY);
        if (program == null) {
            program = (SkyProgram) dc.putShaderProgram(SkyProgram.KEY, new SkyProgram(dc.resources));
        }

        Pool<DrawableSkyAtmosphere> pool = dc.getDrawablePool(DrawableSkyAtmosphere.class);
        Drawable drawable = DrawableSkyAtmosphere.obtain(pool).set(program, this.activeLightDirection);
        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
    }

    protected void renderGround(DrawContext dc) {
        if (dc.terrain.getTileCount() == 0) {
            return; // no terrain surface to render on
        }

        GroundProgram program = (GroundProgram) dc.getShaderProgram(GroundProgram.KEY);
        if (program == null) {
            program = (GroundProgram) dc.putShaderProgram(GroundProgram.KEY, new GroundProgram(dc.resources));
        }

        // Use this layer's night image when the light location is different than the eye location.
        Texture nightTexture = null;
        if (this.nightImageSource != null && this.lightLocation != null) {
            nightTexture = dc.getTexture(this.nightImageSource);
            if (nightTexture == null) {
                nightTexture = dc.retrieveTexture(this.nightImageSource);
            }
        }

        Pool<DrawableGroundAtmosphere> pool = dc.getDrawablePool(DrawableGroundAtmosphere.class);
        Drawable drawable = DrawableGroundAtmosphere.obtain(pool).set(program, this.activeLightDirection, nightTexture);
        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
    }
}