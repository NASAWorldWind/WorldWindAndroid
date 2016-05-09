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
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwindx.R;

public class AtmosphereLayer extends AbstractLayer {

    protected ImageSource nightImageSource;

    protected Location lightLocation;

    protected Vec3 activeLightDirection = new Vec3();

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
    protected void doRender(RenderContext rc) {
        // Compute the currently active light direction.
        this.determineLightDirection(rc);

        // Render the sky portion of the atmosphere.
        this.renderSky(rc);

        // Render the ground portion of the atmosphere.
        this.renderGround(rc);
    }

    protected void determineLightDirection(RenderContext rc) {
        // TODO Make light/sun direction an optional property of the WorldWindow and attach it to the RenderContext each frame
        // TODO RenderContext property defaults to the eye lat/lon like we have below
        if (this.lightLocation != null) {
            rc.globe.geographicToCartesianNormal(this.lightLocation.latitude, this.lightLocation.longitude, this.activeLightDirection);
        } else {
            rc.globe.geographicToCartesianNormal(rc.eyePosition.latitude, rc.eyePosition.longitude, this.activeLightDirection);
        }
    }

    protected void renderSky(RenderContext rc) {
        Pool<DrawableSkyAtmosphere> pool = rc.getDrawablePool(DrawableSkyAtmosphere.class);
        DrawableSkyAtmosphere drawable = DrawableSkyAtmosphere.obtain(pool);

        drawable.program = (SkyProgram) rc.getShaderProgram(SkyProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (SkyProgram) rc.putShaderProgram(SkyProgram.KEY, new SkyProgram(rc.resources));
        }

        drawable.lightDirection.set(this.activeLightDirection);
        drawable.globeRadius = rc.globe.getEquatorialRadius();

        int size = 128;
        if (drawable.vertexPoints == null) {
            int count = size * size;
            double[] array = new double[count];
            Arrays.fill(array, drawable.program.getAltitude());

            drawable.vertexPoints = ByteBuffer.allocateDirect(count * 12).order(ByteOrder.nativeOrder()).asFloatBuffer();
            rc.globe.geographicToCartesianGrid(this.fullSphereSector, size, size, array, null,
                drawable.vertexPoints, 3).rewind();
        }

        if (drawable.triStripElements == null) {
            drawable.triStripElements = assembleTriStripElements(size, size);
        }

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
    }

    protected void renderGround(RenderContext rc) {
        if (rc.terrain.getSector().isEmpty()) {
            return; // no terrain surface to render on
        }

        Pool<DrawableGroundAtmosphere> pool = rc.getDrawablePool(DrawableGroundAtmosphere.class);
        DrawableGroundAtmosphere drawable = DrawableGroundAtmosphere.obtain(pool);

        drawable.program = (GroundProgram) rc.getShaderProgram(GroundProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (GroundProgram) rc.putShaderProgram(GroundProgram.KEY, new GroundProgram(rc.resources));
        }

        drawable.lightDirection.set(this.activeLightDirection);
        drawable.globeRadius = rc.globe.getEquatorialRadius();

        // Use this layer's night image when the light location is different than the eye location.
        if (this.nightImageSource != null && this.lightLocation != null) {
            drawable.nightTexture = rc.getTexture(this.nightImageSource);
            if (drawable.nightTexture == null) {
                drawable.nightTexture = rc.retrieveTexture(this.nightImageSource);
            }
        } else {
            drawable.nightTexture = null;
        }

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
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