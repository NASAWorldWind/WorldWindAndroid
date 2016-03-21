/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

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

        // Get the draw context's tessellated terrain and modelview projection matrix.
        Terrain terrain = dc.getTerrain();
        Matrix4 dcmvp = dc.getModelviewProjection();

        for (int tileIdx = 0; tileIdx < terrain.getTileCount(); tileIdx++) {

            // Use the draw context's modelview projection matrix, offset by the tile's origin.
            Vec3 origin = terrain.getTileOrigin(tileIdx);
            this.mvpMatrix.set(dcmvp).multiplyByTranslation(origin.x, origin.y, origin.z);
            program.loadModelviewProjection(this.mvpMatrix);

            // Use the tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, tileIdx, 0);

            // Draw the tile vertices as lines.
            terrain.drawTileLines(dc, tileIdx);
        }
    }
}
