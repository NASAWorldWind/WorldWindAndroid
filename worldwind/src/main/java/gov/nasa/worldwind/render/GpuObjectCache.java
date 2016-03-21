/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;

import java.lang.reflect.Constructor;

import gov.nasa.worldwind.cache.LruMemoryCache;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

// TODO consider rewriting to use SparseArray, generate int cache keys
public class GpuObjectCache extends LruMemoryCache<Object, GpuObject> {

    public GpuObjectCache(int capacity, int lowWater) {
        super(capacity, lowWater);
    }

    public GpuObject put(Object key, GpuObject value) {
        if (key == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuObjectCache", "put", "missingKey"));
        }

        if (value == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuObjectCache", "put", "missingValue"));
        }

        int size = value.getObjectSize();
        if (size < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuObjectCache", "put", "invalidSize"));
        }

        return super.put(key, value, size);
    }

    public GpuProgram retrieveProgram(DrawContext dc, Class<? extends GpuProgram> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GpuObjectCache", "retrieveProgram", "invalidClass"));
        }

        String key = clazz.getName();
        GpuProgram program = (GpuProgram) this.get(key);
        if (program != null) {
            return program;
        }

        // TODO read in a separate thread, handle absent resources
        // TODO constructor based pattern is brittle and difficult to document
        // TODO programs can be evicted during rendering when the cache is thrashing
        try {
            Constructor<? extends GpuProgram> constructor = clazz.getConstructor(DrawContext.class);
            program = constructor.newInstance(dc);
            this.put(key, program);
            return program;
        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "GpuObjectCache", "retrieveProgram", "invalidClass", e);
            return null;
        }
    }

    public GpuTexture retrieveTexture(DrawContext dc, @DrawableRes int id) {
        Integer key = id;
        GpuTexture texture = (GpuTexture) this.get(key);
        if (texture != null) {
            return texture;
        }

        // TODO read in a separate thread, handle absent resources
        Bitmap bitmap = WWUtil.readResourceAsBitmap(dc.getContext(), id);
        if (bitmap != null) {
            texture = new GpuTexture(bitmap);
            this.put(key, texture);
            return texture;
        } else {
            Logger.logMessage(Logger.ERROR, "GpuObjectCache", "retrieveTexture", "invalidResource");
            return null;
        }
    }

    @Override
    protected GpuObject entryRemoved(Entry<Object, GpuObject> entry) {
        // TODO dispose resources explicitly at end of frame
        entry.value.dispose(); // need to explicitly free Gpu resources associated with this cache entry
        return super.entryRemoved(entry);
    }
}
