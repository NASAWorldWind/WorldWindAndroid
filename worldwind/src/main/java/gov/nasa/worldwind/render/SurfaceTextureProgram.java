/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.content.res.Resources;
import android.opengl.GLES20;

import java.util.ArrayList;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

// TODO Try accumulating surface tile state (texCoordMatrix, texSampler), loading uniforms once, then loading a uniform
// TODO index to select the state for a surface tile. This reduces the uniform calls when many surface tiles intersect
// TODO one terrain tile.
// TODO Try class representing transform with a specific scale+translate object that can be uploaded to a GLSL vec4
public class SurfaceTextureProgram extends ShaderProgram {

    public static final Object KEY = SurfaceTextureProgram.class.getName();

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix3[] texCoordMatrix = {new Matrix3(), new Matrix3()};

    protected float[] mvpMatrixArray = new float[16];

    protected float[] texCoordMatrixArray = new float[9 * 2];

    protected ArrayList<SurfaceTexture> surfaceTextures = new ArrayList<>();

    protected ArrayList<SurfaceTexture> intersectingTextures = new ArrayList<>();

    public SurfaceTextureProgram(Resources resources) {
        try {
            String vs = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_surfacetextureprogram_vert);
            String fs = WWUtil.readResourceAsText(resources, R.raw.gov_nasa_worldwind_surfacetextureprogram_frag);
            this.setProgramSources(vs, fs);
            this.setAttribBindings("vertexPoint", "vertexTexCoord");
        } catch (Exception logged) {
            Logger.logMessage(Logger.ERROR, "SurfaceTextureProgram", "constructor", "errorReadingProgramSource", logged);
        }
    }

    protected void initProgram(DrawContext dc) {
        this.mvpMatrixId = GLES20.glGetUniformLocation(this.programId, "mvpMatrix");
        Matrix4 identity4x4 = new Matrix4(); // 4 x 4 identity matrix
        identity4x4.transposeToArray(this.mvpMatrixArray, 0);
        GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.mvpMatrixArray, 0);

        this.texCoordMatrixId = GLES20.glGetUniformLocation(this.programId, "texCoordMatrix");
        Matrix3 identity3x3 = new Matrix3(); // 3 x 3 identity matrix
        identity3x3.transposeToArray(this.texCoordMatrixArray, 0);
        identity3x3.transposeToArray(this.texCoordMatrixArray, 9);
        GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.texCoordMatrixArray, 0);

        this.texSamplerId = GLES20.glGetUniformLocation(this.programId, "texSampler");
        GLES20.glUniform1i(this.texSamplerId, 0); // GL_TEXTURE0
    }

    public void addSurfaceTexture(SurfaceTexture surfaceTexture) {
        if (surfaceTexture != null) {
            this.surfaceTextures.add(surfaceTexture);
        }
    }

    public void draw(DrawContext dc) {
        // Get the draw context's tessellated terrain.
        Terrain terrain = dc.terrain;

        // Set up to use the shared terrain tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        // Make multitexture unit 0 active.
        dc.activeTextureUnit(GLES20.GL_TEXTURE0);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {
            // Collect the surface textures that intersect the terrain tile.
            Sector terrainSector = terrain.getTileSector(idx);
            this.intersectingTextures.clear();
            for (int jidx = 0, jlen = this.surfaceTextures.size(); jidx < jlen; jidx++) {
                SurfaceTexture texture = this.surfaceTextures.get(jidx);
                if (terrainSector.intersects(texture.getSector())) {
                    this.intersectingTextures.add(texture);
                }
            }

            // Skip terrain tiles that do not intersect any surface texture.
            if (this.intersectingTextures.isEmpty()) {
                continue;
            }

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(dc.modelviewProjection);
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.mvpMatrix.transposeToArray(this.mvpMatrixArray, 0);
            GLES20.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.mvpMatrixArray, 0);

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            for (int jidx = 0, jlen = this.intersectingTextures.size(); jidx < jlen; jidx++) {
                SurfaceTexture texture = this.intersectingTextures.get(jidx);
                if (!texture.bindTexture(dc)) {
                    continue; // texture failed to bind
                }

                // Get the surface texture's sector.
                Sector textureSector = texture.getSector();

                // Use tex coord matrices that registers the surface texture correctly and mask terrain tile fragments
                // that fall outside the surface texture's sector.
                this.texCoordMatrix[0].set(texture.getTexCoordTransform());
                this.texCoordMatrix[0].multiplyByTileTransform(terrainSector, textureSector);
                this.texCoordMatrix[0].transposeToArray(this.texCoordMatrixArray, 0);
                this.texCoordMatrix[1].setToTileTransform(terrainSector, textureSector);
                this.texCoordMatrix[1].transposeToArray(this.texCoordMatrixArray, 9);
                GLES20.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.texCoordMatrixArray, 0);

                // Draw the terrain tile as triangles.
                terrain.drawTileTriangles(dc, idx);
            }
        }
    }

    public void clear(DrawContext dc) {
        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1);

        // Clear references to objects used during drawing.
        this.surfaceTextures.clear();
        this.intersectingTextures.clear();
    }
}
