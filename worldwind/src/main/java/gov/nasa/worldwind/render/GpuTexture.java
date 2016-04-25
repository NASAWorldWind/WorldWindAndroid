/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;

public class GpuTexture implements RenderResource { // TODO rename as Texture

    protected int textureId;

    protected int textureWidth;

    protected int textureHeight;

    protected int textureFormat;

    protected int imageWidth;

    protected int imageHeight;

    protected int imageFormat;

    protected int imageByteCount;

    protected Bitmap imageBitmap;

    public GpuTexture() {
    }

    public GpuTexture(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "constructor", "invalidBitmap"));
        }

        this.setImage(bitmap);
    }

    public int getTextureWidth() {
        return this.textureWidth;
    }

    public int getTextureHeight() {
        return this.textureHeight;
    }

    public int getImageWidth() {
        return this.imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight;
    }

    public int getImageByteCount() {
        return this.imageByteCount;
    }

    public void setImage(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "setImage", "invalidBitmap"));
        }

        this.imageWidth = bitmap.getWidth();
        this.imageHeight = bitmap.getHeight();
        this.imageFormat = GLUtils.getInternalFormat(bitmap);
        this.imageByteCount = bitmap.getByteCount();
        this.imageBitmap = bitmap;
    }

    @Override
    public void release(DrawContext dc) {
        this.deleteTexture(dc);
        this.imageBitmap = null; // imageBitmap can be non-null if the texture has not been bound
    }

    public boolean bindTexture(DrawContext dc) {
        if (this.imageBitmap != null) {
            this.loadImageBitmap(dc);
            this.imageBitmap = null;
        }

        if (this.textureId != 0) {
            dc.bindTexture(this.textureId);
        }

        return this.textureId != 0;
    }

    public boolean applyTexCoordTransform(Matrix3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "applyTexCoordTransform", "missingResult"));
        }

        if (this.textureId != 0) {
            result.multiplyByVerticalFlip();
        }

        return this.textureId != 0;
    }

    protected void loadImageBitmap(DrawContext dc) {
        int currentTexture = dc.currentTexture();

        try {

            // Create the OpenGL texture 2D object.
            if (this.textureId == 0) {
                this.createTexture(dc);
            }

            // Make the OpenGL texture 2D object bound to the current multitexture unit.
            dc.bindTexture(this.textureId);
            // Load the current imageBitmap as the OpenGL texture 2D object's image data.
            this.loadTexImage(dc);

        } catch (Exception e) {

            // The bitmap could not be used as image data for an OpenGL texture 2D object. Delete the texture object
            // to ensure that calls to bindTexture and applyTexCoordTransform fail.
            this.deleteTexture(dc);
            Logger.logMessage(Logger.ERROR, "GpuTexture", "loadTexImage", "Exception attempting to load texture image", e);

        } finally {

            // Restore the current OpenGL texture binding.
            dc.bindTexture(currentTexture);
        }
    }

    protected void createTexture(DrawContext dc) {
        int[] newTexture = new int[1];
        GLES20.glGenTextures(1, newTexture, 0);
        this.textureId = newTexture[0];
    }

    protected void deleteTexture(DrawContext dc) {
        if (this.textureId != 0) {
            GLES20.glDeleteTextures(1, new int[]{this.textureId}, 0);
            this.textureId = 0;
        }
    }

    protected void loadTexImage(DrawContext dc) {
        boolean isPowerOfTwo = WWMath.isPowerOfTwo(this.imageWidth) && WWMath.isPowerOfTwo(this.imageHeight);

        // Define the OpenGL texture 2D object's image data for level zero. Use the higher performance texture
        // update function texSubImage2D when the texture's dimensions and internal format have not changed.
        if (this.textureWidth != this.imageWidth ||
            this.textureHeight != this.imageHeight ||
            this.textureFormat != this.imageFormat) {
            this.textureWidth = this.imageWidth;
            this.textureHeight = this.imageHeight;
            this.textureFormat = this.imageFormat;
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                isPowerOfTwo ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.imageBitmap, 0);
        } else {
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, this.imageBitmap);
        }

        // Generate a complete set of mipmaps for the OpenGL texture 2D object's image data levels 1 through N.
        if (isPowerOfTwo) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
    }
}
