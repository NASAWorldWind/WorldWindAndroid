/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.layer;

import android.opengl.GLES20;

import java.nio.Buffer;

import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.BasicProgram;
import gov.nasa.worldwind.render.DrawContext;

public class WireframeLayer extends AbstractLayer {

    public WireframeLayer() {
        super("Wireframe");
    }

    @Override
    protected void doRender(DrawContext dc) {

        BasicProgram program = (BasicProgram) dc.getGpuObjectCache().retrieveProgram(dc, BasicProgram.class);
        if (program == null) {
            return;
        }

        // Use this layer's OpenGL program.
        dc.useProgram(program);

        // Use the draw context's modelview projection matrix.
        program.loadModelviewProjection(dc.getModelviewProjection());

        // Draw in a solid white color.
        program.enableTexture(false);
        program.loadColor(1, 1, 1, 1);

        // Specify the vertex point and tex coord attributes.
        Buffer vertices = (Buffer) dc.getUserProperty("tessellatorVertices");
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 20, vertices.position(0));

        // Draw the tessellation geometry.
        Buffer triStrip = (Buffer) dc.getUserProperty("tessellatorLines");
        GLES20.glDrawElements(GLES20.GL_LINES, triStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, triStrip);
    }
}
