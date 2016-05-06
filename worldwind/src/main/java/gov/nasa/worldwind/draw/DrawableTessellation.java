/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import android.opengl.GLES20;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Terrain;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.SynchronizedPool;

public class DrawableTessellation implements Drawable {

    protected static final Pool<DrawableTessellation> pool = new SynchronizedPool<>(); // acquire and are release called in separate threads

    protected Matrix4 offsetMvpMatrix = new Matrix4();

    protected Matrix4 mvpMatrix = new Matrix4();

    protected BasicShaderProgram program;

    protected Color color = new Color();

    protected DrawableTessellation() {
    }

    public static DrawableTessellation obtain(BasicShaderProgram program, Color color) {
        DrawableTessellation instance = pool.acquire(); // get an instance from the the pool
        if (instance == null) {
            instance = new DrawableTessellation();
        }

        instance.program = program;

        if (color != null) {
            instance.color.set(color);
        } else {
            instance.color.set(1, 1, 1, 1); // white
        }

        return instance;
    }

    @Override
    public void recycle() {
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

        // Configure the program to draw opaque white fragments.
        this.program.enableTexture(false);
        this.program.loadColor(this.color);

        // Suppress writes to the OpenGL depth buffer.
        GLES20.glDepthMask(false);

        // Compute the portion of the modelview projection matrix that remains constant for each tile.
        this.offsetMvpMatrix.set(dc.projection);
        this.offsetMvpMatrix.offsetProjectionDepth(-1.0e-3); // offset this layer's depth values toward the eye
        this.offsetMvpMatrix.multiplyByMatrix(dc.modelview);

        Terrain terrain = dc.terrain;
        for (int idx = 0, len = terrain.getTileCount(); idx < len; idx++) {

            // Use the draw context's modelview projection matrix, transformed to the terrain tile's local coordinates.
            Vec3 terrainOrigin = terrain.getTileVertexOrigin(idx);
            this.mvpMatrix.set(this.offsetMvpMatrix);
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            program.loadModelviewProjection(this.mvpMatrix);

            // Use the terrain tile's vertex point attribute.
            terrain.useVertexPointAttrib(dc, idx, 0);

            // Draw the terrain tile vertices as lines.
            terrain.drawTileLines(dc, idx);
        }

        // Restore default World Wind OpenGL state.
        GLES20.glDepthMask(true);
    }
}
