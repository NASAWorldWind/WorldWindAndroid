/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceTexture;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;

public class DrawableSurfaceTexture implements Drawable, SurfaceTexture {

    protected static final Pool<DrawableSurfaceTexture> pool = new SynchronizedPool<>(); // acquire and are release called in separate threads

    protected SurfaceTextureProgram program;

    protected Sector sector = new Sector();

    protected Texture texture;

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected DrawableSurfaceTexture() {
    }

    protected static DrawableSurfaceTexture obtain() {
        DrawableSurfaceTexture instance = pool.acquire(); // get an instance from the the pool
        return (instance != null) ? instance : new DrawableSurfaceTexture();
    }

    public static DrawableSurfaceTexture obtain(SurfaceTextureProgram program, Sector sector, Texture texture) {
        DrawableSurfaceTexture instance = obtain(); // get an instance from the the pool
        instance.program = program;

        if (sector != null) {
            instance.sector.set(sector);
        } else {
            instance.sector.setEmpty();
        }

        if (texture != null) {
            instance.texture = texture;
            instance.texCoordMatrix.set(texture.getTexCoordTransform());
        } else {
            instance.texCoordMatrix.setToIdentity();
        }

        return instance;
    }

    public static DrawableSurfaceTexture obtain(SurfaceTextureProgram program, Sector sector, Texture texture, Matrix3 texCoordMatrix) {
        DrawableSurfaceTexture instance = obtain(); // get an instance from the the pool
        instance.program = program;
        instance.texture = texture;

        if (sector != null) {
            instance.sector.set(sector);
        } else {
            instance.sector.setEmpty();
        }

        if (texCoordMatrix != null) {
            instance.texCoordMatrix.set(texCoordMatrix);
        } else {
            instance.texCoordMatrix.setToIdentity();
        }

        return instance;
    }

    @Override
    public void recycle() {
        this.texture = null;
        this.program = null;
        pool.release(this); // return this instance to the pool
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null) {
            return; // program unspecified
        }

        if (!this.program.useProgram(dc)) {
            return; // program failed to build
        }

        try {
            // Add this surface texture.
            this.program.addSurfaceTexture(this);

            // Add all surface textures that are contiguous in the drawable queue.
            Drawable next;
            while ((next = dc.peekDrawable()) != null && this.canBatchWith(next)) { // check if the drawable at the front of the queue can be batched
                this.program.addSurfaceTexture((SurfaceTexture) dc.pollDrawable()); // take it off the queue
            }
        } finally {
            // Draw all of the surface textures.
            this.program.draw(dc);

            // Clear the program's state.
            this.program.clear(dc);
        }
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public Matrix3 getTexCoordTransform() {
        return this.texCoordMatrix;
    }

    @Override
    public boolean bindTexture(DrawContext dc) {
        return (this.texture != null) && this.texture.bindTexture(dc);
    }

    protected boolean canBatchWith(Drawable that) {
        return this.getClass() == that.getClass() && this.program == ((DrawableSurfaceTexture) that).program;
    }
}
