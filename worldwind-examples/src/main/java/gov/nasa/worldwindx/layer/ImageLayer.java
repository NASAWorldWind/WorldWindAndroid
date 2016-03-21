/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.layer;

import android.opengl.GLES20;
import android.support.annotation.DrawableRes;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.BasicProgram;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;

public class ImageLayer extends AbstractLayer {

    protected Sector sector;

    protected int imageId;

    protected Matrix4 mvpMatrix = new Matrix4();

    protected Matrix3 texCoordMatrix = new Matrix3();

    public ImageLayer(Sector sector, @DrawableRes int imageId) {
        super("Simple Image Layer");
        this.sector = sector;
        this.imageId = imageId;
    }

    @Override
    protected void doRender(DrawContext dc) {

        BasicProgram program = (BasicProgram) dc.getGpuObjectCache().retrieveProgram(dc, BasicProgram.class);
        if (program == null) {
            return; // program is not in the GPU object cache yet
        }

        GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.imageId);
        if (texture == null) {
            return; // texture is not in the GPU object cache yet
        }

        // Use World Wind's basic GLSL program.
        dc.useProgram(program);

        // Use this layer's texture.
        program.enableTexture(true);
        program.loadTextureSampler(0); // GL_TEXTURE0
        program.loadColor(1, 1, 1, 1); // don't modify texture fragment colors
        dc.bindTexture(GLES20.GL_TEXTURE0, texture);

        // Get the draw context's tessellated terrain and modelview projection matrix.
        Terrain terrain = dc.getTerrain();
        Matrix4 dcmvp = dc.getModelviewProjection();

        // Set up to use the shared tile tex coord attributes.
        GLES20.glEnableVertexAttribArray(1);
        terrain.useVertexTexCoordAttrib(dc, 1);

        for (int tileIdx = 0; tileIdx < terrain.getTileCount(); tileIdx++) {

            // Skip terrain tiles that do not intersect the image's sector.
            if (!terrain.getTileSector(tileIdx).intersects(this.sector)) {
                continue;
            }

            // Use the draw context's modelview projection matrix, offset by the tile's origin.
            Vec3 origin = terrain.getTileOrigin(tileIdx);
            this.mvpMatrix.set(dcmvp).multiplyByTranslation(origin.x, origin.y, origin.z);
            program.loadModelviewProjection(this.mvpMatrix);

            // Use the texture's transform matrix, scaled and translated by the tile's coordinates.
            this.texCoordMatrix.setToIdentity();
            texture.applyTexCoordTransform(this.texCoordMatrix);
            terrain.applyTexCoordTransform(tileIdx, this.sector, this.texCoordMatrix);
            program.loadTextureTransform(this.texCoordMatrix);

            // Use the tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, tileIdx, 0);

            // Draw the tile vertices as triangles.
            terrain.drawTileTriangles(dc, tileIdx);
        }

        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1);
    }
}
