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

public class DrawableSurfaceTexture implements Drawable, SurfaceTexture {

    public SurfaceTextureProgram program;

    public Sector sector = new Sector();

    public Texture texture;

    public Matrix3 texCoordMatrix = new Matrix3();

    private Pool<DrawableSurfaceTexture> pool;

    public DrawableSurfaceTexture() {
    }

    public static DrawableSurfaceTexture obtain(Pool<DrawableSurfaceTexture> pool) {
        DrawableSurfaceTexture instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableSurfaceTexture().setPool(pool);
    }

    private DrawableSurfaceTexture setPool(Pool<DrawableSurfaceTexture> pool) {
        this.pool = pool;
        return this;
    }

    public DrawableSurfaceTexture set(SurfaceTextureProgram program, Sector sector, Texture texture, Matrix3 texCoordMatrix) {
        this.program = program;
        this.texture = texture;

        if (sector != null) {
            this.sector.set(sector);
        } else {
            this.sector.setEmpty();
        }

        if (texCoordMatrix != null) {
            this.texCoordMatrix.set(texCoordMatrix);
        } else {
            this.texCoordMatrix.setToIdentity();
        }

        return this;
    }

    @Override
    public void recycle() {
        this.texture = null;
        this.program = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
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
