/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.Color;

public class DrawShapeState {

    public static final int MAX_DRAW_ELEMENTS = 4;

    public BasicShaderProgram program;

    public BufferObject vertexBuffer;

    public BufferObject elementBuffer;

    public Vec3 vertexOrigin = new Vec3();

    public boolean enableCullFace = true;

    public boolean enableDepthTest = true;

    protected Color color = new Color();

    protected float lineWidth = 1;

    protected int primCount;

    protected DrawElements[] prims = new DrawElements[MAX_DRAW_ELEMENTS];

    public DrawShapeState() {
        for (int idx = 0; idx < MAX_DRAW_ELEMENTS; idx++) {
            this.prims[idx] = new DrawElements();
        }
    }

    public void reset() {
        this.program = null;
        this.vertexBuffer = null;
        this.elementBuffer = null;
        this.vertexOrigin.set(0, 0, 0);
        this.color.set(1, 1, 1, 1);
        this.enableCullFace = true;
        this.enableDepthTest = true;
        this.lineWidth = 1;
        this.primCount = 0;
    }

    public void color(Color color) {
        this.color.set(color);
    }

    public void lineWidth(float width) {
        this.lineWidth = width;
    }

    public void drawElements(int mode, int count, int type, int offset) {
        DrawElements prim = this.prims[this.primCount++];
        prim.mode = mode;
        prim.count = count;
        prim.type = type;
        prim.offset = offset;
        prim.color.set(this.color);
        prim.lineWidth = this.lineWidth;
    }

    protected static class DrawElements {

        public int mode;

        public int count;

        public int type;

        public int offset;

        public Color color = new Color();

        public float lineWidth;
    }
}
