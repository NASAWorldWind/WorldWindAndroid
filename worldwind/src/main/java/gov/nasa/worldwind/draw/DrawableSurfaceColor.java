/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.util.Pool;

public class DrawableSurfaceColor implements Drawable {

    public BasicShaderProgram program;

    public Color color = new Color();

    private Matrix4 mvpMatrix = new Matrix4();

    private Pool<DrawableSurfaceColor> pool;

    public DrawableSurfaceColor() {
    }

    public static DrawableSurfaceColor obtain(Pool<DrawableSurfaceColor> pool) {
        DrawableSurfaceColor instance = pool.acquire(); // get an instance from the pool
        return (instance != null) ? instance.setPool(pool) : new DrawableSurfaceColor().setPool(pool);
    }

    private DrawableSurfaceColor setPool(Pool<DrawableSurfaceColor> pool) {
        this.pool = pool;
        return this;
    }

    @Override
    public void recycle() {
        this.program = null;

        if (this.pool != null) { // return this instance to the pool
            this.pool.release(this);
            this.pool = null;
        }
    }

    @Override
    public void draw(DrawContext dc) {
        if (this.program == null || !this.program.useProgram(dc)) {
            return; // program unspecified or failed to build
        }

        // Configure the program to draw the specified color.
        this.program.enableTexture(false);
        this.program.loadColor(this.color);

        for (int idx = 0, len = dc.getDrawableTerrainCount(); idx < len; idx++) {
            // Get the drawable terrain associated with the draw context.
            DrawableTerrain terrain = dc.getDrawableTerrain(idx);

            // Use the terrain's vertex point attribute.
            if (!terrain.useVertexPointAttrib(dc, 0 /*vertexPoint*/)) {
                continue; // vertex buffer failed to bind
            }

            // Use the draw context's modelview projection matrix, transformed to terrain local coordinates.
            Vec3 terrainOrigin = terrain.getVertexOrigin();
            this.mvpMatrix.set(dc.modelviewProjection);
            this.mvpMatrix.multiplyByTranslation(terrainOrigin.x, terrainOrigin.y, terrainOrigin.z);
            this.program.loadModelviewProjection(this.mvpMatrix);

            // Draw the terrain as triangles.
            terrain.drawTriangles(dc);
        }
    }
}
