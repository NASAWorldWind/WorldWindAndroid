/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.render.BasicProgram;
import gov.nasa.worldwind.render.DrawContext;

public class ShowTessellationLayer extends AbstractLayer {

    protected Matrix4 mvpMatrix = new Matrix4();

    public ShowTessellationLayer() {
        super("Terrain Tessellation");
    }

    @Override
    protected void doRender(DrawContext dc) {

        BasicProgram program = (BasicProgram) dc.getGpuObjectCache().retrieveProgram(dc, BasicProgram.class);
        if (program == null) {
            return; // program is not in the GPU object cache yet
        }

        // Use World Wind's basic GLSL program.
        dc.useProgram(program);

        // Configure the program to draw opaque white fragments.
        program.enableTexture(false);
        program.loadColor(1, 1, 1, 1);

        // Suppress writes to the OpenGL depth buffer.
        GLES20.glDepthMask(false);

        // Get the draw context's tessellated terrain and modelview projection matrix.
        Terrain terrain = dc.getTerrain();

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(dc.getProjection());
            this.mvpMatrix.offsetProjectionDepth(-1.0e-3); // offset this layer's depth values toward the eye
            this.mvpMatrix.multiplyByMatrix(dc.getModelview());
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            program.loadModelviewProjection(this.mvpMatrix);

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            // Draw the terrain tile vertices as lines.
            terrain.drawTileLines(dc, idx);
        }

        // Restore default World Wind OpenGL state.
        GLES20.glDepthMask(true);
    }
}
