/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import java.util.ArrayList;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.SurfaceTexture;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;

public class DrawableSurfaceTexture implements Drawable, SurfaceTexture {

    public SurfaceTextureProgram program;

    public Sector sector = new Sector();

    public Color color = new Color();

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
        this.color.set(1, 1, 1, 1);
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
        if (this.program == null || !this.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        // Accumulate surface textures in the draw context's scratch list.
        // TODO accumulate in a geospatial quadtree
        ArrayList<Object> scratchList = dc.scratchList();

        try {
            // Add this surface texture.
            scratchList.add(this);

            // Add all surface textures that are contiguous in the drawable queue.
            Drawable next;
            while ((next = dc.peekDrawable()) != null && this.canBatchWith(next)) { // check if the drawable at the front of the queue can be batched
                scratchList.add(dc.pollDrawable()); // take it off the queue
            }

            // Draw the accumulated surface textures.
            this.drawSurfaceTextures(dc);
        } finally {
            // Clear the accumulated surface textures.
            scratchList.clear();
        }
    }

    protected void drawSurfaceTextures(DrawContext dc) {
        // Use the draw context's pick mode.
        this.program.enablePickMode(dc.pickMode);

        // Enable the program to display surface textures from multitexture unit 0.
        this.program.enableTexture(true);
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        // Set up to use vertex tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);

        // Surface textures have been accumulated in the draw context's scratch list.
        ArrayList<Object> scratchList = dc.scratchList();

        for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) {
            // Get the drawable terrain associated with the draw context.
            DrawableTerrain terrain = dc.getDrawableTerrain(idx);

            // Get the terrain's attributes, and keep a flag to ensure we apply the terrain's attributes at most once.
            Sector terrainSector = terrain.getSector();
            Vec3 terrainOrigin = terrain.getVertexOrigin();
            boolean usingTerrainAttrs = false;

            for (int jidx = 0, jlen = scratchList.size(); jidx < jlen; jidx++) {
                // Get the surface texture and its sector.
                DrawableSurfaceTexture texture = (DrawableSurfaceTexture) scratchList.get(jidx);
                Sector textureSector = texture.sector;

                if (!textureSector.intersects(terrainSector)) {
                    continue; // texture does not intersect the terrain
                }

                if (!texture.bindTexture(dc)) {
                    continue; // texture failed to bind
                }

                // Use the terrain's vertex point attribute and vertex tex coord attribute.
                if (!usingTerrainAttrs &&
                    terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/) &&
                    terrain.useVertexTexCoordAttrib(dc, 1 /*vertexTexCoord*/)) {
                    // Suppress subsequent tile state application until the next terrain.
                    usingTerrainAttrs = true;
                    // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
                    this.program.mvpMatrix.set(dc.modelviewProjection);
                    this.program.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
                    this.program.loadModelviewProjection();
                }

                if (!usingTerrainAttrs) {
                    continue; // terrain vertex attribute failed to bind
                }

                // Use tex coord matrices that register the surface texture correctly and mask terrain fragments that
                // fall outside the surface texture's sector.
                this.program.texCoordMatrix[0].set(texture.getTexCoordTransform());
                this.program.texCoordMatrix[0].multiplyByTileTransform(terrainSector, textureSector);
                this.program.texCoordMatrix[1].setToTileTransform(terrainSector, textureSector);
                this.program.loadTexCoordMatrix();

                // Use the surface texture's RGBA color.
                this.program.loadColor(texture.color);

                // Draw the terrain as triangles.
                terrain.drawTriangles(dc);
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
