/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import android.opengl.GLES20;

import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;

public class DrawableGroundAtmosphere implements Drawable {

    protected static final Pool<DrawableGroundAtmosphere> pool = new SynchronizedPool<>(); // acquire and are release called in separate threads

    protected GroundProgram program;

    protected Vec3 lightDirection = new Vec3();

    protected Texture nightTexture;

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Sector fullSphereSector = new Sector().setFullSphere();

    protected DrawableGroundAtmosphere() {
    }

    public static DrawableGroundAtmosphere obtain(GroundProgram program, Vec3 lightDirection, Texture nightTexture) {
        DrawableGroundAtmosphere instance = pool.acquire(); // get an instance from the pool
        if (instance == null) {
            instance = new DrawableGroundAtmosphere();
        }

        instance.program = program;
        instance.nightTexture = nightTexture;

        if (lightDirection != null) {
            instance.lightDirection.set(lightDirection);
        } else {
            instance.lightDirection.set(0, 0, 1);
        }

        return instance;
    }

    @Override
    public void recycle() {
        this.program = null;
        this.nightTexture = null;
        pool.release(this); // return this instance to the pool
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null) {
            return; // program unspecified
        }

        if (!this.program.useProgram(dc)) {
            return; // program failed to build
        }

        // Use the draw context's globe.
        this.program.loadGlobeRadius(dc.globe.getEquatorialRadius()); // TODO the Globe is rendering state

        // Use the draw context's eye point.
        this.program.loadEyePoint(dc.eyePoint);

        // Use this layer's light direction.
        this.program.loadLightDirection(this.lightDirection);

        // Get the draw context's tessellated terrain.
        Terrain terrain = dc.terrain;

        // Set up to use the shared tile tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        // Attempt to bind the night side texture to multi-texture unit 0.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);
        boolean textureBound = this.nightTexture != null && this.nightTexture.bindTexture(dc);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Use the vertex origin for the terrain tile.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.program.loadVertexOrigin(terrainOrigin);

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            this.mvpMatrix.set(dc.modelviewProjection);
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.program.loadModelviewProjection(this.mvpMatrix);

            // Use a tex coord matrix that registers the night texture correctly on each terrain tile.
            if (textureBound) {
                this.texCoordMatrix.set(this.nightTexture.getTexCoordTransform());
                this.texCoordMatrix.multiplyByTileTransform(terrain.getTileSector(idx), this.fullSphereSector);
                this.program.loadTexCoordMatrix(this.texCoordMatrix);
            }

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            // Draw the terrain tile as triangles, multiplying the current fragment color by the program's secondary color.
            this.program.loadFragMode(AtmosphereProgram.FRAGMODE_GROUND_SECONDARY);
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
            terrain.drawTileTriangles(dc, idx);

            // Draw the terrain tile as triangles, adding the current fragment color to the program's primary color.
            this.program.loadFragMode(textureBound ?
                AtmosphereProgram.FRAGMODE_GROUND_PRIMARY_TEX_BLEND : AtmosphereProgram.FRAGMODE_GROUND_PRIMARY);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            terrain.drawTileTriangles(dc, idx);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisableVertexAttribArray(1);
    }
}
