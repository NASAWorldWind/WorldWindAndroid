/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.lang.reflect.Constructor;

import gov.nasa.worldwind.cache.LruMemoryCache;
import gov.nasa.worldwind.util.Logger;

public class GpuObjectCache extends LruMemoryCache<Object, GpuObject> {

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

    @Override
    protected void entryRemoved(Object key, GpuObject value) {
        // TODO programs can be evicted during rendering when the cache is thrashing
        // TODO this is almost certainly unexpected, could we dispose Gpu objects at the end of a frame?
        value.dispose(); // need to explicitly free Gpu resources associated with this cache entry
        Logger.log(Logger.INFO, "GpuObject removed from cache {key=" + key + ", value=" + value + "}" +
            " capacity=" + this.capacity + ", usedCapacity=" + this.usedCapacity);
    }
}
