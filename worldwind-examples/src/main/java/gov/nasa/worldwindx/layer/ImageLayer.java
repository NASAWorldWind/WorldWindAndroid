/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.layer;

import android.opengl.GLES20;
import android.support.annotation.DrawableRes;

import java.nio.Buffer;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.BasicProgram;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;

public class ImageLayer extends AbstractLayer {

    protected int imageId;

    protected Matrix3 texCoordMatrix = new Matrix3();

    public ImageLayer(@DrawableRes int imageId) {
        super("Simple Image Layer");
        this.imageId = imageId;
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

        // Attempt to use this layer's texture.
        GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.imageId);
        if (texture != null) {
            // Bind the texture.
            program.loadTextureSampler(0); // GL_TEXTURE0
            dc.bindTexture(GLES20.GL_TEXTURE0, texture);

            // Use the texture's transform matrix.
            this.texCoordMatrix.setToIdentity();
            texture.applyTextureTransform(this.texCoordMatrix);
            program.loadTextureTransform(this.texCoordMatrix);
        }

        // Set up to use the texture when it's available.
        program.enableTexture(texture != null);
        program.loadColor(1, 1, 1, 1);

        // Specify the vertex point and tex coord attributes.
        Buffer vertices = (Buffer) dc.getUserProperty("tessellatorVertices");
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glEnableVertexAttribArray(1);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 20, vertices.position(0));
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 20, vertices.position(3));

        // Draw the tessellation geometry.
        Buffer triStrip = (Buffer) dc.getUserProperty("tessellatorTriStrip");
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, triStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, triStrip);

        // Restore the default World Wind OpenGL state.
        GLES20.glDisableVertexAttribArray(1);
    }
}
