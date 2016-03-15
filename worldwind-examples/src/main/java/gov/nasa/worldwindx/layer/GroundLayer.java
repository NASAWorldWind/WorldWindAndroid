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
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;
import gov.nasa.worldwindx.render.GroundProgram;

public class GroundLayer extends AbstractLayer {

    protected int nightImageId;

    protected Matrix3 texCoordMatrix = new Matrix3();

    public GroundLayer(@DrawableRes int nightImageId) {
        super("Atmosphere (Ground)");
        this.nightImageId = nightImageId;
    }

    @Override
    protected void doRender(DrawContext dc) {

        GroundProgram program = (GroundProgram) dc.getGpuObjectCache().retrieveProgram(dc,
            GroundProgram.class);
        if (program == null) {
            return;
        }

        // Use this layer's OpenGL program and OpenGL texture.
        dc.useProgram(program);

        // Use the draw context's modelview projection matrix.
        program.loadModelviewProjection(dc.getModelviewProjection());

        // Apply the draw context's properties to the atmosphere program.
        program.loadUniforms(dc);

        // Attempt to use this layer's night texture.
        GpuTexture texture = dc.getGpuObjectCache().retrieveTexture(dc, this.nightImageId);
        if (texture != null) {
            // Bind the texture.
            program.loadTextureSampler(0); // GL_TEXTURE0
            dc.bindTexture(GLES20.GL_TEXTURE0, texture);

            // Use the texture's transform matrix.
            this.texCoordMatrix.setToIdentity();
            texture.applyTextureTransform(this.texCoordMatrix);
            program.loadTextureTransform(this.texCoordMatrix);
        }

        // Specify the vertex point and tex coord attributes.
        Buffer vertices = (Buffer) dc.getUserProperty("tessellatorVertices");
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glEnableVertexAttribArray(1);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 20, vertices.position(0));
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 20, vertices.position(3));

        // Multiply the current fragment color by the program's secondary color.
        Buffer triStrip = (Buffer) dc.getUserProperty("tessellatorTriStrip");
        program.loadFragColor(GroundProgram.FRAGCOLOR_SECONDARY);
        GLES20.glBlendFunc(GLES20.GL_DST_COLOR, GLES20.GL_ZERO);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, triStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, triStrip);

        // Add the current fragment color to the program's primary color.
        program.loadFragColor(GroundProgram.FRAGCOLOR_PRIMARY_TEX_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, triStrip.remaining(), GLES20.GL_UNSIGNED_SHORT, triStrip);

        // Restore the default World Wind OpenGL state.
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisableVertexAttribArray(1);
    }
}