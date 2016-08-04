/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.opengl.GLES20;

import gov.nasa.worldwind.draw.DrawContext;

public class Framebuffer implements RenderResource {

    protected static final int[] UNINITIALIZED_NAME = new int[1];

    protected int[] framebufferName = UNINITIALIZED_NAME;

    public Framebuffer() {
    }

    @Override
    public void release(DrawContext dc) {
        if (this.framebufferName[0] != 0) {
            this.deleteFramebuffer(dc);
        }
    }

    public boolean bindFramebuffer(DrawContext dc) {
        if (this.framebufferName == UNINITIALIZED_NAME) {
            this.createFramebuffer(dc);
        }

        if (this.framebufferName[0] != 0) {
            dc.bindFramebuffer(this.framebufferName[0]);
        }

        return this.framebufferName[0] != 0;
    }

    public boolean attachTexture(DrawContext dc, Texture texture) {
        if (this.framebufferName == UNINITIALIZED_NAME) {
            this.createFramebuffer(dc);
        }

        if (this.framebufferName[0] != 0) {
            this.framebufferTexture(dc, texture);
        }

        return this.framebufferName[0] != 0;
    }

    public boolean isFramebufferComplete(DrawContext dc) {
        // Get the OpenGL framebuffer object status code.
        int e = this.framebufferStatus(dc);
        return e == GLES20.GL_FRAMEBUFFER_COMPLETE;
    }

    protected void createFramebuffer(DrawContext dc) {
        int currentFramebuffer = dc.currentFramebuffer();
        try {
            this.framebufferName = new int[1];
            // Create the OpenGL framebuffer object.
            GLES20.glGenFramebuffers(1, this.framebufferName, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.framebufferName[0]);
        } finally {
            // Restore the current OpenGL framebuffer object binding.
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, currentFramebuffer);
        }
    }

    protected void deleteFramebuffer(DrawContext dc) {
        GLES20.glDeleteFramebuffers(1, this.framebufferName, 0);
        this.framebufferName[0] = 0;
    }

    protected void framebufferTexture(DrawContext dc, Texture texture) {
        int currentFramebuffer = dc.currentFramebuffer();
        try {
            // Make the OpenGL framebuffer object the currently active framebuffer.
            dc.bindFramebuffer(this.framebufferName[0]);
            // Configure the texture as the framebuffer object's color attachment, or remove any color attachment if
            // the texture is null.
            int textureName = (texture != null) ? texture.getTextureName(dc) : 0;
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                textureName, 0 /*level*/);
        } finally {
            // Restore the current OpenGL framebuffer object binding.
            dc.bindFramebuffer(currentFramebuffer);
        }
    }

    protected int framebufferStatus(DrawContext dc) {
        int currentFramebuffer = dc.currentFramebuffer();
        try {
            // Make the OpenGL framebuffer object the currently active framebuffer.
            dc.bindFramebuffer(this.framebufferName[0]);
            // Get the OpenGL framebuffer object status code.
            return GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        } finally {
            // Restore the current OpenGL framebuffer object binding.
            dc.bindFramebuffer(currentFramebuffer);
        }
    }
}
