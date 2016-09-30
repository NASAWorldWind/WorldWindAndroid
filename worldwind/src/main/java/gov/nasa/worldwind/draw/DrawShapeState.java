/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.Texture;

public class DrawShapeState {

    public static final int MAX_DRAW_ELEMENTS = 4;

    public BasicShaderProgram program;

    public BufferObject vertexBuffer;

    public BufferObject elementBuffer;

    public Vec3 vertexOrigin = new Vec3();

    public int vertexStride;

    public boolean enableCullFace = true;

    public boolean enableDepthTest = true;

    public double depthOffset;

    protected Color color = new Color();

    protected float lineWidth = 1;

    protected Texture texture;

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected VertexAttrib texCoordAttrib = new VertexAttrib();

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
        this.vertexStride = 0;
        this.enableCullFace = true;
        this.enableDepthTest = true;
        this.depthOffset = 0;
        this.color.set(1, 1, 1, 1);
        this.lineWidth = 1;
        this.texture = null;
        this.texCoordMatrix.setToIdentity();
        this.texCoordAttrib.size = 0;
        this.texCoordAttrib.offset = 0;
        this.primCount = 0;

        for (int idx = 0; idx < MAX_DRAW_ELEMENTS; idx++) {
            this.prims[idx].texture = null;
        }
    }

    public void color(Color color) {
        this.color.set(color);
    }

    public void lineWidth(float width) {
        this.lineWidth = width;
    }

    public void texture(Texture texture) {
        this.texture = texture;
    }

    public void texCoordMatrix(Matrix3 matrix) {
        this.texCoordMatrix.set(matrix);
    }

    public void texCoordAttrib(int size, int offset) {
        this.texCoordAttrib.size = size;
        this.texCoordAttrib.offset = offset;
    }

    public void drawElements(int mode, int count, int type, int offset) {
        DrawElements prim = this.prims[this.primCount++];
        prim.mode = mode;
        prim.count = count;
        prim.type = type;
        prim.offset = offset;
        prim.color.set(this.color);
        prim.lineWidth = this.lineWidth;
        prim.texture = this.texture;
        prim.texCoordMatrix.set(this.texCoordMatrix);
        prim.texCoordAttrib.size = this.texCoordAttrib.size;
        prim.texCoordAttrib.offset = this.texCoordAttrib.offset;
    }

    protected static class DrawElements {

        public int mode;

        public int count;

        public int type;

        public int offset;

        public Color color = new Color();

        public float lineWidth;

        public Texture texture;

        public Matrix3 texCoordMatrix = new Matrix3();

        public VertexAttrib texCoordAttrib = new VertexAttrib();
    }

    protected static class VertexAttrib {

        public int size;

        public int offset;
    }
}
