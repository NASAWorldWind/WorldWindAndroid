/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawShapeState;
import gov.nasa.worldwind.draw.DrawableSurfaceShape;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.FloatArray;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.ShortArray;

public class Rectangle extends AbstractShape {

    protected static final int VERTEX_STRIDE = 6;

    /**
     * Then center position of the Rectangle
     */
    protected Location center;

    protected double height;

    protected double width;

    protected double headingDegrees;

    protected int numberEdgeIntervals = 16;

    protected int heightSegments;

    protected int widthSegments;

    protected FloatArray vertexArray = new FloatArray();

    protected ShortArray interiorElements = new ShortArray();

    protected ShortArray outlineElements = new ShortArray();

    protected Object vertexBufferKey = nextCacheKey();

    protected Object elementBufferKey = nextCacheKey();

    protected static final Location SCRATCH = new Location();

    protected static final Location START = new Location();

    protected static final Location END = new Location();

    protected static Object nextCacheKey() {
        return new Object();
    }

    public Rectangle() {
    }

    public Rectangle(ShapeAttributes attributes) {
        super(attributes);
    }

    public Rectangle(double latitude, double longitude, double width, double height, double heading) {
        this(latitude, longitude, width, height, heading, null);
    }

    public Rectangle(double latitude, double longitude, double width, double height, double headingDegrees, ShapeAttributes attributes) {
        super(attributes);
        if (latitude > 90 || latitude < -90) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "constructor", "invalid latitude"));
        }

        if (longitude > 180 || longitude < -180) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "constructor", "invalid longitude"));
        }

        if (width < 0 || height < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "constructor", "width and height must be positive"));
        }

        this.center = new Location(latitude, longitude);
        this.width = width;
        this.height = height;
        this.headingDegrees = headingDegrees;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(double latitude, double longitude) {
        if (this.center == null) {
            this.center = new Location(latitude, longitude);
        } else {
            this.center.set(latitude, longitude);
        }
        this.reset();
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(double height) {
        if (height < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "setheight", "height must be greater than zero"));
        }
        this.height = height;
        this.reset();
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width) {
        if (width < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Rectangle", "setwidth", "width must be greater than zero"));
        }
        this.width = width;
        this.reset();
    }

    public double getHeadingDegrees() {
        return this.headingDegrees;
    }

    public void setHeadingDegrees(double headingDegrees) {
        this.headingDegrees = headingDegrees;
        this.reset();
    }

    protected void reset() {
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.center == null || this.height == 0 || this.width == 0) {
            return; // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc);
            this.assembleElements(rc);
            this.vertexBufferKey = nextCacheKey();
            this.elementBufferKey = nextCacheKey();
        }

        // Obtain a drawable form the render context pool.
        DrawableSurfaceShape drawable;
        DrawShapeState drawState;
        Pool<DrawableSurfaceShape> pool = rc.getDrawablePool(DrawableSurfaceShape.class);
        drawable = DrawableSurfaceShape.obtain(pool);
        drawState = drawable.drawState;
        drawable.sector.set(this.boundingSector);


        // Use the basic GLSL program to draw the shape.
        drawState.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawState.program == null) {
            drawState.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(this.vertexBufferKey);
        if (drawState.vertexBuffer == null) {
            int size = this.vertexArray.size() * 4;
            FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(this.vertexArray.array(), 0, this.vertexArray.size());
            drawState.vertexBuffer = new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.vertexBufferKey, drawState.vertexBuffer);
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(this.elementBufferKey);
        if (drawState.elementBuffer == null) {
            int size = (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2);
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.interiorElements.array(), 0, this.interiorElements.size());
            buffer.put(this.outlineElements.array(), 0, this.outlineElements.size());
            drawState.elementBuffer = new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.elementBufferKey, drawState.elementBuffer);
        }

        this.drawInterior(rc, drawState);
        this.drawOutline(rc, drawState);

        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.
        drawState.vertexStride = VERTEX_STRIDE * 4; // stride in bytes
        drawState.enableCullFace = false;
        drawState.enableDepthTest = this.activeAttributes.depthTest;

        // Enqueue the drawable for processing on the OpenGL thread.
        rc.offerSurfaceDrawable(drawable, 0 /*zOrder*/);
    }

    protected void drawInterior(RenderContext rc, DrawShapeState drawState) {
        if (!this.activeAttributes.drawInterior) {
            return;
        }

        drawState.texture(null);

        // Configure the drawable to display the shape's interior.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
        drawState.texCoordAttrib(2 /*size*/, 12 /*offset in bytes*/);
        drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, this.interiorElements.size(),
            GLES20.GL_UNSIGNED_SHORT, 0 /*offset*/);
    }

    protected void drawOutline(RenderContext rc, DrawShapeState drawState) {
        if (!this.activeAttributes.drawOutline) {
            return;
        }

        drawState.texture(null);

        // Configure the drawable to display the shape's outline.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
        drawState.lineWidth(this.activeAttributes.outlineWidth);
        drawState.texCoordAttrib(1 /*size*/, 20 /*offset in bytes*/);
        drawState.drawElements(GLES20.GL_LINE_LOOP, this.outlineElements.size(),
            GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2 /*offset*/);
    }

    protected boolean mustAssembleGeometry(RenderContext rc) {
        return this.vertexArray.size() == 0;
    }

    protected void assembleGeometry(RenderContext rc) {

        // Clear the shape's vertex array. This array will accumulate values as the shapes's
        // geometry is assembled.
        this.vertexArray.clear();

        // Attempt to distribute the edge intervals based on the aspect ratio of the rectangle
        double ratio = this.height / this.width;
        this.widthSegments = Math.min(this.numberEdgeIntervals - 2, Math.max(2, (int) ((this.numberEdgeIntervals / 2) / ratio)));
        this.heightSegments = this.numberEdgeIntervals - this.widthSegments;

        // Calculate the width into radians
        double widthRadians = this.width / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
        double heightRadians = this.height / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);

        // Start in the top left corner and work left to right and then down.
        double widthStep = 1.0 / this.widthSegments;
        double heightStep = 1.0 / this.heightSegments;
        for (int j = 0; j <= this.heightSegments; j++) {
            // Transit from the center vertically for each pass
            this.center.greatCircleLocation(this.headingDegrees, heightRadians / 2 - j * heightStep * heightRadians, SCRATCH);
            // Start from the "negative" side (furtherest west if no heading is applied)
            SCRATCH.greatCircleLocation(this.headingDegrees - 90.0, widthRadians / 2, START);
            // End at the "positive" side
            SCRATCH.greatCircleLocation(this.headingDegrees + 90.0, widthRadians / 2, END);
            // Sample from negative to positive along the path
            for (int i = 0; i <= this.widthSegments; i++) {
                START.interpolateAlongPath(END, WorldWind.GREAT_CIRCLE, i * widthStep, SCRATCH);
                this.addVertex(rc, SCRATCH.latitude, SCRATCH.longitude, 0);
            }
        }

        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
        this.boundingSector.setEmpty();
        this.boundingSector.union(this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE);
        this.boundingBox.setToUnitBox(); // Surface/geographic shape bounding box is unused
    }

    protected void assembleElements(RenderContext rc) {
        // Generate interior element buffer
        for (int j = 0; j < this.heightSegments; j++) {
            for (int i = 0; i <= this.widthSegments; i++) {
                this.interiorElements.add((short) (j * (this.widthSegments + 1) + i));
                this.interiorElements.add((short) ((j + 1) * (this.widthSegments + 1) + i));
            }
            // add the degenerate triangles to wrap the strips
            if (j != (this.heightSegments - 1)) {
                this.interiorElements.add((short) ((j + 1) * (this.widthSegments + 1) + this.widthSegments));
                this.interiorElements.add((short) ((j + 1) * (this.widthSegments + 1)));
            }
        }

        // Generate outline element buffer
        // Top - left to right
        for (int i = 0; i < this.widthSegments; i++) {
            this.outlineElements.add((short) i);
        }
        // Right - top to bottom
        for (int i = 0; i < this.heightSegments; i++) {
            this.outlineElements.add((short) (i * (this.widthSegments + 1) + this.widthSegments));
        }
        // Bottom - right to left
        int elements = (this.heightSegments + 1) * (this.widthSegments + 1);
        for (int i = 0; i < this.widthSegments; i++) {
            this.outlineElements.add((short) --elements);
        }
        // Left - bottom to top
        for (int i = this.heightSegments; i > 0; i--) {
            this.outlineElements.add((short) (i * (this.widthSegments + 1)));
        }
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude) {
        this.vertexArray.add((float) longitude);
        this.vertexArray.add((float) latitude);
        this.vertexArray.add((float) altitude);
        // reserved for future texture coordinate use
        this.vertexArray.add(0);
        this.vertexArray.add(0);
        this.vertexArray.add(0);
    }
}
