/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;

/**
 * Factory for delegating construction of bitmap images. ImageFactory provides a mechanism for World Wind components to
 * manage bitmap memory by specifying bitmaps indirectly, rather than specifying a reference to a Bitmap object. The
 * factory controls the bitmap contents, while World Wind controls the bitmap's lifecycle. This enables World Wind to
 * lazily construct bitmaps only when needed, cache those bitmaps, then release them from memory when they're no longer
 * needed. Additionally, ImageFactory enables World Wind to re-create bitmaps as needed.
 */
public interface ImageFactory {

    /**
     * Returns a bitmap for an optional image source. The returned bitmap is owned and managed by the World Wind that
     * invokes the factory. The factory must not retain a reference to the bitmap and must not recycle the bitmap.
     *
     * @param bitmapSource an optional source argument identifying the bitmap to decode, or null to return a bitmap
     *                     determined by the factory
     *
     * @return the bitmap corresponding to the image source
     */
    Bitmap createBitmap(Object bitmapSource);
}
