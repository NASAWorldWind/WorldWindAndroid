/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTexture;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;

public class DrawableSurfaceTexture implements Drawable, SurfaceTexture {

    public SurfaceTextureProgram program;

    public Sector sector = new Sector();

    public Texture texture;

    public Matrix3 texCoordMatrix = new Matrix3();

    private Pool<DrawableSurfaceTexture> pool;

    public DrawableSurfaceTexture() {
    }

    public static DrawableSurfaceTexture obtain(Pool<DrawableSurfaceTexture> pool) {
        DrawableSurfaceTexture instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableSurfaceTexture().setPool(pool);
    }

    private DrawableSurfaceTexture setPool(Pool<DrawableSurfaceTexture> pool) {
        this.pool = pool;
        return this;
    }

    public DrawableSurfaceTexture set(SurfaceTextureProgram program, Sector sector, Texture texture, Matrix3 texCoordMatrix) {
        this.program = program;
        this.texture = texture;

        if (sector != null) {
            this.sector.set(sector);
        } else {
            this.sector.setEmpty();
        }

        if (texCoordMatrix != null) {
            this.texCoordMatrix.set(texCoordMatrix);
        } else {
            this.texCoordMatrix.setToIdentity();
        }

        return this;
    }

    @Override
    public void recycle() {
        this.texture = null;
        this.program = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null) {
            return; // program unspecified
        }

        if (!this.program.useProgram(dc)) {
            return; // program failed to build
        }

        try {
            // Add this surface texture.
            this.program.surfaceTextures.add(this);

            // Add all surface textures that are contiguous in the drawable queue.
            Drawable next;
            while ((next = dc.peekDrawable()) != null && this.canBatchWith(next)) { // check if the drawable at the front of the queue can be batched
                this.program.surfaceTextures.add((SurfaceTexture) dc.pollDrawable()); // take it off the queue
            }

            // Draw the accumulated  surface textures.
            this.drawSurfaceTextures(dc);
        } finally {
            // Clear the program's accumulated surface textures.
            this.program.surfaceTextures.clear();
        }
    }

    protected void drawSurfaceTextures(DrawContext dc) {
        // Get the draw context's tessellated terrain.
        Terrain terrain = dc.terrain;

        // Set up to use the shared terrain tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        // Make multitexture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {
            // Get the terrain tile's sector, and keep a flag to ensure we apply the terrain tile's state at most once.
            Sector terrainSector = terrain.getTileSector(idx);
            boolean usingTerrainTileState = false;

            for (int jidx = 0, jlen = this.program.surfaceTextures.size(); jidx < jlen; jidx++) {
                // Get the surface texture and its sector.
                SurfaceTexture texture = this.program.surfaceTextures.get(jidx);
                Sector textureSector = texture.getSector();

                if (!textureSector.intersects(terrainSector)) {
                    continue; // texture does not intersect the terrain tile
                }

                if (!texture.bindTexture(dc)) {
                    continue; // texture failed to bind
                }

                if (!usingTerrainTileState) {
                    // Use the draw context's modelview projection matrix, transformed to the terrain tile's local
                    // coordinates.
                    Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
                    this.program.mvpMatrix.set(dc.modelviewProjection);
                    this.program.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
                    this.program.loadModelviewProjection();
                    // Use the terrain tile's vertex point attribute.
                    terrain.useVertexPointAttrib(dc, idx, 0);
                    // Suppress subsequent tile state application until the next terrain tile.
                    usingTerrainTileState = true;
                }

                // Use tex coord matrices that registers the surface texture correctly and mask terrain tile fragments
                // that fall outside the surface texture's sector.
                this.program.texCoordMatrix[0].set(texture.getTexCoordTransform());
                this.program.texCoordMatrix[0].multiplyByTileTransform(terrainSector, textureSector);
                this.program.texCoordMatrix[1].setToTileTransform(terrainSector, textureSector);
                this.program.loadTexCoordMatrix();

                // Draw the terrain tile as triangles.
                terrain.drawTileTriangles(dc, idx);
            }
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1);
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public Matrix3 getTexCoordTransform() {
        return this.texCoordMatrix;
    }

    @Override
    public boolean bindTexture(DrawContext dc) {
        return (this.texture != null) && this.texture.bindTexture(dc);
    }

    protected boolean canBatchWith(Drawable that) {
        return this.getClass() == that.getClass() && this.program == ((DrawableSurfaceTexture) that).program;
    }
}
