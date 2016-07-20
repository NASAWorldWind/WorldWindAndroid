/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import android.opengl.GLES20;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableTerrain;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;

public class DrawableGroundAtmosphere implements Drawable {

    public GroundProgram program;

    public Vec3 lightDirection = new Vec3();

    public double globeRadius;

    public Texture nightTexture;

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Sector fullSphereSector = new Sector().setFullSphere();

    private Pool<DrawableGroundAtmosphere> pool;

    public DrawableGroundAtmosphere() {
    }

    public static DrawableGroundAtmosphere obtain(Pool<DrawableGroundAtmosphere> pool) {
        DrawableGroundAtmosphere instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableGroundAtmosphere().setPool(pool);
    }

    private DrawableGroundAtmosphere setPool(Pool<DrawableGroundAtmosphere> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.program = null;
        this.nightTexture = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null || !this.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        // Use the draw context's globe.
        this.program.loadGlobeRadius(this.globeRadius);

        // Use the draw context's eye point.
        this.program.loadEyePoint(dc.eyePoint);

        // Use this layer's light direction.
        this.program.loadLightDirection(this.lightDirection);

        // Set up to use the shared tile tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);

        // Attempt to bind the night side texture to multi-texture unit 0.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);
        boolean textureBound = this.nightTexture != null && this.nightTexture.bindTexture(dc);

        for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) {
            // Get the drawable terrain associated with the draw context.
            DrawableTerrain terrain = dc.getDrawableTerrain(idx);

            // Use the terrain's vertex point attribute and vertex tex coord attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/) ||
                !terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)) {
                continue; // vertex buffer failed to bind
            }

            // Use the vertex origin for the terrain.
            Vec3 terrainOrigin = terrain.getVertexOrigin();
            this.program.loadVertexOrigin(terrainOrigin);

            // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
            this.mvpMatrix.set(dc.modelviewProjection);
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.program.loadModelviewProjection(this.mvpMatrix);

            // Use a tex coord matrix that registers the night texture correctly on each terrain.
            if (textureBound) {
                this.texCoordMatrix.set(this.nightTexture.getTexCoordTransform());
                this.texCoordMatrix.multiplyByTileTransform(terrain.getSector(), this.fullSphereSector);
                this.program.loadTexCoordMatrix(this.texCoordMatrix);
            }

            // Draw the terrain as triangles, multiplying the current fragment color by the program's secondary color.
            this.program.loadFragMode(AtmosphereProgram.FRAGMODE_SECONDARY);
            GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
            terrain.drawTriangles(dc);

            // Draw the terrain as triangles, adding the current fragment color to the program's primary color.
            this.program.loadFragMode(textureBound ?
                AtmosphereProgram.FRAGMODE_PRIMARY_TEX_BLEND : AtmosphereProgram.FRAGMODE_PRIMARY);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            terrain.drawTriangles(dc);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisableVertexAttribArray(1);
    }
}
