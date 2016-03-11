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

public class GpuTexture implements GpuObject {

    protected int textureId;

    public GpuTexture(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "constructor", "invalidBitmap"));
        }

        int[] newTexture = new int[1];
        int[] prevTexture = new int[1];

        GLES20.glGenTextures(1, newTexture, 0);
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, prevTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, newTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, prevTexture[0]);

        this.textureId = newTexture[0];
    }

    @Override
    public int getObjectId() {
        return textureId;
    }

    @Override
    public void dispose() {
        if (this.textureId != 0) {
            int[] texture = {this.textureId};
            GLES20.glDeleteTextures(GLES20.GL_TEXTURE_2D, texture, 0);
            this.textureId = 0;
        }
    }

    public Matrix3 applyTextureTransform(Matrix3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuTexture", "applyTexTransform", "nullResult"));
        }

        return result.multiplyByVerticalFlip();
    }
}
