/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwindx.R;

public class AtmosphereLayer extends AbstractLayer {

    protected ImageSource nightImageSource;

    protected Location lightLocation;

    protected Vec3 activeLightDirection = new Vec3();

    private int skyWidth = 128;

    private int skyHeight = 128;

    private Sector fullSphereSector = new Sector().setFullSphere();

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
        Pool<DrawableSkyAtmosphere> pool = dc.getDrawablePool(DrawableSkyAtmosphere.class);
        DrawableSkyAtmosphere drawable = DrawableSkyAtmosphere.obtain(pool);

        drawable.program = (SkyProgram) dc.getShaderProgram(SkyProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (SkyProgram) dc.putShaderProgram(SkyProgram.KEY, new SkyProgram(dc.resources));
        }

        drawable.lightDirection.set(this.activeLightDirection);
        drawable.globeRadius = dc.globe.getEquatorialRadius();

        if (drawable.vertexPoints == null) {
            int count = this.skyWidth * this.skyHeight;
            double[] array = new double[count];
            Arrays.fill(array, drawable.program.getAltitude());

            drawable.vertexPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
            dc.globe.geographicToCartesianGrid(this.fullSphereSector, this.skyWidth, this.skyHeight, array, null,
                drawable.vertexPoints, 3).rewind();
        }

        if (drawable.triStripElements == null) {
            drawable.triStripElements = assembleTriStripElements(this.skyWidth, this.skyHeight);
        }

        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
    }

    protected void renderGround(DrawContext dc) {
        if (dc.terrain.getTileCount() == 0) {
            return; // no terrain surface to render on
        }

        Pool<DrawableGroundAtmosphere> pool = dc.getDrawablePool(DrawableGroundAtmosphere.class);
        DrawableGroundAtmosphere drawable = DrawableGroundAtmosphere.obtain(pool);

        drawable.program = (GroundProgram) dc.getShaderProgram(GroundProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (GroundProgram) dc.putShaderProgram(GroundProgram.KEY, new GroundProgram(dc.resources));
        }

        drawable.lightDirection.set(this.activeLightDirection);
        drawable.globeRadius = dc.globe.getEquatorialRadius();

        // Use this layer's night image when the light location is different than the eye location.
        if (this.nightImageSource != null && this.lightLocation != null) {
            drawable.nightTexture = dc.getTexture(this.nightImageSource);
            if (drawable.nightTexture == null) {
                drawable.nightTexture = dc.retrieveTexture(this.nightImageSource);
            }
        } else {
            drawable.nightTexture = null;
        }

        dc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
    }

    // TODO move this into a basic tessellator implementation in World Wind
    // TODO tessellator and atmosphere needs the TriStripIndices - could we add these to BasicGlobe (needs to be on a static context)
    // TODO may need to switch the tessellation method anyway - geographic grid may produce artifacts at the poles
    protected static ShortBuffer assembleTriStripElements(int numLat, int numLon) {

        // Allocate a buffer to hold the indices.
        int count = ((numLat - 1) * numLon + (numLat - 2)) * 2;
        ShortBuffer result = ByteBuffer.allocateDirect(count * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        short[] index = new short[2];
        int vertex = 0;

        for (int latIndex = 0; latIndex < numLat - 1; latIndex++) {
            // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
            for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
                vertex = lonIndex + latIndex * numLon;
                index[0] = (short) (vertex + numLon);
                index[1] = (short) vertex;
                result.put(index);
            }

            // Insert indices to create 2 degenerate triangles:
            // - one for the end of the current row, and
            // - one for the beginning of the next row
            if (latIndex < numLat - 2) {
                index[0] = (short) vertex;
                index[1] = (short) ((latIndex + 2) * numLon);
                result.put(index);
            }
        }

        return (ShortBuffer) result.rewind();
    }
}