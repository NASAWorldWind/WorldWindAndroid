/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.util.Logger;

public class BasicSurfaceTileRenderer implements SurfaceTileRenderer {

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix3[] texCoordMatrix = {new Matrix3(), new Matrix3()};

    protected List<SurfaceTile> intersectingSurfaceTiles = new ArrayList<>();

    public BasicSurfaceTileRenderer() {
    }

    @Override
    public void renderTile(DrawContext dc, SurfaceTile texture) {
        if (texture == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicSurfaceTileRenderer", "renderTile", "missingTile"));
        }

        if (!texture.bindTexture(dc, GLES20.GL_TEXTURE0)) {
            return; // surface tile's texture is not in the GPU object cache yet
        }

        SurfaceTileProgram program = (SurfaceTileProgram) dc.getGpuObjectCache().retrieveProgram(dc, SurfaceTileProgram.class);
        if (program == null) {
            return; // program is not in the GPU object cache yet
        }

        // Use World Wind's surface tile GLSL program.
        dc.useProgram(program);

        // Get the draw context's tessellated terrain and the surface tile's sector.
        Terrain terrain = dc.getTerrain();
        Sector textureSector = texture.getSector();

        // Set up to use the shared terrain tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Skip terrain tiles that do not intersect the surface tile.
            Sector terrainSector = terrain.getTileSector(idx);
            if (!terrainSector.intersects(textureSector)) {
                continue;
            }

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(dc.getModelviewProjection());
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            program.loadModelviewProjection(this.mvpMatrix);

            // Use tex coord matrices that register the surface tile correctly and mask terrain tile fragments that
            // fall outside the surface tile's sector.
            this.texCoordMatrix[0].setToIdentity();
            this.texCoordMatrix[1].setToIdentity();
            texture.applyTexCoordTransform(dc, this.texCoordMatrix[0]);
            terrain.applyTexCoordTransform(idx, textureSector, this.texCoordMatrix[0]);
            terrain.applyTexCoordTransform(idx, textureSector, this.texCoordMatrix[1]);
            program.loadTexCoordMatrix(this.texCoordMatrix);

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            // Draw the terrain tile vertices as triangles.
            terrain.drawTileTriangles(dc, idx);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1);
    }

    @Override
    public void renderTiles(DrawContext dc, Iterable<? extends SurfaceTile> surfaceTiles) {
        if (surfaceTiles == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicSurfaceTileRenderer", "renderTiles", "missingList"));
        }

        SurfaceTileProgram program = (SurfaceTileProgram) dc.getGpuObjectCache().retrieveProgram(dc, SurfaceTileProgram.class);
        if (program == null) {
            return; // program is not in the GPU object cache yet
        }

        // Use World Wind's surface tile GLSL program.
        dc.useProgram(program);

        // Get the draw context's tessellated terrain.
        Terrain terrain = dc.getTerrain();

        // Set up to use the shared terrain tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Collect the surface tiles that intersect the terrain tile.
            Sector terrainSector = terrain.getTileSector(idx);
            this.intersectingSurfaceTiles.clear();
            for (SurfaceTile surfaceTile : surfaceTiles) {
                if (terrainSector.intersects(surfaceTile.getSector())) {
                    this.intersectingSurfaceTiles.add(surfaceTile);
                }
            }

            // Skip terrain tiles that do not intersect any surface tile.
            if (this.intersectingSurfaceTiles.isEmpty()) {
                continue;
            }

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(dc.getModelviewProjection());
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            program.loadModelviewProjection(this.mvpMatrix);

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            for (SurfaceTile texture : this.intersectingSurfaceTiles) {

                if (!texture.bindTexture(dc, GLES20.GL_TEXTURE0)) {
                    continue; // surface tile's texture is not in the GPU object cache yet
                }

                // Get the surface tile's sector.
                Sector textureSector = texture.getSector();

                // Use tex coord matrices that register the surface tile correctly and mask terrain tile fragments that
                // fall outside the surface tile's sector.
                this.texCoordMatrix[0].setToIdentity();
                this.texCoordMatrix[1].setToIdentity();
                texture.applyTexCoordTransform(dc, this.texCoordMatrix[0]);
                terrain.applyTexCoordTransform(idx, textureSector, this.texCoordMatrix[0]);
                terrain.applyTexCoordTransform(idx, textureSector, this.texCoordMatrix[1]);
                program.loadTexCoordMatrix(this.texCoordMatrix);

                // Draw the terrain tile vertices as triangles.
                terrain.drawTileTriangles(dc, idx);
            }
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1);
        this.intersectingSurfaceTiles.clear();
    }
}
