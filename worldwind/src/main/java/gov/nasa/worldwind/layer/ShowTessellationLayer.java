/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.DrawContext;

public class ShowTessellationLayer extends AbstractLayer {

    protected Matrix4 offsetMvpMatrix = new Matrix4();

    protected Matrix4 mvpMatrix = new Matrix4();

    public ShowTessellationLayer() {
        super("Terrain Tessellation");
    }

    @Override
    protected void doRender(DrawContext dc) {

        Terrain terrain = dc.terrain;
        if (terrain.getTileCount() == 0) {
            return; // no terrain to render
        }

        // Use World Wind's basic GLSL program.
        BasicShaderProgram program = (BasicShaderProgram) dc.getShaderProgram(BasicShaderProgram.KEY);
        if (program == null) {
            program = (BasicShaderProgram) dc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(dc.resources));
        }

        if (!program.useProgram(dc)) {
            return; // program failed to build
        }

        // Configure the program to draw opaque white fragments.
        program.enableTexture(false);
        program.loadColor(1, 1, 1, 1);

        // Suppress writes to the OpenGL depth buffer.
        GLES20.glDepthMask(false);

        // Compute the portion of the modelview projection matrix that remains constant for each tile.
        this.offsetMvpMatrix.set(dc.projection);
        this.offsetMvpMatrix.offsetProjectionDepth(-1.0e-3); // offset this layer's depth values toward the eye
        this.offsetMvpMatrix.multiplyByMatrix(dc.modelview);

        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(this.offsetMvpMatrix);
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
