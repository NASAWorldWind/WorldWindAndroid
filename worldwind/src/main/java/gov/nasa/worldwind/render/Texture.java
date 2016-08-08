/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;

public class Texture implements RenderResource {

    protected static final int[] UNINITIALIZED_NAME = new int[1];

    protected int[] textureName = UNINITIALIZED_NAME;

    protected int textureWidth;

    protected int textureHeight;

    protected int textureFormat;

    protected int textureByteCount;

    protected Matrix3 texCoordTransform = new Matrix3();

    protected Bitmap imageBitmap;

    protected boolean haveMipmaps;

    private boolean pickMode;

    public Texture(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Texture", "constructor", (bitmap == null) ? "missingBitmap" : "invalidBitmap"));
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int format = GLUtils.getInternalFormat(bitmap);
        int type = GLUtils.getType(bitmap);

        this.textureWidth = width;
        this.textureHeight = height;
        this.textureFormat = format;
        this.textureByteCount = estimateByteCount(width, height, format, type);
        this.texCoordTransform.setToVerticalFlip();
        this.imageBitmap = bitmap;
    }

    public Texture(int width, int height, int format) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Texture", "constructor", "invalidWidthOrHeight"));
        }

        this.textureWidth = width;
        this.textureHeight = height;
        this.textureFormat = format;
        this.textureByteCount = estimateByteCount(width, height, format, GLES20.GL_UNSIGNED_BYTE);
        this.texCoordTransform.setToIdentity();
    }

    public int getWidth() {
        return this.textureWidth;
    }

    public int getHeight() {
        return this.textureHeight;
    }

    public int getByteCount() {
        return this.textureByteCount;
    }

    public Matrix3 getTexCoordTransform() {
        return this.texCoordTransform;
    }

    @Override
    public void release(DrawContext dc) {
        if (this.textureName[0] != 0) {
            this.deleteTexture(dc);
        }

        if (this.imageBitmap != null) {
            this.imageBitmap = null; // imageBitmap can be non-null if the texture has never been used
        }
    }

    public int getTextureName(DrawContext dc) {
        if (this.textureName == UNINITIALIZED_NAME) {
            this.createTexture(dc);
        }

        return this.textureName[0];
    }

    public boolean bindTexture(DrawContext dc) {
        if (this.textureName == UNINITIALIZED_NAME) {
            this.createTexture(dc);
        }

        if (this.textureName[0] != 0) {
            dc.bindTexture(this.textureName[0]);
        }

        if (this.textureName[0] != 0 && this.pickMode != dc.pickMode) {
            this.setTexParameters(dc);
            this.pickMode = dc.pickMode;
        }

        return this.textureName[0] != 0;
    }

    protected void createTexture(DrawContext dc) {
        int currentTexture = dc.currentTexture();
        try {
            this.textureName = new int[1];
            // Create the OpenGL texture 2D object.
            GLES20.glGenTextures(1, this.textureName, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureName[0]);
            // Configure the texture object's filtering modes and wrap modes.
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // Specify the texture object's image data, either by loading a bitmap or by allocating an empty image.
            if (this.imageBitmap != null) {
                this.loadTexImage(dc, this.imageBitmap);
                this.imageBitmap = null;
            } else {
                this.allocTexImage(dc);
            }
        } finally {
            // Restore the current OpenGL texture object binding.
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, currentTexture);
        }
    }

    protected void deleteTexture(DrawContext dc) {
        GLES20.glDeleteTextures(1, this.textureName, 0);
        this.textureName[0] = 0;
    }

    protected void allocTexImage(DrawContext dc) {
        // Allocate texture memory for the OpenGL texture 2D object. The texture memory is initialized with 0.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0 /*level*/,
            this.textureFormat, this.textureWidth, this.textureHeight, 0 /*border*/,
            this.textureFormat, GLES20.GL_UNSIGNED_BYTE, null /*pixels*/);
    }

    protected void loadTexImage(DrawContext dc, Bitmap bitmap) {
        try {
            // Specify the OpenGL texture 2D object's base image data (level 0).
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0 /*level*/, bitmap, 0 /*border*/);

            // If the bitmap has power-of-two dimensions, generate the texture object's image data for image levels 1
            // through level N, and configure the texture object's filtering modes to use those image levels.
            this.haveMipmaps = WWMath.isPowerOfTwo(bitmap.getWidth()) && WWMath.isPowerOfTwo(bitmap.getHeight());
            if (this.haveMipmaps) {
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            }
        } catch (Exception e) {
            // The Android utility was unable to load the texture image data.
            Logger.logMessage(Logger.ERROR, "Texture", "loadTexImage",
                "Exception attempting to load texture image \'" + bitmap + "\'", e);
        }
    }

    protected void setTexParameters(DrawContext dc) {
        if (dc.pickMode) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        } else if (this.haveMipmaps) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        } else {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }
    }

    protected static int estimateByteCount(int width, int height, int format, int type) {
        // Compute the number of bytes per row of texture image level 0. Use a default of 32 bits per pixel when either
        // of the bitmap's type or internal format are unrecognized. Adjust the width to the next highest power-of-two
        // to better estimate the memory consumed by non power-of-two images.
        int widthPow2 = WWMath.powerOfTwoCeiling(width);
        int bytesPerRow = widthPow2 * 4;
        switch (type) {
            case GLES20.GL_UNSIGNED_BYTE:
                switch (format) {
                    case GLES20.GL_ALPHA:
                        bytesPerRow = widthPow2; // 8 bits per pixel
                        break;
                    case GLES20.GL_RGB:
                        bytesPerRow = widthPow2 * 3; // 24 bits per pixel
                        break;
                    case GLES20.GL_RGBA:
                        bytesPerRow = widthPow2 * 4; // 32 bits per pixel
                        break;
                    case GLES20.GL_LUMINANCE:
                        bytesPerRow = widthPow2; // 8 bits per pixel
                        break;
                    case GLES20.GL_LUMINANCE_ALPHA:
                        bytesPerRow = widthPow2 * 2; // 16 bits per pixel
                        break;
                }
                break;
            case GLES20.GL_UNSIGNED_SHORT_5_6_5:
            case GLES20.GL_UNSIGNED_SHORT_4_4_4_4:
            case GLES20.GL_UNSIGNED_SHORT_5_5_5_1:
                bytesPerRow = widthPow2 * 2; // 16 bits per pixel
                break;
        }

        // Compute the number of bytes for the entire texture image level 0 (i.e. bytePerRow * numRows). Adjust the
        // height to the next highest power-of-two to better estimate the memory consumed by non power-of-two images.
        int heightPow2 = WWMath.powerOfTwoCeiling(height);
        int byteCount = bytesPerRow * heightPow2;

        // If the texture will have mipmaps, add 1/3 to account for the bytes used by texture image level 1 through
        // texture image level N.
        boolean isPowerOfTwo = WWMath.isPowerOfTwo(width) && WWMath.isPowerOfTwo(height);
        if (isPowerOfTwo) {
            byteCount += byteCount / 3;
        }

        return byteCount;
    }
}
