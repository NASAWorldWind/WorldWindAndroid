/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.LruMemoryCache;

public class GpuObjectCache extends LruMemoryCache<Object, GpuObject> {

    protected List<GpuObject> disposalQueue = new ArrayList<>();

    public GpuObjectCache(int capacity) {
        super(capacity);
    }

    public GpuObjectCache(int capacity, int lowWater) {
        super(capacity, lowWater);
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

        // TODO remove this method, refactor GpuProgram like GpuTexture
        // TODO read in a separate thread, handle absent resources
        // TODO constructor based pattern is brittle and difficult to document

        try {
            Constructor<? extends GpuProgram> constructor = clazz.getConstructor(DrawContext.class);
            program = constructor.newInstance(dc);
            this.put(key, program, program.programSize);
            return program;
        } catch (Exception e) {
            Logger.log(Logger.ERROR, "Exception creating GpuProgram from class \'" + clazz + "\'", e);
            return null;
        }
    }

    public void disposeEvictedObjects(DrawContext dc) {

        for (GpuObject object : this.disposalQueue) {
            try {
                object.dispose(dc);
                Logger.log(Logger.INFO, "Disposed GPU object \'" + object + "\'");
            } catch (Exception e) {
                Logger.log(Logger.ERROR, "Exception disposing GPU object \'" + object + "\'", e);
            }
        }

        this.disposalQueue.clear();
    }

    @Override
    protected void entryRemoved(Object key, GpuObject value) {
        // Explicitly free GPU objects associatd with the cache entry. We collect evicted GPU objects here and dispose
        // them at the end of a frame in disposeEvictedObjects. This avoids unexpected side effects like GPU programs
        // being evicted while in use, which can occur when this cache is too small and thrashes during a frame.
        this.disposalQueue.add(value);
    }
}
