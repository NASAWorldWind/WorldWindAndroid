/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwindx.R;

public class AtmosphereLayer extends AbstractLayer {

    protected ImageSource nightImageSource;

    protected ImageOptions nightImageOptions;

    protected Location lightLocation;

    protected Vec3 activeLightDirection = new Vec3();

    private Sector fullSphereSector = new Sector().setFullSphere();

    private static final String VERTEX_POINTS_KEY = AtmosphereLayer.class.getName() + ".vertexPoints";

    private static final String TRI_STRIP_ELEMENTS_KEY = AtmosphereLayer.class.getName() + ".triStripElements";

    public AtmosphereLayer() {
        this.setDisplayName("Atmosphere");
        this.setPickEnabled(false);
        this.nightImageSource = ImageSource.fromResource(R.drawable.dnb_land_ocean_ice_2012);
        this.nightImageOptions = new ImageOptions(WorldWind.RGB_565);
    }

    public ImageSource getNightImageSource() {
        return this.nightImageSource;
    }

    public void setNightImageSource(ImageSource nightImageSource) {
        this.nightImageSource = nightImageSource;
    }

    public ImageOptions getNightImageOptions() {
        return this.nightImageOptions;
    }

    public void setNightImageOptions(ImageOptions nightImageOptions) {
        this.nightImageOptions = nightImageOptions;
    }

    public Location getLightLocation() {
        return this.lightLocation;
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
            rc.globe.geographicToCartesianNormal(rc.camera.latitude, rc.camera.longitude, this.activeLightDirection);
        }
    }

    protected void renderSky(RenderContext rc) {
        Pool<DrawableSkyAtmosphere> pool = rc.getDrawablePool(DrawableSkyAtmosphere.class);
        DrawableSkyAtmosphere drawable = DrawableSkyAtmosphere.obtain(pool);
        int size = 128;

        drawable.program = (SkyProgram) rc.getShaderProgram(SkyProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (SkyProgram) rc.putShaderProgram(SkyProgram.KEY, new SkyProgram(rc.resources));
        }

        drawable.vertexPoints = rc.getBufferObject(VERTEX_POINTS_KEY);
        if (drawable.vertexPoints == null) {
            drawable.vertexPoints = rc.putBufferObject(VERTEX_POINTS_KEY,
                this.assembleVertexPoints(rc, size, size, drawable.program.getAltitude()));
        }

        drawable.triStripElements = rc.getBufferObject(TRI_STRIP_ELEMENTS_KEY);
        if (drawable.triStripElements == null) {
            drawable.triStripElements = rc.putBufferObject(TRI_STRIP_ELEMENTS_KEY,
                this.assembleTriStripElements(size, size));
        }

        drawable.lightDirection.set(this.activeLightDirection);
        drawable.globeRadius = rc.globe.getEquatorialRadius();

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
                drawable.nightTexture = rc.retrieveTexture(this.nightImageSource, this.nightImageOptions);
            }
        } else {
            drawable.nightTexture = null;
        }

        rc.offerSurfaceDrawable(drawable, Double.POSITIVE_INFINITY /*z-order after all other surface drawables*/);
    }

    protected BufferObject assembleVertexPoints(RenderContext rc, int numLat, int numLon, double altitude) {
        int count = numLat * numLon;
        double[] altitudes = new double[count];
        Arrays.fill(altitudes, altitude);

        float[] points = new float[count * 3];
        rc.globe.geographicToCartesianGrid(this.fullSphereSector, numLat, numLon, altitudes, null, points, 3, 0);

        int size = points.length * 4;
        FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(points).rewind();

        return new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer);
    }

    // TODO move this into a basic tessellator implementation in World Wind
    // TODO tessellator and atmosphere needs the TriStripIndices - could we add these to BasicGlobe (needs to be on a static context)
    // TODO may need to switch the tessellation method anyway - geographic grid may produce artifacts at the poles
    protected BufferObject assembleTriStripElements(int numLat, int numLon) {
        // Allocate a buffer to hold the indices.
        int count = ((numLat - 1) * numLon + (numLat - 2)) * 2;
        short[] elements = new short[count];
        int pos = 0, vertex = 0;

        for (int latIndex = 0; latIndex < numLat - 1; latIndex++) {
            // Create a triangle strip joining each adjacent column of vertices, starting in the bottom left corner and
            // proceeding to the right. The first vertex starts with the left row of vertices and moves right to create
            // a counterclockwise winding order.
            for (int lonIndex = 0; lonIndex < numLon; lonIndex++) {
                vertex = lonIndex + latIndex * numLon;
                elements[pos++] = (short) (vertex + numLon);
                elements[pos++] = (short) vertex;
            }

            // Insert indices to create 2 degenerate triangles:
            // - one for the end of the current row, and
            // - one for the beginning of the next row
            if (latIndex < numLat - 2) {
                elements[pos++] = (short) vertex;
                elements[pos++] = (short) ((latIndex + 2) * numLon);
            }
        }

        int size = elements.length * 2;
        ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
        buffer.put(elements).rewind();

        return new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer);
    }
}